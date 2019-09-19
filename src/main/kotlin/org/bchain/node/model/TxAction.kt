package org.bchain.node.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bchain.node.serializer.StringByteArraySerializer

@Serializable
data class TxAction(@Serializable(StringByteArraySerializer::class) @SerialName("address") val contract: ByteArray = ByteArray(0),
                    @Serializable(StringByteArraySerializer::class) val params: ByteArray = ByteArray(0)) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TxAction

        if (!contract.contentEquals(other.contract)) return false
        if (!params.contentEquals(other.params)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = contract.contentHashCode()
        result = 31 * result + params.contentHashCode()
        return result
    }

}
