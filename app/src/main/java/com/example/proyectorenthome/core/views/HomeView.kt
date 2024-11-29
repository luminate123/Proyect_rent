package com.example.proyectorenthome.core.views

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.proyectorenthome.Data.ImagenPropiedad
import com.example.proyectorenthome.Data.Properties
import com.example.proyectorenthome.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.UploadStatus
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.uploadAsFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.util.UUID

@Composable
fun HomeView(
    navigateToReserve: (Int) -> Unit, // Recibe un Int
    navigateToPerfil: (String) -> Unit,
    navigateToChats:() -> Unit
) {
    // Contenido principal aquí, como botones, textos, etc.
    val session1 = supabase.auth.currentSessionOrNull()?.user?.id.toString()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 15.dp),

                color = Color.Transparent,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Inicio",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2C3E50)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                }
            }
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
                    Triple(Icons.Default.Home, "Inicio", {}),
                    Triple(Icons.Default.Search, "Buscar", {}),
                    Triple(Icons.Default.Email, "Inbox", navigateToChats),
                    Triple(Icons.Default.Person, "Perfil", {navigateToPerfil(session1)})
                ).forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.first,
                                contentDescription = item.second,
                                tint = if (index == 0) Color(0xFF2196F3) else Color(0xFF90A4AE)
                            )
                        },
                        label = { Text(item.second) },
                        selected = index == 0,
                        onClick = item.third
                    )
                }
            }
        }
    ) { innerPadding ->
        // Aquí puedes agregar el contenido principal de la pantalla
        // El innerPadding ajusta el contenido para evitar que se superponga con la barra de navegación
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {

                Spacer(modifier = Modifier.height(16.dp))
                Spacer(modifier = Modifier.height(16.dp))
                PropertiesList(navigateToReserve)
                Spacer(modifier = Modifier.height(16.dp))

            }
        }
    }

}




@Composable
fun PropertiesList(navigateToReserve: (Int) -> Unit) { // Cambiado a aceptar un Int
    val properties = remember { mutableStateListOf<Properties>() }

    LaunchedEffect(Unit) {
        loadProperties(properties)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        items(properties) { property ->
            PropertyCard(property = property, navigateToReserve = navigateToReserve)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun PropertyCard(property: Properties, navigateToReserve: (Int) -> Unit) { // Cambiado a aceptar un Int
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            // Image Section
            Image(
                painter = rememberAsyncImagePainter(property.imageUrl), // Load image from URL
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Title Section
            Text(
                text = property.título,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Location and Distance
            Text(
                text = "${property.ciudad} -- ${property.descripción} ",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Rating and Price Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "S/ ${property.precio_noche} noche",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "⭐ ${property.estado}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { navigateToReserve(property.id) }, // Pasa el ID aquí
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Seleccionar")
            }
        }
    }
}

suspend fun loadProperties(properties: MutableList<Properties>) {
    val session1 = supabase.auth.currentSessionOrNull()?.user?.id.toString()
    withContext(Dispatchers.IO) {
        try {
            // Obtener las propiedades
            val result = supabase
                .from("properties")
                .select {
                    filter {
                        neq("usuario_id",session1)
                    }
                }
                .decodeList<Properties>() // Decodifica directamente en una lista de Properties

            Log.d("loadProperties", "Propiedades cargadas: $result")

            // Obtener las imágenes relacionadas en una sola consulta
            val propertyImages = supabase.from("imagenes_propiedades")
                .select(columns = Columns.list("propiedad_id", "url_imagen"))
                .decodeList<ImagenPropiedad>()

            // Crear un mapa para buscar imágenes por propiedad_id
            val imageMap = propertyImages.groupBy { it.propiedad_id }

            // Asignar URLs de imágenes a las propiedades
            result.forEach { property ->
                val images = imageMap[property.id]?.map { it.url_imagen }
                property.imageUrl = images?.joinToString(", ") // Combina URLs si hay varias
                Log.d("loadProperties", "Propiedad: ${property.título}, Imagen: ${property.imageUrl}")
            }

            // Agregar las propiedades cargadas a la lista mutable
            properties.addAll(result)

            // Log final
            Log.d("loadProperties", "Lista final de propiedades: $properties")
        } catch (e: Exception) {
            // Log en caso de error
            Log.e("loadProperties", "Error cargando propiedades: ${e.message}", e)
        }
    }
}