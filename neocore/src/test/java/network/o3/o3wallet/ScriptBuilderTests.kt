package network.o3.o3wallet

import android.util.Log
import network.o3.o3wallet.API.NEO.OPCODE
import network.o3.o3wallet.API.NEO.ScriptBuilder
import org.junit.Test

/**
 * Created by drei on 1/19/18.
 */

class ScriptBuilderTests {
    @Test
    fun testScriptBuilder() {

        var testCases = arrayOf(
                hashMapOf("script" to "00c1046e616d65675f0e5a86edd8e1f62b68d2b3f7c0a761fc5a67dc",
                        "scriptHash" to "dc675afc61a7c0f7b3d2682bf6e1d8ed865a0e5f",
                        "operation" to  "name",
                        "args" to  arrayOf<String>()
                ),
                hashMapOf("script" to "000673796d626f6c6711c4d1f4fba619f2628870d36e3a9773e874705b",
                        "scriptHash" to "5b7074e873973a6ed3708862f219a6fbf4d1c411",
                        "operation" to  "symbol",
                        "args" to null
                ),
                hashMapOf("script" to "0008646563696d616c736711c4d1f4fba619f2628870d36e3a9773e874705b",
                        "scriptHash" to "5b7074e873973a6ed3708862f219a6fbf4d1c411",
                        "operation" to  "decimals",
                        "args" to false
                ),
                hashMapOf("script" to "205fe459481de7b82f0636542ffe5445072f9357a1261515d6d3173c07c762743b51c10962616c616e63654f666711c4d1f4fba619f2628870d36e3a9773e874705b",
                        "scriptHash" to "5b7074e873973a6ed3708862f219a6fbf4d1c411",
                        "operation" to  "balanceOf",
                        "args" to arrayOf<String>("5fe459481de7b82f0636542ffe5445072f9357a1261515d6d3173c07c762743b")
                ),
                hashMapOf("script" to "5767b7040c106561763ce38c0ce658a946e5d1b381db",
                        "scriptHash" to "db81b3d1e546a958e60c8ce33c766165100c04b7",
                        "operation" to  null,
                        "args" to 7
                ),
                hashMapOf("script" to "00046e616d6567f91d6b7085db7c5aaf09f19eeec1ca3c0db2c6ec",
                        "scriptHash" to "ecc6b20d3ccac1ee9ef109af5a7cdb85706b1df9",
                        "operation" to  "name",
                        "args" to null
                )
        )


        val neoScript = ScriptBuilder()
        for (testCase in testCases) {
            val script = testCase["script"] as String
            val scriptHash = testCase["scriptHash"] as String
            val operation = testCase["operation"] as String?
            val args = testCase["args"]
            neoScript.pushContractInvoke(scriptHash, operation, args)
            val neoScriptString = neoScript.getScriptHexString()
            if (neoScriptString.toLowerCase() != script) {
                throw Exception("Test not passed: ${neoScriptString.toLowerCase()} != $script")
            }
            neoScript.resetScript()
        }
    }

    @Test
    fun testIntegerScriptBuilding() {
        var testCases = hashMapOf(
                -1 to "4f",
                0 to "00",
                13 to "5d",
                500 to "02f401",
                65536 to "0300000100000000")
        val neoScript = ScriptBuilder()
        for (key in testCases.keys) {
            neoScript.pushInt(key)
            if (neoScript.getScriptHexString().toLowerCase() != testCases[key]) {
                throw Exception("Test not passed for ${key}: ${neoScript.getScriptHexString().toLowerCase()} != ${testCases[key]}")
            }
            neoScript.resetScript()
        }
    }

    @Test
    fun testNEP5TransferScriptBuilder() {
        val testCases = arrayOf(hashMapOf("script" to "530800e1f505000000001458218b796504d6bde39ba805f92dcda64eae2d8c1458218b796504d6bde39ba805f92dcda64eae2d8c53c1087472616e7366657267cf9472821400ceb06ca780c2a937fec5bbec51b9",
                "scriptHash" to "b951ecbbc5fe37a9c280a76cb0ce0014827294cf",
                "operation" to  "transfer",
                "args" to  arrayOf(100000000,"58218b796504d6bde39ba805f92dcda64eae2d8c", "58218b796504d6bde39ba805f92dcda64eae2d8c")
        ))

        val neoScript = ScriptBuilder()
        for (testCase in testCases) {
            val script = testCase["script"] as String
            val scriptHash = testCase["scriptHash"] as String
            val operation = testCase["operation"] as String?
            val args = testCase["args"]
            neoScript.pushContractInvoke(scriptHash, operation, args)
            val neoScriptString = neoScript.getScriptHexString()
            if (neoScriptString.toLowerCase() != script) {
                throw Exception("Test not passed: ${neoScriptString.toLowerCase()} != $script")
            }
            neoScript.resetScript()
        }
    }
}