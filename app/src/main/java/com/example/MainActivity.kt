package com.example

import android.app.Application
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddEditItemDialog
import com.example.ui.components.CreateEditListDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.ListDetailScreen
import com.example.ui.screens.ListOverviewScreen
import com.example.ui.theme.FreshCartTheme
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.ShoppingViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FreshCartRoot()
        }
    }
}

@Composable
fun FreshCartRoot() {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: ShoppingViewModel = viewModel(
        factory = ShoppingViewModel.provideFactory(application)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Dynamic localization context provider
    val localizedContext = remember(uiState.languageCode) {
        if (uiState.languageCode == "system" || uiState.languageCode.isBlank()) {
            context
        } else {
            val locale = Locale.forLanguageTag(uiState.languageCode)
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            context.createConfigurationContext(config)
        }
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedContext.resources.configuration
    ) {
        FreshCartTheme(themeMode = uiState.themeMode) {
            FreshCartApp(viewModel = viewModel)
        }
    }
}

@Composable
fun FreshCartApp(viewModel: ShoppingViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbarMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        when (val screen = uiState.currentScreen) {
            is Screen.Overview -> {
                ListOverviewScreen(
                    lists = uiState.lists,
                    onSelectList = { listId -> viewModel.navigateToDetail(listId) },
                    onCreateListClick = { viewModel.openCreateListDialog() },
                    onEditListClick = { list -> viewModel.openEditListDialog(list) },
                    onDeleteListClick = { listId, name -> viewModel.deleteList(listId, name) },
                    onSettingsClick = { viewModel.openSettingsDialog() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is Screen.Detail -> {
                BackHandler {
                    viewModel.navigateToOverview()
                }

                ListDetailScreen(
                    list = uiState.currentList,
                    items = uiState.items,
                    itemFilter = uiState.itemFilter,
                    languageCode = uiState.languageCode,
                    onNavigateBack = { viewModel.navigateToOverview() },
                    onAddItemClick = { viewModel.openAddItemDialog() },
                    onEditItemClick = { item -> viewModel.openEditItemDialog(item) },
                    onTogglePurchased = { item -> viewModel.togglePurchased(item) },
                    onDeleteItem = { item -> viewModel.deleteItem(item) },
                    onReorderItems = { reordered -> viewModel.reorderItems(reordered) },
                    onItemFilterChange = { filter -> viewModel.setItemFilter(filter) },
                    onRenameListClick = {
                        uiState.currentList?.let { cur ->
                            viewModel.openEditListDialog(
                                com.example.data.local.dao.ShoppingListSummary(
                                    id = cur.id,
                                    name = cur.name,
                                    createdAt = cur.createdAt,
                                    updatedAt = cur.updatedAt,
                                    totalItems = uiState.items.size,
                                    purchasedItems = uiState.items.count { it.isPurchased }
                                )
                            )
                        }
                    },
                    onDeleteListClick = {
                        uiState.currentList?.let { cur ->
                            viewModel.deleteList(cur.id, cur.name)
                        }
                    },
                    onClearPurchasedClick = {
                        viewModel.deletePurchasedItems(screen.listId)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    // Dialog: App Settings (Theme & Language Switching)
    if (uiState.showSettingsDialog) {
        SettingsDialog(
            currentThemeMode = uiState.themeMode,
            currentLanguageCode = uiState.languageCode,
            onSave = { theme, lang -> viewModel.saveSettings(theme, lang) },
            onDismiss = { viewModel.closeSettingsDialog() }
        )
    }

    // Dialog: Create new shopping list
    if (uiState.showCreateListDialog) {
        CreateEditListDialog(
            isEditing = false,
            onDismiss = { viewModel.closeCreateListDialog() },
            onConfirm = { name -> viewModel.createList(name) }
        )
    }

    // Dialog: Rename existing shopping list
    uiState.editingList?.let { list ->
        CreateEditListDialog(
            initialName = list.name,
            isEditing = true,
            onDismiss = { viewModel.closeEditListDialog() },
            onConfirm = { newName -> viewModel.updateListName(list.id, newName) }
        )
    }

    // Dialog: Add or Edit Shopping Item
    if (uiState.showAddItemDialog) {
        AddEditItemDialog(
            initialItem = uiState.editingItem,
            suggestions = uiState.autocompleteSuggestions,
            languageCode = uiState.languageCode,
            onQueryChange = { query -> viewModel.onAutocompleteQueryChanged(query) },
            onDismiss = { viewModel.closeAddItemDialog() },
            onConfirm = { name, quantity, unit ->
                val editing = uiState.editingItem
                if (editing != null) {
                    viewModel.updateItem(editing.id, name, quantity, unit)
                } else {
                    viewModel.addItem(name, quantity, unit)
                }
            }
        )
    }
}
