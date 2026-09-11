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

    private val gson = Gson()

    init {
        performGetUsers()
    }

    fun performGetUsers() {
        executeRequest("GET", "users") { repository.getUsers() }
    }

    fun performGetUserById(id: String) {
        if (id.isEmpty()) {
            _apiState.value = ApiState.Error("El ID es obligatorio")
            return
        }
        executeRequest("GET", "users/$id") { repository.getUserById(id) }
    }

    fun performCreateUser(name: String, email: String, pass: String) {
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
        executeRequest("POST", "users", bodyJson) { repository.createUser(user) }
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
        executeRequest("PUT", "users/$id", bodyJson) { repository.updateUser(id, user) }
    }

    fun performDeleteUser(id: String) {
        if (id.isEmpty()) {
            _apiState.value = ApiState.Error("El ID es obligatorio")
            return
        }
        executeRequest("DELETE", "users/$id") { repository.deleteUser(id) }
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
                        url = it.raw().request.url.toString(),
                        method = method,
                        responseTime = time,
                        requestBody = requestBody
                    )
                } else {
                    val errorMsg = when (it.code()) {
                        400 -> "400 - Solicitud incorrecta"
                        401 -> "401 - No autorizado"
                        403 -> "403 - Prohibido"
                        404 -> "404 - No encontrado"
                        500 -> "500 - Error del servidor"
                        else -> "Error: ${it.code()}"
                    }
                    _apiState.value = ApiState.Error(errorMsg, it.code())
                }
            }
        }
    }
}
