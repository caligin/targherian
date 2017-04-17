package tech.anima.targherian;

import android.provider.BaseColumns;

public interface Contract {
    interface VehicleEntry extends BaseColumns {
        String TABLE_NAME = "vehicles";
        String LICENSE_PLATE_COLUMN = "license_plate";
        String NAME_COLUMN = "name";
        String MODEL_COLUMN = "model";
        String VEHICLE_REGISTRATION_URI_COLUMN = "vehicle_registration_uri";


        String TEXT_TYPE = " TEXT";
        String COMMA_SEP = ",";
        String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        LICENSE_PLATE_COLUMN + TEXT_TYPE + COMMA_SEP +
                        NAME_COLUMN + TEXT_TYPE + COMMA_SEP +
                        MODEL_COLUMN + TEXT_TYPE + COMMA_SEP +
                        VEHICLE_REGISTRATION_URI_COLUMN + TEXT_TYPE + " )";
        // TODO: ewww what about duplicates? should something be uniq?
    }
}
