package com.pointchange.audio.view.component

import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.pointchange.audio.R

/**
 * @param permission Manifest permission
 *
 * @param permissionText Manifest permission String
 *
 * @param restoreStatus Manifest permission String
 *
 *
 * @param content Operations after enabling permissions
 */
@Composable
fun PermissionRequest(
    permission: String,
    permissionText: String,
    restoreStatus: () -> Unit = {},
    content: () -> Unit,
) {
    val context = LocalContext.current
    var isPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var showPermissionPromptDialog by remember { mutableStateOf(true) }
    var showRationaleDialog by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            isPermissionGranted = isGranted
            if (!isGranted) {
                showPermissionPromptDialog = false
                showRationaleDialog = ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_DENIED
            }
        }
    )
    when {
        isPermissionGranted -> {
            content()
            restoreStatus()
        }

        showRationaleDialog -> {

            AlertDialog(
                onDismissRequest = {
                    showRationaleDialog = false
                    restoreStatus()
                },
                title = { Text(text = stringResource(R.string.go_settings)) },
                text = {
                    Column {
                        Text(text = stringResource(R.string.go_settings_content))
                        Text(text = permissionText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showRationaleDialog = false
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        intent.data = ("package:" + context.applicationContext.packageName).toUri()
                        context.applicationContext.startActivity(intent)
                        restoreStatus()

                    }) {
                        Text(text = stringResource(R.string.go_settings))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showRationaleDialog = false
                        restoreStatus()

                    }) {
                        Text(text = stringResource(R.string.cancel))
                    }
                }
            )
        }

        else -> {
            if (showPermissionPromptDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showPermissionPromptDialog = false
                        restoreStatus()
                    },
                    title = { Text("${stringResource(R.string.enable)} $permissionText") },
                    text = {},
                    confirmButton = {
                        TextButton(onClick = {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    permission
                                ) == PackageManager.PERMISSION_DENIED
                            ) {
                                permissionLauncher.launch(permission)
                            }
                            showPermissionPromptDialog = false
                        }) {
                            Text(stringResource(R.string.ok))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showPermissionPromptDialog = false
                            restoreStatus()

                        }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
        }
    }
}