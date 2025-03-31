/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.arguments.dsl.base

import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.arguments.dsl.types.AllKotlinArgumentTypes
import org.jetbrains.kotlin.arguments.serialization.json.AllDetailsKotlinReleaseVersionSerializer

@Serializable
data class KotlinCompilerArguments(
    val schemaVersion: Int = 1,
    @Serializable(with = AllDetailsKotlinReleaseVersionSerializer::class)
    val releases: Set<KotlinReleaseVersion> = KotlinReleaseVersion.entries.toSet(),
    val types: AllKotlinArgumentTypes = AllKotlinArgumentTypes(),
    val topLevel: KotlinCompilerArgumentsLevel,
)

@KotlinArgumentsDslMarker
internal class KotlinCompilerArgumentsBuilder() {
    private lateinit var topLevel: KotlinCompilerArgumentsLevel

    fun topLevel(
        name: String,
        mergeWith: Set<KotlinCompilerArgumentsLevel> = emptySet(),
        config: KotlinCompilerArgumentsLevelBuilder.() -> Unit
    ) {
        val levelBuilder = KotlinCompilerArgumentsLevelBuilder(name)
        config(levelBuilder)
        topLevel = mergeWith.fold(levelBuilder.build()) { init, level -> init.mergeWith(level) }
    }

    fun build(): KotlinCompilerArguments = KotlinCompilerArguments(
        topLevel = topLevel
    )
}

@KotlinArgumentsDslMarker
internal fun compilerArguments(
    config: KotlinCompilerArgumentsBuilder.() -> Unit,
): KotlinCompilerArguments {
    val kotlinArguments = KotlinCompilerArgumentsBuilder()
    config(kotlinArguments)
    return kotlinArguments.build()
}
