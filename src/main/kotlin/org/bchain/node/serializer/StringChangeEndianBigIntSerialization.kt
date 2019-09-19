package org.bchain.node.serializer

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.internal.IntDescriptor
import org.bchain.node.defaultHexPrefix
import org.bchain.node.hexToBigInt
import org.bchain.node.toHex
import java.lang.StringBuilder
import java.math.BigInteger

object StringChangeEndianBigIntSerialization: KSerializer<BigInteger> {

    override val descriptor = IntDescriptor

    override fun deserialize(decoder: Decoder) = decoder.decodeString().changeEndian().hexToBigInt()

    override fun serialize(encoder: Encoder, obj: BigInteger) = encoder.encodeString(obj.toHex().changeEndian())

    private fun String.changeEndian(prefix: String? = defaultHexPrefix) = StringBuilder().also { builder ->
        val v = if (prefix == null) this else substring(prefix.length)
        v.indices.step(2).forEach {
            val index = v.length - it - 1
            builder.append(v[index - 1]).append(v[index])
        }
    }.toString()

}
