package com.example.proyectorenthome.core.views

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.proyectorenthome.Data.Conversaciones
import com.example.proyectorenthome.Data.User
import com.example.proyectorenthome.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
@Composable
fun ChatView(
    navigateToMessages: (Int) -> Unit,
    navigateToHome: () -> Unit,
    navigateToPerfil: (String) -> Unit
) {
    val chatsReservas = remember { mutableStateOf<List<User>>(emptyList()) }
    val chatsClientes = remember { mutableStateOf<List<User>>(emptyList()) }
    val session1 = supabase.auth.currentSessionOrNull()?.user?.id.toString()
    var selectedTabIndex by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            isLoading = true
            loadChats { chatsReservas.value = it }
            loadChatsClients { chatsClientes.value = it }
        } catch (e: Exception) {
            Log.d("ChatView", "Error cargando datos: ${e.message}")
        } finally {
            isLoading = false
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
                        Text(
                            text = "Chats",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2C3E50)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF2196F3),
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                modifier = Modifier
                                    .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                    .fillMaxWidth(),
                                height = 3.dp,
                                color = Color(0xFF2196F3)
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            selectedContentColor = Color(0xFF2196F3),
                            unselectedContentColor = Color(0xFF90A4AE)
                        ) {
                            Text(
                                "Reservas",
                                modifier = Modifier.padding(vertical = 12.dp),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            selectedContentColor = Color(0xFF2196F3),
                            unselectedContentColor = Color(0xFF90A4AE)
                        ) {
                            Text(
                                "Clientes",
                                modifier = Modifier.padding(vertical = 12.dp),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
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
                    Triple(Icons.Default.Home, "Inicio", navigateToHome),
                    Triple(Icons.Default.Search, "Buscar", {}),
                    Triple(Icons.Default.Email, "Inbox", {}),
                    Triple(Icons.Default.Person, "Perfil", {navigateToPerfil(session1)})
                ).forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.first,
                                contentDescription = item.second,
                                tint = if (index == 2) Color(0xFF2196F3) else Color(0xFF90A4AE)
                            )
                        },
                        label = { Text(item.second) },
                        selected = index == 2,
                        onClick = item.third
                    )
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF2196F3),
                    strokeWidth = 4.dp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Transparent)
            ) {
                when (selectedTabIndex) {
                    0 -> items(chatsReservas.value) { user ->
                        ElegantChatItem(
                            chatName = user.nombre,
                            chatId = user.id,
                            onClick = { navigateToMessages(user.id) }
                        )
                    }
                    1 -> items(chatsClientes.value) { user ->
                        ElegantChatItem(
                            chatName = user.nombre,
                            chatId = user.id,
                            onClick = { navigateToMessages(user.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ElegantChatItem(chatName: String, chatId: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color(0xFF2C3E50)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(0xFF2196F3).copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = chatName.first().uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFF2196F3),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chatName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = "ID: $chatId",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray
                    )
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Go to chat",
                tint = Color(0xFF2196F3)
            )
        }
    }
}

//reservas
suspend fun loadChats(onChatsLoaded: (List<User>) -> Unit) {
    val session = supabase.auth.currentSessionOrNull()?.user?.id.toString()

    try {
        val response = supabase.from("users").select {
            filter {
                eq("UUID", session)
            }
        }.decodeSingle<User>()

        val chatLoaded = supabase.from("conversaciones").select {
            filter {
                eq("usuario1_id", response.id)
            }
        }.decodeList<Conversaciones>()

        val usuario2ID = chatLoaded.map { it.usuario2_id }.distinct()

        val usuarios = if (usuario2ID.isNotEmpty()) {
            supabase.from("users").select {
                filter {
                    isIn("id", usuario2ID)
                }
            }.decodeList<User>()
        } else {
            emptyList()
        }

        // Callback para devolver los usuarios
        onChatsLoaded(usuarios)
    } catch (e: Exception) {
        Log.d("usuario", "Error cargando chats: ${e.message}")
    }
}


//clientes
suspend fun loadChatsClients(onChatsLoaded: (List<User>) -> Unit) {
    val session = supabase.auth.currentSessionOrNull()?.user?.id.toString()

    try {
        val response = supabase.from("users").select {
            filter {
                eq("UUID", session)
            }
        }.decodeSingle<User>()

        val chatLoaded = supabase.from("conversaciones").select {
            filter {
                eq("usuario2_id", response.id)
            }
        }.decodeList<Conversaciones>()

        val usuario1ID = chatLoaded.map { it.usuario1_id }.distinct()

        val clients = if (usuario1ID.isNotEmpty()) {
            supabase.from("users").select {
                filter {
                    isIn("id", usuario1ID)
                }
            }.decodeList<User>()
        } else {
            emptyList()
        }

        Log.d("clients","Cargado:$clients")

        // Callback para devolver los usuarios
        onChatsLoaded(clients)
    } catch (e: Exception) {
        Log.d("usuario", "Error cargando chats: ${e.message}")
    }
}


