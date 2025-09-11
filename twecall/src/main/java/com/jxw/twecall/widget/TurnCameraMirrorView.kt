package com.jxw.twecall.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.jxw.twecall.R
import com.jxw.twecall.TweCallManager
import com.jxw.twecall.utils.LogUtil
import com.jxw.twecall.utils.ext.singClick

/**
 *
 * @author: jyq
 * @desc: 翻转camera 画面镜像view
 * @date: 2025/1/10
 */
class TurnCameraMirrorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_turn_camera_mirror_view, this)
        initListener()
    }

    private fun initListener() {
        singClick {
            LogUtil.i(msg = "TurnCameraMirrorView 点击了镜像翻转")
            TweCallManager.switchCameraMirror() }
    }


}