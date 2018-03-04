package network.o3.o3wallet.API.O3

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.core.FuelManager
import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.toJson
import com.google.gson.Gson
import network.o3.o3wallet.API.CoZ.Claims
import network.o3.o3wallet.API.CoZ.CoZClient
import network.o3.o3wallet.API.CoZ.TransactionHistory
import network.o3.o3wallet.API.NEO.NEP5Token
import network.o3.o3wallet.API.NEO.NEP5Tokens

/**
 * Created by drei on 11/24/17.
 */

class O3API {
    val baseURL = "http://staging-api.o3.network/v1/"
    enum class Route() {
        HISTORY,
        PORTFOLIO,
        FEED;

        fun routeName(): String {
            return this.name.toLowerCase()
        }
    }

    fun getPriceHistory(symbol: String, interval: Int, completion: (Pair<PriceHistory?, Error?>) -> (Unit)) {
        val url = baseURL + Route.HISTORY.routeName() + "/" + symbol + String.format("?i=%d", interval)
        var request = url.httpGet()
        request.responseString { request, response, result ->
            val (data, error) = result
            if (error == null) {
                val gson = Gson()
                val o3Response = gson.fromJson<O3Response>(data!!)
                println(o3Response.result["data"])
                val history = gson.fromJson<PriceHistory>(o3Response.result["data"])
                completion(Pair<PriceHistory?, Error?>(history, null))
            } else {
                completion(Pair<PriceHistory?, Error?>(null, Error(error.localizedMessage)))
            }
        }
    }

    fun getPortfolio(neoAmount: Int, gasAmount: Double, interval: Int, completion: (Pair<Portfolio?, Error?>) -> Unit) {
        val url = baseURL + Route.PORTFOLIO.routeName() + String.format("?i=%d&neo=%d&gas=%f", interval, neoAmount, gasAmount)
        var request = url.httpGet()
        request.responseString { request, response, result ->
           // print (request)
           // print (response)
            val (data, error) = result
            if (error == null) {
                val gson = Gson()
                val o3Response = gson.fromJson<O3Response>(data!!)
                println(o3Response.result["data"])
                val history = gson.fromJson<Portfolio>(o3Response.result["data"])
                completion(Pair<Portfolio?, Error?>(history, null))
            } else {
                completion(Pair<Portfolio?, Error?>(null, Error(error.localizedMessage)))
            }
        }
    }

    fun getNewsFeed(completion: (Pair<FeedData?, Error?>) -> Unit) {
        val url = "https://staging-api.o3.network/v1/feed/"/*baseURL + Route.FEED.routeName()*/
        url.httpGet().responseString { request, response, result ->
            val (data, error) = result
            if (error == null) {
                val gson = Gson()
                val o3Response = gson.fromJson<O3Response>(data!!)
                val feed = gson.fromJson<FeedData>(o3Response.result["data"])
                completion(Pair(feed, null))
            } else {
                completion(Pair(null, Error(error.localizedMessage)))
            }
        }
    }

    fun getAvailableNEP5Tokens(completion: (Pair<Array<NEP5Token>?, Error?>) -> Unit) {
        val url = "https://o3.network/settings/nep5.json"
        url.httpGet().responseString { request, response, result ->
            val (data, error) = result
            if (error == null) {
                val gson = Gson()
                val tokens = gson.fromJson<NEP5Tokens>(data!!)
                completion(Pair(tokens.nep5tokens, null))
            } else {
                completion(Pair(null, Error(error.localizedMessage)))
            }
        }
    }
}