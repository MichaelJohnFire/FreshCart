package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.ProductHistoryDao
import com.example.data.local.dao.ShoppingItemDao
import com.example.data.local.dao.ShoppingListDao
import com.example.data.local.entity.ProductHistoryEntity
import com.example.data.local.entity.ShoppingItemEntity
import com.example.data.local.entity.ShoppingListEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ShoppingListEntity::class,
        ShoppingItemEntity::class,
        ProductHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun shoppingItemDao(): ShoppingItemDao
    abstract fun productHistoryDao(): ProductHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "freshcart_database.db"
                )
                    .addCallback(DatabasePrepopulateCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabasePrepopulateCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.let { database ->
                    // Seed initial starter staples and a sample shopping list
                    val initialStaples = listOf(
                        ProductHistoryEntity(productName = "Fresh Milk", defaultUnit = "liter", useCount = 5),
                        ProductHistoryEntity(productName = "Eggs (Free Range)", defaultUnit = "pack", useCount = 5),
                        ProductHistoryEntity(productName = "Sourdough Bread", defaultUnit = "pcs", useCount = 4),
                        ProductHistoryEntity(productName = "Organic Bananas", defaultUnit = "kg", useCount = 4),
                        ProductHistoryEntity(productName = "Crisp Apples", defaultUnit = "kg", useCount = 4),
                        ProductHistoryEntity(productName = "Ground Coffee", defaultUnit = "pack", useCount = 3),
                        ProductHistoryEntity(productName = "Cheddar Cheese", defaultUnit = "pack", useCount = 3),
                        ProductHistoryEntity(productName = "Cherry Tomatoes", defaultUnit = "pack", useCount = 3),
                        ProductHistoryEntity(productName = "Baby Spinach", defaultUnit = "pack", useCount = 3),
                        ProductHistoryEntity(productName = "Greek Yogurt", defaultUnit = "pack", useCount = 2),
                        ProductHistoryEntity(productName = "Olive Oil", defaultUnit = "bottle", useCount = 2),
                        ProductHistoryEntity(productName = "Chicken Breast", defaultUnit = "kg", useCount = 2)
                    )
                    database.productHistoryDao().insertAll(initialStaples)

                    // Seed default starter list so user immediately has something to explore
                    val listId = database.shoppingListDao().insertList(
                        ShoppingListEntity(
                            name = "Weekly Groceries",
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                    )

                    database.shoppingItemDao().insertItem(
                        ShoppingItemEntity(
                            listId = listId,
                            name = "Fresh Milk",
                            quantity = 2.0,
                            unit = "liter",
                            isPurchased = false
                        )
                    )
                    database.shoppingItemDao().insertItem(
                        ShoppingItemEntity(
                            listId = listId,
                            name = "Sourdough Bread",
                            quantity = 1.0,
                            unit = "pcs",
                            isPurchased = false
                        )
                    )
                    database.shoppingItemDao().insertItem(
                        ShoppingItemEntity(
                            listId = listId,
                            name = "Organic Bananas",
                            quantity = 1.5,
                            unit = "kg",
                            isPurchased = true
                        )
                    )
                }
            }
        }
    }
}
