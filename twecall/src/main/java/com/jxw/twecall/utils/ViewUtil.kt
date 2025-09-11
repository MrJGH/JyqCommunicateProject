package com.jxw.twecall.utils

import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.jxw.basemodel.utils.LauncherDataUtil
import com.jxw.twecall.utils.ext.gone
import com.jxw.twecall.utils.ext.visible

/**
 *
 * @author: jyq
 * @desc: view相关方法
 * @date: 2025/1/14
 */
object ViewUtil {
    /**
     * 控制 摄像头翻转按钮view 显示和隐藏
     */
    fun isShowTurnCameraView(mTurnCamera: AppCompatImageView, mTurnCameraTv: AppCompatTextView) {
        if (LauncherDataUtil.getCameraNumber() > 1) {
            mTurnCamera.visible()
            mTurnCameraTv.visible()
        } else {
            mTurnCamera.gone()
            mTurnCameraTv.gone()
        }
    }

    /**
     * 控制 麦克风view 显示和隐藏
     */
    fun isShowMicrophoneView(
        mMicrophone: AppCompatImageView,
        mMicrophoneTv: AppCompatTextView,
        condition: Boolean = LauncherDataUtil.isNeedMicrophone()
    ) {
        if (condition) {
            mMicrophone.visible()
            mMicrophoneTv.visible()
        } else {
            mMicrophone.gone()
            mMicrophoneTv.gone()
        }
    }

    /**
     * 控制 扬声器view 显示和隐藏
     */
    fun isShowLoudSpeakView(
        mLoudspeaker: AppCompatImageView,
        mLoudspeakerTv: AppCompatTextView,
        condition: Boolean = LauncherDataUtil.isNeedLoudspeaker()
    ) {
        if (condition) {
            mLoudspeaker.visible()
            mLoudspeakerTv.visible()
        } else {
            mLoudspeaker.gone()
            mLoudspeakerTv.gone()
        }
    }

    /**
     * 控制 静音view 显示和隐藏
     */
    fun isShowSoundOffView(
        mLoudspeaker: AppCompatImageView,
        mLoudspeakerTv: AppCompatTextView,
        condition: Boolean = LauncherDataUtil.isNeedSoundOff()
    ) {
        if (condition) {
            mLoudspeaker.visible()
            mLoudspeakerTv.visible()
        } else {
            mLoudspeaker.gone()
            mLoudspeakerTv.gone()
        }
    }


}