package org.bchain.node.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bchain.node.serializer.StringIntSerialization
import org.bchain.node.serializer.StringLongSerialization

@Serializable
data class BlockInfo(@SerialName("R") val r: String,
                     @SerialName("S") val s: String,
                     @SerialName("V") val v: String,
                     @Serializable(StringIntSerialization::class) val number: Int,
                     @Serializable(StringIntSerialization::class) val size: Int,
                     @Serializable(StringLongSerialization::class) val timestamp: Long,
                     @SerialName("receiptsRoot") val receiptRoot: String,
                     @SerialName("transactionsRoot") val transactionRoot: String,
                     @SerialName("consensusData") val consensus: ConsensusInfo,
                     val transactions: List<TransactionInfo>,
                     val hash: String,
                     val logsBloom: String,
                     val producer: String,
                     val stateRoot: String)

@Serializable
data class SimpleBlockInfo(@SerialName("R") val r: String,
                     @SerialName("S") val s: String,
                     @SerialName("V") val v: String,
                     @Serializable(StringIntSerialization::class) val number: Int,
                     @Serializable(StringIntSerialization::class) val size: Int,
                     @Serializable(StringLongSerialization::class) val timestamp: Long,
                     @SerialName("receiptsRoot") val receiptRoot: String,
                     @SerialName("transactionsRoot") val transactionRoot: String,
                     @SerialName("consensusData") val consensus: ConsensusInfo,
                     val transactions: List<String>,
                     val hash: String,
                     val logsBloom: String,
                     val producer: String,
                     val stateRoot: String)

@Serializable
data class SubscribeBlockInfo(val r: String, val s: String, val v: String,
                              @Serializable(StringIntSerialization::class) val number: Int,
                              @Serializable(StringLongSerialization::class) val timestamp: Long,
                              @SerialName("receiptsRoot") val receiptRoot: String,
                              @SerialName("transactionsRoot") val transactionRoot: String,
                              @SerialName("consensusData") val consensus: SubscribeConsensusInfo,
                              @SerialName("blockProducer") val producer: String,
                              val hash: String,
                              val parentHash: String,
                              val logsBloom: String,
                              val stateRoot: String)
