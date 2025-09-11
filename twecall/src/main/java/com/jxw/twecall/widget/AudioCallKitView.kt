package com.jxw.twecall.widget

import android.content.Context
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.lifecycle.Observer
import com.jxw.twecall.R
import com.jxw.twecall.TweCallManager
import com.jxw.twecall.base.BaseCallView
import com.jxw.twecall.listener.IVoipCallStatusListener
import com.jxw.twecall.manager.TweCallStateManager
import com.jxw.twecall.model.CallRole
import com.jxw.twecall.model.CallStatus
import com.jxw.twecall.model.CommandDeviceCallEnum
import com.jxw.twecall.ui.TweCallActivity.Companion.finishActivity
import com.jxw.twecall.ui.TweCallActivity.Companion.openId
import com.jxw.twecall.utils.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *
 * @author: jyq
 * @desc: 音频通讯view
 * @date: 2024/11/19
 */
class AudioCallKitView(context: Context) : BaseCallView(context) {
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var mAudioViewContainer: RelativeLayout
    private lateinit var mAudioCallView: BaseCallView
    private lateinit var mCallTimeView: CallTimerView

    private var callStatusObserver = Observer<CallStatus> {
        when {
            it == CallStatus.HangUp -> {
//                ToastUtil.showTips(context.resources.getString(R.string.call_finish))
                TweCallManager.stopWaitingRingVoice()
                coroutineScope.launch {
                    delay(1000)
                    finishActivity()
                }
            }

            it == CallStatus.AudioAccept && TweCallStateManager.callRole.value == CallRole.Called -> {
                TweCallManager.stopWaitingRingVoice()
                mAudioViewContainer.removeAllViews()
                mAudioCallView = AudioCallerWaitingAndAcceptView(context)
                mAudioViewContainer.addView(mAudioCallView)
            }

            it == CallStatus.AudioAccept && TweCallStateManager.callRole.value == CallRole.Caller -> {
                timeUtil.stopCountdown()
            }
            it == CallStatus.Waiting && TweCallStateManager.callRole.value == CallRole.Called -> {
                timeUtil.stopCountdown()
            }
        }
    }

    init {
        addObserver()
    }

    override fun getLayoutId(): Int = R.layout.layout_callkit_view_audio

    override fun initView() {
        mCallTimeView = findViewById(R.id.tv_time_count)
        mAudioViewContainer = findViewById(R.id.rl_audio_view)

        when (TweCallStateManager.callRole.value) {
            CallRole.Caller -> {
                mAudioCallView = AudioCallerWaitingAndAcceptView(context)
                mAudioViewContainer.addView(mAudioCallView)
                timeUtil.startCountdown {
                    if (TweCallStateManager.callStatus.value == CallStatus.Waiting) {
                        TweCallManager.sendCommand(CommandDeviceCallEnum.COMMAND_DEVICE_CALL_HANGUP_V2.command)
                    }
                    finishActivity()
                }
            }

            CallRole.Called -> {
                mAudioCallView = AudioCalledWaitingView(context)
                mAudioViewContainer.addView(mAudioCallView)
                timeUtil.startCountdown { finishActivity() }
            }

            else -> {
                LogUtil.e(msg = "AudioCallKitView   TweCallStateManager.callRole.value None")
            }
        }
    }

    override fun initData() {
        if (TweCallStateManager.callRole.value == CallRole.Caller) {
            doWxCloudVoipCall()
        } else {
            TweCallManager.playWaitingRingVoice(type = 1)
        }
    }

    override fun addObserver() {
        TweCallStateManager.callStatus.observeForever(callStatusObserver)
    }

    override fun removeObserver() {
        TweCallStateManager.callStatus.removeObserver(callStatusObserver)
        mAudioCallView.clear()
        mCallTimeView.removeObserver()
        coroutineScope.cancel()
        timeUtil.stopCountdown()
    }

    private fun doWxCloudVoipCall() {
        openId?.let {
            TweCallManager.doWxCloudVoipCall(
                openId = it,
                0,
                calleeCameraSwitch = false,
                callerCameraSwitch = false,
                callStatusListener = object : IVoipCallStatusListener {
                    override fun onLineBusy(callStatus: Int, callMessage: String?) {
                        Toast.makeText(context, callMessage, Toast.LENGTH_SHORT).show()
                        finishActivity()
                    }

                    override fun callSuccess(callStatus: Int, callMessage: String?) {
                        Toast.makeText(context, callMessage, Toast.LENGTH_SHORT).show()
                    }

                    override fun callFail(callStatus: Int, callMessage: String?) {
                        if (callStatus == -9) {
                            Toast.makeText(context, context.getString(R.string.call_fail_hint), Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, callMessage, Toast.LENGTH_SHORT).show()
                        }
                        finishActivity()
                    }
                })
        }
    }

}