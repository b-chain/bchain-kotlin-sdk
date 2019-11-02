package org.bchain.node.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bchain.node.serializer.StringByteArraySerializer
import org.msgpack.core.MessagePack
import org.msgpack.value.ValueFactory
import org.msgpack.value.Variable

@Serializable
data class ActionInfo(@SerialName("address") val contract: String, @Serializable(StringByteArraySerializer::class) @SerialName("params") val paramBytes: ByteArray) {

    val parameter: TxParameter by lazy {
        MessagePack.newDefaultUnpacker(paramBytes).use { unpacker ->
            val variable = Variable()
            TxParameter(unpacker.unpackString(),
                    (0 until unpacker.unpackArrayHeader()).map {
                        TxArgument(unpacker.unpackInt(), unpacker.unpackValue(variable).asBinaryValue().asByteArray())
                    })
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ActionInfo

        if (contract != other.contract) return false
        if (!paramBytes.contentEquals(other.paramBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = contract.hashCode()
        result = 31 * result + paramBytes.contentHashCode()
        return result
    }

}
