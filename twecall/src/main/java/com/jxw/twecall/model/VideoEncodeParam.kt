package com.jxw.twecall.model

import android.media.MediaFormat
import kotlin.math.roundToInt

/**
 *
 * @author: jyq
 * @desc: 视频编码参数
 * @date: 2025/03/26
 */
data class VideoEncodeParam(
    var width: Int = -1,//录制宽度
    var height: Int = -1, //录制高度
    var frameRate: Int = 15, // 帧率  描述视频格式的帧速率（以帧/秒为单位）的键。帧率，一般在15至30之内，太小容易造成视频卡顿。
    var iFrameInterval: Int = 2, // I帧间隔: 默认一秒一个 I 帧
    var bitRate: Int = 125000, //码率
    val mime: String = MediaFormat.MIMETYPE_VIDEO_AVC, // 编码格式: 默认 H264
    var maxFrameRate: Int = 18, //帧率下限(目前固定值，针对不同性能的设备可调整)
    var minFrameRate: Int = 10, //帧率上限(目前固定值，针对不同性能的设备可调整)
) {
    /**
     * 获取 比特率上下限 区间范围
     * 目前范围算法 如下 具体可修改
     */
    fun getBitRateRange() = width * height * 0.5.roundToInt()..width * height * 2.0.roundToInt()

    /**
     * 获取 帧率上下限区间范围
     */
    fun getFrameRateRange() = minFrameRate..maxFrameRate

    /**
     * 获取基础的平均帧率
     */
    fun getAverageBitRate(): Int  =(getBitRateRange().first + getBitRateRange().last)/2

}
