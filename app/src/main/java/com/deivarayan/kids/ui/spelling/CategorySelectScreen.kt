package com.deivarayan.kids.ui.spelling

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import com.deivarayan.kids.data.WordData

private val CATEGORY_EMOJI = mapOf(
    "Animals" to "🐶",
    "Food" to "🍎",
    "Colors" to "🌈",
    "Numbers" to "🔢",
    "Family" to "👪",
    "Body" to "✋",
    "Clothing" to "👕",
    "Nature" to "🌳",
    "School" to "📖",
    "Vehicles" to "🚗",
    "House" to "🏠",
    "Toys" to "🧸",
    "Actions" to "🏃",
    "Feelings" to "😄",
    "Shapes" to "⭕",
    "Sports" to "⚽",
)

private val CATEGORY_COLORS = listOf(
    0xFFFF7A18, 0xFF00C48C, 0xFF7C4DFF, 0xFFFFC107,
    0xFF1976D2, 0xFFD32F2F, 0xFF00838F, 0xFF8E24AA,
)

/** null category means "all words". */
@Composable
fun CategorySelectScreen(onCategorySelected: (String?) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Word Spelling", fontWeight = FontWeight.Bold) })
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                CategoryTile(
                    title = "All Words",
                    emoji = "🔤",
                    color = 0xFF37474F,
                    onClick = { onCategorySelected(null) },
                )
            }
            itemsIndexed(WordData.categories, key = { _, category -> category }) { index, category ->
                CategoryTile(
                    title = category,
                    emoji = CATEGORY_EMOJI[category] ?: "⭐",
                    color = CATEGORY_COLORS[index % CATEGORY_COLORS.size],
                    onClick = { onCategorySelected(category) },
                )
            }
        }
    }
}

@Composable
private fun CategoryTile(title: String, emoji: String, color: Long, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick)
            .background(Color(color), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = emoji, fontSize = 48.sp)
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
