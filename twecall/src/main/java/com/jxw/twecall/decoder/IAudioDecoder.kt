package com.jxw.twecall.decoder

/**
 *
 * @author: jyq
 * @desc: 音频解码 接口
 * @date: 2025/2/22
 */
interface IAudioDecoder {
    /**
     * 开始音频解码
     * @param type  twecall sdk返回
     * @param mode  音频通道配置 twecall sdk返回  默认逻辑 1:CHANNEL_OUT_STEREO(立体声) 2:CHANNEL_OUT_MONO(单声道)
     * @param width  音频PCM格式配置 twecall sdk返回  通常与音频的精度有关，比如 8 位、16 位、24 位等。这会影响音频数据的质量和文件大小。
     * @param sampleRate  音频采样率，通常表示每秒钟采样多少次。例如，44.1 kHz（每秒44,100次采样）表示较高的音频质量，而较低的采样率（如 8 kHz）则通常用于语音或低质量音频传输
     */
    fun startAudio(type: Int, mode: Int, width: Int, sampleRate: Int)

    /**
     * 停止音频解码
     */
    fun stopAudio()
}