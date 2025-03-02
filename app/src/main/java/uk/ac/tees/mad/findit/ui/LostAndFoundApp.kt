package uk.ac.tees.mad.findit.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uk.ac.tees.mad.findit.navigation.Routes

@Composable
fun LostAndFoundApp(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        // Navigation graph
        composable(Routes.SPLASH) {  }
    }
}