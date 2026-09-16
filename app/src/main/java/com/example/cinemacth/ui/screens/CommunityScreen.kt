package com.example.cinemacth.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cinemacth.data.model.User
import com.example.cinemacth.ui.viewmodel.CommunityState
import com.example.cinemacth.ui.viewmodel.CommunityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(viewModel: CommunityViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showUserDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<User?>(null) }

    val lazyListState = rememberLazyListState()
    var isFabVisible by remember { mutableStateOf(true) }

    LaunchedEffect(lazyListState) {
        var previousIndex = 0
        var previousScrollOffset = 0
        snapshotFlow { Pair(lazyListState.firstVisibleItemIndex, lazyListState.firstVisibleItemScrollOffset) }
            .collect { (currentIndex, currentScrollOffset) ->
                if (currentIndex > previousIndex) {
                    isFabVisible = false
                } else if (currentIndex < previousIndex) {
                    isFabVisible = true
                } else {
                    if (currentScrollOffset > previousScrollOffset) {
                        isFabVisible = false
                    } else if (currentScrollOffset < previousScrollOffset) {
                        isFabVisible = true
                    }
                }
                previousIndex = currentIndex
                previousScrollOffset = currentScrollOffset
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comunidad CineMatch") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = isFabVisible,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                FloatingActionButton(onClick = {
                    userToEdit = null
                    showUserDialog = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Usuario")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is CommunityState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is CommunityState.Error -> Text(
                    text = state.message,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
                is CommunityState.Success -> {
                    if (state.users.isEmpty()) {
                        Text("No hay miembros en la comunidad todavía.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            state = lazyListState,
                            contentPadding = PaddingValues(bottom = 80.dp),
                            modifier = Modifier.fillMaxSize().padding(8.dp)
                        ) {
                            items(state.users) { user ->
                                UserCard(
                                    user = user,
                                    onEdit = {
                                        userToEdit = user
                                        showUserDialog = true
                                    },
                                    onDelete = { user.id?.let { viewModel.deleteUser(it) } }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showUserDialog) {
            UserFormDialog(
                user = userToEdit,
                onDismiss = { showUserDialog = false },
                onConfirm = { name, email ->
                    if (userToEdit == null) {
                        viewModel.addUser(name, email)
                    } else {
                        viewModel.updateUser(userToEdit!!.copy(name = name, email = email))
                    }
                    showUserDialog = false
                }
            )
        }
    }
}

@Composable
fun UserCard(user: User, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(text = user.email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun UserFormDialog(
    user: User?,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(user?.name ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (user == null) "Nuevo Miembro" else "Editar Miembro") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, email) },
                enabled = name.isNotBlank() && email.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
