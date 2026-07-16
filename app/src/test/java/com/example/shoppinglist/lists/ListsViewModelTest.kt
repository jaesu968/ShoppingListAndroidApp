package com.example.shoppinglist.lists

import com.example.shoppinglist.MainDispatcherRule
import com.example.shoppinglist.data.models.ApiResponse
import com.example.shoppinglist.data.models.ShoppingList
import com.example.shoppinglist.ui.ListsUiState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

// testing out the list view model - unit tests

class ListsViewModelTest {
    // set up main dispatcher rule
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // load lists test
    @Test
    fun loadLists_setsSuccessState_whenApiSucceeds() = runTest {
        val lists = listOf(ShoppingList(id = "1", name = "Groceries"))
        val viewModel = ListsViewModel(
            FakeApiService(
                listsResponse = ApiResponse(success = true, data = lists)
            )
        )
        // set up state variable
        val state = viewModel.uiState.value
        // assert that the state is a success state
        assertTrue(state is ListsUiState.Success)
        assertEquals(lists, (state as ListsUiState.Success).lists)
    }

    // load lists and set error state
    @Test
    fun loadLists_setsErrorState_whenApiThrows() = runTest {
        val viewModel = ListsViewModel(
            FakeApiService(
                throwOnGetAll = RuntimeException("boom")
            )
        )
        val state = viewModel.uiState.value
        assertTrue(state is ListsUiState.Error)
        assertEquals("boom", (state as ListsUiState.Error).message)
    }
}