package com.example.shoppinglist.lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.shoppinglist.data.models.Item
import com.example.shoppinglist.data.models.ItemRequest
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp

// Dialog for adding or editing an item
@Composable
fun ItemFormDialog(
    title: String,
    initial: Item? = null,
    onDismiss: () -> Unit,
    onSubmit: (ItemRequest) -> Unit
) {
    // variables to keep track of the form fields
    // the pattern initial?.name ?: "" makes one composable serve bod modes for both initial and null
    var name by remember(initial) { mutableStateOf(initial?.name ?: "") }
    var qtyText by remember(initial) { mutableStateOf(initial?.qty?.toString() ?: "") }
    var brand by remember(initial) { mutableStateOf(initial?.brand ?: "") }
    var category by remember(initial) { mutableStateOf(initial?.category ?: "") }
    var notes by remember(initial) { mutableStateOf(initial?.notes ?: "") }
    var priceText by remember(initial) {mutableStateOf(initial?.price?.toString() ?: "")}
    var weightText by remember(initial) {mutableStateOf(initial?.weight?.toString() ?: "")}

    // Alert Dialog to display
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title)},
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // existing name and qty fields, plus the five new ones
                OutlinedTextField(label = { Text("Name") }, value = name, onValueChange = {name = it})
                OutlinedTextField(label = { Text("Quantity") }, value = qtyText, onValueChange = {qtyText = it},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(label = { Text("Brand") }, value = brand, onValueChange = {brand = it})
                OutlinedTextField(label = { Text("Category") }, value = category, onValueChange = {category = it})
                OutlinedTextField(label = { Text("Notes") }, value = notes, onValueChange = {notes = it})
                OutlinedTextField(label = { Text("Price") }, value = priceText, onValueChange = {priceText = it},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(label = { Text("Weight") }, value = weightText, onValueChange = {weightText = it},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onSubmit(ItemRequest(
                        name = name.trim(),
                        qty = qtyText.toIntOrNull() ?: 1,
                        brand = brand.ifBlank() { null },
                        category = category.ifBlank() { null },
                        notes = notes.ifBlank() { null },
                        price = priceText.toDoubleOrNull(),
                        weight = weightText.toDoubleOrNull()
                    ))
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss,
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.secondary
            )
        ) { Text("Cancel") }}
    )
}