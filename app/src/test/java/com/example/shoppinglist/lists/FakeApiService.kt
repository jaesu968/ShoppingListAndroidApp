package com.example.shoppinglist.lists

import com.example.shoppinglist.data.api.ApiService
import com.example.shoppinglist.data.models.ApiResponse
import com.example.shoppinglist.data.models.Item
import com.example.shoppinglist.data.models.ItemRequest
import com.example.shoppinglist.data.models.ListRequest
import com.example.shoppinglist.data.models.ShoppingList

// fake api service for testing
class FakeApiService(
    var listsResponse: ApiResponse<List<ShoppingList>> =
        ApiResponse(success = true, data = emptyList()),
    var listResponse: ApiResponse<ShoppingList> = ApiResponse(success = true, data = null),
    var itemResponse: ApiResponse<Item> = ApiResponse(success = true, data = null),
    var unitResponse: ApiResponse<Unit> = ApiResponse(success = true),
    var throwOnAny: Exception? = null
) : ApiService {
    // recorded calls for assertions
    var lastItemRequest: ItemRequest? = null // last item request
    var getListCallCount = 0 // keep track of calls to getList
    var lastListRequest: ListRequest? = null // last list request
    var deleteListId = "" // last deleted list id
    var getAllListsCallCount = 0 // keep track of calls to getAllLists

    // override getAllLists function to return listResponse
    override suspend fun getAllLists(): ApiResponse<List<ShoppingList>> {
        throwOnAny?.let { throw it }
        getAllListsCallCount++ // increment call count
        return listsResponse
    }
    // override getList
    override suspend fun getList(id: String): ApiResponse<ShoppingList> {
        throwOnAny?.let { throw it }
        getListCallCount++ // increment call count
        return listResponse
    }

    // override createList
    override suspend fun createList(request: ListRequest): ApiResponse<ShoppingList> {
        throwOnAny?.let { throw it }
        lastListRequest = request
        return listResponse
    }

    // override updateList
    override suspend fun updateList(id: String, request: ListRequest): ApiResponse<ShoppingList> {
        throwOnAny?.let { throw it }
        lastListRequest = request
        return listResponse
    }

    // override deleteList
    override suspend fun deleteList(id: String): ApiResponse<Unit> {
        throwOnAny?.let { throw it }
        deleteListId = id
        return unitResponse
    }

    // override getItems
    override suspend fun getItems(listId: String): ApiResponse<List<Item>> {
        throwOnAny?.let { throw it }
        return ApiResponse(success = true, data = emptyList())
    }

    // override createItem
    override suspend fun createItem(listId: String, item: ItemRequest): ApiResponse<Item> {
        throwOnAny?.let { throw it }
        lastItemRequest = item
        return itemResponse
    }

    // override updateItem
    override suspend fun updateItem(
        listId: String,
        itemId: String,
        request: ItemRequest
    ): ApiResponse<Item> {
        throwOnAny?.let { throw it }
        lastItemRequest = request
        return itemResponse
    }

    // override deleteItem
    override suspend fun deleteItem(listId: String, itemId: String): ApiResponse<Unit> {
        throwOnAny?.let { throw it }
        return unitResponse
    }
}