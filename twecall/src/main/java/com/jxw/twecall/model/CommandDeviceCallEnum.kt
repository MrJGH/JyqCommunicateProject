package com.jxw.twecall.model

/**
 *
 * @author: jyq
 * @desc: 小程序呼叫设备时 设备发送给小程序的响应指令
 * @date: 2024/11/8
 */
enum class CommandDeviceCallEnum(val command: String) {
    COMMAND_DEVICE_CALL_ANSWER("call_answer"),//设备接听
    COMMAND_DEVICE_CALL_REJECT("call_reject"),//设备拒绝接听
    COMMAND_DEVICE_CALL_BUSY("call_busy"),//设备忙线
    COMMAND_DEVICE_CALL_HANGUP("call_hang_up"),//设备主动挂断（和小程序通话过程中）
    COMMAND_DEVICE_CALL_HANGUP_V2("call_hang_up_v2"),//设备主动挂断（和小程序通话过程中）
    COMMAND_DEVICE_CALL_DEVICE_STATUS("get_device_st"),//当前设备状态

//    COMMAND_DEVICE_CAMERA_OPEN("call_camera_open"),//设备主动 打开摄像头（和小程序通话过程中）
//    COMMAND_DEVICE_CAMERA_CLOSE("call_camera_close"),//设备主动 关闭摄像头（和小程序通话过程中）
}