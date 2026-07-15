package com.example.shoppinglist.lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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


// List Detail Screen
@Composable
fun ListDetailScreen(modifier: Modifier = Modifier, onListClick: (String) -> Unit  = {}, viewModel: ListDetailViewModel = viewModel()) {
    // state
    val state by viewModel.uiState.collectAsState() // collectAsState is what lets us observe the state of the ViewModel
    // when the state changes, the composable will recompose
    when(val s = state){
        is DetailUiState.Loading -> {
            // show loading indicator
            Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally){
                CircularProgressIndicator()
            }
        }
        is DetailUiState.Error -> {
            // show error message and a retry button that calls viewModel.loadLists()
            Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = s.message)
                Button(onClick = { viewModel.loadItems() }) {
                    Text(text = "Retry")
                }
            }
        }
        is DetailUiState.Success -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ){
              item {
                  Text(text = "Shopping List: ${s.listName}",
                  style = MaterialTheme.typography.titleLarge,
                  modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                  )
              }
                items(s.items) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ){
                            Checkbox(
                                checked = item.checked,
                                onCheckedChange = {}
                            )
                            Text(
                                text = if (item.qty > 1) "${item.name} x ${item.qty}" else item.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

        }
    }
}