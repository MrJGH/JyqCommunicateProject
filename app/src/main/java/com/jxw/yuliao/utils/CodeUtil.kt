@file:JvmName("CodeUtil")
@file:JvmMultifileClass

package com.jxw.yuliao.utils

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.*
import androidx.core.content.ContextCompat
import com.jxw.yuliao.App

fun getStringArrayFromResource(@ArrayRes arrayId: Int): Array<String> {
    return App.getInstance().resources.getStringArray(arrayId)
}

fun getStringFromResource(@StringRes stringId: Int): String {
    return App.getInstance().getString(stringId)
}

fun getStringFromResource(@StringRes stringId: Int, vararg formatArgs: Any?): String? {
    return App.getInstance().getString(stringId, *formatArgs)
}

fun getColorFromResource(@ColorRes colorRes: Int): Int {
    return ContextCompat.getColor(App.getInstance(), colorRes)
}

fun getDimensionFromResource(@DimenRes dimenRes: Int): Int {
    return App.getInstance().resources
        .getDimensionPixelSize(dimenRes)
}

fun getDrawableFromResource(@DrawableRes drawableRes: Int): Drawable? {
    return  ContextCompat.getDrawable(App.getInstance(), drawableRes)
}

/**
 * 通过 drawable 文件夹下的图片名字 获取图片的资源id
 * @param drawableName 图片名字
 */
fun getDrawableIdFromResourceName(drawableName: String?): Int {
    return App.getInstance().resources.getIdentifier(
        drawableName,
        "drawable",
        App.getInstance().packageName
    )
}

/**
 * 通过 layout 文件夹下的布局文件名字 获取布局文件的资源id
 * @param layoutName 布局文件名字
 */
fun getLayoutIdFromResourceName(layoutName: String): Int {
    return App.getInstance().resources.getIdentifier(
        layoutName,
        "layout",
        App.getInstance().packageName
    )
}

fun getColorDrawable(@ColorRes colorRes: Int): ColorDrawable? {
    return ColorDrawable(
        ContextCompat.getColor(
            App.getInstance(),
            colorRes
        )
    )
}

