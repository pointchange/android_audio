package com.pointchange.sampleaudio.ui.page

import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointchange.sampleaudio.R
import com.pointchange.sampleaudio.ui.component.BackButton
import com.pointchange.sampleaudio.ui.theme.ext
import com.pointchange.sampleaudio.viewmodel.Nav3ViewModel
import com.pointchange.sampleaudio.viewmodel.PlayerStateViewModel
import kotlin.math.absoluteValue

//data class TimerValue(
//    val hour: Int = 0,
//    val minute: Int = 0,
//    val second: Int = 0
//) {
//    fun toSeconds(): Int = hour * 3600 + minute * 60 + second
//
//    companion object {
//        fun fromSecond(value: Int): TimerValue = TimerValue(
//            hour = value / 3600,
//            minute = (value % 3600) / 60,
//            second = value % 60,
//        )
//    }
//}

@Composable
fun TimerPage() {

    val viewModel = viewModel<PlayerStateViewModel>()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    var hour by remember { mutableIntStateOf(0) }
    var minute by remember { mutableIntStateOf(0) }
    var second by remember { mutableIntStateOf(0) }

    val buttons = listOf(
        Triple(
            R.string.play,
            R.drawable.baseline_play_arrow_24
        ) { viewModel.timerStart(hour * 3600 + minute * 60 + second) },
        Triple(R.string.pause, R.drawable.baseline_pause_24) { viewModel.timerPause() },
        Triple(R.string.recover, R.drawable.baseline_restore_24) { viewModel.timerEnd() },
    )
    val nav3ViewModel = viewModel<Nav3ViewModel>()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = WindowInsets().asPaddingValues()
                    .calculateTopPadding() + MaterialTheme.ext.spacing.large,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            )
    ) {

        BackButton { nav3ViewModel.pop() }

        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            RowContainer {
                Text(
                    text = "%02d".format(timerState.second / 3600),
                    style = MaterialTheme.typography.displayMedium
                )
                Text(text = ":", style = MaterialTheme.typography.displayMedium)

                Text(
                    text = "%02d".format((timerState.second % 3600) / 60),
                    style = MaterialTheme.typography.displayMedium
                )
                Text(text = ":", style = MaterialTheme.typography.displayMedium)

                Text(
                    text = "%02d".format(timerState.second % 60),
                    style = MaterialTheme.typography.displayMedium
                )
            }

            RowContainer {
                WheelColumn(
                    count = 24,
                    onValueChange = { hour = it }
                )
                Text(text = ":", style = MaterialTheme.typography.displayLarge)
                WheelColumn(
                    count = 60,
                    onValueChange = { minute = it }
                )
                Text(text = ":", style = MaterialTheme.typography.displayLarge)
                WheelColumn(
                    count = 60,
                    onValueChange = { second = it }
                )
            }
            RowContainer {
                buttons.forEach { (text, icon, onClick) ->
                    FloatingActionButton(onClick = onClick) {
                        Icon(
                            painter = painterResource(icon),
                            contentDescription = stringResource(text)
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun RowContainer(content: @Composable () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        content()
    }
}

@Composable
fun WheelColumn(count: Int, onValueChange: (value: Int) -> Unit) {

    val initialPage = Int.MAX_VALUE / 2
    val pagerState = rememberPagerState(
        initialPage = initialPage - (initialPage % count),
        pageCount = { Int.MAX_VALUE }
    )
    val hapticFeedback = LocalHapticFeedback.current
    LaunchedEffect(pagerState.settledPage) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onValueChange(pagerState.settledPage % count)
    }

    Box(
        modifier = Modifier
            .size(100.dp, 180.dp),
        contentAlignment = Alignment.Center
    ) {
        VerticalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(60.dp),
            snapPosition = SnapPosition.Center,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            val value = page % count

            Text(
                text = value.toString().padStart(2, '0'),
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.graphicsLayer {
                    val pageOffset =
                        ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                    alpha = lerp(1f, 0.3f, pageOffset.coerceIn(0f, 1f))
                    scaleX = lerp(1f, 0.8f, pageOffset.coerceIn(0f, 1f))
                    scaleY = lerp(1f, 0.8f, pageOffset.coerceIn(0f, 1f))
                }
            )
        }
    }
}