package com.example.cinemacth.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.cinemacth.data.model.FavoriteMovie

@Database(entities = [FavoriteMovie::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
}
