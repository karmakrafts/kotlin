// IGNORE_FIR_DIAGNOSTICS
// RUN_PIPELINE_TILL: FIR2IR
// LANGUAGE: +MultiPlatformProjects
// ISSUE: KT-58845

// MODULE: common
// FILE: common.kt
<!EXPECT_ACTUAL_INCOMPATIBILITY{JVM}!>expect<!> fun interface F1 {
    fun foo()
}

expect <!FUN_INTERFACE_WRONG_COUNT_OF_ABSTRACT_MEMBERS!>fun<!> interface F2

<!EXPECT_ACTUAL_INCOMPATIBILITY{JVM}!>expect<!> fun interface F3 {
    fun foo()
}

<!EXPECT_ACTUAL_INCOMPATIBILITY{JVM}!>expect<!> fun interface F4 {
    fun foo()
}

<!EXPECT_ACTUAL_INCOMPATIBILITY{JVM}!>expect<!> <!FUN_INTERFACE_WRONG_COUNT_OF_ABSTRACT_MEMBERS!>fun<!> interface F5

// MODULE: jvm()()(common)
// FILE: main.kt
interface I {
    fun bar()
}

actual <!FUN_INTERFACE_WRONG_COUNT_OF_ABSTRACT_MEMBERS!>fun<!> interface <!ACTUAL_WITHOUT_EXPECT!>F1<!> {
    actual fun foo()
    fun bar()
}

actual fun interface F2 {
    fun bar()
}

actual <!FUN_INTERFACE_WRONG_COUNT_OF_ABSTRACT_MEMBERS!>fun<!> interface <!ACTUAL_WITHOUT_EXPECT!>F3<!> : I {
    actual fun foo()
}

actual <!FUN_INTERFACE_WRONG_COUNT_OF_ABSTRACT_MEMBERS!>fun<!> interface <!ACTUAL_WITHOUT_EXPECT!>F4<!> {}

actual <!FUN_INTERFACE_WRONG_COUNT_OF_ABSTRACT_MEMBERS!>fun<!> interface <!ACTUAL_WITHOUT_EXPECT!>F5<!> {}
