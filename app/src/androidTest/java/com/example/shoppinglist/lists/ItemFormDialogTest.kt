package com.example.shoppinglist.lists

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.shoppinglist.data.models.Item
import com.example.shoppinglist.data.models.ItemRequest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Rule
import org.junit.Test

class ItemFormDialogTest {
    @get:Rule
    // gives you a host to render composables into and a handle to find and interact with nodes.
    val composeTestRule = createComposeRule()

    @Test
    fun saveButton_isDisabled_whenNameIsBlank() {
        // setContent renders the composable
        composeTestRule.setContent {
            ItemFormDialog(
                title = "New Item",
                onDismiss = {},
                onSubmit = {}
            )
        }
        // onNodeWithText("Save") find the node, then you asert on it.
        composeTestRule.onNodeWithText("Save")
            .assertIsNotEnabled() // assert the save button is not enabled
    }

    // test the save button is enabled when the name is entered
    @Test
    fun saveButton_isEnable_whenNameIsEntered() {
        composeTestRule.setContent {
            ItemFormDialog(title = "New Item", onDismiss = {}, onSubmit = {})
        }
        composeTestRule.onNodeWithText("Name")
            .performTextInput("Milk") // input the text for testing
        composeTestRule.onNodeWithText("Save")
            .assertIsEnabled() // assert the save button is enabled after the text is entered
    }

    // test quantity falling back to 1
    @Test
    fun quantity_fallsBackToOne_whenBlank() {
        var submitted: ItemRequest? = null
        composeTestRule.setContent {
            ItemFormDialog(title = "New Item", onDismiss = {}, onSubmit = { submitted = it })
        }
        composeTestRule.onNodeWithText("Name").performTextInput("Milk")
        composeTestRule.onNodeWithText("Save").performClick()

        assertEquals(1, submitted?.qty)
    }

    // test blank optionals becoming null
    @Test
    fun optionals_becomeNull_whenBlank() {
        var submitted: ItemRequest? = null
        composeTestRule.setContent {
            ItemFormDialog(title = "New Item", onDismiss = {}, onSubmit = { submitted = it })
        }
        // perform actions on the node
        composeTestRule.onNodeWithText("Name").performTextInput("Milk")
        composeTestRule.onNodeWithText("Save").performClick()

        // assertions
        assertNull(submitted?.brand)
        assertNull(submitted?.category)
        assertNull(submitted?.notes)
    }

    // test edit-mode prefill
    @Test
    fun editMode_prefillFields() {
        val item = Item(
            id = "123",
            name = "Milk",
            qty = 2,
            brand = "Oatly",
            category = "Category",
            notes = "Notes",
            price = 1.99,
            weight = 1.0
        )
        composeTestRule.setContent {
            ItemFormDialog(title = "Edit Item", initial = item, onDismiss = {}, onSubmit = {})
        }
        // assertions
        composeTestRule.onNodeWithText("Milk").assertExists()
        composeTestRule.onNodeWithText("Oatly").assertExists()
        composeTestRule.onNodeWithText("1.99").assertExists()
    }
}
