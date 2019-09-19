package org.bchain.node

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.bouncycastle.util.encoders.Hex
import org.bchain.node.model.TxAction
import org.bchain.node.model.TxHeader
import org.bchain.node.model.TxParameter
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

fun String.action(parameter: TxParameter) = TxAction(toAddressBytes(), TxParameter.serializer().stringify(parameter).toByteArray())

fun ByteArray.append0() = copyOf(size + 1)

fun String.toByteArrayWith0() = toByteArray().append0()

fun TxHeader.valueMap(): MapValue = mapOf("Nonce".stringValue() to nonce.intValue()).mapValue()

fun TxAction.valueMap(): MapValue = mapOf("Contract".stringValue() to contract.binaryValue(), "Params".stringValue() to params.binaryValue()).mapValue()

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
