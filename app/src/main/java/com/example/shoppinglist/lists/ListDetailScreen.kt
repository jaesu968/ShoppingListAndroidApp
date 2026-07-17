package com.example.shoppinglist.lists

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import com.example.shoppinglist.data.models.Item
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextDecoration


// List Detail Screen
@OptIn(ExperimentalMaterial3Api::class) // TopAppBar's API is still marked experimental
@Composable
fun ListDetailScreen(modifier: Modifier = Modifier, viewModel: ListDetailViewModel = viewModel(), onBack: () -> Unit) {
    // state
    val state by viewModel.uiState.collectAsState() // collectAsState is what lets us observe the state of the ViewModel
    // variable used to show dialog, remember is used to keep the state of the variable across recompositions
    var showDialog by remember { mutableStateOf(false) }
    // variable to help with deletion confirmation of items
    var itemToDelete by remember { mutableStateOf<Item?>(null) }
    // variable to help with editing items
    var itemToEdit by remember { mutableStateOf<Item?>(null) }
    // wrap everything again, but this time in Scaffold for the Top App Bar
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text((state as? DetailUiState.Success)?.listName ?: "Shopping List") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        // wrap everything in a Box so a FAB can float over it
        Box(modifier = modifier.padding(innerPadding).fillMaxSize()) {
            // when the state changes, the composable will recompose
            DetailContent(
                modifier = Modifier,
                state = state,
                onToggle = viewModel::toggleItem,
                onEdit = { itemToEdit = it },
                onDeleteRequest = { itemToDelete = it },
                onRetry = viewModel::loadItems,
            )
            // FAB that calls the showDialog variable
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            )
            { Icon(Icons.Default.Add, contentDescription = "New Item") }
            // Show Dialog here
            if (showDialog) {
                ItemFormDialog(
                    title = "New Item",
                    onDismiss = { showDialog = false },
                    onSubmit = {
                        viewModel.addItem(it)
                        showDialog = false
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
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = { itemToDelete = null },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.secondary
                            )
                        ) { Text("Cancel") }
                    }
                )
            }
            itemToEdit?.let { item ->
                ItemFormDialog(
                    title = "Edit '${item.name}'",
                    initial = item,
                    onDismiss = { itemToEdit = null},
                    onSubmit = {
                        viewModel.updateItem(item.id, it)
                        itemToEdit = null
                    }
                )
            }
        }
    }
}

// move when expression outside the ListDetailScreen into a separate composable
@Composable
fun DetailContent(
    modifier: Modifier = Modifier,
    state: DetailUiState,
    onToggle: (Item) -> Unit = {},
    onEdit: (Item) -> Unit = {},
    onDeleteRequest: (Item) -> Unit = {},
    onRetry: () -> Unit = {},

) {
    when (val s = state) {
        is DetailUiState.Loading -> {
            // show loading indicator
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(modifier = Modifier.testTag("loadingIndicator"))
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
                Button(onClick = onRetry)  {
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
                items(s.items) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        onClick = { onEdit(item) }) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.checked,
                                onCheckedChange = { onToggle(item) }
                            )
                            Text(
                                text = if (item.qty > 1) "${item.name} x ${item.qty}" else item.name,
                                style = MaterialTheme.typography.bodyLarge,
                                // strike through the text if the item is checked
                                textDecoration = if (item.checked) TextDecoration.LineThrough else null,
                                // color the text based on the checked state
                                color = if (item.checked) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = { onDeleteRequest(item) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}