package tech.anima.targherian;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import static android.app.SearchManager.SUGGEST_COLUMN_TEXT_1;
import static android.app.SearchManager.SUGGEST_COLUMN_TEXT_2;
import static android.app.SearchManager.SUGGEST_URI_PATH_QUERY;

public class Provider extends ContentProvider {

    private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final String AUTHORITY = "tech.anima.targherian.provider";
    private static final String TABLE = Contract.VehicleEntry.TABLE_NAME;
    private static final int SUGGESTION_REQUEST = 1;
    private static final String[] SEARCH_SUGGESTIONS_PROJECTION = {
            Contract.VehicleEntry._ID,
            Queries.aliased(Contract.VehicleEntry.NAME_COLUMN, SUGGEST_COLUMN_TEXT_1),
            Queries.aliased(Contract.VehicleEntry.LICENSE_PLATE_COLUMN, SUGGEST_COLUMN_TEXT_2)
    };

    static {
        matcher.addURI(AUTHORITY, SUGGEST_URI_PATH_QUERY + "/*", SUGGESTION_REQUEST);
    }

    private DatabaseOpener dbOpener;
    private SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        dbOpener = new DatabaseOpener(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] _projection, String _selection, String[] _selectionArgs, String _sortOrder) {
        if (matcher.match(uri) != SUGGESTION_REQUEST) {
            return null;
        }
        db = dbOpener.getWritableDatabase(); // TODO not if notnull, cache

        final String partialText = uri.getLastPathSegment().toLowerCase();
        return Queries.entriesByNamePartialMatch(db, partialText, SEARCH_SUGGESTIONS_PROJECTION);
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + TABLE;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
