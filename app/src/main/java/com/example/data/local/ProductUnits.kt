package com.example.data.local

import java.util.Locale

object ProductUnits {
    /**
     * Polish grocery measurement units:
     * - g (grams)
     * - kg (kilograms)
     * - dag (decagrams)
     * - litry (liters)
     * - szt (pieces / items)
     * - opk (packages)
     */
    val POLISH_UNITS = listOf("g", "kg", "dag", "litry", "szt", "opk")

    /**
     * Default measurement units for English and other locales.
     */
    val DEFAULT_UNITS = listOf("pcs", "kg", "g", "liter", "pack", "bottle", "can", "box")

    /**
     * Determines whether the given language code or system locale corresponds to Polish.
     */
    fun isPolishLanguage(languageCode: String, systemLocale: Locale = Locale.getDefault()): Boolean {
        return when {
            languageCode.equals("pl", ignoreCase = true) -> true
            languageCode.equals("system", ignoreCase = true) && systemLocale.language.equals("pl", ignoreCase = true) -> true
            else -> false
        }
    }

    /**
     * Returns the list of measurement units tailored to the selected language.
     */
    fun getAvailableUnits(languageCode: String, systemLocale: Locale = Locale.getDefault()): List<String> {
        return if (isPolishLanguage(languageCode, systemLocale)) {
            POLISH_UNITS
        } else {
            DEFAULT_UNITS
        }
    }

    /**
     * Returns the recommended default unit for a new item based on language.
     */
    fun getDefaultUnit(languageCode: String, systemLocale: Locale = Locale.getDefault()): String {
        return if (isPolishLanguage(languageCode, systemLocale)) {
            "szt"
        } else {
            "pcs"
        }
    }

    /**
     * Converts or normalizes a unit string for display according to the active language.
     */
    fun localizeUnitForDisplay(unit: String, isPolish: Boolean): String {
        val trimmed = unit.trim()
        if (isPolish) {
            return when (trimmed.lowercase()) {
                "pcs", "pc", "piece", "pieces", "item", "items", "szt", "szt." -> "szt"
                "liter", "liters", "litre", "litres", "l", "litry" -> "litry"
                "pack", "package", "packages", "pkg", "opk", "opak." -> "opk"
                "g", "gram", "grams" -> "g"
                "kg", "kilogram", "kilograms" -> "kg"
                "dag", "dkg", "decagram", "decagrams" -> "dag"
                "bottle" -> "but."
                "can" -> "puszka"
                "box" -> "karton"
                else -> trimmed
            }
        } else {
            return when (trimmed.lowercase()) {
                "szt", "szt." -> "pcs"
                "litry", "litr" -> "liter"
                "opk", "opak." -> "pack"
                else -> trimmed
            }
        }
    }
}
