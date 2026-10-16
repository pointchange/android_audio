package com.pointchange.sampleaudio.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.pointchange.sampleaudio.R
import com.pointchange.sampleaudio.data.AudioGroup
import com.pointchange.sampleaudio.ui.theme.ext
import java.io.File
import java.nio.file.Paths
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.io.path.pathString

@Composable
fun LazyScanResultList(
    list: List<String>,
    onItemClick: (path: String) -> Unit = {},
) {

    val paths = list.map { Paths.get(it) }
        .groupBy { it.parent }
        .map { (dir, files) ->
            AudioGroup(
                title = dir.fileName.toString(),
                parentPath = dir.parent.toString(),
                files = files.map { it.fileName.toString() }
            )
        }
    var expandedIndex by remember { mutableStateOf<Int?>(null) }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(paths) { index, group ->
            val expanded = expandedIndex == index
            ListItem(
                modifier = Modifier.clickable {
                    expandedIndex = if (expanded) null else index
                },
                trailingContent = {
                    Icon(
                        painter = painterResource(
                            if (expanded) {
                                R.drawable.baseline_keyboard_arrow_down_24
                            } else {
                                R.drawable.baseline_keyboard_arrow_right_24
                            }
                        ), contentDescription = null
                    )
                },
                headlineContent = {
                    Text(text = group.title)
                },
                supportingContent = {
                    Text(text = group.parentPath)
                },
            )
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(start = MaterialTheme.ext.spacing.large)) {
                    group.files.forEach { file ->
                        ListItem(
                            modifier = Modifier.clickable {
                                onItemClick(
                                    Paths.get(
                                        group.parentPath,
                                        group.title,
                                        file
                                    ).pathString
                                )
                            },
                            headlineContent = {
                                Text(text = file)
                            }
                        )
                    }
                }
            }
        }
    }
}