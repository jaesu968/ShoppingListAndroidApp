package com.example.shoppinglist.data

// the connection between the UI and the database (offline - Room and Online - MongoDb)

import com.example.shoppinglist.data.api.ApiService // import the API service
import com.example.shoppinglist.data.local.ListDao // import the list Dao for Room DB operations
import com.example.shoppinglist.data.local.ItemDao // import the item DAO for Room DB ops
import com.example.shoppinglist.data.local.toEntity // import the mapper to convert to entity
import com.example.shoppinglist.data.local.toModel // import the mapper to convert to model
import com.example.shoppinglist.data.models.ShoppingList // import the shopping list model
import com.example.shoppinglist.data.models.Item // import the item model
import kotlinx.coroutines.flow.Flow // import flow from kotlinx.coroutines to allow asynchronous data retrieval
import kotlinx.coroutines.flow.map // import map from kotlinx.coroutines to map the flow

class ShoppingListRepository(
    private val api: ApiService, // the API service
    private val listDao: ListDao,  // the list DAO
    private val itemDao: ItemDao // the item DAO
) {
    // Observe - reads only from Room, never fails, works offline
    fun observeLists(): Flow<List<ShoppingList>> =
        // outer .map() transforms each emission of the stream into another stream
        listDao.observeAll().map { entities ->
            // inner .map() transforms each item in the list of entities into a list of models
            entities.map { it.toModel() }
        }

    // Refresh - hits network, writes into Room on success; may throw offline
    suspend fun refreshLists(){
        val response = api.getAllLists()
        if(response.success && response.data != null){
            // writes into Room
            listDao.upsertAll(response.data.map { it.toEntity() })
        }
    }

    // Observe -- reads only from Room, never fails, works offline for items
    fun observeItems(listId: String): Flow<List<Item>> =
        itemDao.observeItemsForList(listId).map {
            entities -> entities.map { it.toModel() }
        }

    // Refresh - hits network, writes into Room on success; may throw offline for items
    suspend fun refreshItems(listId: String){
        val response = api.getItems(listId)
        // if successful, write into Room
        if(response.success && response.data != null) {
            itemDao.clearForList(listId) // remove stale items for this list
            itemDao.upsertAll(response.data.map { it.toEntity() }) // write the fresh set of items
        }
    }
}