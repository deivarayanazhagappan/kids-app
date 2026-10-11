package com.deivarayan.kids.ui.wherewasit

import android.app.Application
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
import androidx.compose.foundation.lazy.grid.items
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
import com.deivarayan.kids.data.WhereWasItLevel
import com.deivarayan.kids.ui.common.ConfettiOverlay
import com.deivarayan.kids.ui.common.KeepScreenOn
import com.deivarayan.kids.ui.common.PauseOnBackground

private val CELL_NEUTRAL = Color(0xFFECEFF1)
private val CELL_CORRECT = Color(0xFF00C48C)
private val CELL_WRONG = Color(0xFFD32F2F)

@Composable
fun WhereWasItGameScreen(
    language: GameLanguage,
    level: WhereWasItLevel,
    onNextLevel: (WhereWasItLevel) -> Unit,
    onBackToLevels: () -> Unit,
) {
    KeepScreenOn()
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: WhereWasItGameViewModel = viewModel(
        factory = WhereWasItGameViewModelFactory(application, language, level),
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
                TopAppBar(title = { Text(if (english) "Where Was It?" else "Wo war es?") })
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
                    modifier = Modifier.padding(bottom = 12.dp),
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
                } else {
                    Text(
                        text = when (state.phase) {
                            WhereWasItPhase.WAITING -> if (english) "Watch closely…" else "Schau genau hin…"
                            WhereWasItPhase.FLASHING -> if (english) "Remember this spot!" else "Merke dir die Stelle!"
                            WhereWasItPhase.GUESSING -> if (english) "Where was it?" else "Wo war es?"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )

                    WhereWasItGrid(
                        level = level,
                        state = state,
                        onCellTap = viewModel::onCellTapped,
                    )
                }
            }
        }

        if (state.isSessionComplete) {
            WhereWasItCompleteOverlay(
                language = language,
                correctCount = state.correctCount,
                totalRounds = state.totalRounds,
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
private fun WhereWasItGrid(
    level: WhereWasItLevel,
    state: WhereWasItUiState,
    onCellTap: (Int) -> Unit,
) {
    val cellCount = level.gridColumns * level.gridColumns
    LazyVerticalGrid(
        columns = GridCells.Fixed(level.gridColumns),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(cellCount) { index ->
            val isFlashingHere = state.phase == WhereWasItPhase.FLASHING && index == state.flashCellIndex
            val isGuessedHere = state.guessedIndex == index
            val isRevealCorrect = state.guessWasCorrect == false && index == state.flashCellIndex
            val color = when {
                isGuessedHere && state.guessWasCorrect == true -> CELL_CORRECT
                isGuessedHere && state.guessWasCorrect == false -> CELL_WRONG
                isRevealCorrect -> CELL_CORRECT
                else -> CELL_NEUTRAL
            }
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clickable(enabled = state.inputEnabled, onClick = { onCellTap(index) })
                    .background(color, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (isFlashingHere) {
                    Text(text = state.flashEmoji, fontSize = 28.sp)
                }
            }
        }
    }
}

@Composable
private fun WhereWasItCompleteOverlay(
    language: GameLanguage,
    correctCount: Int,
    totalRounds: Int,
    nextLevel: WhereWasItLevel?,
    onPlayAgain: () -> Unit,
    onNextLevel: (WhereWasItLevel) -> Unit,
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
            Text(text = "🔎", fontSize = 72.sp)
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
