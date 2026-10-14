package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ProductHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductHistoryDao {

    @Query("""
        SELECT productName FROM product_history 
        WHERE productName LIKE '%' || :query || '%' 
        ORDER BY 
            CASE WHEN productName LIKE :query || '%' THEN 0 ELSE 1 END,
            useCount DESC, 
            lastUsedAt DESC 
        LIMIT 10
    """)
    fun searchSuggestions(query: String): Flow<List<String>>

    @Query("SELECT DISTINCT productName FROM product_history ORDER BY useCount DESC, lastUsedAt DESC LIMIT 20")
    fun getRecentSuggestions(): Flow<List<String>>

    @Query("""
        INSERT INTO product_history (productName, defaultUnit, lastUsedAt, useCount)
        VALUES (:productName, :defaultUnit, :timestamp, 1)
        ON CONFLICT(productName) DO UPDATE SET
            lastUsedAt = :timestamp,
            useCount = useCount + 1,
            defaultUnit = :defaultUnit
    """)
    suspend fun recordProductUsage(productName: String, defaultUnit: String, timestamp: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<ProductHistoryEntity>)
}
