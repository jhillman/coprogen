package com.example.database.table;
 
public final class PlaceTable {
    private PlaceTable() {}
 
    public static final String TABLE_NAME = "place";
 
    public static final String _ID = "_id"; 
 
    public static final String ADDRESS = "address"; 
  
    public static final String[] ALL_COLUMNS = new String[] { _ID, ADDRESS };
 
    public static final String SQL_CREATE = "CREATE TABLE place (_id INTEGER PRIMARY KEY AUTOINCREMENT, address TEXT UNIQUE)";
 
    public static final String SQL_INSERT = "INSERT INTO place ( address ) VALUES ( ? )";
 
    public static final String SQL_DROP = "DROP TABLE IF EXISTS place";
 
    public static final String WHERE_ID_EQUALS = _ID + "=?";
}