package org.bchain.node

import org.bchain.node.model.TxArgument
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
                listOf(TxArgument.address(property),
                        TxArgument.address(btContract),
                        TxArgument.address(symbol),
                        TxArgument.address(amount.toString()),
                        TxArgument.int64(blockNumber),
                        TxArgument.int32(expiryNumber))))
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
                listOf(TxArgument.address(property),
                        TxArgument.int64(node.toBcInteger(amount).toLong()),
                        TxArgument.int64(blockNumber),
                        TxArgument.int32(expiryNumber))))
        if (fee > BigDecimal.ZERO) actions.add(0, Node.bcContractAddress to TxParameter.bcTransferFee(node.toBcInteger(fee)))
        return node.transfer(*actions.toTypedArray(), nonce = nonce, chainId = chainId)
    }

    fun hasProperty(property: String, address: String = node.bchainAddress): Boolean {
        val result = node.callAction(propertyContract, TxParameter("propsOf", listOf(TxArgument.address(address), TxArgument.address(property))), serializer = StringChangeEndianBigIntSerialization).first()
        return result == BigInteger.ONE
    }

}
