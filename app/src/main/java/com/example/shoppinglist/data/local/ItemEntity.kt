package com.example.shoppinglist.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val id: String,
    val listId: String,
    val name: String,
    val qty: Int = 1,
    val checked: Boolean = false,
    val notes: String = "",
    val brand: String = "",
    val category: String = "",
    val price: Double? = null,
    val weight: Double? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)