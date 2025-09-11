package com.jxw.yuliao.activity

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.gyf.immersionbar.BarHide
import com.jxw.basemodel.BuildConfig
import com.jxw.yuliao.R
import com.jxw.yuliao.adapter.SelectHeaderAdapter
import com.jxw.basemodel.base.SelectHeaderListener
import com.jxw.basemodel.bean.SelectHeaderModel
import com.jxw.basemodel.constant.ConstantDataStore
import com.jxw.basemodel.utils.DataStoreUtils
import com.jxw.yuliao.databinding.FragmentSelectHeaderBinding
import com.jxw.yuliao.ext.click
import com.jxw.yuliao.ext.singClick
import com.jxw.yuliao.utils.getDrawableFromResource
import com.jxw.zxkc.ext.destroyImmersionBar
import com.jxw.zxkc.ext.immersionBar
import com.jxw.zxkc.ext.showStatusBar

/**
 *
 * @author: jyq
 * @date: 2023/11/30
 * @descp: 选择年级，学段fragment
 */
class SelectHeaderFragment(var mContext: Context?) : DialogFragment(),
    BaseQuickAdapter.OnItemClickListener<SelectHeaderModel> {
    private lateinit var mBinding: FragmentSelectHeaderBinding
    private val mAdapter = SelectHeaderAdapter()
    private var mHeaderModel: SelectHeaderModel?=null  //选中的头像数据
    private var mListener: SelectHeaderListener = null
    private var userHeaderUrl = "https://cospub.zhinengtongbu.com/jxwnewdata/image/d65ac4bd76d34e7eb333504f30e14c5d.png"

    private var mDataList = mutableListOf<SelectHeaderModel>().apply {
        add(SelectHeaderModel(drawableId = R.drawable.header_one, id = 0, imageUrl = "https://cospub.zhinengtongbu.com/jxwnewdata/image/d65ac4bd76d34e7eb333504f30e14c5d.png"))
        add(SelectHeaderModel(drawableId = R.drawable.header_two,id = 1,imageUrl = "https://cospub.zhinengtongbu.com/jxwnewdata/image/e02abbbc5a304b3bbb87465247489906.png"))
        add(SelectHeaderModel(drawableId = R.drawable.header_three, id = 2,imageUrl = "https://cospub.zhinengtongbu.com/jxwnewdata/image/2c4211fc24504db7a599f4b81ef025a9.png"))
        add(SelectHeaderModel(drawableId = R.drawable.header_four, id = 3,imageUrl = "https://cospub.zhinengtongbu.com/jxwnewdata/image/c119ac50ae554d45aa6d3208dd12c5a1.png"))
        add(SelectHeaderModel(drawableId = R.drawable.header_five, id = 4,imageUrl = "https://cospub.zhinengtongbu.com/jxwnewdata/image/5e8032e72b844d6891b79b73cfbcc129.png"))
        add(SelectHeaderModel(drawableId = R.drawable.header_six, id = 5,imageUrl = "https://cospub.zhinengtongbu.com/jxwnewdata/image/27000e7d47f243068515fecf2a76f100.png"))
        add(SelectHeaderModel(drawableId = R.drawable.header_seven, id = 6,imageUrl = "https://cospub.zhinengtongbu.com/jxwnewdata/image/af3bd1e225014a3898a90d6f96bba6b1.png"))
        add(SelectHeaderModel(drawableId = R.drawable.header_eight, id = 7,imageUrl = "https://cospub.zhinengtongbu.com/jxwnewdata/image/721a6b39f1e74140ae8053ac57614551.png"))

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_select_header,
            container,
            false
        )

        dialog?.window?.run {
            setBackgroundDrawable(getDrawableFromResource(R.color.color_80000000))
            decorView.setBackgroundColor(Color.TRANSPARENT)
            setDimAmount(0f)
            val params: WindowManager.LayoutParams = attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.MATCH_PARENT
            attributes = params

        }
        mBinding?.lifecycleOwner = this
        return mBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListener()
        initData()
    }

    override fun onResume() {
        super.onResume()
//        initSystemBar()
    }

    private fun initView() {
        mBinding.selectHeaderRv.run {
            layoutManager = if (BuildConfig.DEVICE_TYPE == 2) {
                GridLayoutManager(mContext, 4)
            } else {
                GridLayoutManager(mContext, 3)
            }
            adapter = mAdapter
        }
    }

    private fun initListener() {
        mBinding.emptyView.singClick { dismiss() }
        mBinding.sureTv.singClick {
            mHeaderModel?.let { it1 -> mListener?.invoke(it1) }
            dismiss()
        }
        mAdapter.setOnItemClickListener(this)
    }

    private fun initData() {
        val userHeaderUrl = DataStoreUtils.getData(ConstantDataStore.USER_HEADER, userHeaderUrl)
        var model = mDataList.first { selectHeaderModel ->
            selectHeaderModel.imageUrl == userHeaderUrl
        }
        mDataList.forEach {
            it.isSelected = it.id == model.id
        }
        mAdapter.notifyDataSetChanged()

        mAdapter.submitList(mDataList)
    }

    fun setSelectHeaderListener(listener: SelectHeaderListener) {
        this.mListener = listener
    }

    fun getData(): MutableList<SelectHeaderModel> = mDataList

    override fun onClick(
        adapter: BaseQuickAdapter<SelectHeaderModel, *>,
        view: View,
        position: Int
    ) {
        mDataList.forEach {
            it.isSelected = it.id == position
        }
        mHeaderModel = adapter.items[position]
        adapter.notifyDataSetChanged()
    }


    /**
     * 状态栏导航栏初始化
     */
    private fun initSystemBar() {
        immersionBar {
            statusBarView(R.id.top_view)
            showStatusBar()

            transparentStatusBar()
            transparentNavigationBar()
            statusBarDarkFont(true)
            hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR)
        }
    }

    override fun dismiss() {
        super.dismiss()
        dialog?.let {
            destroyImmersionBar(it)
        }

    }
}