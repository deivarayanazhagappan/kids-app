package com.deivarayan.kids.ui.simon

import android.app.Application
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deivarayan.kids.data.GameLanguage
import com.deivarayan.kids.ui.common.KeepScreenOn
import com.deivarayan.kids.ui.common.PauseOnBackground

private val TILE_COLORS = listOf(Color(0xFFD32F2F), Color(0xFF1976D2), Color(0xFF00C48C), Color(0xFFFFC107))

@Composable
fun SimonSaysScreen(language: GameLanguage, onBackToGames: () -> Unit) {
    KeepScreenOn()
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: SimonSaysViewModel = viewModel(
        factory = SimonSaysViewModelFactory(application, language),
    )
    PauseOnBackground(onPause = viewModel::onPause, onResume = viewModel::onResume)
    val state by viewModel.uiState.collectAsState()
    val english = language == GameLanguage.ENGLISH

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text(if (english) "Simon Says" else "Simon sagt") })
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = when {
                        state.phase == SimonPhase.SHOWING -> if (english) "Watch..." else "Schau zu..."
                        state.phase == SimonPhase.WAITING_INPUT -> if (english) "Your turn!" else "Du bist dran!"
                        else -> if (english) "Game Over" else "Spiel vorbei"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Text(
                    text = if (english) "Length: ${state.sequence.size}" else "Länge: ${state.sequence.size}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 24.dp),
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    itemsIndexed(TILE_COLORS, key = { index, _ -> index }) { index, color ->
                        SimonTileView(
                            color = color,
                            isActive = state.activeTileIndex == index,
                            enabled = state.phase == SimonPhase.WAITING_INPUT,
                            onClick = { viewModel.onTileClicked(index) },
                        )
                    }
                }
            }
        }

        if (state.phase == SimonPhase.GAME_OVER) {
            SimonGameOverOverlay(
                language = language,
                reachedLength = state.reachedLength,
                onPlayAgain = viewModel::playAgain,
                onBackToGames = onBackToGames,
            )
        }
    }
}

@Composable
private fun SimonTileView(color: Color, isActive: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(targetValue = if (isActive) 1.08f else 1f, label = "simonTileScale")
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(enabled = enabled, onClick = onClick)
            .background(if (isActive) color else color.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .scale(scale),
    )
}

@Composable
private fun SimonGameOverOverlay(
    language: GameLanguage,
    reachedLength: Int,
    onPlayAgain: () -> Unit,
    onBackToGames: () -> Unit,
) {
    val english = language == GameLanguage.ENGLISH
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "🎮", fontSize = 72.sp)
            Text(
                text = if (english) "Game Over!" else "Spiel vorbei!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            )
            Text(
                text = if (english) "You reached length $reachedLength" else "Du hast Länge $reachedLength erreicht",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 24.dp),
            )
            ActionButton(text = if (english) "Play Again" else "Nochmal spielen", color = Color(0xFF1976D2), onClick = onPlayAgain)
            ActionButton(
                text = if (english) "Back to Games" else "Zurück zu den Spielen",
                color = Color(0xFF546E7A),
                onClick = onBackToGames,
                topPadding = 12.dp,
            )
        }
    }
}

@Composable
private fun ActionButton(text: String, color: Color, onClick: () -> Unit, topPadding: Dp = 0.dp) {
    Box(
        modifier = Modifier
            .padding(top = topPadding)
            .fillMaxWidth()
            .height(52.dp)
            .clickable(onClick = onClick)
            .background(color, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
