package network.o3.o3wallet

import neowallet.Neowallet
import org.junit.Test

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import network.o3.o3wallet.API.NEO.NeoNodeRPC
import network.o3.o3wallet.API.CoZ.CoZClient
import network.o3.o3wallet.API.O3.O3API
import java.nio.ByteBuffer

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class APIUnitTests {
    val testAddress = "AHa8Fk7Zyu2Vq3jYSuiHyCiNaibDiMsUMK"

    @Test
    fun getBlockCount() {
        var latch = CountDownLatch(1)

        NeoNodeRPC().getBlockCount {
            assert(it.first != null)
            assert(it.first!! > 0)
            print(it.first!!)
            latch.countDown()
        }
        latch.await(2000, TimeUnit.MILLISECONDS)
    }

    @Test
    fun getConnectionCount() {
        var latch = CountDownLatch(1)

        NeoNodeRPC().getConnectionCount {
            assert(it.first != null)
            assert(it.first!! > 0)
            print(it.first!!)
            latch.countDown()
        }
        latch.await(2000, TimeUnit.MILLISECONDS)
    }

    @Test
    fun getAccountState() {
        var latch = CountDownLatch(1)
        NeoNodeRPC().getAccountState(testAddress) {
            assert(it.first != null)
            print (it.first!!.toString())
            assert(it.first!!.balances[0].value > 0)
            latch.countDown()
        }
        latch.await(2000, TimeUnit.MILLISECONDS)
    }

    @Test
    fun validateAddress() {
        var latch = CountDownLatch(2)
        NeoNodeRPC().validateAddress(testAddress) {
            assert(it.first != null)
            assert(it.first!! == true)
            print("hello")
            latch.countDown()
        }

        NeoNodeRPC().validateAddress("dsnfjsanfjd") {
            assert(it.first != null)
            assert(it.first!! == false)
            latch.countDown()
        }
        latch.await(3000, TimeUnit.MILLISECONDS)
    }

    @Test
    fun getTransactionHistory() {
        var latch = CountDownLatch(1)
        CoZClient().getTransactionHistory(testAddress) {
            assert(it.first != null)
            print (it.first!!.toString())
            latch.countDown()
        }
        latch.await(2000, TimeUnit.MILLISECONDS)
    }

    @Test
    fun getPortfolio() {
        var latch = CountDownLatch(1)
        O3API().getPortfolio(2, 2.0, 15) {
            assert(it.first != null)
            print (it.first!!.toString())
            latch.countDown()
        }
        latch.await(20000, TimeUnit.MILLISECONDS)
    }

    @Test
    fun getPriceHistory() {
        var latch = CountDownLatch(1)
        O3API().getPriceHistory("NEO", 15) {
            assert(it.first != null)
            print (it.first!!.toString())
            latch.countDown()
        }
        latch.await(20000, TimeUnit.MILLISECONDS)
    }

    @Test
    fun getFeed() {
        var latch = CountDownLatch(1)
        O3API().getNewsFeed {
            assert(it.first != null)
            print (it.first!!.toString())
        }
        latch.await(2000000000, TimeUnit.MILLISECONDS)
    }

    @Test
    fun claim() {
        //testnet
        val wif = ""
        val wallet = Neowallet.generateFromWIF(wif)
        NeoNodeRPC().claimGAS(wallet) {
            var error = it.second
            assert(error == null)
            print(it.first.toString())
        }
    }

    @Test
    fun sendTransaction() {
        val wif = ""
        val wallet = Neowallet.generateFromWIF(wif)
        NeoNodeRPC().sendNativeAssetTransaction(wallet, NeoNodeRPC.Asset.GAS,1.0,wallet.address,null) {
            var error = it.second
            assert(error != null)
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
}
