package com.tencent.qcloud.tuikit.tuichat.classicui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.tencent.qcloud.tuikit.tuichat.R

/**
 *
 * @author: jyq
 * @desc: twecall 激活loading
 * @date: 2024/12/17
 */
class LoadingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    init {
        LayoutInflater.from(context).inflate(R.layout.twecall_loading_progress, this)
    }
}