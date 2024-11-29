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
    NavHost(navController = navController, startDestination = Login) {
        composable<Login> {
            LoginView(
                navigateToRegister = { navController.navigate(Register) },
                navigateToHome = { navController.navigate(Home) }
            )
        }

        composable<Register> {
            RegisterView { navController.navigate(Login) }
        }

        composable<Home> {
            HomeView(
                navigateToReserve = { propertyId ->
                    navController.navigate(Reserve(propertyId = propertyId))
                } ,
                navigateToPerfil = {usuarioId ->
                    navController.navigate(Perfil(usuarioId = usuarioId))
                },
                navigateToChats = {
                    navController.navigate(Chats)
                }
            )
        }

        composable<RegisterProperty> {
            RegisterPropertyView(
                navigateToPerfil = {usuarioId ->
                    navController.navigate(Perfil(usuarioId = usuarioId))
                }
            )
        }

        composable<Reserve> { backStackEntry ->
            val propertyId:Reserve = backStackEntry.toRoute()
            ReserveView(propertyId.propertyId,
                navigateToHome = { navController.navigate(Home) }
            )
        }


        composable<Perfil>{ backStackEntry ->
            val usuarioId: Perfil = backStackEntry.toRoute()
            PerfilView(usuarioId.usuarioId,
                navigateToHome = { navController.navigate(Home) },
                navigateToRegisterProperty = { navController.navigate(RegisterProperty) },
                navigateToChats = {
                    navController.navigate(Chats)
                }
            )
        }

        composable<Chats> {
            ChatView(
                navigateToMessages = { chatId ->
                    navController.navigate(Messages(chatId = chatId))
                },
                navigateToHome = {navController.navigate(Home) },
                navigateToPerfil = {usuarioId ->
                    navController.navigate(Perfil(usuarioId = usuarioId))
                },
            )
        }

        composable<Messages> { backStackEntry ->
            val chatId: Messages = backStackEntry.toRoute()
            MessagesView(chatId.chatId,
                navigateToChats = {
                    navController.navigate(Chats)
                }
                )
        }


    }

}
