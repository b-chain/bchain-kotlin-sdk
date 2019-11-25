package org.bchain.node

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class KVStoreTest {

    private val store by lazy {
        KVStoreHelper("0x8215d367101fca2b04c121588f8e7f8b1bb01d3e",
            Node("118.190.245.106", privateKey = "29ef98425764f0ed3eb129df5875de029659ce4d49fccc9988cf114b7238cd71", debugInfo = true))
    }


    @Test
    fun testCreateStore() {
        val bytes = NodeTest::class.java.getResourceAsStream("/kvstore.wasm").use { it.readBytes() }
        val result = store.node.createWasamContract(bytes)
        // 0xf238256a9fc1a599748f2de5d30c18c0f55c8359a4a62d43d4562801ffbbad72
        println("create contract: $result")
    }

    @Test
    fun testGold() {
        val v = store.node.getTransactionReceiptByHash("0xf9ba1dfaaddf21ca0c4b1d798bf859edc351b60b060b4840aaff56cd460c0975")
        println(v)
    }

    @Test
    fun testStore() {
        val v = store.setString("test1", """{"test":1}""")
        println(v)
       //  store.putString("test1", "")
    }

    @Test
    fun testRestore() {
        val v = store.getString("test1")
        println(v)
    }

}
