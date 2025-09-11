package com.jxw.twecall.widget

import android.content.Context
import android.graphics.SurfaceTexture
import android.os.Handler
import android.view.TextureView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.lifecycle.Observer
import com.jxw.basemodel.utils.LauncherDataUtil
import com.jxw.twecall.R
import com.jxw.twecall.TweCallManager
import com.jxw.twecall.base.BaseCallView
import com.jxw.twecall.listener.IVoipCallStatusListener
import com.jxw.twecall.manager.TweCallStateManager
import com.jxw.twecall.model.CallRole
import com.jxw.twecall.model.CallStatus
import com.jxw.twecall.model.CommandDeviceCallEnum
import com.jxw.twecall.ui.TweCallActivity
import com.jxw.twecall.utils.LogUtil
import com.jxw.twecall.utils.ext.expandTouchArea
import com.jxw.twecall.utils.ext.gone
import com.jxw.twecall.utils.ext.visible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 *
 * @author: jyq
 * @desc:
 * @date: 2024/12/31
 */
open class BaseVideoCallKitView(context: Context) : BaseCallView(context = context), TextureView.SurfaceTextureListener {
    internal val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    internal lateinit var mVideoViewContainer: RelativeLayout
    private var mTurnCameraMirrorViewContainer: RelativeLayout? = null
    internal lateinit var surfaceViewVoip: MoveTextureView
    internal lateinit var textureViewVoip: MoveTextureView
    private var mCameraControlView: CameraControlView? = null
    private var mUserInfoView: UserInfoView? = null
    private var mCallTimeView: CallTimerView? = null
    internal lateinit var mVideoCallView: BaseCallView
    private var isClearScreen = false
    internal var isTextureViewFullScreen = true //  textureViewVoip是否是全屏
    internal val mHideTime = 5000L    //视频接听后 隐藏view 的时长
    private var mClearScreenViewObserver = Observer<Boolean> {
        if (it.not()) {
            mHandler.sendEmptyMessageDelayed(0, mHideTime)
        } else {
            mHandler.removeMessages(0)
        }
    }
    internal var mHandler: Handler = Handler { msg ->
        when (msg.what) {
            0 -> clearScreenView()
            else -> {}
        }
        true
    }

    override fun getLayoutId(): Int = R.layout.layout_callkit_view_video

    override fun initView() {
        mVideoViewContainer = findViewById(R.id.rl_video_view)
        mTurnCameraMirrorViewContainer = findViewById(R.id.rl_turn_camera_mirror_view)
        surfaceViewVoip = findViewById(R.id.surfaceView_voip)
        textureViewVoip = findViewById(R.id.textureView_voip)
        mUserInfoView = findViewById(R.id.user_info_view)
        mCameraControlView = findViewById(R.id.camera_control_view)
        mCallTimeView = findViewById(R.id.tv_time_count)

        textureViewVoip.getTextTureView().surfaceTextureListener = this
        surfaceViewVoip.getTextTureView().surfaceTextureListener = this

        when (TweCallStateManager.callRole.value) {
            CallRole.Caller -> {
                mVideoCallView = VideoCallerWaitingAndAcceptView(context)
                mVideoViewContainer?.addView(mVideoCallView)
                timeUtil.startCountdown {
                    if (TweCallStateManager.callStatus.value == CallStatus.Waiting) {
                        TweCallManager.sendCommand(CommandDeviceCallEnum.COMMAND_DEVICE_CALL_HANGUP_V2.command)
                    }
                    TweCallActivity.finishActivity()
                }
            }

            CallRole.Called -> {
                mVideoCallView = VideoCalledWaitingView(context)
                mVideoViewContainer?.addView(mVideoCallView)
                timeUtil.startCountdown { TweCallActivity.finishActivity() }
            }

            else -> {
                LogUtil.e(msg = "VideoCallKitView   TweCallStateManager.callRole.value None")
            }
        }

        if (LauncherDataUtil.isNeedTurnCameraMirror()) {
            mTurnCameraMirrorViewContainer?.addView(TurnCameraMirrorView(context))
            mTurnCameraMirrorViewContainer?.expandTouchArea(context.resources.getDimensionPixelSize(R.dimen.x5))
        }

    }

    override fun initListener() {
    }

    override fun initData() {
        if (TweCallStateManager.callRole.value == CallRole.Caller) {
            LogUtil.e(msg = "开始拨打视频 openId =${TweCallActivity.openId}")
            doWxCloudVoipCall()
        } else {
            TweCallManager.playWaitingRingVoice(type = 2)
        }
    }


    override fun addObserver() {
        TweCallStateManager.clearScreenViewLiveData.observeForever(mClearScreenViewObserver)
    }

    override fun removeObserver() {
        TweCallStateManager.clearScreenViewLiveData.removeObserver(mClearScreenViewObserver)
        mUserInfoView?.removeObserver()
        mCameraControlView?.removeObserver()
        mCallTimeView?.removeObserver()
        mVideoCallView?.clear()
        textureViewVoip?.removeObserver()
        surfaceViewVoip?.removeObserver()
        mHandler.removeCallbacksAndMessages(null)
        coroutineScope.cancel()
        timeUtil.stopCountdown()
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        when (surface) {
            textureViewVoip.getTextTureView().surfaceTexture -> TweCallManager.openCamera(surfaceTexture = surface)
            surfaceViewVoip.getTextTureView().surfaceTexture -> TweCallManager.startVideoPlay(
                surfaceTexture = surface,
                surfaceViewVoip.getTextTureView()
            )
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        if (surface == textureViewVoip.getTextTureView().surfaceTexture) {
            TweCallManager.closeCamera()
        }
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}


    fun changeTextureView() {
        if (isTextureViewFullScreen) {
            surfaceViewVoip.changeFullView()
            textureViewVoip.changeSmallView()
        } else {
            textureViewVoip.changeFullView()
            surfaceViewVoip.changeSmallView()
        }
        isTextureViewFullScreen = !isTextureViewFullScreen
    }

    private fun doWxCloudVoipCall() {
        TweCallActivity.openId?.let {
            TweCallManager.doWxCloudVoipCall(openId = it, 1, callStatusListener = object : IVoipCallStatusListener {
                override fun onLineBusy(callStatus: Int, callMessage: String?) {
                    Toast.makeText(context, callMessage, Toast.LENGTH_SHORT).show()
                    TweCallActivity.finishActivity()
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
                    TweCallActivity.finishActivity()
                }
            })
        }
    }


    /**
     * 清屏或者显示
     */
    internal fun switchScreenView() {
        if (isClearScreen) {
            showScreenView()
        } else {
            clearScreenView()
        }
    }

    /**
     * 视频接听的时候  隐藏屏幕中的view
     */
    private fun clearScreenView() {
        if (TweCallStateManager.callStatus.value == CallStatus.AudioAccept || TweCallStateManager.callStatus.value == CallStatus.VideoAccept) {
            isClearScreen = true
            TweCallStateManager.clearScreenViewLiveData.value = true
            mCallTimeView?.gone()
            mCameraControlView?.gone()
            mVideoViewContainer?.gone()
            mTurnCameraMirrorViewContainer?.gone()
        }
    }

    /**
     * 视频接听的时候  显示屏幕中的view
     */
    private fun showScreenView() {
        if (TweCallStateManager.callStatus.value == CallStatus.AudioAccept || TweCallStateManager.callStatus.value == CallStatus.VideoAccept) {
            isClearScreen = false
            TweCallStateManager.clearScreenViewLiveData.value = false
            mCallTimeView?.visible()
            mCameraControlView?.visible()
            mVideoViewContainer?.visible()
            mTurnCameraMirrorViewContainer?.visible()
        }
    }

}