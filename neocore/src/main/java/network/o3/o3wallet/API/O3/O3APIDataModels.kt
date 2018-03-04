package network.o3.o3wallet.API.O3

import com.google.gson.JsonArray
import com.google.gson.JsonObject

/**
 * Created by drei on 11/24/17.
 */

data class O3Response(var code: Int, var result: JsonObject)


data class PriceData (val currency: String,
                      val average: Double,
                      val averageUSD: Double,
                      val averageBTC: Double,
                      val time: String)

data class PriceHistory(val symbol: String,
                        val currency: String,
                        val data: Array<PriceData>)

data class Portfolio(val price: Map<String, PriceData>,
                          val firstPrice: Map<String, PriceData>,
                          val data: Array<PriceData>)

data class FeedData(val features: JsonArray, val items: Array<FeedItem>)

data class NewsImage(val title: String, val url: String)

data class FeedItem(val title: String,
                    val description: String,
                    val link: String,
                    val published: String,
                    val source: String,
                    val images: Array<NewsImage>)
