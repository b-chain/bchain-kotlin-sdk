package org.bchain.node.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bchain.node.serializer.Base64ByteArraySerializer
import org.bchain.node.toHex
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Serializable
data class ArgumentInfo(val type: String, @SerialName("val") @Serializable(Base64ByteArraySerializer::class) val valueBytes: ByteArray) {

    fun addressValue(): String {
        val needType = "address"
        if (type != needType) throw NumberFormatException("$type is not equal $needType")
        return valueBytes.toHex()
    }

    fun int32(): Int {
        val needType = "int32"
        if (type != needType) throw NumberFormatException("$type is not equal $needType")
        return ByteBuffer.wrap(valueBytes).order(ByteOrder.LITTLE_ENDIAN).int
    }

    fun int64(): Long {
        val needType = "int64"
        if (type != needType) throw NumberFormatException("$type is not equal $needType")
        return ByteBuffer.wrap(valueBytes).order(ByteOrder.LITTLE_ENDIAN).long
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArgumentInfo

        if (type != other.type) return false
        if (!valueBytes.contentEquals(other.valueBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + valueBytes.contentHashCode()
        return result
    }

}
