package com.jxw.twecall.utils

object HexUtil {

    /**
     * 将 ByteArray 转换为十六进制字符串
     * @param bytes 原始字节数组
     * @param separator 分隔符（可选），如 " "、":" 等
     */
    fun toHex(bytes: ByteArray, separator: String = " "): String {
        return bytes.joinToString(separator) { byte -> "%02X".format(byte) }
    }

    /**
     * 将十六进制字符串转换回 ByteArray（必须是偶数长度，且只包含 0-9A-Fa-f）
     */
    fun fromHex(hex: String): ByteArray {
        val cleanHex = hex.replace(Regex("[^0-9A-Fa-f]"), "")
        require(cleanHex.length % 2 == 0) { "Hex string must have even length" }

        return ByteArray(cleanHex.length / 2) { i ->
            cleanHex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }
}
