package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R

@Composable
fun CreateEditListDialog(
    initialName: String = "",
    isEditing: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (name: String) -> Unit
) {
    var text by remember { mutableStateOf(initialName) }
    var isError by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.ListAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = if (isEditing) stringResource(R.string.dialog_edit_list_title) else stringResource(R.string.dialog_create_list_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isEditing) stringResource(R.string.dialog_edit_list_desc) else stringResource(R.string.dialog_create_list_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        if (isError && it.isNotBlank()) isError = false
                    },
                    label = { Text(stringResource(R.string.hint_list_name)) },
                    placeholder = { Text(stringResource(R.string.hint_list_name)) },
                    singleLine = true,
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text(stringResource(R.string.error_list_name_empty))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .testTag("list_name_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.trim().isEmpty()) {
                        isError = true
                    } else {
                        onConfirm(text.trim())
                    }
                },
                modifier = Modifier.testTag("save_list_button")
            ) {
                Text(if (isEditing) stringResource(R.string.action_save) else stringResource(R.string.action_create))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_list_button")
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
