// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
class A {
    companion object B {
        class <!REDECLARATION!>G<!>
        val <!REDECLARATION!>G<!> = 1
    }
}