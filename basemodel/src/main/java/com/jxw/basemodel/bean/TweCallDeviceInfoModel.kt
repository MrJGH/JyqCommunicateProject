package com.jxw.basemodel.bean

/**
 *
 * @author: jyq
 * @desc: twecall  设备信息相关信息
 * @date: 2024/12/14
 */
data class TweCallDeviceInfoModel(
    val modelId: String,
    val sn: String,
    val snticket: String,
)

/**
 * twecall  设备激活相关信息
 */
data class ActivateInfoModel(
    val activeStatus: Int = 0, //设备状态 0: 未激活, 1: 激活,
    val deviceSecret: String,//设备秘钥
    val deviceSn: String,     //twecall设备sn   一般格式为  FHO6N9WCFW_尾标_主程序sn  eg:FHO6N9WCFW_hscxxsbos_866153071757389
    val expireStatus: Int = 0, //0: 已过期, 1: 未过期
    val expireTime: Long, //过期时间 时间戳
    val productId: String, //物联网平台 productId  eg:FHO6N9WCFW
    val modelId: String, //modelId  eg:WQPn-cLtDcsxm8nFu9TrFg
    val wxAppId: String, //wxAppId  eg:wxcea7223fc0f9000f

) {

    /**
     * 获取 twecall 是否激活
     */
    fun getActivateStatus(): Boolean = activeStatus == 0

    /**
     * 获取 当前Twecall 是否过期
     * true 没有过期 false 已经过期
     */
    fun getExpireStatus(): Boolean = expireStatus == 1

    /**
     * 获取 twecall 的deviceName  一般格式为  尾标_主程序sn  eg:hscxxsbos_866153071757389
     */
    fun getTweCallDeviceName(): String = deviceSn.removePrefix(productId + "_")

}

