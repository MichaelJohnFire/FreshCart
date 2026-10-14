package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "product_history",
    indices = [
        Index(value = ["productName"], unique = true)
    ]
)
data class ProductHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productName: String,
    val defaultUnit: String = "pcs",
    val lastUsedAt: Long = System.currentTimeMillis(),
    val useCount: Int = 1
)
