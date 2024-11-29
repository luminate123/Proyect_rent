package com.example.proyectorenthome.Data

import kotlinx.serialization.Serializable

@Serializable
data class UserLogin(
    val email: String,
    val contraseña: String
)

@Serializable
data class User(
    val id: Int,
    val nombre: String,
    val email: String,
    val contraseña: String,
    val teléfono: String,
    val UUID: String
    )

@Serializable
data class Properties(
    val id:Int,
    val usuario_id:String,
    val título: String,
    val descripción: String,
    val dirección: String,
    val ciudad: String,
    val precio_noche: Float,
    val capacidad: Int,
    val estado: String,
    var imageUrl: String? = null // Campo para almacenar la URL de la imagen
)

@Serializable
data class ImagenPropiedad(
    val propiedad_id: Int,
    val url_imagen: String
)


data class PropertyWithImage(
    val property: Properties?,
    val image: ImagenPropiedad?
)

@Serializable
data class Reserva(
    val propiedad_id: Int,
    val usuario_id: String,
    val fecha_inicio: String,
    val fecha_fin: String,
    val num_huespedes: Int,
    val total_pago: Float,
    val estado: String
)