package com.jxw.yuliao.activity

import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.jxw.basemodel.BuildConfig
import com.jxw.basemodel.base.BaseActivity
import com.jxw.basemodel.bean.SelectHeaderModel
import com.jxw.basemodel.constant.ConstantDataStore
import com.jxw.basemodel.network.DataRepository
import com.jxw.basemodel.network.safeApiCall
import com.jxw.basemodel.utils.DataStoreUtils
import com.jxw.yuliao.MainActivity
import com.jxw.yuliao.R
import com.jxw.yuliao.adapter.SelectHeaderAdapter
import com.jxw.yuliao.databinding.ActivitySelectHeaderBinding
import com.jxw.yuliao.ext.addOnTextChangedListener
import com.jxw.yuliao.ext.enable
import com.jxw.yuliao.ext.singClick
import com.jxw.yuliao.ext.startActivity
import com.jxw.yuliao.ext.visible
import com.jxw.yuliao.widget.TitleBarView
import kotlinx.coroutines.launch

/**
 *
 * @author: jyq
 * @date: 2023/12/22
 * @descp: 首次进入初始化头像和姓名界面
 */
class SelectHeaderActivity : BaseActivity<ActivitySelectHeaderBinding>(),
    BaseQuickAdapter.OnItemClickListener<SelectHeaderModel> {

    private val mAdapter = SelectHeaderAdapter()

    //默认第一个头像
    private var mHeaderModel: SelectHeaderModel = SelectHeaderModel(isSelected = true, drawableId = R.drawable.header_one, id = 0, imageUrl = "https://cospub.zhinengtongbu.com/jxwnewdata/image/d65ac4bd76d34e7eb333504f30e14c5d.png")
    //选中的头像数据
    private var mData = mutableListOf<SelectHeaderModel>().apply {
        add(SelectHeaderModel(isSelected = true, drawableId = R.drawable.header_one, id = 0, imageUrl = "https://cospub.zhinengtongbu.com/jxwnewdata/image/d65ac4bd76d34e7eb333504f30e14c5d.png"))
        add(SelectHeaderModel(drawableId = R.drawable.header_two,id = 1,imageUrl = "https://cospub.zhinengtongbu.com/jxwnewdata/image/e02abbbc5a304b3bbb87465247489906.png"))
        add(SelectHeaderModel(drawableId = R.drawable.header_three, id = 2,imageUrl = "https://cospub.zhinengtongbu.com/jxwnewdata/image/2c4211fc24504db7a599f4b81ef025a9.png"))
        add(SelectHeaderModel(drawableId = R.drawable.header_four, id = 3,imageUrl = "https://cospub.zhinengtongbu.com/jxwnewdata/image/c119ac50ae554d45aa6d3208dd12c5a1.png"))
        add(SelectHeaderModel(drawableId = R.drawable.header_five, id = 4,imageUrl = "https://cospub.zhinengtongbu.com/jxwnewdata/image/5e8032e72b844d6891b79b73cfbcc129.png"))
        add(SelectHeaderModel(drawableId = R.drawable.header_six, id = 5,imageUrl = "https://cospub.zhinengtongbu.com/jxwnewdata/image/27000e7d47f243068515fecf2a76f100.png"))
        add(SelectHeaderModel(drawableId = R.drawable.header_seven, id = 6,imageUrl = "https://cospub.zhinengtongbu.com/jxwnewdata/image/af3bd1e225014a3898a90d6f96bba6b1.png"))
        add(SelectHeaderModel(drawableId = R.drawable.header_eight, id = 7,imageUrl = "https://cospub.zhinengtongbu.com/jxwnewdata/image/721a6b39f1e74140ae8053ac57614551.png"))

    }
    private var mTitleBarView: TitleBarView? = null

    override fun layoutId(): Int = R.layout.activity_select_header
    override fun initView() {
        mTitleBarView = findViewById<TitleBarView?>(R.id.titleBarView).apply {
            getNextTextView()?.visible()
        }

        mBinding.selectHeaderRv.run {
            layoutManager = if (BuildConfig.DEVICE_TYPE == 2) {
                GridLayoutManager(mContext, 4)
            } else {
                GridLayoutManager(mContext, 3)
            }
            adapter = mAdapter
        }

    }

    override fun initListener() {
        mBinding.nextTv.singClick {
            val name = mBinding.nameTv.text.toString().trim()
            saveUserInfo(mHeaderModel.imageUrl, name)
        }
        mTitleBarView?.setNextClickListener {
            val name = mBinding.nameTv.text.toString().trim()
            saveUserInfo(mHeaderModel.imageUrl, name)
        }
        mBinding.nameTv.addOnTextChangedListener {
            onTextChanged { _, start, _, count ->
                if (count > 0 || start > 0) {
                    mBinding.nextTv.enable(true)
                    mTitleBarView?.getNextTextView()?.enable(true)
                } else {
                    mBinding.nextTv.enable(false)
                    mTitleBarView?.getNextTextView()?.enable(false)
                }
            }
        }
        mAdapter.setOnItemClickListener(this)
    }

    override fun initData() {
        mAdapter.submitList(mData)
    }

    /**
     * 保存用户信息
     */
    private fun saveUserInfo(icon: String, nickname: String) {
        lifecycleScope.launch {
            val data = safeApiCall(
                callback = {},
                responseFunction = { DataRepository.saveUserInformation(icon = icon, nickname = nickname) })
            data?.let {
                DataStoreUtils.putData(ConstantDataStore.FIRST_SAVE_USER_INFO, true)
                DataStoreUtils.putData(ConstantDataStore.USER_NAME, nickname)
                DataStoreUtils.putData(ConstantDataStore.USER_HEADER, mHeaderModel.imageUrl)
                startActivity<MainActivity>()
                finish()
            }
        }
    }

    override fun onClick(
        adapter: BaseQuickAdapter<SelectHeaderModel, *>,
        view: View,
        position: Int
    ) {
        mHeaderModel = adapter.items[position]
        mData.forEach {
            it.isSelected = it.id == position
        }
        adapter.notifyDataSetChanged()
    }

    companion object {
        private const val TAG = "SelectHeaderActivity"
    }
}