package com.deivarayan.kids.ui.spelling

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deivarayan.kids.data.GameLanguage

@Composable
fun LanguageSelectScreen(onLanguageSelected: (GameLanguage) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Word Spelling", fontWeight = FontWeight.Bold) })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Choose a language",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 24.dp),
            )
            LanguageButton(
                emoji = "🇺🇸",
                label = "English",
                color = Color(0xFF1976D2),
                onClick = { onLanguageSelected(GameLanguage.ENGLISH) },
            )
            LanguageButton(
                emoji = "🇩🇪",
                label = "Deutsch",
                color = Color(0xFFD32F2F),
                onClick = { onLanguageSelected(GameLanguage.GERMAN) },
                modifierTopPadding = 16.dp,
            )
        }
    }
}

@Composable
private fun LanguageButton(
    emoji: String,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifierTopPadding: androidx.compose.ui.unit.Dp = 0.dp,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = modifierTopPadding)
            .height(88.dp)
            .clickable(onClick = onClick)
            .background(color, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$emoji  $label",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
        )
    }
}
