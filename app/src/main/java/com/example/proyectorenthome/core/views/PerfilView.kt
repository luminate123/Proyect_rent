package com.example.proyectorenthome.core.views

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.proyectorenthome.Data.ImagenPropiedad
import com.example.proyectorenthome.Data.Properties
import com.example.proyectorenthome.Data.PropertyWithImage
import com.example.proyectorenthome.Data.User
import com.example.proyectorenthome.R
import com.example.proyectorenthome.core.navigation.Perfil
import com.example.proyectorenthome.supabase
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PerfilView(
    usuarioId: String,
    navigateToHome: () -> Unit,
    navigateToRegisterProperty: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(modifier = Modifier.fillMaxWidth()) {
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = false,
                    onClick = navigateToHome
                )
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar") },
                    label = { Text("Buscar") },
                    selected = false,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Inbox") },
                    label = { Text("Inbox") },
                    selected = false,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = true,
                    onClick = {}
                )
            }
        }
    ) { innerPadding ->
        var perfilName by remember { mutableStateOf("") }
        var perfilEmail by remember { mutableStateOf("") }
        var perfilPhone by remember { mutableStateOf("") }
        var perfilPassword by remember { mutableStateOf("") }

        //properties perfil
        var propiedadesState by remember { mutableStateOf<List<Properties>>(emptyList()) }



        val composableScope = rememberCoroutineScope()
        // Simula la carga de datos del perfil
        LaunchedEffect(usuarioId) {
            val loadedPerfil = loadPerfil(usuarioId)
            perfilName = loadedPerfil?.nombre ?: "Usuario"
            perfilEmail = loadedPerfil?.email?: "Email"
            perfilPhone = loadedPerfil?.teléfono?: "Telefono"
            perfilPassword = loadedPerfil?.contraseña?: "Password"
        }
        LaunchedEffect(usuarioId) {
            val loadedProperties = loadPropertiesPerfil(usuarioId)

            if (loadedProperties != null && loadedProperties.isNotEmpty()) {
                // Manejar la lista de propiedades
                propiedadesState = loadedProperties // Asignar la lista a un estado o variable
            } else {
                Log.w("LaunchedEffect", "No se encontraron propiedades para el usuarioId: $usuarioId")
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            // Encabezado del perfil
            item {
                PerfilHeader(perfilName = perfilName)
            }

            // Detalles del perfil
            item {
                PerfilDetails(
                    email = perfilEmail,
                    name = perfilName,
                    phone = perfilPhone,
                    password = perfilPassword
                )
            }
            item {
                Text(text = "Lista de Propiedades")

            }
            // Lista de propiedades
            items(propiedadesState) { propiedad ->
                var imageProperty by remember { mutableStateOf("url_image") }

                LaunchedEffect(propiedad.id) {
                    val loadedImagenProperty = loadPropertiesImages(propiedad.id)
                    imageProperty = loadedImagenProperty?.url_imagen ?: "url_image"
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp) // Altura fija para la tarjeta
                    ) {
                        // Imagen
                        Image(
                            painter = rememberAsyncImagePainter(imageProperty), // Reemplaza con tu fuente de imágenes
                            contentDescription = "Imagen de la propiedad",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )

                        // Detalles y botón
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Título de la propiedad
                            Text(
                                text = propiedad.título,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Descripción de la propiedad (si existe)
                            if (!propiedad.descripción.isNullOrEmpty()) {
                                Text(
                                    text = propiedad.descripción,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }

                            // Botón para eliminar
                            Button(
                                onClick = {
                                    composableScope.launch(Dispatchers.IO) {
                                        eliminarProperty(propiedad.id)
                                        withContext(Dispatchers.Main) {
                                            propiedadesState = propiedadesState.filter { it.id != propiedad.id }
                                        }
                                    }
                                },// Implementa esta función
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(text = "Eliminar")
                            }
                        }
                    }
                }
            }
            item {
                RegisterPropertyButton(onClick = navigateToRegisterProperty)
            }

        }

    }
}

@Composable
fun RegisterPropertyButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Registrar una propiedad")
    }
}


@Composable
fun PerfilHeader(perfilName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Avatar",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = perfilName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun PerfilDetails(
    email: String,
    name: String,
    phone: String,
    password: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        PerfilField(label = "Correo electrónico", value = email)
        PerfilField(label = "Nombres", value = name)
        PerfilField(label = "Teléfono", value = phone)
        PasswordField(label = "Contraseña", password = password)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilField(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        TextField(
            value = value,
            onValueChange = {}, // Campo no editable
            readOnly = true,
            colors = TextFieldDefaults.textFieldColors(
                disabledIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordField(label: String, password: String) {
    // Estado para controlar la visibilidad de la contraseña
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        TextField(
            value = password,
            onValueChange = {}, // Campo no editable
            readOnly = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.Lock else Icons.Filled.Lock,
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            colors = TextFieldDefaults.textFieldColors(
                disabledIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

suspend fun loadPerfil(usuarioId: String):User? {
    return withContext(Dispatchers.IO) {
        try {
            val perfil = supabase.from("users").select {
                filter { eq("UUID", usuarioId) }
            }.decodeSingle<User>()

            Log.d("loadPerfil", "Perfil: $perfil")
            perfil
        } catch (e: Exception) {
            Log.e("loadPerfil", "Error cargando perfil: ${e.message}", e)
            null
        }
    }
}

suspend fun loadPropertiesPerfil(usuarioId: String): List<Properties>? {
    return withContext(Dispatchers.IO) {
        try {
            // Consulta a Supabase para obtener todas las propiedades del perfil
            val propertiesPerfil = supabase.from("properties")
                .select {
                    filter { eq("usuario_id", usuarioId) }
                }
                .decodeList<Properties>()

            Log.d("loadPerfil", "Properties Perfil cargados con éxito: $propertiesPerfil")
            propertiesPerfil
        } catch (e: Exception) {
            Log.e("loadPerfil", "Error cargando Properties perfil para usuarioId: $usuarioId - ${e.message}", e)
            null
        }
    }
}

suspend fun loadPropertiesImages(propertyId: Int): ImagenPropiedad? {
    return withContext(Dispatchers.IO) {
        try {
            // Consulta a Supabase para obtener las imágenes asociadas a una propiedad
            val propertiesImage = supabase.from("imagenes_propiedades")
                .select {
                    filter { eq("propiedad_id", propertyId) } // Usa propiedad_id en lugar de usuario_id
                }
                .decodeSingle<ImagenPropiedad>() // Decodifica el resultado en un solo objeto

            Log.d("loadPropertiesImages", "Imagen cargada con éxito: $propertiesImage")
            propertiesImage // Devuelve la imagen obtenida
        } catch (e: Exception) {
            Log.e("loadPropertiesImages", "Error cargando imagen para propiedadId: $propertyId - ${e.message}", e)
            null
        }
    }
}

suspend fun eliminarProperty(propertyId: Int){
    try {
        supabase.from("properties").delete{
            filter {
                eq("id",propertyId)
            }
        }
        Log.d("se elimino correctamente","Propiedad eliminado")
    }catch (e: Exception){
        Log.d("Error","Propiedad no eliminado:${e.message}")
    }
}


