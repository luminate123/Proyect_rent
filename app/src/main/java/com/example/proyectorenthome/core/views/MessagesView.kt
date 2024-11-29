package com.example.proyectorenthome.core.views

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectorenthome.Data.ChatDetail
import com.example.proyectorenthome.Data.Conversaciones
import com.example.proyectorenthome.Data.MensajeInsert
import com.example.proyectorenthome.Data.User
import com.example.proyectorenthome.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.ktor.util.Identity.decode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun MessagesView(chatId: Int , navigateToChats:() -> Unit) {
    var messageList by remember { mutableStateOf<List<ChatDetail>>(emptyList()) }
    var messageText by remember { mutableStateOf("") }
    val composableScope = rememberCoroutineScope()

    LaunchedEffect(chatId) {
        loadMessageClientReserve(chatId) { messages ->
            messageList = messages.sortedByDescending { it.fecha_envio }
        }
        loadMessageClientClients(chatId) { messages ->
            messageList = messages.sortedByDescending { it.fecha_envio }
        }
    }

    LaunchedEffect(Unit) {
        realtime(composableScope) { newMessage ->
            messageList = (messageList + newMessage)
                .sortedByDescending { it.fecha_envio }
        }
    }

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
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
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
                        IconButton(onClick = { navigateToChats()}) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Localized description"
                            )
                        }
                        Text(
                            text = "Conversacion",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2C3E50)
                            )
                        )
                    }
                }
            }
        },
        bottomBar = {
            MessageInputBar(
                messageText = messageText,
                onMessageChange = { messageText = it },
                onSendMessage = {
                    composableScope.launch(Dispatchers.IO) {
                        if (messageText.isNotEmpty()) {
                            insertMessage(chatId, messageText)
                            messageText = ""
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Transparent),
            reverseLayout = true,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(messageList) { message ->
                ElegantMessageItem(
                    message = message.contenido,
                    isSent = message.remitente_id != chatId,
                    timestamp = message.fecha_envio.epochSeconds// Conversión de Instant a Long
                )
            }

        }
    }
}

@Composable
fun ElegantMessageItem(
    message: String,
    isSent: Boolean,
    timestamp: Long
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isSent) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isSent) Color(0xFF2196F3) else Color(0xFFE0E0E0),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (!isSent) 0.dp else 16.dp,
                        bottomEnd = if (isSent) 0.dp else 16.dp
                    )
                )
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = message,
                color = if (isSent) Color.White else Color(0xFF333333),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatTimestamp(timestamp),
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF607D8B),
                fontSize = 10.sp
            )
        )
    }
}

@Composable
fun MessageInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = messageText,
            onValueChange = onMessageChange,
            placeholder = {
                Text(
                    "Escribe un mensaje...",
                    color = Color(0xFF607D8B)
                )
            },
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFFF0F4F8), shape = RoundedCornerShape(24.dp))
                .border(
                    width = 1.dp,
                    color = Color(0xFFE0E0E0),
                    shape = RoundedCornerShape(24.dp)
                ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF0F4F8),
                unfocusedContainerColor = Color(0xFFF0F4F8),
                disabledContainerColor = Color(0xFFF0F4F8),
            ),
            shape = RoundedCornerShape(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onSendMessage,
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFF2196F3), shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Enviar mensaje",
                tint = Color.White
            )
        }
    }
}

// Utility function to format timestamp
fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
fun realtime(scope: CoroutineScope, onNewMessage: (ChatDetail) -> Unit) {
    scope.launch {
        try {
            val channel = supabase.channel("mensajes_channel") // Identificador único para el canal
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "mensajes"
            }
            // Escuchar cambios en la tabla "mensajes"
            changeFlow.onEach {
                when (it) {
                    is PostgresAction.Insert -> {
                        val newMessage = it.record.let { record ->
                            ChatDetail(
                                id = (record["id"] as? JsonElement)?.jsonPrimitive?.int ?: 0,
                                conversacion_id = (record["conversacion_id"] as? JsonElement)?.jsonPrimitive?.int ?: 0,
                                contenido = (record["contenido"] as? JsonElement)?.jsonPrimitive?.content ?: "",
                                remitente_id = (record["remitente_id"] as? JsonElement)?.jsonPrimitive?.int ?: 0,
                                fecha_envio = (record["fecha_envio"] as? JsonElement)?.jsonPrimitive?.content?.let {
                                    Instant.parse(it) // Convierte el String ISO 8601 a Instant
                                } ?: Instant.DISTANT_PAST // Valor por defecto en caso de no tener fecha
                            )
                        }

                        if (newMessage != null) onNewMessage(newMessage)
                    }
                    is PostgresAction.Update -> {
                        // Manejar actualizaciones, si es necesario
                    }
                    is PostgresAction.Delete -> {
                        // Manejar eliminaciones, si es necesario
                    }
                    else -> Unit
                }
            }.launchIn(scope)
            channel.subscribe()
        } catch (e: Exception) {
            Log.e("realtime", "Error en realtime: ${e.message}")
        }
    }
}


suspend fun loadMessageClientReserve(userId: Int,onMessageLoaded: (List<ChatDetail>) -> Unit) {
    val session = supabase.auth.currentSessionOrNull()?.user?.id.toString()
     try {
        // Paso 1: Obtener datos del usuario actual
        val response = supabase.from("users").select {
            filter {
                eq("UUID", session)
            }
        }.decodeSingle<User>()

        Log.d("message_loaded", "Usuario actual: $response")

        // Paso 2: Obtener conversación entre el usuario actual y el otro usuario
        val chatLoaded = supabase.from("conversaciones").select {
            filter {
                eq("usuario1_id", response.id)
                eq("usuario2_id", userId)
            }
        }.decodeSingleOrNull<Conversaciones>()

        if (chatLoaded == null) {
            Log.d("message_loaded", "No se encontró conversación entre usuario $response.id y $userId")
            return
        }

        Log.d("message_loaded", "Conversación encontrada: $chatLoaded")

        // Paso 3: Cargar mensajes asociados a la conversación
        val loadMessages = supabase.from("mensajes").select {
            filter {
                eq("conversacion_id", chatLoaded.id)
            }
            order(column = "fecha_envio",Order.DESCENDING)
        }.decodeList<ChatDetail>()


        Log.d("message_loaded", "Mensajes cargados: $loadMessages")

         onMessageLoaded(loadMessages)

     } catch (e: Exception) {
        Log.d("message_loaded", "Error cargando mensajes: ${e.message}")
    }
}
suspend fun loadMessageClientClients(userId: Int,onMessageLoaded: (List<ChatDetail>) -> Unit) {
    val session = supabase.auth.currentSessionOrNull()?.user?.id.toString()
    try {
        // Paso 1: Obtener datos del usuario actual
        val response = supabase.from("users").select {
            filter {
                eq("UUID", session)
            }
        }.decodeSingle<User>()

        Log.d("message_loaded", "Usuario actual: $response")

        // Paso 2: Obtener conversación entre el usuario actual y el otro usuario
        val chatLoaded = supabase.from("conversaciones").select {
            filter {
                eq("usuario1_id",userId )
                eq("usuario2_id",response.id )
            }
        }.decodeSingleOrNull<Conversaciones>()

        if (chatLoaded == null) {
            Log.d("message_loaded", "No se encontró conversación entre usuario $response.id y $userId")
            return
        }

        Log.d("message_loaded", "Conversación encontrada: $chatLoaded")

        // Paso 3: Cargar mensajes asociados a la conversación
        val loadMessages = supabase.from("mensajes").select {
            filter {
                eq("conversacion_id", chatLoaded.id)
            }
            order(column = "fecha_envio",Order.DESCENDING)
        }.decodeList<ChatDetail>()


        Log.d("message_loaded", "Mensajes cargados: $loadMessages")

        onMessageLoaded(loadMessages)

    } catch (e: Exception) {
        Log.d("message_loaded", "Error cargando mensajes: ${e.message}")
    }
}

//reserva
suspend fun insertMessage(userId: Int, contenido: String) {
    val session = supabase.auth.currentSessionOrNull()?.user?.id.toString()
    Log.d("insert_message", "Inicio de la función. Sesión actual: $session")

    try {
        // Paso 1: Obtener datos del usuario actual
        val response = supabase.from("users").select {
            filter {
                eq("UUID", session)
            }
        }.decodeSingle<User>()

        Log.d("insert_message", "Usuario obtenido correctamente: $response")

        // Paso 2: Obtener conversación entre el usuario actual y el otro usuario
        val chatLoaded = supabase.from("conversaciones").select {
            filter {
                eq("usuario1_id", response.id)
                eq("usuario2_id", userId)
            }
        }.decodeSingleOrNull<Conversaciones>()

        if (chatLoaded == null) {
            Log.d("insert_message", "No se encontró conversación entre usuario ${response.id} y $userId. Intentando con los valores intercambiados.")

            // Intentar con los valores intercambiados
            val swappedChatLoaded = supabase.from("conversaciones").select {
                filter {
                    eq("usuario1_id", userId)
                    eq("usuario2_id", response.id)
                }
            }.decodeSingleOrNull<Conversaciones>()

            if (swappedChatLoaded == null) {
                Log.d("insert_message", "Tampoco se encontró conversación con los valores intercambiados.")
                return
            }
        }


        Log.d("insert_message", "Conversación obtenida correctamente: $chatLoaded")

        // Paso 3: Crear instancia del mensaje
        val mensaje = MensajeInsert(
            conversacion_id = chatLoaded?.id?:1,
            remitente_id = response.id,
            contenido = contenido,
            url_imagen = null
        )
        // Paso 3: Insertar mensaje en la conversación encontrada
        val charge = supabase.from("mensajes").insert(mensaje){
            select()
            single()
        }

        Log.d("insert_message", "Mensaje insertado correctamente: $charge")
    } catch (e: Exception) {
        Log.e("insert_message", "Error en la inserción del mensaje: ${e.message}", e)
    }
}
/*
suspend fun insertMessageClient(userId: Int, contenido: String) {
    val session = supabase.auth.currentSessionOrNull()?.user?.id.toString()
    Log.d("insert_message", "Inicio de la función. Sesión actual: $session")

    try {
        // Paso 1: Obtener datos del usuario actual
        val response = supabase.from("users").select {
            filter {
                eq("UUID", session)
            }
        }.decodeSingle<User>()

        Log.d("insert_message", "Usuario obtenido correctamente: $response")

        // Paso 2: Obtener conversación entre el usuario actual y el otro usuario
        val chatLoaded = supabase.from("conversaciones").select {
            filter {
                eq("usuario1_id", response.id)
                eq("usuario2_id", userId)
            }
        }.decodeSingleOrNull<Conversaciones>()

        if (chatLoaded == null) {
            Log.d("insert_message", "No se encontró conversación entre usuario ${response.id} y $userId. Intentando con los valores intercambiados.")

            // Intentar con los valores intercambiados
            val swappedChatLoaded = supabase.from("conversaciones").select {
                filter {
                    eq("usuario1_id", userId)
                    eq("usuario2_id", response.id)
                }
            }.decodeSingleOrNull<Conversaciones>()

            if (swappedChatLoaded == null) {
                Log.d("insert_message", "Tampoco se encontró conversación con los valores intercambiados.")
                return
            }
        }


        Log.d("insert_message", "Conversación obtenida correctamente: $chatLoaded")

        // Paso 3: Crear instancia del mensaje
        val mensaje = MensajeInsert(
            conversacion_id = chatLoaded?.id?:1,
            remitente_id = response.id,
            contenido = contenido,
            url_imagen = null
        )
        // Paso 3: Insertar mensaje en la conversación encontrada
        val charge = supabase.from("mensajes").insert(mensaje){
            select()
            single()
        }

        Log.d("insert_message", "Mensaje insertado correctamente: $charge")
    } catch (e: Exception) {
        Log.e("insert_message", "Error en la inserción del mensaje: ${e.message}", e)
    }
}
*/

