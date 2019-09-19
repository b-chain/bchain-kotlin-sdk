package org.bchain.node.serializer

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.internal.ArrayClassDesc
import kotlinx.serialization.internal.ByteDescriptor
import java.util.*

object Base64ByteArraySerializer: KSerializer<ByteArray> {

    override val descriptor = ArrayClassDesc(ByteDescriptor)

    override fun deserialize(decoder: Decoder): ByteArray = Base64.getDecoder().decode(decoder.decodeString())

    override fun serialize(encoder: Encoder, obj: ByteArray) {
        encoder.encodeString(Base64.getEncoder().encodeToString(obj))
    }

}
