package network.o3.o3wallet.API.NEO

import network.o3.o3wallet.hexStringToByteArray
import network.o3.o3wallet.to8BytesArray
import network.o3.o3wallet.toHex
import network.o3.o3wallet.toMinimumByteArray
import java.nio.ByteOrder

/**
 * Created by drei on 1/19/18.
 */
class ScriptBuilder {
    var bytes = byteArrayOf()
    fun getScriptHexString(): String {
        return bytes.toHex()
    }

    fun pushOpCode(opcode: OPCODE) {
        bytes += byteArrayOf(opcode.value)
    }

    fun pushBool(value: Boolean) {
        if (value == true) {
            pushOpCode(OPCODE.PUSH1)
        } else {
            pushOpCode(OPCODE.PUSH0)
        }
    }

    fun pushInt(value: Int) {

        when (value) {
            -1 -> pushOpCode(OPCODE.PUSHM1)
            0 -> pushOpCode(OPCODE.PUSH0)
            1 -> pushOpCode(OPCODE.PUSH1)
            2 -> pushOpCode(OPCODE.PUSH2)
            3 -> pushOpCode(OPCODE.PUSH3)
            4 -> pushOpCode(OPCODE.PUSH4)
            5 -> pushOpCode(OPCODE.PUSH5)
            6 -> pushOpCode(OPCODE.PUSH6)
            7 -> pushOpCode(OPCODE.PUSH7)
            8 -> pushOpCode(OPCODE.PUSH8)
            9 -> pushOpCode(OPCODE.PUSH9)
            10 -> pushOpCode(OPCODE.PUSH10)
            11 -> pushOpCode(OPCODE.PUSH11)
            12 -> pushOpCode(OPCODE.PUSH12)
            13 -> pushOpCode(OPCODE.PUSH13)
            14 -> pushOpCode(OPCODE.PUSH14)
            15 -> pushOpCode(OPCODE.PUSH15)
            16 -> pushOpCode(OPCODE.PUSH16)
            else -> pushData(to8BytesArray(value).toHex())
        }
    }

    fun appendByteArray(bytesToAppend: ByteArray) {
        for(byte in bytesToAppend) {
            bytes += byteArrayOf(byte)
        }
    }

    fun pushHexString(stringValue: String) {
        val stringBytes = stringValue.hexStringToByteArray()
        if (stringBytes.size < 0x4B) {
            appendByteArray(toMinimumByteArray(stringBytes.size))
            appendByteArray(stringBytes)
        } else if (stringBytes.size < 0x100) {
            pushOpCode(OPCODE.PUSHDATA1)
            appendByteArray(toMinimumByteArray(stringBytes.size))
            appendByteArray(stringBytes)
        } else if (stringBytes.size < 0x10000) {
            pushOpCode(OPCODE.PUSHDATA2)
            appendByteArray(toMinimumByteArray(stringBytes.size))
            appendByteArray(stringBytes)
        } else {
            pushOpCode(OPCODE.PUSHDATA4)
            appendByteArray(toMinimumByteArray(stringBytes.size))
            appendByteArray(stringBytes)
        }
    }

    fun pushArray(array: Array<Any?>) {
        for (elem in array) {
            pushData(elem)
        }
        pushInt(array.size)
        pushOpCode(OPCODE.PACK)
    }

    fun pushArrayList(array: ArrayList<Any?>) {
        for (elem in array) {
            pushData(elem)
        }
        pushInt(array.size)
        pushOpCode(OPCODE.PACK)
    }

    fun resetScript() {
        bytes = byteArrayOf()
    }

    fun pushData(data: Any?) {
        if (data == null) {
            pushBool(false)
            return
        }
        val unwrappedData = data!!

        if (unwrappedData is String) {
            pushHexString(unwrappedData)
        } else if (unwrappedData is Boolean) {
            pushBool(unwrappedData)
        } else if (unwrappedData is Int) {
            pushInt(unwrappedData)
        } else if (unwrappedData is Array<*>) {
            pushArray(unwrappedData as Array<Any?>)
        } else if (unwrappedData is ArrayList<*>) {
            pushArrayList(unwrappedData as ArrayList<Any?>)
        } else {
            return
        }
    }

    fun pushContractInvoke(scriptHash: String, operation: String? = null,
                           args: Any? = null, useTailCall: Boolean = false) {
        pushData(args)
        if (operation != null) {
            val hex = operation.toByteArray().toHex()
            pushData(hex)
        }
        if (scriptHash.length != 40) {
            //You're fucked
            throw(Exception("Invalid Script Hash"))
        }
        if (useTailCall) {
            pushOpCode(OPCODE.TAILCALL)
        } else {
            pushOpCode(OPCODE.APPCALL)
            val toAppendBytes = scriptHash.hexStringToByteArray()
            toAppendBytes.reverse()
            appendByteArray(toAppendBytes)
        }
    }
}