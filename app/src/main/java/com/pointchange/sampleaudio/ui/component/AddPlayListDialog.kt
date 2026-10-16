package com.pointchange.sampleaudio.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pointchange.sampleaudio.R
import com.pointchange.sampleaudio.data.QueueListName
import com.pointchange.sampleaudio.player.queue.QueueData

@Composable
fun AddPlayListDialog(
    queue: List<QueueData>,
    confirmButton: (selected: Int) -> Unit,
    onBack: () -> Unit
) {
    val list = queue.mapNotNull {
        if (it.name == QueueListName.FAVOR.label) {
            null
        } else {
            it.name
        }
    }
    var selected by remember { mutableIntStateOf(-1) }
    AlertDialog(
        onDismissRequest = { onBack() },
        title = { Text(text = "添加到已有歌单") },
        text = {
            LazyColumn {
                items(count = list.size, key = { list[it] }) { index ->
                    val item = list[index]
                    ListItem(
                        modifier = Modifier
                            .clickable(onClick = {
                                selected = index
                            }),
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = {
                            Text(text = item, modifier = Modifier.padding(16.dp))

                        },
                        trailingContent = {
                            if (selected == index) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_check_24),
                                    contentDescription = stringResource(R.string.selected_item)
                                )
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                confirmButton(selected)
                onBack()
            }) { Text(text = stringResource(R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = {
                onBack()
            }) { Text(text = stringResource(R.string.cancel)) }
        }
    )
}