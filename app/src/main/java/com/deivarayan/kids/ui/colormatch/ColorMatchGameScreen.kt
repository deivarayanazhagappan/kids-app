package com.deivarayan.kids.ui.colormatch

import android.app.Application
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.deivarayan.kids.data.NamedColor
import com.deivarayan.kids.ui.common.ConfettiOverlay
import com.deivarayan.kids.ui.common.KeepScreenOn
import com.deivarayan.kids.ui.common.PauseOnBackground

private val NEUTRAL_BORDER = Color(0xFF546E7A)
private val CORRECT_BORDER = Color(0xFF00C48C)
private val WRONG_BORDER = Color(0xFFD32F2F)

@Composable
fun ColorMatchGameScreen(language: GameLanguage, onBackToGames: () -> Unit) {
    KeepScreenOn()
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: ColorMatchGameViewModel = viewModel(
        factory = ColorMatchGameViewModelFactory(application, language),
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
                TopAppBar(title = { Text(if (english) "Color Match" else "Farben-Match") })
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
                    text = if (english) "Round ${state.roundNumber} / ${state.totalRounds}" else "Runde ${state.roundNumber} / ${state.totalRounds}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 20.dp),
                )

                val target = state.target
                if (state.options.isEmpty() || target == null) {
                    Text(text = "Loading…", fontSize = 20.sp)
                } else {
                    Text(
                        text = if (english) "Find this color!" else "Finde diese Farbe!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )

                    Box(
                        modifier = Modifier
                            .padding(bottom = 28.dp)
                            .size(90.dp)
                            .background(Color(target.colorHex), RoundedCornerShape(20.dp)),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        state.options.take(2).forEachIndexed { index, option ->
                            ColorOptionView(
                                color = option,
                                status = state.optionStatuses.getOrElse(index) { ColorOptionStatus.NEUTRAL },
                                enabled = state.inputEnabled,
                                onClick = { viewModel.onOptionClicked(index) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        state.options.drop(2).forEachIndexed { offsetIndex, option ->
                            val index = offsetIndex + 2
                            ColorOptionView(
                                color = option,
                                status = state.optionStatuses.getOrElse(index) { ColorOptionStatus.NEUTRAL },
                                enabled = state.inputEnabled,
                                onClick = { viewModel.onOptionClicked(index) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }

        if (state.isSessionComplete) {
            ColorMatchCompleteOverlay(
                language = language,
                correctCount = state.correctCount,
                totalRounds = state.totalRounds,
                onPlayAgain = viewModel::playAgain,
                onBackToGames = onBackToGames,
            )
        }

        ConfettiOverlay(trigger = confettiTrigger, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun ColorOptionView(
    color: NamedColor,
    status: ColorOptionStatus,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = when (status) {
        ColorOptionStatus.NEUTRAL -> NEUTRAL_BORDER
        ColorOptionStatus.CORRECT -> CORRECT_BORDER
        ColorOptionStatus.WRONG -> WRONG_BORDER
    }
    val scale by animateFloatAsState(targetValue = if (status == ColorOptionStatus.NEUTRAL) 1f else 1.06f, label = "colorOptionScale")

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(enabled = enabled, onClick = onClick)
            .background(borderColor, RoundedCornerShape(18.dp))
            .padding(6.dp)
            .background(Color(color.colorHex), RoundedCornerShape(14.dp))
            .scale(scale),
    )
}

@Composable
private fun ColorMatchCompleteOverlay(
    language: GameLanguage,
    correctCount: Int,
    totalRounds: Int,
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
            Text(text = "🎨", fontSize = 72.sp)
            Text(
                text = if (english) {
                    "You got $correctCount out of $totalRounds right!"
                } else {
                    "Du hast $correctCount von $totalRounds richtig!"
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp),
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
