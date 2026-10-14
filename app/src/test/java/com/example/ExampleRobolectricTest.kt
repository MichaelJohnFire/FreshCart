package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.local.ThemeMode
import com.example.data.local.entity.ShoppingItemEntity
import com.example.data.local.entity.ShoppingListEntity
import com.example.data.repository.ShoppingRepository
import com.example.data.repository.SortOrder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: ShoppingRepository
    private lateinit var preferencesManager: PreferencesManager

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ShoppingRepository(
            shoppingListDao = db.shoppingListDao(),
            shoppingItemDao = db.shoppingItemDao(),
            productHistoryDao = db.productHistoryDao()
        )
        preferencesManager = PreferencesManager(context)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `appName is FreshCart`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FreshCart", appName)
    }

    @Test
    fun `save and observe settings with DataStore`() = runBlocking {
        // Test saving settings together via saveSettings
        preferencesManager.saveSettings(ThemeMode.DARK, "pl")
        assertEquals(ThemeMode.DARK, preferencesManager.themeModeFlow.first())
        assertEquals("pl", preferencesManager.languageCodeFlow.first())

        // Test updating theme mode
        preferencesManager.setThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, preferencesManager.themeModeFlow.first())

        // Test updating language
        preferencesManager.setLanguage("en")
        assertEquals("en", preferencesManager.languageCodeFlow.first())

        // Test resetting to system default
        preferencesManager.saveSettings(ThemeMode.SYSTEM, "system")
        assertEquals(ThemeMode.SYSTEM, preferencesManager.themeModeFlow.first())
        assertEquals("system", preferencesManager.languageCodeFlow.first())
    }

    @Test
    fun `polish translations load correctly`() {
        val baseContext = ApplicationProvider.getApplicationContext<Context>()
        val config = android.content.res.Configuration(baseContext.resources.configuration)
        config.setLocale(java.util.Locale.forLanguageTag("pl"))
        val polishContext = baseContext.createConfigurationContext(config)

        assertEquals("Moje Listy Zakupów", polishContext.getString(R.string.title_shopping_lists))
        assertEquals("Nowa Lista", polishContext.getString(R.string.action_create_list))
        assertEquals("Kupione", polishContext.getString(R.string.filter_purchased))
        assertEquals("Ustawienia", polishContext.getString(R.string.settings_title))
        assertEquals("Brak list zakupów", polishContext.getString(R.string.empty_lists_title))
    }

    @Test
    fun `create and rename shopping list`() = runBlocking {
        val listId = repository.createList("Groceries")
        val created = repository.getListById(listId).first()
        assertEquals("Groceries", created?.name)

        repository.updateListName(listId, "Supermarket Run")
        val updated = repository.getListById(listId).first()
        assertEquals("Supermarket Run", updated?.name)
    }

    @Test
    fun `manage items and toggle purchased`() = runBlocking {
        val listId = repository.createList("Weekly Essentials")
        val itemId = repository.addItem(listId, "Fresh Milk", 2.0, "liter")

        val items = repository.getItems(listId, SortOrder.DEFAULT).first()
        assertEquals(1, items.size)
        assertEquals("Fresh Milk", items[0].name)
        assertEquals(2.0, items[0].quantity, 0.001)
        assertEquals("liter", items[0].unit)
        assertFalse(items[0].isPurchased)

        // Toggle purchased
        repository.togglePurchased(itemId, true)
        val purchasedItems = repository.getItems(listId, SortOrder.DEFAULT).first()
        assertTrue(purchasedItems[0].isPurchased)

        // Update item details
        repository.updateItem(itemId, "Whole Milk", 3.0, "liter")
        val updatedItems = repository.getItems(listId, SortOrder.DEFAULT).first()
        assertEquals("Whole Milk", updatedItems[0].name)
        assertEquals(3.0, updatedItems[0].quantity, 0.001)

        // Delete item
        repository.deleteItem(itemId)
        val afterDelete = repository.getItems(listId, SortOrder.DEFAULT).first()
        assertTrue(afterDelete.isEmpty())
    }

    @Test
    fun `autocomplete suggestions across lists`() = runBlocking {
        val list1 = repository.createList("List 1")
        val list2 = repository.createList("List 2")

        repository.addItem(list1, "Organic Avocados", 3.0, "pcs")
        repository.addItem(list2, "Avocado Oil", 1.0, "bottle")

        val suggestions = repository.searchSuggestions("Avo").first()
        assertTrue(suggestions.any { it.contains("Avocado", ignoreCase = true) })
    }

    @Test
    fun `sorting items A to Z and Z to A`() = runBlocking {
        val listId = repository.createList("Party Snacks")
        repository.addItem(listId, "Oranges", 1.0, "kg")
        repository.addItem(listId, "Apples", 2.0, "kg")
        repository.addItem(listId, "Bananas", 1.5, "kg")

        val ascItems = repository.getItems(listId, SortOrder.ALPHABETICAL_ASC).first()
        assertEquals("Apples", ascItems[0].name)
        assertEquals("Bananas", ascItems[1].name)
        assertEquals("Oranges", ascItems[2].name)

        val descItems = repository.getItems(listId, SortOrder.ALPHABETICAL_DESC).first()
        assertEquals("Oranges", descItems[0].name)
        assertEquals("Bananas", descItems[1].name)
        assertEquals("Apples", descItems[2].name)
    }
}
