package org.bchain.node

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class KVStoreTest {

    private val store by lazy {
        KVStoreHelper("0x736eea55743b8182b6823aca9bd6ca358824c0da",
            Node("118.190.245.106", privateKey = "29ef98425764f0ed3eb129df5875de029659ce4d49fccc9988cf114b7238cd71", debugInfo = true))
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
        val v = store.node.getTransactionReceiptByHash("0xea3d29cea7fef9e76d3fde988d4d5ff1c6140221cac7e27fc5fa2045715de4cd")
        println(v)
    }

    @Test
    fun testStore() {
        val v = store.setString("test2", """{"test":2}""")
        println(v)
       //  store.putString("test1", "")
    }

    @Test
    fun testRestore() {
        val v = store.getString("test2")
        println(v)
    }

}
