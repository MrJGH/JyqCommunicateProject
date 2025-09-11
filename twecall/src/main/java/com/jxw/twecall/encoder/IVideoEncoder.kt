package com.jxw.twecall.encoder

import android.media.MediaFormat
import com.jxw.twecall.listener.OnEncodeListener

/**
 *
 * @author: jyq
 * @desc:
 * @date: 2024/12/19
 */
interface IVideoEncoder {
    /**
     * 开始
     */
    fun start()

    /**
     * 结束
     */
    fun stop()

    /**
     * 释放
     */
    fun release()

    /**
     * 视频编码 h264
     */
    fun encoderH264(data: ByteArray)

    /**
     * 设置视频编码监听
     */
    fun setEncoderListener(onEncodeListener: OnEncodeListener)

    /**
     * 动态设置比特率
     * @param bitRate 比特率
     */
    fun setVideoBitRate(bitRate: Int)

    /**
     * 动态设置帧率
     * @param frameRate
     */
    fun setVideoFrameRate(frameRate: Int)

    /**
     * 获取视频比特率
     */
    fun getVideoBitRate(): Int

    /**
     * 获取视频帧率
     */
    fun getVideoFrameRate(): Int

    /**
     * 获取视频 比特率范围
     */
    fun getBitRateInterval(): IntRange

    /**
     * 初始化 视频 MediaFormat
     */
    fun initVideoMediaFormat(): MediaFormat
}