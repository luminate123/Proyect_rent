package com.example.proyectorenthome.core.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.proyectorenthome.Data.ImagenPropiedad
import com.example.proyectorenthome.core.navigation.RegisterProperty
import com.example.proyectorenthome.supabase
import io.github.jan.supabase.gotrue.auth
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
@Composable
fun RegisterPropertyView() {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState), // Habilitar desplazamiento
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Campos del formulario
        TextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título") })
        TextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") })
        TextField(value = direccion, onValueChange = { direccion = it }, label = { Text("Dirección") })
        TextField(value = ciudad, onValueChange = { ciudad = it }, label = { Text("Ciudad") })
        TextField(
            value = precio,
            onValueChange = { precio = it },
            label = { Text("Precio por noche") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        TextField(
            value = capacidad,
            onValueChange = { capacidad = it },
            label = { Text("Capacidad") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        TextField(value = estado, onValueChange = { estado = it }, label = { Text("Estado") })

        Spacer(modifier = Modifier.height(16.dp))

        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
            }
        }

        val identities = supabase.auth.currentSessionOrNull()?.user?.id?.toString() ?: "ID no disponible"
        Button(onClick = {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            launcher.launch(intent)
        }) {
            Text(text = "Seleccionar Imagen")
        }
        Spacer(modifier = Modifier.height(16.dp))

        selectedImageUri?.let { uri ->
            Text(text = "Imagen seleccionada: ${uri.path}")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Botón para enviar
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

                        //valor de propiedad creada
                        val propiedadId = Json.parseToJsonElement(insertPropertyJson)
                            .jsonObject["id"]?.jsonPrimitive?.int ?: -1 // Replace -1 with your default value

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
                    } catch (e: Exception) {
                        composableScope.launch(Dispatchers.Main) {
                            mensaje = "Error al registrar propiedad: ${e.message}"
                            mensajeColor = Color.Red
                        }
                    }
                }
            }
        ) {
            Text("Registrar Propiedad")
        }

        if (propiedadIdTexto.isNotEmpty()) {
            Text(text = propiedadIdTexto, color = Color.Black, modifier = Modifier.padding(8.dp))
        }

        if (urlImagenTexto.isNotEmpty()) {
            Text(text = urlImagenTexto, color = Color.Black, modifier = Modifier.padding(8.dp))
        }

        if (mensaje.isNotEmpty()) {
            Text(
                text = mensaje,
                color = mensajeColor,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
