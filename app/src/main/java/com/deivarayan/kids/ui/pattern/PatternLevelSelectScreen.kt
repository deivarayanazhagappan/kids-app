package com.deivarayan.kids.ui.pattern

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deivarayan.kids.data.PatternLevel

@Composable
fun PatternLevelSelectScreen(onLevelSelected: (PatternLevel) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Pattern Game", fontWeight = FontWeight.Bold) })
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
                text = "Choose a level",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 24.dp),
            )
            LevelButton(
                emoji = "🐢",
                level = PatternLevel.BEGINNER,
                subtitle = "2 colors • find the last one",
                color = Color(0xFF00C48C),
                onClick = onLevelSelected,
            )
            LevelButton(
                emoji = "🚶",
                level = PatternLevel.INTERMEDIATE,
                subtitle = "3 colors • find one in the middle",
                color = Color(0xFFFFC107),
                onClick = onLevelSelected,
                topPadding = 16.dp,
            )
            LevelButton(
                emoji = "🐆",
                level = PatternLevel.EXPERT,
                subtitle = "4 colors • find two anywhere",
                color = Color(0xFFFF7A18),
                onClick = onLevelSelected,
                topPadding = 16.dp,
            )
        }
    }
}

@Composable
private fun LevelButton(
    emoji: String,
    level: PatternLevel,
    subtitle: String,
    color: Color,
    onClick: (PatternLevel) -> Unit,
    topPadding: Dp = 0.dp,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding)
            .height(88.dp)
            .clickable { onClick(level) }
            .background(color, RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Column {
            Text(
                text = "$emoji  ${level.displayName}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
            )
        }
    }
}
