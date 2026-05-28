@file:Suppress("unused")

package com.w2sv.datastoreutils.preferences

import kotlinx.coroutines.flow.Flow

/**
 * A persisted preference value that can be observed through [flow] and updated through [save].
 */
class PersistedPreference<T>(
    /**
     * Emits the current preference value and later updates.
     */
    val flow: Flow<T>,

    /**
     * Persists a new preference value.
     */
    val save: suspend (T) -> Unit
)
