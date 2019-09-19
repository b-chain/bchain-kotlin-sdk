package org.bchain.node

import kotlinx.serialization.KSerializer
import kotlinx.serialization.internal.ArrayListSerializer
import kotlinx.serialization.internal.IntSerializer
import kotlinx.serialization.internal.StringSerializer
import net.consensys.cava.crypto.Hash
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.msgpack.core.MessagePack
import org.msgpack.value.ValueFactory
import java.math.BigInteger
import java.security.*
import net.consensys.cava.crypto.SECP256K1
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.bchain.node.model.*
import org.bchain.node.serializer.StringChangeEndianBigIntSerialization
import org.bchain.node.serializer.StringLongSerialization
import java.lang.RuntimeException
import java.math.BigDecimal

class Node(host: String = "127.0.0.1", port: Int = 8989, privateKey: String? = null, private var debugInfo: Boolean = false) {

    companion object {
        init {
            Security.addProvider(BouncyCastleProvider())
        }

        const val bcContractAddress = "0xb78f12Cb3924607A8BC6a66799e159E3459097e9"
        const val bcDecimalScale = 8
        val bcDecimal: BigDecimal = BigDecimal.TEN.pow(bcDecimalScale)
        val bigIntegerTwo = 2.toBigInteger()
    }

    private val messagePacker by lazy { MessagePack.newDefaultBufferPacker() }
    private val requestUrl = "${if (host.contains(":/")) "" else "http://"}$host:$port"

    private lateinit var keyPair: SECP256K1.KeyPair

    lateinit var bchainAddress: String
        private set

    private val httpClient by lazy { OkHttpClient.Builder().build() }

    init { if (privateKey != null) updatePrivateKey(privateKey) }

    fun updatePrivateKey(key: String) {
        keyPair = SECP256K1.KeyPair.fromSecretKey(SECP256K1.SecretKey.fromInteger(key.toBigInteger(16)))
        val (xBytes, yBytes) = keyPair.publicKey().asEcPoint().run { rawXCoord.encoded to rawYCoord.encoded }
        val bytes = ByteArray(64)
        fun ByteArray.copy(endIndex: Int = bytes.size) = copyInto(bytes, endIndex - size, 0, size)
        xBytes.copy(bytes.size / 2)
        yBytes.copy()
        val hash = bytes.sha3()
        bchainAddress = hash.toHex(offset = hash.size - 20, len = 20)
    }

    fun transferBc(toAddress: String,
                           value: BigDecimal,
                           memo: String = "",
                           fee: BigDecimal = BigDecimal.ZERO,
                           nonce: Long = -1,
                           chainId: BigInteger = BigInteger.ONE): String {
        if (memo.toByteArray().size >= 32) throw InvalidParameterException("memo must < 32 bytes.")
        if (value <= BigDecimal.ZERO) throw InvalidParameterException("value must > 0.")
        if (fee < BigDecimal.ZERO) throw InvalidParameterException("fee must >= 0.")
        val actions = mutableListOf(bcContractAddress to TxParameter.bcTransfer(toAddress, toBcInteger(value), memo))
        if (fee > BigDecimal.ZERO) actions.add(0, bcContractAddress to TxParameter.bcTransferFee(toBcInteger(fee)))
        return transfer(*actions.toTypedArray(), nonce = nonce, chainId = chainId)
    }

    fun transfer(vararg actions: Pair<String, TxParameter>,
                         nonce: Long = -1,
                         chainId: BigInteger = BigInteger.ONE): String {
        val n = if (nonce < 0) getAccountNonce(bchainAddress) else nonce
        val acts = actions.map { it.first.action(it.second) }.toTypedArray()
        val bytes = txToPackArray(*acts, nonce = n, chainId = chainId)
        val sh3 = bytes.sha3()
        val sign = sh3.signHashed()
        val tx = sign.txToPackArray(*acts, nonce = n, chainId = chainId)
        return "bchain_sendRawTransaction".invoke(StringSerializer, tx.toHex())
    }

    fun getBcBalance(address: String = bchainAddress) = toBcDecimal(callAction(bcContractAddress, TxParameter("balenceOf", listOf(TxArgument.address(address))), serializer = StringChangeEndianBigIntSerialization).first())

    fun <T> callAction(contractAddress: String, parameter: TxParameter, number: Long = -1, serializer: KSerializer<T>): List<T> = "bchain_actionCall".invoke(ArrayListSerializer(serializer), contractAddress.action(parameter), number.toBlockNumber())

    fun getProtocolVersion(): Int = "bchain_protocolVersion".invoke(IntSerializer)

    fun getTxPoolStatus(): TxPoolStatus = "txpool_status".invoke(TxPoolStatus.serializer())

    fun getAccounts(): List<String> = "personal_listAccounts".invoke(ArrayListSerializer(StringSerializer))

    fun createAccount(password: String): String = "personal_newAccount".invoke(StringSerializer, password)

    fun importAccount(privateKey: String, password: String): String = "personal_importRawKey".invoke(StringSerializer, privateKey, password)

    fun getBlockTopNumber(): Long = "bchain_blockNumber".invoke(StringLongSerialization)

    fun getBlockByNumber(number: Long = -1, full: Boolean = true): BlockInfo = "bchain_getBlockByNumber".invoke(BlockInfo.serializer(), number.toBlockNumber(), full)

    fun getBlockByHash(hash: String, full: Boolean = true): BlockInfo = "bchain_getBlockByHash".invoke(BlockInfo.serializer(), hash, full)

    fun getAccountNonce(address: String = bchainAddress, number: Long = -1): Long = "bchain_getAccountNonce".invoke(StringLongSerialization, address, number.toBlockNumber())

    fun getTransactionByHash(transactionHash: String): TransactionInfo = "bchain_getTransactionByHash".invoke(TransactionInfo.serializer(), transactionHash)

    fun getTransactionReceiptByHash(transactionHash: String): TransactionReceiptInfo = "bchain_getTransactionReceipt".invoke(TransactionReceiptInfo.serializer(), transactionHash)

    private fun <T> String.invoke(serializer: KSerializer<T>, vararg params: Any): T {
        val method = Method.serializer().stringify(Method(this@invoke, params.toList()))
        if (debugInfo) println("input: $method")
        val jsonContent = "application/json"
        val response = httpClient.newCall(
                Request.Builder()
                        .url(requestUrl)
                        .method("POST", method.toRequestBody(jsonContent.toMediaTypeOrNull()))
                        .header("Content-Type", jsonContent)
                        .build()).execute()
        val responseContent = response.body?.string()
        response.close()
        if (responseContent == null) throw RuntimeException("no output, ${response.code}: ${response.message}")
        if (debugInfo) println("output: $responseContent")
        return MethodResult.serializer(serializer).parse(responseContent).tryResult()
    }

    private fun SECP256K1.Signature.txToPackArray(vararg actions: TxAction, nonce: Long, chainId: BigInteger) = messagePacker.apply {
        clear()
        val data = mapOf(
                "H".stringValue() to TxHeader(nonce).valueMap(),
                "Acts".stringValue() to actions.map { it.valueMap() }.arrayValue(),
                "V".stringValue() to (v().toInt().toBigInteger() + 35.toBigInteger() + chainId * bigIntegerTwo).bigintValue(),
                "R".stringValue() to r().bigintValue(),
                "S".stringValue() to s().bigintValue()).mapValue()
        packValue(mapOf("Data".stringValue() to data).mapValue())
    }.toByteArray()

    private fun txToPackArray(vararg actions: TxAction, nonce: Long, chainId: BigInteger) = messagePacker.apply {
        clear()
        packValue(ValueFactory.newArray(
                TxHeader(nonce).valueMap(),
                actions.map { it.valueMap() }.arrayValue(),
                chainId.bigintValue(),
                0.intValue(), 0.intValue()))
    }.toByteArray()

    fun toBcDecimal(value: BigInteger): BigDecimal = value.toBigDecimal().divide(bcDecimal)
    fun toBcInteger(value: BigDecimal): BigInteger = value.multiply(bcDecimal).toBigInteger()
    private fun ByteArray.signHashed(pk: SECP256K1.KeyPair = keyPair) = SECP256K1.signHashed(this, pk)
    private fun ByteArray.sha3() = Hash.keccak256(this)
    private fun Long.toBlockNumber() = if (this >= 0) toHex() else "latest"

}
