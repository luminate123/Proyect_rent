package com.example.proyectorenthome.core.views

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilView(
    usuarioId: String,
    navigateToHome: () -> Unit,
    navigateToRegisterProperty: () -> Unit,
    navigateToChats: () -> Unit,
    navigateToLogin: () -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF0F4F8),
                        Color(0xFFE0E7EF)
                    )
                )
            ),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mi Perfil",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50)
                        )
                    )
                },
                actions = {
                    IconButton(onClick = {
                        closeSession()
                        navigateToLogin()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Configuraciones",
                            tint = Color(0xFF607D8B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF607D8B), Color(0xFFECEFF1))
                        ),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
            ) {
                listOf(
                    Triple(Icons.Default.Home, "Inicio", navigateToHome),
                    Triple(Icons.Default.Search, "Buscar", {}),
                    Triple(Icons.Default.Email, "Inbox", navigateToChats),
                    Triple(Icons.Default.Person, "Perfil", {})
                ).forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.first,
                                contentDescription = item.second,
                                tint = if (index == 3) Color(0xFF2196F3) else Color(0xFF90A4AE)
                            )
                        },
                        label = { Text(item.second) },
                        selected = index == 3,
                        onClick = item.third
                    )
                }
            }
        }
    ) { innerPadding ->
        var perfilName by remember { mutableStateOf("") }
        var perfilEmail by remember { mutableStateOf("") }
        var perfilPhone by remember { mutableStateOf("") }
        var perfilPassword by remember { mutableStateOf("") }
        var propiedadesState by remember { mutableStateOf<List<Properties>>(emptyList()) }

        LaunchedEffect(usuarioId) {
            val loadedPerfil = loadPerfil(usuarioId)
            perfilName = loadedPerfil?.nombre ?: "Usuario"
            perfilEmail = loadedPerfil?.email ?: "Correo electrónico"
            perfilPhone = loadedPerfil?.teléfono ?: "Teléfono"
            perfilPassword = loadedPerfil?.contraseña ?: "Contraseña"
        }

        LaunchedEffect(usuarioId) {
            val loadedProperties = loadPropertiesPerfil(usuarioId)
            propiedadesState = loadedProperties ?: emptyList()
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Transparent)
        ) {
            item {
                ProfileHeaderSection(
                    perfilName = perfilName,
                    email = perfilEmail,
                    phone = perfilPhone
                )
            }

            item {
                SectionTitle("Mis Propiedades")
            }
            if (propiedadesState.isEmpty()) {
                item {
                    EmptyPropertiesPlaceholder(navigateToRegisterProperty)
                }
            } else {
                items(propiedadesState) { propiedad ->
                    var imageProperty by remember { mutableStateOf("url_image") }

                    LaunchedEffect(propiedad.id) {
                        val loadedImage = loadPropertiesImages(propiedad.id)
                        imageProperty = loadedImage?.url_imagen ?: "url_image"
                    }

                    AnimatedPropertyCard(
                        propiedad = propiedad,
                        imageProperty = imageProperty,
                        onDelete = {
                            propiedadesState = propiedadesState.filter { it.id != propiedad.id }
                        }
                    )
                }
            }
            item {
                RegisterPropertyButton(onClick = navigateToRegisterProperty)
            }
        }
    }
}

@Composable
fun ProfileHeaderSection(
    perfilName: String,
    email: String,
    phone: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(24.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            color = Color(0xFF2196F3).copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (perfilName.isNotEmpty()) perfilName.first().uppercase() else "?",
                        style = MaterialTheme.typography.displayMedium.copy(
                            color = Color(0xFF2196F3),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (perfilName.isNotEmpty()) perfilName else "Usuario",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C3E50)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ContactInfoItem(
                        icon = Icons.Default.Email,
                        text = email,
                        tint = Color(0xFF4CAF50)
                    )
                    ContactInfoItem(
                        icon = Icons.Default.Phone,
                        text = phone,
                        tint = Color(0xFF2196F3)
                    )
                }
            }
        }
    }
}

@Composable
fun ContactInfoItem(
    icon: ImageVector,
    text: String,
    tint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF607D8B)
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        ),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}



@Composable
fun EmptyPropertiesPlaceholder(
    onRegisterProperty: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                tint = Color(0xFF90A4AE),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No tienes propiedades registradas",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF607D8B)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRegisterProperty,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Text("Registrar Propiedad")
            }
        }
    }
}


@Composable
fun AnimatedPropertyCard(
    propiedad: Properties,
    imageProperty: String,
    onDelete: () -> Unit
) {
    var isDeleting by remember { mutableStateOf(false) }
    var shouldDelete by remember { mutableStateOf(false) } // State to trigger deletion

    // Trigger the delete operation when `shouldDelete` is set to true
    if (shouldDelete) {
        LaunchedEffect(Unit) {
            eliminarProperty(propiedad.id) // Call the suspend function
            onDelete() // Update the UI
        }
    }

    val animatedSize by animateFloatAsState(
        targetValue = if (isDeleting) 0f else 1f,
        animationSpec = tween(durationMillis = 300)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .graphicsLayer {
                scaleX = animatedSize
                scaleY = animatedSize
            }
            .clickable(enabled = !isDeleting) {}
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.height(180.dp)) {
                // Property Image
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color(0xFFF0F4F8))
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(imageProperty),
                        contentDescription = "Imagen de la propiedad",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Property Details
                Column(
                    modifier = Modifier
                        .weight(2f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = propiedad.título,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color(0xFF2196F3),
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (!propiedad.descripción.isNullOrEmpty()) {
                            Text(
                                text = propiedad.descripción,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF607D8B)
                                ),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            isDeleting = true
                            Handler(Looper.getMainLooper()).postDelayed({
                                isDeleting = false
                                shouldDelete = true // Set state to trigger deletion
                            }, 300)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF6B6B),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}


@Composable
fun PerfilHeader(perfilName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF4CAF50), Color(0xFF81C784))
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = perfilName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
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
            .shadow(4.dp, shape = RoundedCornerShape(16.dp))
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
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        TextField(
            value = value,
            onValueChange = {}, // Campo no editable
            readOnly = true,
            colors = TextFieldDefaults.textFieldColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordField(label: String, password: String) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
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
                        imageVector = if (passwordVisible) Icons.Filled.Lock else Icons.Outlined.Lock,
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
@Composable
fun RegisterPropertyButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(
            text = "Registrar una propiedad",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimary
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

fun closeSession(){
    CoroutineScope(Dispatchers.IO).launch {
        try {
            supabase.auth.signOut()
        }catch (e:Exception){
            Log.d("error","Error al cerrar sesion")
        }
    }
}


