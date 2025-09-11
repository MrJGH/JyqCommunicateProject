package com.jxw.twecall.widget

import android.content.Context
import androidx.appcompat.widget.AppCompatImageView
import com.jxw.twecall.R
import com.jxw.twecall.TweCallManager
import com.jxw.twecall.base.BaseCallView
import com.jxw.twecall.model.CommandDeviceCallEnum
import com.jxw.twecall.ui.TweCallActivity
import com.jxw.twecall.utils.LogUtil
import com.jxw.twecall.utils.ext.singClick

/**
 *
 * @author: jyq
 * @desc: 音频 接收等待view
 * @date: 2024/11/12
 */
class AudioCalledWaitingView(context: Context) : BaseCallView(context) {
    private lateinit var mRefuse: AppCompatImageView
    private lateinit var mAnswer: AppCompatImageView

    init {
        addObserver()
    }

    override fun getLayoutId(): Int = R.layout.layout_audio_called_waiting_view

    override fun initView() {
        mRefuse = findViewById(R.id.iv_refuse)
        mAnswer = findViewById(R.id.iv_answer)

    }

    override fun initListener() {
        mRefuse.singClick {
            LogUtil.e(msg = "AudioCalledWaitingView mRefuse singClick")
            TweCallManager.sendCommand(CommandDeviceCallEnum.COMMAND_DEVICE_CALL_REJECT.command)
            TweCallActivity.finishActivity()
        }
        mAnswer.singClick(delay = 1000) {
            LogUtil.e(msg = "AudioCalledWaitingView mAnswer singClick")
            val sendCommandStatus = TweCallManager.sendCommand(CommandDeviceCallEnum.COMMAND_DEVICE_CALL_ANSWER.command)
            mAnswer.isEnabled = !sendCommandStatus
            if (sendCommandStatus) {
                mAnswer.isSelected = true
            } else {
                TweCallActivity.finishActivity()
            }
        }
    }

}