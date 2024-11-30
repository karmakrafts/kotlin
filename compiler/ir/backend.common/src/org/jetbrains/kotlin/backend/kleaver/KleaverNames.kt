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
    val structAnnotation: FqName = ClassId(packageName, Name.identifier("Struct")).asSingleFqName()
    val unionAnnotation: FqName = ClassId(packageName, Name.identifier("Union")).asSingleFqName()
    val assemblyAnnotation: FqName = ClassId(packageName, Name.identifier("Assembly")).asSingleFqName()
}