package org.bchain.node.model

import kotlinx.serialization.Serializable
import org.bchain.node.serializer.StringIntSerialization
import org.bchain.node.serializer.StringLongSerialization

@Serializable
data class TransactionReceiptInfo(val blockHash: String,
                                  @Serializable(StringLongSerialization::class) val blockNumber: Long,
                                  @Serializable(StringIntSerialization::class) val transactionIndex: Int,
                                  val from: String,
                                  val transactionHash: String,
                                  val contractAddress: List<String>)
