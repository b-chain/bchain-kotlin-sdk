package org.bchain.node.serializer

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.internal.IntDescriptor
import org.bchain.node.hexToBigInt
import org.bchain.node.toHex
import java.math.BigInteger

object StringBigIntSerialization: KSerializer<BigInteger> {

    override val descriptor = IntDescriptor

    override fun deserialize(decoder: Decoder) = decoder.decodeString().hexToBigInt()

    override fun serialize(encoder: Encoder, obj: BigInteger) = encoder.encodeString(obj.toHex())

}
