package com.jxw.yuliao.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.jxw.yuliao.R
import com.jxw.basemodel.base.EmptyListener
import com.jxw.yuliao.databinding.LayoutLoadQrcodeBinding
import com.jxw.yuliao.ext.click
import com.jxw.yuliao.ext.gone
import com.jxw.yuliao.ext.visible
import com.jxw.yuliao.utils.ActivityRouteUtil
import com.jxw.yuliao.utils.getDrawableFromResource

/**
 *
 * @author: jyq
 * @date: 2023/12/25
 * @descp: 加载二维码view
 */
open class QrcodeView @JvmOverloads constructor(
    private var mContext: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(mContext, attrs, defStyleAttr) {
    private var onRefresh: EmptyListener = null
    private var mView: View? = null
    private var llLinkNet: LinearLayout? = null
    private var llLoading: LinearLayout? = null
    private var llNoNet: LinearLayout? = null
    private var llRefresh: LinearLayout? = null
    private var ivQrcode: AppCompatImageView? = null

    init {
        attrs?.let {
            val a = mContext.obtainStyledAttributes(attrs, R.styleable.QrcodeView)
            val layoutTypeSize = a.getInteger(R.styleable.QrcodeView_layout_type, 1)
            mView = if (layoutTypeSize == 2) {
                LayoutInflater.from(mContext).inflate(R.layout.layout_load_qrcode2, this, true)
            } else {
                LayoutInflater.from(mContext).inflate(R.layout.layout_load_qrcode, this, true)
            }
            initView(mView)
            a.recycle()
        }
        initListener()
    }

    private fun initView(mView: View?) {
        llLinkNet = mView?.findViewById(R.id.ll_link_net)
        llLoading = mView?.findViewById(R.id.ll_loading)
        llNoNet = mView?.findViewById(R.id.ll_no_net)
        llRefresh = mView?.findViewById(R.id.ll_refresh)
        ivQrcode = mView?.findViewById(R.id.iv_qrcode)
    }

    private fun initListener() {
        llLinkNet?.click {
            llLoading?.visible()
            llNoNet?.gone()
            llRefresh?.gone()
            ActivityRouteUtil.toWlanActivity(mContext)
        }
        llRefresh?.click {
            llLoading?.visible()
            llNoNet?.gone()
            llRefresh?.gone()
            onRefresh?.invoke()
        }
    }

    /**
     * 设置图片
     */
    fun setImageView(url: String) {
        showLoading()
        ivQrcode?.visible()
        ivQrcode?.let {
            Glide.with(mContext).load(url)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d(TAG, "网络访问失败，请检查是否开始网络或者增加http的访问许可");
                        showLoadFail { setImageView(url) }
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d(TAG, "网络访问成功，可以显示图片${Thread.currentThread() == Looper.getMainLooper().thread}");
                        mView?.background = getDrawableFromResource(R.drawable.shape_white_corner_20)
                        ivQrcode?.visible()
                        llLoading?.gone()
                        llNoNet?.gone()
                        llRefresh?.gone()
                        it.setImageDrawable(resource)
                        return false
                    }
                }).into(it)
        }
    }


    /**
     * 显示loading
     */
    fun showLoading() {
        visible()
        mView?.background = getDrawableFromResource(R.drawable.shape_black_trans_corner_20)
        ivQrcode?.gone()
        llLoading?.visible()
        llNoNet?.gone()
        llRefresh?.gone()
    }

    /**
     * 没有网络接口回调
     */
    fun showNoNetWork() {
        visible()
        mView?.background = getDrawableFromResource(R.drawable.shape_black_trans_corner_20)
        ivQrcode?.gone()
        llNoNet?.visible()
        llRefresh?.gone()
        llLoading?.gone()

    }


    /**
     * 加载失败
     * @param onRefresh 刷新数据
     */
    fun showLoadFail(onRefresh: EmptyListener = null) {
        Log.e(TAG, "showLoadFail  加载失败")
        this.onRefresh = onRefresh
        visible()
        mView?.background = getDrawableFromResource(R.drawable.shape_black_trans_corner_20)
        ivQrcode?.gone()
        llLoading?.gone()
        llNoNet?.gone()
        llRefresh?.visible()

    }

    companion object {
        private const val TAG = "QrcodeView"
    }
}