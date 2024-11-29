package com.example.proyectorenthome.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import com.example.proyectorenthome.core.views.*

@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "Login") {
        composable("Login") {
            LoginView(
                navigateToRegister = { navController.navigate("Register") },
                navigateToHome = { navController.navigate(Home) }
            )
        }

        composable("Register") {
            RegisterView { navController.navigate("Login") }
        }

        composable<Home> {
            HomeView(
                navigateToReserve = { propertyId ->
                    navController.navigate(Reserve(propertyId = propertyId))
                } ,
                navigateToPerfil = {usuarioId ->
                    navController.navigate(Perfil(usuarioId = usuarioId))
                }
            )
        }

        composable("RegisterProperty") {
            RegisterPropertyView()
        }

        composable<Reserve> { backStackEntry ->
            val propertyId:Reserve = backStackEntry.toRoute()
            ReserveView(propertyId.propertyId)
        }


        composable<Perfil>{ backStackEntry ->
            val usuarioId: Perfil = backStackEntry.toRoute()
            PerfilView(usuarioId.usuarioId,
                navigateToHome = { navController.navigate(Home) },
                navigateToRegisterProperty = { navController.navigate("RegisterProperty") }
            )
        }
    }

}

