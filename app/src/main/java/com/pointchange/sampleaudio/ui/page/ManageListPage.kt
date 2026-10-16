package com.pointchange.sampleaudio.ui.page

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointchange.sampleaudio.R
import com.pointchange.sampleaudio.data.QueueListName
import com.pointchange.sampleaudio.datastore.DataStoreProvider
import com.pointchange.sampleaudio.player.queue.QueueData
import com.pointchange.sampleaudio.ui.component.BackButton
import com.pointchange.sampleaudio.viewmodel.Nav3ViewModel
import com.pointchange.sampleaudio.viewmodel.PlayerStateViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageListPage() {
    val viewModel = viewModel<PlayerStateViewModel>()
    val nav3ViewModel = viewModel<Nav3ViewModel>()
    val queue by viewModel.libraryQueueAsFn.queue.collectAsStateWithLifecycle()
    val list = queue.toMutableStateList()
    var value by remember { mutableStateOf("") }
    var addError by remember { mutableStateOf(false) }
    var isEdit by remember { mutableStateOf(false) }
    var editIndex by remember { mutableIntStateOf(-1) }
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState =
        rememberReorderableLazyListState(lazyListState) { from, to ->
            val temp = list[to.index]
            list[to.index] = list[from.index]
            list[from.index] = temp
        }

    fun add() {
        val name = value.trim()
        if (!list.any { it.name == name } && name.isNotEmpty() && name.isNotBlank()) {
            list.add(QueueData(name))
        } else {
            addError = true
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton { nav3ViewModel.pop() }
                },
                title = {
                    OutlinedTextField(
                        value = value,
                        onValueChange = {
                            addError = false
                            value = it
                        },
                        isError = addError,
                        supportingText = {
                            if (addError) {
                                Text(text = stringResource(R.string.error_name_tip))
                            }
                        },
                        placeholder = { Text(text = stringResource(R.string.add_list_name)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            add()
                        })
                    )
                },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            add()
                        }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_add_24),
                            contentDescription = stringResource(R.string.add)
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilledTonalButton(
                        onClick = { nav3ViewModel.pop() },
                        modifier = Modifier.widthIn(min = 100.dp, max = 160.dp)
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }
                    OutlinedButton(
                        onClick = {
                            list.clear()
                            list.addAll(queue)
                        }
                    ) { Text(text = stringResource(R.string.recover)) }
                    Button(
                        onClick = {
//                            viewModel.setQueue(list)
                            viewModel.libraryQueueAsFn.updateQueueNames(
                                queue = list
                            )
                            nav3ViewModel.pop()
                        },
                        modifier = Modifier.widthIn(min = 100.dp, max = 160.dp)
                    ) {
                        Text(text = stringResource(R.string.ok))
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()

        ) {
            items(count = list.size, key = { index -> list[index].name }) { index ->
                val item = list[index]
                ReorderableItem(state = reorderableLazyListState, key = item.name) {
                    Box(modifier = Modifier.longPressDraggableHandle()) {
                        ListItem(
                            modifier = Modifier.combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    when (item.name) {
                                        QueueListName.DEFAULT.label, QueueListName.FAVOR.label -> {}
                                        else -> {
                                            isEdit = true
                                            editIndex = index
                                        }
                                    }
                                }
                            ),
                            leadingContent = {
                                Icon(
                                    modifier = Modifier.draggableHandle(),
                                    painter = painterResource(R.drawable.baseline_drag_handle_24),
                                    contentDescription = stringResource(R.string.drag)
                                )
                            },
                            headlineContent = {
                                Text(text = item.name)
                            },
                            trailingContent = {
                                IconButton(
                                    enabled = when (item.name) {
                                        QueueListName.DEFAULT.label, QueueListName.FAVOR.label -> false
                                        else -> true
                                    },
                                    onClick = { list.removeAt(index) }) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_delete_24),
                                        contentDescription = stringResource(R.string.delete)
                                    )
                                }

                            }
                        )
                    }

                }
            }
        }

        if (isEdit) {
            var name by remember { mutableStateOf(list[editIndex].name) }
            var error by remember { mutableStateOf(false) }
            AlertDialog(
                onDismissRequest = {
                    isEdit = false
                },
                title = {
                    Text(text = stringResource(R.string.edit_list_name))
                },
                text = {
                    TextField(
                        value = name,
                        onValueChange = {
                            error = false
                            name = it
                        },
                        supportingText = {
                            if (error) {
                                Text(text = stringResource(R.string.error_name_tip))
                            }
                        },
                        isError = error
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val newName = name.trim()
                            if (!list.any { it.name == name } && newName.isNotEmpty()) {
                                list[editIndex] = list[editIndex].copy(name = newName)
                                isEdit = false
                            } else {
                                error = true
                            }
                        },
                    ) {
                        Text(text = stringResource(R.string.ok))
                    }
                },
                dismissButton = {
                    FilledTonalButton(
                        onClick = {
                            isEdit = false
                        },
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }
                }
            )
        }

    }
}
