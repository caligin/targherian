package tech.anima.targherian;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String PROVIDER_AUTHORITY = "tech.anima.targherian.imageprovider";
    private ImageButton pictureInput = null;
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
        if(!storageDir.exists()){
            storageDir.mkdirs();
        }
        final File image = File.createTempFile(namePrefix, ".jpg", storageDir);
        if (image == null) {
            throw new IllegalStateException("Got null image form createTempFile"); //tbh, can this even happen?
        }
        return image;
    }
}



