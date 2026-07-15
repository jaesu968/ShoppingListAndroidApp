package com.example.shoppinglist.ui

import com.example.shoppinglist.data.models.ShoppingList

sealed interface ListsUiState {
    data object Loading: ListsUiState
    data class Success(val lists: List<ShoppingList>): ListsUiState
    data class Error(val message: String): ListsUiState
}