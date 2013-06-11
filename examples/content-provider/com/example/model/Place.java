package com.example.model;
 
import android.content.ContentValues;
import android.database.Cursor;
import com.example.database.tables.PlaceTable;
 
public class Place {
|
    private long mId;
 
    private String mAddress; 
  
    private ContentValues mValues = new ContentValues();
 
    public Place(final Cursor cursor) {
        setId(cursor.getLong(cursor.getColumnIndex(PlaceTable.ID)));
        setAddress(cursor.getString(cursor.getColumnIndex(PlaceTable.ADDRESS))); 
    }
 
    /* Auto generatted setters */
    public void setAddress(String address) {
        mAddress = address;
        mValues.put(PlaceTable.ADDRESS, address);
    }
  
    /* Auto generated getters */
    public long getId() {
        return mId;
    }
 
    public String getAddress {
        return mAddress;
    }
   
    public ContentValues getContentValues() {
        return mValues;
    }
}