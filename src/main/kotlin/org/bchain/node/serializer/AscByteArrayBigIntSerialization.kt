package org.bchain.node.serializer

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.internal.IntDescriptor
import org.bchain.node.defaultHexPrefix
import org.bchain.node.hexToBytes
import java.math.BigInteger

object AscByteArrayBigIntSerialization: KSerializer<BigInteger> {

    override val descriptor = IntDescriptor

    override fun deserialize(decoder: Decoder) = decoder.decodeString().hexToBytes().toString(Charsets.UTF_8).toBigInteger()

    override fun serialize(encoder: Encoder, obj: BigInteger) = encoder.encodeString(defaultHexPrefix + obj.toString(16))

}
