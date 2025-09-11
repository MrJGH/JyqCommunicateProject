package com.jxw.twecall.widget

import android.content.Context
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import com.jxw.twecall.R
import com.jxw.twecall.TweCallManager
import com.jxw.twecall.base.BaseCallView
import com.jxw.twecall.listener.IDeviceCallStatusListener
import com.jxw.twecall.listener.IHangUpStatusListener
import com.jxw.twecall.manager.TweCallStateManager
import com.jxw.twecall.model.CallRole
import com.jxw.twecall.model.CallStatus
import com.jxw.twecall.model.CommandDeviceCallEnum
import com.jxw.twecall.model.MqttMessageType
import com.jxw.twecall.ui.TweCallActivity.Companion.finishActivity
import com.jxw.twecall.utils.LogUtil
import com.jxw.twecall.utils.ViewUtil
import com.jxw.twecall.utils.ext.singClick

/**
 *
 * @author: jyq
 * @desc: 音频 主动呼出等待和接通后 view
 * @date: 2024/11/12
 */
class AudioCallerWaitingAndAcceptView(context: Context) : BaseCallView(context), IDeviceCallStatusListener {
    private lateinit var mMicrophone: AppCompatImageView  //麦克风
    private lateinit var mMicrophoneTv: AppCompatTextView  //麦克风 文案
    private lateinit var mIvCancel: AppCompatImageView  //挂断 或者取消
    private lateinit var mTvCancel: AppCompatTextView  //挂断 或者取消 文案
    private lateinit var mLoudspeaker: AppCompatImageView  //扬声器
    private lateinit var mLoudspeakerTv: AppCompatTextView  //扬声器 文案
    private lateinit var mSoundOff: AppCompatImageView  //静音
    private lateinit var mSoundOffTv: AppCompatTextView  //静音 文案

    private var mCallTime = 0 //通讯时长

    private var calLTimeObserver = Observer<Int> { mCallTime = it }

    private var isCallingHangUpObserver = Observer<Boolean> {
        if (it && TweCallStateManager.callRole.value == CallRole.Caller) {
            TweCallManager.upLoadLoadCallResult(callResultType = 1, callType = MqttMessageType.GROUP_CHAT_AUDIO.type, callTime = mCallTime)
        }
    }
    private var isSpeakerObserver = Observer<Boolean> {
        mLoudspeaker.isSelected = !it
        mSoundOff.isSelected = !it
        if (it) {
            mLoudspeakerTv.text = context.getString(R.string.loudspeaker_open)
            mSoundOffTv.text = context.getString(R.string.sound_off)
        } else {
            mLoudspeakerTv.text = context.getString(R.string.loudspeaker_close)
            mSoundOffTv.text = context.getString(R.string.sound_off)
        }
    }
    private var isMicrophoneObserver = Observer<Boolean> {
        mMicrophone.isSelected = !it
        if (it) {
            mMicrophoneTv.text = context.getString(R.string.microphone_open)
        } else {
            mMicrophoneTv.text = context.getString(R.string.microphone_close)
        }
    }

    private var callStatusObserver = Observer<CallStatus> {
        when {
            it == CallStatus.Waiting -> mTvCancel.text = context.getString(R.string.cancel)
            it == CallStatus.AudioAccept -> mTvCancel.text = context.getString(R.string.hangup)
        }
    }

    init {
        addObserver()
    }

    override fun getLayoutId(): Int = R.layout.layout_audio_caller_waiting_and_accept_view

    override fun initView() {
        mMicrophone = findViewById(R.id.iv_mic)
        mMicrophoneTv = findViewById(R.id.tv_mic)
        mIvCancel = findViewById(R.id.iv_cancel)
        mTvCancel = findViewById(R.id.tv_cancel)
        mLoudspeaker = findViewById(R.id.iv_loudspeaker)
        mLoudspeakerTv = findViewById(R.id.tv_loudspeaker)
        mSoundOff = findViewById(R.id.iv_sound_off)
        mSoundOffTv = findViewById(R.id.tv_sound_off)

        ViewUtil.isShowMicrophoneView(mMicrophone, mMicrophoneTv)
        ViewUtil.isShowLoudSpeakView(mLoudspeaker, mLoudspeakerTv)
        ViewUtil.isShowSoundOffView(mSoundOff, mSoundOffTv)
    }


    override fun initListener() {
        mMicrophone.singClick {
            if (mMicrophone.isSelected) {
                TweCallManager.openRecording()
                mMicrophoneTv.text = context.getString(R.string.microphone_open)
            } else {
                TweCallManager.closeRecording()
                mMicrophoneTv.text = context.getString(R.string.microphone_close)
            }
        }
        mIvCancel.singClick {
            when (TweCallStateManager.callStatus.value) {
                CallStatus.Waiting -> {
                    TweCallManager.sendCommand(CommandDeviceCallEnum.COMMAND_DEVICE_CALL_HANGUP_V2.command)
                    finishActivity()
                }

                CallStatus.AudioAccept -> {
                    TweCallManager.upLoadLoadCallResult(callResultType = 1, callType = MqttMessageType.GROUP_CHAT_AUDIO.type, callTime = mCallTime)
                    TweCallManager.sendCommand(CommandDeviceCallEnum.COMMAND_DEVICE_CALL_HANGUP_V2.command)
                    TweCallManager.doWxCloudVoipHangUp(object : IHangUpStatusListener {
                        override fun hangUpSuccess(hangUpStatus: Int, hangUpMessage: String?) {
                            finishActivity()
                        }

                        override fun hangUpFail(hangUpStatus: Int, hangUpMessage: String?) {
                            finishActivity()
                        }
                    })
                }

                else -> {
                    finishActivity()
                    LogUtil.e(msg = "TweCallStateManager.callStatus.value 状态为None")
                }
            }
        }
        mLoudspeaker.singClick {
            if (mLoudspeaker.isSelected) {
                TweCallManager.openSpeaker()
                mLoudspeakerTv.text = context.getString(R.string.loudspeaker_open)
            } else {
                TweCallManager.closeSpeaker()
                mLoudspeakerTv.text = context.getString(R.string.loudspeaker_close)
            }
        }
        mSoundOff.singClick {
            if (mSoundOff.isSelected) {
                TweCallManager.openSpeaker()
            } else {
                TweCallManager.closeSpeaker()
            }
        }
    }

    override fun addObserver() {
        TweCallStateManager.isSpeakerState.observeForever(isSpeakerObserver)
        TweCallStateManager.isMicrophoneMuteState.observeForever(isMicrophoneObserver)
        TweCallStateManager.callStatus.observeForever(callStatusObserver)
        TweCallStateManager.callTimeLiveData.observeForever(calLTimeObserver)
        TweCallStateManager.isCallingHangUpLiveData.observeForever(isCallingHangUpObserver)
        TweCallManager.addDeviceCallWxListener(this)
    }

    override fun removeObserver() {
        TweCallStateManager.isSpeakerState.removeObserver(isSpeakerObserver)
        TweCallStateManager.isMicrophoneMuteState.removeObserver(isMicrophoneObserver)
        TweCallStateManager.callStatus.removeObserver(callStatusObserver)
        TweCallStateManager.callTimeLiveData.removeObserver(calLTimeObserver)
        TweCallStateManager.isCallingHangUpLiveData.removeObserver(isCallingHangUpObserver)
        TweCallManager.removeDeviceCallWxListener(this)
    }

    override fun onWxCancelCallBack() {
        TweCallManager.upLoadLoadCallResult(callResultType = 3, callType = MqttMessageType.GROUP_CHAT_AUDIO.type)
    }

}