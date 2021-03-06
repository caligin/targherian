package tech.anima.targherian;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class Queries {

    public static Cursor entriesByNamePartialMatch(SQLiteDatabase db, String partialName) {
        return entriesByNamePartialMatch(db, partialName, null);
    }

    public static Cursor entriesByNamePartialMatch(SQLiteDatabase db, String partialName, String[] projection) {
        final String selection = Contract.VehicleEntry.NAME_COLUMN + " LIKE ?";
        final String[] selectionArgs = new String[]{"%" + partialName + "%"};
        return db.query(Contract.VehicleEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
    }

    public static Cursor byId(SQLiteDatabase db, long id, String[] projection) {
        final String selection = Contract.VehicleEntry._ID + " = ?";
        final String[] selectionArgs = new String[]{Long.toString(id)};
        return db.query(Contract.VehicleEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
    }

    public static String aliased(String column, String alias) {
        return column + " as " + alias;
    }
}
