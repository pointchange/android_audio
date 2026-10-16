package com.pointchange.sampleaudio.ui.component

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

@Composable
fun SearchBarIconButton(
    triple:Triple<Int, Int, () -> Unit>,
    show: Boolean=true
) {
    if(show){
        IconButton(onClick = triple.third) {
            Icon(
                painter = painterResource(triple.second),
                contentDescription = stringResource(
                    triple.first
                ),
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}