package com.example.shoppinglist.data.local

// file to cover conversions for read and write operations to and from the database

import com.example.shoppinglist.data.models.ShoppingList // import the ShoppingList model
import com.example.shoppinglist.data.models.Item // import the item model

// network -> entity (writing lists into Room)
fun ShoppingList.toEntity(): ListEntity =
    ListEntity(
        id = id,
        name = name,
        updatedAt = updatedAt
    )

// entity -> network (reading lists out for the UI)
fun ListEntity.toModel(): ShoppingList =
    ShoppingList(
        id = id,
        name = name,
        updatedAt = updatedAt,
        items = null // items live in a separate table; not embedded here
    )
// network -> entity (writing items into Room)
fun Item.toEntity(): ItemEntity =
    ItemEntity(
        id = id,
        listId = listId,
        name = name,
        qty = qty,
        checked = checked,
        notes = notes,
        brand = brand,
        category = category,
        price = price,
        weight = weight,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
// entity -> network (reading items out to the UI)
fun ItemEntity.toModel(): Item =
    Item(
        id = id,
        listId = listId,
        name = name,
        qty = qty,
        checked = checked,
        notes = notes,
        brand = brand,
        category = category,
        price = price,
        weight = weight,
        createdAt = createdAt,
        updatedAt = updatedAt
    )