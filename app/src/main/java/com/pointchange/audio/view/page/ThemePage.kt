package com.pointchange.audio.view.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointchange.audio.R
import com.pointchange.audio.model.SettingViewModel
import com.pointchange.audio.model.ThemeName
import com.pointchange.audio.view.LocalNav
import com.pointchange.audio.view.state.rememberIsDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePage(settingViewModel: SettingViewModel = viewModel()) {
    val themeConfig by settingViewModel.themeConfig.collectAsStateWithLifecycle()
    val navActions = LocalNav.current
    val list = ThemeName.entries
    val state = rememberPagerState(themeConfig.themeColor.ordinal) { list.size }
    var isLight by remember { mutableStateOf(true) }
    val transparent = Color.Transparent

    val currentTheme = list[state.currentPage]

    Scaffold(
        modifier = Modifier.background(
            brush = Brush.verticalGradient(
                if (isLight) {
                    listOf(
                        currentTheme.getColor().primaryLight,
                        currentTheme.getColor().secondaryLight,
                        currentTheme.getColor().tertiaryLight,
                    )
                } else {
                    listOf(
                        currentTheme.getColor().primaryDark,
                        currentTheme.getColor().secondaryDark,
                        currentTheme.getColor().tertiaryDark,
                    )
                }

            )
        ),
        containerColor = transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = transparent,
                ),
                navigationIcon = {
                    IconButton(onClick = { navActions?.removeLast() }) {
                        Icon(
                            painter = painterResource(R.drawable.round_navigate_before_24),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(themeConfig.themeColor.text),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),

                        style = TextStyle(
                            fontSize = 18.sp,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.6F), offset = Offset(0F, 0F), blurRadius = 3F
                            )
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { isLight = !isLight }) {
                        if (isLight) {
                            Icon(
                                painter = painterResource(R.drawable.round_light_mode_24),
                                contentDescription = stringResource(R.string.theme_light)
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.round_dark_mode_24),
                                contentDescription = stringResource(R.string.theme_dark)
                            )
                        }

                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = transparent) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    ElevatedButton(
                        enabled = currentTheme != themeConfig.themeColor,
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        onClick = {
                            settingViewModel.setThemeColor(currentTheme)
                        }) { Text(stringResource(R.string.ok)) }

                }
            }
        }

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = state,
                modifier = Modifier
                    .fillMaxSize(),
//                    .wrapContentHeight()
//                    .padding(top = 16.dp, bottom = 16.dp),
                pageSpacing = 16.dp,
                contentPadding = PaddingValues(horizontal = 32.dp)
            ) { i ->
                val item = ThemeName.entries[i]
                ElevatedCard(
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    modifier = Modifier
                        .size(width = 240.dp, height = 400.dp)
                ) {
                    if (i == 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            Text(text = stringResource(R.string.theme_Dynamic_Color))
                        }
                    } else {

                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        if (isLight) {
                                            listOf(
                                                item.getColor().primaryLight,
                                                item.getColor().secondaryLight,
                                                item.getColor().tertiaryLight,
                                            )
                                        } else {
                                            listOf(
                                                item.getColor().primaryDark,
                                                item.getColor().secondaryDark,
                                                item.getColor().tertiaryDark,
                                            )
                                        }

                                    )
                                )
                        ) {}
                    }
                }
            }
        }
    }
}