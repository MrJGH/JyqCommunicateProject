package com.jxw.twecall.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 *
 * @author: jyq
 * @desc: 检测视频流 （用于监听小程序是否开启和关闭摄像头）
 * @date: 2024/11/30
 */
internal class CheckVideoStreamManager {

    // 协程作用域，用于管理协程的生命周期
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var mVideoStreamCurrentTime = -1L  //推送过来的视频流的当前时间
    private var mCameraStatusChangeTime = 2000L  //相机开关检测的时间 间隔  超过当前时间就是关闭了摄像头
    private var mCheckVideoStreamJob: Job? = null

    /**
     * 更新当前视频流时间
     */
    fun updateVideoStreamCurrentTime() {
        mVideoStreamCurrentTime = System.currentTimeMillis()
    }

    /**
     * 开启视频流检测机制
     */
    fun openCheckVideoStreamJob() {
        TweCallStateManager.isWxCameraState.postValue(true)
        mCheckVideoStreamJob = coroutineScope.launch {
            while (isActive) {
                delay(2000)
                if (System.currentTimeMillis() - mVideoStreamCurrentTime > mCameraStatusChangeTime) {
                    TweCallStateManager.isWxCameraState.postValue(false)
                } else {
                    TweCallStateManager.isWxCameraState.postValue(true)
                }
            }
        }
    }

    /**
     * 关闭视频流检测机制
     */
    fun closeCheckVideoStreamJob() {
        mVideoStreamCurrentTime = -1
        mCheckVideoStreamJob?.cancel()
    }


}