package com.example.shoppinglist.lists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.shoppinglist.data.api.RetrofitClient
import com.example.shoppinglist.data.models.Item
import com.example.shoppinglist.data.models.ItemRequest
import com.example.shoppinglist.ui.DetailUiState
import com.example.shoppinglist.data.api.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// view model for list details
class ListDetailViewModel(savedStateHandle: SavedStateHandle,
    private val api: ApiService = RetrofitClient.api
): ViewModel() {
    // get list id from saved state handle
    private val listId: String = checkNotNull(savedStateHandle["listId"])

    // start mutable state flow for detail ui state
    private val _uiState =
        MutableStateFlow<DetailUiState>(DetailUiState.Loading) // mutable state flow
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow() // expose immutable state flow

    // init block to load data
    init {
        loadItems()
    }

    companion object {
        // factory that lets Navigation supply the nav-arg-populated SavedStateHandle
        // (createSavedStateHandle() reads it from CreationExtras) while we keep the
        // default api = RetrofitClient.api
        val Factory = viewModelFactory {
            initializer {
                ListDetailViewModel(savedStateHandle = createSavedStateHandle())
            }
        }
    }

    // function to load items
    fun loadItems() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            // try catch block to handle errors
            try {
                // fetch lists from database
                val response = api.getList(listId)
                if (response.success && response.data != null) {
                    // check if response is successful and data is not null
                    val list = response.data // get list data
                    _uiState.value = DetailUiState.Success(
                        list.name,
                        list.items ?: emptyList()
                    ) // set success state with data
                } else {
                    _uiState.value = DetailUiState.Error(
                        response.error ?: response.message ?: "Unknown error"
                    ) // set error state with message
                }
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(e.message ?: "Network error")
            }
        }
    }

    // function to toggle the checkbox on items
    fun toggleItem(item: Item) {
        viewModelScope.launch {
            try {
                val response = api.updateItem(
                    listId, item.id, ItemRequest(checked = !item.checked)
                )
                // check for successful response
                if (response.success && response.data != null) {
                    val current = _uiState.value
                    if (current is DetailUiState.Success) {
                        _uiState.value = current.copy(
                            // state must be replaced not mutated to preserve immutability
                            items = current.items.map {
                                if (it.id == item.id) response.data else it // update item in list
                            }
                        )
                    }
                } else {
                    _uiState.value = DetailUiState.Error(
                        response.error ?: response.message ?: "Failed to update item"
                    )
                }
            } catch (e: Exception) {
                // item stays unchecked on failure
                _uiState.value = DetailUiState.Error(e.message ?: "Network error")
            }
        }
    }

    // function to add items
    fun addItem(request: ItemRequest) {
        viewModelScope.launch {
            // try catch block to get responses and handle errors
            try {
                val response =
                    api.createItem(listId, request)
                // if successful response, load the item
                if (response.success && response.data != null) loadItems()
                // if not successful, set error state
                else _uiState.value = DetailUiState.Error(
                    response.error ?: response.message ?: "Failed to create item"
                )
            } catch (e: Exception) {
                // and if the if-else block fails, set error state
                _uiState.value = DetailUiState.Error(e.message ?: "Network error")
            }
        }
    }

    // function to delete items
    fun deleteItem(item: Item) {
        viewModelScope.launch {
            try {
                val response = api.deleteItem(listId, item.id)
                // if successful response, remove item from list
                if (response.success) {
                    val current = _uiState.value
                    if (current is DetailUiState.Success) {
                        _uiState.value = current.copy(
                            // filter out the item, effectively removing it
                            items = current.items.filter { it.id != item.id }
                        )
                    }
                } else {
                    // if not successful, set error state
                    _uiState.value = DetailUiState.Error(
                        response.error ?: response.message ?: "Failed to delete item"
                    )
                }
            } catch (e: Exception) {
                // if the if-else block fails, set error state
                _uiState.value = DetailUiState.Error(e.message ?: "Network error")
            }
        }
    }

    // function to update item
    fun updateItem(itemId: String, request: ItemRequest) {
        viewModelScope.launch {
            try {
                val response = api.updateItem(listId, itemId, request)
                // if successful response, load items
                if(response.success) loadItems()
                else _uiState.value = DetailUiState.Error(
                    response.error ?: response.message ?: "Failed to update item"
                )
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(e.message ?: "Network error")
            }
        }
    }
}



