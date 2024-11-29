package com.example.proyectorenthome.core.views

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.proyectorenthome.Data.ImagenPropiedad
import com.example.proyectorenthome.Data.Properties
import com.example.proyectorenthome.Data.PropertyWithImage
import com.example.proyectorenthome.Data.Reserva
import com.example.proyectorenthome.core.navigation.Reserve
import com.example.proyectorenthome.supabase
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import kotlin.math.sin

@Composable
fun ReserveView(propertyId: Int) {
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
        val loadedProperty = loadProperty(propertyId) // Llamar a la función suspendida
        if (loadedProperty != null) {
            val propertyData = loadedProperty.property // Acceder a la propiedad
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

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Imagen principal de la propiedad
        imagenProperty?.url_imagen?.let { imageUrl ->
            // Carga de imagen (puedes usar Glide o Coil)
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = "Imagen de la propiedad",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
        }

        // Información de la propiedad
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = property?.ciudad ?: "Ubicación desconocida",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "A ${property?.dirección}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "S/ ${property?.precio_noche} noche",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selección de rango de fechas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Blue
                    )
                ) {
                    Text("Seleccionar rango de fechas")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Fecha Inicio: $startDate", style = MaterialTheme.typography.bodyMedium)
                Text("Fecha Fin: $endDate", style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(8.dp))

                // Selector de número de huéspedes
                SelectListOption(
                    maxGuest = maxGuest,
                    numGuests = numGuests,
                    onNumGuestsChange = { numGuests = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Mostrar total de pago
                OutlinedTextField(
                    value = totalPayment,
                    onValueChange = {},
                    label = { Text("Total de pago (S/)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Confirmar Reserva
        Button(
            onClick = {
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
                .padding(horizontal = 16.dp)
        ) {
            Text("Confirmar Reserva")
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
