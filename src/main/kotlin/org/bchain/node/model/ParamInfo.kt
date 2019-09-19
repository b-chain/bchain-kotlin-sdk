package org.bchain.node.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParamInfo(@SerialName("func_name") val function: String, @SerialName("args") val arguments: List<ArgumentInfo>)
