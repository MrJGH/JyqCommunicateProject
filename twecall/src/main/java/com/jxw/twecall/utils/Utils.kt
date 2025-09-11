package com.jxw.twecall.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Resources
import android.graphics.Matrix
import android.util.DisplayMetrics
import android.util.Log
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.content.ContextCompat.getSystemService
import com.jxw.basemodel.utils.LauncherDataUtil
import java.io.File
import java.io.FileWriter
import java.lang.reflect.Method


fun showPopupWindow(anchorView: View, popupViewGroup: View): PopupWindow {
    val popupWindow = PopupWindow(
        popupViewGroup,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    popupWindow.isOutsideTouchable = true
    popupWindow.isFocusable = true

    val displayMetrics: DisplayMetrics = anchorView.context.resources.displayMetrics
    val screenWidth = displayMetrics.widthPixels
    val location = IntArray(2)
    anchorView.getLocationOnScreen(location)
    val xOffset =
        screenWidth - location[0] - anchorView.width - (8 * displayMetrics.density).toInt()

    popupWindow.showAsDropDown(anchorView, xOffset, 0)
    return popupWindow
}

/*
fun TextView.updateOperate(
    isInoperable: Boolean,
    @ColorRes ableTextColorRes: Int = R.color.blue_0052D9,
    @ColorRes enableTextColorRes: Int = R.color.white_0052D9
) {
    if (isInoperable) {
        setTextColor(ContextCompat.getColor(this.context, ableTextColorRes))
        setBackgroundResource(R.drawable.background_blue_cell_btn)
    } else {
        setTextColor(ContextCompat.getColor(this.context, enableTextColorRes))
        setBackgroundResource(R.drawable.background_grey_cell_btn)
    }
}
*/

fun copyTextToClipboard(context: Context, text: String) {
    // 获取剪贴板管理器
    val clipboard: ClipboardManager? = getSystemService(context, ClipboardManager::class.java)
    // 创建一个剪贴板数据对象
    val clip = ClipData.newPlainText("label", text)
    // 将剪贴板数据设置到剪贴板管理器
    clipboard?.setPrimaryClip(clip)
}

fun getFile(path: String): File {
    val file = File(path)
    if (file.exists()) {
        file.delete()
    }
    file.createNewFile()
    return file
}

fun getScreenWidth(resource: Resources): Int {
    return resource.displayMetrics.widthPixels
}

fun getScreenHeight(resource: Resources): Int {
    return resource.displayMetrics.heightPixels
}

fun adjustAspectRatio(
    videoWidth: Int,
    videoHeight: Int,
    textureView: TextureView,
    screenWidth: Int? = null,
    screenHeight: Int? = null
) {
    val viewLayoutParams = textureView.layoutParams
    val viewWidth = screenWidth ?: viewLayoutParams.width
    val viewHeight = screenHeight ?: viewLayoutParams.height
    Log.d(
        "utils",
        "screenWidth:$screenWidth  screenHeight:$screenHeight   videoWidth:$videoWidth  videoHeight:$videoHeight"
    )
    if (videoWidth == 0 || videoHeight == 0) {
        return
    }
    val viewAspectRatio = viewWidth.toFloat() / viewHeight
    val videoAspectRatio = videoWidth.toFloat() / videoHeight

    var scaleX = 1.0f
    var scaleY = 1.0f

    if (videoAspectRatio > viewAspectRatio) {
        // Video is wider than view, scale by height
        scaleX = videoAspectRatio / viewAspectRatio
    } else {
        // Video is taller than view, scale by width
        scaleY = viewAspectRatio / videoAspectRatio
    }

    val pivotPointX = viewWidth / 2f
    val pivotPointY = viewHeight / 2f

    val matrix = Matrix()
    matrix.setScale(scaleX, scaleY, pivotPointX, pivotPointY)

    textureView.setTransform(matrix)
    textureView.invalidate()
}


fun getBitRateIntervalByPixel(width: Int, height: Int): Array<Double> {
    return arrayOf(width * height * 0.5, width * height * 2.0)
}

fun deleteDeviceKeyFile(path: String) {
    val fileName = "device_key"

    // 创建一个File对象，表示device_key文件
    val deviceKeyFile = File(path, fileName)

    // 检查文件是否存在
    if (deviceKeyFile.exists()) {
        // 文件存在，尝试删除
        if (deviceKeyFile.delete()) {
            LogUtil.e(msg = "device_key文件已成功删除。")
        } else {
            LogUtil.e(msg = "删除device_key文件失败。")
        }
    } else {
        LogUtil.e(msg = "device_key文件不存在。")
    }
}


fun turnCameraMirror(cameraMirrorType: Int = LauncherDataUtil.getCameraMirrorType()) {
    when (cameraMirrorType) {
        1 -> turnCameraMirror1()
        else -> turnCameraMirror0()
    }
}

/**
 * 翻转camera 画面镜像view
 */
fun turnCameraMirror0() {
    try {
        val customSystemManagerClass: Class<*> = Class.forName("com.android.server.CustomSystemManager")
        val subCameraSwitchFace = customSystemManagerClass.getDeclaredMethod("subCameraSwitchFace", Boolean::class.java) as? Method
        subCameraSwitchFace?.let {
            it.isAccessible = true
            if (customSystemManagerClass != null) {
                LogUtil.e(msg = "ifCurSubCameraIsBack() = " + ifCurSubCameraIsBack(customSystemManagerClass))
                subCameraSwitchFace.invoke(customSystemManagerClass, !ifCurSubCameraIsBack(customSystemManagerClass))
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun ifCurSubCameraIsBack(customSystemManagerClass: Class<*>): Boolean {
    try {
        val ifCurSubCameraIsBack = customSystemManagerClass?.getDeclaredMethod("ifCurSubCameraIsBack") as? Method
        ifCurSubCameraIsBack?.let {
            it.isAccessible = true
            if (customSystemManagerClass != null) {
                return ifCurSubCameraIsBack.invoke(customSystemManagerClass) as? Boolean ?: false
            }
            return false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
    return false
}

/**
 * 翻转camera 画面镜像view
 */
private fun turnCameraMirror1() {
    try {
        val nowCode = getSystemPropCameraFilp()
        val code = if (nowCode.equals("0")) "4" else "0"
        val fifo = File("/data/vendor_de/camera/camera_device_state")
        val writer = FileWriter(fifo)
        writer.write(code)
        writer.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun getSystemPropCameraFilp(): String? {
    return try {
        val systemProperties = Class.forName("android.os.SystemProperties")
        val get = systemProperties.getMethod("get", String::class.java, String::class.java)
        val sx = get.invoke(null, "persist.camera.camera_state", "0") as String
        LogUtil.d(msg = "persist.camera.camera_state：$sx")
        // sx 4:已翻转，0：正常
        sx
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        "0"
    }
}

