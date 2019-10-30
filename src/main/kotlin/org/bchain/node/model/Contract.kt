package org.bchain.node.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bchain.node.serializer.Base64ByteArraySerializer

@Serializable
data class Contract(@SerialName("inter_name") val name: String, @Serializable(Base64ByteArraySerializer::class) @SerialName("code") val code: ByteArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Contract

        if (name != other.name) return false
        if (!code.contentEquals(other.code)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + code.contentHashCode()
        return result
    }

}
