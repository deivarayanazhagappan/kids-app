package com.deivarayan.kids.ui.spelling

import android.app.Application
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deivarayan.kids.data.GameLanguage
import com.deivarayan.kids.data.GameSpeed
import com.deivarayan.kids.ui.common.ConfettiOverlay
import com.deivarayan.kids.ui.common.KeepScreenOn
import com.deivarayan.kids.ui.common.PauseOnBackground
import com.deivarayan.kids.ui.common.WordPicture
import com.deivarayan.kids.ui.common.onHorizontalSwipe

@Composable
fun SpellingGameScreen(language: GameLanguage, speed: GameSpeed, category: String?) {
    KeepScreenOn()
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: SpellingGameViewModel = viewModel(
        factory = SpellingGameViewModelFactory(application, language, speed, category),
    )
    PauseOnBackground(onPause = viewModel::onPause, onResume = viewModel::onResume)
    val state by viewModel.uiState.collectAsState()

    var confettiTrigger by remember { mutableIntStateOf(0) }
    LaunchedEffect(viewModel) {
        viewModel.categoryCompletedEvent.collect { confettiTrigger++ }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text(if (language == GameLanguage.ENGLISH) "Word Spelling" else "Wörter buchstabieren") })
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .onHorizontalSwipe(
                        onSwipeLeft = { viewModel.skipToNext() },
                        onSwipeRight = { viewModel.skipToPrevious() },
                    )
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                val entry = state.entry
                if (entry == null) {
                    Text(text = "Loading…", fontSize = 20.sp)
                    return@Column
                }

                WordPicture(emoji = entry.emoji, imageUrl = state.imageUrl, contentDescription = entry.text)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    entry.text.forEachIndexed { index, ch ->
                        val highlighted = index == state.highlightedLetterIndex || state.isSpeakingWholeWord
                        LetterTile(char = ch, highlighted = highlighted)
                    }
                }

                Text(
                    text = entry.category,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 24.dp),
                )
                Text(
                    text = "${state.wordNumber} / ${state.totalWords}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        ConfettiOverlay(trigger = confettiTrigger, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun LetterTile(char: Char, highlighted: Boolean) {
    if (char.isWhitespace()) {
        Spacer(modifier = Modifier.padding(horizontal = 6.dp))
        return
    }
    val scale by animateFloatAsState(targetValue = if (highlighted) 1.15f else 1f, label = "letterScale")
    val backgroundColor = if (highlighted) MaterialTheme.colorScheme.tertiary else Color.Transparent
    val textColor = if (highlighted) Color.White else MaterialTheme.colorScheme.onBackground

    Text(
        text = char.toString(),
        fontSize = 40.sp,
        fontWeight = FontWeight.Bold,
        color = textColor,
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .scale(scale),
    )
}
