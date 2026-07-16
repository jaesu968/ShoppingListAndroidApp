package com.example.shoppinglist.lists

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoppinglist.ui.ListsUiState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import com.example.shoppinglist.data.models.ShoppingList


// lists Screen that will show up on home screen
@Composable
fun ListsScreen(modifier: Modifier = Modifier, onListClick: (String) -> Unit = {}, viewModel: ListsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState() // collectAsState is what lets us observe the state of the ViewModel
    var showDialog by remember { mutableStateOf(false) } // this is used to show the dialog
    var listToDelete by remember { mutableStateOf<ShoppingList?>(null) } // this is used to identify the list to delete
    var listToRename by remember { mutableStateOf<ShoppingList?>(null) } // this is used to identify the list to rename
    // Wrap everything again in Scaffold to add another FAB
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "New List")
            }
        }
    ) { innerPadding ->
        // wrap everything in a Box so a FAB can float over it
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            // when the state changes, the composable will be recomposed
            when (val s = state) {
                is ListsUiState.Loading -> {
                    // show loading indicator
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ListsUiState.Error -> {
                    // show error message and a retry button that calls viewModel.loadLists()
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = s.message)
                        Button(onClick = { viewModel.loadLists() }) {
                            Text(text = "Retry")
                        }
                    }
                }

                is ListsUiState.Success -> {
                    // show list of lists
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "My Shopping Lists",
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(s.lists) { list ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                // make it clickable and pass the list id to the onListClick function
                                onClick = { onListClick(list.id) }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = list.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    IconButton(onClick = { listToDelete = list }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete list"
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    IconButton(onClick = { listToRename = list }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Rename list")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // show dialog here
            if (showDialog) {
                var name by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("New Shopping list") },
                    text = {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") })
                    },
                    confirmButton = {
                        TextButton(
                            enabled = name.isNotBlank(),
                            onClick = {
                                viewModel.createList(name.trim())
                                showDialog = false
                            }
                        ) { Text("Create") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                    }
                )
            }
            // Deletion part of the card
            listToDelete?.let { list ->
                AlertDialog(
                    onDismissRequest = { listToDelete = null },
                    title = { Text("Delete '${list.name}'?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteList(list.id)
                                listToDelete = null
                            }
                        ) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = { listToDelete = null }) { Text("Cancel") }
                    }
                )
            }
            listToRename?.let { list ->
                var name by remember(list) { mutableStateOf(list.name) } // name needs to be prefilled with the current list name
                AlertDialog(
                    onDismissRequest = { listToRename = null },
                    title = { Text("Rename '${list.name}'?") },
                    text = {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("List Name") })
                    },
                    confirmButton = {
                        TextButton(
                            enabled = name.isNotBlank(),
                            onClick = {
                                viewModel.updateList(list.id, name.trim())
                                listToRename = null
                            },
                        ) { Text("Rename") }
                    },
                    dismissButton = {
                        TextButton(onClick = { listToRename = null }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}



