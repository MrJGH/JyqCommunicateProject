package com.jxw.twecall.ui

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.util.Log
import android.view.KeyEvent
import android.widget.RelativeLayout
import com.jxw.twecall.R
import com.jxw.twecall.TweCallManager
import com.jxw.twecall.base.BaseActivity
import com.jxw.twecall.base.BaseCallView
import com.jxw.twecall.constant.Constant
import com.jxw.twecall.manager.ActivityManager
import com.jxw.twecall.manager.AudioSpeakerManager
import com.jxw.twecall.manager.MediaPlayerManager
import com.jxw.twecall.manager.PowerScreenManager
import com.jxw.twecall.manager.TweCallStateManager
import com.jxw.twecall.manager.TweCallStateManager.fromAccount
import com.jxw.twecall.manager.TweCallStateManager.toAccount
import com.jxw.twecall.model.CallMediaType
import com.jxw.twecall.utils.LogUtil
import com.jxw.twecall.widget.AudioCallKitView
import com.jxw.twecall.widget.VideoCallKitView
import kotlinx.coroutines.cancel

/**
 *
 * @author: jyq
 * @desc:
 * @date: 2024/11/18
 */
internal class TweCallActivity : BaseActivity() {
    private lateinit var mLayoutContainer: RelativeLayout
    private lateinit var mBaseCallView: BaseCallView

    override fun layoutId(): Int = R.layout.activity_twecall

    override fun initView() {
        AudioSpeakerManager.init(this)
        volumeControlStream = AudioManager.STREAM_VOICE_CALL
        ActivityManager.add(this)
        activity = this
        initIntentData()
        mLayoutContainer = findViewById(R.id.rl_container)
        if (TweCallStateManager.mediaType.value == CallMediaType.Audio) {
            mBaseCallView = AudioCallKitView(this)
            mLayoutContainer.addView(mBaseCallView)
        } else if (TweCallStateManager.mediaType.value == CallMediaType.Video) {
            mBaseCallView = VideoCallKitView(this)
            mLayoutContainer.addView(mBaseCallView)
        }
    }

    private fun initIntentData() {
        openId = intent.getStringExtra(Constant.TWE_CALL_USER_OPEN_ID)
        userHeader = intent.getStringExtra(Constant.TWE_CALL_USER_HEADER_URL)
        userName = intent.getStringExtra(Constant.TWE_CALL_USER_NAME)
        fromAccount = intent.getStringExtra(Constant.TWE_CALL_FROM_ACCOUNT)
        toAccount = intent.getStringExtra(Constant.TWE_CALL_TO_ACCOUNT)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        LogUtil.e(msg = "onKeyDown keyCode=$keyCode  event=$event")
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                AudioSpeakerManager.adjustRaise()
                return true
            }

            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                AudioSpeakerManager.adjustLow()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        mBaseCallView.removeObserver()
        TweCallManager.resetInitParam()
        cancel()
        AudioSpeakerManager.clear(this)
        super.onDestroy()
    }

    override fun onBackPressed() {}

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var activity: BaseActivity? = null
        var openId: String? = null
        var userHeader: String? = null
        var userName: String? = null

        fun finishActivity() {
            Log.d("Twecall", ">>>>>>>> finishActivity")
            (activity as? Context)?.let { MediaPlayerManager.clear(context = it) }
            (activity as? Context)?.let { PowerScreenManager.clear(context = it) }
            TweCallManager.finishStream()
            openId = null
            userHeader = null
            userName = null
            activity?.finish()
            activity = null
        }
    }
}