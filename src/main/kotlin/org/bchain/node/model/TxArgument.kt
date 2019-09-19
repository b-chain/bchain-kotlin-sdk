package org.bchain.node.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bchain.node.serializer.Base64ByteArraySerializer
import org.bchain.node.toByteArrayWith0
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Serializable
data class TxArgument(val type: String, @SerialName("val") @Serializable(Base64ByteArraySerializer::class) val value: ByteArray) {

    companion object {
        fun address(address: String, with0: Boolean = true) = TxArgument("address", if (with0) address.toByteArrayWith0() else address.toByteArray())
        fun int32(value: Int) = TxArgument("int32", ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array())
        fun int64(value: Long) = TxArgument("int64", ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(value).array())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TxArgument

        if (type != other.type) return false
        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + value.contentHashCode()
        return result
    }

}
