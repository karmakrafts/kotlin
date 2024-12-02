package io.karma.kleaver.runtime

import io.karma.kleaver.compiler.ByRef
import io.karma.kleaver.compiler.ExperimentalKleaverApi
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.native.internal.IntrinsicType
import kotlin.native.internal.TypedIntrinsic

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
 * A type of structure where the offset of all its member fields
 * into the backing memory of the structure is zero, effectively
 * overlaying its fields in memory.
 */
@ExperimentalKleaverApi
public interface Union : Struct

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
 * of the specified structure that was calculated at compile-time.
 */
@ExperimentalKleaverApi
@TypedIntrinsic(IntrinsicType.KLEAVER_ALIGN_OF)
public external fun <T : Struct> alignOf(): Int

/**
 * This intrinsic is lowered to the byte size
 * of the specified structure that was calculated at compile-time.
 */
@ExperimentalKleaverApi
@TypedIntrinsic(IntrinsicType.KLEAVER_SIZE_OF)
public external fun <T : Struct> sizeOf(): Long