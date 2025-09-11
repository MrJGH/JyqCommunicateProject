package com.jxw.basemodel.bean

/**
 *
 * @author: jyq
 * @desc: 主程序传递过来的配置model
 * @date: 2024/8/10
 *
 * @param  showMoreFunc 是否展示 聊天更多功能
 * @param  sendPictureOnly   更多功能中 “图片” 功能是否只能发送图片  true 是只能发送图片 ，false 是图片和视频都能发送
 * @param  isNeedSendAudioAndVideo   是否需要音视频通话
 * @param  isNeedSendPhoto   是否需要发送图片
 * @param  isNeedCapture   是否需要拍照
 * @param  isNeedVideoRecord 是否需要摄像
 * @param  isNeedTurnCameraMirror 是否需要 镜像翻转（注意：这里不是切换摄像头，针对单摄像投设备存在物理性翻转摄像头，会产生画面镜像）
 * @param  isNeedLoudspeaker 是否需要 扬声器
 * @param  isNeedMicrophone 是否需要 麦克风
 * @param  isNeedSoundOff 是否需要 静音按钮功能
 * @param  isNeedSendEmoji 是否需要 发送表情包功能
 * @param  isOnlySendVoice 是否只能发送语音（默认语音和文字都可以发送）
 * @param cameraMirrorType  镜像翻转类型（不同硬件厂商会有不同的 镜像方法）
 * @param videoEncoderType  控制twecall视频编码类型
 * @param audioEncoderType  控制twecall音频编码类型
 * @param cameraNumber  获取 摄像头数量（针对于音视频通话的摄像头个数，扫读笔设备要移除扫描的摄像头）
 * @param cameraFacingFrontId  前置摄像头cameraId
 * @param cameraFacingBackId  后置摄像头cameraId
 * @param defaultSelectCameraId  默认开启的摄像头cameraId 默认前置摄像头
 * @param cameraPreviewWidthSize  相机预览画面 宽度
 * @param cameraPreviewHeightSize   相机预览画面 高度
 * @param receivePixel  设备下行接收视频分辨率  1:240x320  2:320x240  3:480x352  4:480x640
 */
data class AppConfigModel(
    var showMoreFunc: Boolean? = true,
    var sendPictureOnly: Boolean? = false,
    var isNeedSendAudioAndVideo: Boolean? = false,
    var isNeedSendPhoto: Boolean? = false,
    var isNeedCapture: Boolean? = false,
    var isNeedVideoRecord: Boolean? = false,
    var isNeedTurnCameraMirror: Boolean? = false,
    var isNeedLoudspeaker: Boolean? = false,
    var isNeedMicrophone: Boolean? = false,
    var isNeedSoundOff: Boolean? = false,
    var isNeedSendEmoji: Boolean? = false,
    var isOnlySendVoice: Boolean? = false,
    var cameraMirrorType: Int = -1,
    var videoEncoderType: Int = -1,
    var audioEncoderType: Int = -1,
    var cameraNumber: Int = android.hardware.Camera.getNumberOfCameras(),
    var cameraFacingFrontId: Int = android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT,
    var cameraFacingBackId: Int = android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK,
    var defaultSelectCameraId: Int = android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT,
    var cameraPreviewWidthSize: Int = 640,
    var cameraPreviewHeightSize: Int = 480,
    var receivePixel: Int = 1,
)
