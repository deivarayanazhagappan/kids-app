package com.deivarayan.kids.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * Calls [onPause] when the app is backgrounded (e.g. minimised, screen
 * locked) and [onResume] when it returns to the foreground. This is a
 * single-activity app, so the screen's own lifecycle already reflects the
 * whole app's visibility.
 */
@Composable
fun PauseOnBackground(onPause: () -> Unit, onResume: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> onPause()
                Lifecycle.Event.ON_START -> onResume()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
