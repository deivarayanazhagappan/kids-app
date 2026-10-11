package com.deivarayan.kids.ui.rhyme

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.deivarayan.kids.data.RhymeLevel
import com.deivarayan.kids.ui.common.ConfettiOverlay
import com.deivarayan.kids.ui.common.KeepScreenOn
import com.deivarayan.kids.ui.common.PauseOnBackground

@Composable
fun RhymeGameScreen(
    language: GameLanguage,
    level: RhymeLevel,
    onNextLevel: (RhymeLevel) -> Unit,
    onBackToLevels: () -> Unit,
) {
    KeepScreenOn()
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: RhymeGameViewModel = viewModel(
        factory = RhymeGameViewModelFactory(application, language, level),
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
                TopAppBar(title = { Text(if (english) "Rhyme Time" else "Reim-Zeit") })
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
                    text = if (english) "Round ${state.roundNumber} / $RHYME_ROUND_LIMIT" else "Runde ${state.roundNumber} / $RHYME_ROUND_LIMIT",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 24.dp),
                )

                Text(
                    text = if (english) "These words rhyme!" else "Diese Wörter reimen sich!",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 24.dp),
                )

                Text(
                    text = state.words.joinToString("  •  "),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    RhymeActionButton(
                        text = if (english) "🔊 Listen Again" else "🔊 Nochmal hören",
                        color = Color(0xFF546E7A),
                        enabled = state.inputEnabled,
                        onClick = viewModel::onReplayClicked,
                        modifier = Modifier.weight(1f),
                    )
                    RhymeActionButton(
                        text = if (english) "Next ➡" else "Weiter ➡",
                        color = Color(0xFF00C48C),
                        enabled = state.inputEnabled,
                        onClick = viewModel::onNextClicked,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        if (state.isSessionComplete) {
            RhymeCompleteOverlay(
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
private fun RhymeActionButton(
    text: String,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clickable(enabled = enabled, onClick = onClick)
            .background(if (enabled) color else color.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun RhymeCompleteOverlay(
    language: GameLanguage,
    nextLevel: RhymeLevel?,
    onPlayAgain: () -> Unit,
    onNextLevel: (RhymeLevel) -> Unit,
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
                text = if (english) "Great job! You're a rhyming star!" else "Toll gemacht! Du bist ein Reim-Profi!",
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
