package com.example.shoppinglist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shoppinglist.ui.theme.ShoppingListTheme
import com.example.shoppinglist.lists.ListsScreen
import com.example.shoppinglist.lists.ListDetailScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShoppingListTheme {
                ShoppingListApp()
            }
        }
    }
}

@Composable
fun ShoppingListApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "lists"
    ){
        composable("lists"){
            ListsScreen(
                onListClick = { listId -> navController.navigate("detail/$listId")}
            )
        }
        composable("detail/{listId}"){
            // List Details Screen
            ListDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
