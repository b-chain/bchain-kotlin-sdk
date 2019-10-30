package org.bchain.node.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bchain.node.stringify
import java.math.BigInteger

@Serializable
data class TxParameter(@SerialName("func_name") val functionName: String, val args: List<TxArgument>) {

    companion object {
        fun createContract(contract: Contract) = TxParameter("createContract", listOf(TxArgument.address(Contract.serializer().stringify(contract))))
        fun bcTransfer(toAddress: String, value: BigInteger, memo: String) = TxParameter("transfer", listOf(TxArgument.address(toAddress), TxArgument.int64(value.toLong()), TxArgument.address(memo)))
        fun bcTransferFee(value: BigInteger) = TxParameter("transferFee", listOf(TxArgument.int64(value.toLong())))
    }

}
