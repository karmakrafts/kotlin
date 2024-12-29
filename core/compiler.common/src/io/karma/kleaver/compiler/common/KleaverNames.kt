package io.karma.kleaver.compiler.common

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * @author Alexander Hinze
 * @since 22/12/2024
 */
object KleaverNames {
    val compilerPackage: FqName = FqName("io.karma.kleaver.compiler")
    val forceInline: ClassId = ClassId(compilerPackage, Name.identifier("ForceInline"))

    val runtimePackage: FqName = FqName("io.karma.kleaver.runtime")
    val struct: ClassId = ClassId(runtimePackage, Name.identifier("Struct"))
}