package com.example.model;
 
import android.content.ContentValues;
import android.database.Cursor;
import com.example.database.table.PlaceTable;
 
public class Place {
    private long mRowId;
 
    private String mAddress; 
  
    private ContentValues mValues = new ContentValues();
 
    public Place() {}
 
    public Place(final Cursor cursor) {
        this(cursor, false);
    }
 
    public Place(final Cursor cursor, boolean prependTableName) {
        String prefix = prependTableName ? PlaceTable.TABLE_NAME + "_" : "";
        setRowId(cursor.getLong(cursor.getColumnIndex(prefix + PlaceTable._ID)));
        setAddress(cursor.getString(cursor.getColumnIndex(prefix + PlaceTable.ADDRESS))); 
    }
 
    public void setRowId(long _id) {
        mRowId = _id;
        mValues.put(PlaceTable._ID, _id);
    }
 
    public void setAddress(String address) {
        mAddress = address;
        mValues.put(PlaceTable.ADDRESS, address);
    }
  
    public long getRowId() {
        return mRowId;
    }
 
    public String getAddress() {
        return mAddress;
    }
   
    public ContentValues getContentValues() {
        return mValues;
    }
}