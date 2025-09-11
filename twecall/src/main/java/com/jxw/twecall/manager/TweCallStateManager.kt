package com.jxw.twecall.manager

import androidx.lifecycle.MutableLiveData
import com.jxw.twecall.model.CallMediaType
import com.jxw.twecall.model.CallRole
import com.jxw.twecall.model.CallStatus

/**
 *
 * @author: jyq
 * @desc:  呼叫过程中状态 管理
 * @date: 2024/11/18
 */
internal object TweCallStateManager {
    var isAlreadyLink = false  //音视频流是否已经连接 （用来判断是否 设备呼叫小程序，小程序超时和拒接的情况）
    var fromAccount: String? = null   //呼叫的 账号
    var toAccount: String? = null   //被呼叫的账号

    @Volatile
    var callMediaType: CallMediaType? = null //通讯类型

    var isCameraState = MutableLiveData<Boolean>().apply { value = false }    //相机状态
    var isWxCameraState = MutableLiveData<Boolean>().apply { value = true }    //小程序相机状态
    var isMicrophoneMuteState = MutableLiveData<Boolean>().apply { value = true }  //麦克风状态 默认开启
    var isSpeakerState = MutableLiveData<Boolean>().apply { value = true }    //扬声器状态 默认开启

    var mediaType = MutableLiveData<CallMediaType>().apply { value = CallMediaType.Unknown }  //通讯类型
    var callRole = MutableLiveData<CallRole>().apply { value = CallRole.None }  //通讯角色
    var callStatus = MutableLiveData<CallStatus>().apply { value = CallStatus.None }  //通讯状态
    var clearScreenViewLiveData = MutableLiveData<Boolean>().apply { value = false }  //清理 屏幕所有view
    var callTimeLiveData = MutableLiveData<Int>().apply { value = 0 }  //通讯时长

    var isCallingHangUpLiveData = MutableLiveData<Boolean>().apply { value = false }  //是否是通话过程中挂断
    fun init() {
        clear()
    }

    /**
     * 初始化 音频状态参数（拨打音频前 调用）
     */
    fun initCallerAudio() {
        clear()
        mediaType.value = CallMediaType.Audio
        callRole.value = CallRole.Caller
    }

    /**
     * 初始化 音频状态参数（接受音频前 调用）
     */
    fun initCalledAudio() {
        clear()
        mediaType.value = CallMediaType.Audio
        callRole.value = CallRole.Called
    }

    /**
     * 初始化 视频状态参数（拨打视频前 调用）
     */
    fun initCallerVideo() {
        clear()
        this.mediaType.value = CallMediaType.Video
        this.callRole.value = CallRole.Caller
    }

    /**
     * 初始化 视频状态参数（接受视频前 调用）
     */
    fun initCalledVideo() {
        clear()
        this.mediaType.value = CallMediaType.Video
        this.callRole.value = CallRole.Called
    }

    private fun clear() {
        isAlreadyLink = false
        isCameraState.value = true
        isWxCameraState.value = true
        isMicrophoneMuteState.value = true
        isSpeakerState.value = true
        mediaType.value = CallMediaType.Unknown
        callRole.value = CallRole.None
        callStatus.value = CallStatus.None
        isCallingHangUpLiveData.value = false
        clearScreenViewLiveData.value = false
        callTimeLiveData.value = 0
        fromAccount = null
        toAccount = null
        callMediaType = null
    }

}