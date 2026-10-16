package com.pointchange.sampleaudio.ui.component

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalFlexBoxApi
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointchange.sampleaudio.R
import com.pointchange.sampleaudio.data.Bands
import com.pointchange.sampleaudio.data.EqualizerState
import com.pointchange.sampleaudio.remember.rememberUserPreferences
import com.pointchange.sampleaudio.ui.page.PlayerSheetType
import com.pointchange.sampleaudio.ui.theme.ext
import com.pointchange.sampleaudio.viewmodel.PlayerStateViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFlexBoxApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(sheetState: SheetState) {
    val viewModel = viewModel<PlayerStateViewModel>()
    val equalizerState by viewModel.equalizerState.collectAsStateWithLifecycle()
    var checked by remember { mutableStateOf(equalizerState.enable) }

    val presetNames =
        listOf(stringResource(R.string.preset_null)) + viewModel.equalizerFn.getPresetNames()
    val gap = MaterialTheme.ext.spacing.xs
    var selected by remember { mutableIntStateOf(equalizerState.selected) }
    val bands = remember {
        mutableStateListOf<Bands>().apply {
            addAll(equalizerState.bands)
        }
//        listOf(
//            Triple("60", 1, 0f ),
//            Triple("250", 3, 0f ),
//            Triple("1k", 5, 0f ),
//            Triple("4k", 7, 0f ),
//            Triple("16k", 9, 0f ),
//        ).toMutableStateList()
    }
    var volume by remember { mutableIntStateOf(equalizerState.userVolume) }
    fun save() {
        viewModel.setEqualizerConfig(
            EqualizerState(
                enable = checked,
                selected = selected,
                presetName = if (selected == 0) null else presetNames[selected],
                bands = bands,
                userVolume = volume
            )
        )
    }
    LaunchedEffect(Unit) {
        snapshotFlow { sheetState.currentValue }.collect {
            if (it == SheetValue.Hidden) {
                save()
            }
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP, Lifecycle.Event.ON_PAUSE -> {
                    save()
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.ext.spacing.small),
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.ext.spacing.medium)
            .verticalScroll(state = rememberScrollState())
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            headlineContent = {
                Text(text = stringResource(R.string.equalize))
            },
            trailingContent = {
                Switch(
                    checked = checked,
                    onCheckedChange = {
                        if (!it) {
                            viewModel.equalizerFn.disable()
                            viewModel.equalizerFn.reset()
                        } else {

                        }
                        checked = it
                    }
                )
            }
        )
        AnimatedVisibility(visible = checked) {
            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.ext.spacing.small),

                ) {
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    overlineContent = {
                        Text(
                            text = stringResource(R.string.software_volume) + ": $volume",
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    headlineContent = {
                        Slider(
                            value = volume.toFloat(),
                            onValueChange = {
                                volume = it.roundToInt()
                                viewModel.setVolume(volume)
                            },
                            valueRange = 0f..200f
                        )
                    },
                    supportingContent = {
                        Text(
                            text = stringResource(R.string.software_volume_warn),
                            color = when (volume) {
                                in 150..175 -> Color(0xFFFF8000)
                                in 175..200 -> MaterialTheme.colorScheme.error
                                else -> Color.Transparent
                            }
                        )
                    },
                    trailingContent = {
                        IconButton(onClick = { volume = 100 }) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_replay_24),
                                contentDescription = stringResource(R.string.recover)
                            )
                        }
                    }
                )
                Text(text = stringResource(R.string.preset))
                if (presetNames.isNotEmpty()) {
                    FlexBox(
                        config = {
                            gap(gap)
                            wrap(FlexWrap.Wrap)
                        }
                    ) {
                        presetNames.forEachIndexed { index, name ->
                            FilterChip(
                                onClick = {
                                    if (index == 0) {
                                        viewModel.equalizerFn.disable()
                                    } else {
                                        viewModel.equalizerFn.setPreset(index - 1)
                                    }
                                    selected = index
                                },
                                label = {
                                    Text(name)
                                },
                                selected = selected == index,
                                leadingIcon = if (selected == index) {
                                    {
                                        Icon(
                                            painter = painterResource(R.drawable.baseline_check_24),
                                            contentDescription = null
                                        )
                                    }
                                } else {
                                    null
                                },
                            )
                        }
                    }

                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    bands.forEachIndexed { index, item ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${item.value.toInt()}", fontSize = 12.sp)
                            Box(
                                modifier = Modifier
                                    .height(190.dp)
                                    .width(45.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Slider(
                                    value = item.value,
                                    enabled = selected == 0,
                                    onValueChange = {
                                        val currentItem = bands[index].copy(value = it)
                                        bands[index] = currentItem
                                        viewModel.equalizerFn.setBand(item.id, it)

                                    },
                                    valueRange = -12f..12f,
                                    modifier = Modifier
                                        .requiredWidth(190.dp)
                                        .rotate(-90f)
                                )
                            }
                            Text(text = item.frequency, fontSize = 12.sp)
                            Text(text = item.effect, fontSize = 12.sp)
                        }
                    }
                }
            }

        }

    }
}

