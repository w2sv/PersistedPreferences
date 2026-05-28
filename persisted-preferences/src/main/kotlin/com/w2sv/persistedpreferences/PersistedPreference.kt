@file:Suppress("unused")

package com.w2sv.persistedpreferences

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * A persisted preference value that can be observed through [flow], converted to a [StateFlow], and updated through [save].
 */
class PersistedPreference<T>(
    /**
     * Emits the current preference value and later updates.
     */
    val flow: Flow<T>,

    /**
     * Persists a new preference value.
     */
    val save: suspend (T) -> Unit,

    private val default: () -> T
) {
    /**
     * Converts [flow] into a [StateFlow] using this preference's configured default as the initial value.
     */
    fun stateIn(scope: CoroutineScope, started: SharingStarted = SharingStarted.WhileSubscribed()): StateFlow<T> =
        flow.stateIn(
            scope = scope,
            started = started,
            initialValue = default()
        )
}
