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
                throwOnAny = RuntimeException("boom")
            )
        )
        val state = viewModel.uiState.value
        assertTrue(state is ListsUiState.Error)
        assertEquals("boom", (state as ListsUiState.Error).message)
    }
    // load list and set error state when api returns failure
    @Test
    fun loadLists_setErrorState_whenApiReturnsFailure() = runTest {
        val viewModel = ListsViewModel(
            FakeApiService(
                listsResponse = ApiResponse(success = false, message = "server said no")
            )
        )
        // set up
        val state = viewModel.uiState.value
        // assertions
        assertTrue(state is ListsUiState.Error)
        assertEquals("server said no", (state as ListsUiState.Error).message)
    }
    // create list success
    @Test
    fun createList_reloadsLists_whenApiSucceeds() = runTest {
        // set up variables
        val groceries = ShoppingList(id = "1", name ="Groceries") // list to create
        // fake api service
        val fake = FakeApiService(
            listsResponse = ApiResponse(success = true, data = listOf(groceries)),
            listResponse = ApiResponse(success = true, data = groceries) // createList returns this
        )
        val vm = ListsViewModel(fake) // init load empty list
        //reload after creation to see the new list
        fake.listsResponse = ApiResponse(success = true, data = listOf(groceries, groceries))
        vm.createList("Groceries") // create the list
        // assertions
        assertEquals("Groceries", fake.lastListRequest?.name) // right payload sent
        assertEquals(2, fake.getAllListsCallCount) // init + reload
        val state = vm.uiState.value as ListsUiState.Success // state is success
        assertEquals(listOf(groceries, groceries), state.lists) // new list
    }
    // create List but it fails -> no reload, error surfaced
    @Test
    fun createList_setsErrorState_whenApiFails() = runTest {
        val groceries = ShoppingList(id = "1", name ="Groceries")
        val fake = FakeApiService(
            listsResponse = ApiResponse(success = true, data = listOf(groceries)),
            listResponse = ApiResponse(success = false, error = "error") // createList returns this
        )
        // init load empty list
        val vm = ListsViewModel(fake)
        vm.createList("Groceries") // create the list
        // assertions
        assertEquals("Groceries", fake.lastListRequest?.name) // right payload sent
        assertEquals(1, fake.getAllListsCallCount) // init only, no reload on failure
        val state = vm.uiState.value as ListsUiState.Error // state is error
        assertEquals("error", state.message)
    }
    // delete list success
    @Test
    fun deleteList_reloadsLists_whenApiSucceeds() = runTest {
        // variables to use in the list
        val groceries = ShoppingList(id = "1", name = "Groceries")
        val fake = FakeApiService(
            listsResponse = ApiResponse(success = true, data = listOf(groceries)),
            unitResponse = ApiResponse(success = true)
        )
        val vm = ListsViewModel(fake) // init load with one list
        // reload after deletion sees the list gone
        fake.listsResponse = ApiResponse(success = true, data = emptyList())
        vm.deleteList("1") // delete the list
        // assertions
        assertEquals("1", fake.deleteListId) // right id sent
        assertEquals(2, fake.getAllListsCallCount) // init + reload
        val state = vm.uiState.value as ListsUiState.Success // state is success
        assertEquals(emptyList<ShoppingList>(), state.lists) // list is now empty
    }
    // delete list failure -> no reload, error surfaced
    @Test
    fun deleteList_setsErrorState_whenApiFails() = runTest {
        // variables to use in the list
        val groceries = ShoppingList(id = "1", name = "Groceries")
        val fake = FakeApiService(
            listsResponse = ApiResponse(success = true, data = listOf(groceries)),
            unitResponse = ApiResponse(success = false, error = "error")
        )
        val vm = ListsViewModel(fake) // init load empty list
        vm.deleteList("1") // delete the list
        // assertions
        assertEquals("1", fake.deleteListId)
        assertEquals(1, fake.getAllListsCallCount) // init only, no reload on failure
        val state = vm.uiState.value as ListsUiState.Error
        assertEquals("error", state.message)
    }

    // update List success
    @Test
    fun updateList_reloadsLists_whenApiSucceeds() = runTest {
        // variables to use in the list
        val groceries = ShoppingList(id = "1", name = "Groceries")
        val fake = FakeApiService(
            listsResponse = ApiResponse(success = true, data = listOf(groceries)),
            listResponse = ApiResponse(success = true, data = groceries)
        )
        val vm = ListsViewModel(fake)
        fake.listsResponse = ApiResponse(success = true, data = listOf(groceries, groceries))
        vm.updateList("1", "Groceries1") // give the list a new name and update the list
        // assertions
        assertEquals("Groceries1", fake.lastListRequest?.name) // check did the list update correctly
        assertEquals(2, fake.getAllListsCallCount) // update get all lists call count should be 2
        val state = vm.uiState.value as ListsUiState.Success // state is success
        assertEquals(listOf(groceries, groceries), state.lists) // list should be updated
    }
    // update list failure -> no reload, error surfaced
    @Test
    fun updateList_setsErrorState_whenApiFails() = runTest {
        // variables to use in the list
        val groceries = ShoppingList(id = "1", name = "Groceries")
        val fake = FakeApiService(
            listsResponse = ApiResponse(success = true, data = listOf(groceries)),
            listResponse = ApiResponse(success = false, error = "error")
        )
        val vm = ListsViewModel(fake)
        vm.updateList("1", "Groceries1") // give the list a new name and update the list
        // assertions
        assertEquals("Groceries1", fake.lastListRequest?.name) // check did the list update correctly
        assertEquals(1, fake.getAllListsCallCount) // init only, no reload on failure
        val state = vm.uiState.value as ListsUiState.Error // state is error
        assertEquals("error", state.message)
    }
}
