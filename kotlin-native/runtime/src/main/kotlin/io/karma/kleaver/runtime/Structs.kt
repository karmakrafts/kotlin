package io.karma.kleaver.runtime

import io.karma.kleaver.compiler.ByRef
import io.karma.kleaver.compiler.ExperimentalKleaverApi
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.native.internal.IntrinsicType
import kotlin.native.internal.TypedIntrinsic
import kotlin.reflect.KProperty1

/**
 * @author Alexander Hinze
 * @since 30/11/2024
 */

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
 * Obtains the address of the structure being passed in.
 * Can be used to pass Kleaver structures to cinterop code.
 */
@OptIn(ExperimentalForeignApi::class)
@ExperimentalKleaverApi
@TypedIntrinsic(IntrinsicType.KLEAVER_ADDRESS_OF)
public external fun addressOf(@ByRef ref: Struct?): COpaquePointer?

/**
 * This intrinsic is lowered to the memory alignment
 * of the specified structure.
 */
@ExperimentalKleaverApi
@TypedIntrinsic(IntrinsicType.KLEAVER_ALIGN_OF)
public external fun <T : Struct> alignOf(): Int

/**
 * This intrinsic is lowered to the byte size
 * of the specified structure.
 */
@ExperimentalKleaverApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SIZE_OF)
public external fun <T : Struct> sizeOf(): Long

/**
 * This intrinsic is lowered to the byte offset
 * in memory of the specified structure property.
 */
@ExperimentalKleaverApi
@TypedIntrinsic(IntrinsicType.KLEAVER_OFFSET_OF)
public external fun <T : Struct> offsetOf(selector: KProperty1<T, *>): Long