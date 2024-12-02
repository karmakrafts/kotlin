/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.karma.kleaver.compiler.backend

/**
 * @author Alexander Hinze
 * @since 30/11/2024
 */
internal object KleaverLog {
    inline fun info(crossinline message: () -> String = { "" }) {
        println("\uD83D\uDD2A ${message()}")
    }

    inline fun error(crossinline message: () -> String = { "" }) {
        System.err.println("\uD83D\uDD2A ${message()}")
    }
}