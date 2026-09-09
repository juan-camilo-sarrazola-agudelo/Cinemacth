package com.example.cinemacth.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cinemacth.ui.viewmodel.ApiState
import com.example.cinemacth.ui.viewmodel.ApiTesterViewModel
import com.google.gson.GsonBuilder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiTesterScreen(viewModel: ApiTesterViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("GET", "POST", "PUT", "DELETE")
    val apiState by viewModel.apiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("API Tester - Usuarios") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> GetSection(viewModel)
                1 -> PostSection(viewModel)
                2 -> PutSection(viewModel)
                3 -> DeleteSection(viewModel)
            }

            Spacer(modifier = Modifier.height(24.dp))

            ResponseSection(apiState)
        }
    }
}

@Composable
fun GetSection(viewModel: ApiTesterViewModel) {
    var userId by remember { mutableStateOf("") }
    Column {
        OutlinedTextField(
            value = userId,
            onValueChange = { userId = it },
            label = { Text("ID Usuario (Opcional para todos)") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { 
                if (userId.isEmpty()) viewModel.performGetUsers() 
                else viewModel.performGetUserById(userId) 
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Enviar GET")
        }
    }
}

@Composable
fun PostSection(viewModel: ApiTesterViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    Column {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = { viewModel.performCreateUser(name, email, pass) },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Enviar POST")
        }
    }
}

@Composable
fun PutSection(viewModel: ApiTesterViewModel) {
    var userId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Column {
        OutlinedTextField(value = userId, onValueChange = { userId = it }, label = { Text("ID Usuario") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nuevo Nombre") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Nuevo Email") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = { viewModel.performUpdateUser(userId, name, email) },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Enviar PUT")
        }
    }
}

@Composable
fun DeleteSection(viewModel: ApiTesterViewModel) {
    var userId by remember { mutableStateOf("") }
    var showConfirm by remember { mutableStateOf(false) }

    Column {
        OutlinedTextField(value = userId, onValueChange = { userId = it }, label = { Text("ID Usuario") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = { showConfirm = true },
            modifier = Modifier.padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Eliminar Usuario", color = Color.White)
        }

        if (showConfirm) {
            AlertDialog(
                onDismissRequest = { showConfirm = false },
                title = { Text("Confirmar") },
                text = { Text("¿Seguro que quieres eliminar al usuario $userId?") },
                confirmButton = {
                    TextButton(onClick = { 
                        viewModel.performDeleteUser(userId)
                        showConfirm = false
                    }) { Text("SÍ, ELIMINAR") }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirm = false }) { Text("CANCELAR") }
                }
            )
        }
    }
}

@Composable
fun ResponseSection(state: ApiState<Any>) {
    val gson = remember { GsonBuilder().setPrettyPrinting().create() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("RESPUESTA DEL SERVIDOR", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            when (state) {
                is ApiState.Idle -> Text("Esperando petición...")
                is ApiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                is ApiState.Error -> {
                    Text("ERROR", color = Color.Red, fontWeight = FontWeight.Bold)
                    Text(state.message)
                    state.statusCode?.let { Text("Código: $it") }
                }
                is ApiState.Success -> {
                    Text("Método: ${state.method}", fontWeight = FontWeight.Bold)
                    Text("URL: ${state.url}", fontSize = 12.sp)
                    Text("Status: ${state.statusCode}", color = if (state.statusCode < 300) Color(0xFF4CAF50) else Color.Red)
                    Text("Tiempo: ${state.responseTime}ms")
                    
                    state.requestBody?.let {
                        Spacer(Modifier.height(8.dp))
                        Text("Body Enviado:", fontWeight = FontWeight.Bold)
                        Box(Modifier.background(Color.LightGray.copy(alpha = 0.2f)).padding(8.dp).fillMaxWidth()) {
                            Text(it, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text("JSON Respuesta:", fontWeight = FontWeight.Bold)
                    val prettyJson = gson.toJson(state.data)
                    Box(
                        modifier = Modifier
                            .background(Color.DarkGray)
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = prettyJson,
                            color = Color.Green,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
