package com.example.proyectorenthome.core.navigation

import kotlinx.serialization.Serializable

@Serializable
object Login

@Serializable
object Register

@Serializable
object Home

@Serializable
object RegisterProperty

@Serializable
data class Reserve(val propertyId : Int)

@Serializable
data class Perfil(val usuarioId : String)

@Serializable
object Chats

@Serializable
data class Messages(val chatId: Int)