package network.o3.o3wallet

import android.util.Log
import neowallet.Neowallet
import network.o3.o3wallet.API.CoZ.CoZClient
import network.o3.o3wallet.API.NEO.NeoNodeRPC
import network.o3.o3wallet.API.O3.O3API
import org.junit.Test
import org.junit.Assert
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class NeoAPITests {
    val testAddress = "AHa8Fk7Zyu2Vq3jYSuiHyCiNaibDiMsUMK"

    @Test
    fun getBlockCount() {
        var latch = CountDownLatch(1)

        NeoNodeRPC().getBlockCount {
            Assert.assertTrue(it.first != null)
            Assert.assertTrue(it.first!! > 0)
            print(it.first!!)
            latch.countDown()
        }
        latch.await(2000, TimeUnit.MILLISECONDS)
    }

    @Test
    fun getConnectionCount() {
        var latch = CountDownLatch(1)

        NeoNodeRPC().getConnectionCount {
            Assert.assertTrue(it.first != null)
            Assert.assertTrue(it.first!! > 0)
            print(it.first!!)
            latch.countDown()
        }
        latch.await(3000, TimeUnit.MILLISECONDS)
    }

    @Test
    fun getAccountState() {
        var latch = CountDownLatch(1)
        NeoNodeRPC().getAccountState(testAddress) {
            Assert.assertTrue(it.first != null)
            print (it.first!!.toString())
            Assert.assertTrue(it.first!!.balances[0].value > 0)
            latch.countDown()
        }
        latch.await(2000, TimeUnit.MILLISECONDS)
    }

    @Test
    fun validateAddress() {
        var latch = CountDownLatch(2)
        NeoNodeRPC().validateAddress(testAddress) {
            Assert.assertTrue(it.first != null)
            Assert.assertTrue(it.first!! == true)
            print("hello")
            latch.countDown()
        }

        NeoNodeRPC().validateAddress("dsnfjsanfjd") {
            Assert.assertTrue(it.first != null)
            Assert.assertTrue(it.first!! == false)
            latch.countDown()
        }
        latch.await(3000, TimeUnit.MILLISECONDS)
    }

    @Test
    fun getTransactionHistory() {
        var latch = CountDownLatch(1)
        CoZClient().getTransactionHistory(testAddress) {
            Assert.assertTrue(it.first != null)
            print (it.first!!.toString())
            latch.countDown()
        }
        latch.await(2000, TimeUnit.MILLISECONDS)
    }


    @Test
    fun claim() {
        //testnet
        val wif = "L4Ns4Uh4WegsHxgDG49hohAYxuhj41hhxG6owjjTWg95GSrRRbLL"
        val wallet = Neowallet.generateFromWIF(wif)
        NeoNodeRPC().claimGAS(wallet) {
            var error = it.second
            assert(error == null)
            print(it.first.toString())
        }
    }


    @Test
    fun testGetTokenBalance() {
        var latch = CountDownLatch(1)
        NeoNodeRPC().getTokenBalanceOf("ecc6b20d3ccac1ee9ef109af5a7cdb85706b1df9",address = "AJShjraX4iMJjwVt8WYYzZyGvDMxw6Xfbe") {
            print(it.first)
            latch.countDown()
        }
        latch.await(2000000000, TimeUnit.MILLISECONDS)
    }

    @Test
    fun testConvertHexStringByteArrayToInt() {
        var v = "00ab510d" //hex little endian
        var b = v.hexStringToByteArray().reversedArray()
        var amount = ByteBuffer.wrap(b).getInt()
        print(amount)
    }


    @Test
    fun testBuildInvocation() {
        val scriptHash = "ecc6b20d3ccac1ee9ef109af5a7cdb85706b1df9"
        val operation = "submit"
        val to = "5KfFjSyuuuNEMqvbGxxca8GNGDzDKyTK44NgSgZjVokqmQ4zY12"
        val info = ""
        val args = arrayOf(info, to.hash160().toString())

        val node = NeoNodeRPC()
        node.buildInvocationScript(scriptHash, operation, args)
        Assert.assertTrue(false)
    }

}