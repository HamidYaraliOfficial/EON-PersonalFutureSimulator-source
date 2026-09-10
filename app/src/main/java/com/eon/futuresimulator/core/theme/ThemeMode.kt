package com.eon.futuresimulator.core.theme

/** User-selectable appearance. Persisted via UserPreferencesRepository / DataStore. */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    AMOLED,
    RED,
    BLUE;

    companion object {
        fun fromStorageKey(key: String?): ThemeMode =
            entries.firstOrNull { it.name == key } ?: SYSTEM
    }
}
