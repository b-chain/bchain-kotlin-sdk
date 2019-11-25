package org.bchain.node

import kotlinx.serialization.KSerializer
import org.bchain.node.model.TxParameter
import org.bchain.node.serializer.StringByteArraySerializer
import java.math.BigDecimal
import java.math.BigInteger

class KVStoreHelper(var storeContract: String, val node: Node) {

    fun set(key: String, value: ByteArray,
            fee: BigDecimal = BigDecimal.ZERO,
            expiryNumber: Int = 100,
            nonce: Long = -1,
            chainId: BigInteger = BigInteger.ONE): String {
        val blockNumber = node.getBlockTopNumber()
        val actions = mutableListOf(storeContract to TxParameter("set",
                listOf(key.txArgument(),
                        value.txArgument(),
                        value.size.txArgument(),
                        blockNumber.txArgument(),
                        expiryNumber.txArgument())))
        if (fee > BigDecimal.ZERO) actions.add(0, Node.bcContractAddress to TxParameter.bcTransferFee(node.toBcInteger(fee)))
        return node.transfer(*actions.toTypedArray(), nonce = nonce, chainId = chainId)
    }

    fun<T> get(key: String, serializer: KSerializer<T>): T = node.callAction(storeContract, TxParameter("get", listOf(key.txArgument())), serializer = serializer).first()


    fun setString(key: String, content: String,
                  fee: BigDecimal = BigDecimal.ZERO,
                  expiryNumber: Int = 100,
                  nonce: Long = -1,
                  chainId: BigInteger = BigInteger.ONE) = set(key, content.toByteArray(), fee, expiryNumber, nonce, chainId)

    fun getString(key: String) = get(key, StringByteArraySerializer).toString()

}

