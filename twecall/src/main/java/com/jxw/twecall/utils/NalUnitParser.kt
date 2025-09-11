package com.jxw.twecall.utils
/**
 * @author: jyq
 * @desc: NalUnitParser nal单元数据 解析判断是否正常
 * @date: 2025/5/12
 */
class NalUnitParser {

    private var hasSps = false
    private var hasPps = false
    private var hasIdr = false

    /**
     * 判断是否可以初始化解码器（已收到 SPS、PPS 和 IDR 帧）
     */
    fun canStartDecoder(): Boolean {
        return hasSps && hasPps && hasIdr
    }

    /**
     * 重置标志（用于重新开始解析新的码流）
     */
    fun reset() {
        hasSps = false
        hasPps = false
        hasIdr = false
    }

    /**
     * 解析输入字节数据，识别 NAL 单元并记录 SPS/PPS/IDR
     */
    fun parse(data: ByteArray) {
        var offset = 0
        while (offset <= data.size - 4) {
            if (isStartCode(data, offset)) {
                val nalStart = offset + 4
                if (nalStart < data.size) {
                    val nalType = data[nalStart].toInt() and 0x1F
                    when (nalType) {
                        7 -> hasSps = true
                        8 -> hasPps = true
                        5 -> hasIdr = true
                    }
                }
                offset = nalStart
            } else {
                offset++
            }
        }
    }

    private fun isStartCode(data: ByteArray, offset: Int): Boolean {
        return data[offset] == 0x00.toByte() &&
               data[offset + 1] == 0x00.toByte() &&
               data[offset + 2] == 0x00.toByte() &&
               data[offset + 3] == 0x01.toByte()
    }
}
