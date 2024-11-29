package com.example.proyectorenthome.core.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.proyectorenthome.Data.ImagenPropiedad
import com.example.proyectorenthome.core.navigation.RegisterProperty
import com.example.proyectorenthome.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.UploadStatus
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.uploadAsFlow
import io.github.jan.supabase.toJsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPropertyView(navigateToPerfil: (String) -> Unit) {
    val context = LocalContext.current
    // Estados para los datos del formulario
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var ciudad by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var capacidad by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("disponible") }
    val propiedadId by remember { mutableStateOf<Long?>(null) }

    // Estados para el mensaje de éxito o error
    var mensaje by remember { mutableStateOf("") }
    var mensajeColor by remember { mutableStateOf(Color.Unspecified) }
    val composableScope = rememberCoroutineScope()

    var propiedadIdTexto by remember { mutableStateOf("") }
    var urlImagenTexto by remember { mutableStateOf("") }

    // Control del scroll
    val scrollState = rememberScrollState()
    val identities = supabase.auth.currentSessionOrNull()?.user?.id?.toString() ?: "ID no disponible"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Propiedad", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = { navigateToPerfil(identities)}) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                },

            )

        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Property Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PropertyTextField(
                            value = titulo,
                            onValueChange = { titulo = it },
                            label = "Título de Propiedad",
                            leadingIcon = Icons.Default.Home
                        )

                        PropertyTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = "Descripción",
                            multiLine = true
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PropertyTextField(
                                modifier = Modifier.weight(1f),
                                value = direccion,
                                onValueChange = { direccion = it },
                                label = "Dirección",
                                leadingIcon = Icons.Default.LocationOn
                            )
                            PropertyTextField(
                                modifier = Modifier.weight(1f),
                                value = ciudad,
                                onValueChange = { ciudad = it },
                                label = "Ciudad",
                                leadingIcon = Icons.Default.Home
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PropertyTextField(
                                modifier = Modifier.weight(1f),
                                value = precio,
                                onValueChange = { precio = it },
                                label = "Precio/N",
                                leadingIcon = Icons.Default.Add,
                                keyboardType = KeyboardType.Number
                            )
                            PropertyTextField(
                                modifier = Modifier.weight(1f),
                                value = capacidad,
                                onValueChange = { capacidad = it },
                                label = "Capacidad",
                                leadingIcon = Icons.Default.Person,
                                keyboardType = KeyboardType.Number
                            )
                        }
                    }
                }
                var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        selectedImageUri = result.data?.data
                    }
                }
                                // Image Upload Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(onClick = {
                            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                type = "image/*"
                            }
                            launcher.launch(intent)

                        })  {
                            Icon(Icons.Default.Add, contentDescription = "Seleccionar Imagen")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Seleccionar Imagen")
                        }

                        selectedImageUri?.let { uri ->
                            Image(
                                painter = rememberImagePainter(uri),
                                contentDescription = "Imagen Seleccionada",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                // Submit Button
                Button(
                    onClick = {
                        composableScope.launch(Dispatchers.IO) {
                            try {
                                // Inserta la propiedad en la base de datos
                                val insertPropertyJson = supabase.from("properties").insert(
                                    mapOf(
                                        "usuario_id" to identities,
                                        "título" to titulo,
                                        "descripción" to descripcion,
                                        "dirección" to direccion,
                                        "ciudad" to ciudad,
                                        "precio_noche" to precio,
                                        "capacidad" to capacidad,
                                        "estado" to estado
                                    )
                                ) {
                                    select(columns = Columns.list("id"))
                                    single()
                                }.data.toString()

                                val uniqueFileName = "${identities}__${UUID.randomUUID()}.jpg"

                                // Valor de propiedad creada
                                val propiedadId = Json.parseToJsonElement(insertPropertyJson)
                                    .jsonObject["id"]?.jsonPrimitive?.int ?: -1

                                // Actualiza el texto con el ID o un mensaje indicando que no se encontró
                                propiedadIdTexto = propiedadId.let { "Propiedad ID: $it" } ?: "Propiedad no encontrada"

                                selectedImageUri?.let { uri ->
                                    val inputStream = context.contentResolver.openInputStream(uri)
                                    val byteArray = inputStream?.readBytes() ?: throw Exception("No se pudo leer el archivo")

                                    val bucket = supabase.storage.from("Properties")
                                    bucket.uploadAsFlow(uniqueFileName, byteArray).collect {
                                        when (it) {
                                            is UploadStatus.Progress -> println("Progreso: ${it.totalBytesSend.toFloat() / it.contentLength * 100}%")
                                            is UploadStatus.Success -> mensaje = "Imagen subida exitosamente como $uniqueFileName"
                                        }
                                    }
                                } ?: run {
                                    mensaje = "Por favor, selecciona una imagen"
                                }

                                urlImagenTexto = supabase.storage.from("Properties").publicUrl(uniqueFileName)

                                val imagenPropiedad = ImagenPropiedad(
                                    propiedad_id = propiedadId,
                                    url_imagen = urlImagenTexto
                                )

                                supabase.from("imagenes_propiedades").insert(imagenPropiedad) {
                                    select()
                                    single()
                                }

                                // Redirigir hacia atrás en el hilo principal
                                composableScope.launch(Dispatchers.Main) {
                                    // Vuelve al composable anterior
                                    navigateToPerfil(identities)
                                    Toast.makeText(
                                        context,
                                        "Propiedad registrada exitosamente",
                                        Toast.LENGTH_LONG
                                    ).show()

                                }
                            } catch (e: Exception) {
                                composableScope.launch(Dispatchers.Main) {
                                    mensaje = "Error al registrar propiedad: ${e.message}"
                                    mensajeColor = Color.Red
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Registrar Propiedad")
                }


            }
        }
    }
}

@Composable
fun PropertyTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector? = null,
    multiLine: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = !multiLine,
        maxLines = if (multiLine) 4 else 1,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = label) } }
    )
}