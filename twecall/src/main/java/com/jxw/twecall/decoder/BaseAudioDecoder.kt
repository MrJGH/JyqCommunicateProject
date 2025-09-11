package com.jxw.twecall.decoder

import android.content.Context
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.NoiseSuppressor
import com.jxw.twecall.model.AudioDecoderParam
import com.jxw.twecall.utils.LogUtil
import com.jxw.twecall.utils.ext.tryCatch
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.Executors

/**
 *
 * @author: jyq
 * @desc:
 * @date: 2024/12/19
 */
open class BaseAudioDecoder : IAudioDecoder {
    private val TAG = "BaseAudioDecoder"
    private var mAcousticEchoCanceler: AcousticEchoCanceler? = null  //回声消除器
    private var mNoiseSuppressor: NoiseSuppressor? = null  //回声消除器 噪声抑制

    private var mAudioDecoderParam = AudioDecoderParam()
    private var mAudioExecutor = Executors.newSingleThreadExecutor()
    private var mAudioTrack: AudioTrack? = null
    private var mAudioCodec: MediaCodec? = null
    private var mAudioFormat: MediaFormat? = null

    override fun startAudio(type: Int, mode: Int, width: Int, sampleRate: Int) {
        initAudioDecoderParam(mode, width, sampleRate)
        initAudioTrack()
        initAudio(type, mode.plus(1))
    }

    /**
     * 初始化 音频解码的参数
     */
    private fun initAudioDecoderParam(mode: Int, width: Int, sampleRate: Int): AudioDecoderParam {
        mAudioDecoderParam.apply {
            channelConfig = if (mode == 1) AudioFormat.CHANNEL_OUT_STEREO else AudioFormat.CHANNEL_OUT_MONO
            audioFormat = if (width == 1) AudioFormat.ENCODING_PCM_16BIT else AudioFormat.ENCODING_PCM_8BIT
            sampleRateInHz = when (sampleRate) {
                in 7999 until 16001 -> 16000
                in 16000 until 44101 -> 44100
                in 44100 until 48001 -> 48000
                else -> sampleRate
            }
        }
        LogUtil.i(msg = "initAudioDecoderParam 初始化音频解码的参数完成 mode =$mode width =$width  sampleRate =$sampleRate")
        return mAudioDecoderParam
    }

    /**
     * 初始化  AudioTrack(用于播放实时音频流)
     * @param gain 音量增益，通常是一个浮动类型的数值。这个值控制音量的大小，范围通常是 0.0 到 1.0，或者更大的值。0.0 表示静音，1.0 表示默认音量（通常是最大音量）
     */
    private fun initAudioTrack(gain: Float = 1.0f): AudioTrack? {
        val minBufSize =
            AudioTrack.getMinBufferSize(mAudioDecoderParam.sampleRateInHz, mAudioDecoderParam.channelConfig, mAudioDecoderParam.audioFormat)
        mAudioTrack = AudioTrack(
            mAudioDecoderParam.streamType,
            mAudioDecoderParam.sampleRateInHz,
            mAudioDecoderParam.channelConfig,
            mAudioDecoderParam.audioFormat,
            minBufSize,
            mAudioDecoderParam.audioMode
        ).apply {
            initAEC(audioSessionId)
            initNoiseSuppressor(audioSessionId)
            setVolume(gain)
            play()
        }
        LogUtil.i(msg = "initAudioTrack 初始化 AudioTrack完成 ")
        return mAudioTrack
    }

    /**
     * @param channelCount  1:CHANNEL_OUT_STEREO(立体声) 2:CHANNEL_OUT_MONO(单声道)
     * @param audioMimeType 音频解码器类型  默认 MIMETYPE_AUDIO_AAC模式
     */
    private fun initAudio(type: Int, channelCount: Int, audioMimeType: String = MediaFormat.MIMETYPE_AUDIO_AAC) {
        LogUtil.i(msg = "BaseAudioDecoder initAudio type =$type  channelCount =$channelCount  audioMimeType =$audioMimeType")
        if (type == 4) {
            LogUtil.i(msg = "initAudio 创建解码器$audioMimeType 音频解码器，解码类型详见 MediaFormat")
            try {
                mAudioCodec = MediaCodec.createDecoderByType(audioMimeType)
                initAudioFormat(audioMimeType, channelCount)
                mAudioCodec?.apply {
                    configure(mAudioFormat, null, null, 0)
                    start()
                    LogUtil.i(msg = "initAudio 启动$audioMimeType 音频解码器，解码类型详见 MediaFormat")
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }

        }
    }

    /**
     * 初始化 音频格式
     * @param channelCount  1:CHANNEL_OUT_STEREO(立体声) 2:CHANNEL_OUT_MONO(单声道)
     * @param audioMimeType 音频解码器类型  默认 MIMETYPE_AUDIO_AAC模式
     */
    private fun initAudioFormat(audioMimeType: String, channelCount: Int): MediaFormat? {
        mAudioFormat = MediaFormat.createAudioFormat(audioMimeType, mAudioDecoderParam.sampleRateInHz, channelCount).apply {
            setInteger(MediaFormat.KEY_PCM_ENCODING, mAudioDecoderParam.audioFormat)
            setInteger(MediaFormat.KEY_IS_ADTS, 1)
            setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 256 * 1024)
            setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            setByteBuffer("csd-0", createAdtsData())
        }
        LogUtil.i(msg = "initAudioFormat 初始化音频格式完成 当前音频解码器类型$audioMimeType 解码类型详见 MediaFormat")
        return mAudioFormat
    }


    /**
     * 回声消除器 麦克风
     * 作用：消除或减少由扬声器音频回馈到麦克风的回声（语音通话、视频通话中的回声消除）
     * @param audioSessionId
     */
    private fun initAEC(audioSessionId: Int) {
        if (AcousticEchoCanceler.isAvailable()) {
            if (mAcousticEchoCanceler == null) {
                mAcousticEchoCanceler = AcousticEchoCanceler.create(audioSessionId)
            }
            mAcousticEchoCanceler?.enabled = true
            LogUtil.d(TAG, "initAEC 开启回音消除")
        } else {
            LogUtil.d(TAG, "initAEC 当前设备不支持回音消除")
        }
    }

    /**
     * 开启噪声抑制
     * 作用：有些设备的回声是由于环境噪音导致的，可以使用 NoiseSuppressor 进行降噪
     * @param audioSessionId
     */
    private fun initNoiseSuppressor(audioSessionId: Int) {
        if (NoiseSuppressor.isAvailable()) {
            if (mNoiseSuppressor == null) {
                mNoiseSuppressor = NoiseSuppressor.create(audioSessionId)
            }
            mNoiseSuppressor?.enabled = true
            LogUtil.d(TAG, "initNoiseSuppressor 开启噪声抑制")
        } else {
            LogUtil.d(TAG, "initNoiseSuppressor 当前设备不支持噪声抑制")
        }
    }

    /**
     * 创建Adts 数据
     */
    private fun createAdtsData(): ByteBuffer {
        val adtsData = ByteArray(2)
        val sampleRateIdx = when (mAudioDecoderParam.sampleRateInHz) {
            16000 -> 0x08
            8000 -> 0x0B
            44100 -> 0x04
            48000 -> 0x03
            else -> 0
        }
        adtsData[0] = ((MediaCodecInfo.CodecProfileLevel.AACObjectLC shl 3) or (sampleRateIdx shr 1)).toByte()
        adtsData[1] = ((sampleRateIdx shl 7) and 0x80 or (mAudioDecoderParam.channelConfig shl 3)).toByte()
        return ByteBuffer.wrap(adtsData)
    }

    open fun decoderAAC(data: ByteArray, len: Int, pts: Long): Int {
        if (mAudioExecutor?.isShutdown == true) {
            LogUtil.w(msg = "decoderAAC  mAudioExecutor?.isShutdown == true")
            return -1
        }
        if (mAudioTrack == null) {
            LogUtil.w(msg = "decoderAAC  mAudioTrack is null")
            return -2
        }

        mAudioExecutor?.submit {
            try {
                if (mAudioCodec == null) {
                    mAudioTrack?.write(data, 0, len)
                    return@submit
                }
                val inputBufferIndex = mAudioCodec?.dequeueInputBuffer(0) ?: -1
                if (inputBufferIndex >= 0) {
                    val inputBuffer = mAudioCodec?.getInputBuffer(inputBufferIndex)
                    inputBuffer?.apply {
                        clear()
                        put(data)
                        mAudioCodec?.queueInputBuffer(inputBufferIndex, 0, len, pts * 1000, 0)
                    }
                } else {
                    LogUtil.e(TAG, msg = "audio inputBufferIndex invalid: $inputBufferIndex")
                }

                val bufferInfo = MediaCodec.BufferInfo()
                var outputBufferIndex = mAudioCodec?.dequeueOutputBuffer(bufferInfo, 0) ?: -1
                while (outputBufferIndex >= 0) {
                    val outputBuffer = mAudioCodec?.getOutputBuffer(outputBufferIndex)
                    outputBuffer?.let {
                        val outData = ByteArray(bufferInfo.size)
                        it.get(outData)
                        if (mAudioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
                            mAudioTrack?.write(outData, 0, outData.size)
                        } else {
                            LogUtil.w(TAG, "AudioTrack is not playing, skipping write.")
                        }
                        mAudioCodec?.releaseOutputBuffer(outputBufferIndex, false)
                    }
                    outputBufferIndex = mAudioCodec?.dequeueOutputBuffer(bufferInfo, 0) ?: -1
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return 0
    }

    override fun stopAudio() {
        tryCatch<BaseAudioDecoder> {
            mAudioExecutor?.let {
                it.shutdown()
                mAudioExecutor = null
            }
            mAudioTrack?.let {
                it.stop()
                it.release()
                mAudioTrack = null
            }
            mAudioCodec?.let {
                it.stop()
                it.release()
                mAudioCodec = null
            }
            mAcousticEchoCanceler?.let {
                it.enabled = false
                it.release()
                mAcousticEchoCanceler = null
            }
            mNoiseSuppressor?.let {
                it.enabled = false
                it.release()
                mNoiseSuppressor = null
            }
        }
    }

}