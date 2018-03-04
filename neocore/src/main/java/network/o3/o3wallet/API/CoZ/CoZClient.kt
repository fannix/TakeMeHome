package network.o3.o3wallet.API.CoZ

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.Gson
import neowallet.Neowallet
import network.o3.o3wallet.API.NEO.NeoNodeRPC
import network.o3.o3wallet.Account
import network.o3.o3wallet.hexStringToByteArray
import network.o3.o3wallet.toHex

/**
 * Created by drei on 11/24/17.
 */

class CoZClient {
    // val baseAPIURL = "http://api.wallet.cityofzion.io/v2/address/"
    // use TESTNET for testing
    val baseAPIURL = "http://testnet-api.wallet.cityofzion.io/v2/address/" //TESTNET
    enum class Route() {
        HISTORY,
        CLAIMS,
        BALANCE;

        fun routeName(): String {
            return this.name.toLowerCase() + "/"
        }
    }

    fun getTransactionHistory(address: String, completion: (Pair<TransactionHistory?, Error?>) -> (Unit)) {
//        val url = "http://api.wallet.cityofzion.io/v2/address/history/" + address
        val url = "http://testnet-api.wallet.cityofzion.io/v2/address/history/" + address
        var request = url.httpGet()
        request.headers["User-Agent"] =  ""
        request.responseString { request, response, result ->
            val (data, error) = result
            print(response)
            if (error == null) {
                val gson = Gson()
                val history = gson.fromJson<TransactionHistory>(data!!)
                completion(Pair<TransactionHistory?, Error?>(history, null))
            } else {
                completion(Pair<TransactionHistory?, Error?>(null, Error(error.localizedMessage)))
            }
        }
    }

    fun getClaims(address: String, completion: (Pair<Claims?, Error?>) -> Unit) {
        val url = baseAPIURL + Route.CLAIMS.routeName() + address
        var request = url.httpGet()
        request.headers["User-Agent"] =  ""
        request.responseString { request, response, result ->
            val (data, error) = result
            if (error == null) {
                val gson = Gson()
                val history = gson.fromJson<Claims>(data!!)
                completion(Pair<Claims?, Error?>(history, null))
            } else {
                completion(Pair<Claims?, Error?>(null, Error(error.localizedMessage)))
            }
        }
    }

    fun getBalance(address: String, completion: (Pair<Assets?, Error?>) -> Unit) {
        val url = baseAPIURL + Route.BALANCE.routeName() + address
        var request = url.httpGet()
        request.headers["User-Agent"] =  ""
        request.timeout(6000000).responseString { request, response, result ->
            val (data, error) = result
            if (error == null) {
                val gson = Gson()
                val assets = gson.fromJson<Assets>(data!!)
                completion(Pair<Assets?, Error?>(assets, null))
            } else {
                completion(Pair<Assets?, Error?>(null, Error(error.localizedMessage)))
            }
        }
    }

}
