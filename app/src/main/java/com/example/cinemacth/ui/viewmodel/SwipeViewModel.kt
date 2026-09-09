package com.example.cinemacth.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinemacth.data.model.FavoriteMovie
import com.example.cinemacth.data.model.Movie
import com.example.cinemacth.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SwipeViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _moviesToSwipe = MutableStateFlow<List<Movie>>(emptyList())
    val moviesToSwipe: StateFlow<List<Movie>> = _moviesToSwipe

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadSwipeMovies()
    }

    fun loadSwipeMovies() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Usamos trending o populares como base para el swipe
                val response = repository.getTrendingMovies()
                _moviesToSwipe.value = response.results.shuffled()
            } catch (e: Exception) {
                // Manejar error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onMatch(movie: Movie) {
        viewModelScope.launch {
            repository.insertFavorite(
                FavoriteMovie(
                    id = movie.id,
                    title = movie.title,
                    posterPath = movie.posterPath,
                    voteAverage = movie.voteAverage,
                    releaseDate = movie.releaseDate
                )
            )
            removeMovieFromList(movie)
        }
    }

    fun onDismiss(movie: Movie) {
        removeMovieFromList(movie)
    }

    private fun removeMovieFromList(movie: Movie) {
        _moviesToSwipe.value = _moviesToSwipe.value.filter { it.id != movie.id }
        if (_moviesToSwipe.value.isEmpty()) {
            loadSwipeMovies() // Recargar si se acaban
        }
    }
}
