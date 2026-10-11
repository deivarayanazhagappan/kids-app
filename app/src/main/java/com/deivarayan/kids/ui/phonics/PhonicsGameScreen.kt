package com.deivarayan.kids.ui.phonics

import android.app.Application
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deivarayan.kids.data.GameLanguage
import com.deivarayan.kids.data.GameSpeed
import com.deivarayan.kids.ui.common.KeepScreenOn
import com.deivarayan.kids.ui.common.PauseOnBackground
import com.deivarayan.kids.ui.common.WordPicture
import com.deivarayan.kids.ui.common.onHorizontalSwipe

@Composable
fun PhonicsGameScreen(language: GameLanguage, speed: GameSpeed) {
    KeepScreenOn()
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: PhonicsGameViewModel = viewModel(
        factory = PhonicsGameViewModelFactory(application, language, speed),
    )
    PauseOnBackground(onPause = viewModel::onPause, onResume = viewModel::onResume)
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (language == GameLanguage.ENGLISH) "Phonics" else "Phonetik") })
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

            WordPicture(emoji = entry.exampleEmoji, imageUrl = state.imageUrl, contentDescription = entry.exampleWord)

            val graphemeScale by animateFloatAsState(
                targetValue = if (state.isSoundPlaying) 1.2f else 1f,
                label = "graphemeScale",
            )
            Text(
                text = entry.grapheme,
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = if (state.isSoundPlaying) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(top = 24.dp)
                    .background(
                        if (state.isSoundPlaying) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f) else Color.Transparent,
                        RoundedCornerShape(16.dp),
                    )
                    .padding(horizontal = 24.dp, vertical = 4.dp)
                    .scale(graphemeScale),
            )

            Text(
                text = entry.exampleWord,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (state.isSpeakingWord) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 16.dp),
            )

            Text(
                text = "${state.unitNumber} / ${state.totalUnits}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 24.dp),
            )
        }
    }
}
