package com.jxw.twecall.base

import android.content.Context
import android.graphics.SurfaceTexture
import android.text.TextUtils
import android.view.Surface
import android.view.TextureView
import com.jxw.basemodel.network.UrlManager
import com.jxw.twecall.TweCallManager
import com.jxw.twecall.listener.AvtListener
import com.jxw.twecall.listener.DeviceStatusListener
import com.jxw.twecall.listener.IDeviceCallStatusListener
import com.jxw.twecall.listener.IWxCallStatusListener
import com.jxw.twecall.listener.TweCallInitListener
import com.jxw.twecall.manager.ActivityManager
import com.jxw.twecall.manager.AudioSpeakerManager
import com.jxw.twecall.manager.CameraRecorder2
import com.jxw.twecall.manager.CheckVideoStreamManager
import com.jxw.twecall.manager.MediaPlayerManager
import com.jxw.twecall.manager.PowerScreenManager
import com.jxw.twecall.manager.SimplePlayer
import com.jxw.twecall.manager.TweCallStateManager
import com.jxw.twecall.manager.TweCallStateManager.isAlreadyLink
import com.jxw.twecall.model.CallStatus
import com.jxw.twecall.model.CommandDeviceCallEnum
import com.jxw.twecall.model.CommandResponseModel
import com.jxw.twecall.model.CommandTweCallModel
import com.jxw.twecall.model.CommandWxCallEnum
import com.jxw.twecall.model.TweCallInitStatus
import com.jxw.twecall.ui.TweCallActivity
import com.jxw.twecall.utils.LogUtil
import com.jxw.twecall.utils.ext.toBean
import com.jxw.twecall.utils.ext.toBeanOrNull
import com.jxw.twecall.utils.ext.tryCatch
import com.jxw.twecall.widget.MoveTextureViewHelper
import com.tencent.iot.video.device.VideoNativeInterface
import com.tencent.iot.video.device.annotations.StreamType
import com.tencent.iot.video.device.consts.P2pEventType
import com.tencent.iot.video.device.model.AvDataInfo
import com.tencent.iot.video.device.model.AvtInitInfo
import com.tencent.iot.video.device.model.CongestionCtrlInfo
import com.tencent.iot.video.device.model.SysInitInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.ref.WeakReference

/**
 *
 * @author: jyq
 * @desc: twecall  base
 * @date: 2024/11/5
 */
open class BaseTweCallManager {
    private val mRegion = "china"
    internal val mMiniprogramVersion = UrlManager.getMiniProgramVersion()
    internal val DATA_PATH = "/storage/emulated/0/"    //device_key 文件保存路径
    internal val mWxCallStatusListeners: HashSet<IWxCallStatusListener> = HashSet()
    internal val mDeviceCallStatusListeners: HashSet<IDeviceCallStatusListener> = HashSet()

    // 协程作用域，用于管理协程的生命周期
    private val supervisorJob = SupervisorJob()
    internal val coroutineScope = CoroutineScope(Dispatchers.IO + supervisorJob)

    private val mCameraRecorder by lazy { CameraRecorder2() }
    private val mCheckVideoStreamManager by lazy { CheckVideoStreamManager() }
    open var mContext: WeakReference<Context>? = null
    private val mPlayer by lazy { SimplePlayer() }
    internal var mTweCallInitListener: TweCallInitListener? = null
    internal var initStatus = TweCallInitStatus.INIT_STATUS_FAIL  //初始化状态

    private var mVisitor: Int = 0
    private var mChannel: Int = 0
    private var mVideoResType: Int = 0
    private var mType: Int = 0
    private var mHeight: Int = 0
    private var mWidth: Int = 0
    private var condition1 = false
    private var condition2 = false
    private val lock = Any()
    private var mRemotePreviewSurface: SurfaceTexture? = null //远程视频流展示view
    private var mRemoteTextureView: TextureView? = null //远程视频流展示view
    private var mCameraPreviewSurface: SurfaceTexture? = null //相机预览view


    open fun initTweCall(context: Context, productId: String, deviceName: String, deviceKey: String) {
        LogUtil.d(msg = "initTweCall 真正开始初始化")
        this.mContext = WeakReference(context)
        mPlayer.init(context)
        PowerScreenManager.init(context)
        MediaPlayerManager.init(context)
        coroutineScope.launch(Dispatchers.Main) { TweCallStateManager.init() }

        val info = SysInitInfo(productId, deviceName, deviceKey, mRegion)
        val sysInit = VideoNativeInterface.getInstance().initIvSystem(info, object : DeviceStatusListener {
            override fun onOnline(netDateTime: Long) {
                super.onOnline(netDateTime)
                initWxCloudVoip()
            }

            override fun onOffline(status: Int) {
                super.onOffline(status)
                VideoNativeInterface.getInstance().exitWxCloudVoipV2()
                if (status == 1) {
                    initStatus = TweCallInitStatus.INIT_STATUS_FAIL
                    mTweCallInitListener?.initFail()
                    coroutineScope.launch(Dispatchers.Main) {
                        val exitIvAvt = VideoNativeInterface.getInstance().exitIvAvt()
                        LogUtil.e(msg = "exit avt resCode:$exitIvAvt")
                        val exitIvDm = VideoNativeInterface.getInstance().exitIvDm()
                        LogUtil.e(msg = "exit dm resCode:$exitIvDm")
                        val exitIvSys = VideoNativeInterface.getInstance().exitIvSys()
                        LogUtil.e(msg = "exit sys resCode:$exitIvSys")
                    }
                }
                LogUtil.e(msg = "ActivityManager.top():${ActivityManager.top()}")
                if (ActivityManager.top() is TweCallActivity) {
                    ActivityManager.top()?.let {
                        ActivityManager.clear()
                    }
                }
            }
        })
        LogUtil.e(msg = "initIvSystem  initStatus==$sysInit")
        if (sysInit != 0) {
            initStatus = TweCallInitStatus.INIT_STATUS_FAIL
            mTweCallInitListener?.initFail()
            return
        }
        val dmInit = VideoNativeInterface.getInstance().initIvDm()
        LogUtil.e(msg = "initIvDm  initIvDm==$dmInit")
        if (dmInit != 0) {
            initStatus = TweCallInitStatus.INIT_STATUS_FAIL
            mTweCallInitListener?.initFail()
            return
        }
        //启用水位告警以及告警的有高中低三挡水位值，当 p2p 内部缓存的水位到达这个值的时候会收到 onNotify回调
        val congestion = CongestionCtrlInfo().apply {
            lowMark = 200 * 1024
            warnMark = 400 * 1024
            highMark = 500 * 1024
        }
        val avtInit = VideoNativeInterface.getInstance().initIvAvt(AvtInitInfo().apply { this.congestion = congestion }, object : AvtListener {
            override fun onGetAvEncInfo(visitor: Int, channel: Int, videoResType: Int): AvDataInfo {
                mVisitor = visitor
                mChannel = channel
                mVideoResType = videoResType
                return super.onGetAvEncInfo(visitor, channel, videoResType)
            }

            override fun onStartRealPlay(visitor: Int, channel: Int, videoResType: Int) {
                isAlreadyLink = false
                LogUtil.d(msg = "onStartRealPlay  visitor $visitor channel $channel res_type $videoResType" + " = treadName =" + Thread.currentThread().name)
                mCameraRecorder.startRecording(visitor, videoResType)
            }

            override fun onStopRealPlay(visitor: Int, channel: Int, videoResType: Int) {
                LogUtil.d(msg = "onStopRealPlay  visitor $visitor channel $channel res_type $videoResType")
                if (!isAlreadyLink) {
                    coroutineScope.launch(Dispatchers.Main) {
                        for (listener in mDeviceCallStatusListeners) {
                            listener.onWxCancelCallBack()
                        }
                    }
                }
                mCameraRecorder.stopRecording(visitor, videoResType)
                TweCallStateManager.callStatus.postValue(CallStatus.HangUp)
            }

            override fun onStartRecvAudioStream(
                visitor: Int,
                channel: Int,
                type: Int,
                option: Int,
                mode: Int,
                width: Int,
                sample_rate: Int,
                sample_num: Int
            ): Int {
                LogUtil.d(msg = "onStartRecvAudioStream  visitor $visitor channel $channel type $type")
                isAlreadyLink = true
                TweCallStateManager.callStatus.postValue(CallStatus.AudioAccept)
                val audioPlay = mPlayer.startAudioPlay(visitor, type, option, mode, width, sample_rate, sample_num)
                coroutineScope.launch {
                    withContext(Dispatchers.Main) {
                        openRecording()
                        openSpeaker()
                    }
                }
                return audioPlay
            }

            override fun onStopRecvStream(visitor: Int, channel: Int, streamType: Int): Int {
                //twecall bug 音视频通话返回的都是 StreamType.IV_AVT_STREAM_TYPE_AV 目前不处理
                return when (streamType) {
                    StreamType.IV_AVT_STREAM_TYPE_AV -> {
                        LogUtil.d(msg = "onStopRecvStream 小程序设备接通后 任意一方挂断了当前音频或者视频通话  stream_type $streamType")
                        TweCallStateManager.isCallingHangUpLiveData.postValue(true)
                        TweCallStateManager.callStatus.postValue(CallStatus.HangUp)
                        mPlayer.stopAudioPlay(visitor)
                        mPlayer.stopVideoPlay(visitor)
                    }

                    else -> {
                        LogUtil.d(msg = "onStopRecvStream  stream_type $streamType")
                        0
                    }
                }
            }

            override fun onRecvStream(visitor: Int, streamType: Int, data: ByteArray, len: Int, pts: Long, seq: Long): Int {
//                LogUtil.d(msg = "onRecvStream  visitor=$visitor streamType=$streamType data=$data  len=$len  pts=$pts ")
                if (streamType == 1) {
                    mCheckVideoStreamManager.updateVideoStreamCurrentTime()
                    return mPlayer.playVideoStream(visitor, data, len, pts, seq)
                } else if (streamType == 0) {
                    return mPlayer.playAudioStream(visitor, data, len, pts, seq)
                }
                return super.onRecvStream(visitor, streamType, data, len, pts, seq)
            }

            override fun onStartRecvVideoStream(visitor: Int, channel: Int, type: Int, height: Int, width: Int, frameRate: Int): Int {
                LogUtil.d(msg = "onStartRecvVideoStream  visitor $visitor channel $channel type $type")
                mChannel = channel
                mVisitor = visitor
                mType = type
                mHeight = height
                mWidth = width
                mCheckVideoStreamManager.openCheckVideoStreamJob()
                synchronized(lock) {
                    condition2 = true
                    checkConditions()
                }
                isAlreadyLink = true
                TweCallStateManager.callStatus.postValue(CallStatus.VideoAccept)

                coroutineScope.launch { withContext(Dispatchers.Main) { openRecording() } }
                coroutineScope.launch { withContext(Dispatchers.Main) { openSpeaker() } }
                return if (mRemotePreviewSurface != null) 0 else -1
            }

            override fun onNotify(event: Int, visitor: Int, channel: Int, videoResType: Int) {
                var msg = ""
                when (event) {
                    P2pEventType.IV_AVT_EVENT_P2P_PEER_CONNECT_FAIL, P2pEventType.IV_AVT_EVENT_P2P_PEER_ERROR -> {
                        LogUtil.d(msg = "receive event: peer error")
                        msg = "network err"
//                        ToastUtil.showTips("当前网络不佳")
                    }

                    P2pEventType.IV_AVT_EVENT_P2P_PEER_ADDR_CHANGED -> {
                        LogUtil.d(msg = "receive event: peer addr change")
                        msg = "peer change"
                    }

                    P2pEventType.IV_AVT_EVENT_P2P_PEER_READY, P2pEventType.IV_AVT_EVENT_P2P_WATERMARK_LOW, P2pEventType.IV_AVT_EVENT_P2P_WATERMARK_WARN, P2pEventType.IV_AVT_EVENT_P2P_WATERMARK_HIGH, P2pEventType.IV_AVT_EVENT_P2P_LOCAL_NET_READY -> {}
                    else -> {
                        LogUtil.d(msg = "not support event")
                        msg = "unsupport event type $event"
                    }
                }
                LogUtil.w(msg = "onNotify()   msg=== $msg" + "event====" + event)
            }

            override fun onRecvCommand(command: Int, visitor: Int, channel: Int, videoResType: Int, args: String?): String {
                super.onRecvCommand(command, visitor, channel, videoResType, args)
                val commandTweCallModel = args?.toBeanOrNull<CommandTweCallModel>()
                commandTweCallModel?.let {
                    when (it.type) {
                        CommandWxCallEnum.COMMAND_WX_CALL_AUDIO_START.command -> {
                            LogUtil.w(msg = "onRecvCommand 接收小程序发起音频请求")
                            coroutineScope.launch(Dispatchers.Main) {
                                PowerScreenManager.startAcquire(0)
                                TweCallManager.startAudioAndVideoCall(
                                    callType = 2,
                                    userHeader = it.userHeader,
                                    userName = it.userName,
                                    fromAccount = it.fromAccount,
                                    toAccount = it.toAccount
                                )
                                TweCallStateManager.callStatus.postValue(CallStatus.Waiting)
                                for (listener in mWxCallStatusListeners) {
                                    listener.onWxCallAudioStart()
                                }
                            }
                        }

                        CommandWxCallEnum.COMMAND_WX_CALL_VIDEO_START.command -> {
                            LogUtil.w(msg = "onRecvCommand 接收小程序发起视频请求")
                            coroutineScope.launch(Dispatchers.Main) {
                                PowerScreenManager.startAcquire(0)
                                TweCallManager.startAudioAndVideoCall(
                                    callType = 3,
                                    userHeader = it.userHeader,
                                    userName = it.userName,
                                    fromAccount = it.fromAccount,
                                    toAccount = it.toAccount
                                )
                                TweCallStateManager.callStatus.postValue(CallStatus.Waiting)
                                for (listener in mWxCallStatusListeners) {
                                    listener.onWxCallVideoStart()
                                }
                            }
                        }

                        else -> {}
                    }
                }
                when (args) {
                    CommandWxCallEnum.COMMAND_WX_CALL_START.command -> {
                        LogUtil.w(msg = "onRecvCommand 接收到小程序的呼叫")
                        coroutineScope.launch(Dispatchers.Main) {
                            for (listener in mWxCallStatusListeners) {
                                listener.onWxCallStart()
                            }
                        }
                    }

                    CommandWxCallEnum.COMMAND_WX_CALL_CANCEL.command -> {
                        LogUtil.w(msg = "onRecvCommand 小程序取消呼叫")
                        coroutineScope.launch(Dispatchers.Main) {
                            TweCallStateManager.callStatus.postValue(CallStatus.HangUp)
                            for (listener in mWxCallStatusListeners) {
                                listener.onWxCallCancel()
                            }
                        }
                    }

                    CommandWxCallEnum.COMMAND_WX_CALL_HANGUP.command -> {
                        LogUtil.w(msg = "onRecvCommand 小程序挂断")
                        coroutineScope.launch(Dispatchers.Main) {
                            for (listener in mWxCallStatusListeners) {
                                listener.onWxCallHangUp()
                            }
                        }
                    }

                    CommandWxCallEnum.COMMAND_WX_CALL_TIMEOUT.command -> {
                        LogUtil.w(msg = "onRecvCommand 小程序呼叫超时")
                        coroutineScope.launch(Dispatchers.Main) {
                            for (listener in mWxCallStatusListeners) {
                                listener.onWxCallTimeOut()
                            }
                        }
                    }

                    CommandWxCallEnum.COMMAND_WX_CALL_DEVICE_STATUS.command -> {
                        if (VideoNativeInterface.getInstance().isWxCloudVoipBusyV2()) {
                            LogUtil.w(msg = "onRecvCommand 小程序从app获取设备通讯状态 busy")
                            sendCommand(CommandDeviceCallEnum.COMMAND_DEVICE_CALL_DEVICE_STATUS.command, "status" to 1)
                        } else {
                            LogUtil.w(msg = "onRecvCommand 小程序从app获取设备通讯状态 not business")
                            sendCommand(CommandDeviceCallEnum.COMMAND_DEVICE_CALL_DEVICE_STATUS.command, "status" to 0)
                        }
                    }
                }
                return ""
            }

        })
        LogUtil.e(msg = "initIvAvt  initIvAvt==$avtInit")
        if (avtInit != 0) {
            initStatus = TweCallInitStatus.INIT_STATUS_FAIL
            mTweCallInitListener?.initFail()
            return
        }

    }

    open fun initWxCloudVoip() {}

    private fun checkConditions() {
        if (condition1 && condition2 && mRemotePreviewSurface != null) {
            mPlayer.startVideoPlay(Surface(mRemotePreviewSurface), mVisitor, mType, mHeight, mWidth)
//            mPlayer.isSaveRecord(true)
        }
    }

    /**
     * 开启视频通话
     */
    fun startVideoPlay(surfaceTexture: SurfaceTexture? = null, textureView: TextureView? = null) {
        this.mRemotePreviewSurface = surfaceTexture
        this.mRemoteTextureView = textureView
        synchronized(lock) {
            condition1 = true
            checkConditions()
        }
    }

    /**
     * 发送 指令（当小程序给设备拨打音视频时候，设备接听， 挂断 ，拒接）
     * @param command 信令
     * @param param 其他参数
     */
    fun sendCommand(command: String, vararg param: Pair<String, Any>): Boolean {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("iv_private_cmd", command)
            param.forEach { jsonObject.put(it.first, it.second) }
            val res = VideoNativeInterface.getInstance().sendCommand(mVisitor, jsonObject.toString(), 1 * 1000)
            LogUtil.e(msg = "sendCommand  command = $command  param = $param  res = $res ")
            return !TextUtils.isEmpty(res) && res.toBean<CommandResponseModel>().status == "0"
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 设备端主动结束P2P视频传输（当小程序给设备拨打音视频时候 挂断 ，拒接）
     */
    fun finishStream() {
        tryCatch<TweCallManager> {
            val sendFinishStreamMsg = VideoNativeInterface.getInstance().sendFinishStreamMsg(mVisitor, mChannel, mVideoResType)
            val sendFinishStream = VideoNativeInterface.getInstance().sendFinishStream(mVisitor, mChannel, mVideoResType)
            LogUtil.e(msg = "sendFinishStreamMsg ==$sendFinishStreamMsg     sendFinishStream==$sendFinishStream")
        }
    }

    /**
     * 重置初始化参数
     */
    fun resetInitParam() {
        mVisitor = 0
        mChannel = 0
        mVideoResType = 0
        mType = 0
        mHeight = 0
        mWidth = 0
        condition1 = false
        condition2 = false
        mRemotePreviewSurface?.release()
        mCameraPreviewSurface?.release()
        mRemotePreviewSurface = null
        mCameraPreviewSurface = null
        supervisorJob.cancelChildren()
        mPlayer.release()
        mCameraRecorder.release()
        MoveTextureViewHelper.resetViewTranslation()
        mCheckVideoStreamManager.closeCheckVideoStreamJob()
        ActivityManager.clear()
        stopWaitingRingVoice()
    }

    /**
     * 退出关闭所有
     */
    fun clear() {
        coroutineScope.cancel()
        resetInitParam()
        VideoNativeInterface.getInstance().exitWxCloudVoipV2()
        VideoNativeInterface.getInstance().exitIvAvt()
        VideoNativeInterface.getInstance().exitIvDm()
        VideoNativeInterface.getInstance().exitIvSys()
    }

    /**
     * 对外暴漏的  twecall初始化监听接口
     */
    fun setInitStatusListener(listener: TweCallInitListener) {
        this.mTweCallInitListener = listener
    }

    /********************************************************* 以下  音频相关 ***************************************************/


    /**
     * 打开扬声器
     */
    fun openSpeaker() {
        AudioSpeakerManager.turnOnSpeaker()
        TweCallStateManager.isSpeakerState.value = true
    }

    /**
     * 关闭扬声器
     */
    fun closeSpeaker() {
        AudioSpeakerManager.turnOffSpeaker()
        TweCallStateManager.isSpeakerState.value = false
    }

    /**
     * 开启麦克风
     */
    fun openRecording() {
        mCameraRecorder.setMuted(false)
        TweCallStateManager.isMicrophoneMuteState.value = true
    }

    /**
     * 关闭麦克风
     */
    fun closeRecording() {
        mCameraRecorder.setMuted(true)
        TweCallStateManager.isMicrophoneMuteState.value = false
    }

    /********************************************************* 以下  相机相关 ***************************************************/


    /**
     * 打开摄像头
     * @param surfaceTexture
     */
    fun openCamera(surfaceTexture: SurfaceTexture? = null) {
        surfaceTexture?.let { mCameraPreviewSurface = it }
        mCameraPreviewSurface?.let {
//            mCameraRecorder.isSaveRecord(true)
            mCameraRecorder.openCamera(it, mContext?.get())
            TweCallStateManager.isCameraState.value = true
        }
    }

    /**
     * 关闭摄像头
     */
    fun closeCamera() {
        mCameraRecorder.closeCamera()
        TweCallStateManager.isCameraState.value = false
    }

    /**
     * 切换摄像头
     */
    fun switchCamera(surfaceTexture: SurfaceTexture? = null) {
        surfaceTexture?.let { mCameraPreviewSurface = it }
        mCameraPreviewSurface?.let { mCameraRecorder.switchCamera(it, mContext?.get()) }
    }

    /**
     * 翻转camera 画面镜像view
     */
    fun switchCameraMirror(surfaceTexture: SurfaceTexture? = null) {
        surfaceTexture?.let { mCameraPreviewSurface = it }
        mCameraPreviewSurface?.let { mCameraRecorder.switchCameraMirror(it, mContext?.get()) }
    }


    /********************************************************* 以下  控制呼叫等待铃声 ***************************************************/
    /**
     * 播放呼叫铃声
     * @param type 呼叫类型  1 音频呼叫 2视频呼叫
     */
    fun playWaitingRingVoice(type: Int) {
        val url = if (type == 1) {
            "wait_call_audio.mp3"
        } else {
            "wait_call_video.mp3"
        }
        MediaPlayerManager.playStart(url = url, isLocal = true, playFinishFunction = {
            if (!isAlreadyLink) {
                playWaitingRingVoice(type)
            }
        })
    }

    /**
     * 停止播放 铃声
     */
    fun stopWaitingRingVoice() {
        MediaPlayerManager.clear()
    }

}

