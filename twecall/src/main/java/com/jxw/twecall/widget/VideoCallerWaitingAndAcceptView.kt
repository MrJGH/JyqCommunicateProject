package com.jxw.twecall.widget

import android.content.Context
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import com.jxw.basemodel.BuildConfig
import com.jxw.basemodel.utils.LauncherDataUtil
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
import com.jxw.twecall.utils.ext.singClick

/**
 *
 * @author: jyq
 * @desc: 视频  主动呼出等待和接通后 view
 * @date: 2024/11/12
 */
class VideoCallerWaitingAndAcceptView(context: Context) : BaseCallView(context), IDeviceCallStatusListener {
    private lateinit var mIvCancel: AppCompatImageView  //挂断 或者取消
    private lateinit var mTvCancel: AppCompatTextView  //挂断 或者取消
    private var mCallTime = 0 //通讯时长
    private var calLTimeObserver = Observer<Int> { mCallTime = it }
    private var isCallingHangUpObserver = Observer<Boolean> {
        if (it && TweCallStateManager.callRole.value == CallRole.Caller) {
            TweCallManager.upLoadLoadCallResult(callResultType = 1, callType = MqttMessageType.GROUP_CHAT_VIDEO.type, callTime = mCallTime)
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

    override fun getLayoutId(): Int = R.layout.layout_video_caller_waiting_and_accept_view
    private var mTurnCameraMirrorViewContainer: RelativeLayout? = null

    override fun initView() {
        mIvCancel = findViewById(R.id.iv_cancel)
        mTvCancel = findViewById(R.id.tv_cancel)
        mTurnCameraMirrorViewContainer = findViewById(R.id.rl_turn_camera_mirror_view)
        if (LauncherDataUtil.isNeedTurnCameraMirror() and (BuildConfig.DEVICE_TYPE == 1)) {
            mTurnCameraMirrorViewContainer?.addView(TurnCameraMirrorView(context))
        }
    }

    override fun initListener() {
        mIvCancel.singClick {
            when (TweCallStateManager.callStatus.value) {
                CallStatus.Waiting -> {
                    TweCallManager.sendCommand(CommandDeviceCallEnum.COMMAND_DEVICE_CALL_HANGUP_V2.command)
                    finishActivity()
                }

                CallStatus.AudioAccept, CallStatus.VideoAccept -> {
                    TweCallManager.upLoadLoadCallResult(callResultType = 1, callType = MqttMessageType.GROUP_CHAT_VIDEO.type, callTime = mCallTime)
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
                    TweCallManager.sendCommand(CommandDeviceCallEnum.COMMAND_DEVICE_CALL_HANGUP.command)
                    TweCallManager.doWxCloudVoipHangUp(object : IHangUpStatusListener {
                        override fun hangUpSuccess(hangUpStatus: Int, hangUpMessage: String?) {
                        }

                        override fun hangUpFail(hangUpStatus: Int, hangUpMessage: String?) {
                        }
                    })
                    finishActivity()
                    LogUtil.e(msg = "TweCallStateManager.callStatus.value 状态为" + TweCallStateManager.callStatus.value)
                }
            }
        }
    }

    override fun addObserver() {
        TweCallStateManager.callStatus.observeForever(callStatusObserver)
        TweCallStateManager.callTimeLiveData.observeForever(calLTimeObserver)
        TweCallStateManager.isCallingHangUpLiveData.observeForever(isCallingHangUpObserver)
        TweCallManager.addDeviceCallWxListener(this)
    }

    override fun removeObserver() {
        TweCallStateManager.callStatus.removeObserver(callStatusObserver)
        TweCallStateManager.callTimeLiveData.removeObserver(calLTimeObserver)
        TweCallStateManager.isCallingHangUpLiveData.removeObserver(isCallingHangUpObserver)
        TweCallManager.removeDeviceCallWxListener(this)

    }

    override fun onWxCancelCallBack() {
        TweCallManager.upLoadLoadCallResult(callResultType = 3, callType = MqttMessageType.GROUP_CHAT_VIDEO.type)
    }


}