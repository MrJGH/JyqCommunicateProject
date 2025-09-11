package com.jxw.basemodel.constant

/**
 *
 * @author: jyq
 * @date: 2023/12/25
 * @descp: 接口api
 */
object ConstantApi {
    const val WECHAT_QRCODE_API="/api/jxwdevice/wxmp/qrcode"   //获取二维码

    const val CONTACT_PERSON_LIST_API="/api/jxwdevice/userlist"  //获取联系人列表（通过查到联系人数量判断是否绑定过设备）

    const val USER_SIG_CREATE_API="/chat/api/account/user-sig/create"  //秘钥查询接口

    const val SAVE_USER_INFO_API="/api/user/save"  //保存用户信息

    const val GET_DEVICE_BIND_USER_INFO_API="/chat/api/user-device/device-info"  //获取设备（孩子信息）

    const val TWE_CALL_VIDEO_DEVICE_BACK_API="/chat/api/video/device/back"  //设备回传 (通知小程序 设备端已经准备就绪，小程序可以发起音视频通话了)

    const val TWE_CALL_DEVICE_SN_TICKET="/chat/h5/video/findDeviceTicket"  //获取设备snticket 相关信息

    const val TWE_CALL_DEVICE_ACTIVATE_INFO_API="/chat/api/video/activateInfo"  //获取设备 激活详情

    const val TWE_CALL_INSERT_CALL_RESULT="/chat/api/video/insertCallResult"  //插入通话结果

    const val TWE_CALL_ACTIVATE_API="/chat/api/video/activate"  //激活twecall

    const val TWE_CALL_GET_OPEN_ID="/chat/api/video/friendInfo"  //通过 im的 用户id 获取当前用户的openid

    const val TWE_CALL_UPLOAD_APP_INFO="/chat/api/chat-application-info/save"  //上报当前app 版本信息

}