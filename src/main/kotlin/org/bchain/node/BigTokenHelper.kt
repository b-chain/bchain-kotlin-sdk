package org.bchain.node

import org.bchain.node.model.TxArgument
import org.bchain.node.model.TxParameter
import org.bchain.node.serializer.AscByteArrayBigIntSerialization
import java.math.BigDecimal
import java.math.BigInteger
import java.security.InvalidParameterException

class BigTokenHelper(var btContract: String, val node: Node) {

    fun transfer(toAddress: String,
                 symbol: String,
                 amount: BigDecimal,
                 fee: BigDecimal = BigDecimal.ZERO,
                 memo: String = "",
                 expiryNumber: Int = 100,
                 nonce: Long = -1,
                 chainId: BigInteger = BigInteger.ONE) = multipleTransfer(BigTokenTransferParameter(toAddress, symbol, amount, memo), fee = fee, expiryNumber = expiryNumber, nonce = nonce, chainId = chainId)

    fun multipleTransfer(vararg params: BigTokenTransferParameter, fee: BigDecimal = BigDecimal.ZERO, expiryNumber: Int = 100, nonce: Long = -1, chainId: BigInteger = BigInteger.ONE): String {
        if (fee < BigDecimal.ZERO) throw InvalidParameterException("fee must >= 0.")
        if (params.isEmpty()) throw InvalidParameterException("no params")
        val blockNumber = node.getBlockTopNumber()
        val actions = params.map {
            if (it.toAddress.isBlank()) throw InvalidParameterException("target address cannot be blank.")
            if (it.symbol.isBlank()) throw InvalidParameterException("symbol cannot be blank.")
            if (it.memo.toByteArray().size >= 32) throw InvalidParameterException("memo must < 32 bytes.")
            if (it.amount <= BigDecimal.ZERO) throw InvalidParameterException("value must > 0.")
            it.toTransferAction(blockNumber, expiryNumber)
        }
        val array = (if (fee > BigDecimal.ZERO) listOf(Node.bcContractAddress to TxParameter.bcTransferFee(node.toBcInteger(fee))).plus(actions) else actions).toTypedArray()
        return node.transfer(*array, nonce = nonce, chainId = chainId)
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

    private fun BigTokenTransferParameter.toTransferAction(blockNumber: Long, expiryNumber: Int) = btContract to TxParameter("transfer", listOf(
            TxArgument.address(toAddress),            // to
            TxArgument.address(amount.withDecimal(getDecimal(symbol)).toString()),        // amount
            TxArgument.address(symbol),                 // symbol
            TxArgument.address(memo),                 // memo
            TxArgument.int64(blockNumber),                     // blkNumber
            TxArgument.int32(expiryNumber)                   // expiry
    ))

    data class BigTokenTransferParameter(val toAddress: String, val symbol: String, val amount: BigDecimal, val memo: String)

}
