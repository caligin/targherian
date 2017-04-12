package tech.anima.targherian;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.io.File;

public class SearchResultsActivity extends ListActivity {

    private static final String TAG = SearchResultsActivity.class.getSimpleName();
    private final TargherianDatabaseOpener dbOpener = new TargherianDatabaseOpener(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO: learn more about android:launchMode="singleTop"
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (!Intent.ACTION_SEARCH.equals(intent.getAction())) {
            Log.wtf(TAG, "received unrecognized intent action");
            throw new IllegalArgumentException("received unrecognized intent action");
        }
        final String query = getQuery(intent);
        //TODO: do this stuff async with a loadermanager
        final Cursor nameMatches = getNameMatches(query);

        final String[] cursorFieldNames = {
                TargherianContract.VehicleEntry.LICENSE_PLATE_COLUMN,
                TargherianContract.VehicleEntry.NAME_COLUMN
        };
        final int[] viewFieldIds = {
                android.R.id.text1,
                android.R.id.text2
        };
        this.setListAdapter(new SimpleCursorAdapter(this, android.R.layout.two_line_list_item, nameMatches, cursorFieldNames, viewFieldIds, 0));
    }

    private String getQuery(Intent intent) {
        final String normalSearchQuery = intent.getStringExtra(SearchManager.QUERY);
        if (normalSearchQuery != null) {
            return normalSearchQuery;

        }
        return intent.getCharSequenceExtra("user_query").toString(); //wtf idk why but when using the hints it comes as a charsequence on a different key...
    }

    public Cursor getNameMatches(String query) {

        final Cursor cursor = Queries.entriesByNamePartialMatch(dbOpener.getReadableDatabase(), query);

        if (cursor == null) {
            return null;
        }
        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        final String selection = TargherianContract.VehicleEntry._ID + " = ?";
        final String[] projection = new String[]{TargherianContract.VehicleEntry.VEHICLE_REGISTRATION_URI_COLUMN};
        final String[] selectionArgs = new String[]{Long.toString(id)};
        final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(TargherianContract.VehicleEntry.TABLE_NAME);
        final Cursor cursor = builder.query(dbOpener.getReadableDatabase(), projection, selection, selectionArgs, null, null, null);
        if (cursor == null || !cursor.moveToFirst()) {
            throw new IllegalStateException("query by id failed");
        }
        final String picturePath = cursor.getString(0);
        final Uri pictureUri = FileProvider.getUriForFile(this, MainActivity.PROVIDER_AUTHORITY, new File(picturePath));
        final Intent viewImageIntent = new Intent();
        viewImageIntent.setAction(Intent.ACTION_VIEW);
        viewImageIntent.setDataAndType(pictureUri, "image/*");
        final int permissionFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
        final ComponentName galleryActivity = viewImageIntent.resolveActivity(getPackageManager());
        grantUriPermission(galleryActivity.getPackageName(), pictureUri, permissionFlags);
        viewImageIntent.setFlags(permissionFlags);
        startActivity(viewImageIntent);

    }
}
