package org.jetbrains.kotlin.backend.kleaver

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * @author Alexander Hinze
 * @since 25/11/2024
 */
object KleaverNames {
    val packageName: FqName = FqName("kotlinx.kleaver")
    val neverThrowsAnnotation: FqName = ClassId(packageName, Name.identifier("NeverThrows")).asSingleFqName()
}