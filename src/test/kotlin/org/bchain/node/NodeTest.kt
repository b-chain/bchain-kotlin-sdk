package org.bchain.node

import org.bchain.node.model.TransactionInfo
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.math.BigDecimal

@RunWith(JUnit4::class)
class NodeTest {

    @Test
    fun testSerializable() {
        val content = """
            {
            "blockHash":"0x9275cc1f5414deb5d5df0adda8ffae44b85c2bc722ca18d5c016a126c836d818",
            "blockNumber":null,
            "from":"0xb3726e0fed2b7eaaa609b77ec507f26d5f8fe561",
            "hash":"0x21a8bc28d02a34c3fb2aaf22a2e4de3593841255246b08526ea90c810c54f501",
            "nonce":"0x1f52",
            "transactionIndex":"0x17",
            "actions":[
            {
            "address":"0x26ea1c5a38bb48bd58e62a5fa3ff06c9e328855e",
            "params":"0x7b2266756e635f6e616d65223a227472616e73666572222c2261726773223a5b7b2274797065223a2261646472657373222c2276616c223a224d4867784d324d7a4e4755315a6a6c694f4451784e7a59325a574a6b4d7a4e6a596a51335a544e6c5a57526b5a6a4a6859324d345a57457941413d3d227d2c7b2274797065223a2261646472657373222c2276616c223a224d5455774d4441774d44417741413d3d227d2c7b2274797065223a2261646472657373222c2276616c223a225256524941413d3d227d2c7b2274797065223a2261646472657373222c2276616c223a22564649784f5441304d6a4d784e7a45784d5452454d4456424e6a64454d444d30526b45784e6b553341413d3d227d2c7b2274797065223a22696e743634222c2276616c223a2243516b43414141414141413d227d2c7b2274797065223a22696e743332222c2276616c223a225a41414141413d3d227d5d7d"
            }],
            "v":"0x26",
            "r":"0x65452c8a792f10946b9b68b55d9ea9694651441aa6ef3e7d749ac883186a9a5a",
            "s":"0x17fcf5bb174d0797d052c5657587678bbdffb6037cc6691985a2880c11659d2f"}
        """.trimIndent()
        val s = TransactionInfo.serializer().parse(content)
        println(s)
    }

    @Test
    fun testNode() {
        val node = Node("192.168.2.33", privateKey = "29ef98425764f0ed3eb129df5875de029659ce4d49fccc9988cf114b7238cd71", debugInfo = true)
        val topNumber = node.getBlockTopNumber()
        /*
        val n = node.getBlockByNumber(304644)
        val d = n.transactions.first().actions.first().parameter.arguments.first().addressValue()
        println(d)
         */
        assert(topNumber > 0)
        val bc = node.getBcBalance()
        println("${node.bchainAddress}:$bc")
        // val test = node.getTransactionByHash("0x19c55de70d527a5c19dfcc3c07d991cdaf07fe52301a51d5dceb9e6d2551f1c1")
        val test = node.getTransactionByHash("0x704336f3ee9ac6b6dffb7261e34dd2b9d515ff9436ebbbd0740e51e2da3b1ab9")
        println(test.actions.first().parameter)
        /*
        if (bc > BigDecimal.ZERO) {
            println(node.transferBc("0x840bba84322becb61b75366a3c579d9d5f53cc3c", BigDecimal.ONE))
        }
         */
    }

    @Test
    fun testBalance() {
        val node = Node("192.168.2.246", privateKey = "7ea3b94a9113434ca0cb4d7c280390a4be2e6accb6340c89513111d7559d288b", debugInfo = true)
        val bt = BigTokenHelper("0x6cfd090a741a12014fac9a65facd990b5e0af135", node)
        val v = bt.balanceOf("KING", "0xccf2bd1134ab2b408cd59127d4cda89886849139")
        println(v)
    }

    @Test
    fun testCreateContract() {
        val node = Node("192.168.2.33", privateKey = "7ea3b94a9113434ca0cb4d7c280390a4be2e6accb6340c89513111d7559d288b", debugInfo = true)
        val bytes = NodeTest::class.java.getResourceAsStream("/props.wasm").use { it.readBytes() }
        val result = node.createWasamContract(bytes)
        // 0xf238256a9fc1a599748f2de5d30c18c0f55c8359a4a62d43d4562801ffbbad72
        println("create contract: $result")
    }

    @Test
    fun testPropertyContract() {
        val hash = "0xf238256a9fc1a599748f2de5d30c18c0f55c8359a4a62d43d4562801ffbbad72"
        val propertyContract = "0x783719d53232f53570a63ea41da4be1669b4a2fd"
        val node = Node("127.0.0.1", privateKey = "7ea3b94a9113434ca0cb4d7c280390a4be2e6accb6340c89513111d7559d288b", debugInfo = true)
        val info = node.getTransactionReceiptByHash(hash)
        assert(info.contractAddress.first() == propertyContract)
        val propertyName = "test"
        val kingContract = "0xf053a7a6d62a8b1120e7c9a6177d411f9f3ffaac"
        val helper = PropertyHelper(propertyContract, node)
        // helper.buyByBc(propertyName, 0.1.toBigDecimal())
        assert(helper.hasProperty(propertyName))
        assert(!helper.hasProperty("none"))
    }
    
}
