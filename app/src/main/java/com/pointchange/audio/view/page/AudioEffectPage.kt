package com.pointchange.audio.view.page

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointchange.audio.R
import com.pointchange.audio.model.AudioListViewModel
import com.pointchange.audio.view.LocalNav

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioEffectPage(
    audioListViewModel: AudioListViewModel = viewModel()
) {
    val navActions = LocalNav.current
    val selectedIndex by audioListViewModel.currentPresetIndex.collectAsStateWithLifecycle()
    val presets = audioListViewModel.presetList
    var isOpenAudioEffect by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { navActions?.removeLast() }) {
                        Icon(
                            painter = painterResource(R.drawable.round_navigate_before_24),
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                title = {
                    Text(text = stringResource(R.string.audio_effect_title))
                },
                actions = {
                    Switch(
                        checked = isOpenAudioEffect,
                        onCheckedChange = { isOpenAudioEffect = it }
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(presets) { index, presetName ->
                    val isSelected = index == selectedIndex

                    PresetItem(
                        name = presetName,
                        isSelected = isSelected,
                        onClick = {
                            if (!isOpenAudioEffect) return@PresetItem
                            audioListViewModel.selectPreset(index)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PresetItem(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val cardColors = if (isSelected) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Card(
        colors = cardColors,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge
            )

            if (isSelected) {
                Icon(
                    painter = painterResource(R.drawable.round_check_24),
                    contentDescription = stringResource(R.string.audio_effect_selected),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
