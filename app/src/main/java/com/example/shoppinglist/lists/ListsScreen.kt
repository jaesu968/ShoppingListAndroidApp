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
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment


// lists Screen that will show up on home screen
@Composable
fun ListsScreen(modifier: Modifier = Modifier, onListClick: (String) -> Unit = {}, viewModel: ListsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState() // collectAsState is what lets us observe the state of the ViewModel
    // when the state changes, the composable will be recomposed
    when (val s = state) {
        is ListsUiState.Loading -> {
            // show loading indicator
            Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally){
                CircularProgressIndicator()
            }
        }

        is ListsUiState.Error -> {
            // show error message and a retry button that calls viewModel.loadLists()
            Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = s.message)
                Button(onClick = { viewModel.loadLists() }) {
                    Text(text = "Retry")
                }
            }
        }

        is ListsUiState.Success -> {
            // show list of lists
            LazyColumn(
                modifier = modifier
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
                    Card(modifier = Modifier.fillMaxWidth(),
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
}


