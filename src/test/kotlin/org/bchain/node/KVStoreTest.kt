package org.bchain.node

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class KVStoreTest {

    private val store by lazy {
        KVStoreHelper("0x157b4058372443db579f32a70131eb760f8397d8",
            Node("118.190.245.106", privateKey = "29ef98425764f0ed3eb129df5875de029659ce4d49fccc9988cf114b7238cd71", debugInfo = true))
    }

    @Test
    fun testBlockInfo() {
        val r = store.node.getSimpleBlockByNumber(20)
        println(r)
    }

    @Test
    fun testCreateStore() {
        val bytes = NodeTest::class.java.getResourceAsStream("/database.wasm").use { it.readBytes() }
        val result = store.node.createWasamContract(bytes)
        // 0xf238256a9fc1a599748f2de5d30c18c0f55c8359a4a62d43d4562801ffbbad72
        println("create contract: $result")
    }

    @Test
    fun testGold() {
        val v = store.node.getTransactionReceiptByHash("0xdfdff14664682382d394f41ad5d1ba273952e45f2eb495797e996f4b6c145b46")
        println(v)
    }

    @Test
    fun testStore() {
        val v = store.multipleSet(
                listOf(
                        "test1" to """{"test":1}""".toByteArray(Charsets.UTF_8),
                        "test2" to """{"test":2}""".toByteArray(Charsets.UTF_8),
                        "test3" to """{"test":3}""".toByteArray(Charsets.UTF_8)))
        println(v)
    }

    @Test
    fun testRestore() {
        val keys = listOf("test1", "test2", "test3")
        keys.forEach { println("$it = ${store.getString(it)}") }
    }

}
