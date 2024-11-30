package io.karma.kleaver.runtime

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.native.internal.IntrinsicType
import kotlin.native.internal.TypedIntrinsic

/**
 * @author Alexander Hinze
 * @since 30/11/2024
 */

@ExperimentalKleaverApi
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.VALUE_PARAMETER)
public annotation class ByRef

@ExperimentalKleaverApi
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
public annotation class AlignAs(val alignment: Int)

@ExperimentalKleaverApi
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
public annotation class FieldOffset(val offset: Int)

@ExperimentalKleaverApi
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
public annotation class BitField(val bits: Int)

/**
 * Declares the class which implements this interface as a value type.
 * Value types are not affected by the overhead of the runtime like
 * regular Kotlin objects, since they are flattened at compile time
 * and may be allocated on the native stack.
 *
 * **Draft for KT-73241** Add better support for value types / structures in Kotlin Native
 */
@ExperimentalKleaverApi
public interface Struct

/**
 * A type of structure where the offset of all its member fields
 * into the backing memory of the structure is zero, effectively
 * overlaying its fields in memory.
 */
@ExperimentalKleaverApi
public interface Union : Struct

@OptIn(ExperimentalForeignApi::class)
@ExperimentalKleaverApi
@TypedIntrinsic(IntrinsicType.KLEAVER_ADDRESS_OF)
public external fun addressOf(@ByRef ref: Struct?): COpaquePointer?

@DelicateMemoryApi
@ExperimentalKleaverApi
@TypedIntrinsic(IntrinsicType.KLEAVER_ALIGN_OF)
public external fun <T : Struct> alignOf(): Int

@DelicateMemoryApi
@ExperimentalKleaverApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SIZE_OF)
public external fun <T : Struct> sizeOf(): Long