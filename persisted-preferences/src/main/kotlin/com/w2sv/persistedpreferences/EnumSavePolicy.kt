@file:Suppress("unused")

package com.w2sv.persistedpreferences

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.w2sv.kotlinutils.enumEntryByOrdinal

/**
 * Converts enum entries to and from Int or String values that can be stored by Preferences DataStore.
 */
class EnumSavePolicy<E : Enum<E>, Savable> private constructor(
    /**
     * Converts an enum entry into its stored representation.
     */
    val toSavable: (E) -> Savable,

    /**
     * Converts a stored representation back to an enum entry, falling back to default for unknown values.
     */
    val toExternal: (Savable, default: () -> E?) -> E?,

    /**
     * Stored representation of an explicitly saved null value. Leave as null if the PersistedPreference should return the default value
     * when the persisted value is null (as is the case when no value has saved to the key yet).
     */
    val nullSavable: Savable? = null,

    /**
     * Creates the Preferences DataStore key used to store this policy's [Savable] representation.
     */
    @PublishedApi
    internal val keyFactory: (String) -> Preferences.Key<Savable>
) {
    companion object {
        /**
         * Stores enum entries by their ordinal.
         */
        inline fun <reified E : Enum<E>> byOrdinal(): EnumSavePolicy<E, Int> =
            byInt(
                toSavable = { it.ordinal },
                toExternal = { ordinal, default ->
                    if (ordinal == -1) {
                        null
                    } else {
                        try {
                            enumEntryByOrdinal<E>(ordinal)
                        } catch (e: IndexOutOfBoundsException) {
                            default()
                        }
                    }
                },
                nullSavable = -1
            )

        /**
         * Stores enum entries by their name.
         */
        inline fun <reified E : Enum<E>> byName(): EnumSavePolicy<E, String> =
            byString(
                toSavable = { it.name },
                toExternal = { name, default ->
                    if (name.isEmpty()) {
                        null
                    } else {
                        enumValues<E>().firstOrNull { it.name == name } ?: default()
                    }
                },
                nullSavable = ""
            )

        /**
         * Stores enum entries as custom Int values.
         */
        fun <E : Enum<E>> byInt(
            toSavable: (E) -> Int,
            toExternal: (Int, default: () -> E?) -> E?,
            nullSavable: Int? = null
        ): EnumSavePolicy<E, Int> =
            EnumSavePolicy(
                toSavable = toSavable,
                toExternal = toExternal,
                nullSavable = nullSavable,
                keyFactory = ::intPreferencesKey
            )

        /**
         * Stores enum entries as custom String values.
         */
        fun <E : Enum<E>> byString(
            toSavable: (E) -> String,
            toExternal: (String, default: () -> E?) -> E?,
            nullSavable: String? = null
        ): EnumSavePolicy<E, String> =
            EnumSavePolicy(
                toSavable = toSavable,
                toExternal = toExternal,
                nullSavable = nullSavable,
                keyFactory = ::stringPreferencesKey
            )
    }
}
