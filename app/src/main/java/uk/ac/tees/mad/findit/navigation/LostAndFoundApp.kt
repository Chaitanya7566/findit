package uk.ac.tees.mad.findit.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import uk.ac.tees.mad.findit.ui.screens.auth.AuthScreen
import uk.ac.tees.mad.findit.ui.screens.home.HomeScreen
import uk.ac.tees.mad.findit.ui.screens.item_details.ItemDetailScreen
import uk.ac.tees.mad.findit.ui.screens.newitem.NewItemScreen
import uk.ac.tees.mad.findit.ui.screens.profile.ProfileScreen
import uk.ac.tees.mad.findit.ui.screens.splash.SplashScreen

@Composable
fun LostAndFoundApp(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        // Navigation graph
        composable(route = Routes.SPLASH) {
            SplashScreen(
                onNavigateToAuth = {
                    navController.navigate(Routes.AUTH) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }


        composable(route = Routes.AUTH) {
            AuthScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Routes.HOME) {
            HomeScreen(
                onNavigateToItemDetail = { itemId ->
                    navController.navigate("${Routes.ITEM_DETAIL}/$itemId")
                },
                onNavigateToNewItem = {
                    navController.navigate(Routes.NEW_ITEM)
                },
                onNavigateToProfile = {
                    navController.navigate(Routes.PROFILE)
                }
            )
        }

        composable(
            route = "${Routes.ITEM_DETAIL}/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            ItemDetailScreen(
                itemId = itemId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Routes.PROFILE) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToItemDetail = { itemId ->
                    navController.navigate("${Routes.ITEM_DETAIL}/$itemId")
                }
            )
        }

        composable(route = Routes.NEW_ITEM) {
            NewItemScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
