package org.bchain.node.model

import kotlinx.serialization.Serializable

@Serializable
data class TxHeader(val nonce: Long = 0)
