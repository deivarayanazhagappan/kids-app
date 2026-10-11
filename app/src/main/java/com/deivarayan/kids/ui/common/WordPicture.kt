package com.deivarayan.kids.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent

private val CARD_SHAPE = RoundedCornerShape(28.dp)

/**
 * Shows a fetched photo once loaded, framed as a card; falls back to the
 * emoji in a soft gradient badge while loading, on failure, or when no
 * [imageUrl] is available (e.g. offline).
 */
@Composable
fun WordPicture(emoji: String, imageUrl: String?, contentDescription: String, size: Dp = 220.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(elevation = 14.dp, shape = CARD_SHAPE, clip = false)
            .clip(CARD_SHAPE)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), CARD_SHAPE),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUrl != null) {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            ) {
                when (painter.state) {
                    is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                    else -> EmojiBadge(emoji, size)
                }
            }
        } else {
            EmojiBadge(emoji, size)
        }
    }
}

@Composable
private fun EmojiBadge(emoji: String, containerSize: Dp) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, fontSize = (containerSize.value * 0.5f).sp)
    }
}
