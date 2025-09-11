package com.jxw.twecall.mirror

import com.jxw.twecall.utils.LogUtil
import java.lang.reflect.Method

/**
 *
 * @author: jyq
 * @desc: 镜像翻转配置
 * @date: 2025/4/9
 */
internal class TurnCameraMirrorType1 : ITurnCameraMirror {
    private var customSystemManagerClass: Class<*>? = null
    override fun turnCameraMirror() {
        try {
            customSystemManagerClass = Class.forName("com.android.server.CustomSystemManager")
            val subCameraSwitchFace = customSystemManagerClass?.getDeclaredMethod("subCameraSwitchFace", Boolean::class.java) as? Method
            subCameraSwitchFace?.let {
                it.isAccessible = true
                if (customSystemManagerClass != null) {
                    subCameraSwitchFace.invoke(customSystemManagerClass, !getCurrentCameraMirrorParam())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getCurrentCameraMirrorParam(): Boolean {
        try {
            val ifCurSubCameraIsBack = customSystemManagerClass?.getDeclaredMethod("ifCurSubCameraIsBack") as? Method
            ifCurSubCameraIsBack?.let {
                it.isAccessible = true
                if (customSystemManagerClass != null) {
                    val cameraIdStatus = ifCurSubCameraIsBack.invoke(customSystemManagerClass) as? Boolean ?: false
                    LogUtil.d(msg = "getCurrentCameraMirrorParam  cameraIdStatus=$cameraIdStatus")
                    return cameraIdStatus
                }
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return false
    }
}