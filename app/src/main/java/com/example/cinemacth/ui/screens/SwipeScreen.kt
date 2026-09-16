package com.example.cinemacth.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cinemacth.data.model.Movie
import com.example.cinemacth.ui.viewmodel.SwipeViewModel
import kotlin.math.roundToInt

@Composable
fun SwipeScreen(
    viewModel: SwipeViewModel,
    onMovieClick: (Int) -> Unit
) {
    val movies by viewModel.moviesToSwipe.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Match de Películas",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text("Desliza a la derecha para Match", style = MaterialTheme.typography.bodySmall)
        
        Spacer(modifier = Modifier.height(32.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (isLoading && movies.isEmpty()) {
                CircularProgressIndicator()
            } else if (movies.isNotEmpty()) {
                // Mostramos solo la de arriba para el swipe
                val movie = movies.first()
                SwipeCard(
                    movie = movie,
                    onSwipeLeft = { viewModel.onDismiss(movie) },
                    onSwipeRight = { viewModel.onMatch(movie) },
                    onClick = { onMovieClick(movie.id) }
                )
            } else {
                Text("No hay más películas por ahora.")
                Button(onClick = { viewModel.loadSwipeMovies() }) {
                    Text("Recargar")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FloatingActionButton(
                onClick = { if (movies.isNotEmpty()) viewModel.onDismiss(movies.first()) },
                containerColor = Color.LightGray
            ) {
                Icon(Icons.Default.Close, contentDescription = "Descartar")
            }
            FloatingActionButton(
                onClick = { if (movies.isNotEmpty()) viewModel.onMatch(movies.first()) },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Favorite, contentDescription = "Match", tint = Color.Red)
            }
        }
    }
}

@Composable
fun SwipeCard(
    movie: Movie,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onClick: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }

    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(500.dp)
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .graphicsLayer {
                rotationZ = offsetX / 20
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (offsetX > 400) onSwipeRight()
                        else if (offsetX < -400) onSwipeLeft()
                        offsetX = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                    }
                )
            }
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = movie.fullPosterUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(movie.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("⭐ ${movie.voteAverage}", color = Color.Yellow)
                }
            }
        }
    }
}
