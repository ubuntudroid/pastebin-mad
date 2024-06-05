package omg.lol.pastebin.core.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * Alternative [Flow.onStart()][kotlinx.coroutines.flow.onStart] implementation which guarantees that emissions from [action] are collected
 * even if a downstream flow is a SharedFlow.
 *
 * From [Flow.onStart() docs][kotlinx.coroutines.flow.onStart]:
 *
 * > The [action] is called before the upstream flow is started, so if it is used with a [SharedFlow]
 * there is **no guarantee** that emissions from the upstream flow that happen inside or immediately
 * after this `onStart` action will be collected.
 *
 * Returns a flow that invokes the given [action] **before** this flow starts to be collected.
 *
 * The receiver of the [action] is [FlowCollector], so `onStart` can emit additional elements.
 * For example:
 *
 * ```
 * flowOf("a", "b", "c")
 *     .sharedFlowSafeOnStart { emit("Begin") }
 *     .collect { println(it) } // prints Begin, a, b, c
 * ```
 *
 * @param action the action to invoke before the upstream flow is started to be collected
 *
 * @see [kotlinx.coroutines.flow.onStart]
 */
fun <T> Flow<T>.sharedFlowSafeOnStart(action: suspend FlowCollector<T>.() -> Unit): Flow<T> = flow {
    action()
    emitAll(this@sharedFlowSafeOnStart)
}
