package com.example.shoppinglist.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoppinglist.data.api.ApiService
import com.example.shoppinglist.data.api.RetrofitClient
import com.example.shoppinglist.data.models.ListRequest
import com.example.shoppinglist.ui.ListsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.shoppinglist.data.ShoppingListRepository
import android.app.Application
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.example.shoppinglist.data.local.DatabaseBuilder


// ViewModel for holding state and do the fetching of data
class ListsViewModel(private val repository: ShoppingListRepository, private val api: ApiService = RetrofitClient.api) : ViewModel() {
    // StateFlow for holding the UI state
    private val _uiState = MutableStateFlow<ListsUiState>(ListsUiState.Loading)
    val uiState: StateFlow<ListsUiState> = _uiState.asStateFlow() // expose the state as a StateFlow

    // companion object to build factory for creating the ViewModel
    companion object {
        val Factory = viewModelFactory {
            initializer {
                // the framework hands us the Application via CreationExtras
                val app = this[APPLICATION_KEY] as Application
                val db = DatabaseBuilder.getDatabase(app)
                val repository = ShoppingListRepository(
                    api = RetrofitClient.api,
                    // accessors for the DAOs from the database
                    listDao = db.listDao(),
                    itemDao = db.itemDao()
                )
                ListsViewModel(
                    repository = repository
                )
            }
        }
    }

    init{
        observeLists() // start the Room subscription
        refresh() // kick oof a network sync
    }
    // observe lists from Room
    private fun observeLists(){
        viewModelScope.launch {
            repository.observeLists().collect{
                lists -> // collect the lists from the repository
                _uiState.value = ListsUiState.Success(lists)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                repository.refreshLists()
            } catch (e:Exception){
                // network failed - but Room still has cached data, so DON'T blow away the screen
                // (for now, just leave it)
            }
        }
    }


    // Create a List function '
    fun createList(name: String){
        viewModelScope.launch {
            // try catch block to get responses and handle errors
            try {
                val response = api.createList(ListRequest(name))
                // if successful response, load the lists
                if (response.success) refresh()
                // if not successful, set the error state
                else _uiState.value = ListsUiState.Error(response.error ?: "Failed to create list")
            } catch (e: Exception){
                // and if the if-else block fails, set the error state
                _uiState.value = ListsUiState.Error(e.message ?: "Network error")
            }
        }
    }

    // Delete lists function
    fun deleteList(id: String){
        viewModelScope.launch {
            // try catch block to get responses and handle errors
            try {
                val response = api.deleteList(id)
                // if successful response, load the lists
                if (response.success) refresh()
                // if not successful, set the error state
                else _uiState.value = ListsUiState.Error(response.error ?: "Failed to delete list")
            } catch (e: Exception){
                // and if the if-else block fails, set the error state
                _uiState.value = ListsUiState.Error(e.message ?: "Network error")
            }
        }
    }

    // update list function to update the name of the list
    fun updateList(id: String, name: String){
        viewModelScope.launch {
            // try catch block to get responses and handle errors
            try {
                val response = api.updateList(id, ListRequest(name))
                // if successful response, load the lists
                if (response.success) refresh()
                // if not successful, set the error state
                else _uiState.value = ListsUiState.Error(response.error ?: "Failed to update list")
            } catch (e: Exception){
                // and if the if-else block fails, set the error state
                _uiState.value = ListsUiState.Error(e.message ?: "Network error")
            }
        }
    }
}