package com.jxw.twecall.model

import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaFormat

/**
 *
 * @author: jyq
 * @desc: 音频编码参数
 * @date: 2024/10/31
 */
data class AudioEncodeParam(
    var bitRate: Int = 96000, // 比特率
    var maxInputSize: Int = 1024 * 1024, // 最大输入数据大小
    var audioMimeType: String = MediaFormat.MIMETYPE_AUDIO_AAC  // 编码格式: 默认 AAC
)

/**
 *
 * @author: jyq
 * @desc: 音频解码的参数
 * @date: 2024/10/31
 */
data class AudioDecoderParam(
    var streamType: Int = AudioManager.STREAM_VOICE_CALL,// 音频流类型
    var sampleRateInHz: Int = -1,// 采样率
    var channelConfig: Int = -1, // 声道格式
    var audioFormat: Int = -1,// 音频格式
    var audioMode: Int = AudioTrack.MODE_STREAM,//流模式，表示逐渐播放音频数据。如果是文件播放，可以使用 MODE_STATIC 模式。
)

/**
 *
 * @author: jyq
 * @desc: Mic 参数
 * @date: 2024/10/31
 */
data class MicParam(
    var audioSource: Int = -1,// 音频输入源
    var sampleRateInHz: Int = -1,// 采样率
    var channelConfig: Int = -1, // 声道格式
    var audioFormat: Int = -1,// 音频格式
    var streamType: Int = -1,// 音频流类型
    var mode: Int = -1 // 音频流形式
)
