package com.jxw.basemodel.network

import com.jxw.basemodel.base.BaseApplication
import com.jxw.basemodel.base.ErrorListener
import com.jxw.basemodel.bean.ActivateInfoModel
import com.jxw.basemodel.bean.QrCodeModel
import com.jxw.basemodel.bean.RegisterInfoModel
import com.jxw.basemodel.bean.TweCallDeviceInfoModel
import com.jxw.basemodel.bean.UserInfoModel
import com.jxw.basemodel.bean.UserListModel
import com.jxw.basemodel.constant.ConstantApi
import com.jxw.basemodel.constant.ConstantDataStore
import com.jxw.basemodel.utils.DataStoreUtils
import com.jxw.basemodel.utils.DeviceUtils
import com.jxw.basemodel.utils.PackageUtil
import com.jxw.validate.utils.LauncherDataUtils
import rxhttp.tryAwait
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.toAwaitResponse


/**
 *
 * @author: jyq
 * @date: 2023/12/16
 * @descp: 数据仓库  （所有接口的调用）
 */
object DataRepository {
    /**
     * 获取 绑定二维码
     * @param modelName 默认一个参数
     * @param deviceSeries extraIsbn version  imei（使用客户提供二维码时生效 eg:安用通 缤果 ）
     */
    suspend fun getQrCode(): QrCodeModel? {
        return RxHttp.get(UrlManager.getBaseOldDomain(ConstantApi.WECHAT_QRCODE_API))
            .add("modelName", "chat")
            .add("deviceSeries", LauncherDataUtils.serialNumber)
            .add("extraIsbn", LauncherDataUtils.apkv)
            .add("version", PackageUtil.getAppVersion("com.jxw.launcher")?.first)
            .add("imei", DeviceUtils.getIMEI(BaseApplication.getInstance()))
            .add("flag",1)
            .add("isWxApp","true")
            .add("isEnv",UrlManager.getMiniProgramVersion()!=0)
            .add("wxmpAccount","gh_59d247175286")
            .toAwaitResponse<QrCodeModel>()
            .await()
    }

    /**
     * 获取 用户信息 和 平台对应的SDK_APPID , SECRETKEY
     */
    suspend fun getUseInfo(): RegisterInfoModel {
        return RxHttp.get(UrlManager.getNewBaseDomain(ConstantApi.USER_SIG_CREATE_API))
            .toAwaitResponse<RegisterInfoModel>()
            .await()
    }

    /**
     * 获取设备（孩子信息）
     */
    suspend fun getDeviceBindUserInfo(): UserInfoModel {
        return RxHttp.get(UrlManager.getNewBaseDomain(ConstantApi.GET_DEVICE_BIND_USER_INFO_API))
            .toAwaitResponse<UserInfoModel>()
            .await()
    }

    /**
     * 获取联系人列表（通过查到联系人数量判断是否绑定过设备）
     */
    suspend fun getContactPersonList(): MutableList<UserListModel>? {
        return RxHttp.get(UrlManager.getBaseOldDomain(ConstantApi.CONTACT_PERSON_LIST_API))
            .toAwaitResponse<MutableList<UserListModel>>()
            .await()
    }

    /**
     * 保存用户信息
     */
    suspend fun saveUserInformation(
        icon: String,
        nickname: String,
        onError: ((Throwable) -> Unit)? = null
    ): Any? {
        return RxHttp.postJson(UrlManager.getBaseOldDomain(ConstantApi.SAVE_USER_INFO_API))
            .add("icon", icon)
            .add("nickname", nickname)
            .toAwaitResponse<Any>()
            .tryAwait { onError?.invoke(it) }
    }

    /**
     * 设备回传 (通知小程序 设备端已经准备就绪，小程序可以发起音视频通话了)
     * @param notifyResultType 1-已唤起，2-已拒接
     * Deprecated 优化后已经废弃
     */
    @Deprecated(message = "已废弃")
    suspend fun tweCallDeviceBack(notifyResultType: Int): Any? {
        return RxHttp.postJson(UrlManager.getNewBaseDomain(ConstantApi.TWE_CALL_VIDEO_DEVICE_BACK_API))
            .add("notifyResultType", notifyResultType)
            .toAwaitResponse<Any>()
            .tryAwait()

    }

    /**
     * 获取设备snticket 相关信息
     * @param deviceId  设备id
     * @param modelId twecall 初始化需要的modelId  平台申请的固定值
     * @param miniProgramAppId  小程序版本 0 "正式版", 1  "开发版", 2 "体验版"
     * @param isThirdApp 是否是第三方小程序
     */
    suspend fun getSnTicket(
        deviceId: String,
        modelId: String,
        miniProgramAppId: Int,
        isThirdApp: Int = 1,
        onError: ErrorListener = null
    ): TweCallDeviceInfoModel? {
        return RxHttp.get(UrlManager.getNewBaseDomain(ConstantApi.TWE_CALL_DEVICE_SN_TICKET))
            .add("deviceId", deviceId)
            .add("IsThirdApp", isThirdApp)
            .add("ModelId", modelId)
            .add("MiniProgramAppId", miniProgramAppId)
            .toAwaitResponse<TweCallDeviceInfoModel>()
            .tryAwait { onError?.invoke(it) }
    }

    /**
     * 上报 通话结果
     * @param fromAccount  //拨打方账户
     * @param toAccount  //接受方账户
     * @param callType //通话类型(视频通话：GROUP_CHAT_VIDEO 语音通话：GROUP_CHAT_AUDIO)
     * @param callResultType //通话结果类型 1-拨打成功，2-对方拒接，3-无人接听
     * @param callDuration //通话时长
     */
    suspend fun upLoadCallResult(fromAccount: String?, toAccount: String?, callType: String, callResultType: Int, callDuration: Int): Any? {
        return RxHttp.postJson(UrlManager.getNewBaseDomain(ConstantApi.TWE_CALL_INSERT_CALL_RESULT))
            .add("fromAccount", fromAccount)
            .add("toAccount", toAccount)
            .add("callType", callType)
            .add("callResultType", callResultType)
            .add("callDuration", callDuration)
            .add("businessID", "custom_twe_call")
            .toAwaitResponse<Any>()
            .tryAwait()
    }

    /**
     * 获取好友WXOpenId
     */
    suspend fun getUserOpenId(imUserId: String): String? {
        return RxHttp.get(UrlManager.getNewBaseDomain(ConstantApi.TWE_CALL_GET_OPEN_ID))
            .add("imUserId", imUserId)
            .toAwaitResponse<String>()
            .tryAwait()
    }

    /**
     * 激活twecall
     */
    suspend fun activateDevice(onError: ErrorListener = null): Boolean? {
        return RxHttp.postJson(UrlManager.getNewBaseDomain(ConstantApi.TWE_CALL_ACTIVATE_API))
            .toAwaitResponse<Boolean>()
            .tryAwait { onError?.invoke(it) }
    }

    /**
     * 查询设备激活详情
     * @param deviceId  deviceId
     * @param isThirdApp 是否是第三方小程序
     */
    suspend fun getActivateInfo(
        isThirdApp: Int = 1,
        onError: ErrorListener = null
    ): ActivateInfoModel? {
        return RxHttp.get(UrlManager.getNewBaseDomain(ConstantApi.TWE_CALL_DEVICE_ACTIVATE_INFO_API))
            .add("IsThirdApp", isThirdApp)
            .toAwaitResponse<ActivateInfoModel>()
            .tryAwait { onError?.invoke(it) }
    }

    /**
     * 上报当前应用版本信息
     */
    suspend fun upLoadAppInfo() :Any?{
        return RxHttp.postJson(UrlManager.getNewBaseDomain(ConstantApi.TWE_CALL_UPLOAD_APP_INFO))
            .add("versionCode", DataStoreUtils.getData(ConstantDataStore.APP_VERSION_CODE,0))
            .add("packageName", DataStoreUtils.getData(ConstantDataStore.APP_PACKAGE_NAME,"com.jxw.yuliao"))
            .toAwaitResponse<Any>()
            .tryAwait()
    }

}