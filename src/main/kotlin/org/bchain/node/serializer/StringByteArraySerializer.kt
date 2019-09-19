package org.bchain.node.serializer

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.internal.ArrayClassDesc
import kotlinx.serialization.internal.ByteDescriptor
import org.bchain.node.toHex
import org.bchain.node.hexToBytes

object StringByteArraySerializer: KSerializer<ByteArray> {

    override val descriptor = ArrayClassDesc(ByteDescriptor)

    override fun deserialize(decoder: Decoder): ByteArray = decoder.decodeString().hexToBytes()

    override fun serialize(encoder: Encoder, obj: ByteArray) {
        encoder.encodeString(obj.toHex())
    }

}
