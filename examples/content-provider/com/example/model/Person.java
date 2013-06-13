package com.example.model;
 
import android.content.ContentValues;
import android.database.Cursor;
import com.example.database.table.PersonTable;
 
public class Person {
    private long mId;
 
    private String mName; 
 
    private int mAge; 
 
    private boolean mAlive; 
 
    private double mBodyFat; 
  
    private ContentValues mValues = new ContentValues();
 
    public Person(final Cursor cursor) {
        setId(cursor.getLong(cursor.getColumnIndex(PersonTable.ID)));
        setName(cursor.getString(cursor.getColumnIndex(PersonTable.NAME))); 
        setAge(cursor.getInt(cursor.getColumnIndex(PersonTable.AGE))); 
        setAlive(cursor.isNull(cursor.getColumnIndex(PersonTable.ALIVE)) ? false : cursor.getInt(cursor.getColumnIndex(PersonTable.ALIVE)) != 0); 
        setBodyFat(cursor.getDouble(cursor.getColumnIndex(PersonTable.BODY_FAT))); 
    }
 
    /* Auto generatted setters */
    public void setId(long id) {
        mId = id;
        mValues.put(PersonTable.ID, id);
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
  
    /* Auto generated getters */
    public long getId() {
        return mId;
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
}