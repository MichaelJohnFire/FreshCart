package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ShoppingListEntity
import kotlinx.coroutines.flow.Flow

data class ShoppingListSummary(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val totalItems: Int,
    val purchasedItems: Int
)

@Dao
interface ShoppingListDao {

    @Query("""
        SELECT 
            l.id AS id, 
            l.name AS name, 
            l.createdAt AS createdAt, 
            l.updatedAt AS updatedAt,
            COUNT(i.id) AS totalItems,
            COALESCE(SUM(CASE WHEN i.isPurchased = 1 THEN 1 ELSE 0 END), 0) AS purchasedItems
        FROM shopping_lists l
        LEFT JOIN shopping_items i ON l.id = i.listId
        GROUP BY l.id
        ORDER BY l.updatedAt DESC
    """)
    fun getAllListsWithStats(): Flow<List<ShoppingListSummary>>

    @Query("SELECT * FROM shopping_lists WHERE id = :id LIMIT 1")
    fun getListById(id: Long): Flow<ShoppingListEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: ShoppingListEntity): Long

    @Update
    suspend fun updateList(list: ShoppingListEntity)

    @Query("UPDATE shopping_lists SET name = :name, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateListName(id: Long, name: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM shopping_lists WHERE id = :id")
    suspend fun deleteListById(id: Long)
}
