package com.jxw.yuliao.activity

import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.jxw.basemodel.base.BaseActivity
import com.jxw.basemodel.constant.ConstantDataStore
import com.jxw.basemodel.network.DataRepository
import com.jxw.basemodel.network.ApiHelper
import com.jxw.basemodel.network.safeApiCall
import com.jxw.basemodel.utils.DataStoreUtils
import com.jxw.basemodel.utils.LauncherDataUtil
import com.jxw.twecall.utils.toDateString
import com.jxw.yuliao.R
import com.jxw.yuliao.databinding.ActivityPersonalBinding
import com.jxw.yuliao.ext.gone
import com.jxw.yuliao.ext.singClick
import com.jxw.yuliao.ext.visible
import com.jxw.yuliao.utils.getStringFromResource
import com.jxw.yuliao.widget.QrcodeView
import kotlinx.coroutines.launch

/**
 *
 * @author: jyq
 * @date: 2023/12/21
 * @descp: 个人信息界面
 */
class PersonalActivity : BaseActivity<ActivityPersonalBinding>() {
    private val mEditNameDialogFragment by lazy { EditNameDialogFragment() }
    private val mSelectHeaderFragment by lazy { SelectHeaderFragment(this) }
    private lateinit var mQrcodeIv: QrcodeView
    private var userHeaderUrl = "https://cospub.zhinengtongbu.com/jxwnewdata/image/d65ac4bd76d34e7eb333504f30e14c5d.png"

    override fun layoutId(): Int = R.layout.activity_personal

    override fun initView() {
        mQrcodeIv = findViewById(R.id.qrcode_iv)
        val isAlready =
            DataStoreUtils.getData(ConstantDataStore.WECHAT_QRCODE_BIND_CHILD_INFORMATION, false)
        if (isAlready) {
            mBinding.wechatBindHintTv.visible()
            mQrcodeIv.visible()
        } else {
            mBinding.wechatBindHintTv.gone()
            mQrcodeIv.gone()
        }
        if (LauncherDataUtil.isNeedSendAudioAndVideo()) {
            mBinding.moreIv.gone()
            mBinding.clVipCenter.visible()
        } else {
            mBinding.moreIv.visible()
            mBinding.clVipCenter.gone()
        }
    }

    override fun initListener() {
        mBinding.headerCl.singClick {
            mSelectHeaderFragment.show(supportFragmentManager, "SelectHeaderFragment")
        }
        if (LauncherDataUtil.isNeedSendAudioAndVideo().not()) {
            mBinding.nameCl.singClick {
                mEditNameDialogFragment.show(supportFragmentManager, "EditNameDialogFragment")
            }
        }

        mSelectHeaderFragment.setSelectHeaderListener {
            userHeaderUrl = it.imageUrl
            mBinding.headerIv.setImageResource(it.drawableId)
            saveUserInfo(userHeaderUrl, mBinding.userNameTv.text.toString())
        }
        mEditNameDialogFragment.setSureListener {
            mBinding.userNameTv.text = it
            saveUserInfo(userHeaderUrl, mBinding.userNameTv.text.toString())
        }
    }

    override fun initData() {}

    override fun onResume() {
        super.onResume()
        getDeviceBindUserInfo()
        val isAlready =
            DataStoreUtils.getData(ConstantDataStore.WECHAT_QRCODE_BIND_CHILD_INFORMATION, false)
        if (isAlready) {
            lifecycleScope.launch {
                val data = safeApiCall(
                    callback = { onError { _, _ -> mQrcodeIv.showNoNetWork() } },
                    responseFunction = { DataRepository.getQrCode() })
                data?.let {
                    mQrcodeIv.setImageView(it.qrcode)
                }
            }
        }

        if (LauncherDataUtil.isNeedSendAudioAndVideo()) {
            ApiHelper.getActivateInfo().observe(this){
                if (it.getExpireStatus()){
                    if (it.expireTime.minus(System.currentTimeMillis()) < 7 * 24 * 60 * 60 * 1000) {
                        mBinding.tvVipCenterStatusHint.text = getStringFromResource(R.string.vip_center_status_hint, it.expireTime.toDateString("yyyy.MM.dd"))
                    }
                }else{
                    mBinding.tvVipCenterStatusHint.text = getStringFromResource(R.string.twecall_already_expired)
                }
            }
        }
    }

    /**
     * 保存用户信息
     */
    private fun saveUserInfo(icon: String, nickname: String) {
        ApiHelper.saveUserInfo(icon,nickname).observe(this){
            DataStoreUtils.putData(ConstantDataStore.USER_NAME, nickname)
            DataStoreUtils.putData(ConstantDataStore.USER_HEADER, icon)
        }
    }

    /**
     * 获取绑定用户信息
     */
    private fun getDeviceBindUserInfo() {
        ApiHelper.getDeviceBindUserInfo().observe(this){
            mBinding.userNameTv.text = it.nickname
            if (it.icon.isNullOrEmpty().not()) {
                userHeaderUrl = it.icon!!
            }
            Glide.with(this@PersonalActivity).load(userHeaderUrl).into(mBinding.headerIv)
        }
    }

    companion object {
        private const val TAG = "PersonalActivity"
    }
}