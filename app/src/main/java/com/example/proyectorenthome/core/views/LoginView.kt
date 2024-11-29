package com.example.proyectorenthome.core.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyectorenthome.Data.UserLogin
import com.example.proyectorenthome.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginView(
    navigateToRegister: () -> Unit,
    navigateToHome: () -> Unit
) {
    // Estados para almacenar entradas y mensajes
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var mensajeColor by remember { mutableStateOf(Color.Unspecified) }
    val composableScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                composableScope.launch(Dispatchers.IO) {
                    try {
                        // Intentar iniciar sesión
                        val result = signInWithEmail(email, password)

                        supabase.from("users").update(
                            {
                                set("UUID", supabase.auth.currentSessionOrNull()?.user?.id?.toString())
                            }
                        ){
                            select()
                            filter {
                                eq("email", email)
                            }
                        }

                        // Si no hay excepción, redirigir al home
                        composableScope.launch(Dispatchers.Main) {
                            mensaje = "Inicio de sesión exitoso"
                            mensajeColor = Color.Green
                            navigateToHome()
                        }
                    } catch (e: Exception) {
                        // Error de conexión o credenciales inválidas
                        composableScope.launch(Dispatchers.Main) {
                            mensaje = "Error al iniciar sesión: ${e.message}"
                            mensajeColor = Color.Red
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar Sesión")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { navigateToRegister() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar mensaje de éxito o error
        if (mensaje.isNotEmpty()) {
            Text(
                text = mensaje,
                color = mensajeColor,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

suspend fun signInWithEmail(email: String, password: String): Boolean {
    // Intentar iniciar sesión con Supabase
    supabase.auth.signInWith(Email) {
        this.email = email
        this.password = password
    }
    // Si no hay excepción, el inicio de sesión fue exitoso
    return true
}
