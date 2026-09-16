package com.example.cinemacth.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinemacth.data.model.User
import com.example.cinemacth.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CommunityState {
    object Loading : CommunityState()
    data class Success(val users: List<User>) : CommunityState()
    data class Error(val message: String) : CommunityState()
}

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CommunityState>(CommunityState.Loading)
    val uiState: StateFlow<CommunityState> = _uiState

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = CommunityState.Loading
            try {
                val response = repository.getUsers()
                if (response.isSuccessful) {
                    _uiState.value = CommunityState.Success(response.body() ?: emptyList())
                } else {
                    _uiState.value = CommunityState.Error("Error al cargar usuarios: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = CommunityState.Error("Fallo de red: ${e.message}")
            }
        }
    }

    fun addUser(name: String, email: String) {
        viewModelScope.launch {
            try {
                val newUser = User(name = name, email = email, username = name.lowercase().replace(" ", "."))
                val response = repository.createUser(newUser)
                if (response.isSuccessful) {
                    loadUsers() // Recargar lista para ver el cambio (aunque sea simulado por la API)
                }
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            try {
                val response = repository.updateUser(user.id.toString(), user)
                if (response.isSuccessful) {
                    loadUsers()
                }
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch {
            try {
                val response = repository.deleteUser(id.toString())
                if (response.isSuccessful) {
                    loadUsers()
                }
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
}
