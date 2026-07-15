package com.example.shoppinglist.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null,
    val message: String? = null
)

@Serializable
data class ShoppingList(
    @SerialName("_id") val id: String,
    var name: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val items: List<Item>? = null
)

@Serializable
data class Item(
    @SerialName("_id") val id: String = "",
    val listId : String = "",
    val name: String,
    val qty: Int = 1,
    val checked: Boolean = false,
    val notes : String = "",
    val brand: String = "",
    val category: String = "",
    val price: Double? = null,
    val weight: Double? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

// request classes
@Serializable
data class ListRequest(val name: String)

@Serializable
data class ItemRequest(
    val name: String? = null,
    val qty: Int? = null,
    val checked: Boolean? = null,
    val notes: String? = null,
    val brand: String? = null,
    val category: String? = null,
    val price: Double? = null,
    val weight: Double? = null,
)
