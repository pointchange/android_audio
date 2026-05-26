package com.pointchange.audio.view.component

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.pointchange.audio.R

@Composable
fun PlayPauseBtn(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    IconButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Icon(
            painter = if (isPlaying) painterResource(R.drawable.round_pause_24) else painterResource(
                R.drawable.round_play_arrow_24
            ),
            contentDescription = stringResource(R.string.pause),
            tint = tint,
            modifier = modifier,
        )
    }
}