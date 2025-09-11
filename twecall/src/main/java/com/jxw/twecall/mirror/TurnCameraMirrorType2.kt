package com.jxw.twecall.mirror

import com.jxw.twecall.utils.LogUtil

/**
 *
 * @author: jyq
 * @desc: 镜像翻转配置（悦蓝）
 * @date: 2025/4/9
 */
internal class TurnCameraMirrorType2 : ITurnCameraMirror {
    override fun turnCameraMirror() {
        try {
            val nowCode = getCurrentCameraMirrorParam()
            val code = if (nowCode == "0") "4" else "0"
            val cls = Class.forName("android.os.CameraFlipManager")
            val method = cls.getDeclaredMethod("flipCamera", String::class.java)
            method.invoke(null, code)
            LogUtil.e(TAG, "turnCameraMirror 摄像头翻转成功，模式: $code")
        } catch (e: Exception) {
            e.printStackTrace()
            LogUtil.e(TAG, "turnCameraMirror 反射调用失败" + e.message)
        }
    }

    /**
     * 反射获取摄像头状态
     * @return   翻转模式：
     *             0：不翻转
     *             1：左右 + 上下翻转
     *             2：上下翻转
     *             4：左右翻转
     */
    override fun getCurrentCameraMirrorParam(): String {
        return try {
            val cls = Class.forName("android.os.CameraFlipManager")
            val method = cls.getDeclaredMethod("getCameraState")
            method.invoke(null) as? String ?: "0"
        } catch (e: Exception) {
            e.printStackTrace()
            LogUtil.e(TAG, "getCurrentCameraMirrorParam 反射调用失败" + e.message)
            "0"
        }
    }

    companion object {
        private const val TAG = "TurnCameraMirrorType2"
    }
}