package com.example.shoppinglist.data.local

import androidx.room.Database // import to make a database
import androidx.room.RoomDatabase // import to tell Room this is a database

// this class only declares the schema

@Database(
    entities = [ListEntity::class, ItemEntity::class], // the list of every table(entity) in the database
    version = 1, // database version, exists to force migrations if needed
    exportSchema = false // don't generate a schema file
)
// abstract class that extends RoomDatabase and Room will generate an implementation
abstract class AppDatabase: RoomDatabase(){
    abstract fun listDao(): ListDao // accessor for the list DAO
    abstract fun itemDao(): ItemDao // accessor for the item DAO
}
