package com.example.database.table;
 
public interface PersonTable {
    String TABLE_NAME = "person";
 
    String ID = "_id"; 
 
    String NAME = "name"; 
 
    String AGE = "age"; 
 
    String ALIVE = "alive"; 
 
    String BODY_FAT = "body_fat"; 
  
    String[] ALL_COLUMNS = new String[] { ID, NAME, AGE, ALIVE, BODY_FAT };
 
    String SQL_CREATE = "CREATE TABLE person ( _id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, age INTEGER, alive INTEGER, body_fat NUMERIC )";
 
    String SQL_INSERT = "INSERT INTO person ( name, age, alive, body_fat ) VALUES ( ?, ?, ?, ? )";
 
    String SQL_DROP = "DROP TABLE IF EXISTS person";
}