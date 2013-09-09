package com.example.database.table;
 
public interface PlaceTable {
    String TABLE_NAME = "place";
 
    String _ID = "_id"; 
 
    String ADDRESS = "address"; 
  
    String[] ALL_COLUMNS = new String[] { _ID, ADDRESS };
 
    String SQL_CREATE = "CREATE TABLE place ( _id INTEGER PRIMARY KEY AUTOINCREMENT, address TEXT )";
 
    String SQL_INSERT = "INSERT INTO place ( address ) VALUES ( ? )";
 
    String SQL_DROP = "DROP TABLE IF EXISTS place";
 
    String WHERE_ID_EQUALS = _ID + "=?";
}