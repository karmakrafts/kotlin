// RUN_PIPELINE_TILL: BACKEND
// ISSUE: KT-54483, KT-75169
// WITH_STDLIB

abstract class Cache {
    fun get(): Int = 10
    fun get2(): Int = 10
    fun <T> get3(): Int = 10
    fun <T : CharSequence> get4(): Int = 10
}

inline fun <reified T> Cache.<!EXTENSION_SHADOWED_BY_MEMBER!>get<!>() = 10
fun <T> Cache.<!EXTENSION_SHADOWED_BY_MEMBER!>get2<!>() = 10
fun <T, R> Cache.<!EXTENSION_SHADOWED_BY_MEMBER!>get3<!>() = 10
fun <T : List<String>> Cache.<!EXTENSION_SHADOWED_BY_MEMBER!>get4<!>() = 10
