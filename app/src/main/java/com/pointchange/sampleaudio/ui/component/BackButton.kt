package com.pointchange.sampleaudio.ui.component

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.pointchange.sampleaudio.R

@Composable
fun BackButton(
    tint : Color = Color.Unspecified,
    onClick:()->Unit={}
){
    IconButton(onClick =onClick) {
        Icon(
            painter = painterResource(R.drawable.baseline_arrow_back_24),
            contentDescription = stringResource(R.string.back),
            tint = tint
        )
    }
}