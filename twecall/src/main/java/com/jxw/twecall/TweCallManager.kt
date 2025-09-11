package com.jxw.twecall

import android.annotation.SuppressLint
import android.content.Context
import com.jxw.basemodel.constant.ConstantDataStore
import com.jxw.basemodel.network.DataRepository
import com.jxw.basemodel.utils.DataStoreUtils
import com.jxw.basemodel.utils.LauncherDataUtil
import com.jxw.twecall.base.BaseTweCallManager
import com.jxw.twecall.constant.Constant
import com.jxw.twecall.listener.IDeviceCallStatusListener
import com.jxw.twecall.listener.IHangUpStatusListener
import com.jxw.twecall.listener.IVoipCallStatusListener
import com.jxw.twecall.listener.IWxCallStatusListener
import com.jxw.twecall.manager.TweCallStateManager
import com.jxw.twecall.manager.TweCallStateManager.fromAccount
import com.jxw.twecall.manager.TweCallStateManager.initCalledAudio
import com.jxw.twecall.manager.TweCallStateManager.initCalledVideo
import com.jxw.twecall.manager.TweCallStateManager.initCallerAudio
import com.jxw.twecall.manager.TweCallStateManager.initCallerVideo
import com.jxw.twecall.manager.TweCallStateManager.toAccount
import com.jxw.twecall.model.CallMediaType
import com.jxw.twecall.model.CallRole
import com.jxw.twecall.model.CommandDeviceCallEnum
import com.jxw.twecall.model.ErrorInfoEnum
import com.jxw.twecall.model.TweCallInitStatus
import com.jxw.twecall.ui.TweCallActivity
import com.jxw.twecall.utils.LogUtil
import com.jxw.twecall.utils.deleteDeviceKeyFile
import com.jxw.twecall.utils.ext.startActivity
import com.tencent.iot.video.device.VideoNativeInterface
import com.tencent.iot.video.device.annotations.LogLevelType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rxhttp.wrapper.exception.ParseException

/**
 *
 * @author: jyq
 * @desc: twecall管理类
 * @date: 2024/10/31
 */
object TweCallManager : BaseTweCallManager() {

    /**
     * 初始化 （带校验的初始化）
     */
    fun init(context: Context) {
        LogUtil.initLog(BuildConfig.DEBUG)
        LogUtil.w(msg = "init 开始初始化 当前初始化状态 $initStatus")
        if (initStatus == TweCallInitStatus.INIT_STATUS_FAIL) {
            initStatus = TweCallInitStatus.INIT_STATUS_INITIALIZING
            mTweCallInitListener?.initializing()
            checkActivateStatus(context)
            upLoadAppInfo()
        }
    }

    /**
     * 初始化
     */
    private fun init(
        context: Context,
        productId: String,
        deviceName: String,
        deviceKey: String
    ) {
        LogUtil.w(msg = "productId = $productId   deviceName = $deviceName    deviceKey = $deviceKey")
        VideoNativeInterface.getInstance().initLog(if (BuildConfig.DEBUG) LogLevelType.IV_eLOG_DEBUG else LogLevelType.IV_eLOG_DISABLE)
        val isActivateStatus = DataStoreUtils.getData(ConstantDataStore.TWE_CALL_DEVICE_ACTIVATE_STATUS, false)
        if (isActivateStatus && deviceName.isNotEmpty() && deviceKey.isNotEmpty()) {
            initTweCall(context, productId, deviceName, deviceKey)
        } else {
            initStatus = TweCallInitStatus.INIT_STATUS_FAIL
            mTweCallInitListener?.initFail()
            LogUtil.e(msg = "init  当前设备未激活 无法开始初始化")
        }
    }

    override fun initWxCloudVoip() {
        val mModelId = DataStoreUtils.getData(ConstantDataStore.TWE_CALL_MODEL_ID, "")
        val mWxaAppId = DataStoreUtils.getData(ConstantDataStore.TWE_CALL_WX_APP_ID, "")
        LogUtil.w(msg = "mModelId = $mModelId    mWxaAppId = $mWxaAppId")
        coroutineScope.launch(Dispatchers.Main) {
            val status =
                VideoNativeInterface.getInstance().initWxCloudVoipV2(DATA_PATH, mModelId, mWxaAppId, mMiniprogramVersion, null)
            LogUtil.e(msg = "initWxCloudVoip  staus==$status")
            when (status) {
                0 -> {
                    LogUtil.e(msg = "initWxCloudVoip  TweCall 初始化成功 status = 0")
                    registerAvtVoip()
                }

                19 -> {
                    deleteDeviceKeyFile(DATA_PATH)
                    val status =
                        VideoNativeInterface.getInstance().initWxCloudVoipV2(DATA_PATH, mModelId, mWxaAppId, mMiniprogramVersion, null)
                    if (status == 0) {
                        initStatus = TweCallInitStatus.INIT_STATUS_SUCCESS
                        mTweCallInitListener?.initSuccess()
                        LogUtil.e(msg = "initWxCloudVoip  TweCall 初始化成功 status = 0")
                    } else {
                        initStatus = TweCallInitStatus.INIT_STATUS_FAIL
                        mTweCallInitListener?.initFail()
                        LogUtil.e(msg = "initWxCloudVoip  TweCall 初始化失败 status = $status  ${ErrorInfoEnum.getMessageByCode(status)}")
                    }
                }

                else -> {
                    initStatus = TweCallInitStatus.INIT_STATUS_FAIL
                    mTweCallInitListener?.initFail()
                    LogUtil.e(msg = "initWxCloudVoip  TweCall 初始化失败 status = $status  ${ErrorInfoEnum.getMessageByCode(status)}")
                }
            }
        }
    }

    /**
     * 开始注册
     */
    private fun registerAvtVoip() {
        var registerStatus = VideoNativeInterface.getInstance().isAvtVoipRegistered()
        if (registerStatus == 0) {
            LogUtil.w(msg = "initWxCloudVoip  TweCall 首次注册 registerStatus = 0")
            getSnTicketInfo()
        } else {
            initStatus = TweCallInitStatus.INIT_STATUS_SUCCESS
            mTweCallInitListener?.initSuccess()
            LogUtil.w(msg = "initWxCloudVoip  TweCall 已经注册过 registerStatus = $registerStatus")
        }
    }

    /**
     * 发起通话
     * @param callType 0 音频 1视频
     */
    @SuppressLint("WrongConstant")
    fun doWxCloudVoipCall(
        openId: String,
        callType: Int,
        recvPixel: Int = LauncherDataUtil.getReceivePixel(),
        callerCameraSwitch: Boolean = true,
        calleeCameraSwitch: Boolean = true,
        callStatusListener: IVoipCallStatusListener? = null
    ) {
        if (VideoNativeInterface.getInstance().isWxCloudVoipBusyV2()) {
            sendCommand(CommandDeviceCallEnum.COMMAND_DEVICE_CALL_BUSY.command)
            callStatusListener?.onLineBusy(-2, mContext?.get()?.getString(R.string.call_line_busy))
            return
        }
        coroutineScope.launch {
            val callStatus =
                VideoNativeInterface.getInstance().doWxCloudVoipCallV2(
                    openId,
                    callType,
                    recvPixel,
                    callerCameraSwitch,
                    calleeCameraSwitch
                )
            withContext(Dispatchers.Main) {
                when (callStatus) {
                    -2 -> callStatusListener?.onLineBusy(callStatus, mContext?.get()?.getString(R.string.call_line_busy))
                    0 -> callStatusListener?.callSuccess(callStatus, mContext?.get()?.getString(R.string.call_success))
                    else -> callStatusListener?.callFail(callStatus, mContext?.get()?.getString(R.string.call_fail))
                }
            }
        }
    }

    /**
     * 挂断 音视频通话
     */
    fun doWxCloudVoipHangUp(hangUpListener: IHangUpStatusListener? = null) {
        coroutineScope.launch {
            val hangUpStatus = VideoNativeInterface.getInstance().doWxCloudVoipHangUpV2()
            when (hangUpStatus) {
                0 -> hangUpListener?.hangUpSuccess(hangUpStatus, mContext?.get()?.getString(R.string.hangup_success))
                else -> hangUpListener?.hangUpFail(hangUpStatus, mContext?.get()?.getString(R.string.hangup_fail))
            }
        }
    }

    /**
     * 添加wx呼叫设备监听(监听微信的动作)
     */
    fun addWxCallDeviceListener(listener: IWxCallStatusListener) {
        if (mWxCallStatusListeners.contains(listener).not()) {
            mWxCallStatusListeners.add(listener)
        }
    }

    /**
     * 移除wx呼叫设备监听(监听微信的动作)
     */
    fun removeWxCallDeviceListener(listener: IWxCallStatusListener) {
        if (mWxCallStatusListeners.contains(listener)) {
            mWxCallStatusListeners.remove(listener)
        }
    }


    /**
     * 添加设备呼叫vx监听(监听微信的动作)
     */
    fun addDeviceCallWxListener(listener: IDeviceCallStatusListener) {
        if (mDeviceCallStatusListeners.contains(listener).not()) {
            mDeviceCallStatusListeners.add(listener)
        }
    }

    /**
     *  移除设备呼叫vx监听(监听微信的动作)
     */
    fun removeDeviceCallWxListener(listener: IDeviceCallStatusListener) {
        if (mDeviceCallStatusListeners.contains(listener)) {
            mDeviceCallStatusListeners.remove(listener)
        }
    }

    /********************************************************* 以下  数据接口相关 ***************************************************/

    /**
     * 上报 音视频视频通话结果
     */
    fun upLoadLoadCallResult(callResultType: Int, callType: String, callTime: Int = 0) {
        LogUtil.i(msg = "upLoadLoadCallResult 上报 音视频视频通话结果")
        if (TweCallStateManager.callRole.value == CallRole.Caller) {
            coroutineScope.launch {
                DataRepository.upLoadCallResult(
                    fromAccount = fromAccount,
                    toAccount = toAccount,
                    callType = callType,
                    callDuration = callTime,
                    callResultType = callResultType
                )
            }
        }
    }

    /**
     * 获取设备snticket 相关信息
     */
    private fun getSnTicketInfo() {
        LogUtil.i(msg = "getSnTicketInfo 获取设备snticket 相关信息")
        coroutineScope.launch {
            DataStoreUtils.getData(ConstantDataStore.IM_ACCOUNT_INFO, "").takeIf { it.isNotEmpty() }?.let {
                val mModelId = DataStoreUtils.getData(ConstantDataStore.TWE_CALL_MODEL_ID, "")
                val deviceInfo = DataRepository.getSnTicket(it, mModelId, mMiniprogramVersion)
                withContext(Dispatchers.Main) {
                    val registerStatus = VideoNativeInterface.getInstance().registerAvtVoip(deviceInfo?.snticket)
                    if (registerStatus == 0) {
                        initStatus = TweCallInitStatus.INIT_STATUS_SUCCESS
                        mTweCallInitListener?.initSuccess()
                    } else {
                        initStatus = TweCallInitStatus.INIT_STATUS_FAIL
                        mTweCallInitListener?.initFail()
                    }
                    LogUtil.i(msg = "注册状态code为0代表注册成功  当前注册code=$registerStatus ${ErrorInfoEnum.getMessageByCode(registerStatus)}")
                }
            }
        }
    }

    /**
     * 校验 是否已经激活
     */
    private fun checkActivateStatus(context: Context) {
        LogUtil.i(msg = "checkActivateStatus 校验 是否已经激活")
        coroutineScope.launch {
            val activateInfoModel = DataRepository.getActivateInfo()
            activateInfoModel?.modelId?.let { DataStoreUtils.putData(ConstantDataStore.TWE_CALL_MODEL_ID, it) }
            activateInfoModel?.wxAppId?.let { DataStoreUtils.putData(ConstantDataStore.TWE_CALL_WX_APP_ID, it) }
            activateInfoModel?.productId?.let { DataStoreUtils.putData(ConstantDataStore.TWE_CALL_PRODUCT_ID, it) }
            activateInfoModel?.deviceSecret?.let { DataStoreUtils.putData(ConstantDataStore.TWE_CALL_DEVICE_KEY, it) }
            activateInfoModel?.getTweCallDeviceName()?.let { DataStoreUtils.putData(ConstantDataStore.TWE_CALL_DEVICE_NAME, it) }
            if (activateInfoModel?.getActivateStatus() == true) {
                val activateDevice = DataRepository.activateDevice { it ->
                    (it as? ParseException)?.let { LogUtil.e(msg = "接口异常  激活失败 code = " + it.errorCode + "   message=" + it.message) }
                }
                LogUtil.i(msg = "twecall 激活状态activateDevice$activateDevice")
                if (activateDevice == true) {
                    DataStoreUtils.putData(ConstantDataStore.TWE_CALL_DEVICE_ACTIVATE_STATUS, true)
                } else {
                    DataStoreUtils.putData(ConstantDataStore.TWE_CALL_DEVICE_ACTIVATE_STATUS, false)
                }
            } else {
                DataStoreUtils.putData(ConstantDataStore.TWE_CALL_DEVICE_ACTIVATE_STATUS, true)
            }
            init(
                context = context,
                productId = DataStoreUtils.getData(ConstantDataStore.TWE_CALL_PRODUCT_ID, ""),
                deviceName = DataStoreUtils.getData(ConstantDataStore.TWE_CALL_DEVICE_NAME, ""),
                deviceKey = DataStoreUtils.getData(ConstantDataStore.TWE_CALL_DEVICE_KEY, ""),
            )
        }
    }

    /**
     * 上报当前应用版本信息
     */
    private fun upLoadAppInfo() {
        coroutineScope.launch { DataRepository.upLoadAppInfo() }
    }

    /**
     * 开启音视频通话
     * @param callType 通讯类型 0主动呼叫音频 1主动呼叫视频 2被动接受音频 3被动接受视频
     * @param openId
     * @param userHeader
     * @param userName
     * @param fromAccount
     * @param toAccount
     */
    fun startAudioAndVideoCall(
        callType: Int,
        openId: String = "",
        userHeader: String = "",
        userName: String = "",
        fromAccount: String?,
        toAccount: String?
    ) {
        when (callType) {
            0 -> {
                initCallerAudio()
                TweCallStateManager.callMediaType = CallMediaType.Audio
            }

            1 -> {
                initCallerVideo()
                TweCallStateManager.callMediaType = CallMediaType.Video
            }

            2 -> {
                initCalledAudio()
                TweCallStateManager.callMediaType = CallMediaType.Audio
            }

            3 -> {
                initCalledVideo()
                TweCallStateManager.callMediaType = CallMediaType.Video
            }
        }

        mContext?.get()?.startActivity<TweCallActivity>(
            bundle = arrayOf(
                Constant.TWE_CALL_USER_OPEN_ID to openId,
                Constant.TWE_CALL_USER_HEADER_URL to userHeader,
                Constant.TWE_CALL_USER_NAME to userName,
                Constant.TWE_CALL_FROM_ACCOUNT to fromAccount,
                Constant.TWE_CALL_TO_ACCOUNT to toAccount,
            )
        )
    }
}