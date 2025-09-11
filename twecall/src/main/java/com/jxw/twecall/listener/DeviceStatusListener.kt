package com.jxw.twecall.listener

import com.jxw.twecall.utils.LogUtil
import com.jxw.twecall.utils.toDateString
import com.tencent.iot.video.device.callback.IvDeviceCallback

/**
 *
 * @author: jyq
 * @desc: 设备状态回调
 * @date: 2024/10/31
 */
interface DeviceStatusListener : IvDeviceCallback {
    /**
     * 设备上线回调，通知用户设备已经上线，并会带入网络时间参数，该回调中不要做耗时太长的操作。
     * 网络异常时该时间参数不保证可靠，用户如需对设备进行校时，建议使用 iv_sys_get_time 接口
     * 当sdk不支持mqtt通信时(根据配置FEATURE_DM_CONFIG_USE_MQTT)，该接口无效
     * 详情见 README
     * @param netDateTime 网络时间
     */
    override fun onOnline(netDateTime: Long) {
        LogUtil.d(msg = "onOnline  设备上线了,上线时间${netDateTime.toDateString()}")
    }

    /**
     * 设备失去与 IoT Video 服务器的连接后，通过此回调通知用户，该回调中不要做耗时太长的操作
     * 当sdk不支持mqtt通信时(根据配置FEATURE_DM_CONFIG_USE_MQTT)，该接口无效
     * 详情见 README
     * @param status
     */
    override fun onOffline(status: Int) {
        LogUtil.d(msg = "onOffline  设备下线了,当前状态$status")
    }

    /**
     * 功能模块在后台服务开启状态的回调通知
     * 详情见 README
     * @param moduleStatus  0 未开启，1 开启
     */
    override fun onModuleStatus(moduleStatus: Int) {
        if (moduleStatus == 1) {
            LogUtil.d(msg = "onModuleStatus  功能模块在后台服务开启状态的回调通知,当前状态开启")
        } else {
            LogUtil.d(msg = "onModuleStatus  功能模块在后台服务开启状态的回调通知,当前状态未开启")
        }
    }

}