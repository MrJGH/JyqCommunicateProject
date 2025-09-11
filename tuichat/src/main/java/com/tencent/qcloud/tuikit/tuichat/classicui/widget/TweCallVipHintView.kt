package com.tencent.qcloud.tuikit.tuichat.classicui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import com.jxw.twecall.utils.ext.gone
import com.jxw.twecall.utils.ext.singClick
import com.tencent.qcloud.tuikit.tuichat.R

/**
 *
 * @author: jyq
 * @desc: twecall 激活loading
 * @date: 2024/12/17
 */
class TweCallVipHintView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    init {
        val view = LayoutInflater.from(context).inflate(R.layout.twecall_vip_hint_view, this)
        initView(view)
    }

    private fun initView(view: View) {
        view.singClick {  }
        view.findViewById<AppCompatTextView>(R.id.tv_ok).singClick { gone() }
    }
}