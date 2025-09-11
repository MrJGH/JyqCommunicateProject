package com.jxw.twecall.encoder

/**
 *
 * @author: jyq
 * @desc: 音频编码 接口
 * @date: 2025/2/25
 */
interface IAudioEncoder {
    /**
     * 开始 音频编码录制
     */
    fun startAudio()

    /**
     * 停止 音频编码录制
     */
    fun stopAudio()

    /**
     * 开始录音
     */
    fun startRecord()

    /**
     * 停止录音
     */
    fun stopRecord()
}