package com.jxw.twecall.decoder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import com.jxw.twecall.model.VideoDecodeParam
import com.jxw.twecall.utils.LogUtil
import com.jxw.twecall.utils.NalUnitParser
import java.io.IOException
import java.util.concurrent.Executors

/**
 * @author: jyq
 * @desc:
 * @date: 2025/5/07
 */
open class BaseVideoDecoder(private val videoDecodeParam: VideoDecodeParam) {
    private var mMediaCodec: MediaCodec? = null  //视频解码器
    private var mExecutor = Executors.newSingleThreadExecutor()
    private var isInitMediaCodecSuccess = false
    private var mNalUnitParser: NalUnitParser = NalUnitParser()

    /**
     * 开始 创建解码器
     * 注意调用说明 当数据流存在 sps pps 和idr 帧的时候才能调用，不然会导致编码器启动失败
     */
    private fun startVideo() {
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
    fun initVideoMediaFormat(): MediaFormat {
        return MediaFormat.createVideoFormat(videoDecodeParam.mime, videoDecodeParam.width, videoDecodeParam.height).apply {
            setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1024 * 1024);
            setInteger(MediaFormat.KEY_ROTATION, 0);
            setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline);
            setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel31);
        }
    }

    /**
     * 创建 视频解码器
     */
    open fun initMediaCodec() {
        try {
            mMediaCodec = MediaCodec.createDecoderByType(videoDecodeParam.mime)
        } catch (e: IOException) {
            LogUtil.e(TAG, "创建解码器异常.." + e.message)
            e.printStackTrace()
            throw java.lang.RuntimeException(e)
        }
        mMediaCodec?.configure(initVideoMediaFormat(), videoDecodeParam.surface, null, 0)
        mMediaCodec?.start()
        isInitMediaCodecSuccess = true
        LogUtil.e(TAG, "成功创建解码器mMediaCodec:$mMediaCodec videoDecodeParam.surface:${videoDecodeParam.surface}")
    }


    fun decoderH264(data: ByteArray, len: Int, pts: Long): Int {

//        LogUtil.d(TAG, "ByteArray = ${HexUtil.toHex(data)}")  // 打印byte数组
        if (mExecutor?.isShutdown == true) {
            LogUtil.e(msg = "decoderH264  mExecutor?.isShutdown == true")
            return -1
        }

        // 如果尚未初始化解码器，尝试解析数据
        if (!isInitMediaCodecSuccess) {
            mNalUnitParser.parse(data)
            if (mNalUnitParser.canStartDecoder()) {
                startVideo()  // 初始化解码器
                mNalUnitParser.reset() // 初始化后重置标志
            }
        }
        if (mMediaCodec == null || !isInitMediaCodecSuccess) {
            LogUtil.e(TAG, "decoderH264.....mMediaCodec is null or init mediaCodec fail")
            return -2
        }
        mExecutor?.submit {
            try {
                mMediaCodec?.let {
                    val inputBufferIndex: Int = it.dequeueInputBuffer(-1)
                    if (inputBufferIndex >= 0) {
                        // 获取输入缓冲区
                        val inputBuffer = it.getInputBuffer(inputBufferIndex)
                        inputBuffer?.let { inputBuffer ->
                            // 将输入数据写入输入缓冲区
                            inputBuffer.clear()
                            inputBuffer.put(data, 0, len)
                            // 提交输入缓冲区
                            it.queueInputBuffer(inputBufferIndex, 0, len, 0, 0)
                        }
                    }

                    // 获取输出缓冲区
                    val bufferInfo = MediaCodec.BufferInfo()
                    var outputBufferIndex: Int = it.dequeueOutputBuffer(bufferInfo, 0)
                    LogUtil.i(TAG, "decoderH264 outputBufferIndex===$outputBufferIndex")
                    while (outputBufferIndex >= 0) {
                        // 释放输出缓冲区
                        it.releaseOutputBuffer(outputBufferIndex, true)
                        // 获取下一个输出缓冲区的索引
                        outputBufferIndex = it.dequeueOutputBuffer(bufferInfo, 0)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                LogUtil.e(TAG, "decoderH264 is error")
            }
        }
        return 0
    }

    fun stopVideo() {
        if (mExecutor != null) {
            mExecutor?.shutdown()
            mExecutor = null
        }
        if (mMediaCodec != null) {
            mMediaCodec?.stop()
            mMediaCodec?.release()
            mMediaCodec = null
        }
        isInitMediaCodecSuccess = false
    }


    companion object {
        private const val TAG = "BaseVideoDecoder"
    }


}