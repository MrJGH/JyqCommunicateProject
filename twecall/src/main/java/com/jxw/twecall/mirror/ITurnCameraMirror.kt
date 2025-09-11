package com.jxw.twecall.mirror

/**
 *
 * @author: jyq
 * @desc: 镜像翻转
 * @date: 2025/4/9
 */
internal interface ITurnCameraMirror {
    /**
     * 翻转 摄像头画面
     */
    fun turnCameraMirror()

    /**
     * 获取当前设备 摄像头画面镜像的参数
     */
    fun getCurrentCameraMirrorParam():Any
}