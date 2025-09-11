package com.jxw.basemodel.utils

import com.jxw.basemodel.bean.AppConfigModel
import com.jxw.basemodel.ext.toBeanOrNull
import com.jxw.validate.utils.LauncherDataUtils

/**
 * @author：jyq
 * @date：2024/8/10
 * @descp：主程序传递过来的配置 工具类
 */
object LauncherDataUtil {
    private val data = LauncherDataUtils.queryAppConfig()?.trim()

    /**
     * 获取主程序数据实体
     */
    private fun getData() = data?.toBeanOrNull<AppConfigModel>()

    /**
     * 判断主程序返回的 是否展示聊天更多功能
     */
    fun isShowMoreFunc() = getData()?.showMoreFunc ?: true

    /**
     *  判断 更多功能中 “图片” 功能是否只能发送图片
     *  @return  true 是只能发送图片 ，false 是图片和视频都能发送
     */
    fun isOnlySendPicture() = getData()?.sendPictureOnly ?: false

    /**
     * 获取 尾标
     */
    fun getApkv() = LauncherDataUtils.apkv

    /**
     * 获取设备sn 号
     */
    fun getSnNumber() = LauncherDataUtils.serialNumber

    /**
     * 是否需要音视频通话
     */
    fun isNeedSendAudioAndVideo() = getData()?.isNeedSendAudioAndVideo ?: false

    /**
     * 是否需要发送图片
     */
    fun isNeedSendPhoto() = getData()?.isNeedSendPhoto ?: false

    /**
     * 是否需要拍照
     */
    fun isNeedCapture() = getData()?.isNeedCapture ?: false

    /**
     * 是否需要摄像
     */
    fun isNeedVideoRecord() = getData()?.isNeedVideoRecord ?: false

    /**
     * 是否只能发送语音（默认语音和文字都可以发送）
     */
    fun isOnlySendVoice() = getData()?.isOnlySendVoice ?: false

    /**
     * 获取 镜像翻转类型
     */
    fun getCameraMirrorType() = getData()?.cameraMirrorType ?: -1

    /**
     * 获取 不同硬件的 视频编码类型
     */
    fun getVideoEncoderType(): Int = getData()?.videoEncoderType ?: -1

    /**
     * 获取 不同硬件的 音频编码类型
     */
    fun getAudioEncoderType(): Int = getData()?.audioEncoderType ?: -1

    /**
     * 获取 摄像头数量（针对于音视频通话的摄像头个数，扫读笔设备要移除扫描的摄像头）
     */
    fun getCameraNumber(): Int = getData()?.cameraNumber ?: android.hardware.Camera.getNumberOfCameras()

    /**
     * 获取 后置摄像头cameraId
     * @return 默认返回0
     */
    fun getCameraBackFrontId(): Int = getData()?.cameraFacingBackId ?: android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK

    /**
     * 获取 前置摄像头cameraId
     * @return 默认返回1
     */
    fun getCameraFacingFrontId(): Int = getData()?.cameraFacingFrontId ?: android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT

    /**
     * 获取 默认开启的摄像头cameraId 默认前置摄像头
     * @return 默认返回1
     */
    fun getDefaultSelectCameraId(): Int = getData()?.defaultSelectCameraId ?: android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT

    /**
     * 是否需要 镜像翻转（注意：这里不是切换摄像头，针对单摄像投设备存在物理性翻转摄像头，会产生画面镜像）
     */
    fun isNeedTurnCameraMirror(): Boolean = getData()?.isNeedTurnCameraMirror ?: false

    /**
     * 是否需要 扬声器 或者静音功能（目前只有口袋机配置生效，扫读笔没有扬声器功能，和静音功能两者最多只能存在一个，或者都不存在）
     */
    fun isNeedLoudspeaker(): Boolean = getData()?.isNeedLoudspeaker ?: false

    /**
     * 是否需要 静音功能（目前只有口袋机配置生效，扫读笔没有扬声器功能,和扬声器两者最多只能存在一个，或者都不存在）
     */
    fun isNeedSoundOff(): Boolean = getData()?.isNeedSoundOff ?: false

    /**
     * 是否需要 麦克风
     */
    fun isNeedMicrophone(): Boolean = getData()?.isNeedMicrophone ?: false

    /**
     * 是否需要 发送表情包功能
     */
    fun isNeedSendEmoji(): Boolean = getData()?.isNeedSendEmoji ?: false

    /**
     * 获取相机预览画面 宽度
     */
    fun getCameraPreviewWidthSize(): Int = getData()?.cameraPreviewWidthSize ?: 640

    /**
     * 获取相机预览画面 高度
     */
    fun getCameraPreviewHeightSize(): Int = getData()?.cameraPreviewHeightSize ?: 480

    /**
     * 获取设备下行接收视频分辨率  1:240x320  2:320x240  3:480x352  4:480x640
     */
    fun getReceivePixel(): Int = getData()?.receivePixel ?: 1


}