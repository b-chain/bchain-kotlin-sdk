package org.bchain.node

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.bchain.node.model.*
import org.bouncycastle.util.encoders.Hex
import org.msgpack.core.MessageBufferPacker
import org.msgpack.core.MessagePack
import org.msgpack.core.MessagePacker
import org.msgpack.value.*
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

const val defaultHexPrefix = "0x"

fun String.toAddressBytes(): ByteArray {
    val bytes = hexToBytes()
    if (bytes.size != 20) throw UnknownFormatConversionException("$this is not address, not enough length")
    return bytes
}

fun String.hexToBytes(prefix: String? = "0x"): ByteArray {
    val s = if (prefix != null) {
        if (!startsWith(prefix, true)) throw UnknownFormatConversionException("$this is not address.")
        substring(prefix.length)
    } else this
    return (s.indices step 2).map { s.substring(it, it + 2).toInt(16).toByte() }.toByteArray()
}

fun String.action(parameter: TxParameter) = TxAction(toAddressBytes(), serializeByMessagePack {
    packString(parameter.functionName)
    packArrayHeader(parameter.args.size)
    parameter.args.forEach {
        packInt(it.type)
        packValue(it.value.binaryValue())
    }
})

fun ByteArray.append0() = copyOf(size + 1)

fun String.toByteArrayWith0() = toByteArray().append0()

fun BigInteger.toCustomBigIntByteArray(): ByteArray {
    val array = toByteArray()
    var offset = 0
    var size = array.size
    if (array.first() == 0.toByte()) {
        offset++
        size--
    }
    return ByteBuffer.allocate(size + 1).order(ByteOrder.BIG_ENDIAN).put((if (this > BigInteger.ZERO) 1 else if (this < BigInteger.ZERO) -1 else 0).toByte()).put(array, offset, size).array()
}

fun List<Value>.arrayValue(): ArrayValue = ValueFactory.newArray(this)
fun Map<out Value, Value>.mapValue(): MapValue = ValueFactory.newMap(this)
fun String.stringValue(): StringValue = ValueFactory.newString(this)
fun BigInteger.bigintValue(): MapValue = ValueFactory.newMap(mapOf(ValueFactory.newString("bigint") to ValueFactory.newBinary(toCustomBigIntByteArray())))
fun Int.intValue(): IntegerValue = ValueFactory.newInteger(this)
fun Long.intValue(): IntegerValue = ValueFactory.newInteger(this)
fun ByteArray.binaryValue(): BinaryValue = ValueFactory.newBinary(this)

fun Int.toHex(prefix: String? = defaultHexPrefix) = toString(16).checkPrefix(prefix)
fun Long.toHex(prefix: String? = defaultHexPrefix) = toString(16).checkPrefix(prefix)
fun BigInteger.toHex(prefix: String? = defaultHexPrefix) = toString(16).checkPrefix(prefix)
fun ByteArray.toHex(prefix: String? = defaultHexPrefix, offset: Int = 0, len: Int = size): String = Hex.toHexString(this, offset, len).checkPrefix(prefix)

fun String.hexToInt(defaultValue: Int = 0, prefix: String? = defaultHexPrefix) = (if (prefix != null && startsWith(prefix)) substring(prefix.length).toIntOrNull(16) else toIntOrNull()) ?: defaultValue
fun String.hexToLong(defaultValue: Long = 0, prefix: String? = defaultHexPrefix) = (if (prefix != null && startsWith(prefix)) substring(prefix.length).toLongOrNull(16) else toLongOrNull()) ?: defaultValue
fun String.hexToBigInt(prefix: String? = defaultHexPrefix) = BigInteger(if (prefix != null && startsWith(prefix)) substring(prefix.length) else this, 16)

fun<T> KSerializer<T>.stringify(obj: T) = Json.stringify(this, obj)
fun<T> KSerializer<T>.parse(content: String) = Json.nonstrict.parse(this, content)

fun String.checkPrefix(prefix: String?) = if (prefix != null) prefix + this else this

fun serializeByMessagePack(messagePacker: MessageBufferPacker = MessagePack.newDefaultBufferPacker(), block: MessagePacker.() -> Unit): ByteArray = messagePacker.use {
    block(it)
    it.toByteArray()
}

fun Int.txArgument() = TxArgument(TxArgumentType.Int32.type, ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(this).array())
fun Long.txArgument() = TxArgument(TxArgumentType.Int64.type, ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(this).array())
fun Float.txArgument() = TxArgument(TxArgumentType.Float32.type, ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(this).array())
fun Double.txArgument() = TxArgument(TxArgumentType.Float64.type, ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putDouble(this).array())
fun String.txArgument(with0: Boolean = true) = TxArgument(TxArgumentType.Address.type, if (with0) toByteArrayWith0() else toByteArray())
