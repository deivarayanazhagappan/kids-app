package com.deivarayan.kids.ui.pattern

import android.app.Application
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deivarayan.kids.data.GameLanguage
import com.deivarayan.kids.data.PatternLevel
import com.deivarayan.kids.ui.common.ConfettiOverlay
import com.deivarayan.kids.ui.common.KeepScreenOn
import com.deivarayan.kids.ui.common.PauseOnBackground

private val NEUTRAL_COLOR = Color(0xFF546E7A)
private val CORRECT_COLOR = Color(0xFF00C48C)
private val WRONG_COLOR = Color(0xFFD32F2F)
private val ACTIVE_BLANK_COLOR = Color(0xFFFFC107)
private val PENDING_BLANK_COLOR = Color(0xFF546E7A)
private const val ROUND_LIMIT = 20

@Composable
fun PatternGameScreen(
    language: GameLanguage,
    level: PatternLevel,
    onNextLevel: (PatternLevel) -> Unit,
    onBackToLevels: () -> Unit,
) {
    KeepScreenOn()
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: PatternGameViewModel = viewModel(
        factory = PatternGameViewModelFactory(application, language, level),
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
                TopAppBar(title = { Text(if (english) "Pattern Game" else "Muster-Spiel") })
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
                    text = if (english) "Round ${state.roundNumber} / $ROUND_LIMIT" else "Runde ${state.roundNumber} / $ROUND_LIMIT",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 24.dp),
                )

                if (state.cells.isEmpty()) {
                    Text(text = "Loading…", fontSize = 20.sp)
                } else {
                    val cellFontSize = when {
                        state.cells.size <= 5 -> 36.sp
                        state.cells.size <= 7 -> 28.sp
                        else -> 22.sp
                    }
                    val cellSpacing = when {
                        state.cells.size <= 5 -> 8.dp
                        state.cells.size <= 7 -> 6.dp
                        else -> 4.dp
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(cellSpacing),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        state.cells.forEachIndexed { index, emoji ->
                            val isActive = index == state.activeCellIndex
                            val isPending = emoji == PATTERN_BLANK && !isActive
                            PatternCellView(emoji = emoji, isActive = isActive, isPending = isPending, fontSize = cellFontSize)
                        }
                    }

                    Text(
                        text = when {
                            level == PatternLevel.BEGINNER && english -> "What comes next?"
                            level == PatternLevel.BEGINNER -> "Was kommt als Nächstes?"
                            english -> "What's missing?"
                            else -> "Was fehlt?"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 32.dp, bottom = 16.dp),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        state.options.forEachIndexed { index, emoji ->
                            PatternOptionView(
                                emoji = emoji,
                                status = state.optionStatuses.getOrElse(index) { PatternOptionStatus.NEUTRAL },
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
            PatternCompleteOverlay(
                language = language,
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
private fun PatternCellView(emoji: String, isActive: Boolean, isPending: Boolean, fontSize: TextUnit) {
    val background = when {
        isActive -> ACTIVE_BLANK_COLOR.copy(alpha = 0.35f)
        isPending -> PENDING_BLANK_COLOR.copy(alpha = 0.15f)
        else -> Color.Transparent
    }
    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(10.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, fontSize = fontSize)
    }
}

@Composable
private fun PatternOptionView(
    emoji: String,
    status: PatternOptionStatus,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = when (status) {
        PatternOptionStatus.NEUTRAL -> NEUTRAL_COLOR
        PatternOptionStatus.CORRECT -> CORRECT_COLOR
        PatternOptionStatus.WRONG -> WRONG_COLOR
    }
    val scale by animateFloatAsState(targetValue = if (status == PatternOptionStatus.NEUTRAL) 1f else 1.08f, label = "patternOptionScale")

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(enabled = enabled, onClick = onClick)
            .background(color, RoundedCornerShape(16.dp))
            .scale(scale),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, fontSize = 40.sp)
    }
}

@Composable
private fun PatternCompleteOverlay(
    language: GameLanguage,
    nextLevel: PatternLevel?,
    onPlayAgain: () -> Unit,
    onNextLevel: (PatternLevel) -> Unit,
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
                text = if (english) "Great job! You solved them all!" else "Toll gemacht! Du hast alle gelöst!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp),
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
