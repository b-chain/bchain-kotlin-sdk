package org.bchain.node.model

import kotlinx.serialization.Serializable
import org.bchain.node.serializer.StringIntSerialization

@Serializable
data class TxPoolStatus(@Serializable(StringIntSerialization::class) val pending: Int,
                        @Serializable(StringIntSerialization::class) val queued: Int)
