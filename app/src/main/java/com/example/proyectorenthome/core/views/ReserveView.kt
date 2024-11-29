package com.example.proyectorenthome.core.views

import android.graphics.fonts.FontStyle
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.proyectorenthome.Data.Conversaciones
import com.example.proyectorenthome.Data.ImagenPropiedad
import com.example.proyectorenthome.Data.Properties
import com.example.proyectorenthome.Data.PropertyWithImage
import com.example.proyectorenthome.Data.Reserva
import com.example.proyectorenthome.Data.User
import com.example.proyectorenthome.core.navigation.Reserve
import com.example.proyectorenthome.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import kotlin.math.sin

@Composable
fun ReserveView(propertyId: Int, navigateToHome: () -> Unit) {
    var property by remember { mutableStateOf<Properties?>(null) }
    var imagenProperty by remember { mutableStateOf<ImagenPropiedad?>(null) }
    var userId by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var numGuests by remember { mutableStateOf("1") }
    var totalPayment by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var maxGuest by remember { mutableStateOf(1) }
    val session = supabase.auth.currentSessionOrNull()

    LaunchedEffect(propertyId) {
        val loadedProperty = loadProperty(propertyId)
        if (loadedProperty != null) {
            val propertyData = loadedProperty.property
            val imagenpropertyData = loadedProperty.image
            property = propertyData
            imagenProperty = imagenpropertyData

            if (propertyData != null) {
                userId = supabase.auth.currentSessionOrNull()?.user?.id.toString() ?: ""
                maxGuest = propertyData.capacidad?.toInt() ?: 0
            }
        }
    }

    fun calculateTotalCost() {
        val pricePerNight = property?.precio_noche?.toBigDecimal() ?: BigDecimal.ZERO
        val days = calculateDaysBetween(startDate, endDate)
        totalPayment = if (days > 0) {
            (pricePerNight * BigDecimal(days)).toString()
        } else {
            "0.0"
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                colors = listOf(
                    Color(0xFFF0F4F8),
                    Color(0xFFFFFFFF)
                )
            )),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Imagen principal de la propiedad con efecto elegante
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                imagenProperty?.url_imagen?.let { imageUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "Imagen de la propiedad",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = 0.9f
                                scaleX = 1.05f
                                scaleY = 1.05f
                            }
                            .blur(10.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                // Imagen principal superpuesta
                imagenProperty?.url_imagen?.let { imageUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "Imagen de la propiedad",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                            .shadow(
                                elevation = 20.dp,
                                shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)
                            )
                            .border(
                                width = 3.dp,
                                color = Color.White,
                                shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)
                            ),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Información de la propiedad con diseño refinado
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navigateToHome()}) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }

                    Text(
                        text = property?.ciudad ?: "Ubicación",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A365D)
                        )
                    )
                }
                Text(
                    text = property?.ciudad ?: "Ubicación",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A365D)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "A ${property?.dirección}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF4A5568)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "S/ ${property?.precio_noche} por noche",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color(0xFF2C5282),
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            // Tarjeta de reserva con diseño elevado
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF7FAFC)
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    // Selector de fechas con diseño moderno
                    Button(
                        onClick = { showDatePicker = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3182CE)
                        ),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Text(
                            "Seleccionar fechas",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Información de fechas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Check-in: $startDate",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF4A5568)
                            )
                        )
                        Text(
                            "Check-out: $endDate",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF4A5568)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Selector de huéspedes con diseño refinado
                    SelectListOption(
                        maxGuest = maxGuest,
                        numGuests = numGuests,
                        onNumGuestsChange = { numGuests = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Total de pago con efecto de destacado
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFEBF8FF),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Total de pago",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color(0xFF2C5282)
                                )
                            )
                            Text(
                                "S/ $totalPayment",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2C5282)
                                )
                            )
                        }
                    }
                }
            }

            // Botón de reserva con diseño moderno
            Button(
                onClick = {
                    createChat(userId,propertyId)
                    handleReservation(
                        propertyId = propertyId,
                        userId = userId,
                        startDate = startDate,
                        endDate = endDate,
                        numGuests = numGuests.toIntOrNull() ?: 0,
                        totalPayment = totalPayment.toFloatOrNull() ?: 0f,
                        reservationState = "pendiente"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2C5282)
                ),
                shape = RoundedCornerShape(15.dp)
            ) {
                Text(
                    "Confirmar Reserva",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Modal de selección de fechas
            if (showDatePicker) {
                DateRangePickerModal(
                    onDateRangeSelected = { dateRange ->
                        startDate = formatDate(dateRange.first ?: 0)
                        endDate = formatDate(dateRange.second ?: 0)
                        calculateTotalCost()
                        showDatePicker = false
                    },
                    onDismiss = { showDatePicker = false }
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectListOption(maxGuest: Int, numGuests: String, onNumGuestsChange: (String) -> Unit) {

    // Crear una lista de opciones basada en maxGuest
    val list = (1..maxGuest).map { it.toString() }

    var isExpanded by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = !isExpanded }
        ) {
            TextField(
                modifier = Modifier.menuAnchor(),
                value = numGuests, // Mostramos el valor actual de numGuests
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                }
            )

            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
            ) {
                list.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option) },
                        onClick = {
                            onNumGuestsChange(option) // Actualizamos numGuests al seleccionar
                            isExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}



fun calculateDaysBetween(startDate: String, endDate: String): Int {
    if (startDate.isBlank() || endDate.isBlank()) return 0
    val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    return try {
        val start = formatter.parse(startDate)
        val end = formatter.parse(endDate)
        val diff = end.time - start.time
        (diff / (1000 * 60 * 60 * 24)).toInt()
    } catch (e: Exception) {
        0
    }
}


suspend fun loadProperty(propertyId: Int): PropertyWithImage? {
    return withContext(Dispatchers.IO) {
        try {
            val property = supabase.from("properties").select {
                filter { eq("id", propertyId) }
            }.decodeSingle<Properties>()

            val image = supabase.from("imagenes_propiedades").select {
                filter { eq("propiedad_id", propertyId) }
            }.decodeSingle<ImagenPropiedad>()

            Log.d("loadProperty", "Propiedad cargada: $property")
            Log.d("loadProperty", "Imagen cargada: $image")

            // Retornar ambos valores encapsulados en la data class
            PropertyWithImage(property, image)
        } catch (e: Exception) {
            Log.e("loadProperty", "Error cargando propiedad o imagen: ${e.message}", e)
            null
        }
    }
}


fun formatDate(timestamp: Long): String {
    val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    return formatter.format(java.util.Date(timestamp))
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerModal(
    onDateRangeSelected: (Pair<Long?, Long?>) -> Unit,
    onDismiss: () -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateRangeSelected(
                        Pair(
                            dateRangePickerState.selectedStartDateMillis,
                            dateRangePickerState.selectedEndDateMillis
                        )
                    )
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            title = {
                Text(
                    text = "Select date range"
                )
            },
            showModeToggle = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(16.dp)
        )
    }
}
/*
suspend fun loadProperty(propertyId: Int){

    withContext(Dispatchers.IO) {
        try {
            val result = supabase.from("properties").select() {
                filter {
                    eq("id",propertyId)
                }
            }.decodeList<Properties>()
            Log.d("loadProperties", "Propiedades cargadas: $result")

        }catch(e:Exception){
            Log.e("loadProperty", "Error cargando propiedad: ${e.message}", e)
        }
    }
}*/
fun handleReservation(
    propertyId: Int,
    userId: String,
    startDate: String,
    endDate: String,
    numGuests: Int,
    totalPayment: Float,
    reservationState: String
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val reserva = Reserva(
                propiedad_id = propertyId,
                usuario_id = userId,
                fecha_inicio = startDate,
                fecha_fin = endDate,
                num_huespedes = numGuests,
                total_pago = totalPayment,
                estado = reservationState
            )

            // Inserción en la base de datos
            val response = supabase.from("reservas").insert(reserva) {
                select()
                single()
            }

            println("Reserva registrada con éxito: $response")
        } catch (e: Exception) {
            println("Error al registrar la reserva: ${e.message}")
        }
    }
}

fun createChat(
    user1ID: String, user2ID: Int
){
    CoroutineScope(Dispatchers.IO).launch {
        try{
            val response = supabase.from("users").select {
                filter {
                    eq("UUID", user1ID)
                }
            }.decodeSingle<User>()

            val response2 = supabase.from("properties").select {
                filter {
                    eq("id",user2ID)
                }
            }.decodeSingle<Properties>()

            val response3 = supabase.from("users").select {
                filter {
                    eq("UUID",response2.usuario_id)
                }
            }.decodeSingle<User>()

            val conversacion = Conversaciones(
                usuario1_id = response.id,
                usuario2_id = response3.id
            )
            supabase.from("conversaciones").insert(conversacion){
                select()
                single()
            }

            supabase.from("reservas").select()

        }catch (e:Exception){
            Log.d("error","error al cargar")
        }

    }

}
