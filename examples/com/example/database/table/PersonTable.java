package com.example.database.table;
 
public final class PersonTable {
    private PersonTable() {}
 
    public static final String TABLE_NAME = "person";
 
    public static final String _ID = "_id"; 
 
    public static final String NAME = "name"; 
 
    public static final String AGE = "age"; 
 
    public static final String ALIVE = "alive"; 
 
    public static final String BODY_FAT = "body_fat"; 
 
    public static final String ID_PLACE = "id_place"; 
 
    public static final String DATA = "data"; 
  
    public static final String[] ALL_COLUMNS = new String[] { _ID, NAME, AGE, ALIVE, BODY_FAT, ID_PLACE, DATA };
 
    public static final String SQL_CREATE = "CREATE TABLE person (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT DEFAULT NULL, age INTEGER NOT NULL, alive INTEGER DEFAULT 1, body_fat NUMERIC, id_place TEXT, data BLOB, FOREIGN KEY(id_place) REFERENCES place(_id) ON UPDATE NO ACTION ON DELETE CASCADE, UNIQUE( name, id_place ))";
 
    public static final String SQL_INSERT = "INSERT INTO person ( name, age, alive, body_fat, id_place, data ) VALUES ( ?, ?, ?, ?, ?, ? )";
 
    public static final String SQL_DROP = "DROP TABLE IF EXISTS person";
 
    public static final String WHERE_ID_EQUALS = _ID + "=?";
}