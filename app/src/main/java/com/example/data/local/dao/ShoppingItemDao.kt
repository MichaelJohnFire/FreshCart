package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {

    @Query("SELECT * FROM shopping_items WHERE listId = :listId ORDER BY isPurchased ASC, name COLLATE NOCASE ASC")
    fun getItemsAscending(listId: Long): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM shopping_items WHERE listId = :listId ORDER BY isPurchased ASC, name COLLATE NOCASE DESC")
    fun getItemsDescending(listId: Long): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM shopping_items WHERE listId = :listId ORDER BY isPurchased ASC, createdAt DESC")
    fun getItemsByDefault(listId: Long): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM shopping_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Long): ShoppingItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItemEntity): Long

    @Update
    suspend fun updateItem(item: ShoppingItemEntity)

    @Query("UPDATE shopping_items SET isPurchased = :isPurchased WHERE id = :id")
    suspend fun setPurchased(id: Long, isPurchased: Boolean)

    @Query("UPDATE shopping_items SET name = :name, quantity = :quantity, unit = :unit WHERE id = :id")
    suspend fun updateItemDetails(id: Long, name: String, quantity: Double, unit: String)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("DELETE FROM shopping_items WHERE listId = :listId AND isPurchased = 1")
    suspend fun deletePurchasedItems(listId: Long)
}
