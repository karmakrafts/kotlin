// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS

// WITH_RUNTIME
// FILE: Baz.java

public class Baz {
    public static String baz() {
        return Util.foo() + Util.bar();
    }
}

// FILE: bar.kt

@file:JvmName("Util")
@file:JvmMultifileClass
public fun bar(): String = barx()

private fun barx(): String = "K"

// FILE: foo.kt

@file:JvmName("Util")
@file:JvmMultifileClass
public fun foo(): String = foox()

private fun foox(): String = "O"

// FILE: test.kt

fun box(): String = Baz.baz()
