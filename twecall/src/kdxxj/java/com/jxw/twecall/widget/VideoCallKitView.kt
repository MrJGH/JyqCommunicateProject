package com.jxw.twecall.widget

import android.content.Context
import androidx.lifecycle.Observer
import com.jxw.twecall.TweCallManager
import com.jxw.twecall.manager.TweCallStateManager
import com.jxw.twecall.model.CallRole
import com.jxw.twecall.model.CallStatus
import com.jxw.twecall.ui.TweCallActivity.Companion.finishActivity
import com.jxw.twecall.utils.ext.gone
import com.jxw.twecall.utils.ext.isVisible
import com.jxw.twecall.utils.ext.singClick
import com.jxw.twecall.utils.ext.visible
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *
 * @author: jyq
 * @desc: 视频通讯view 口袋机版本
 * @date: 2024/11/19
 */
class VideoCallKitView(context: Context) : BaseVideoCallKitView(context) {

    private var callStatusObserver = Observer<CallStatus> {
        when {
            it == CallStatus.Waiting -> {
                if (TweCallStateManager.callRole.value == CallRole.Called) {
                    timeUtil.stopCountdown()
                }
                textureViewVoip.visible()
                surfaceViewVoip.gone()
            }
            
            it == CallStatus.HangUp -> {
                TweCallManager.stopWaitingRingVoice()
                coroutineScope.launch {
                    delay(1000)
                    finishActivity()
                }
            }

            it == CallStatus.VideoAccept && TweCallStateManager.callRole.value == CallRole.Caller -> {
                timeUtil.stopCountdown()
                surfaceViewVoip.initTranslation()
                textureViewVoip.visible()
                surfaceViewVoip.visible()
                changeTextureView()
                mHandler.sendEmptyMessageDelayed(0, mHideTime)
            }

            it == CallStatus.VideoAccept && TweCallStateManager.callRole.value == CallRole.Called -> {
                TweCallManager.stopWaitingRingVoice()
                surfaceViewVoip.initTranslation()
                mVideoViewContainer.removeAllViews()
                mVideoCallView = VideoCallerWaitingAndAcceptView(context)
                mVideoViewContainer.addView(mVideoCallView)
                textureViewVoip.visible()
                surfaceViewVoip.visible()
                changeTextureView()
                mHandler.sendEmptyMessageDelayed(0, mHideTime)
            }
        }
    }

    init {
        addObserver()
    }

    override fun initListener() {
        textureViewVoip.singClick {
            if (surfaceViewVoip.isVisible && !isTextureViewFullScreen) {
                changeTextureView()
            } else {
                switchScreenView()
            }
        }
        surfaceViewVoip.singClick {
            if (isTextureViewFullScreen) {
                changeTextureView()
            } else {
                switchScreenView()
            }
        }
    }

    override fun addObserver() {
        super.addObserver()
        TweCallStateManager.callStatus.observeForever(callStatusObserver)
    }

    override fun removeObserver() {
        super.removeObserver()
        TweCallStateManager.callStatus.removeObserver(callStatusObserver)
    }

}