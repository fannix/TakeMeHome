package fannix.com.takemehome

import io.nayuki.bitcoin.crypto.Ripemd160
import network.o3.o3wallet.toHash160
import network.o3.o3wallet.toHex

import org.junit.Test
import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testToHexString() {
        assertEquals("41", API.toHex("A"));
        assertEquals("41", API.toHex("22"));
    }

    @Test
    fun testToHash160() {
        toHash160("ASfFNunqtv73UDidxz5Z5fi5fYHAZb3NrQ");
    }

    @Test
    fun testRipemd160() {
        val arr = Ripemd160.getHash("ID1: 0000-0000-00000".toByteArray())
        println(arr.toHex());
    }
}
