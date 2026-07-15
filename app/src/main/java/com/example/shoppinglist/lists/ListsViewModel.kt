package com.example.shoppinglist.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoppinglist.data.api.RetrofitClient
import com.example.shoppinglist.data.models.ListRequest
import com.example.shoppinglist.ui.ListsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel for holding state and do the fetching of data
class ListsViewModel : ViewModel() {
    // StateFlow for holding the UI state
    private val _uiState = MutableStateFlow<ListsUiState>(ListsUiState.Loading)
    val uiState: StateFlow<ListsUiState> = _uiState.asStateFlow()
    // init block to fetch the data
    init { loadLists() } // load the data upon startup of app

    // function to load lists from the database
    fun loadLists() {
        // launch a coroutine for asynchronous fetching of data from the database
        // launch is what lets us call the suspend functions in ApiService and get the response
        // all without blocking the UI thread and auto-cancels if the ViewModel is destroyed
        // it is the structured equivalent of async in onMounted in Vue
        viewModelScope.launch {
            _uiState.value = ListsUiState.Loading // set loading state
            try {
                // fetch lists from database
                val response = RetrofitClient.api.getAllLists()
                if (response.success && response.data != null) {
                    _uiState.value = ListsUiState.Success(response.data) // set success state with data
                } else {
                    _uiState.value = ListsUiState.Error(response.error ?: response.message ?: "Unknown error") // set error state with message
                }
            } catch (e: Exception){
                _uiState.value = ListsUiState.Error(e.message ?: "Network error") // set error state with message
            }
        }
    }

    // Create a List function '
    fun createList(name: String){
        viewModelScope.launch {
            // try catch block to get responses and handle errors
            try {
                val response = RetrofitClient.api.createList(ListRequest(name))
                // it successful response, load the lists
                if (response.success) loadLists()
                // if not successful, set the error state
                else _uiState.value = ListsUiState.Error(response.error ?: "Failed to create list")
            } catch (e: Exception){
                // and if , the if-else block fails, set the error state
                _uiState.value = ListsUiState.Error(e.message ?: "Network error")
            }
        }
    }



}