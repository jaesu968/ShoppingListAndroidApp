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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.material3.AlertDialog


// lists Screen that will show up on home screen
@Composable
fun ListsScreen(modifier: Modifier = Modifier, onListClick: (String) -> Unit = {}, viewModel: ListsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState() // collectAsState is what lets us observe the state of the ViewModel
    var showDialog by remember { mutableStateOf(false) } // this is used to show the dialog
    // wrap everything in a Box so a FAB can float over it
    Box(modifier = modifier.fillMaxSize()) {
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
                            Text(
                                text = list.name,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
        // FAB that calls the showDialog variable
        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) { Icon(Icons.Default.Add, contentDescription = "New list") }
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
    }
}



