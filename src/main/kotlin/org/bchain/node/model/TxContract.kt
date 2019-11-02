package org.bchain.node.model

import org.bchain.node.binaryValue
import org.bchain.node.serializeByMessagePack

data class TxContract(val name: String, val code: ByteArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TxContract

        if (name != other.name) return false
        if (!code.contentEquals(other.code)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + code.contentHashCode()
        return result
    }

    fun txArgument() = TxArgument(TxArgumentType.Address.type, let {
        serializeByMessagePack {
            packString(it.name)
            packValue(it.code.binaryValue())
        }
    })

}

