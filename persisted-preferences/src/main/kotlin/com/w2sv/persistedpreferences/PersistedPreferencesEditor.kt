package com.w2sv.persistedpreferences

import androidx.datastore.preferences.core.MutablePreferences

/**
 * Collects typed preference updates inside a single Preferences DataStore transaction.
 */
class PersistedPreferencesEditor internal constructor(private val preferences: MutablePreferences) {

    /**
     * Stages [value] for this preference in the current transaction.
     */
    infix fun <T> PersistedPreference<T>.setTo(value: T) {
        saveTo(preferences, value)
    }
}
