package com.jxw.twecall.encoder

import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import com.jxw.twecall.listener.OnEncodeListener
import com.jxw.twecall.model.AudioEncodeParam
import com.jxw.twecall.model.MicParam
import com.jxw.twecall.utils.LogUtil
import com.jxw.twecall.utils.ext.tryCatch
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.Executors

/**
 *
 * @author: jyq
 * @desc: 音频编码 base
 * @date: 2025/2/25
 */
open class BaseAudioEncoder : IAudioEncoder {
    private var mAudioExecutor = Executors.newSingleThreadExecutor()

    private var mAcousticEchoCanceler: AcousticEchoCanceler? = null  //回声消除器
    private var mNoiseSuppressor: NoiseSuppressor? = null  //回声消除器 噪声抑制
    private var mAutomaticGainControl: AutomaticGainControl? = null  //自动增益控制

    private var mMicSampleRateInHz = -1  //麦克风采样率
    private val mAudioEncodeParam = AudioEncodeParam()  //音频编码参数
    private var bufferSizeInBytes = 0
    private var mAudioRecord: AudioRecord? = null
    private var mAudioCodec: MediaCodec? = null  //

    @Volatile
    private var stopEncode = false
    open val mAudioSampleRate = 16000
    private val mAudioBitRate = 48000
    private var isMuted = false
    private var seq = 0L
    private var mEncodeListener: OnEncodeListener? = null

    override fun startAudio() {
        initAudioEncodeParam()
        initAudio()
        startRecord()
    }

    override fun stopAudio() {
        release()
    }

    /**
     * 初始化 mic麦克风参数
     */
    open fun initMicParam(): MicParam {
        val mMicParam = MicParam().apply {
            audioFormat = android.media.AudioFormat.ENCODING_PCM_16BIT
            channelConfig = android.media.AudioFormat.CHANNEL_IN_MONO
            sampleRateInHz = mAudioSampleRate
            audioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION
        }
        LogUtil.d(TAG, "initMicParam 初始化mic麦克风参数 mMicParam =$mMicParam")
        return mMicParam
    }

    /**
     * 初始化 音频编码参数
     */
    private fun initAudioEncodeParam() {
        mAudioEncodeParam.apply {
            audioMimeType = MediaFormat.MIMETYPE_AUDIO_AAC
            bitRate = mAudioBitRate
        }
        LogUtil.d(TAG, "initAudioEncodeParam 初始化音频编码参数 mAudioEncodeParam =$mAudioEncodeParam")
    }

    /**
     * 初始化 音频编码器
     */
    private fun initAudio(micParam: MicParam = initMicParam()) {
        mMicSampleRateInHz = micParam.sampleRateInHz
        bufferSizeInBytes = 2 * AudioRecord.getMinBufferSize(micParam.sampleRateInHz, micParam.channelConfig, micParam.audioFormat)
        LogUtil.d(TAG, "initAudioRecord bufferSizeInBytes =$bufferSizeInBytes")
        mAudioRecord =
            AudioRecord(micParam.audioSource, micParam.sampleRateInHz, micParam.channelConfig, micParam.audioFormat, bufferSizeInBytes).apply {
                initAEC(audioSessionId)
                initNoiseSuppressor(audioSessionId)
                initAGC(audioSessionId)
            }
        try {
            LogUtil.d(TAG, msg = "initAudio 创建编码器${mAudioEncodeParam.audioMimeType}音频编码器,解码类型详见 MediaFormat")
            mAudioCodec = MediaCodec.createEncoderByType(mAudioEncodeParam.audioMimeType)
            mAudioCodec?.configure(
                initAudioFormat(mAudioEncodeParam.audioMimeType, 1, mMicSampleRateInHz),
                null,
                null,
                MediaCodec.CONFIGURE_FLAG_ENCODE
            )
        } catch (e: IOException) {
            e.printStackTrace()
            mAudioRecord = null
            mAudioCodec = null
        }
    }


    /**
     * 初始化 音频格式
     * @param audioMimeType 音频解码器类型  默认 MIMETYPE_AUDIO_AAC模式
     * @param channelCount  1:CHANNEL_OUT_STEREO(立体声) 2:CHANNEL_OUT_MONO(单声道)
     * @param sampleRateInHz  麦克风采样率
     */
    private fun initAudioFormat(audioMimeType: String, channelCount: Int, sampleRateInHz: Int): MediaFormat? {
        val mAudioFormat = MediaFormat.createAudioFormat(audioMimeType, sampleRateInHz, channelCount).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, mAudioEncodeParam.bitRate)
            setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, mAudioEncodeParam.maxInputSize)
        }
        LogUtil.d(TAG, "initAudioFormat 初始化音频格式 mAudioFormat =$mAudioFormat")
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
     * 自动增益控制
     * 作用：自动调整音量，保持音频信号的音量一致（语音通话、录音、视频通话中的音量自动调整。）
     * @param audioSessionId
     */
    private fun initAGC(audioSessionId: Int) {
        if (AutomaticGainControl.isAvailable()) {
            if (mAutomaticGainControl == null) {
                mAutomaticGainControl = AutomaticGainControl.create(audioSessionId)
            }
            mAutomaticGainControl?.enabled = true
            LogUtil.d(TAG, "initAGC 开启自动增益控制")
        } else {
            LogUtil.d(TAG, "initAGC 当前设备不支持自动增益控制")
        }
    }

    open fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    fun setOnEncodeListener(listener: OnEncodeListener?) {
        mEncodeListener = listener
    }

    /**
     * 开始录制 音频
     */
    override fun startRecord() {
        mAudioExecutor.submit {
            mAudioCodec?.let {
                try {
                    stopEncode = false
                    mAudioRecord?.startRecording()
                    it.start()
                    val audioInfo = MediaCodec.BufferInfo()
                    while (!stopEncode) {
                        // 将 AudioRecord 获取的 PCM 原始数据送入编码器
                        val audioInputBufferId = it.dequeueInputBuffer(0)
                        if (audioInputBufferId >= 0) {
                            var inputBuffer = it.getInputBuffer(audioInputBufferId)
                            var readSize = -1
                            if (inputBuffer != null) {
                                mAudioRecord?.let { audioRecord ->
                                    readSize = audioRecord.read(inputBuffer, bufferSizeInBytes)
                                }
                                if (isMuted && readSize > 0) {
                                    for (i in 0 until readSize) {
                                        inputBuffer.put(i, 0.toByte())
                                    }
                                }
                            }
                            if (readSize >= 0) {
                                it.queueInputBuffer(audioInputBufferId, 0, readSize, System.nanoTime() / 1000, 0)
                            }
                        }
                        var audioOutputBufferIndex = it.dequeueOutputBuffer(audioInfo, 0)
                        LogUtil.i(TAG, "startRecord audioOutputBufferIndex===$audioOutputBufferIndex")
                        while (audioOutputBufferIndex >= 0) {
                            var outputBuffer = it.getOutputBuffer(audioOutputBufferIndex)
                            if (audioInfo.size > 2) {
                                outputBuffer?.position(audioInfo.offset)
                                outputBuffer?.limit(audioInfo.offset + audioInfo.size)
                                addADTStoPacket(outputBuffer)
                            }
                            it.releaseOutputBuffer(audioOutputBufferIndex, false)
                            audioOutputBufferIndex = it.dequeueOutputBuffer(audioInfo, 0)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun addADTStoPacket(outputBuffer: ByteBuffer?) {
        val bytes = ByteArray(outputBuffer!!.remaining())
        outputBuffer[bytes, 0, bytes.size]
        val dataBytes = ByteArray(bytes.size + 7)
        System.arraycopy(bytes, 0, dataBytes, 7, bytes.size)
        addADTStoPacket(dataBytes, dataBytes.size)
        if (stopEncode) {
            return
        }
        LogUtil.i(TAG, "startRecord audioEncoder dataBytes===$dataBytes")
        if (mEncodeListener != null) {
            mEncodeListener?.onAudioEncoded(dataBytes, System.currentTimeMillis(), seq)
            seq++
        } else {
            LogUtil.d(TAG, "Encode listener is null, please set encode listener.")
        }
    }

    private fun addADTStoPacket(packet: ByteArray, packetLen: Int) {
        // AAC LC
        val profile = 2
        // CPE
        val chanCfg = 1
        val freqIdx = samplingFrequencyIndexMap[mMicSampleRateInHz]!!
        packet[0] = 0xFF.toByte()
        packet[1] = 0xF9.toByte()
        packet[2] = ((profile - 1 shl 6) + (freqIdx shl 2) + (chanCfg shr 2)).toByte()
        packet[3] = ((chanCfg and 3 shl 6) + (packetLen shr 11)).toByte()
        packet[4] = (packetLen and 0x7FF shr 3).toByte()
        packet[5] = ((packetLen and 7 shl 5) + 0x1F).toByte()
        packet[6] = 0xFC.toByte()
    }

    override fun stopRecord() {
        stopEncode = true
    }


    private fun release() {
        LogUtil.d(TAG, "release...")
        tryCatch<BaseAudioEncoder> {
            stopEncode = true
            mAudioExecutor?.let {
                it.shutdown()
                mAudioExecutor = null
            }
            mAudioRecord?.let {
                it.stop()
                it.release()
                mAudioRecord = null
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
            mAutomaticGainControl?.let {
                it.enabled = false
                it.release()
                mAutomaticGainControl = null
            }
        }
    }

    companion object {
        private const val TAG = "BaseAudioEncoder"

        /**
         * 采样频率对照表
         */
        val samplingFrequencyIndexMap: MutableMap<Int, Int> = HashMap<Int, Int>().apply {
            this[96000] = 0
            this[96000] = 0
            this[88200] = 1
            this[64000] = 2
            this[48000] = 3
            this[44100] = 4
            this[32000] = 5
            this[24000] = 6
            this[22050] = 7
            this[16000] = 8
            this[12000] = 9
            this[11025] = 10
            this[8000] = 11
        }
    }

}