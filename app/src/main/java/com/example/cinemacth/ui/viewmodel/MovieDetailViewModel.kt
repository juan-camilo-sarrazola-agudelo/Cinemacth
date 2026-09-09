package com.example.cinemacth.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinemacth.data.model.*
import com.example.cinemacth.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _movieDetail = MutableStateFlow<MovieDetail?>(null)
    val movieDetail: StateFlow<MovieDetail?> = _movieDetail

    private val _cast = MutableStateFlow<List<Cast>>(emptyList())
    val cast: StateFlow<List<Cast>> = _cast

    private val _videos = MutableStateFlow<List<Video>>(emptyList())
    val videos: StateFlow<List<Video>> = _videos

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadMovieDetails(movieId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _movieDetail.value = repository.getMovieDetails(movieId)
                _cast.value = repository.getMovieCredits(movieId).cast
                _videos.value = repository.getMovieVideos(movieId).results
                checkFavoriteStatus(movieId)
            } catch (e: Exception) {
                // Manejar error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun checkFavoriteStatus(movieId: Int) {
        _isFavorite.value = repository.isFavorite(movieId)
    }

    fun toggleFavorite(movie: MovieDetail) {
        viewModelScope.launch {
            if (_isFavorite.value) {
                repository.deleteFavorite(
                    FavoriteMovie(
                        id = movie.id,
                        title = movie.title,
                        posterPath = movie.posterPath,
                        voteAverage = movie.voteAverage,
                        releaseDate = movie.releaseDate
                    )
                )
            } else {
                repository.insertFavorite(
                    FavoriteMovie(
                        id = movie.id,
                        title = movie.title,
                        posterPath = movie.posterPath,
                        voteAverage = movie.voteAverage,
                        releaseDate = movie.releaseDate
                    )
                )
            }
            _isFavorite.value = !_isFavorite.value
        }
    }
}
