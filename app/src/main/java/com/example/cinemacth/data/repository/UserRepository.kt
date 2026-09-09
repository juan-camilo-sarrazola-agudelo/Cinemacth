package com.example.cinemacth.data.repository

import com.example.cinemacth.data.api.UserApiService
import com.example.cinemacth.data.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApiService: UserApiService
) {
    suspend fun getUsers() = userApiService.getUsers()
    
    suspend fun getUserById(id: String) = userApiService.getUserById(id)
    
    suspend fun createUser(user: User) = userApiService.createUser(user)
    
    suspend fun updateUser(id: String, user: User) = userApiService.updateUser(id, user)
    
    suspend fun deleteUser(id: String) = userApiService.deleteUser(id)
}
