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

@ExperimentalKleaverApi
public interface Struct

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