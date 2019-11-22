package org.bchain.node

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class KVStoreTest {

    private val store by lazy { KVStoreHelper("0x783719d53232f53570a63ea41da4be1669b4a2fd", Node("dev.cqultra.com", privateKey = "29ef98425764f0ed3eb129df5875de029659ce4d49fccc9988cf114b7238cd71", debugInfo = true)) }

    @Test
    fun testStore() {
        store.putString("test1", "")
    }

    @Test
    fun testRestore() {

    }

}
