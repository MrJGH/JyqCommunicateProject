package com.jxw.twecall.encoder

import android.media.MediaRecorder
import com.jxw.twecall.model.MicParam
import com.jxw.twecall.utils.LogUtil

class AudioEncoder1 : BaseAudioEncoder() {

    override fun initMicParam(): MicParam {
        val mMicParam = MicParam().apply {
            audioFormat = android.media.AudioFormat.ENCODING_PCM_16BIT
            channelConfig = android.media.AudioFormat.CHANNEL_IN_MONO
            sampleRateInHz = mAudioSampleRate
            audioSource = MediaRecorder.AudioSource.MIC
        }
        LogUtil.d(TAG, "initMicParam 初始化mic麦克风参数 mMicParam =$mMicParam")
        return mMicParam
    }

    companion object {
        private const val TAG = "AudioEncoder1"
    }

}