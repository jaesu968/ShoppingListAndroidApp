package com.example.shoppinglist.data.local

// imports to use DAO and associated functions
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow // import flow from kotlinx.coroutines to allow asynchronous data retrieval

@Dao // annotation to tell Room this is a DAO
interface ItemDao {
    @Query("SELECT * FROM items WHERE listId = :listId") // get all items for a list
    fun observeItemsForList(listId: String): Flow<List<ItemEntity>>

    @Upsert
    suspend fun upsertAll(items: List<ItemEntity>) // update or insert an item in a list

    @Query("DELETE FROM items WHERE listId = :listId")
    suspend fun clearForList(listId: String) // delete all items for a specific list
}