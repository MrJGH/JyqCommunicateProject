package com.jxw.twecall.model

/**
 *
 * @author: jyq
 * @desc:  小程序呼叫设备时 设备接收到小程序的指令
 * @date: 2024/11/6
 */
enum class CommandWxCallEnum(val command: String) {
    COMMAND_WX_CALL_START("wx_call_start"),//小程序发起请求
    COMMAND_WX_CALL_CANCEL("wx_call_cancel"),//小程序取消呼叫
    COMMAND_WX_CALL_HANGUP("wx_call_hangup"),//小程序挂断
    COMMAND_WX_CALL_TIMEOUT("wx_call_timeout"),//小程序呼叫超时
    COMMAND_WX_CALL_AUDIO_START("wx_call_audio_start"),//小程序发起音频请求
    COMMAND_WX_CALL_VIDEO_START("wx_call_video_start"),//小程序发起视频请求

    COMMAND_WX_CALL_DEVICE_STATUS("get_device_st"),//当前设备状态
//    COMMAND_WX_CALL_CAMERA_OPEN("wx_call_camera_open"),//小程序开启摄像头
//    COMMAND_WX_CALL_CAMERA_CLOSE("wx_call_camera_close")//小程序关闭摄像头
}