@file:JvmName("DisplayUtil")
@file:JvmMultifileClass

package com.jxw.yuliao.utils

import android.util.TypedValue
import com.jxw.yuliao.App


fun dpToPx(dpValue: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dpValue,
        App.getInstance().resources.displayMetrics
    )
}

fun dpToPx(dpValue: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dpValue.toFloat(),
        App.getInstance().resources.displayMetrics
    ).toInt()
}

/**
 * 所有字体均使用dp
 */
fun spToPx(spValue: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        spValue,
        App.getInstance().resources.displayMetrics
    )
}