package com.example.shoppinglist.data.local

// imports to use DAO and associated functions
import androidx.room.Dao // import to use DAO
import androidx.room.Query // import to make queries
import androidx.room.Upsert // import to upsert lists
import kotlinx.coroutines.flow.Flow // import flow from kotlinx.coroutines to allow asynchronous data retrieval

@Dao // annotation to tell Room this is a DAO
interface ListDao {
    @Query("SELECT * FROM shopping_lists") // get all lists
    fun observeAll(): Flow<List<ListEntity>>

    @Upsert
    suspend fun upsertAll(lists: List<ListEntity>) // update or insert a list

    @Query("DELETE FROM shopping_lists")
    suspend fun clear() // delete all lists

}