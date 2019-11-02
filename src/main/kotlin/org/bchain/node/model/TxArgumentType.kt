package org.bchain.node.model

enum class TxArgumentType(val type: Int) {
    Int32(0), Int64(1), Float32(2), Float64(3), Address(4);
    val key = name.toLowerCase()
}
