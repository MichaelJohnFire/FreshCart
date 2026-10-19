package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.local.ThemeMode
import com.example.data.local.dao.ShoppingListSummary
import com.example.data.local.entity.ShoppingItemEntity
import com.example.data.local.entity.ShoppingListEntity
import com.example.data.repository.ItemFilter
import com.example.data.repository.ShoppingRepository
import com.example.data.repository.SortOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface Screen {
    data object Overview : Screen
    data class Detail(val listId: Long) : Screen
}

data class ShoppingUiState(
    val currentScreen: Screen = Screen.Overview,
    val lists: List<ShoppingListSummary> = emptyList(),
    val currentList: ShoppingListEntity? = null,
    val items: List<ShoppingItemEntity> = emptyList(),
    val sortOrder: SortOrder = SortOrder.DEFAULT,
    val itemFilter: ItemFilter = ItemFilter.ALL,
    val searchQuery: String = "",
    val autocompleteSuggestions: List<String> = emptyList(),
    val showCreateListDialog: Boolean = false,
    val editingList: ShoppingListSummary? = null,
    val showAddItemDialog: Boolean = false,
    val editingItem: ShoppingItemEntity? = null,
    val snackbarMessage: String? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val languageCode: String = "system",
    val showSettingsDialog: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingViewModel(
    application: Application,
    private val repository: ShoppingRepository,
    private val preferencesManager: PreferencesManager = PreferencesManager.getInstance(application)
) : AndroidViewModel(application) {

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Overview)
    private val _sortOrder = MutableStateFlow(SortOrder.DEFAULT)
    private val _itemFilter = MutableStateFlow(ItemFilter.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _autocompleteQuery = MutableStateFlow("")
    private val _showCreateListDialog = MutableStateFlow(false)
    private val _editingList = MutableStateFlow<ShoppingListSummary?>(null)
    private val _showAddItemDialog = MutableStateFlow(false)
    private val _editingItem = MutableStateFlow<ShoppingItemEntity?>(null)
    private val _showSettingsDialog = MutableStateFlow(false)
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    val lists: StateFlow<List<ShoppingListSummary>> = repository.listsWithStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val currentListFlow = _currentScreen.flatMapLatest { screen ->
        if (screen is Screen.Detail) {
            repository.getListById(screen.listId)
        } else {
            flowOf(null)
        }
    }

    private val rawItemsFlow = combine(_currentScreen, _sortOrder) { screen, sort ->
        Pair(screen, sort)
    }.flatMapLatest { (screen, sort) ->
        if (screen is Screen.Detail) {
            repository.getItems(screen.listId, sort)
        } else {
            flowOf(emptyList())
        }
    }

    private val filteredItemsFlow = combine(rawItemsFlow, _itemFilter, _searchQuery) { items, filter, query ->
        items.filter { item ->
            val matchesFilter = when (filter) {
                ItemFilter.ALL -> true
                ItemFilter.UNCHECKED -> !item.isPurchased
                ItemFilter.PURCHASED -> item.isPurchased
            }
            val matchesSearch = query.isBlank() || item.name.contains(query.trim(), ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    val autocompleteSuggestions: StateFlow<List<String>> = _autocompleteQuery
        .flatMapLatest { query -> repository.searchSuggestions(query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private data class DialogState(
        val showCreate: Boolean,
        val editingList: ShoppingListSummary?,
        val showAdd: Boolean,
        val editingItem: ShoppingItemEntity?,
        val showSettings: Boolean
    )

    private val dialogStateFlow = combine(
        _showCreateListDialog,
        _editingList,
        _showAddItemDialog,
        _editingItem,
        _showSettingsDialog
    ) { showCreate, editingList, showAdd, editingItem, showSettings ->
        DialogState(showCreate, editingList, showAdd, editingItem, showSettings)
    }

    private val userPrefsFlow = combine(
        preferencesManager.themeModeFlow,
        preferencesManager.languageCodeFlow
    ) { theme, language ->
        Pair(theme, language)
    }

    val uiState: StateFlow<ShoppingUiState> = combine(
        combine(_currentScreen, lists, currentListFlow) { screen, allLists, curList ->
            Triple(screen, allLists, curList)
        },
        combine(filteredItemsFlow, _sortOrder, _itemFilter) { items, sort, filter ->
            Triple(items, sort, filter)
        },
        combine(_searchQuery, autocompleteSuggestions) { query, suggestions ->
            Pair(query, suggestions)
        },
        combine(dialogStateFlow, userPrefsFlow) { dialogs, prefs ->
            Pair(dialogs, prefs)
        },
        _snackbarMessage
    ) { (screen, allLists, curList), (items, sort, filter), (query, suggestions), (dialogState, prefs), snackbar ->
        ShoppingUiState(
            currentScreen = screen,
            lists = allLists,
            currentList = curList,
            items = items,
            sortOrder = sort,
            itemFilter = filter,
            searchQuery = query,
            autocompleteSuggestions = suggestions,
            showCreateListDialog = dialogState.showCreate,
            editingList = dialogState.editingList,
            showAddItemDialog = dialogState.showAdd,
            editingItem = dialogState.editingItem,
            showSettingsDialog = dialogState.showSettings,
            themeMode = prefs.first,
            languageCode = prefs.second,
            snackbarMessage = snackbar
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShoppingUiState())

    fun navigateToDetail(listId: Long) {
        _searchQuery.value = ""
        _currentScreen.value = Screen.Detail(listId)
    }

    fun navigateToOverview() {
        _searchQuery.value = ""
        _currentScreen.value = Screen.Overview
    }

    fun openCreateListDialog() {
        _showCreateListDialog.value = true
    }

    fun closeCreateListDialog() {
        _showCreateListDialog.value = false
    }

    fun openEditListDialog(list: ShoppingListSummary) {
        _editingList.value = list
    }

    fun closeEditListDialog() {
        _editingList.value = null
    }

    fun openSettingsDialog() {
        _showSettingsDialog.value = true
    }

    fun closeSettingsDialog() {
        _showSettingsDialog.value = false
    }

    fun saveSettings(themeMode: ThemeMode, languageCode: String) {
        viewModelScope.launch {
            preferencesManager.saveSettings(themeMode, languageCode)
            _showSettingsDialog.value = false
            _snackbarMessage.value = getApplication<Application>().getString(R.string.msg_settings_saved)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            preferencesManager.setLanguage(languageCode)
        }
    }

    fun createList(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = repository.createList(name)
            _showCreateListDialog.value = false
            _snackbarMessage.value = "Created \"$name\""
            navigateToDetail(id)
        }
    }

    fun updateListName(listId: Long, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.updateListName(listId, newName)
            _editingList.value = null
            _snackbarMessage.value = "Renamed to \"$newName\""
        }
    }

    fun deleteList(listId: Long, listName: String) {
        viewModelScope.launch {
            repository.deleteList(listId)
            if (_currentScreen.value is Screen.Detail && (_currentScreen.value as Screen.Detail).listId == listId) {
                navigateToOverview()
            }
            _snackbarMessage.value = "Deleted \"$listName\""
        }
    }

    fun openAddItemDialog() {
        _editingItem.value = null
        _autocompleteQuery.value = ""
        _showAddItemDialog.value = true
    }

    fun closeAddItemDialog() {
        _showAddItemDialog.value = false
        _editingItem.value = null
    }

    fun openEditItemDialog(item: ShoppingItemEntity) {
        _editingItem.value = item
        _autocompleteQuery.value = item.name
        _showAddItemDialog.value = true
    }

    fun onAutocompleteQueryChanged(query: String) {
        _autocompleteQuery.value = query
    }

    fun addItem(name: String, quantity: Double, unit: String) {
        val screen = _currentScreen.value
        if (screen !is Screen.Detail || name.isBlank()) return
        viewModelScope.launch {
            repository.addItem(screen.listId, name, quantity, unit)
            _showAddItemDialog.value = false
            _snackbarMessage.value = "Added $name"
        }
    }

    fun updateItem(id: Long, name: String, quantity: Double, unit: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.updateItem(id, name, quantity, unit)
            _showAddItemDialog.value = false
            _editingItem.value = null
            _snackbarMessage.value = "Updated $name"
        }
    }

    fun togglePurchased(item: ShoppingItemEntity) {
        viewModelScope.launch {
            repository.togglePurchased(item.id, !item.isPurchased)
        }
    }

    fun reorderItems(reorderedItems: List<ShoppingItemEntity>) {
        _sortOrder.value = SortOrder.DEFAULT
        viewModelScope.launch {
            repository.reorderItems(reorderedItems)
        }
    }

    fun deleteItem(item: ShoppingItemEntity) {
        viewModelScope.launch {
            repository.deleteItem(item.id)
            _snackbarMessage.value = "Removed ${item.name}"
        }
    }

    fun deletePurchasedItems(listId: Long) {
        viewModelScope.launch {
            repository.deletePurchasedItems(listId)
            _snackbarMessage.value = "Cleared purchased items"
        }
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun setItemFilter(filter: ItemFilter) {
        _itemFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = AppDatabase.getInstance(application)
                    val repo = ShoppingRepository(
                        shoppingListDao = db.shoppingListDao(),
                        shoppingItemDao = db.shoppingItemDao(),
                        productHistoryDao = db.productHistoryDao()
                    )
                    return ShoppingViewModel(
                        application = application,
                        repository = repo,
                        preferencesManager = PreferencesManager.getInstance(application)
                    ) as T
                }
            }
        }
    }
}
