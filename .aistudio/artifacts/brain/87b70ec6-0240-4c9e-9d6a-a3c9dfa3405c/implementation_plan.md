# FreshCart: Smart Shopping List Manager

A production-ready Android application built with Jetpack Compose and Room Database for offline-first local persistence, designed to streamline shopping list creation, cross-list autocomplete suggestions, and quick in-store item checking.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following key decisions were confirmed through interactive clarification and are locked into this plan:
> - **Organization**: Flat shopping list with quick status filter chips (All, Unchecked, Purchased) and instantaneous local search.
> - **Visual Theme**: Fresh Market Green palette with crisp surfaces, warm organic tones, and polished Material 3 component cards.
> - **Item Quantities**: Structured numerical quantity paired with common or customizable measurement units (e.g., pcs, kg, g, pack, box, liter).

---

## 1. Overview & Core Concept

FreshCart is designed for everyday shoppers, households, and meal planners who need a dependable, distraction-free shopping assistant on their phone.

- **Primary Goal**: Make creating and completing shopping lists fast and effortless, especially when walking grocery aisles one-handed.
- **Key Value**: 
  - Rapid list creation and list switching.
  - Zero-latency autocomplete derived from all historical purchases across any shopping list.
  - Quick checked/purchased toggling with strike-through visual feedback and progress indicators.
  - Smooth A-to-Z / Z-to-A alphabetical sorting to organize shopping trips logically.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Dashboard & List Selection**:
   - The user opens the app to see an overview of all active shopping lists (e.g., "Weekly Groceries", "Hardware Store", "Party BBQ").
   - Each list card presents completion stats (e.g., "4/12 items checked"), a progress indicator ring, and action menus (rename, delete).
   - A floating action button (FAB) allows immediate creation of a new list via an intuitive dialog.

2. **List Detail & Item Management**:
   - Tapping a list enters the list detail view with the list title, quick progress bar, search bar, and filter chips.
   - The user adds items via an "Add Item" bar or modal featuring:
     - Real-time autocomplete suggestions popup based on previously entered product names across the app.
     - Quantity stepper and unit selector dropdown (pcs, kg, pack, bottle, can, etc.).
   - Tapping an item's checkbox immediately marks it as purchased with tactile haptic feedback and strikethrough animation.
   - Long-press or edit button opens an inline or sheet editor to modify item name, quantity, or unit.
   - Swipe-to-delete or explicit delete action allows quick cleanup.

3. **Sorting & Filtering**:
   - The top action bar provides a Sort menu: "Name (A to Z)" and "Name (Z to A)", along with filter chips for "All", "Pending", and "Purchased".

### Visual Identity & Theme

- **Aesthetic Direction**: Modern fresh grocery market—clean whites, rich forest greens, soft sage accents, and warm cream cards.
- **Color Palette**:
  - `Primary`: Deep Forest Green (`#1E5E3A`)
  - `OnPrimary`: Pure White (`#FFFFFF`)
  - `PrimaryContainer`: Mint Sage (`#D1E7DD`)
  - `Secondary`: Citrus Amber (`#D97724`)
  - `Surface`: Warm Ivory Neutral (`#FBFBF9`)
  - `SurfaceVariant`: Soft Tint Slate (`#EEF2EE`)
  - `CardContainer`: Crisp elevated white (`#FFFFFF`)
- **Typography**: Clean Android system typography with bold, legible headlines and distinct numerical emphasis for item quantities.
- **Interactive Polish**:
  - Subtle completion strike-through transitions.
  - Clear empty states with playful illustrations for empty lists.
  - 48dp+ minimum interactive touch targets for one-handed aisle checking.

---

## 3. Key Product Decisions & Trade-Offs

- **Room SQLite vs. In-Memory / Preferences**:
  - *Chosen Approach*: Room Database with reactive Kotlin `Flow`.
  - *Why*: Room provides ACID transactions, robust foreign-key relationships between lists and items, efficient autocomplete indexing (`SELECT DISTINCT name`), and zero loss on app restarts.
- **Cross-List Autocomplete Engine**:
  - *Chosen Approach*: Querying distinct product names directly from the `shopping_items` table and a dedicated product catalog history table.
  - *Why*: Instant suggestions without needing network requests; learns naturally from the user's shopping habits over time.
- **Navigation Architecture**:
  - *Chosen Approach*: Simple sealed screen state routing (`Screen.ListOverview`, `Screen.ListDetail(listId)`) with proper Android `BackHandler` integration.
  - *Why*: Clean, robust, lightweight, and avoids complex external routing overhead while maintaining fluid back navigation and edge-to-edge support.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────┐
│                      UI Layer                          │
│                                                        │
│  ┌─────────────────────────┐  ┌─────────────────────┐  │
│  │    ListOverviewScreen   │  │   ListDetailScreen  │  │
│  │   (Card Grid, Add List) │  │ (Items, Sort, Autoc)│  │
│  └────────────┬────────────┘  └──────────┬──────────┘  │
│               │                          │             │
│               └────────────┬─────────────┘             │
│                            │                           │
│                 ┌──────────▼──────────┐                │
│                 │ ShoppingViewModel   │                │
│                 │ (StateFlow & MVI)   │                │
│                 └──────────┬──────────┘                │
└────────────────────────────┼───────────────────────────┘
                             │
┌────────────────────────────▼───────────────────────────┐
│                    Repository Layer                    │
│                                                        │
│                 ┌─────────────────────┐                │
│                 │ ShoppingRepository  │                │
│                 └──────────┬──────────┘                │
└────────────────────────────┼───────────────────────────┘
                             │
┌────────────────────────────▼───────────────────────────┐
│                     Database Layer                     │
│                                                        │
│  ┌──────────────────────┐      ┌────────────────────┐  │
│  │     ShoppingListDao  │      │   ShoppingItemDao  │  │
│  └──────────┬───────────┘      └─────────┬──────────┘  │
│             │                            │             │
│             └─────────────┬──────────────┘             │
│                           │                            │
│                 ┌─────────▼─────────┐                  │
│                 │   AppDatabase     │                  │
│                 │     (Room)        │                  │
│                 └───────────────────┘                  │
└────────────────────────────────────────────────────────┘
```

### Data Models & Entities

1. **`ShoppingListEntity`**:
   - `id: Long` (Primary key, auto-generated)
   - `title: String`
   - `createdAt: Long`
   - `updatedAt: Long`

2. **`ShoppingItemEntity`**:
   - `id: Long` (Primary key, auto-generated)
   - `listId: Long` (Foreign key to `ShoppingListEntity` with CASCADE delete)
   - `name: String`
   - `quantity: Double`
   - `unit: String` (e.g. "pcs", "kg", "pack")
   - `isPurchased: Boolean`
   - `createdAt: Long`

3. **`ProductHistoryEntity`**:
   - `id: Long` (Primary key, auto-generated)
   - `name: String` (Unique indexed product title)
   - `lastUsedAt: Long`

### State & Event Mapping

- `ShoppingUiState`: Holds list of all shopping lists, currently selected list, active items sorted by user selection (A-Z or Z-A), current filter, search query, and autocomplete matching suggestions.
- `User Actions`:
  - `CreateList(name)` / `UpdateListName(id, newName)` / `DeleteList(id)`
  - `AddItem(listId, name, quantity, unit)`
  - `UpdateItem(id, name, quantity, unit)`
  - `ToggleItemPurchased(id, isPurchased)`
  - `DeleteItem(id)`
  - `SetSortOrder(SortOrder.ASC | SortOrder.DESC)`
  - `SetFilter(Filter.ALL | Filter.UNCHECKED | Filter.PURCHASED)`
  - `SearchQueryChanged(query)` -> triggers instant autocomplete matches.
