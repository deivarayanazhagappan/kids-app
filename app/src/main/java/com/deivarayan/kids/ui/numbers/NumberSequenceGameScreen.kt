package com.deivarayan.kids.ui.numbers

import android.app.Application
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deivarayan.kids.data.GameLanguage
import com.deivarayan.kids.data.NumberSequenceLevel
import com.deivarayan.kids.ui.common.ConfettiOverlay
import com.deivarayan.kids.ui.common.KeepScreenOn
import com.deivarayan.kids.ui.common.PauseOnBackground
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val FILLED_COLOR = Color(0xFF43A047)
private val BLANK_COLOR = Color(0xFF546E7A)
private val TILE_COLOR = Color(0xFF1976D2)
private val WRONG_COLOR = Color(0xFFD32F2F)

@Composable
fun NumberSequenceGameScreen(
    language: GameLanguage,
    level: NumberSequenceLevel,
    onNextLevel: (NumberSequenceLevel) -> Unit,
    onBackToLevels: () -> Unit,
) {
    KeepScreenOn()
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: NumberSequenceGameViewModel = viewModel(
        factory = NumberSequenceGameViewModelFactory(application, language, level),
    )
    PauseOnBackground(onPause = viewModel::onPause, onResume = viewModel::onResume)
    val state by viewModel.uiState.collectAsState()
    val english = language == GameLanguage.ENGLISH
    val latestState = rememberUpdatedState(state)

    var confettiTrigger by remember { mutableIntStateOf(0) }
    LaunchedEffect(state.isSessionComplete) {
        if (state.isSessionComplete) confettiTrigger++
    }

    var showWrong by remember { mutableStateOf(false) }
    LaunchedEffect(state.wrongAttemptTick) {
        if (state.wrongAttemptTick > 0) {
            showWrong = true
            delay(900)
            showWrong = false
        }
    }

    val blankBounds = remember(state.roundNumber) { mutableStateMapOf<Int, Rect>() }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text(if (english) "Number Sequence" else "Zahlenreihe") })
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = if (english) "Round ${state.roundNumber} / $NUMBER_SEQUENCE_ROUND_LIMIT" else "Runde ${state.roundNumber} / $NUMBER_SEQUENCE_ROUND_LIMIT",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                if (state.cells.isEmpty()) {
                    Text(text = "Loading…", fontSize = 20.sp)
                } else {
                    val cellFontSize = if (state.cells.size <= 5) 30.sp else 22.sp
                    val cellSize = if (state.cells.size <= 5) 56.dp else 44.dp

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        state.cells.forEachIndexed { index, value ->
                            NumberCell(
                                value = value,
                                size = cellSize,
                                fontSize = cellFontSize,
                                modifier = Modifier.onGloballyPositioned { coords ->
                                    if (value == null) blankBounds[index] = coords.boundsInRoot()
                                },
                            )
                        }
                    }

                    Text(
                        text = if (english) "Drag the right number into the gap" else "Zieh die richtige Zahl in die Lücke",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 28.dp, bottom = 20.dp),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    ) {
                        state.options.filter { it.status == NumberTileStatus.AVAILABLE }.forEach { tile ->
                            DraggableNumberTile(
                                tile = tile,
                                enabled = state.inputEnabled,
                                blankBounds = blankBounds,
                                latestState = latestState,
                                onDropped = { blankIndex ->
                                    val correct = viewModel.checkAnswer(blankIndex, tile.id)
                                    if (correct) viewModel.placeTile(blankIndex, tile.id) else viewModel.onWrongAttempt()
                                    correct
                                },
                            )
                        }
                    }

                    if (showWrong) {
                        Text(
                            text = if (english) "Wrong! Try again." else "Falsch! Versuch's nochmal.",
                            color = WRONG_COLOR,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 16.dp),
                        )
                    }
                }
            }
        }

        if (state.isSessionComplete) {
            NumberSequenceCompleteOverlay(
                language = language,
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
private fun NumberCell(value: Int?, size: Dp, fontSize: TextUnit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .border(2.dp, if (value == null) BLANK_COLOR else Color.Transparent, RoundedCornerShape(12.dp))
            .background(
                if (value == null) BLANK_COLOR.copy(alpha = 0.15f) else FILLED_COLOR.copy(alpha = 0.15f),
                RoundedCornerShape(12.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (value != null) {
            Text(text = value.toString(), fontSize = fontSize, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DraggableNumberTile(
    tile: NumberTile,
    enabled: Boolean,
    blankBounds: Map<Int, Rect>,
    latestState: State<NumberSequenceUiState>,
    onDropped: (blankIndex: Int) -> Boolean,
) {
    val scope = rememberCoroutineScope()
    val offset = remember(tile.id) { Animatable(Offset.Zero, Offset.VectorConverter) }
    var restingBounds by remember(tile.id) { mutableStateOf<Rect?>(null) }

    Box(
        modifier = Modifier.onGloballyPositioned { coords -> restingBounds = coords.boundsInRoot() },
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .offset { IntOffset(offset.value.x.roundToInt(), offset.value.y.roundToInt()) }
                .background(TILE_COLOR, RoundedCornerShape(14.dp))
                .pointerInput(tile.id, enabled) {
                    if (!enabled) return@pointerInput
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch { offset.snapTo(offset.value + dragAmount) }
                        },
                        onDragEnd = {
                            val bounds = restingBounds ?: return@detectDragGestures
                            val center = Offset(
                                bounds.left + offset.value.x + bounds.width / 2f,
                                bounds.top + offset.value.y + bounds.height / 2f,
                            )
                            val currentCells = latestState.value.cells
                            val target = blankBounds.entries.firstOrNull { (index, rect) ->
                                currentCells.getOrNull(index) == null && rect.contains(center)
                            }
                            scope.launch {
                                if (target != null && onDropped(target.key)) {
                                    val dx = target.value.center.x - bounds.center.x
                                    val dy = target.value.center.y - bounds.center.y
                                    offset.animateTo(Offset(dx, dy), tween(150))
                                } else {
                                    offset.animateTo(Offset.Zero, tween(200))
                                }
                            }
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(text = tile.value.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        }
    }
}

@Composable
private fun NumberSequenceCompleteOverlay(
    language: GameLanguage,
    nextLevel: NumberSequenceLevel?,
    onPlayAgain: () -> Unit,
    onNextLevel: (NumberSequenceLevel) -> Unit,
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
            Text(text = "🎉", fontSize = 72.sp)
            Text(
                text = if (english) "Great job! You solved them all!" else "Toll gemacht! Du hast alle gelöst!",
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
