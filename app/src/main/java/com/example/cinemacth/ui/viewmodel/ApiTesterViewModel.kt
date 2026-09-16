package com.example.cinemacth.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinemacth.data.model.User
import com.example.cinemacth.data.repository.UserRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class ApiTesterViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _apiState = MutableStateFlow<ApiState<Any>>(ApiState.Idle)
    val apiState: StateFlow<ApiState<Any>> = _apiState

    // Única fuente de verdad para la lista de usuarios en la UI
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val gson = Gson()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _apiState.value = ApiState.Loading
            val response = repository.getUsers()
            if (response.isSuccessful) {
                _users.value = response.body() ?: emptyList()
                _apiState.value = ApiState.Idle
            } else {
                _apiState.value = ApiState.Error("Error al cargar usuarios")
            }
        }
    }

    fun performGetUsers() {
        loadUsers()
    }

    fun performGetUserById(id: String) {
        if (id.isEmpty()) {
            _apiState.value = ApiState.Error("El ID es obligatorio")
            return
        }
        executeRequest("GET", "users/$id") { repository.getUserById(id) }
    }

    fun performCreateUser(name: String, email: String) {
        if (name.isEmpty() || email.isEmpty()) {
            _apiState.value = ApiState.Error("Nombre y Email son obligatorios")
            return
        }
        val user = User(
            name = name, 
            email = email, 
            username = name.lowercase().replace(" ", "."),
            phone = "555-1234"
        )
        val bodyJson = gson.toJson(user)
        executeRequest("POST", "users", bodyJson) { 
            val resp = repository.createUser(user)
            if (resp.isSuccessful) {
                // Actualizamos la lista local inmediatamente
                _users.value = _users.value + (resp.body()!!)
            }
            resp
        }
    }

    fun performUpdateUser(id: String, name: String, email: String) {
        if (id.isEmpty() || name.isEmpty() || email.isEmpty()) {
            _apiState.value = ApiState.Error("ID, Nombre y Email son obligatorios")
            return
        }
        val user = User(
            id = id.toIntOrNull(),
            name = name, 
            email = email,
            username = "updated.user"
        )
        val bodyJson = gson.toJson(user)
        executeRequest("PUT", "users/$id", bodyJson) { 
            val resp = repository.updateUser(id, user)
            if (resp.isSuccessful) {
                // Actualizamos la lista local inmediatamente
                _users.value = _users.value.map { if (it.id.toString() == id) resp.body()!! else it }
            }
            resp
        }
    }

    fun performDeleteUser(id: String) {
        if (id.isEmpty()) {
            _apiState.value = ApiState.Error("El ID es obligatorio")
            return
        }
        executeRequest("DELETE", "users/$id") { 
            val resp = repository.deleteUser(id)
            if (resp.isSuccessful) {
                // Actualizamos la lista local inmediatamente
                _users.value = _users.value.filter { it.id.toString() != id }
            }
            resp
        }
    }

    private fun <T> executeRequest(
        method: String,
        endpoint: String,
        requestBody: String? = null,
        call: suspend () -> Response<T>
    ) {
        viewModelScope.launch {
            _apiState.value = ApiState.Loading
            var response: Response<T>? = null
            val time = measureTimeMillis {
                try {
                    response = call()
                } catch (e: Exception) {
                    _apiState.value = ApiState.Error("Fallo de red: ${e.message}")
                    return@launch
                }
            }

            response?.let {
                if (it.isSuccessful) {
                    _apiState.value = ApiState.Success(
                        data = it.body() ?: "Operación exitosa",
                        statusCode = it.code(),
                        url = "Local/InMemory",
                        method = method,
                        responseTime = time,
                        requestBody = requestBody
                    )
                } else {
                    _apiState.value = ApiState.Error("Error: ${it.code()}", it.code())
                }
            }
        }
    }
}
