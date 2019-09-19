package org.bchain.node.serializer

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.internal.IntDescriptor
import org.bchain.node.hexToLong
import org.bchain.node.toHex

object StringLongSerialization: KSerializer<Long> {

    override val descriptor = IntDescriptor

    override fun deserialize(decoder: Decoder) = decoder.decodeString().hexToLong()

    override fun serialize(encoder: Encoder, obj: Long) = encoder.encodeString(obj.toHex())

}
