// KIND: STANDALONE
// MODULE: state
// FILE: state.kt

package oh.my.state

class State(val bytes: ByteArray? = null)

// MODULE: feature
// FILE: features.kt
package oh.my.kotlin

class FeatureA {}

class FeatureB

// MODULE: anotherFeature(state)
// FILE: main.kt
import oh.my.state.State

class FeatureC() {
    val state: State = State()
}

// MODULE: main(feature,anotherFeature)
// EXPORT_TO_SWIFT
// FILE: main.kt
import oh.my.kotlin.FeatureA

fun foo(): FeatureA {
    return FeatureA()
}

fun bar(): FeatureC {
    return FeatureC()
}

// MODULE: main_2(feature)
// EXPORT_TO_SWIFT
// FILE: main_2.kt
package foo

import oh.my.kotlin.FeatureB

fun foo(): FeatureB {
    return FeatureB()
}
