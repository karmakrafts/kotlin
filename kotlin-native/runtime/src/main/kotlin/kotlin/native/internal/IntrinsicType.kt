package kotlin.native.internal

internal class IntrinsicType {
    companion object {
        // Arithmetic
        const val PLUS = "PLUS"
        const val MINUS = "MINUS"
        const val TIMES = "TIMES"
        const val SIGNED_DIV = "SIGNED_DIV"
        const val SIGNED_REM = "SIGNED_REM"
        const val UNSIGNED_DIV = "UNSIGNED_DIV"
        const val UNSIGNED_REM = "UNSIGNED_REM"
        const val INC = "INC"
        const val DEC = "DEC"
        const val UNARY_PLUS = "UNARY_PLUS"
        const val UNARY_MINUS = "UNARY_MINUS"
        const val SHL = "SHL"
        const val SHR = "SHR"
        const val USHR = "USHR"
        const val AND = "AND"
        const val OR = "OR"
        const val XOR = "XOR"
        const val INV = "INV"
        const val SIGN_EXTEND = "SIGN_EXTEND"
        const val ZERO_EXTEND = "ZERO_EXTEND"
        const val INT_TRUNCATE = "INT_TRUNCATE"
        const val FLOAT_TRUNCATE = "FLOAT_TRUNCATE"
        const val FLOAT_EXTEND = "FLOAT_EXTEND"
        const val SIGNED_TO_FLOAT = "SIGNED_TO_FLOAT"
        const val UNSIGNED_TO_FLOAT = "UNSIGNED_TO_FLOAT"
        const val FLOAT_TO_SIGNED = "FLOAT_TO_SIGNED"
        const val SIGNED_COMPARE_TO = "SIGNED_COMPARE_TO"
        const val UNSIGNED_COMPARE_TO = "UNSIGNED_COMPARE_TO"
        const val NOT = "NOT"
        const val REINTERPRET = "REINTERPRET"
        const val EXTRACT_ELEMENT = "EXTRACT_ELEMENT"
        const val ARE_EQUAL_BY_VALUE = "ARE_EQUAL_BY_VALUE"
        const val IEEE_754_EQUALS = "IEEE_754_EQUALS"

        // ObjC related stuff
        const val OBJC_GET_MESSENGER = "OBJC_GET_MESSENGER"
        const val OBJC_GET_MESSENGER_STRET = "OBJC_GET_MESSENGER_STRET"
        const val OBJC_GET_OBJC_CLASS = "OBJC_GET_OBJC_CLASS"
        const val OBJC_CREATE_SUPER_STRUCT = "OBJC_CREATE_SUPER_STRUCT"
        const val OBJC_INIT_BY = "OBJC_INIT_BY"
        const val OBJC_GET_SELECTOR = "OBJC_GET_SELECTOR"

        // Other
        const val INTEROP_READ_BITS = "INTEROP_READ_BITS"
        const val INTEROP_WRITE_BITS = "INTEROP_WRITE_BITS"
        const val CREATE_UNINITIALIZED_INSTANCE = "CREATE_UNINITIALIZED_INSTANCE"
        const val IDENTITY = "IDENTITY"
        const val IMMUTABLE_BLOB = "IMMUTABLE_BLOB"
        const val INIT_INSTANCE = "INIT_INSTANCE"
        const val IS_SUBTYPE = "IS_SUBTYPE"
        const val IS_EXPERIMENTAL_MM = "IS_EXPERIMENTAL_MM"
        const val THE_UNIT_INSTANCE = "THE_UNIT_INSTANCE"

        // Enums
        const val ENUM_VALUES = "ENUM_VALUES"
        const val ENUM_VALUE_OF = "ENUM_VALUE_OF"
        const val ENUM_ENTRIES = "ENUM_ENTRIES"

        // Coroutines
        const val GET_CONTINUATION = "GET_CONTINUATION"
        const val RETURN_IF_SUSPENDED = "RETURN_IF_SUSPENDED"
        const val SAVE_COROUTINE_STATE = "SAVE_COROUTINE_STATE"
        const val RESTORE_COROUTINE_STATE = "RESTORE_COROUTINE_STATE"

        // Interop
        const val INTEROP_READ_PRIMITIVE = "INTEROP_READ_PRIMITIVE"
        const val INTEROP_WRITE_PRIMITIVE = "INTEROP_WRITE_PRIMITIVE"
        const val INTEROP_GET_POINTER_SIZE = "INTEROP_GET_POINTER_SIZE"
        const val INTEROP_NATIVE_PTR_TO_LONG = "INTEROP_NATIVE_PTR_TO_LONG"
        const val INTEROP_NATIVE_PTR_PLUS_LONG = "INTEROP_NATIVE_PTR_PLUS_LONG"
        const val INTEROP_GET_NATIVE_NULL_PTR = "INTEROP_GET_NATIVE_NULL_PTR"
        const val INTEROP_CONVERT = "INTEROP_CONVERT"
        const val INTEROP_BITS_TO_FLOAT = "INTEROP_BITS_TO_FLOAT"
        const val INTEROP_BITS_TO_DOUBLE = "INTEROP_BITS_TO_DOUBLE"
        const val INTEROP_SIGN_EXTEND = "INTEROP_SIGN_EXTEND"
        const val INTEROP_NARROW = "INTEROP_NARROW"
        const val INTEROP_STATIC_C_FUNCTION = "INTEROP_STATIC_C_FUNCTION"
        const val INTEROP_FUNPTR_INVOKE = "INTEROP_FUNPTR_INVOKE"

        // Kleaver implementation begin
        const val KLEAVER_MEMORY_COPY = "KLEAVER_MEMORY_COPY"
        const val KLEAVER_MEMORY_SET = "KLEAVER_MEMORY_SET"
        const val KLEAVER_MEMORY_MOVE = "KLEAVER_MEMORY_MOVE"
        const val KLEAVER_ALLOCA = "KLEAVER_ALLOCA"
        const val KLEAVER_ADDRESS_OF = "KLEAVER_ADDRESS_OF"
        const val KLEAVER_ALIGN_OF = "KLEAVER_ALIGN_OF"
        const val KLEAVER_SIZE_OF = "KLEAVER_SIZE_OF"
        const val KLEAVER_OFFSET_OF = "KLEAVER_OFFSET_OF"

        const val KLEAVER_SAT_ADDS = "KLEAVER_ADDS_SAT"
        const val KLEAVER_SAT_SUBS = "KLEAVER_SUBS_SAT"
        const val KLEAVER_SAT_SHLS = "KLEAVER_SHLS_SAT"
        const val KLEAVER_SAT_ADDU = "KLEAVER_ADDU_SAT"
        const val KLEAVER_SAT_SUBU = "KLEAVER_SUBU_SAT"
        const val KLEAVER_SAT_SHLU = "KLEAVER_SHLU_SAT"

        const val KLEAVER_U2F = "KLEAVER_U2F"
        const val KLEAVER_F2U = "KLEAVER_F2U"
        const val KLEAVER_UDIV = "KLEAVER_UDIV"
        const val KLEAVER_UREM = "KLEAVER_UREM"
        const val KLEAVER_UCMP = "KLEAVER_UCMP"

        const val KLEAVER_BIT_REVERSE = "KLEAVER_BIT_REVERSE"
        const val KLEAVER_BIT_SWAP = "KLEAVER_BIT_SWAP"
        const val KLEAVER_CTPOP = "KLEAVER_CTPOP"
        const val KLEAVER_CTLZ = "KLEAVER_CTLZ"
        const val KLEAVER_CTTZ = "KLEAVER_CTTZ"
        const val KLEAVER_FSHL = "KLEAVER_FSHL"
        const val KLEAVER_FSHR = "KLEAVER_FSHR"
        // Kleaver implementation end

        // Worker
        const val WORKER_EXECUTE = "WORKER_EXECUTE"

        // Atomic
        const val ATOMIC_GET_FIELD = "ATOMIC_GET_FIELD"
        const val ATOMIC_SET_FIELD = "ATOMIC_SET_FIELD"
        const val COMPARE_AND_SET_FIELD = "COMPARE_AND_SET_FIELD"
        const val COMPARE_AND_EXCHANGE_FIELD = "COMPARE_AND_EXCHANGE_FIELD"
        const val GET_AND_SET_FIELD = "GET_AND_SET_FIELD"
        const val GET_AND_ADD_FIELD = "GET_AND_ADD_FIELD"
        const val COMPARE_AND_SET = "COMPARE_AND_SET"
        const val COMPARE_AND_EXCHANGE = "COMPARE_AND_EXCHANGE"
        const val GET_AND_SET = "GET_AND_SET"
        const val GET_AND_ADD = "GET_AND_ADD"
        const val ATOMIC_GET_ARRAY_ELEMENT = "ATOMIC_GET_ARRAY_ELEMENT"
        const val ATOMIC_SET_ARRAY_ELEMENT = "ATOMIC_SET_ARRAY_ELEMENT"
        const val COMPARE_AND_EXCHANGE_ARRAY_ELEMENT = "COMPARE_AND_EXCHANGE_ARRAY_ELEMENT"
        const val GET_AND_SET_ARRAY_ELEMENT = "GET_AND_SET_ARRAY_ELEMENT"
        const val GET_AND_ADD_ARRAY_ELEMENT = "GET_AND_ADD_ARRAY_ELEMENT"
        const val COMPARE_AND_SET_ARRAY_ELEMENT = "COMPARE_AND_SET_ARRAY_ELEMENT"
    }
}
