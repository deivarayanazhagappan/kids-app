package com.deivarayan.kids.ui.quicktap

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deivarayan.kids.data.GameLanguage
import com.deivarayan.kids.data.QuickTapLevel
import com.deivarayan.kids.ui.common.ConfettiOverlay
import com.deivarayan.kids.ui.common.KeepScreenOn
import com.deivarayan.kids.ui.common.PauseOnBackground

private val PLAY_AREA_BG = Color(0xFFECEFF1)
private val GO_COLOR = Color(0xFF00C48C)
private val GO_TAPPED_COLOR = Color(0xFFB0BEC5)

@Composable
fun QuickTapGameScreen(
    language: GameLanguage,
    level: QuickTapLevel,
    onNextLevel: (QuickTapLevel) -> Unit,
    onBackToLevels: () -> Unit,
) {
    KeepScreenOn()
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: QuickTapGameViewModel = viewModel(
        factory = QuickTapGameViewModelFactory(application, language, level),
    )
    PauseOnBackground(onPause = viewModel::onPause, onResume = viewModel::onResume)
    val state by viewModel.uiState.collectAsState()
    val english = language == GameLanguage.ENGLISH

    var confettiTrigger by remember { mutableIntStateOf(0) }
    LaunchedEffect(state.isSessionComplete, state.resultTier) {
        if (state.isSessionComplete && state.resultTier == QuickTapResultTier.EXCELLENT) confettiTrigger++
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text(if (english) "Quick Tap" else "Schnell Tippen") })
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = if (english) "Round ${state.roundNumber} / ${state.totalRounds}" else "Runde ${state.roundNumber} / ${state.totalRounds}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                if (state.isShowingIntro) {
                    Box(modifier = Modifier.fillMaxWidth().height(380.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (english) "Get ready…" else "Mach dich bereit…",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else if (state.motivationText != null) {
                    Box(modifier = Modifier.fillMaxWidth().height(380.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "💪 ${state.motivationText}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    QuickTapPlayArea(
                        level = level,
                        state = state,
                        english = english,
                        onTargetTap = viewModel::onTargetTapped,
                    )
                }
            }
        }

        if (state.isSessionComplete) {
            QuickTapCompleteOverlay(
                language = language,
                resultTier = state.resultTier,
                resultPercent = state.resultPercent,
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
private fun QuickTapPlayArea(
    level: QuickTapLevel,
    state: QuickTapUiState,
    english: Boolean,
    onTargetTap: (Int) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
            .background(PLAY_AREA_BG, RoundedCornerShape(20.dp)),
    ) {
        if (state.phase == RoundPhase.SHOWING) {
            val sizeDp = level.targetSizeDp.dp
            state.targets.forEach { target ->
                val xDp = (maxWidth - sizeDp) * target.xFraction
                val yDp = (maxHeight - sizeDp) * target.yFraction
                Box(
                    modifier = Modifier
                        .offset(x = xDp, y = yDp)
                        .size(sizeDp)
                        .background(if (target.tapped) GO_TAPPED_COLOR else GO_COLOR, CircleShape)
                        .clickable(enabled = !target.tapped, onClick = { onTargetTap(target.id) }),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!target.tapped) {
                        Text(text = "👆", fontSize = (level.targetSizeDp / 2).sp)
                    }
                }
            }
            Text(
                text = if (english) {
                    "Tapped ${state.tappedThisRound} / ${state.targets.size}"
                } else {
                    "Getippt ${state.tappedThisRound} / ${state.targets.size}"
                },
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                text = if (english) "Wait for it…" else "Warte ab…",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun QuickTapCompleteOverlay(
    language: GameLanguage,
    resultTier: QuickTapResultTier?,
    resultPercent: Int,
    nextLevel: QuickTapLevel?,
    onPlayAgain: () -> Unit,
    onNextLevel: (QuickTapLevel) -> Unit,
    onBackToLevels: () -> Unit,
) {
    val english = language == GameLanguage.ENGLISH
    val (emoji, message) = when (resultTier) {
        QuickTapResultTier.EXCELLENT -> "🎉" to (if (english) "Excellent!" else "Ausgezeichnet!")
        QuickTapResultTier.GOOD -> "👍" to (if (english) "Good job!" else "Gut gemacht!")
        QuickTapResultTier.TRY_AGAIN, null -> "💪" to (
            if (english) "Try again to increase your speed!" else "Versuch es nochmal, um schneller zu werden!"
            )
    }
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
            Text(text = emoji, fontSize = 72.sp)
            Text(
                text = message,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            )
            Text(
                text = if (english) "You tapped $resultPercent% of the targets" else "Du hast $resultPercent% der Ziele getroffen",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 24.dp),
            )
            ActionButton(text = if (english) "Play Again" else "Nochmal spielen", color = Color(0xFF1976D2), onClick = onPlayAgain)
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
