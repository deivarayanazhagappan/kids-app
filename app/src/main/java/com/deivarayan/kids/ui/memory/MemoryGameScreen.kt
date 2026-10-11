package com.deivarayan.kids.ui.memory

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.deivarayan.kids.data.MemoryLevel
import com.deivarayan.kids.ui.common.ConfettiOverlay
import com.deivarayan.kids.ui.common.KeepScreenOn
import com.deivarayan.kids.ui.common.PauseOnBackground

private val FACE_DOWN_COLOR = Color(0xFF1976D2)
private val MATCHED_COLOR = Color(0xFF00C48C)

@Composable
fun MemoryGameScreen(
    language: GameLanguage,
    level: MemoryLevel,
    onNextLevel: (MemoryLevel) -> Unit,
    onBackToLevels: () -> Unit,
) {
    KeepScreenOn()
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: MemoryGameViewModel = viewModel(
        factory = MemoryGameViewModelFactory(application, language, level),
    )
    PauseOnBackground(onPause = viewModel::onPause, onResume = viewModel::onResume)
    val state by viewModel.uiState.collectAsState()
    val english = language == GameLanguage.ENGLISH

    var confettiTrigger by remember { mutableIntStateOf(0) }
    LaunchedEffect(state.isSessionComplete) {
        if (state.isSessionComplete) confettiTrigger++
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text(if (english) "Memory" else "Memory-Spiel") })
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
            ) {
                Text(
                    text = if (english) {
                        "Pairs: ${state.matchedPairs} / ${state.totalPairs}   Moves: ${state.moves}"
                    } else {
                        "Paare: ${state.matchedPairs} / ${state.totalPairs}   Züge: ${state.moves}"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(level.columns),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    itemsIndexed(state.tiles, key = { _, tile -> tile.id }) { index, tile ->
                        MemoryTileView(
                            emoji = tile.emoji,
                            face = state.faces.getOrElse(index) { TileFace.DOWN },
                            enabled = state.inputEnabled,
                            onClick = { viewModel.onTileClicked(index) },
                        )
                    }
                }
            }
        }

        if (state.isSessionComplete) {
            MemoryCompleteOverlay(
                language = language,
                moves = state.moves,
                nextLevel = viewModel.nextLevel,
                onPlayAgain = viewModel::playAgain,
                onNextLevel = onNextLevel,
                onBackToLevels = onBackToLevels,
            )
        }

        ConfettiOverlay(trigger = confettiTrigger, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun MemoryTileView(emoji: String, face: TileFace, enabled: Boolean, onClick: () -> Unit) {
    val color = when (face) {
        TileFace.DOWN -> FACE_DOWN_COLOR
        TileFace.UP -> MaterialTheme.colorScheme.surfaceVariant
        TileFace.MATCHED -> MATCHED_COLOR
    }
    val scale by animateFloatAsState(targetValue = if (face == TileFace.DOWN) 1f else 1.05f, label = "tileScale")

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(enabled = enabled && face == TileFace.DOWN, onClick = onClick)
            .background(color, RoundedCornerShape(10.dp))
            .scale(scale),
        contentAlignment = Alignment.Center,
    ) {
        if (face != TileFace.DOWN) {
            Text(text = emoji, fontSize = 22.sp)
        }
    }
}

@Composable
private fun MemoryCompleteOverlay(
    language: GameLanguage,
    moves: Int,
    nextLevel: MemoryLevel?,
    onPlayAgain: () -> Unit,
    onNextLevel: (MemoryLevel) -> Unit,
    onBackToLevels: () -> Unit,
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
            Text(text = "🎉", fontSize = 72.sp)
            Text(
                text = if (english) "You found all the pairs!" else "Du hast alle Paare gefunden!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            )
            Text(
                text = if (english) "In $moves moves" else "In $moves Zügen",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 24.dp),
            )
            MemoryActionButton(
                text = if (english) "Play Again" else "Nochmal spielen",
                color = Color(0xFF1976D2),
                onClick = onPlayAgain,
            )
            if (nextLevel != null) {
                MemoryActionButton(
                    text = if (english) "Next Level →" else "Nächstes Level →",
                    color = Color(0xFF00C48C),
                    onClick = { onNextLevel(nextLevel) },
                    topPadding = 12.dp,
                )
            }
            MemoryActionButton(
                text = if (english) "Back to Levels" else "Zurück zu den Leveln",
                color = Color(0xFF546E7A),
                onClick = onBackToLevels,
                topPadding = 12.dp,
            )
        }
    }
}

@Composable
private fun MemoryActionButton(text: String, color: Color, onClick: () -> Unit, topPadding: Dp = 0.dp) {
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
