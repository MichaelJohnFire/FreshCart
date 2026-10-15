package com.example.data.repository

import com.example.data.local.dao.ProductHistoryDao
import com.example.data.local.dao.ShoppingItemDao
import com.example.data.local.dao.ShoppingListDao
import com.example.data.local.dao.ShoppingListSummary
import com.example.data.local.entity.ShoppingItemEntity
import com.example.data.local.entity.ShoppingListEntity
import kotlinx.coroutines.flow.Flow

enum class SortOrder {
    DEFAULT,
    ALPHABETICAL_ASC,
    ALPHABETICAL_DESC
}

enum class ItemFilter {
    ALL,
    UNCHECKED,
    PURCHASED
}

class ShoppingRepository(
    private val shoppingListDao: ShoppingListDao,
    private val shoppingItemDao: ShoppingItemDao,
    private val productHistoryDao: ProductHistoryDao
) {

    val listsWithStats: Flow<List<ShoppingListSummary>> = shoppingListDao.getAllListsWithStats()

    fun getListById(listId: Long): Flow<ShoppingListEntity?> = shoppingListDao.getListById(listId)

    suspend fun createList(name: String): Long {
        val now = System.currentTimeMillis()
        return shoppingListDao.insertList(
            ShoppingListEntity(name = name.trim(), createdAt = now, updatedAt = now)
        )
    }

    suspend fun updateListName(listId: Long, newName: String) {
        shoppingListDao.updateListName(listId, newName.trim(), System.currentTimeMillis())
    }

    suspend fun deleteList(listId: Long) {
        shoppingListDao.deleteListById(listId)
    }

    fun getItems(listId: Long, sortOrder: SortOrder): Flow<List<ShoppingItemEntity>> {
        return when (sortOrder) {
            SortOrder.ALPHABETICAL_ASC -> shoppingItemDao.getItemsAscending(listId)
            SortOrder.ALPHABETICAL_DESC -> shoppingItemDao.getItemsDescending(listId)
            SortOrder.DEFAULT -> shoppingItemDao.getItemsByDefault(listId)
        }
    }

    suspend fun addItem(listId: Long, name: String, quantity: Double, unit: String): Long {
        val cleanName = name.trim()
        val cleanUnit = unit.trim().ifEmpty { "pcs" }
        val nextPosition = shoppingItemDao.getMaxPosition(listId) + 1
        val id = shoppingItemDao.insertItem(
            ShoppingItemEntity(
                listId = listId,
                name = cleanName,
                quantity = quantity,
                unit = cleanUnit,
                isPurchased = false,
                position = nextPosition,
                createdAt = System.currentTimeMillis()
            )
        )
        // Remember in autocomplete product history across all lists
        if (cleanName.isNotBlank()) {
            productHistoryDao.recordProductUsage(cleanName, cleanUnit)
        }
        return id
    }

    suspend fun reorderItems(orderedItems: List<ShoppingItemEntity>) {
        val positions = orderedItems.mapIndexed { index, item ->
            Pair(item.id, index)
        }
        shoppingItemDao.updateItemOrder(positions)
    }

    suspend fun updateItem(id: Long, name: String, quantity: Double, unit: String) {
        val cleanName = name.trim()
        val cleanUnit = unit.trim().ifEmpty { "pcs" }
        shoppingItemDao.updateItemDetails(id, cleanName, quantity, cleanUnit)
        if (cleanName.isNotBlank()) {
            productHistoryDao.recordProductUsage(cleanName, cleanUnit)
        }
    }

    suspend fun togglePurchased(id: Long, isPurchased: Boolean) {
        shoppingItemDao.setPurchased(id, isPurchased)
    }

    suspend fun deleteItem(id: Long) {
        shoppingItemDao.deleteItemById(id)
    }

    suspend fun deletePurchasedItems(listId: Long) {
        shoppingItemDao.deletePurchasedItems(listId)
    }

    fun searchSuggestions(query: String): Flow<List<String>> {
        return if (query.isBlank()) {
            productHistoryDao.getRecentSuggestions()
        } else {
            productHistoryDao.searchSuggestions(query.trim())
        }
    }
}
