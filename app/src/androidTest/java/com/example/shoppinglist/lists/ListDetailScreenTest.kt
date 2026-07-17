package com.example.shoppinglist.lists

import androidx.compose.ui.test.assertIsDisplayed
import com.example.shoppinglist.ui.DetailUiState
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import com.example.shoppinglist.data.models.Item
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals

class ListDetailScreenTest {
    @get:Rule
    // gives you a host to render composables into and a handle to find and interact with nodes.
    val composeTestRule = createComposeRule()

    // render DetailContent with a hand-built DetailUiState, then assert in each test
    // build Items inline like the ViewModal tests do Item (id = "a', name = "Milk")

    // loading shows progress indicator
    @Test
    fun loading_showsProgressIndicator() {
        composeTestRule.setContent {
            DetailContent(state = DetailUiState.Loading)
        }
        composeTestRule.onNodeWithTag("loadingIndicator").assertIsDisplayed()
    }
    // error shows message and retry button
    @Test
    fun error_showsMessageAndRetryButton() {
        composeTestRule.setContent {
            DetailContent(state = DetailUiState.Error("Network error"))
        }
        composeTestRule.onNodeWithText("Network error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }
    // error - retry button fires on Retry
    @Test
    fun error_retryButton_firesOnRetry() {
        // pass onRetry = { retried = true }
        // capture with variable below
        var retried = false
        composeTestRule.setContent {
            DetailContent(state = DetailUiState.Error("Network error"), onRetry = { retried = true })
        }
        composeTestRule.onNodeWithText("Retry").performClick()
        assertTrue(retried)
    }
    // success when render items with quantity formatting
    @Test
    fun success_rendersItems_withQuantityFormatting() {
        composeTestRule.setContent {
            DetailContent(
                state = DetailUiState.Success(
                    listName = "Groceries",
                    items = listOf(
                        Item(id = "a", name = "Milk"),
                        Item(id = "b", name = "Eggs", qty = 12)
                    )
                )
            )
        }
        composeTestRule.onNodeWithText("Milk").assertIsDisplayed()
        composeTestRule.onNodeWithText("Eggs x 12").assertIsDisplayed()
    }

    // Success when checking item fires on toggle with the right item
    @Test
    fun success_checkingItem_firesOnToggle() {
        // capture toggled
        var toggled: Item? = null
        // pas onToggle = { toggled = it }
        composeTestRule.setContent {
            DetailContent(
                state = DetailUiState.Success(
                    listName = "Groceries",
                    items = listOf(
                        Item(id = "a", name = "Milk")
                    )
                ),
                onToggle = { toggled = it }
            )
        }
        composeTestRule.onNode(isToggleable()).performClick()
        assertEquals("a", toggled?.id)
    }
    // success when clicking on the delete button
    @Test
    fun success_deleteButton_firesOnDeleteRequest() {
        // capture delete requested
        var deleteRequested: Item? = null
        // pass onDeleteRequest = { deleteRequested = it }
        composeTestRule.setContent {
            DetailContent(
                state = DetailUiState.Success(
                    listName = "Groceries",
                    items = listOf(
                        Item(id = "a", name = "Milk")
                        )
                    ),
                onDeleteRequest = { deleteRequested = it }
            )
        }
        composeTestRule.onNodeWithContentDescription("Delete").performClick()
        assertEquals("a", deleteRequested?.id)
    }
}
