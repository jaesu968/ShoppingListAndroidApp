package com.example.shoppinglist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shoppinglist.ui.theme.ShoppingListTheme
import com.example.shoppinglist.lists.ListsScreen
import com.example.shoppinglist.lists.ListDetailScreen
import com.example.shoppinglist.lists.ListDetailViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            var darkTheme by rememberSaveable { mutableStateOf(systemDark)}
            ShoppingListTheme (darkTheme = darkTheme) {
                ShoppingListApp(
                    darkTheme = darkTheme,
                    onToggleTheme = { darkTheme = !darkTheme }
                )
            }
        }
    }
}

@Composable
fun ShoppingListApp(darkTheme: Boolean, onToggleTheme: () -> Unit) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "lists"
    ){
        composable("lists"){
            ListsScreen(
                onListClick = { listId -> navController.navigate("detail/$listId")},
                darkTheme = darkTheme,
                onToggleTheme = onToggleTheme
            )
        }
        composable("detail/{listId}"){
            // List Details Screen — VM built via the entry-scoped factory
            // Navigation injects a SavedStateHandle populated with listId
            ListDetailScreen(
                viewModel = viewModel(factory = ListDetailViewModel.Factory),
                onBack = { navController.popBackStack() }
            )
        }
    }
}
