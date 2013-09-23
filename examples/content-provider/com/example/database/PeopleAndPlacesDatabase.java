package com.example.database;
 
import com.example.database.table.*;
 
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
 
public class PeopleAndPlacesDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "people_and_places.db";
    private static final int DATABASE_VERSION = 1;
 
    public PeopleAndPlacesDatabase(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    @Override
    public final void onCreate(final SQLiteDatabase db) {
        db.execSQL(PersonTable.SQL_CREATE);
 
        db.execSQL(PlaceTable.SQL_CREATE);
    }
  
    @Override
    public final void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        upgrade(db, oldVersion, newVersion);
    }
  
    private final void dropTablesAndCreate(final SQLiteDatabase db) {
        db.execSQL(PersonTable.SQL_DROP);
 
        db.execSQL(PlaceTable.SQL_DROP);
   
        onCreate(db);
    }
 
    // BEGIN PERSISTED SECTION - put custom methods here
    // you may change the contents of this method, but do not rename/remove it
    private void upgrade(db, final int oldVersion, final int newVersion) {
        dropTablesAndCreate(db);
    }
    // END PERSISTED SECTION
}