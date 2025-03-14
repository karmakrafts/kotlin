/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.js.backend.ast

import org.jetbrains.kotlin.js.common.RESERVED_KEYWORDS
import kotlin.collections.ArrayList

class JsObjectScope(parent: JsScope, description: String) : JsScope(parent, description)

object JsDynamicScope : JsScope(null, "Scope for dynamic declarations") {
    override fun doCreateName(name: String) = JsName(name, false)
}

open class JsFunctionScope(parent: JsScope, description: String) : JsDeclarationScope(parent, description) {
    override fun hasOwnName(name: String): Boolean = RESERVED_WORDS.contains(name) || super.hasOwnName(name)

    open fun declareNameUnsafe(identifier: String): JsName = super.declareName(identifier)
}

open class JsDeclarationScope(parent: JsScope, description: String, useParentScopeStack: Boolean = false) : JsScope(parent, description) {
    private var labelScopesImpl: ArrayList<LabelScope>? =
        if (parent is JsDeclarationScope && useParentScopeStack) parent.labelScopesImpl else null

    private val topLabelScope
        get() = labelScopesImpl?.let { if (it.isNotEmpty()) it.last() else null }

    private val labelScopes: ArrayList<LabelScope>
        get() = labelScopesImpl ?: ArrayList<LabelScope>().also { labelScopesImpl = it }

    open fun enterLabel(label: String, outputName: String): JsName {
        val scope = LabelScope(topLabelScope, label, outputName)
        labelScopes.add(scope)
        return scope.labelName
    }

    open fun exitLabel() {
        labelScopes.let {
            assert(it.isNotEmpty()) { "No scope to exit from" }
            it.removeLast()
        }
    }

    open fun findLabel(label: String): JsName? =
            topLabelScope?.findName(label)

    private inner class LabelScope(parent: LabelScope?, val ident: String, outputIdent: String) : JsScope(parent, "Label scope for $ident") {
        val labelName: JsName = JsName(outputIdent, true)

        override fun findOwnName(name: String): JsName? =
                if (name == ident) labelName else null

        /**
         * Safe call is necessary, because hasOwnName can be called
         * in constructor before labelName is initialized (see KT-4394)
         */
        @Suppress("UNNECESSARY_SAFE_CALL")
        override fun hasOwnName(name: String): Boolean =
                name in RESERVED_WORDS
                || name == ident
                || name == labelName?.ident
                || parent?.hasOwnName(name) ?: false
    }

    companion object {
        val RESERVED_WORDS: Set<String> = RESERVED_KEYWORDS + setOf(
                // global identifiers usually declared in a typical JS interpreter
                "NaN", "isNaN", "Infinity", "undefined",
                "Error", "Object", "Math", "String", "Number", "Boolean", "Date", "Array", "RegExp", "JSON",

                // global identifiers usually declared in know environments (node.js, browser, require.js, WebWorkers, etc)
                "require", "define", "module", "window", "self",

                // the special Kotlin object
                "Kotlin"
        )
    }
}

