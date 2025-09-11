package com.jxw.twecall.manager

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import com.jxw.basemodel.base.IBaseManager
import com.jxw.twecall.utils.LogUtil

/**
 *
 * @author: jyq
 * @desc: 扬声器 音频管理工具类
 * @date: 2024/10/31
 */
internal object AudioSpeakerManager : IBaseManager {
    private var mAudioManager: AudioManager? = null
    private var hasEarpiece: Boolean = false    // 检测设备是否有听筒

    override fun init(context: Context) {
        mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        hasEarpiece = context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
        turnOnSpeaker()
        requestAudioFocus()
    }

    /**
     * 打开扬声器
     */
    fun turnOnSpeaker() {
        mAudioManager?.apply {
            mode = AudioManager.MODE_IN_COMMUNICATION
            isSpeakerphoneOn = true
        }
    }

    /**
     * 关闭扬声器
     */
    fun turnOffSpeaker() {
        mAudioManager?.isSpeakerphoneOn = false
        mAudioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
        if (!hasEarpiece) {
            mAudioManager?.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 0, 0)
        }
    }

    /**
     * 通话音量调高一级，并且弹出系统的音量 UI。
     */
    fun adjustRaise() {
        mAudioManager?.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
    }

    /**
     * 通话音量降低一级，并且弹出系统的音量 UI。
     */
    fun adjustLow() {
        mAudioManager?.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
    }

    /**
     * 检查当前是否是扬声器播放
     */
    fun isSpeakerOn(): Boolean = mAudioManager?.isSpeakerphoneOn ?: true

    /**
     * 请求音频焦点
     */
    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .build()
                )
                .setOnAudioFocusChangeListener(audioFocusListener)
                .build()

            mAudioManager?.requestAudioFocus(focusRequest)
        } else {
            mAudioManager?.requestAudioFocus(
                audioFocusListener,
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    /**
     * 音频焦点监听
     */
    private val audioFocusListener =
        AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS -> LogUtil.i(msg = "永久失去音频焦点")
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> LogUtil.i(msg = "暂时失去音频焦点")
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> LogUtil.i(msg = "暂时失去焦点，降低音量")
                AudioManager.AUDIOFOCUS_GAIN -> LogUtil.i(msg = "重新获得音频焦点")
            }
        }

    override fun clear(context: Context?) {
        mAudioManager?.mode = AudioManager.MODE_NORMAL
        mAudioManager?.isSpeakerphoneOn = false
    }
}