package com.example.proyectorenthome.core.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.proyectorenthome.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun RegisterView(navigateToLogin: () -> Unit) {
    // Estados para almacenar la entrada del usuario
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contraseña by remember { mutableStateOf("") }
    var teléfono by remember { mutableStateOf("") }

    // Estado para mostrar mensajes
    var mensaje by remember { mutableStateOf("") }
    var mensajeColor by remember { mutableStateOf(Color.Unspecified) } // Color del mensaje
    val composableScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = contraseña,
            onValueChange = { contraseña = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = teléfono,
            onValueChange = { teléfono = it },
            label = { Text("Teléfono") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                composableScope.launch(Dispatchers.IO) {
                    try {
                        supabase.from("users").insert(
                            mapOf(
                                "nombre" to nombre,
                                "email" to email,
                                "contraseña" to contraseña,
                                "teléfono" to teléfono
                            )
                        ) {
                            select()
                            single()
                        }

                        // Llamar a signUpNewUser con los valores del formulario
                        signUpNewUser(email, contraseña)

                        // Actualizar el mensaje en el hilo principal
                        composableScope.launch(Dispatchers.Main) {
                            mensaje = "Usuario registrado con éxito"
                            mensajeColor = Color.Green // Color para el éxito
                        }
                    } catch (e: Exception) {
                        // Manejo de errores y actualizar el mensaje
                        composableScope.launch(Dispatchers.Main) {
                            mensaje = "Error al registrar usuario: ${e.message}"
                            mensajeColor = Color.Red // Color para el error
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { navigateToLogin() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ya tengo una cuenta")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar el mensaje con color
        if (mensaje.isNotEmpty()) {
            Text(
                text = mensaje,
                color = mensajeColor,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}


suspend fun signUpNewUser(email: String, password: String) {
    supabase.auth.signUpWith(Email) {
        this.email = email
        this.password = password
    }
}
