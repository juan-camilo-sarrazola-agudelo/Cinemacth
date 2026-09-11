package com.example.cinemacth.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cinemacth.data.model.User
import com.example.cinemacth.ui.viewmodel.ApiState
import com.example.cinemacth.ui.viewmodel.ApiTesterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiTesterScreen(viewModel: ApiTesterViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("LISTAR", "REGISTRAR", "EDITAR", "BORRAR")
    val apiState by viewModel.apiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Gestión de Comunidad") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { 
                            selectedTab = index 
                            if (index == 0) viewModel.performGetUsers()
                        },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> ListSection(apiState)
                1 -> PostSection(viewModel, apiState)
                2 -> PutSection(viewModel, apiState)
                3 -> DeleteSection(viewModel, apiState)
            }
        }
    }
}

@Composable
fun ListSection(state: ApiState<Any>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Miembros de la Comunidad", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        when (state) {
            is ApiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            is ApiState.Error -> Text("Error: ${state.message}", color = Color.Red)
            is ApiState.Success -> {
                val users = state.data as? List<*>
                if (users != null) {
                    LazyColumn {
                        items(users) { item ->
                            val user = item as? User
                            user?.let { UserListItem(it) }
                        }
                    }
                } else if (state.data is User) {
                    UserListItem(state.data as User)
                }
            }
            else -> Text("Pulsa LISTAR para cargar.")
        }
    }
}

@Composable
fun UserListItem(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(text = user.name, fontWeight = FontWeight.Bold)
                Text(text = "ID: ${user.id} • ${user.email}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun PostSection(viewModel: ApiTesterViewModel, state: ApiState<Any>) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text("Nuevo Registro", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre Completo") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo Electrónico") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = { viewModel.performCreateUser(name, email, "") },
            modifier = Modifier.padding(top = 16.dp).fillMaxWidth()
        ) {
            Text("Registrar Miembro")
        }

        if (state is ApiState.Success && state.method == "POST") {
            SuccessMessage("¡${(state.data as User).name} ha sido registrado!")
        } else if (state is ApiState.Error) {
            Text(state.message, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun PutSection(viewModel: ApiTesterViewModel, state: ApiState<Any>) {
    var userId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text("Editar Información", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = userId, onValueChange = { userId = it }, label = { Text("ID del Miembro") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nuevo Nombre") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Nuevo Correo") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = { viewModel.performUpdateUser(userId, name, email) },
            modifier = Modifier.padding(top = 16.dp).fillMaxWidth()
        ) {
            Text("Guardar Cambios")
        }

        if (state is ApiState.Success && state.method == "PUT") {
            SuccessMessage("Cambios guardados correctamente.")
        }
    }
}

@Composable
fun DeleteSection(viewModel: ApiTesterViewModel, state: ApiState<Any>) {
    var userId by remember { mutableStateOf("") }
    var showConfirm by remember { mutableStateOf(false) }

    Column {
        Text("Baja de Miembro", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = userId, onValueChange = { userId = it }, label = { Text("ID del Miembro a eliminar") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = { showConfirm = true },
            modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Eliminar de la Comunidad", color = Color.White)
        }

        if (state is ApiState.Success && state.method == "DELETE") {
            SuccessMessage("Miembro eliminado de la base de datos.")
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
                    }) { Text("ELIMINAR") }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirm = false }) { Text("CANCELAR") }
                }
            )
        }
    }
}

@Composable
fun SuccessMessage(message: String) {
    Row(
        modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
        Spacer(Modifier.width(8.dp))
        Text(message, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
    }
}
