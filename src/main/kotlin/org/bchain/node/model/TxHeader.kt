package org.bchain.node.model

import org.bchain.node.intValue
import org.bchain.node.mapValue
import org.bchain.node.stringValue
import org.msgpack.value.MapValue

data class TxHeader(val nonce: Long = 0) {
    fun valueMap(): MapValue = mapOf("Nonce".stringValue() to nonce.intValue()).mapValue()
}
