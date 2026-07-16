package com.example.shoppinglist.lists

import com.example.shoppinglist.data.api.ApiService
import com.example.shoppinglist.data.models.ApiResponse
import com.example.shoppinglist.data.models.Item
import com.example.shoppinglist.data.models.ItemRequest
import com.example.shoppinglist.data.models.ListRequest
import com.example.shoppinglist.data.models.ShoppingList

// fake api service for testing
class FakeApiService(
    private val listsResponse: ApiResponse<List<ShoppingList>> =
        ApiResponse(success = true, data = emptyList()),
    private val throwOnGetAll: Exception? = null
) : ApiService {
    // override getAllLists function to return listResponse
    override suspend fun getAllLists(): ApiResponse<List<ShoppingList>> {
        throwOnGetAll?.let { throw it }
        return listsResponse
    }
    // override getList
    override suspend fun getList(id: String): ApiResponse<ShoppingList> =
        TODO("not needed yet")

    // override createList
    override suspend fun createList(request: ListRequest): ApiResponse<ShoppingList> {
        TODO("Not yet implemented")
    }

    // override updateList
    override suspend fun updateList(id: String, request: ListRequest): ApiResponse<ShoppingList> {
        TODO("Not yet implemented")
    }

    // override deleteList
    override suspend fun deleteList(id: String): ApiResponse<Unit> {
        TODO("Not yet implemented")
    }

    // override getItems
    override suspend fun getItems(listId: String): ApiResponse<List<Item>> {
        TODO("Not yet implemented")
    }

    // override createItem
    override suspend fun createItem(listId: String, item: ItemRequest): ApiResponse<Item> {
        TODO("Not yet implemented")
    }

    // override updateItem
    override suspend fun updateItem(
        listId: String,
        itemId: String,
        request: ItemRequest
    ): ApiResponse<Item> {
        TODO("Not yet implemented")
    }

    // override deleteItem
    override suspend fun deleteItem(listId: String, itemId: String): ApiResponse<Unit> {
        TODO("Not yet implemented")
    }

}