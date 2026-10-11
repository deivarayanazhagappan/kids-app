package com.deivarayan.kids.ui.wherewasit

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
import com.deivarayan.kids.data.WhereWasItLevel

@Composable
fun WhereWasItLevelSelectScreen(onLevelSelected: (WhereWasItLevel) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Where Was It?", fontWeight = FontWeight.Bold) })
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
                level = WhereWasItLevel.BEGINNER,
                subtitle = "3x3 grid, longer look",
                color = Color(0xFF00C48C),
                onClick = onLevelSelected,
            )
            LevelButton(
                emoji = "🚶",
                level = WhereWasItLevel.INTERMEDIATE,
                subtitle = "4x4 grid, shorter look",
                color = Color(0xFFFFC107),
                onClick = onLevelSelected,
                topPadding = 16.dp,
            )
            LevelButton(
                emoji = "🐆",
                level = WhereWasItLevel.EXPERT,
                subtitle = "5x5 grid, quick flash",
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
    level: WhereWasItLevel,
    subtitle: String,
    color: Color,
    onClick: (WhereWasItLevel) -> Unit,
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
