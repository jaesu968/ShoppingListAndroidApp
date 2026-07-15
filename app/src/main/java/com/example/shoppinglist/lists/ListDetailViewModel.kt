package com.example.shoppinglist.lists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoppinglist.data.api.RetrofitClient
import com.example.shoppinglist.data.models.Item
import com.example.shoppinglist.data.models.ItemRequest
import com.example.shoppinglist.ui.DetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


// view model for list details
class ListDetailViewModel(savedStateHandle: SavedStateHandle): ViewModel() {
    // get list id from saved state handle
    private val listId: String = checkNotNull(savedStateHandle["listId"])

    // start mutable state flow for detail ui state
    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading) // mutable state flow
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow() // expose immutable state flow

    // init block to load data
    init { loadItems() }

    // function to load items
    fun loadItems() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            // try catch block to handle errors
            try{
                // fetch lists from database
                val response = RetrofitClient.api.getList(listId)
                if (response.success && response.data != null) {
                    // check if response is successful and data is not null
                    val list = response.data // get list data
                    _uiState.value = DetailUiState.Success(list.name, list.items ?: emptyList()) // set success state with data
                } else {
                    _uiState.value = DetailUiState.Error(response.error ?: response.message ?: "Unknown error") // set error state with message
                }
            } catch (e: Exception){
                _uiState.value = DetailUiState.Error(e.message ?: "Network error")
            }
        }
    }
    // function to toggle the checkbox on items
    fun toggleItem(item: Item){
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.updateItem(
                    listId, item.id, ItemRequest(checked = !item.checked)
                )
                // check for successful response
                if (response.success && response.data != null){
                    val current = _uiState.value
                    if(current is DetailUiState.Success){
                        _uiState.value = current.copy(
                            // state must be replaced not mutated to preserve immutability
                            items = current.items.map {
                                if (it.id == item.id) response.data else it // update item in list
                            }
                        )
                    }
                }
            } catch(e: Exception){
                // item stays unchecked on failure
            }
        }
    }
}

