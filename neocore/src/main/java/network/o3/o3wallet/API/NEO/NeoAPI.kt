
package network.o3.o3wallet.API.NEO


import android.util.Log
import com.github.kittinunf.fuel.httpPost
import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import neowallet.Wallet
import network.o3.o3wallet.API.CoZ.*
import neowallet.Neowallet
import java.math.BigInteger
import android.R.array
import android.util.Size
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import network.o3.o3wallet.*
import org.json.JSONArray
import unsigned.toUByte
import java.nio.*


class NeoNodeRPC {
    //var nodeURL = "http://node1.o3.network:10332"

    val TAG = "NeoNodeRPC"

    var nodeURL = "http://seed2.neo.org:20332" //TESTNET
    enum class Asset() {
        NEO,
        GAS;

        fun assetID(): String {
            if (this == GAS) {
                return "602c79718b16e442de58778e148d0b1084e3b2dffd5de6b7b16cee7969282de7"
            } else if (this == NEO) {
                return "c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b"
            }
            return ""
        }
    }

    constructor(url: String = "http://seed2.neo.org:10332") {
        this.nodeURL = url
    }

    enum class RPC() {
        GETBLOCKCOUNT,
        GETCONNECTIONCOUNT,
        VALIDATEADDRESS,
        GETACCOUNTSTATE,
        SENDRAWTRANSACTION,
        INVOKEFUNCTION;

        fun methodName(): String {
            return this.name.toLowerCase()
        }
    }

    fun getBlockCount(completion: (Pair<Int?, Error?>) -> (Unit)) {
        val dataJson = jsonObject(
                "jsonrpc" to "2.0",
                "method" to RPC.GETBLOCKCOUNT.methodName(),
                "params" to jsonArray(),
                "id" to 1
        )

        var request = nodeURL.httpPost().body(dataJson.toString())
        println(RPC.GETBLOCKCOUNT.methodName())
        request.headers["Content-Type"] = "application/json"
        request.responseString { request, response, result ->
            print(result.component1())

            val (data, error) = result
            if (error == null) {
                val gson = Gson()
                val nodeResponse = gson.fromJson<NodeResponsePrimitive>(data!!)
                val blockCount = gson.fromJson<Int>(nodeResponse.result)
                completion(Pair<Int?, Error?>(blockCount, null))
            } else {
                completion(Pair<Int?, Error?>(null, Error(error.localizedMessage)))
            }
        }
    }

    fun getConnectionCount(completion: (Pair<Int?, Error?>) -> Unit) {
        val dataJson = jsonObject(
                "jsonrpc" to "2.0",
                "method" to RPC.GETCONNECTIONCOUNT.methodName(),
                "params" to jsonArray(),
                "id" to 1
        )

        var request = nodeURL.httpPost().body(dataJson.toString())
        request.headers["Content-Type"] = "application/json"
        request.responseString { request, response, result ->

            val (data, error) = result
            if (error == null) {
                val gson = Gson()
                val nodeResponse = gson.fromJson<NodeResponsePrimitive>(data!!)
                val blockCount = gson.fromJson<Int>(nodeResponse.result)
                completion(Pair<Int?, Error?>(blockCount, null))
            } else {
                completion(Pair<Int?, Error?>(null, Error(error.localizedMessage)))
            }
        }
    }

    fun getAccountState(address: String, completion: (Pair<AccountState?, Error?>) -> Unit) {
        val dataJson = jsonObject(
                "jsonrpc" to "2.0",
                "method" to RPC.GETACCOUNTSTATE.methodName(),
                "params" to jsonArray(address),
                "id" to 1
        )

        var request = nodeURL.httpPost().body(dataJson.toString())
        request.headers["Content-Type"] = "application/json"
        request.responseString { request, response, result ->

            val (data, error) = result
            if (error == null) {
                val gson = Gson()
                val nodeResponse = gson.fromJson<NodeResponse>(data!!)
                val block = gson.fromJson<AccountState>(nodeResponse.result)
                completion(Pair<AccountState?, Error?>(block, null))
            } else {
                Log.d("ERROR", error.localizedMessage)
                completion(Pair<AccountState?, Error?>(null, Error(error.localizedMessage)))
            }
        }
    }

    fun validateAddress(address: String, completion: (Pair<Boolean?, Error?>) -> Unit) {
        val valid = neowallet.Neowallet.validateNEOAddress(address)
        completion(kotlin.Pair<kotlin.Boolean?, Error?>(valid, null))
    }

    private fun sendRawTransaction(data: ByteArray, completion: (Pair<Boolean?, Error?>) -> Unit) {
        val dataJson = jsonObject(
                "jsonrpc" to "2.0",
                "method" to RPC.SENDRAWTRANSACTION.methodName(),
                "params" to jsonArray(data.toHex()),
                "id" to 15
        )

        var request = nodeURL.httpPost().body(dataJson.toString()).timeout(6000000)
        request.headers["Content-Type"] = "application/json"
        request.responseString { request, response, result ->
            val (data, error) = result
            print("send raw transaction")
            println(data!!)
            if (error == null) {
                val gson = Gson()
                try {
                    val nodeResponse = gson.fromJson<SendRawTransactionResponse>(data!!)
                    completion(Pair<Boolean?, Error?>(nodeResponse.result, null))
                } catch (error: Error) {
                    completion(kotlin.Pair<kotlin.Boolean?, Error?>(null, Error(error.localizedMessage)))
                }
            } else {
                completion(Pair<Boolean?, Error?>(null, Error(error.localizedMessage)))
            }
        }
    }


    fun claimGAS(wallet: Wallet, completion: (Pair<Boolean?, Error?>) -> (Unit)) {
        CoZClient().getClaims(wallet.address) {
            val claims = it.first
            var error = it.second
            if (error != null) {
                completion(Pair<Boolean?, Error?>(false, error))
            } else {
                val payload = generateClaimTransactionPayload(wallet, claims!!)
                sendRawTransaction(payload) {
                    var success = it.first
                    var error = it.second
                    completion(Pair<Boolean?, Error?>(success, error))
                }

            }
        }
    }

    fun sendNativeAssetTransaction(wallet: Wallet, asset: Asset, amount: Double, toAddress: String, attributes: Array<TransactionAttritbute>?, completion: (Pair<Boolean?, Error?>) -> (Unit)) {
        CoZClient().getBalance(wallet.address) {
            var assets = it.first
            var error = it.second
            if (error != null) {
                completion(Pair<Boolean?, Error?>(false, error))
            } else {
                val payload = generateSendTransactionPayload(wallet, asset, amount, toAddress, assets!!, attributes)
                sendRawTransaction(payload) {
                    var success = it.first
                    var error = it.second
                    completion(Pair<Boolean?, Error?>(success, error))
                }
            }
        }
    }

    private fun to8BytesArray(value: Int): ByteArray {
        return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()
    }

    private fun to8BytesArray(value: Long): ByteArray {
        return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(value).array()
    }

    data class SendAssetReturn(val totalAmount: Double?, val payload: ByteArray?, val error: Error?)
    data class TransactionAttritbute(val messaeg: String?)

    private fun getInputsNecessaryToSendAsset(asset: Asset, amount: Double, assets: Assets): SendAssetReturn {
        var sortedUnspents: List<Unspent>
        var neededForTransaction: MutableList<Unspent> = arrayListOf()

        if (asset == Asset.NEO) {
            if (assets.NEO.balance < amount) {
                return SendAssetReturn(null, null, Error("insufficient balance"))
            }
            sortedUnspents = assets.NEO.unspent.sortedBy { it.value }
        } else {
            if (assets.GAS.balance < amount) {
                return SendAssetReturn(null, null, Error("insufficient balance"))
            }
            sortedUnspents = assets.GAS.unspent.sortedBy { it.value }
        }
        var runningAmount = 0.0
        var index = 0
        var count: Int = 0
        //Assume we always have enough balance to do this, prevent the check for bal
        while (runningAmount < amount) {
            neededForTransaction.add(sortedUnspents[index])
            runningAmount += sortedUnspents[index].value
            index += 1
            count += 1
        }
        var inputData: ByteArray = byteArrayOf(count.toByte())
        for (t: Unspent in neededForTransaction) {
            val data = hexStringToByteArray(t.txid)
            val reversedBytes = data.reversedArray()
            inputData = inputData + reversedBytes + ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(t.index.toShort()).array()
        }
        return SendAssetReturn(runningAmount, inputData, null)
    }

    private fun packRawTransactionBytes(payloadPrefix: ByteArray, wallet: Wallet, asset: Asset, inputData: ByteArray,
                                        runningAmount: Double, toSendAmount: Double, toAddress: String,
                                        attributes: Array<TransactionAttritbute>?): ByteArray {
        var inputDataBytes = inputData
        val needsTwoOutputTransactions = runningAmount != toSendAmount

        var numberOfAttributes: Byte = 0x00.toByte()
        var attributesPayload: ByteArray = ByteArray(0)
        //TODO add attribute
//        if attributes != nil {
//            for (attribute in attributes!!) {
//            if attribute.data != nil {
//                attributesPayload = attributesPayload + attribute.data!
//                numberOfAttributes = numberOfAttributes + 1
//            }
//        }
//        }

        var payload: ByteArray = payloadPrefix + numberOfAttributes
        payload = payload + attributesPayload + inputDataBytes
        if (needsTwoOutputTransactions) {
            //Transaction To Reciever
            payload = payload + byteArrayOf(0x02.toByte()) + asset.assetID().hexStringToByteArray().reversedArray()
            val amountToSendInMemory: Int = (toSendAmount * 100000000).toInt()
            payload += to8BytesArray(amountToSendInMemory)
            //reciever addressHash
            payload += toAddress.hashFromAddress().hexStringToByteArray()
            //Transaction To Sender
            payload += asset.assetID().hexStringToByteArray().reversedArray()
            val amountToGetBackInMemory = (runningAmount * 100000000).toLong() - (toSendAmount * 100000000).toLong()
            payload += to8BytesArray(amountToGetBackInMemory)
            payload += wallet.hashedSignature

        } else {
            payload = payload + byteArrayOf(0x01.toByte()) + asset.assetID().hexStringToByteArray().reversedArray()
            val amountToSendInMemory = (toSendAmount * 100000000).toLong()
            payload += to8BytesArray(amountToSendInMemory)
            payload += toAddress.hashFromAddress().hexStringToByteArray()
        }
        return payload
    }

    private fun generateSendTransactionPayload(wallet: Wallet, asset: Asset, amount: Double, toAddress: String, assets: Assets, attributes: Array<TransactionAttritbute>?): ByteArray {
        var error: Error?
        val inputData = getInputsNecessaryToSendAsset(asset, amount, assets)
        val payloadPrefix = byteArrayOf(0x80.toUByte(), 0x00.toByte())
        val rawTransaction = packRawTransactionBytes(payloadPrefix, wallet,
                asset,
                inputData.payload!!,
                inputData.totalAmount!!,
                amount, toAddress, attributes)
        val privateKeyHex = wallet.privateKey.toHex()
        val signatureData = Neowallet.sign(rawTransaction, privateKeyHex)
        val finalPayload = concatenatePayloadData(wallet, rawTransaction, signatureData)
        return finalPayload
    }

    /*
    private fun genereateInvokeInputData(wallet: Wallet, assets: Assets): ByteArray {
       /* var payload: ByteArray = byteArrayOf(0xd1.toUByte()) //Invoke Transaction Type
        payload += byteArrayOf(0x00.toUByte()) // Version
        // TODO: Im making this a one input with absolute minimum gas cost since I only care about NEP-5 Transfer
        // TODO: In the future this needs to be expanded to allow for more generic smart contracts
        val inputCount = 0x01.toUByte()
        payload += byteArrayOf(inputCount)
        for (unspent in assets.GAS.unspent)*/
    }*/


    private fun generateInvokeTransactionPayload(wallet: Wallet, assets: Assets, script: String, contractAddress: String): ByteArray {
        // need to send more than 5 gas, otherwise the transaction will fail
        val inputData = getInputsNecessaryToSendAsset(NeoNodeRPC.Asset.GAS, 5.00000001, assets)
        val payloadPrefix = byteArrayOf(0xd1.toUByte(), 0x00.toUByte()) + script.hexStringToByteArray()
        var rawTransaction = packRawTransactionBytes(payloadPrefix, wallet, Asset.GAS,
                inputData.payload!!,
                inputData.totalAmount!!, 5.00000001,
                // Account is null, which address to use?
                wallet.address!!,
               null)

        val privateKeyHex = wallet.privateKey.toHex()
        val signature = Neowallet.sign(rawTransaction, privateKeyHex)
        var finalPayload = concatenatePayloadData(wallet, rawTransaction, signature)
        finalPayload = finalPayload + contractAddress.hexStringToByteArray()
        return finalPayload

    }

    private fun concatenatePayloadData(wallet: Wallet, txData: ByteArray, signatureData: ByteArray): ByteArray {
        var payload = txData + byteArrayOf(0x01.toByte())                        // signature number
        payload += byteArrayOf(0x41.toByte())                              // signature struct length
        payload += byteArrayOf(0x40.toByte())                                 // signature data length
        payload += signatureData                   // signature
        payload += byteArrayOf(0x23.toByte())                                 // contract data length
        payload = payload + byteArrayOf(0x21.toByte()) + wallet.publicKey + byteArrayOf(0xac.toByte()) // NeoSigned publicKey
        return payload
    }

    private fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    private fun generateClaimInputData(wallet: Wallet, claims: Claims): ByteArray {
        var payload: ByteArray = byteArrayOf(0x02.toByte()) // Claim Transaction Type
        payload += byteArrayOf(0x00.toByte()) // Version
        val claimsCount = claims.claims.count().toByte()
        payload += byteArrayOf(claimsCount)
        for (claim: Claim in claims.claims) {
            payload += hexStringToByteArray(claim.txid).reversedArray()
            payload += ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(claim.index.toShort()).array()
        }
        payload += byteArrayOf(0x00.toByte()) // Attributes
        payload += byteArrayOf(0x00.toByte()) // Inputs
        payload += byteArrayOf(0x01.toByte()) // Output Count
        payload += hexStringToByteArray(NeoNodeRPC.Asset.GAS.assetID()).reversedArray()
        payload += ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(claims.total_claim).array()
        payload += wallet.hashedSignature

        return payload
    }

    private fun generateClaimTransactionPayload(wallet: Wallet, claims: Claims): ByteArray {
        val rawClaim = generateClaimInputData(wallet, claims)
        val privateKeyHex = wallet.privateKey.toHex()
        val signature = Neowallet.sign(rawClaim, privateKeyHex)
        val finalPayload = concatenatePayloadData(wallet, rawClaim, signature)
        return finalPayload
    }


    fun getTokenBalanceOf(tokenHash: String, address: String, completion: (Pair<Long?, Error?>) -> Unit) {

        var params: ArrayList<Any> = arrayListOf<Any>()
        params.add(tokenHash)
        params.add("balanceOf")
        var invokeFunctionParams: ArrayList<Any> = arrayListOf()
        //var stack = Stack(type = "Hash160",value = address.hash160().toString())
        var stack = JsonObject()
        stack.set("type", "Hash160")
        stack.set("value", address.hash160().toString())
        invokeFunctionParams.add(stack)
        params.add(jsonArray(invokeFunctionParams))

        val dataJson = jsonObject(
                "jsonrpc" to "2.0",
                "method" to RPC.INVOKEFUNCTION.methodName(),
                "params" to jsonArray(params),
                "id" to 1
        )

        var request = nodeURL.httpPost().body(dataJson.toString())
        request.headers["Content-Type"] = "application/json"
        request.responseString { request, response, result ->
            val (data, error) = result
            if (error == null) {
                val gson = Gson()
                Log.w(TAG, data)
                val nodeResponse = gson.fromJson<NodeResponse>(data!!)
                val invokeResponse = gson.fromJson<InvokeFunctionResponse>(nodeResponse.result)
                if (invokeResponse.stack.count() > 0) {
                    val stack = invokeResponse.stack[0]
                    var amount: Long = 0
                    if (stack.value.isNotEmpty()) {
                        amount = stack.value.littleEndianHexStringToInt64()
                    }

                    completion(Pair<Long?, Error?>(amount, null))
                } else {
                    completion(Pair<Long?, Error?>(0, null))
                }
            } else {
                completion(Pair<Long?, Error?>(null, Error(error.localizedMessage)))
            }
        }
    }

    fun buildNEP5TransferScript(scriptHash: String, fromAddress: String, toAddress: String, amount: Double): ByteArray {
        val amountToSendInMemory: Int = (amount * 100000000).toInt()
        val fromAddressHash = fromAddress.hashFromAddress()
        val toAddressHash = toAddress.hashFromAddress()
        val scriptBuilder = ScriptBuilder()
        scriptBuilder.pushContractInvoke(scriptHash, operation = "transfer",
                args = arrayOf(amountToSendInMemory, toAddressHash, fromAddressHash)
        )
        var script = scriptBuilder.getScriptHexString()
        return byteArrayOf((script.length / 2).toUByte()) + script.hexStringToByteArray()
    }

    // Args: scriptHash -> Contract Address of NEP-5 Token to Transfer
    // fromAddress -> Address of Sender
    // toAddress -> Address of Recipient
    // transfer amount *
    fun sendNEP5Token(wallet: Wallet, tokenContractHash: String, fromAddress: String, toAddress: String, amount: Double,
                          completion: (Pair<Boolean?, Error?>) -> Unit) {
        CoZClient().getBalance(wallet.address) {
            var assets = it.first
            var error = it.second
            if (error != null) {
                completion(Pair<Boolean?, Error?>(false, error))
                return@getBalance
            } else {
                val scriptBytes = buildNEP5TransferScript(tokenContractHash, fromAddress, toAddress, amount)
                val scriptBytesString = scriptBytes.toHex()
                val finalPayload = generateInvokeTransactionPayload(wallet, assets!!, scriptBytes.toHex(), tokenContractHash)
                val finalPayloadString = finalPayload.toHex()
                sendRawTransaction(finalPayload) {
                    var success = it.first
                    var error = it.second
                    completion(Pair<Boolean?, Error?>(success, error))
                }
            }
        }
    }

    fun buildInvocationScript(scriptHash: String, operation: String, args: Any?): ByteArray {
        val scriptBuilder = ScriptBuilder()
        scriptBuilder.pushContractInvoke(scriptHash, operation, args)
        var script = scriptBuilder.getScriptHexString()
        // the length of array is prefix
        return byteArrayOf((script.length / 2).toUByte()) + script.hexStringToByteArray()
    }


    fun invoke(wallet: Wallet, scriptHash: String,
               operation: String, args: Any?,
               completion: (Pair<Boolean?, Error?>) -> Unit) {
        CoZClient().getBalance(wallet.address) {
            var assets = it.first
            var error = it.second
            Log.d("invoke", it.toString())
            if (error != null) {
                completion(Pair<Boolean?, Error?>(false, error))
                return@getBalance
            } else {
                val scriptBytes = buildInvocationScript(scriptHash, operation, args)
                val scriptBytesString = scriptBytes.toHex()
                val finalPayload = generateInvokeTransactionPayload(wallet, assets!!, scriptBytes.toHex(), scriptHash)
                val finalPayloadString = finalPayload.toHex()
                Log.d("script", scriptBytesString)
                Log.d("payload", finalPayloadString)

                sendRawTransaction(finalPayload) {
                    var success = it.first
                    var error = it.second
                    completion(Pair<Boolean?, Error?>(success, error))
                }
            }
        }
    }


}