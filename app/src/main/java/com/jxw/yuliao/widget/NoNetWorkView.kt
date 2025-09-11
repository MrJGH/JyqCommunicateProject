package com.jxw.yuliao.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.jxw.yuliao.R
import com.jxw.yuliao.databinding.LayoutNoNetworkBinding
import com.jxw.yuliao.ext.click
import com.jxw.yuliao.ext.visible
import com.jxw.yuliao.utils.ActivityRouteUtil

/**
 *
 * @author: jyq
 * @date: 2023/12/25
 * @descp: 无网络view
 */
class NoNetWorkView @JvmOverloads constructor(
    private var mContext: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(mContext, attrs, defStyleAttr) {
    private var mBinding: LayoutNoNetworkBinding? = null

    init {
        val mView =
            LayoutInflater.from(mContext).inflate(R.layout.layout_no_network, this, true)
        mView.tag = "layout/layout_no_network_0"
        mBinding = DataBindingUtil.bind(mView)
    }

    /**
     * 没有网络接口回调
     * @param onRefresh 刷新数据
     */
    fun showNetWork(onRefresh:(()->Unit)?=null){
        visible()
        isClickable=false
        isFocusable=false
        mBinding?.reloadTv?.click {  onRefresh?.invoke() }
        mBinding?.checkNetworkTv?.click { ActivityRouteUtil.toWlanActivity(mContext)  }

    }
}