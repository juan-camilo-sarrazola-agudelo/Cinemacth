package com.example.cinemacth.data.repository

import com.example.cinemacth.data.api.TmdbApiService
import com.example.cinemacth.data.local.FavoriteDao
import com.example.cinemacth.data.model.FavoriteMovie
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepository @Inject constructor(
    private val apiService: TmdbApiService,
    private val favoriteDao: FavoriteDao
) {
    // API Calls
    suspend fun getTrendingMovies() = apiService.getTrendingMovies()
    suspend fun getNowPlayingMovies() = apiService.getNowPlayingMovies()
    suspend fun getTopRatedMovies() = apiService.getTopRatedMovies()
    suspend fun searchMovies(query: String) = apiService.searchMovies(query)
    suspend fun getMovieDetails(movieId: Int) = apiService.getMovieDetails(movieId)
    suspend fun getMovieCredits(movieId: Int) = apiService.getMovieCredits(movieId)
    suspend fun getMovieVideos(movieId: Int) = apiService.getMovieVideos(movieId)
    suspend fun getWatchProviders(movieId: Int) = apiService.getWatchProviders(movieId)
    suspend fun getGenres() = apiService.getGenres()

    // Local DB Calls
    fun getAllFavorites() = favoriteDao.getAllFavorites()
    suspend fun insertFavorite(movie: FavoriteMovie) = favoriteDao.insertFavorite(movie)
    suspend fun deleteFavorite(movie: FavoriteMovie) = favoriteDao.deleteFavorite(movie)
    suspend fun isFavorite(movieId: Int) = favoriteDao.isFavorite(movieId)
}
