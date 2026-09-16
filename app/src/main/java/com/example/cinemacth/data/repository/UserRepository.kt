package com.example.cinemacth.data.repository

import com.example.cinemacth.data.api.UserApiService
import com.example.cinemacth.data.model.User
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApiService: UserApiService
) {
    // Fuente de verdad en memoria para la sesión activa
    private val userCache = mutableListOf<User>()

    suspend fun getUsers(): Response<List<User>> {
        return if (userCache.isEmpty()) {
            val response = userApiService.getUsers()
            if (response.isSuccessful) {
                response.body()?.let { userCache.addAll(it) }
            }
            response
        } else {
            Response.success(userCache.toList())
        }
    }

    suspend fun getUserById(id: String): Response<User> {
        val user = userCache.find { it.id.toString() == id }
        return if (user != null) {
            Response.success(user)
        } else {
            userApiService.getUserById(id)
        }
    }

    fun createUser(user: User): Response<User> {
        // Generamos un ID local incremental
        val nextId = (userCache.maxOfOrNull { it.id ?: 0 } ?: 0) + 1
        val newUser = user.copy(id = nextId)
        userCache.add(newUser)
        return Response.success(newUser)
    }

    fun updateUser(id: String, user: User): Response<User> {
        val index = userCache.indexOfFirst { it.id.toString() == id }
        return if (index != -1) {
            val updatedUser = user.copy(id = id.toIntOrNull() ?: user.id)
            userCache[index] = updatedUser
            Response.success(updatedUser)
        } else {
            val errorBody = "Usuario no encontrado".toResponseBody("text/plain".toMediaTypeOrNull())
            Response.error(404, errorBody)
        }
    }

    fun deleteUser(id: String): Response<Unit> {
        val removed = userCache.removeIf { it.id.toString() == id }
        return if (removed) {
            Response.success(Unit)
        } else {
            val errorBody = "No se pudo eliminar".toResponseBody("text/plain".toMediaTypeOrNull())
            Response.error(404, errorBody)
        }
    }
}
