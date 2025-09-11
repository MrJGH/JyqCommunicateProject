package com.jxw.twecall.utils.ext

import android.util.TypedValue
import com.jxw.twecall.TweCallManager


fun dpToPx(dpValue: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dpValue,
        TweCallManager.mContext?.get()?.resources?.displayMetrics
    )
}

fun dpToPx(dpValue: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dpValue.toFloat(),
        TweCallManager.mContext?.get()?.resources?.displayMetrics
    ).toInt()
}

/**
 * 所有字体均使用dp
 */
fun spToPx(spValue: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        spValue,
        TweCallManager.mContext?.get()?.resources?.displayMetrics
    )
}