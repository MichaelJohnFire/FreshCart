Shopping List App for Android
Application built with Kotlin and Jetpack Compose, designed to make grocery shopping and list organization fast, intuitive, and customizable.

Features:
List Management: Create, name, and edit multiple shopping lists effortlessly.
Item Tracking: Add items with names and quantities, edit them at any time, mark items as purchased, or delete them.
Smart Autocomplete: The app remembers previously entered product names across all lists and suggests them as you type, speeding up future list creation.
Sorting Options: Sort your product lists alphabetically (A to Z and Z to A).
Dynamic Theming: Switch seamlessly between Light and Dark themes (with support for Material 3 design).
Localization: Full multi-language support with built-in translations for English and Polish.
Persistent Preferences: Settings such as theme and language choices are securely saved using Android Jetpack DataStore.

Tech Stack & Architecture
Language: Kotlin
UI Framework: Jetpack Compose & Material 3
Architecture: MVVM (Model-View-ViewModel) with Clean Architecture principles
Local Persistence:
  Room Database (for shopping lists and items)
  Jetpack DataStore (for user preferences)
State Management: Kotlin Coroutines & StateFlow

Getting Started
Clone the repository:
git clone https://github.com/your-username/shopping-list-app.git
Open the project in Android Studio (Ladybug or newer recommended).
Sync project with Gradle files.
Run the app on an emulator or a physical Android device.

Localization
The app currently supports:
🇺🇸 English (Default)
🇵🇱 Polish (Polski)

License
This project is open-source and available under the MIT License.
