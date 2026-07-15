package com.example.shoppinglist.lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoppinglist.ui.DetailUiState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.input.KeyboardType
import com.example.shoppinglist.data.models.Item


// List Detail Screen
@Composable
fun ListDetailScreen(modifier: Modifier = Modifier, viewModel: ListDetailViewModel = viewModel()) {
    // state
    val state by viewModel.uiState.collectAsState() // collectAsState is what lets us observe the state of the ViewModel
    // variable used to show dialog, remember is used to keep the state of the variable across recompositions
    var showDialog by remember { mutableStateOf(false) }
    // variable to help with deletion confirmation of items
    var itemToDelete by remember { mutableStateOf<Item?>(null) }
    // wrap everything in a Box so a FAB can float over it
    Box(modifier = modifier.fillMaxSize()) {
        // when the state changes, the composable will recompose
        when (val s = state) {
            is DetailUiState.Loading -> {
                // show loading indicator
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            is DetailUiState.Error -> {
                // show error message and a retry button that calls viewModel.loadLists()
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = s.message)
                    Button(onClick = { viewModel.loadItems() }) {
                        Text(text = "Retry")
                    }
                }
            }

            is DetailUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        Text(
                            text = "Shopping List: ${s.listName}",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                    }
                    items(s.items) { item ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.checked,
                                    onCheckedChange = { viewModel.toggleItem(item) }
                                )
                                Text(
                                    text = if (item.qty > 1) "${item.name} x ${item.qty}" else item.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { itemToDelete = item }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    // FAB that calls the showDialog variable
    FloatingActionButton(
        onClick = { showDialog = true },
        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
    )
        { Icon(Icons.Default.Add, contentDescription = "New Item")}
        // Show Dialog here
        if (showDialog){
            var name by remember { mutableStateOf("") }
            var qtyText by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("New Item")},
                text = {
                    Column {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Item Name") }
                        )
                        OutlinedTextField(
                            value = qtyText,
                            onValueChange = { qtyText = it},
                            label = { Text("Quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = name.isNotBlank(),
                        onClick = {
                            viewModel.addItem(name.trim(), qtyText.toIntOrNull() ?: 1)
                            showDialog = false
                        }
                    ) {Text("Create")}
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {Text("Cancel")}
                }
            )
        }
        // Deletion part of the card
        itemToDelete?.let { item ->
            AlertDialog(
                onDismissRequest = { itemToDelete = null },
                title = { Text("Delete '${item.name}'?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteItem(item)
                            itemToDelete = null
                        }
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { itemToDelete = null }) { Text("Cancel") }
                }
            )
        }
    }
}