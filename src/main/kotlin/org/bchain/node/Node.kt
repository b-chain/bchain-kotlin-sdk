package org.bchain.node

import kotlinx.serialization.KSerializer
import kotlinx.serialization.internal.ArrayListSerializer
import kotlinx.serialization.internal.IntSerializer
import kotlinx.serialization.internal.StringSerializer
import kotlinx.serialization.json.JsonObject
import net.consensys.cava.crypto.Hash
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.msgpack.value.ValueFactory
import java.math.BigInteger
import java.security.*
import net.consensys.cava.crypto.SECP256K1
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.bchain.node.model.*
import org.bchain.node.serializer.StringChangeEndianBigIntSerialization
import org.bchain.node.serializer.StringLongSerialization
import java.io.Closeable
import java.lang.Exception
import java.lang.RuntimeException
import java.math.BigDecimal

class Node(host: String = "127.0.0.1", port: Int = 8989, wsPort: Int = 8990, privateKey: String? = null, private var debugInfo: Boolean = false) {

    companion object {
        init {
            Security.addProvider(BouncyCastleProvider())
        }

        const val bcContractAddress = "0xb78f12Cb3924607A8BC6a66799e159E3459097e9"
        const val systemContractAddress = "0x2ba8A6318fb0390e8693af78c8086C086D923A96"
        const val bcDecimalScale = 8
        val bcDecimal: BigDecimal = BigDecimal.TEN.pow(bcDecimalScale)
        val bigIntegerTwo = 2.toBigInteger()
    }

    // private val messagePacker by lazy { MessagePack.newDefaultBufferPacker() }
    private val requestUrl = "${if (host.contains(":/")) "" else "http://"}$host:$port"
    private val requestWsUrl = "${if (host.contains(":/")) "" else "http://"}$host:$wsPort"

    private lateinit var keyPair: SECP256K1.KeyPair

    lateinit var bchainAddress: String
        private set

    private val httpClient by lazy { OkHttpClient.Builder().build() }

    private val openingWebSockets = mutableMapOf<WebSocket, MutableMap<String, BlockEventListener>>()

    init { if (privateKey != null) updatePrivateKey(privateKey) }

    fun createBlockEventListener(callback: SubscribeSuccessCallback? = null, event: SubscribeBlockInfoCallback) = createEventSubscribeListener("newHeads", subscribeSuccess = {
        callback?.onSubscribeSuccess(it)
    }) { event.onNewBlock(SubscribeBlockInfo.serializer().parse(it.toString())) }

    fun createPendingEventListener(callback: SubscribeSuccessCallback? = null, event: SubscribeTransactionInfoCallback) = createEventSubscribeListener("newPendingTransactions", subscribeSuccess = {
        callback?.onSubscribeSuccess(it)
    }) { event.onNewTransaction(TransactionInfo.serializer().parse(it.toString())) }

    fun createEventSubscribeListener(vararg subscribes: String, subscribeSuccess: (Closeable) -> Unit, process: (JsonObject) -> Unit): Closeable {
        val webSocket = openingWebSockets.entries.firstOrNull()
        val method = Method("bchain_subscribe", subscribes.toList())
        val w = webSocket?.key?.apply { send(Method.serializer().stringify(method)) }
                ?: httpClient.newWebSocket(Request.Builder().url(requestWsUrl).build(), object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        super.onOpen(webSocket, response)
                        webSocket.send(Method.serializer().stringify(method))
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        super.onMessage(webSocket, text)
                        val id = try { MethodResult.serializer(StringSerializer).parse(text).tryResult() } catch (ignore: Exception) { null }
                        if (id != null) {
                            var map = openingWebSockets[webSocket]
                            if (map == null) {
                                map = mutableMapOf()
                                openingWebSockets[webSocket] = map
                            }
                            val c = BlockEventListener(process, id, webSocket)
                            map[id] = c
                            subscribeSuccess(c)
                        } else {
                            val map = openingWebSockets[webSocket]
                            if (map != null) {
                                val result = SubscribeMethodResult.serializer().parse(text)
                                map[result.params.subscription]?.process?.invoke(result.params.result)
                            }
                        }
                    }

                    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                        super.onClosing(webSocket, code, reason)
                        openingWebSockets.remove(webSocket)
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        super.onFailure(webSocket, t, response)
                        throw t
                    }

                })
        return Closeable { w.cancel() }
    }

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

    fun createWasamContract(content: ByteArray, fee: BigDecimal = BigDecimal.ZERO, nonce: Long = -1, chainId: BigInteger = BigInteger.ONE): String {
        val actions = mutableListOf(systemContractAddress to TxParameter.createContract(TxContract("wasmre.WasmRE", content)))
        if (fee > BigDecimal.ZERO) actions.add(0, bcContractAddress to TxParameter.bcTransferFee(toBcInteger(fee)))
        return transfer(*actions.toTypedArray(), nonce = nonce, chainId = chainId)
    }

    fun transferBc(toAddress: String,
                           value: BigDecimal,
                           memo: String = "",
                           fee: BigDecimal = BigDecimal.ZERO,
                           nonce: Long = -1,
                           chainId: BigInteger = BigInteger.ONE) = multipleTransferBc(BcTransferParameter(toAddress, value, memo), fee = fee, nonce = nonce, chainId = chainId)

    fun multipleTransferBc(vararg params: BcTransferParameter, fee: BigDecimal = BigDecimal.ZERO, nonce: Long = -1, chainId: BigInteger = BigInteger.ONE): String {
        if (fee < BigDecimal.ZERO) throw InvalidParameterException("fee must >= 0.")
        if (params.isEmpty()) throw InvalidParameterException("no params")
        val actions = params.map {
            if (it.toAddress.isBlank()) throw InvalidParameterException("target address cannot be blank.")
            if (it.memo.toByteArray().size >= 32) throw InvalidParameterException("memo must < 32 bytes.")
            if (it.amount <= BigDecimal.ZERO) throw InvalidParameterException("value must > 0.")
            bcContractAddress to TxParameter.bcTransfer(it.toAddress, toBcInteger(it.amount), it.memo)
        }
        val array = (if (fee > BigDecimal.ZERO) listOf(bcContractAddress to TxParameter.bcTransferFee(toBcInteger(fee))).plus(actions) else actions).toTypedArray()
        return transfer(*array, nonce = nonce, chainId = chainId)
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

    fun getBcBalance(address: String = bchainAddress) = toBcDecimal(callAction(bcContractAddress, TxParameter("balenceOf", listOf(address.txArgument())), serializer = StringChangeEndianBigIntSerialization).first())

    fun <T> callAction(contractAddress: String, parameter: TxParameter, number: Long = -1, serializer: KSerializer<T>): List<T> = "bchain_actionCall".invoke(ArrayListSerializer(serializer), contractAddress.action(parameter), number.toBlockNumber())

    fun getProtocolVersion(): Int = "bchain_protocolVersion".invoke(IntSerializer)

    fun getTxPoolStatus(): TxPoolStatus = "txpool_status".invoke(TxPoolStatus.serializer())

    fun getAccounts(): List<String> = "personal_listAccounts".invoke(ArrayListSerializer(StringSerializer))

    fun createAccount(password: String): String = "personal_newAccount".invoke(StringSerializer, password)

    fun importAccount(privateKey: String, password: String): String = "personal_importRawKey".invoke(StringSerializer, privateKey, password)

    fun getBlockTopNumber(): Long = "bchain_blockNumber".invoke(StringLongSerialization)

    fun getBlockByNumber(number: Long = -1): BlockInfo = "bchain_getBlockByNumber".invoke(BlockInfo.serializer(), number.toBlockNumber(), true)

    fun getSimpleBlockByNumber(number: Long = -1): SimpleBlockInfo = "bchain_getBlockByNumber".invoke(SimpleBlockInfo.serializer(), number.toBlockNumber(), false)

    fun getBlockByHash(hash: String): BlockInfo = "bchain_getBlockByHash".invoke(BlockInfo.serializer(), hash, true)

    fun getSimpleBlockByHash(hash: String): SimpleBlockInfo = "bchain_getBlockByHash".invoke(SimpleBlockInfo.serializer(), hash, false)

    fun getAccountNonce(address: String = bchainAddress, number: Long = -1): Long = "bchain_getAccountNonce".invoke(StringLongSerialization, address, number.toBlockNumber())

    fun getTransactionByHash(transactionHash: String): TransactionInfo = "bchain_getTransactionByHash".invoke(TransactionInfo.serializer(), transactionHash)

    fun getTransactionReceiptByHash(transactionHash: String): TransactionReceiptInfo = "bchain_getTransactionReceipt".invoke(TransactionReceiptInfo.serializer(), transactionHash)

    fun toBcDecimal(value: BigInteger): BigDecimal = value.toBigDecimal().divide(bcDecimal)

    fun toBcInteger(value: BigDecimal): BigInteger = value.multiply(bcDecimal).toBigInteger()

    private fun txToPackArray(vararg actions: TxAction, nonce: Long, chainId: BigInteger) = serializeByMessagePack {
        packValue(ValueFactory.newArray(
                TxHeader(nonce).valueMap(),
                actions.map { it.valueMap() }.arrayValue(),
                chainId.bigintValue(),
                0.intValue(), 0.intValue()))
    }

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

    private fun SECP256K1.Signature.txToPackArray(vararg actions: TxAction, nonce: Long, chainId: BigInteger) = serializeByMessagePack {
        val data = mapOf(
                "H".stringValue() to TxHeader(nonce).valueMap(),
                "Acts".stringValue() to actions.map { it.valueMap() }.arrayValue(),
                "V".stringValue() to (v().toInt().toBigInteger() + 35.toBigInteger() + chainId * bigIntegerTwo).bigintValue(),
                "R".stringValue() to r().bigintValue(),
                "S".stringValue() to s().bigintValue()).mapValue()
        packValue(mapOf("Data".stringValue() to data).mapValue())
    }

    private fun ByteArray.signHashed(pk: SECP256K1.KeyPair = keyPair) = SECP256K1.signHashed(this, pk)
    private fun ByteArray.sha3() = Hash.keccak256(this)
    private fun Long.toBlockNumber() = if (this >= 0) toHex() else "latest"

    data class BcTransferParameter(val toAddress: String, val amount: BigDecimal, val memo: String)

    private inner class BlockEventListener(val process: (JsonObject) -> Unit, val id: String, val webSocket: WebSocket): Closeable {

        override fun close() {
            val map = openingWebSockets[webSocket]?.apply { remove(id) }
            if (map == null || map.isEmpty()) {
                webSocket.cancel()
                openingWebSockets.remove(webSocket)
            }
        }

    }

    interface SubscribeSuccessCallback {
        fun onSubscribeSuccess(subscribe: Closeable)
    }

    interface SubscribeBlockInfoCallback {
        fun onNewBlock(info: SubscribeBlockInfo)
    }

    interface SubscribeTransactionInfoCallback {
        fun onNewTransaction(info: TransactionInfo)
    }

}
