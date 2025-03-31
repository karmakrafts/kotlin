/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.compiler.test

import org.jetbrains.kotlin.codegen.forTestCompile.ForTestCompileRuntime
import org.jetbrains.kotlin.scripting.compiler.plugin.SCRIPT_TEST_BASE_COMPILER_ARGUMENTS_PROPERTY
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.K2ReplCompiler
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.K2ReplEvaluator
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.withMessageCollectorAndDisposable
import java.io.File
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.declaredMemberProperties
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.impl.internalScriptingRunSuspend
import kotlin.script.experimental.jvm.KJvmEvaluatedSnippet
import kotlin.script.experimental.jvm.updateClasspath
import kotlin.script.experimental.util.LinkedSnippet
import kotlin.test.*

class ReplReceiver1 {
    val ok = "OK"
}

@Suppress("unused") // Used in snippets
class TestReplReceiver1() { fun checkReceiver(block: ReplReceiver1.() -> Any) = block(ReplReceiver1()) }


class CustomK2ReplTest {

    @Test
    fun testSimple() {
        evalAndCheckSnippetsResultVals(
            sequenceOf(
                "val x = 3",
                "x + 4",
                "x"
            ),
            sequenceOf(
                null,
                7,
                3
            )
        )
    }

    @Test
    fun testWithImplicitReceiver() {
        evalAndCheckSnippetsWithReplReceiver1(
            sequenceOf("val x = ok", "ok", "x",),
            sequenceOf(null, "OK", "OK"),
        )
    }

    @Test
    fun testWithImplicitReceiverWithShadowing() {
        evalAndCheckSnippetsWithReplReceiver1(
            sequenceOf("val ok = 42", "ok",),
            sequenceOf(null, 42),
        )
    }

    @Test
    fun testWithImplicitReceiverIntExtension() {
        evalAndCheckSnippetsWithReplReceiver1(
            sequenceOf(
                "fun org.jetbrains.kotlin.scripting.compiler.test.ReplReceiver1.foo() = ok.length",
                "foo()",
            ),
            sequenceOf(null, 2),
        )
    }

    @Test
    fun testWithImplicitReceiverExtExtension() {
        evalAndCheckSnippetsWithReplReceiver1(
            sequenceOf(
                "val obj = org.jetbrains.kotlin.scripting.compiler.test.TestReplReceiver1()",
                "obj.checkReceiver { ok }",
            ),
            sequenceOf(null, "OK"),
        )
    }

    @Test
    fun testWithReceiverExtension() {
        evalAndCheckSnippetsResultVals(
            sequenceOf(
                "val obj = org.jetbrains.kotlin.scripting.compiler.test.TestReplReceiver1()",
                "obj.checkReceiver { ok }",
            ),
            sequenceOf(null, "OK"),
        )
    }

    @Test
    fun testWithUpdatingDefaultImports() {
        evalAndCheckSnippetsResultVals(
            sequenceOf(
                "kotlin.random.Random.nextInt(10)/10",
                "Random.nextInt(10)/10",
            ),
            sequenceOf(0, 0),
            baseCompilationConfiguration.with {
                refineConfiguration {
                    beforeCompiling { (script, config, _) ->
                        config.with {
                            if (!script.text.contains("kotlin.random.Random")) {
                                defaultImports("kotlin.random.Random")
                            }
                        }.asSuccess()
                    }
                }
            }
        )
    }

    @Test
    fun testBasicReflection() {
        evalAndCheckSnippets(
            sequenceOf(
                "var x = 3",
                "fun f() = x"
            ),
            baseCompilationConfiguration,
            baseEvaluationConfiguration,
            {
                it.onSuccess { s ->
                    s.get().result.let { r ->
                        @Suppress("UNCHECKED_CAST") val propx = r.scriptClass!!.declaredMemberProperties.first() as kotlin.reflect.KMutableProperty1<Any, Int>
                        val x = propx.get(r.scriptInstance!!)
                        assertEquals(3, x)
                        propx.set(r.scriptInstance!!, 5)
                    }
                    it
                }
            },
            {
                it.onSuccess { s ->
                    s.get().result.let { r ->
                        val funf = r.scriptClass!!.declaredMemberFunctions.first()
                        val fret = funf.call() as Int
                        assertEquals(5, fret)
                    }
                    it
                }
            }
        )
    }
}

private val baseCompilationConfiguration: ScriptCompilationConfiguration =
    ScriptCompilationConfiguration {
        val classpath = System.getProperty("kotlin.test.script.classpath")?.split(File.pathSeparator)
            ?.mapNotNull { File(it).takeIf { file -> file.exists() } }.orEmpty()
        updateClasspath(classpath + ForTestCompileRuntime.runtimeJarForTests())
        compilerOptions("-Xrender-internal-diagnostic-names=true")
    }

private val baseEvaluationConfiguration: ScriptEvaluationConfiguration = ScriptEvaluationConfiguration {}

private fun compileEvalAndCheckSnippetsSequence(
    snippets: Sequence<String>,
    compilationConfiguration: ScriptCompilationConfiguration,
    evaluationConfiguration: ScriptEvaluationConfiguration,
    expectedResultCheckers: Sequence<(ResultWithDiagnostics<LinkedSnippet<KJvmEvaluatedSnippet>>) -> ResultWithDiagnostics<LinkedSnippet<KJvmEvaluatedSnippet>>>
): ResultWithDiagnostics<List<LinkedSnippet<KJvmEvaluatedSnippet>>> =
    withMessageCollectorAndDisposable { messageCollector, disposable ->
        val compiler = K2ReplCompiler(K2ReplCompiler.createCompilationState(messageCollector, disposable, compilationConfiguration))
        val evaluator = K2ReplEvaluator()
        val filenameExtension = compilationConfiguration[ScriptCompilationConfiguration.fileExtension] ?: "repl.kts"
        var snippetNo = 1
        val checkersIterator = expectedResultCheckers.iterator()
        @Suppress("DEPRECATION_ERROR")
        internalScriptingRunSuspend {
            snippets.asIterable().mapSuccess { snippet ->
                val checker = if (checkersIterator.hasNext()) checkersIterator.next() else null
                compiler.compile(snippet.toScriptSource("s${snippetNo++}.$filenameExtension")).onSuccess {
                    evaluator.eval(it, evaluationConfiguration).let { checker?.invoke(it) ?: it }
                }
            }
        }
    }

private fun checkEvaluatedSnippetsResultVals(
    expectedResultVals: Sequence<Any?>,
    evaluationResults: ResultWithDiagnostics<List<LinkedSnippet<KJvmEvaluatedSnippet>>>
) {
    val expectedIter = expectedResultVals.iterator()
    val successResults = evaluationResults.valueOr {
        fail("Evaluation failed:\n  ${it.reports.joinToString("\n  ") { it.message }}")
        return
    }
    for (res in successResults) {
        if (!expectedIter.hasNext()) break
        val expectedVal = expectedIter.next()
        when (val resVal = res.get().result) {
            is ResultValue.Unit -> assertTrue(expectedVal == null, "Unexpected evaluation result: Unit")
            is ResultValue.Error -> fail("Unexpected evaluation result: runtime error: ${resVal.error.message}")
            is ResultValue.Value -> assertTrue(expectedVal == resVal.value, "Unexpected evaluation result: ${resVal.value}")
            is ResultValue.NotEvaluated -> fail("Unexpected evaluation result: NotEvaluated")
        }
    }
}

private fun evalAndCheckSnippetsResultVals(
    snippets: Sequence<String>,
    expectedResultVals: Sequence<Any?>,
    compilationConfiguration: ScriptCompilationConfiguration = baseCompilationConfiguration,
    evaluationConfiguration: ScriptEvaluationConfiguration = baseEvaluationConfiguration
) {
    // this is K2-only tests
    if (System.getProperty(SCRIPT_TEST_BASE_COMPILER_ARGUMENTS_PROPERTY)?.contains("-language-version 1.9") == true) return

    val evaluationResults = compileEvalAndCheckSnippetsSequence(snippets, compilationConfiguration, evaluationConfiguration, emptySequence())
    checkEvaluatedSnippetsResultVals(expectedResultVals, evaluationResults)
}

private fun evalAndCheckSnippets(
    snippets: Sequence<String>,
    compilationConfiguration: ScriptCompilationConfiguration,
    evaluationConfiguration: ScriptEvaluationConfiguration,
    vararg resultCheckers: (ResultWithDiagnostics<LinkedSnippet<KJvmEvaluatedSnippet>>) -> ResultWithDiagnostics<LinkedSnippet<KJvmEvaluatedSnippet>>
) {
    // this is K2-only tests
    if (System.getProperty(SCRIPT_TEST_BASE_COMPILER_ARGUMENTS_PROPERTY)?.contains("-language-version 1.9") == true) return

    val results =
        compileEvalAndCheckSnippetsSequence(snippets, compilationConfiguration, evaluationConfiguration, resultCheckers.asSequence())
    checkEvaluatedSnippetsResultVals(emptySequence(), results)
}

private fun evalAndCheckSnippetsWithReplReceiver1(
    snippets: Sequence<String>,
    expectedResultVals: Sequence<Any?>,
    compilationConfiguration: ScriptCompilationConfiguration = baseCompilationConfiguration,
    evaluationConfiguration: ScriptEvaluationConfiguration = baseEvaluationConfiguration
) {
    evalAndCheckSnippetsResultVals(
        snippets, expectedResultVals,
        compilationConfiguration.with {
            implicitReceivers(ReplReceiver1::class)
        },
        evaluationConfiguration.with {
            implicitReceivers(ReplReceiver1())
        }
    )

}