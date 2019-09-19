package org.bchain.node.model

import kotlinx.serialization.Serializable

@Serializable
data class ConsensusInfo(val id: String, val data: String)
