package org.bchain.node.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bchain.node.parse
import org.bchain.node.serializer.StringByteArraySerializer

@Serializable
data class ActionInfo(@SerialName("address") val contract: String, @Serializable(StringByteArraySerializer::class) @SerialName("params") val paramBytes: ByteArray) {

    val parameter: ParamInfo by lazy { ParamInfo.serializer().parse(paramBytes.toString(Charsets.UTF_8)) }

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
