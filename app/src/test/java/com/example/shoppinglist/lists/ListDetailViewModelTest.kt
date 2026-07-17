package com.example.shoppinglist.lists

import androidx.lifecycle.SavedStateHandle
import com.example.shoppinglist.MainDispatcherRule
import com.example.shoppinglist.data.models.ApiResponse
import com.example.shoppinglist.data.models.Item
import com.example.shoppinglist.data.models.ItemRequest
import com.example.shoppinglist.data.models.ShoppingList
import com.example.shoppinglist.ui.DetailUiState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

// testing out the list detail view model - unit tests

class ListDetailViewModelTest {
    // set up main dispatcher rule
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // every test gets a fresh ViewModel wired to its own fake
    private fun viewModel(fake: FakeApiService) = ListDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf("listId" to "1")),
        api = fake
    )

    // load items and set success state when api succeeds
    @Test
    fun loadItems_setsSuccessState_whenApiSucceeds() = runTest {
        // set up fake api service
        val items = listOf(Item(id = "a", name = "Milk"))
        val fake = FakeApiService(
            listResponse = ApiResponse(
                success = true,
                data = ShoppingList(id = "1", name = "Groceries", items = items)
            )
        )
        // set up view model
        val state = viewModel(fake).uiState.value
        // assertions
        assertTrue(state is DetailUiState.Success)
        state as DetailUiState.Success
        assertEquals("Groceries", state.listName) // list name loaded
        assertEquals(items, state.items) // items loaded
    }

    // toggle item test - check what was sent and what happened to state in one scenario
    @Test
    fun toggleItem_sendsInvertedFlag_andReplacesOnlyThatItem() = runTest {
        // set up variables for one list
        val milk = Item(id = "a", name = "Milk", checked = false)
        val eggs = Item(id = "b", name = "Eggs", checked = false)
        // make milk checked and set up fake api service
        val checkedMilk  = milk.copy(checked = true)
        val fake = FakeApiService(
            listResponse = ApiResponse(
                success = true,
                data = ShoppingList(id = "1", name = "Groceries", items = listOf(milk, eggs))
            ),
            itemResponse = ApiResponse(success = true, data = checkedMilk)
        )
        val vm = viewModel(fake) // init loads both items -> Success

        vm.toggleItem(milk) // toggle milk

        // Assertions
        assertEquals(true, fake.lastItemRequest?.checked) // inverse of milk.checked was sent
        val state = vm.uiState.value as DetailUiState.Success // state is success
        assertEquals(listOf(checkedMilk, eggs), state.items) // milk was replaced, eggs untouched
    }
    // failure path for toggle item -> Error state (Option A: else branch surfaces the error)
    @Test
    fun toggleItem_setsErrorState_whenApiFails() = runTest {
        // set up variables for one list
        val milk = Item(id = "a", name = "Milk", checked = false)
        val eggs = Item(id = "b", name = "Eggs", checked = false)
        // fake api service returns a failure for the update
        val fake = FakeApiService(
            listResponse = ApiResponse(
                success = true,
                data = ShoppingList(id = "1", name = "Groceries", items = listOf(milk, eggs))
            ),
            itemResponse = ApiResponse(success = false, error = "error")
        )
        val vm = viewModel(fake) // init loads both items -> Success

        vm.toggleItem(milk) // toggle milk

        // Assertions
        assertEquals(true, fake.lastItemRequest?.checked) // inverse of milk.checked was still sent
        val state = vm.uiState.value as DetailUiState.Error // failure surfaced as Error
        assertEquals("error", state.message)
    }

    // loadItems with the items = null -> Success with empty list (covering hte ?: emptyList() branch
    @Test
    fun loadItems_setsSuccessStateWithEmptyList_whenItemsIsNull() = runTest {
        // no variables for a list needed as it will be empty
        val fake = FakeApiService(
            listResponse = ApiResponse(
                success = true,
                data = ShoppingList(id = "1", name = "Groceries", items = null)
            ),
            itemResponse = ApiResponse(success = true, data = null)
        )
        val state = viewModel(fake).uiState.value
        // Assertions
        assertTrue(state is DetailUiState.Success) // check state is success
        state as DetailUiState.Success // set state as success
        assertEquals(emptyList<Item>(), state.items)
    }

    // load Items failure -> listResponse = ApiResponse(success = false, error = ".."
    @Test
    fun loadItems_setsErrorState_whenApiReturnsFailure() = runTest {
        // variables passed since listResponse is failure
        // set up fake api service
        val fake = FakeApiService(
            listResponse = ApiResponse(success = false, error = "error")
        )
        val state = viewModel(fake).uiState.value
        assertTrue(state is DetailUiState.Error)
    }
    // same thing but a throwOnAny variant
    @Test
    fun loadItems_setsErrorState_whenApiThrows() = runTest {
        val fake = FakeApiService(
            throwOnAny = RuntimeException("boom")
        )
        val state = viewModel(fake).uiState.value
        assertTrue(state is DetailUiState.Error)
        assertEquals("boom", (state as DetailUiState.Error).message)
    }
    // test for deleteItem success
    @Test
    fun deleteItem_removesItemFromList_whenApiSucceeds() = runTest {
        // set up variables for the list to delete from
        val milk = Item(id = "a", name = "Milk", checked = false)
        val eggs = Item(id = "b", name = "Eggs", checked = false)
        // set up fake api service
        val fake = FakeApiService(
            listResponse = ApiResponse(
                success = true,
                data = ShoppingList(id = "1", name = "Groceries", items = listOf(milk, eggs))
            ),
            itemResponse = ApiResponse(success = true, data = null)
        )
        val vm = viewModel(fake) // init loads both items -> Success
        vm.deleteItem(milk) // delete milk
        // assertions
        val state = vm.uiState.value as DetailUiState.Success
        assertEquals(listOf(eggs), state.items)
    }
    // test for deleteItem failure
    @Test
    fun deleteItem_doesNotRemoveItemFromList_whenApiFails() = runTest {
        // set up variables for the list to delete from
        val milk = Item(id = "a", name = "Milk", checked = false)
        val eggs = Item(id = "b", name = "Eggs", checked = false)
        // set up fake api service
        val fake = FakeApiService(
            listResponse = ApiResponse(
                success = true,
                data = ShoppingList(id = "1", name = "Groceries", items = listOf(milk, eggs))
            ),
            unitResponse = ApiResponse(success = false, error = "error")
        )
        val vm = viewModel(fake) // init loads both items -> Success
        vm.deleteItem(milk) // delete milk
        // assertions
        val state = vm.uiState.value as DetailUiState.Error
        assertEquals("error", state.message)
    }

    // test add item, for on successful create, reload the list
    @Test
    fun addItem_addsItemToList_whenApiSucceeds() = runTest {
        // set up variables already in the list
        val milk = Item(id = "a", name = "Milk", checked = false)
        val eggs = Item(id = "b", name = "Eggs", checked = false)
        // and a variable to add to the list
        val cheese = Item(id = "c", name = "Cheese", checked = false)
        // set up fake api service
        val fake = FakeApiService(
            listResponse = ApiResponse(
                success = true,
                data = ShoppingList(id = "1", name = "Groceries", items = listOf(milk, eggs))
            ),
            itemResponse = ApiResponse(success = true, data = cheese)
        )
        val vm = viewModel(fake) // init loads both items -> Success
        // the reload after creation to see the server's new state
        fake.listResponse = ApiResponse(success = true,
            data = ShoppingList(id = "1", name = "Groceries", items = listOf(milk, eggs, cheese)))
        vm.addItem(ItemRequest(name = "Cheese")) // add cheese to the list
        // assertions
        val state = vm.uiState.value as DetailUiState.Success
        assertEquals(listOf(milk, eggs, cheese), state.items)
        // assert that fake.getListCallCount == 2 (init + reload)
        assertEquals(2, fake.getListCallCount)
    }
    // failure path for addItem
    @Test
    fun addItem_doesNotAddItemsToList_whenApiFails() = runTest {
        // set up variables already in the list
        val milk = Item(id = "a", name = "Milk", checked = false)
        val eggs = Item(id = "b", name = "Eggs", checked = false)
        // and a variable to add to the list
        val cheese = ItemRequest(name = "Cheese")
        // set up fake api service
        val fake = FakeApiService(
            listResponse = ApiResponse(
                success = true,
                data = ShoppingList(id = "1", name = "Groceries", items = listOf(milk, eggs))
            ),
            itemResponse = ApiResponse(success = false, error = "error")
        )
        val vm = viewModel(fake) // init loads both items -> Success
        vm.addItem(cheese) // add cheese to the list
        // assertions
        val state = vm.uiState.value as DetailUiState.Error
        assertEquals("error", state.message)
    }

    // test updateItem, for on successful update, reload the list
    @Test
    fun updateItem_reloadsListWithUpdatedItem_whenApiSucceeds() = runTest {
        // set up variables already in the list
        val milk = Item(id = "a", name = "Milk", checked = false)
        val eggs = Item(id = "b", name = "Eggs", checked = false)
        // the renamed version the server will return on reload
        val oatMilk = milk.copy(name = "Oat Milk")
        // set up fake api service
        val fake = FakeApiService(
            listResponse = ApiResponse(
                success = true,
                data = ShoppingList(id = "1", name = "Groceries", items = listOf(milk, eggs))
            ),
            itemResponse = ApiResponse(success = true, data = oatMilk)
        )
        val vm = viewModel(fake) // init loads both items -> Success
        // the reload after update sees the server's new state
        fake.listResponse = ApiResponse(success = true,
            data = ShoppingList(id = "1", name = "Groceries", items = listOf(oatMilk, eggs)))
        vm.updateItem("a", ItemRequest(name = "Oat Milk")) // rename milk
        // assertions
        assertEquals("Oat Milk", fake.lastItemRequest?.name) // right payload sent
        val state = vm.uiState.value as DetailUiState.Success
        assertEquals(listOf(oatMilk, eggs), state.items)
        assertEquals(2, fake.getListCallCount) // init + reload
    }

    // failure path for updateItem
    @Test
    fun updateItem_setsErrorState_whenApiFails() = runTest {
        // set up variables already in the list
        val milk = Item(id = "a", name = "Milk", checked = false)
        val eggs = Item(id = "b", name = "Eggs", checked = false)
        // set up fake api service
        val fake = FakeApiService(
            listResponse = ApiResponse(
                success = true,
                data = ShoppingList(id = "1", name = "Groceries", items = listOf(milk, eggs))
            ),
            itemResponse = ApiResponse(success = false, error = "error")
        )
        val vm = viewModel(fake) // init loads both items -> Success
        vm.updateItem("a", ItemRequest(name = "Oat Milk")) // rename milk
        // assertions
        val state = vm.uiState.value as DetailUiState.Error
        assertEquals("error", state.message)
    }

}

