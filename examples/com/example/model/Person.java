package com.example.model;
 
import android.content.ContentValues;
import android.database.Cursor;
 
import android.os.Parcel;
import android.os.Parcelable;
   
import com.example.database.table.PersonTable; 
  
import java.util.ArrayList;
import java.util.List;
 
public class Person implements Parcelable {
    private transient long mRowId;
    private String mName; 
    private int mAge; 
    private boolean mAlive; 
    private double mBodyFat; 
    private long mIdPlace; 
    private byte[] mData; 
  
    private transient ContentValues mValues = new ContentValues();
 
    public Person() {}
 
    public Person(final Cursor cursor) {
        this(cursor, false);
    }
 
    public Person(final Cursor cursor, boolean prependTableName) {
        String prefix = prependTableName ? PersonTable.TABLE_NAME + "_" : "";
        setRowId(cursor.getLong(cursor.getColumnIndex(prefix + PersonTable._ID)));
        setName(cursor.getString(cursor.getColumnIndex(prefix + PersonTable.NAME))); 
        setAge(cursor.getInt(cursor.getColumnIndex(prefix + PersonTable.AGE))); 
        setAlive(!cursor.isNull(cursor.getColumnIndex(prefix + PersonTable.ALIVE)) && cursor.getInt(cursor.getColumnIndex(prefix + PersonTable.ALIVE)) != 0); 
        setBodyFat(cursor.getDouble(cursor.getColumnIndex(prefix + PersonTable.BODY_FAT))); 
        setIdPlace(cursor.getLong(cursor.getColumnIndex(prefix + PersonTable.ID_PLACE))); 
        setData(cursor.getBlob(cursor.getColumnIndex(prefix + PersonTable.DATA))); 
    }
 
    public Person(Parcel parcel) {
        mRowId = parcel.readLong();
 
        setName(parcel.readString()); 
 
        setAge(parcel.readInt()); 
 
        setAlive(parcel.readInt() == 1); 
 
        setBodyFat(parcel.readDouble()); 
 
        setIdPlace(parcel.readLong()); 
 
        int dataLength = parcel.readInt();
        if (dataLength >= 0) {
            byte[] data = new byte[dataLength];
            parcel.readByteArray(data);
            setData(data); 
        }
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
 
        parcel.writeLong(getIdPlace()); 
 
        if (getData() != null) {
            parcel.writeInt(getData().length);
            parcel.writeByteArray(getData()); 
        } else {
            parcel.writeInt(-1);
        }
    }
 
    public static final Creator<Person> CREATOR = new Creator<Person>() {
        public Person createFromParcel(Parcel in) {
            return new Person(in);
        }
 
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };
  
    public final void setRowId(long _id) {
        mRowId = _id;
        mValues.put(PersonTable._ID, _id);
    }
 
    public final void setName(String name) {
        mName = name;
        mValues.put(PersonTable.NAME, name);
    }
 
    public final void setAge(int age) {
        mAge = age;
        mValues.put(PersonTable.AGE, age);
    }
 
    public final void setAlive(boolean alive) {
        mAlive = alive;
        mValues.put(PersonTable.ALIVE, alive);
    }
 
    public final void setBodyFat(double bodyFat) {
        mBodyFat = bodyFat;
        mValues.put(PersonTable.BODY_FAT, bodyFat);
    }
 
    public final void setIdPlace(long idPlace) {
        mIdPlace = idPlace;
        mValues.put(PersonTable.ID_PLACE, idPlace);
    }
 
    public final void setData(byte[] data) {
        mData = data;
        mValues.put(PersonTable.DATA, data);
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
 
    public long getIdPlace() {
        return mIdPlace;
    }
 
    public byte[] getData() {
        return mData;
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