package org.bchain.node.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConsensusInfo(val id: String, val data: String)

@Serializable
data class SubscribeConsensusInfo(@SerialName("consensus_id") val id: String, @SerialName("consensus_param") val param: String)
