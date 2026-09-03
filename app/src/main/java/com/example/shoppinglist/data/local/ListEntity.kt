package com.example.shoppinglist.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_lists")
data class ListEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)