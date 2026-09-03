package com.example.shoppinglist.data.local

// file to build a real instance with Room.databaseBuilder

import android.content.Context // Context is needed to handle app resources, Room needs it to locate the database file on disk
import androidx.room.Room // import Room to build the database

object DatabaseBuilder {
    @Volatile // this is a singleton, ensure all threads see the same instance
    private var instance: AppDatabase? = null // the database instance

    // build the database
    fun getDatabase(context: Context): AppDatabase =
        instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext, // pass application Context, never an activity because it can be destroyed easily on rotation
                AppDatabase::class.java, // reference to the database class
                "shopping_list.db"  // the file name on disk (file on device where cache persists)
            ).build().also { instance = it } /// assign the instance and return it
        }

}