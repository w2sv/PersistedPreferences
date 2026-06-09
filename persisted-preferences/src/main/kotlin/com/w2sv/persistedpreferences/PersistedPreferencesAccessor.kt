@file:Suppress("unused")

package com.w2sv.persistedpreferences

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.w2sv.kotlinutils.makeIf
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Creates typed [PersistedPreference] instances backed by a [DataStore] of [Preferences].
 *
 * Use it directly as an injected dependency, or subclass it from a repository implementation that wants to expose
 * concrete preference properties.
 */
open class PersistedPreferencesAccessor(private val dataStore: DataStore<Preferences>, private val log: (() -> String) -> Unit = {}) {

    // ============
    // Primitives
    // ============

    /**
     * Creates a persisted preference for values that can be stored directly in Preferences DataStore.
     */
    fun <T> persistedPreference(key: Preferences.Key<T>, default: () -> T): PersistedPreference<T> =
        PersistedPreference(
            flow = flow(key, default),
            save = { save(key, it) },
            default = default,
            saveTo = { preferences, value -> preferences.save(key, value) }
        )

    /**
     * Creates a persisted preference for nullable values using [nullSavable] as the stored null representation.
     *
     * [nullSavable] must not be used as a regular saved value, because it will be read as null.
     */
    fun <T> nullablePreference(
        key: Preferences.Key<T>,
        default: () -> T?,
        nullSavable: T
    ): PersistedPreference<T?> =
        PersistedPreference(
            flow = preferenceFlow { preferences ->
                when (val saved = preferences[key]) {
                    null -> default()
                    nullSavable -> null
                    else -> saved
                }
            },
            save = { save(key, it ?: nullSavable) },
            default = default,
            saveTo = { preferences, value -> preferences.save(key, value ?: nullSavable) }
        )

    // ============
    // Enums
    // ============

    /**
     * Creates a persisted enum preference using [savePolicy].
     */
    inline fun <reified E : Enum<E>, Savable> enumPreference(
        key: Preferences.Key<Savable>,
        noinline default: () -> E,
        savePolicy: EnumSavePolicy<E, Savable>
    ): PersistedPreference<E> =
        persistedPreference(
            key = key,
            default = default,
            toSavable = savePolicy.toSavable,
            toExternal = { savePolicy.toExternal(it, default) ?: default() }
        )

    /**
     * Creates a persisted enum preference using [savePolicy].
     */
    inline fun <reified E : Enum<E>, Savable> enumPreference(
        keyName: String,
        noinline default: () -> E,
        savePolicy: EnumSavePolicy<E, Savable>
    ): PersistedPreference<E> =
        enumPreference(
            key = savePolicy.keyFactory(keyName),
            default = default,
            savePolicy = savePolicy
        )

    /**
     * Creates a nullable persisted enum preference using [savePolicy].
     */
    fun <E : Enum<E>, Savable> nullableEnumPreference(
        key: Preferences.Key<Savable>,
        default: () -> E?,
        savePolicy: EnumSavePolicy<E, Savable>
    ): PersistedPreference<E?> =
        PersistedPreference(
            flow = preferenceFlow { preferences ->
                when (val saved = preferences[key]) {
                    null -> default()
                    savePolicy.nullSavable -> null
                    else -> savePolicy.toExternal(saved, default)
                }
            },
            save = {
                if (it == null) {
                    dataStore.edit { preferences ->
                        val nullSavable = savePolicy.nullSavable
                        if (nullSavable == null) {
                            preferences.remove(key)
                        } else {
                            preferences.save(key, nullSavable)
                        }
                    }
                } else {
                    save(key, savePolicy.toSavable(it))
                }
            },
            default = default,
            saveTo = { preferences, value ->
                if (value == null && savePolicy.nullSavable == null) {
                    preferences.remove(key)
                } else {
                    preferences.save(key, value?.let(savePolicy.toSavable) ?: savePolicy.nullSavable!!)
                }
            }
        )

    /**
     * Creates a nullable persisted enum preference using [savePolicy].
     */
    fun <E : Enum<E>, Savable> nullableEnumPreference(
        keyName: String,
        default: () -> E?,
        savePolicy: EnumSavePolicy<E, Savable>
    ): PersistedPreference<E?> =
        nullableEnumPreference(
            key = savePolicy.keyFactory(keyName),
            default = default,
            savePolicy = savePolicy
        )

    // ============
    // Uri
    // ============

    private fun uriFlow(preferencesKey: Preferences.Key<String>, defaultValue: () -> Uri?): Flow<Uri?> =
        preferenceFlow {
            it[preferencesKey]
                ?.let { string -> makeIf(string != DEFAULT_STRING) { Uri.parse(string) } }
                ?: defaultValue()
        }

    private suspend fun <T> saveAsString(preferencesKey: Preferences.Key<String>, value: T?) {
        dataStore.edit { it.save(preferencesKey, value?.toString() ?: DEFAULT_STRING) }
    }

    /**
     * Creates a persisted [Uri] preference stored as its string representation.
     */
    fun uriPreference(key: Preferences.Key<String>, default: () -> Uri?): PersistedPreference<Uri?> =
        PersistedPreference(
            flow = uriFlow(key, default),
            save = { saveAsString(key, it) },
            default = default,
            saveTo = { preferences, value -> preferences.save(key, value?.toString() ?: DEFAULT_STRING) }
        )

    /**
     * Creates a persisted [Uri] preference stored as its string representation.
     */
    fun uriPreference(keyName: String, default: () -> Uri?): PersistedPreference<Uri?> =
        uriPreference(key = stringPreferencesKey(keyName), default = default)

    // ================
    // LocalDateTime
    // ================

    @RequiresApi(Build.VERSION_CODES.O)
    private fun localDateTimeFlow(preferencesKey: Preferences.Key<String>, defaultValue: () -> LocalDateTime?): Flow<LocalDateTime?> =
        preferenceFlow {
            it[preferencesKey]
                ?.let { string -> makeIf(string != DEFAULT_STRING) { LocalDateTime.parse(string) } }
                ?: defaultValue()
        }

    /**
     * Creates a persisted [LocalDateTime] preference stored as its string representation.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun localDateTimePreference(key: Preferences.Key<String>, default: () -> LocalDateTime?): PersistedPreference<LocalDateTime?> =
        PersistedPreference(
            flow = localDateTimeFlow(key, default),
            save = { saveAsString(key, it) },
            default = default,
            saveTo = { preferences, value -> preferences.save(key, value?.toString() ?: DEFAULT_STRING) }
        )

    /**
     * Creates a persisted [LocalDateTime] preference stored as its string representation.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun localDateTimePreference(keyName: String, default: () -> LocalDateTime?): PersistedPreference<LocalDateTime?> =
        localDateTimePreference(key = stringPreferencesKey(keyName), default = default)

    // =====
    // Lists
    // =====

    /**
     * Creates a persisted list preference using explicit list serialization functions.
     */
    fun <T> listPreference(
        key: Preferences.Key<String>,
        default: () -> List<T>,
        serialize: (List<T>) -> String,
        deserialize: (String) -> List<T>
    ): PersistedPreference<List<T>> =
        persistedPreference(
            key = key,
            default = default,
            toSavable = serialize,
            toExternal = deserialize
        )

    /**
     * Creates a persisted list preference using explicit list serialization functions.
     */
    fun <T> listPreference(
        keyName: String,
        default: () -> List<T>,
        serialize: (List<T>) -> String,
        deserialize: (String) -> List<T>
    ): PersistedPreference<List<T>> =
        listPreference(
            key = stringPreferencesKey(keyName),
            default = default,
            serialize = serialize,
            deserialize = deserialize
        )

    /**
     * Creates a persisted list preference by joining serialized elements with [separator].
     */
    fun <T> listPreference(
        key: Preferences.Key<String>,
        default: () -> List<T>,
        separator: String = ",",
        serializeElement: (T) -> String,
        deserializeElement: (String) -> T
    ): PersistedPreference<List<T>> =
        persistedPreference(
            key = key,
            default = default,
            toSavable = { it.joinToString(separator = separator, transform = serializeElement) },
            toExternal = { string ->
                if (string.isEmpty()) {
                    emptyList()
                } else {
                    string.split(separator).map(deserializeElement)
                }
            }
        )

    /**
     * Creates a persisted list preference by joining serialized elements with [separator].
     */
    fun <T> listPreference(
        keyName: String,
        default: () -> List<T>,
        separator: String = ",",
        serializeElement: (T) -> String,
        deserializeElement: (String) -> T
    ): PersistedPreference<List<T>> =
        listPreference(
            key = stringPreferencesKey(keyName),
            default = default,
            separator = separator,
            serializeElement = serializeElement,
            deserializeElement = deserializeElement
        )

    /**
     * Creates a persisted preference by mapping between an external value and a Preferences-storable value.
     */
    fun <External, Savable> persistedPreference(
        key: Preferences.Key<Savable>,
        default: () -> External,
        toSavable: (External) -> Savable,
        toExternal: (Savable) -> External
    ): PersistedPreference<External> =
        PersistedPreference(
            flow = flow(key) { toSavable(default()) }.map(toExternal),
            save = { save(key, toSavable(it)) },
            default = default,
            saveTo = { preferences, value -> preferences.save(key, toSavable(value)) }
        )

    /**
     * Atomically persists all typed preference updates staged in [block].
     */
    suspend fun edit(block: PersistedPreferencesEditor.() -> Unit) {
        dataStore.edit { preferences ->
            PersistedPreferencesEditor(preferences).block()
        }
    }

    // ================
    // Internal Helpers
    // ================

    @PublishedApi
    internal fun <R> preferenceFlow(transform: suspend (Preferences) -> R): Flow<R> =
        dataStore.data.map { transform(it) }.distinctUntilChanged()

    private fun <T> flow(preferencesKey: Preferences.Key<T>, defaultValue: () -> T): Flow<T> =
        preferenceFlow { it[preferencesKey] ?: defaultValue() }

    private suspend fun <T> save(preferencesKey: Preferences.Key<T>, value: T) {
        dataStore.edit { it.save(preferencesKey, value) }
    }

    private fun <T> MutablePreferences.save(preferencesKey: Preferences.Key<T>, value: T) {
        this[preferencesKey] = value
        log { "Saved ${preferencesKey.name}=$value" }
    }
}

private const val DEFAULT_STRING = ""
