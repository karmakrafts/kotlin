// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS

fun test(cl: Int.() -> Int):Int = 11.cl()

class Foo {
    val a = test { this }
}

fun box(): String {
    if (Foo().a != 11) return "fail"

    return "OK"
}