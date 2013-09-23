package com.example.model;
 
import android.content.ContentValues;
import android.database.Cursor;
 
import android.os.Parcel;
import android.os.Parcelable;
   
import com.example.database.table.PersonTable;
 
import java.util.ArrayList;
import java.util.List;
 
public class Person implements Parcelable {
    private long mRowId;
    private String mName; 
    private int mAge; 
    private boolean mAlive; 
    private double mBodyFat; 
  
    private ContentValues mValues = new ContentValues();
 
    public Person() {}
 
    public Person(final Cursor cursor) {
        this(cursor, false);
    }
 
    public Person(final Cursor cursor, boolean prependTableName) {
        String prefix = prependTableName ? PersonTable.TABLE_NAME + "_" : "";
        setRowId(cursor.getLong(cursor.getColumnIndex(prefix + PersonTable._ID)));
        setName(cursor.getString(cursor.getColumnIndex(prefix + PersonTable.NAME))); 
        setAge(cursor.getInt(cursor.getColumnIndex(prefix + PersonTable.AGE))); 
        setAlive(cursor.isNull(cursor.getColumnIndex(prefix + PersonTable.ALIVE)) ? false : cursor.getInt(cursor.getColumnIndex(prefix + PersonTable.ALIVE)) != 0); 
        setBodyFat(cursor.getDouble(cursor.getColumnIndex(prefix + PersonTable.BODY_FAT))); 
    }
 
    public Person(Parcel parcel) {
        mRowId = parcel.readLong();
 
        setName(parcel.readString()); 
 
        setAge(parcel.readInt()); 
 
        setAlive(parcel.readInt() == 0); 
 
        setBodyFat(parcel.readDouble()); 
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
 
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(mRowId);
 
        parcel.writeString(getName()); 
 
        parcel.writeInt(getAge()); 
 
        parcel.writeInt(getAlive() ? 1 : 0); 
 
        parcel.writeDouble(getBodyFat()); 
    }
 
    public static final Creator<Person> CREATOR = new Creator<Person>() {
        public Person createFromParcel(Parcel in) {
            return new Person(in);
        }
 
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };
  
    public void setRowId(long _id) {
        mRowId = _id;
        mValues.put(PersonTable._ID, _id);
    }
 
    public void setName(String name) {
        mName = name;
        mValues.put(PersonTable.NAME, name);
    }
 
    public void setAge(int age) {
        mAge = age;
        mValues.put(PersonTable.AGE, age);
    }
 
    public void setAlive(boolean alive) {
        mAlive = alive;
        mValues.put(PersonTable.ALIVE, alive);
    }
 
    public void setBodyFat(double bodyFat) {
        mBodyFat = bodyFat;
        mValues.put(PersonTable.BODY_FAT, bodyFat);
    }
  
    public long getRowId() {
        return mRowId;
    }
 
    public String getName() {
        return mName;
    }
 
    public int getAge() {
        return mAge;
    }
 
    public boolean getAlive() {
        return mAlive;
    }
 
    public double getBodyFat() {
        return mBodyFat;
    }
   
    public ContentValues getContentValues() {
        return mValues;
    }
  
    public static List<Person> listFromCursor(Cursor cursor) {
        List<Person> list = new ArrayList<Person>();
 
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(new Person(cursor));
            } while (cursor.moveToNext());
        }
 
        return list;
    }
 
    // BEGIN PERSISTED SECTION - put custom methods here

    // END PERSISTED SECTION
}