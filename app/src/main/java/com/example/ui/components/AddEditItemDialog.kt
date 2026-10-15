package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.ProductUnits
import com.example.data.local.entity.ShoppingItemEntity
import java.text.DecimalFormat

@Composable
fun AddEditItemDialog(
    initialItem: ShoppingItemEntity? = null,
    suggestions: List<String>,
    languageCode: String = "system",
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (name: String, quantity: Double, unit: String) -> Unit
) {
    val isEditing = initialItem != null
    val isPolish = remember(languageCode) { ProductUnits.isPolishLanguage(languageCode) }
    val availableUnits = remember(languageCode) { ProductUnits.getAvailableUnits(languageCode) }
    val defaultUnit = remember(languageCode) { ProductUnits.getDefaultUnit(languageCode) }

    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var quantity by remember { mutableDoubleStateOf(initialItem?.quantity ?: 1.0) }
    var unit by remember {
        mutableStateOf(
            if (initialItem != null) {
                ProductUnits.localizeUnitForDisplay(initialItem.unit, isPolish)
            } else {
                defaultUnit
            }
        )
    }
    var isNameError by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val df = remember { DecimalFormat("#.##") }

    LaunchedEffect(Unit) {
        onQueryChange(name)
        if (!isEditing) {
            focusRequester.requestFocus()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = if (isEditing) stringResource(R.string.dialog_edit_item_title) else stringResource(R.string.dialog_add_item_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (isEditing) stringResource(R.string.dialog_edit_item_desc) else stringResource(R.string.dialog_add_item_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Product Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        onQueryChange(it)
                        if (isNameError && it.isNotBlank()) isNameError = false
                    },
                    label = { Text(stringResource(R.string.hint_item_name)) },
                    placeholder = { Text(stringResource(R.string.hint_item_name)) },
                    singleLine = true,
                    isError = isNameError,
                    supportingText = {
                        if (isNameError) {
                            Text(stringResource(R.string.error_item_name_empty))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .testTag("item_name_input")
                )

                // Autocomplete Suggestions from History
                if (suggestions.isNotEmpty() && name.isNotBlank() && !suggestions.contains(name)) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.suggestions_from_history),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        suggestions.take(5).forEachIndexed { index, suggestion ->
                            SuggestionChip(
                                onClick = {
                                    name = suggestion
                                    onQueryChange(suggestion)
                                    isNameError = false
                                },
                                label = { Text(suggestion) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.testTag("suggestion_chip_$index")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quantity Stepper & Input
                Text(
                    text = stringResource(R.string.hint_quantity),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (quantity > 0.5) {
                                quantity = if (quantity <= 1.0) 0.5 else quantity - 1.0
                            }
                        },
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .testTag("quantity_decrement_button")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.cd_decrease))
                    }

                    OutlinedTextField(
                        value = df.format(quantity),
                        onValueChange = { input ->
                            val parsed = input.toDoubleOrNull()
                            if (parsed != null && parsed >= 0) {
                                quantity = parsed
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("item_quantity_input")
                    )

                    IconButton(
                        onClick = { quantity += 1.0 },
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .testTag("quantity_increment_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_increase))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Unit Selection Chips
                Text(
                    text = stringResource(R.string.hint_unit),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableUnits.forEach { u ->
                        FilterChip(
                            selected = unit.equals(u, ignoreCase = true),
                            onClick = { unit = u },
                            label = { Text(u) },
                            modifier = Modifier.testTag("unit_chip_$u")
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isEmpty()) {
                        isNameError = true
                    } else {
                        onConfirm(name.trim(), quantity, unit)
                    }
                },
                modifier = Modifier.testTag("save_item_button")
            ) {
                Text(if (isEditing) stringResource(R.string.action_save) else stringResource(R.string.action_add_item))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_item_button")
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
