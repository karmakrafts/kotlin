@_exported import ExportedKotlinPackages
import KotlinRuntimeSupport
import KotlinRuntime
@_implementationOnly import KotlinBridges_stdlib

public extension ExportedKotlinPackages.kotlin {
    public protocol Annotation: KotlinRuntime.KotlinBase {
    }
    public final class ByteArray: KotlinRuntime.KotlinBase, KotlinRuntimeSupport._KotlinBridged {
        public var size: Swift.Int32 {
            get {
                return kotlin_ByteArray_size_get(self.__externalRCRef())
            }
        }
        public init(
            size: Swift.Int32
        ) {
            fatalError()
        }
        public init(
            size: Swift.Int32,
            `init`: @escaping (Swift.Int32) -> Swift.Int8
        ) {
            fatalError()
        }
        package override init(
            __externalRCRef: Swift.UnsafeMutableRawPointer?
        ) {
            super.init(__externalRCRef: __externalRCRef)
        }
    }
    public final class IntArray: KotlinRuntime.KotlinBase, KotlinRuntimeSupport._KotlinBridged {
        public var size: Swift.Int32 {
            get {
                return kotlin_IntArray_size_get(self.__externalRCRef())
            }
        }
        public init(
            size: Swift.Int32
        ) {
            fatalError()
        }
        public init(
            size: Swift.Int32,
            `init`: @escaping (Swift.Int32) -> Swift.Int32
        ) {
            fatalError()
        }
        package override init(
            __externalRCRef: Swift.UnsafeMutableRawPointer?
        ) {
            super.init(__externalRCRef: __externalRCRef)
        }
    }
    public final class Boolean: KotlinRuntime.KotlinBase, KotlinRuntimeSupport._KotlinBridged {
    }
    public final class Char: KotlinRuntime.KotlinBase, KotlinRuntimeSupport._KotlinBridged {
    }
    open class Exception: ExportedKotlinPackages.kotlin.Throwable {
        public override init() {
            let __kt = kotlin_Exception_init_allocate()
            super.init(__externalRCRef: __kt)
            kotlin_Exception_init_initialize__TypesOfArguments__Swift_UnsafeMutableRawPointer__(__kt)
        }
        public override init(
            message: Swift.String?
        ) {
            let __kt = kotlin_Exception_init_allocate()
            super.init(__externalRCRef: __kt)
            kotlin_Exception_init_initialize__TypesOfArguments__Swift_UnsafeMutableRawPointer_Swift_Optional_Swift_String___(__kt, message ?? nil)
        }
        public override init(
            message: Swift.String?,
            cause: ExportedKotlinPackages.kotlin.Throwable?
        ) {
            let __kt = kotlin_Exception_init_allocate()
            super.init(__externalRCRef: __kt)
            kotlin_Exception_init_initialize__TypesOfArguments__Swift_UnsafeMutableRawPointer_Swift_Optional_Swift_String__Swift_Optional_ExportedKotlinPackages_kotlin_Throwable___(__kt, message ?? nil, cause.map { it in it.__externalRCRef() } ?? nil)
        }
        public override init(
            cause: ExportedKotlinPackages.kotlin.Throwable?
        ) {
            let __kt = kotlin_Exception_init_allocate()
            super.init(__externalRCRef: __kt)
            kotlin_Exception_init_initialize__TypesOfArguments__Swift_UnsafeMutableRawPointer_Swift_Optional_ExportedKotlinPackages_kotlin_Throwable___(__kt, cause.map { it in it.__externalRCRef() } ?? nil)
        }
        package override init(
            __externalRCRef: Swift.UnsafeMutableRawPointer?
        ) {
            super.init(__externalRCRef: __externalRCRef)
        }
    }
    open class RuntimeException: ExportedKotlinPackages.kotlin.Exception {
        public override init() {
            let __kt = kotlin_RuntimeException_init_allocate()
            super.init(__externalRCRef: __kt)
            kotlin_RuntimeException_init_initialize__TypesOfArguments__Swift_UnsafeMutableRawPointer__(__kt)
        }
        public override init(
            message: Swift.String?
        ) {
            let __kt = kotlin_RuntimeException_init_allocate()
            super.init(__externalRCRef: __kt)
            kotlin_RuntimeException_init_initialize__TypesOfArguments__Swift_UnsafeMutableRawPointer_Swift_Optional_Swift_String___(__kt, message ?? nil)
        }
        public override init(
            message: Swift.String?,
            cause: ExportedKotlinPackages.kotlin.Throwable?
        ) {
            let __kt = kotlin_RuntimeException_init_allocate()
            super.init(__externalRCRef: __kt)
            kotlin_RuntimeException_init_initialize__TypesOfArguments__Swift_UnsafeMutableRawPointer_Swift_Optional_Swift_String__Swift_Optional_ExportedKotlinPackages_kotlin_Throwable___(__kt, message ?? nil, cause.map { it in it.__externalRCRef() } ?? nil)
        }
        public override init(
            cause: ExportedKotlinPackages.kotlin.Throwable?
        ) {
            let __kt = kotlin_RuntimeException_init_allocate()
            super.init(__externalRCRef: __kt)
            kotlin_RuntimeException_init_initialize__TypesOfArguments__Swift_UnsafeMutableRawPointer_Swift_Optional_ExportedKotlinPackages_kotlin_Throwable___(__kt, cause.map { it in it.__externalRCRef() } ?? nil)
        }
        package override init(
            __externalRCRef: Swift.UnsafeMutableRawPointer?
        ) {
            super.init(__externalRCRef: __externalRCRef)
        }
    }
    open class IllegalArgumentException: ExportedKotlinPackages.kotlin.RuntimeException {
        public override init() {
            let __kt = kotlin_IllegalArgumentException_init_allocate()
            super.init(__externalRCRef: __kt)
            kotlin_IllegalArgumentException_init_initialize__TypesOfArguments__Swift_UnsafeMutableRawPointer__(__kt)
        }
        public override init(
            message: Swift.String?
        ) {
            let __kt = kotlin_IllegalArgumentException_init_allocate()
            super.init(__externalRCRef: __kt)
            kotlin_IllegalArgumentException_init_initialize__TypesOfArguments__Swift_UnsafeMutableRawPointer_Swift_Optional_Swift_String___(__kt, message ?? nil)
        }
        public override init(
            message: Swift.String?,
            cause: ExportedKotlinPackages.kotlin.Throwable?
        ) {
            let __kt = kotlin_IllegalArgumentException_init_allocate()
            super.init(__externalRCRef: __kt)
            kotlin_IllegalArgumentException_init_initialize__TypesOfArguments__Swift_UnsafeMutableRawPointer_Swift_Optional_Swift_String__Swift_Optional_ExportedKotlinPackages_kotlin_Throwable___(__kt, message ?? nil, cause.map { it in it.__externalRCRef() } ?? nil)
        }
        public override init(
            cause: ExportedKotlinPackages.kotlin.Throwable?
        ) {
            let __kt = kotlin_IllegalArgumentException_init_allocate()
            super.init(__externalRCRef: __kt)
            kotlin_IllegalArgumentException_init_initialize__TypesOfArguments__Swift_UnsafeMutableRawPointer_Swift_Optional_ExportedKotlinPackages_kotlin_Throwable___(__kt, cause.map { it in it.__externalRCRef() } ?? nil)
        }
        package override init(
            __externalRCRef: Swift.UnsafeMutableRawPointer?
        ) {
            super.init(__externalRCRef: __externalRCRef)
        }
    }
    public final class Byte: ExportedKotlinPackages.kotlin.Number {
    }
    public final class Short: ExportedKotlinPackages.kotlin.Number {
    }
    public final class Int: ExportedKotlinPackages.kotlin.Number {
    }
    public final class Long: ExportedKotlinPackages.kotlin.Number {
    }
    public final class Float: ExportedKotlinPackages.kotlin.Number {
    }
    public final class Double: ExportedKotlinPackages.kotlin.Number {
    }
    public final class String: KotlinRuntime.KotlinBase, ExportedKotlinPackages.kotlin.CharSequence, KotlinRuntimeSupport._KotlinBridged {
    }
    open class Throwable: KotlinRuntime.KotlinBase, KotlinRuntimeSupport._KotlinBridged {
        open var message: Swift.String? {
            get {
                return kotlin_Throwable_message_get(self.__externalRCRef())
            }
        }
        open var cause: ExportedKotlinPackages.kotlin.Throwable? {
            get {
                return { switch kotlin_Throwable_cause_get(self.__externalRCRef()) { case nil: .none; case let res: ExportedKotlinPackages.kotlin.Throwable(__externalRCRef: res); } }()
            }
        }
        public final func getStackTrace() -> Swift.Never {
            fatalError()
        }
        public final func printStackTrace() -> Swift.Void {
            return kotlin_Throwable_printStackTrace(self.__externalRCRef())
        }
        open func toString() -> Swift.String {
            return kotlin_Throwable_toString(self.__externalRCRef())
        }
        public init(
            message: Swift.String?,
            cause: ExportedKotlinPackages.kotlin.Throwable?
        ) {
            let __kt = kotlin_Throwable_init_allocate()
            super.init(__externalRCRef: __kt)
            kotlin_Throwable_init_initialize__TypesOfArguments__Swift_UnsafeMutableRawPointer_Swift_Optional_Swift_String__Swift_Optional_ExportedKotlinPackages_kotlin_Throwable___(__kt, message ?? nil, cause.map { it in it.__externalRCRef() } ?? nil)
        }
        public init(
            message: Swift.String?
        ) {
            let __kt = kotlin_Throwable_init_allocate()
            super.init(__externalRCRef: __kt)
            kotlin_Throwable_init_initialize__TypesOfArguments__Swift_UnsafeMutableRawPointer_Swift_Optional_Swift_String___(__kt, message ?? nil)
        }
        public init(
            cause: ExportedKotlinPackages.kotlin.Throwable?
        ) {
            let __kt = kotlin_Throwable_init_allocate()
            super.init(__externalRCRef: __kt)
            kotlin_Throwable_init_initialize__TypesOfArguments__Swift_UnsafeMutableRawPointer_Swift_Optional_ExportedKotlinPackages_kotlin_Throwable___(__kt, cause.map { it in it.__externalRCRef() } ?? nil)
        }
        public override init() {
            let __kt = kotlin_Throwable_init_allocate()
            super.init(__externalRCRef: __kt)
            kotlin_Throwable_init_initialize__TypesOfArguments__Swift_UnsafeMutableRawPointer__(__kt)
        }
        package override init(
            __externalRCRef: Swift.UnsafeMutableRawPointer?
        ) {
            super.init(__externalRCRef: __externalRCRef)
        }
    }
    public final class UByte: KotlinRuntime.KotlinBase, KotlinRuntimeSupport._KotlinBridged {
    }
    public final class UInt: KotlinRuntime.KotlinBase, KotlinRuntimeSupport._KotlinBridged {
    }
    public final class ULong: KotlinRuntime.KotlinBase, KotlinRuntimeSupport._KotlinBridged {
    }
    public final class UShort: KotlinRuntime.KotlinBase, KotlinRuntimeSupport._KotlinBridged {
    }
}
public extension ExportedKotlinPackages.kotlin.time {
    public final class Duration: KotlinRuntime.KotlinBase, KotlinRuntimeSupport._KotlinBridged {
    }
}
public extension ExportedKotlinPackages.kotlin.Annotation where Self : KotlinRuntimeSupport._KotlinBridged {
}
