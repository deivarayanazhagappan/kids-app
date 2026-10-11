package com.deivarayan.kids.ui.common

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

private const val SWIPE_THRESHOLD_PX = 120f

/** Invokes [onSwipeLeft] or [onSwipeRight] once a horizontal drag gesture ends past the threshold. */
fun Modifier.onHorizontalSwipe(onSwipeLeft: () -> Unit, onSwipeRight: () -> Unit): Modifier =
    this.pointerInput(Unit) {
        var totalDrag = 0f
        detectHorizontalDragGestures(
            onDragStart = { totalDrag = 0f },
            onHorizontalDrag = { change, dragAmount ->
                totalDrag += dragAmount
                change.consume()
            },
            onDragEnd = {
                when {
                    totalDrag < -SWIPE_THRESHOLD_PX -> onSwipeLeft()
                    totalDrag > SWIPE_THRESHOLD_PX -> onSwipeRight()
                }
            },
        )
    }
