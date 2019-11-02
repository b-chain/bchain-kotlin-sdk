package org.bchain.node.model

import org.bchain.node.txArgument
import java.math.BigInteger

data class TxParameter(val functionName: String, val args: List<TxArgument>) {

    companion object {
        fun createContract(contract: TxContract) = TxParameter("createContract", listOf(contract.txArgument()))
        fun bcTransfer(toAddress: String, value: BigInteger, memo: String) = TxParameter("transfer", listOf(toAddress.txArgument(), value.toLong().txArgument(), memo.txArgument()))
        fun bcTransferFee(value: BigInteger) = TxParameter("transferFee", listOf(value.toLong().txArgument()))
    }

}
