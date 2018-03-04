package network.o3.o3wallet.API.NEO

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class SendRawTransactionResponse(var jsonrpc: String, var id: Int, var result: Boolean)

data class NodeResponse(var jsonrpc: String, var id: Int, var result: JsonObject)
data class NodeResponsePrimitive(var jsonrpc: String, var id: Int, var result: JsonPrimitive)

data class ValidatedAddress(val address: String, val isvalid: Boolean)

data class Script(val invocation: String, val verification: String)

data class ValueIn(val transactionID: String, val valueOut: Int)

data class ValueOut(val n: Int, val asset: String, val value: String, val address: String)

//TODO FIGURE OUT HANDLING OF SERIALIZED NAMES
data class Transaction(val txid: String,
                  val size: Int,
                  val type: String,
                  val version: Int,
                  val vin: Array<ValueIn>,
                  val vout: Array<ValueOut>,
                  val sys_fee: String,
                  val net_fee: String,
                  val scripts: Array<Script>) {
}

data class Block(val confirmations: Int,
            val hash: String,
            val index: Int,
            val merkleroot: String,
            val nextblockhash: String,
            val nextconsensus: String,
            val nonce: String,
            val previousblockhash: String,
            val size: Int,
            val time: Int,
            val version: Int,
            val script: Script,
            val tx: Array<Transaction>
)

data class Balance(val asset: String,
                   val value: Double)

data class AccountState(val version: Int,
                        val script_hash: String,
                        val frozen: Boolean,
                        val votes: Array<Int>,
                        val balances: Array<Balance>)


data class Node(val url: String, val blockcount: Int, val peercount: Int)
data class NeoNetwork(val main: Array<Node>, val test: Array<Node>)


enum class AssetType {
	NATIVE,
	NEP5TOKEN;
}

data class AccountAsset(val assetID: String,
                        val name: String,
                        var value: Double,
                        val decimal: Int,
						var type: AssetType,
                        val symbol: String): Serializable

data class NEP5Token(val tokenHash: String,
                        val name: String,
                        val totalSupply: Double,
                        val decimal: Int,
                        val symbol: String)

data class NEP5Tokens(val nep5tokens: Array<NEP5Token>)



data class InvokeFunctionResponse(
		val state: String, //HALT, BREAK
		val gas_consumed: String, //0.338
		val stack: List<Stack>
)

data class Stack(
		@SerializedName("type")	val type: String, //ByteArray
		@SerializedName("value") val value: String //00ab510d //little endian
)