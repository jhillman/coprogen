package com.example.database;
 
import com.example.database.table.*;
 
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
 
import android.util.Log;
 
public class PeopleAndPlacesDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "people_and_places.db";
    private static final int DATABASE_VERSION = 1;    private static final String PRAGMA_FOREIGN_KEY_SCRIPT = "PRAGMA foreign_keys = ON;"; 
    public static final String TAG = "PeopleAndPlacesDatabase";
 
    private final Context mContext;
 
    public PeopleAndPlacesDatabase(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }
 
    @Override
    public final void onCreate(final SQLiteDatabase db) {
        db.execSQL(PersonTable.SQL_CREATE);
 
        db.execSQL(PlaceTable.SQL_CREATE);
  
        initialize(db);
   }
  
    @Override
    public final void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        upgrade(db, oldVersion, newVersion);
    }
 
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL(PRAGMA_FOREIGN_KEY_SCRIPT);
    }
  
    private void dropTablesAndCreate(final SQLiteDatabase db) {
        db.execSQL(PersonTable.SQL_DROP);
 
        db.execSQL(PlaceTable.SQL_DROP);
   
        onCreate(db);
    }
 
    // BEGIN PERSISTED SECTION - put custom methods here
    // you may change the contents of these methods, but do not rename/remove them
    private void upgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        Log.e(TAG, "Updating database from version " + oldVersion + " to " + newVersion + ".");
        dropTablesAndCreate(db);
    }
 
    private void initialize(final SQLiteDatabase db) {
    }
    // END PERSISTED SECTION
}