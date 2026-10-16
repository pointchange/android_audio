package com.pointchange.sampleaudio.ui.page

import android.os.Environment
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointchange.sampleaudio.R
import com.pointchange.sampleaudio.ui.component.BackButton
import com.pointchange.sampleaudio.ui.component.ColumnCenterContainer
import com.pointchange.sampleaudio.ui.theme.ext
import com.pointchange.sampleaudio.viewmodel.Nav3ViewModel
import com.pointchange.sampleaudio.viewmodel.PlayerStateViewModel
import java.io.File

data class FolderItem(
    val name: String,
    val path: String
)

fun getSubFolders(path: String): List<FolderItem> {
    return File(path).listFiles()?.filter { it.isDirectory }
        ?.map { FolderItem(name = it.name, path = it.absolutePath) } ?: emptyList()
}

fun parentPath(path: String): String? {
    val file = File(path)
    return file.parent
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderFilterPage() {
    val nav3ViewModel = viewModel<Nav3ViewModel>()
    val viewModel = viewModel<PlayerStateViewModel>()
    val scanConfig by viewModel.scanConfig.collectAsStateWithLifecycle()
    val rootPath = Environment.getExternalStorageDirectory().absolutePath
    var currentPath by remember { mutableStateOf(rootPath) }
    var folders by remember { mutableStateOf(getSubFolders(rootPath)) }
    val filterFolders = remember { scanConfig.excludeFolders.toMutableStateList() }
    fun back() {
        if (currentPath == rootPath) {
            nav3ViewModel.pop()
        } else {
            parentPath(currentPath)?.let {
                currentPath = it
                folders = getSubFolders(it)
            }
        }
    }
    BackHandler {
        back()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton {
                        back()
                    }
                },
                title = { Text(text = stringResource(R.string.select_filter_folder)) }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.setScanConfigExcludeFolders(filterFolders)
                        nav3ViewModel.pop()
                    }) {
                    Text(text = stringResource(R.string.ok))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(MaterialTheme.ext.spacing.medium)
        ) {
            Text(text = currentPath, modifier = Modifier.padding(MaterialTheme.ext.spacing.small))
            if (folders.isEmpty()) {
                ColumnCenterContainer {
                    Text(
                        text = stringResource(R.string.empty),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                return@Column
            }
            LazyColumn() {
                items(folders) { folder ->
                    val checked = filterFolders.contains(folder.path)
                    Row(
                        modifier = Modifier
                            .clickable {
                                currentPath = folder.path
                                folders = getSubFolders(folder.path)
                            }
                            .fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = checked, onCheckedChange = {
                            if (it) {
                                filterFolders.add(folder.path)
                            } else {
                                filterFolders.remove(folder.path)
                            }
                        })
                        Icon(
                            painter = painterResource(R.drawable.twotone_folder_open_24),
                            contentDescription = stringResource(R.string.folder),
                            tint = Color(0xFFFBC02D),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = folder.name,
                            modifier = Modifier.padding(start = MaterialTheme.ext.spacing.small)
                        )
                    }
                }
            }
        }
    }

}