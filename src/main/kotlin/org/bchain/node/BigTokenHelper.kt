package org.bchain.node

import org.bchain.node.model.TxArgument
import org.bchain.node.model.TxParameter
import org.bchain.node.serializer.AscByteArrayBigIntSerialization
import java.math.BigDecimal
import java.math.BigInteger

class BigTokenHelper(var btContract: String, val node: Node) {

    fun transfer(toAddress: String,
                 symbol: String,
                 amount: BigDecimal,
                 fee: BigDecimal = BigDecimal.ZERO,
                 memo: String = "",
                 expiryNumber: Int = 100,
                 nonce: Long = -1,
                 chainId: BigInteger = BigInteger.ONE): String {
        val blockNumber = node.getBlockTopNumber()
        val decimal = getDecimal(symbol)
        val actions = mutableListOf(createTransferAction(toAddress, symbol, amount.withDecimal(decimal), memo, blockNumber, expiryNumber))
        if (fee > BigDecimal.ZERO) actions.add(0, Node.bcContractAddress to TxParameter.bcTransferFee(node.toBcInteger(fee)))
        return node.transfer(*actions.toTypedArray(), nonce = nonce, chainId = chainId)
    }

    fun createTransferAction(toAddress: String, symbol: String, amount: BigInteger, memo: String, blockNumber: Long, expiryNumber: Int) = btContract to TxParameter("transfer", listOf(
            TxArgument.address(toAddress),            // to
            TxArgument.address(amount.toString()),        // amount
            TxArgument.address(symbol),                 // symbol
            TxArgument.address(memo),                 // memo
            TxArgument.int64(blockNumber),                     // blkNumber
            TxArgument.int32(expiryNumber)                   // expiry
    ))

    fun balanceOf(coin: String, address: String): Pair<BigDecimal, Int> {
        val decimal = getDecimal(coin)
        return try {
            node.callAction(btContract,
                    TxParameter("balanceOf", listOf(TxArgument.address(address), TxArgument.address(coin))), serializer = AscByteArrayBigIntSerialization)
                    .first().divideDecimal(decimal)
        } catch (_: Exception) {
            BigDecimal.ZERO
        } to decimal
    }

    fun getDecimal(symbol: String) = try {
        node.callAction(btContract, TxParameter("getDecimals", listOf(TxArgument.address(symbol))), serializer = AscByteArrayBigIntSerialization).first().toInt()
    } catch (_: Exception) {
        0
    }

    private fun BigDecimal.withDecimal(decimal: Int): BigInteger {
        val d = BigInteger.valueOf(10).pow(decimal)
        return (this * d.toBigDecimal()).toBigInteger()
    }

    private fun BigInteger.divideDecimal(decimal: Int): BigDecimal {
        val d = toString()
        return "${d.substring(0, d.length - decimal)}.${d.substring(d.length - decimal)}".toBigDecimal()
    }

}
