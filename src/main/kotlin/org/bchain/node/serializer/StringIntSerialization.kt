package org.bchain.node.serializer

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.internal.IntDescriptor
import org.bchain.node.hexToInt
import org.bchain.node.toHex

object StringIntSerialization: KSerializer<Int> {

    override val descriptor = IntDescriptor

    override fun deserialize(decoder: Decoder) = decoder.decodeString().hexToInt()

    override fun serialize(encoder: Encoder, obj: Int) = encoder.encodeString(obj.toHex())

}
