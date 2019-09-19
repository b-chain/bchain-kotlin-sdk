package org.bchain.node.model

import kotlinx.serialization.Serializable
import org.bchain.node.serializer.StringByteArraySerializer
import org.bchain.node.serializer.StringIntSerialization
import org.bchain.node.serializer.StringLongSerialization

@Serializable
data class TransactionInfo(val blockHash: String,
                           @Serializable(StringLongSerialization::class) val blockNumber: Long?,
                           val from: String,
                           val hash: String,
                           val nonce: String,
                           @Serializable(StringIntSerialization::class) val transactionIndex: Int,
                           @Serializable(StringLongSerialization::class) val v: Long,
                           @Serializable(StringLongSerialization::class) val s: Long,
                           @Serializable(StringLongSerialization::class) val r: Long,
                           val actions: List<ActionInfo>)
