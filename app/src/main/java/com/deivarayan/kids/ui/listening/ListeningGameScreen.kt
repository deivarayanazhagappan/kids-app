package com.deivarayan.kids.ui.listening

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deivarayan.kids.data.GameLanguage
import com.deivarayan.kids.data.ListeningLevel
import com.deivarayan.kids.ui.common.ConfettiOverlay
import com.deivarayan.kids.ui.common.KeepScreenOn
import com.deivarayan.kids.ui.common.PauseOnBackground

private val NEUTRAL_TILE_COLOR = Color(0xFF546E7A)
private val CORRECT_TILE_COLOR = Color(0xFF00C48C)
private val WRONG_TILE_COLOR = Color(0xFFD32F2F)
private const val ROUND_LIMIT = 30

@Composable
fun ListeningGameScreen(
    language: GameLanguage,
    level: ListeningLevel,
    onNextLevel: (ListeningLevel) -> Unit,
    onBackToLevels: () -> Unit,
) {
    KeepScreenOn()
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: ListeningGameViewModel = viewModel(
        factory = ListeningGameViewModelFactory(application, language, level),
    )
    PauseOnBackground(onPause = viewModel::onPause, onResume = viewModel::onResume)
    val state by viewModel.uiState.collectAsState()

    var confettiTrigger by remember { mutableIntStateOf(0) }
    LaunchedEffect(state.isSessionComplete, state.isFinalLevel) {
        if (state.isSessionComplete && state.isFinalLevel) {
            confettiTrigger++
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text(if (language == GameLanguage.ENGLISH) "Listening" else "Hören") })
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
                    text = if (language == GameLanguage.ENGLISH) {
                        "Round ${state.roundsCompleted} / $ROUND_LIMIT"
                    } else {
                        "Runde ${state.roundsCompleted} / $ROUND_LIMIT"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 24.dp),
                )

                if (state.options.isEmpty()) {
                    Text(text = "Loading…", fontSize = 20.sp)
                } else {
                    val columns = when (level.tileCount) {
                        2 -> 2
                        4 -> 2
                        else -> 4
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        itemsIndexed(state.options, key = { index, _ -> index }) { index, number ->
                            NumberTile(
                                number = number,
                                status = state.tileStatuses.getOrElse(index) { TileStatus.NEUTRAL },
                                enabled = state.inputEnabled,
                                onClick = { viewModel.onTileSelected(index) },
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = state.showThumbsUp,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "👍", fontSize = 140.sp)
            }
        }

        if (state.isSessionComplete) {
            SessionCompleteOverlay(
                language = language,
                isFinalLevel = state.isFinalLevel,
                nextLevel = viewModel.nextLevel,
                onPlayAgain = viewModel::playAgain,
                onNextLevel = onNextLevel,
                onBackToLevels = onBackToLevels,
            )
        }

        if (state.isShowingIntro) {
            IntroOverlay(language = language, introText = viewModel.introText)
        }

        ConfettiOverlay(trigger = confettiTrigger, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun IntroOverlay(language: GameLanguage, introText: String) {
    val english = language == GameLanguage.ENGLISH
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "👂🔢", fontSize = 56.sp)
            Text(
                text = if (english) "How to play" else "So spielst du",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            )
            Text(
                text = introText,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SessionCompleteOverlay(
    language: GameLanguage,
    isFinalLevel: Boolean,
    nextLevel: ListeningLevel?,
    onPlayAgain: () -> Unit,
    onNextLevel: (ListeningLevel) -> Unit,
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
            Text(text = if (isFinalLevel) "🏆" else "🎉", fontSize = 72.sp)
            Text(
                text = when {
                    isFinalLevel && english -> "You have completed all levels!"
                    isFinalLevel -> "Du hast alle Level geschafft!"
                    english -> "Great job! Try the next level!"
                    else -> "Toll gemacht! Versuch das nächste Level!"
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp),
            )
            ActionButton(
                text = if (english) "Play Again" else "Nochmal spielen",
                color = Color(0xFF1976D2),
                onClick = onPlayAgain,
            )
            if (nextLevel != null) {
                ActionButton(
                    text = if (english) "Next Level →" else "Nächstes Level →",
                    color = Color(0xFF00C48C),
                    onClick = { onNextLevel(nextLevel) },
                    topPadding = 12.dp,
                )
            }
            ActionButton(
                text = if (english) "Back to Levels" else "Zurück zu den Leveln",
                color = Color(0xFF546E7A),
                onClick = onBackToLevels,
                topPadding = 12.dp,
            )
        }
    }
}

@Composable
private fun ActionButton(text: String, color: Color, onClick: () -> Unit, topPadding: androidx.compose.ui.unit.Dp = 0.dp) {
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

@Composable
private fun NumberTile(number: Int, status: TileStatus, enabled: Boolean, onClick: () -> Unit) {
    val color = when (status) {
        TileStatus.NEUTRAL -> NEUTRAL_TILE_COLOR
        TileStatus.CORRECT -> CORRECT_TILE_COLOR
        TileStatus.WRONG -> WRONG_TILE_COLOR
    }
    val scale by animateFloatAsState(targetValue = if (status == TileStatus.NEUTRAL) 1f else 1.08f, label = "tileScale")

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(enabled = enabled) { onClick() }
            .background(color, RoundedCornerShape(20.dp))
            .scale(scale),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp,
        )
    }
}
