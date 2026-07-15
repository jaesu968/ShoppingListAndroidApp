package com.example.shoppinglist.ui

import com.example.shoppinglist.data.models.Item

sealed interface DetailUiState{
    data object Loading: DetailUiState
    data class Success(val listName: String, val items: List<Item>): DetailUiState
    data class Error(val message: String) : DetailUiState
}