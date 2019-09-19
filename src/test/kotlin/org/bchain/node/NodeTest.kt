package org.bchain.node

import org.bchain.node.model.TransactionInfo
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

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
        val node = Node("192.168.2.246")
        val topNumber = node.getBlockTopNumber()
        val n = node.getBlockByNumber(303590L)
        println(n)
        assert(topNumber > 0)
    }

}
