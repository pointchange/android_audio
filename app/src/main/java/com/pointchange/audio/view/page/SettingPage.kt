package com.pointchange.audio.view.page

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointchange.audio.R
import com.pointchange.audio.model.SettingViewModel
import com.pointchange.audio.view.LocalNav
import com.pointchange.audio.view.NavID
import com.pointchange.audio.view.page.SettingList.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingPage(settingViewModel: SettingViewModel = viewModel()) {
    val navActions = LocalNav.current
    val loadingState by settingViewModel.loadingState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(loadingState.parseLoading) {
        if (loadingState.parseLoading) {
            Toast.makeText(context, "解析成功", Toast.LENGTH_SHORT).show()
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
        }
    }
}