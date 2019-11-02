package org.bchain.node

import org.bchain.node.model.TxParameter
import org.bchain.node.serializer.StringChangeEndianBigIntSerialization
import java.math.BigDecimal
import java.math.BigInteger

class PropertyHelper(var propertyContract: String, val node: Node) {

    fun buyByBt(property: String,
            btContract: String,
                 symbol: String,
                 amount: BigDecimal,
                 fee: BigDecimal = BigDecimal.ZERO,
                 expiryNumber: Int = 100,
                 nonce: Long = -1,
                 chainId: BigInteger = BigInteger.ONE): String {
        val blockNumber = node.getBlockTopNumber()
        val actions = mutableListOf(propertyContract to TxParameter("buyByBt",
                listOf(property.txArgument(),
                        btContract.txArgument(),
                        symbol.txArgument(),
                        amount.toString().txArgument(),
                        blockNumber.txArgument(),
                        expiryNumber.txArgument())))
        if (fee > BigDecimal.ZERO) actions.add(0, Node.bcContractAddress to TxParameter.bcTransferFee(node.toBcInteger(fee)))
        return node.transfer(*actions.toTypedArray(), nonce = nonce, chainId = chainId)
    }

    fun buyByBc(property: String,
                amount: BigDecimal,
                fee: BigDecimal = BigDecimal.ZERO,
                expiryNumber: Int = 100,
                nonce: Long = -1,
                chainId: BigInteger = BigInteger.ONE): String {
        val blockNumber = node.getBlockTopNumber()
        val actions = mutableListOf(propertyContract to TxParameter("buyByBc",
                listOf(property.txArgument(),
                        node.toBcInteger(amount).toLong().txArgument(),
                        blockNumber.txArgument(),
                        blockNumber.txArgument())))
        if (fee > BigDecimal.ZERO) actions.add(0, Node.bcContractAddress to TxParameter.bcTransferFee(node.toBcInteger(fee)))
        return node.transfer(*actions.toTypedArray(), nonce = nonce, chainId = chainId)
    }

    fun hasProperty(property: String, address: String = node.bchainAddress): Boolean {
        val result = node.callAction(propertyContract, TxParameter("propsOf", listOf(address.txArgument(), property.txArgument())), serializer = StringChangeEndianBigIntSerialization).first()
        return result == BigInteger.ONE
    }

}
