package com.jxw.twecall.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import com.jxw.twecall.R
import com.jxw.twecall.TweCallManager
import com.jxw.twecall.manager.TweCallStateManager
import com.jxw.twecall.model.CallStatus
import com.jxw.twecall.utils.ViewUtil
import com.jxw.twecall.utils.ext.singClick

/**
 *
 * @author: jyq
 * @desc: 相机 相关控制功能 view
 * @date: 2024/11/20
 */
class CameraControlView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private lateinit var mMicrophone: AppCompatImageView  //麦克风
    private lateinit var mMicrophoneTv: AppCompatTextView  //麦克风 文案
    private lateinit var mLoudspeaker: AppCompatImageView  //扬声器
    private lateinit var mLoudspeakerTv: AppCompatTextView  //扬声器 文案
    private lateinit var mTurnCamera: AppCompatImageView  //翻转摄像头
    private lateinit var mTurnCameraTv: AppCompatTextView  //翻转摄像头 文案
    private lateinit var mSoundOff: AppCompatImageView  //静音
    private lateinit var mSoundOffTv: AppCompatTextView  //静音 文案

    private var isMicrophoneObserver = Observer<Boolean> {
        mMicrophone.isSelected = !it
    }
    private var isSpeakerObserver = Observer<Boolean> {
        mLoudspeaker.isSelected = !it
        mSoundOff.isSelected = !it
    }
    private var callStatusObserver = Observer<CallStatus> {
        if (it == CallStatus.VideoAccept) {
            ViewUtil.isShowMicrophoneView(mMicrophone, mMicrophoneTv)
            ViewUtil.isShowLoudSpeakView(mLoudspeaker, mLoudspeakerTv)
            ViewUtil.isShowSoundOffView(mSoundOff, mSoundOffTv)
        } else if (it == CallStatus.Waiting) {
            ViewUtil.isShowMicrophoneView(mMicrophone, mMicrophoneTv,false)
            ViewUtil.isShowLoudSpeakView(mLoudspeaker, mLoudspeakerTv,false)
            ViewUtil.isShowSoundOffView(mSoundOff, mSoundOffTv,false)
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_camera_control_view, this)
        initView()
        initListener()
        addObserver()
    }


    private fun initView() {
        mMicrophone = findViewById(R.id.iv_mic)
        mMicrophoneTv = findViewById(R.id.tv_mic)
        mLoudspeaker = findViewById(R.id.iv_loudspeaker)
        mLoudspeakerTv = findViewById(R.id.tv_loudspeaker)
        mTurnCamera = findViewById(R.id.iv_turn)
        mTurnCameraTv = findViewById(R.id.tv_turn)
        mSoundOff = findViewById(R.id.iv_sound_off)
        mSoundOffTv = findViewById(R.id.tv_sound_off)

        ViewUtil.isShowTurnCameraView(mTurnCamera, mTurnCameraTv)

    }


    private fun initListener() {
        mMicrophone.singClick {
            if (mMicrophone.isSelected) {
                TweCallManager.openRecording()
                mMicrophoneTv.text = context.getString(R.string.microphone_open)
            } else {
                TweCallManager.closeRecording()
                mMicrophoneTv.text = context.getString(R.string.microphone_close)
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

        mTurnCamera.singClick(delay = 1000) {
            TweCallManager.switchCamera()
        }

        mSoundOff.singClick {
            if (mSoundOff.isSelected) {
                TweCallManager.openSpeaker()
            } else {
                TweCallManager.closeSpeaker()
            }
        }
    }

    private fun addObserver() {
        TweCallStateManager.isMicrophoneMuteState.observeForever(isMicrophoneObserver)
        TweCallStateManager.isSpeakerState.observeForever(isSpeakerObserver)
        TweCallStateManager.callStatus.observeForever(callStatusObserver)
    }

    fun removeObserver() {
        TweCallStateManager.isMicrophoneMuteState.removeObserver(isMicrophoneObserver)
        TweCallStateManager.isSpeakerState.removeObserver(isSpeakerObserver)
        TweCallStateManager.callStatus.removeObserver(callStatusObserver)

    }
}