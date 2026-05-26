package com.pointchange.audio.view.page

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointchange.audio.R
import com.pointchange.audio.model.PlayerViewModel
import com.pointchange.audio.model.SettingViewModel
import com.pointchange.audio.model.TimerState
import com.pointchange.audio.service.AudioService
import com.pointchange.audio.service.VlcManager
import com.pointchange.audio.util.formatTime
import com.pointchange.audio.view.LocalNav
import kotlin.math.absoluteValue


data class TimerSetList(
    val time: Int = 0,
    val text: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerPage(
    settingViewModel: SettingViewModel = viewModel(),
    playerViewModel: PlayerViewModel = viewModel()
) {
    val navActions = LocalNav.current
    var h by remember { mutableIntStateOf(0) }
    var m by remember { mutableIntStateOf(0) }
    var s by remember { mutableIntStateOf(0) }

    val timerConfig by settingViewModel.timerConfig.collectAsStateWithLifecycle()
    var remainTime by remember { mutableLongStateOf(0L) }

    fun clear() {
        settingViewModel.clearTimer()
        remainTime = 0
    }

    LaunchedEffect(timerConfig.isRunning, timerConfig.targetTime) {
        if (timerConfig.isRunning) {
            while (timerConfig.isRunning) {
                var remaining = 0L
                withFrameNanos {
                    remaining = timerConfig.targetTime - System.currentTimeMillis()
                }

                if (remaining <= 0) {
                    clear()
                    playerViewModel.pause()
                } else {
                    remainTime = remaining
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navActions?.removeLast() }) {
                        Icon(
                            painter = painterResource(R.drawable.round_navigate_before_24),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                title = {
                    Text(text = stringResource(R.string.timer))
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .wrapContentHeight()
                    .windowInsetsPadding(
                        BottomAppBarDefaults.windowInsets
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                ) {
                    when (timerConfig.timerState) {
                        TimerState.PENDING -> {
                            LargeFloatingActionButton(
                                onClick = {
                                    val time = (h * 60 * 60 + m * 60 + s) * 1000L
                                    if (time == 0L) return@LargeFloatingActionButton
                                    settingViewModel.setTimerConfigTargetTime(time + System.currentTimeMillis())
                                    settingViewModel.setTimerConfigRunning(true)
                                    settingViewModel.setTimerConfigTimerState(TimerState.RUNNING)
                                    settingViewModel.setTimerConfigRecordFirstTargetTime(time)
                                }) {
                                Icon(
                                    painter = painterResource(R.drawable.round_play_arrow_24),
                                    contentDescription = stringResource(R.string.timer_start),
                                    modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize * 2)
                                )
                            }
                        }

                        TimerState.RUNNING -> {
                            LargeFloatingActionButton(onClick = {
                                clear()
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.round_close_24),
                                    contentDescription = stringResource(R.string.timer_close),
                                    modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize * 2)
                                )
                            }
                            LargeFloatingActionButton(onClick = {
                                settingViewModel.setTimerConfigRunning(false)
                                settingViewModel.setTimerConfigTimerState(TimerState.PAUSING)
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.round_pause_24),
                                    contentDescription = stringResource(R.string.timer_pause),
                                    modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize * 2)
                                )
                            }
                        }

                        TimerState.PAUSING -> {
                            LargeFloatingActionButton(onClick = {
                                clear()
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.round_close_24),
                                    contentDescription = stringResource(R.string.timer_close),
                                    modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize * 2)
                                )
                            }
                            LargeFloatingActionButton(onClick = {
                                settingViewModel.setTimerConfigTargetTime(remainTime + System.currentTimeMillis())
                                settingViewModel.setTimerConfigRunning(true)
                                settingViewModel.setTimerConfigTimerState(TimerState.RUNNING)
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.round_play_arrow_24),
                                    contentDescription = stringResource(R.string.timer_start),
                                    modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize * 2)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (timerConfig.timerState == TimerState.PENDING) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TimeWheelPicker(24) {
                            h = it
                        }
                        Text(text = ":", style = MaterialTheme.typography.displayLarge)
                        TimeWheelPicker(60) {
                            m = it

                        }
                        Text(text = ":", style = MaterialTheme.typography.displayLarge)
                        TimeWheelPicker(60) {
                            s = it
                        }
                    }
                }
            } else {
                val arcColor = MaterialTheme.colorScheme.primary
                val circleColor = arcColor.copy(alpha = 0.12F)
                val progress =
                    (remainTime / timerConfig.recordFirstTargetTime.toFloat()).coerceIn(
                        0F,
                        1F
                    )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(300.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 8.dp.toPx()
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.minDimension / 2
                        drawCircle(
                            color = circleColor,
                            radius = radius,
                            center = center,
                            style = Stroke(width = strokeWidth)
                        )
                        drawArc(
                            color = arcColor,
                            startAngle = -90F,
                            sweepAngle = 360F * progress,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = formatTime(remainTime),
                        style = MaterialTheme.typography.displayLarge
                    )
                }
            }
        }
    }
}

@Composable
fun TimeWheelPicker(count: Int, onValueChange: (Int) -> Unit) {

    val initialPage = Int.MAX_VALUE / 2
    val pagerState = rememberPagerState(
        initialPage = initialPage - (initialPage % count),
        pageCount = { Int.MAX_VALUE }
    )

    LaunchedEffect(pagerState.settledPage) {
        onValueChange(pagerState.settledPage % count)
    }

    Box(
        modifier = Modifier
            .width(100.dp)
            .height(180.dp), contentAlignment = Alignment.Center
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

@Preview(showBackground = true)
@Composable
fun TimerPagePreview() {
    TimerPage()
}