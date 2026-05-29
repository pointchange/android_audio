package com.pointchange.audio.view.page

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointchange.audio.R
import com.pointchange.audio.model.PlayerViewModel
import com.pointchange.audio.model.SettingViewModel
import com.pointchange.audio.model_data.DataStoreCacheManager
import com.pointchange.audio.model_data.dataStore
import com.pointchange.audio.service.VlcManager
import com.pointchange.audio.view.LocalNav
import com.pointchange.audio.view.NavID
import com.pointchange.audio.view.page.SettingList.*
import kotlinx.coroutines.flow.first
import kotlin.text.toInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingPage(
    settingViewModel: SettingViewModel = viewModel(),
    playerViewModel: PlayerViewModel = viewModel()
) {
    val navActions = LocalNav.current
    val loadingState by settingViewModel.loadingState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(loadingState.parseLoading) {
        if (loadingState.parseLoading) {
            Toast.makeText(context, "解析成功", Toast.LENGTH_SHORT).show()
        }
    }
    var volume by remember { mutableIntStateOf(100) }
    var isHeighten by remember { mutableStateOf(false) }
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
                    Text(text = stringResource(R.string.setting))
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            SettingList.entries.forEach {
                ListItem(
                    leadingContent = {
                        Icon(painter = painterResource(it.icon), contentDescription = null)
                    },
                    headlineContent = {
                        Text(text = stringResource(it.text))
                    },
                    modifier = Modifier.clickable(onClick = { navActions?.add(it.nav) })
                )
            }
            SettingListNotNav.entries.forEach {
                ListItem(
                    leadingContent = {
                        Icon(painter = painterResource(it.icon), contentDescription = null)
                    },
                    headlineContent = {
                        Text(text = stringResource(it.text))
                    },
                    modifier = Modifier.clickable(
                        enabled = when (it) {
                            SettingListNotNav.PARSE -> !loadingState.parseLoading
                        },
                        onClick = {
                            when (it) {
                                SettingListNotNav.PARSE -> {
                                    settingViewModel.parseUir()
                                }
                            }
                        }
                    )
                )
            }
            ListItem(
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.round_volume_up_24),
                        contentDescription = null
                    )
                },
                headlineContent = {
                    Text(text = stringResource(R.string.set_volume))
                },
                trailingContent = {
                    Switch(
                        checked = isHeighten,
                        onCheckedChange = {
                            isHeighten = it
                        }
                    )
                }
            )
            if (isHeighten) {
                ListItem(
                    leadingContent = {
                        Button(onClick = {
                            val reset = 100
                            volume = reset
                            playerViewModel.setVolume(reset)

                        }) {
                            Text(text = stringResource(R.string.reset))
                        }
                    },
                    headlineContent = {
                        Slider(
                            value = volume.toFloat(),
                            onValueChange = {
                                playerViewModel.setVolume(it.toInt())
                                volume = it.toInt()
                            },
                            valueRange = 0F..300F,
                        )
                    },
                    trailingContent = { Text(text = "$volume %") }
                )
            }
        }
    }
}