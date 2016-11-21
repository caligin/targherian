package tech.anima.targherian;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String PROVIDER_AUTHORITY = "tech.anima.targherian.imageprovider";
    private final TargherianDatabaseOpener dbOpener = new TargherianDatabaseOpener(this);
    private final int defaultPictureButtonIcon = android.R.drawable.ic_menu_camera;
    private ImageButton pictureInput = null;
    private Button confirmAdd = null;
    private File photoFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (pictureInput == null) {
            pictureInput = (ImageButton) findViewById(R.id.pictureInput);
            pictureInput.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dispatchTakePictureIntent();
                }
            });
        }
        if (confirmAdd == null) {
            confirmAdd = (Button) findViewById(R.id.confirmAdd);
            confirmAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    storeEntry();
                }
            });
        }
    }

    private void dispatchTakePictureIntent() {
        final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final ComponentName cameraActivity = takePictureIntent.resolveActivity(getPackageManager());
        if (cameraActivity == null) {
            Log.wtf(TAG, "camera unavailable");
            throw new IllegalStateException("camera unavailable");
        }
        try {
            photoFile = createImageFile(); //has to be a field b/c the result Intent is null instead of reflecting the url
            final Uri photoURI = FileProvider.getUriForFile(this, PROVIDER_AUTHORITY, photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            final int permissionFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
            grantUriPermission(cameraActivity.getPackageName(), photoURI, permissionFlags);
            takePictureIntent.setFlags(permissionFlags);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_IMAGE_CAPTURE) {
            Log.wtf(TAG, "received activity result for unknown code");
            return;
        }
        handleCameraResult(resultCode, data);
    }

    private void handleCameraResult(int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                final Bitmap imageBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                pictureInput.setImageBitmap(imageBitmap);
                break;
            case RESULT_CANCELED:
                //do nothing
                break;
            default:
                Log.wtf(TAG, "unknown resultcode for camera result");
        }
    }

    private File createImageFile() throws IOException {
        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        final String namePrefix = "tgn_" + timeStamp + "_";
        final File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        final File image = File.createTempFile(namePrefix, ".jpg", storageDir);
        if (image == null) {
            throw new IllegalStateException("Got null image form createTempFile"); //tbh, can this even happen?
        }
        return image;
    }

    private void storeEntry() {
        //grab dataz
        final EditText licensePlateInput = (EditText) findViewById(R.id.licensePlateInput);
        final String licensePlate = licensePlateInput.getText().toString();
        final EditText nameInput = (EditText) findViewById(R.id.nameInput);
        final String name = nameInput.getText().toString();
        // TODO + model
        //validate
        if (licensePlate.trim().isEmpty() && name.trim().isEmpty()) {
            // TODO: license can have moar validation to see if it's a valid one
            // TODO: is it true that either is enough or do we always want the license?
            Toast.makeText(this, "Must provide either a license number or a name", Toast.LENGTH_LONG);
            return;
        }
        if (photoFile == null) {
            Toast.makeText(this, "Must take a picture of the vehicle registration", Toast.LENGTH_LONG);
            return;
        }
        //store
        final SQLiteDatabase db = dbOpener.getWritableDatabase();
        final ContentValues values = new ContentValues();
        values.put(TargherianContract.VehicleEntry.LICENSE_PLATE_COLUMN, licensePlate);
        values.put(TargherianContract.VehicleEntry.NAME_COLUMN, name);
        values.put(TargherianContract.VehicleEntry.VEHICLE_REGISTRATION_URI_COLUMN, photoFile.getAbsolutePath());
        final long newLineId = db.insert(TargherianContract.VehicleEntry.TABLE_NAME, null, values);// TODO: related to the note about uniq, there are alternative calls to insert, read docs!
        if (newLineId == -1) {
            // TODO handle error?
            Log.e(TAG, "something went wrong with db insert, insert returned -1");
            Toast.makeText(this, "Failed to save", Toast.LENGTH_LONG);
            return;
        }
        //reset
        licensePlateInput.getText().clear();
        nameInput.getText().clear();
        pictureInput.setImageResource(defaultPictureButtonIcon);
    }
}



