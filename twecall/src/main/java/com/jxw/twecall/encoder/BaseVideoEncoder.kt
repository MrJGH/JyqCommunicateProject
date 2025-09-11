package com.jxw.twecall.encoder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.os.Bundle
import com.jxw.twecall.listener.OnEncodeListener
import com.jxw.twecall.model.VideoEncodeParam
import com.jxw.twecall.utils.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Arrays
import java.util.concurrent.Executors

/**
 * @author: jyq
 * @desc:
 * @date: 2024/12/19
 */
open class BaseVideoEncoder(private val videoEncodeParam: VideoEncodeParam) : IVideoEncoder {
    private var mMediaCodec: MediaCodec? = null  //视频编码器
    private var mEncoderListener: OnEncodeListener? = null
    private var mExecutor = Executors.newSingleThreadExecutor()
    private var seq = 0L

    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())    // 协程作用域
    private var encodeChannel: Channel<ByteArray>? = Channel(capacity = 15, BufferOverflow.DROP_OLDEST) //缓冲通道

    init {
        enableChannelEncode()
    }

    override fun start() {
        try {
            initMediaCodec()
        } catch (e: IOException) {
            e.printStackTrace()
            LogUtil.e(TAG, "start " + e.message)
            throw RuntimeException(e)
        }
    }

    /**
     * 初始化 视频 MediaFormat
     */
    override fun initVideoMediaFormat(): MediaFormat {
        return MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, videoEncodeParam.width, videoEncodeParam.height).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, videoEncodeParam.bitRate)
            setInteger(MediaFormat.KEY_FRAME_RATE, videoEncodeParam.frameRate)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, videoEncodeParam.iFrameInterval)
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR)
            setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel3)
        }
    }

    /**
     * 创建 视频编码器
     */
    open fun initMediaCodec(mediaCodecInfo: MediaCodecInfo? = initMediaCodecInfo()) {
        if (mediaCodecInfo == null) {
            LogUtil.e(TAG, "无法找到支持的视频编码器类型")
            return
        }
        try {
            mMediaCodec = MediaCodec.createByCodecName(mediaCodecInfo.name)
        } catch (e: IOException) {
            LogUtil.e(TAG, "创建编码器异常.." + e.message)
            e.printStackTrace()
            throw java.lang.RuntimeException(e)
        }
        mMediaCodec?.configure(initVideoMediaFormat(), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mMediaCodec?.start()
    }


    /**
     * 初始化 视频编码器 获取编码器相关信息
     * 优先选择硬件编码器（hardwareEncoderList） 如果没有找到硬件编码器，则选择 H.264（AVC）软件编码器作为默认
     */
    open fun initMediaCodecInfo(): MediaCodecInfo? {
        val hardwareEncoderList =
            mutableListOf("OMX.MTK.VIDEO.ENCODER.AVC", "c2.android.avc.encoder", "OMX.qcom.video.encoder.avc", "OMX.hisi.video.encoder.avc")
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        val codecInfoArray = codecList.codecInfos
        for (codecInfo in codecInfoArray) {
            if (codecInfo.isEncoder) {
                for (type in codecInfo.supportedTypes) {
                    if (type.startsWith("video/")) {
                        val capabilities = codecInfo.getCapabilitiesForType(type)
                        val supportColorFormats = Arrays.toString(capabilities.colorFormats)
                        if (hardwareEncoderList.contains(codecInfo.name)) {
                            LogUtil.d(TAG, "hardwareEncoderName=${codecInfo.name} supportColorFormats=$supportColorFormats ")
                            capabilities.profileLevels.forEach { LogUtil.d(TAG, "supportProfileLevels level=${it.level} profile=${it.profile}") }
                            return codecInfo
                        } else if (MediaFormat.MIMETYPE_VIDEO_AVC == type) {
                            LogUtil.d(TAG, "softwareEncoderName=${codecInfo.name} supportColorFormats=$supportColorFormats ")
                            capabilities.profileLevels.forEach { LogUtil.d(TAG, "supportProfileLevels level=${it.level} profile=${it.profile}") }
                            return codecInfo
                        }
                    }
                }
            }
        }
        return null
    }


    /**
     * 子类可重写：转换、旋转、降噪、降频等
     */
    protected open fun handleBeforeEncode(data: ByteArray): ByteArray {
        return data
    }

    /**
     * 启动缓冲通道编码逻辑
     */
    private fun enableChannelEncode() {
        coroutineScope.launch {
            try {
                for (data in encodeChannel!!) {
                    val processed = handleBeforeEncode(data)
                    encodeH264Internal(processed)
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "enableChannelEncode channel encode error: ${e.message}")
            }
        }
    }

    override fun encoderH264(data: ByteArray) {
        encodeChannel?.trySend(data)
    }

    private fun encodeH264Internal(data: ByteArray) {
        if (mExecutor.isShutdown) {
            mExecutor = Executors.newSingleThreadExecutor()
        }
        if (mMediaCodec == null) {
            LogUtil.e(TAG, "encoderH264.....mMediaCodec is null")
            return
        }
        mExecutor.submit {
            try {
                mMediaCodec?.let {
                    val inputBufferIndex: Int = it.dequeueInputBuffer(-1)
                    if (inputBufferIndex >= 0) {
                        // 获取输入缓冲区
                        val inputBuffer = it.getInputBuffer(inputBufferIndex)
                        inputBuffer?.let { inputBuffer ->
                            // 将输入数据写入输入缓冲区
                            inputBuffer.clear()
                            inputBuffer.put(data)
                            // 提交输入缓冲区
                            it.queueInputBuffer(inputBufferIndex, 0, data.size, System.nanoTime() / 1000, 0)
                        }
                    }

                    // 获取输出缓冲区
                    val bufferInfo = MediaCodec.BufferInfo()
                    var outputBufferIndex: Int = it.dequeueOutputBuffer(bufferInfo, 0)
                    LogUtil.i(TAG, "encoderH264 outputBufferIndex===$outputBufferIndex")
                    while (outputBufferIndex >= 0) {
                        // 获取输出缓冲区
                        val outputBuffer = it.getOutputBuffer(outputBufferIndex)
                        outputBuffer?.let { outputBuffer ->
                            // 处理输出数据
                            val outData = ByteArray(bufferInfo.size)
                            LogUtil.i(TAG, "encoderH264 outData===$outData")
                            outputBuffer[outData]
                            mEncoderListener?.let {
                                it.onVideoEncoded(outData, System.currentTimeMillis(), seq, true)
                                seq++
                            }
                            // 释放输出缓冲区
                            it.releaseOutputBuffer(outputBufferIndex, false)
                        }
                        // 获取下一个输出缓冲区的索引
                        outputBufferIndex = it.dequeueOutputBuffer(bufferInfo, 0)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                LogUtil.e(TAG, "encoderH264 is error")
            }
        }
    }

    override fun setVideoBitRate(bitRate: Int) {
        val nowBitrate = videoEncodeParam.bitRate
        val targetBitrate = bitRate.coerceIn(videoEncodeParam.getBitRateRange().first, videoEncodeParam.getBitRateRange().last)
        if (nowBitrate == targetBitrate) {
            return
        }
        /* // 计算比特率变化的百分比，避免剧烈变化
         val changeRatio = kotlin.math.abs(nowBitrate - targetBitrate).toFloat() / nowBitrate
         if (changeRatio > 0.2f) { // 限制最大变动幅度
             videoEncodeParam.bitRate = (nowBitrate * 1.2).toInt().coerceAtMost(targetBitrate)
         } else {
             videoEncodeParam.bitRate = targetBitrate
         }*/
        videoEncodeParam.bitRate = targetBitrate
        val params = Bundle()
        params.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, videoEncodeParam.bitRate)
        mMediaCodec?.setParameters(params)
    }

    override fun setVideoFrameRate(frameRate: Int) {
        val nowFrameRate = videoEncodeParam.frameRate
        val targetFrameRate = nowFrameRate.coerceIn(videoEncodeParam.getFrameRateRange().first, videoEncodeParam.getFrameRateRange().last)
        if (nowFrameRate == targetFrameRate) {
            return
        }
        videoEncodeParam.frameRate = frameRate
        val params = Bundle()
        params.putInt(MediaFormat.KEY_FRAME_RATE, frameRate)
        mMediaCodec?.setParameters(params)
    }


    override fun getVideoBitRate(): Int = videoEncodeParam.bitRate;

    override fun getVideoFrameRate(): Int = videoEncodeParam.frameRate;

    override fun getBitRateInterval() = videoEncodeParam.getBitRateRange()


    override fun setEncoderListener(onEncodeListener: OnEncodeListener) {
        mEncoderListener = onEncodeListener
    }

    override fun stop() {
        try {
            encodeChannel?.close()
            coroutineScope.cancel()

            if (mMediaCodec != null) {
                mMediaCodec?.stop()
                mMediaCodec?.release()
                mMediaCodec = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun release() {
        mExecutor.shutdown()
    }

    companion object {
        private const val TAG = "BaseVideoEncoder"
    }


}