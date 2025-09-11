package com.tencent.qcloud.tuikit.tuichat.classicui.widget.input.inputmore

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.tencent.qcloud.tuikit.tuichat.BuildConfig
import com.tencent.qcloud.tuikit.tuichat.R
import com.tencent.qcloud.tuikit.tuichat.bean.InputMoreActionUnit

/**

 * @Author jyq
 * @Date 2024/12/27
 *  指示器
 */
class IndicatorView @JvmOverloads constructor(
    private val mContext: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(mContext, attrs, defStyleAttr) {

    private val viewList = mutableListOf<ImageView>()
    private val mColumnCount = BuildConfig.INPUT_MORE_COLUMN_COUNT

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_HORIZONTAL
    }

    fun setDataList(dataList: MutableList<InputMoreActionUnit>) {
        viewList.clear()
        removeAllViews()
        for (i in 1..(dataList.size + mColumnCount - 1) / mColumnCount) {
            val indicatorView =
                LayoutInflater.from(mContext).inflate(R.layout.view_vp_indicator, null)
            indicatorView.isEnabled = false
            addView(indicatorView)
            viewList.add(indicatorView.findViewById(R.id.ivPoi))
        }
    }

    fun changeIndicator(index: Int) {
        viewList.forEachIndexed { poi, view ->
            if (index == poi) {
                view.background = ContextCompat.getDrawable(mContext, R.drawable.ic_poi_sel)
            } else {
                view.background = ContextCompat.getDrawable(mContext, R.drawable.ic_poi_nor)
            }
        }
    }

}