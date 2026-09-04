package com.example.shoppinglist.lists

import android.app.Application
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
import com.example.shoppinglist.data.ShoppingListRepository
import com.example.shoppinglist.data.local.DatabaseBuilder
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY


// view model for list details
class ListDetailViewModel(savedStateHandle: SavedStateHandle,
                          private val repository: ShoppingListRepository,
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
        observeItems()
        refresh()
    }

    companion object {
        // factory that lets Navigation supply the nav-arg-populated SavedStateHandle
        // (createSavedStateHandle() reads it from CreationExtras) while we keep the
        // default api = RetrofitClient.api
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as Application
                val db = DatabaseBuilder.getDatabase(app)
                val repository = ShoppingListRepository(
                    api = RetrofitClient.api,
                    listDao = db.listDao(),
                    itemDao = db.itemDao()
                )
                ListDetailViewModel(
                    this.createSavedStateHandle(),
                    repository = repository
                )
            }
        }
    }

    // observe items for this list from Room (offline source of truth)
    private fun observeItems(){
        viewModelScope.launch {
            repository.observeItems(listId).collect { items ->
                // items Flow carries no list name, so preserve the one we already have
                // "get this field only is the state is the right shape"
                val currentName = (_uiState.value as? DetailUiState.Success)?.listName ?: ""
                _uiState.value = DetailUiState.Success(currentName, items)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                repository.refreshItems(listId)
            } catch (e: Exception){
                // offline - Room still has cached items, don't blow away the screen
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
                if(response.success && response.data != null) refresh() // call refresh to updated item
                else {
                    // if not successful set error state
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
                if (response.success && response.data != null) refresh() // call refresh to see new item
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
                if (response.success) refresh() // call refresh to see item removed
                else {
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
                if(response.success) refresh() // call refresh to see the updated item
                else _uiState.value = DetailUiState.Error(
                    response.error ?: response.message ?: "Failed to update item"
                )
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(e.message ?: "Network error")
            }
        }
    }
}



