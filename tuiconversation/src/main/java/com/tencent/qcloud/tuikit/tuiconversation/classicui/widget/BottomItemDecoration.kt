package com.tencent.qcloud.tuikit.tuiconversation.classicui.widget

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

/**
 *
 * @author: jyq
 * @date: 2024/1/4
 * @descp: item 底部偏移量
 */
class BottomItemDecoration(private var bottomItemTopOffset: Float) : ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.set(0,0 , 0, bottomItemTopOffset.toInt())
    }
}