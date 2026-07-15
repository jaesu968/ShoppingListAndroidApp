package com.example.shoppinglist.data.api

import com.example.shoppinglist.data.models.ApiResponse
import com.example.shoppinglist.data.models.Item
import com.example.shoppinglist.data.models.ItemRequest
import com.example.shoppinglist.data.models.ListRequest
import com.example.shoppinglist.data.models.ShoppingList
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    // Shopping Lists
    @GET("lists")
    suspend fun getAllLists(): ApiResponse<List<ShoppingList>>
    @GET("lists/{id}")
    suspend fun getList(@Path("id") id: String): ApiResponse<ShoppingList>
    @POST("lists")
    suspend fun createList(@Body request: ListRequest): ApiResponse<ShoppingList>
    // Update and delete list
    @PUT("lists/{id}")
    suspend fun updateList(@Path("id") id: String, @Body request: ListRequest): ApiResponse<ShoppingList>
    @DELETE("lists/{id}")
    suspend fun deleteList(@Path("id") id: String): ApiResponse<Unit>

    // Items
    // get all list items
    @GET("lists/{listId}/items")
    suspend fun getItems(@Path("listId") listId: String): ApiResponse<List<Item>>
    // create an item
    @POST("lists/{listId}/items")
    suspend fun createItem(@Path("listId") listId: String, @Body item: ItemRequest): ApiResponse<Item>
    // update an item
    @PUT("lists/{listId}/items/{itemId}")
    suspend fun updateItem(
        @Path("listId") listId: String,
        @Path("itemId") itemId: String,
        @Body request: ItemRequest
    ): ApiResponse<Item>
    // delete an item
    @DELETE("lists/{listId}/items/{itemId}")
    suspend fun deleteItem(
        @Path("listId") listId: String,
        @Path("itemId") itemId: String
    ): ApiResponse<Unit>


}

// CreateListRequest method