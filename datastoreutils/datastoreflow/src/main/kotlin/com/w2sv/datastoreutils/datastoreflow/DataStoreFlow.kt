@file:Suppress("unused")

package com.w2sv.datastoreutils.datastoreflow

import com.w2sv.kotlinutils.coroutines.flow.firstBlocking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted

/**
 * [Flow] holding a [default] value for conversion to a [DataStoreStateFlow], and exposing a [save] method.
 */
class DataStoreFlow<V>(flow: Flow<V>, val default: () -> V, val save: suspend (V) -> Unit) : Flow<V> by flow {

    fun stateIn(
        scope: CoroutineScope,
        started: SharingStarted,
        default: () -> V = this.default
    ): DataStoreStateFlow<V> =
        DataStoreStateFlow(
            flow = this,
            default = default(),
            scope = scope,
            started = started,
            save = save
        )

    fun stateInWithSynchronousInitial(scope: CoroutineScope): DataStoreStateFlow<V> =
        DataStoreStateFlow(
            flow = this,
            default = firstBlocking(),
            scope = scope,
            started = SharingStarted.Eagerly,
            save = save
        )
}
