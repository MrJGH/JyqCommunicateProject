package com.jxw.yuliao.activity

import android.view.WindowManager
import com.jxw.basemodel.base.BaseActivity
import com.jxw.basemodel.bean.RegisterInfoModel
import com.jxw.basemodel.constant.ConstantDataStore
import com.jxw.basemodel.network.ApiHelper
import com.jxw.basemodel.utils.DataStoreUtils
import com.jxw.validate.utils.LauncherDataUtils
import com.jxw.yuliao.App
import com.jxw.yuliao.BuildConfig
import com.jxw.yuliao.MainActivity
import com.jxw.yuliao.R
import com.jxw.yuliao.databinding.ActivitySplashBinding
import com.jxw.yuliao.ext.gone
import com.jxw.yuliao.ext.startActivity
import com.jxw.yuliao.ext.visible
import com.jxw.yuliao.utils.DemoLog
import com.jxw.yuliao.utils.TUIUtils
import com.jxw.yuliao.widget.NoNetWorkView
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.qcloud.tuicore.interfaces.TUICallback
import com.tencent.qcloud.tuicore.util.ToastUtil

/**
 *
 * @author: jyq
 * @date: 2024/1/4
 * @descp: 启动页
 */
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    private lateinit var mNoNetWorkView: NoNetWorkView

    override fun layoutId(): Int = R.layout.activity_splash

    override fun initView() {
        LauncherDataUtils.checkFuncAuth(this, BuildConfig.AUTH_APPID)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        mNoNetWorkView = findViewById(R.id.splash_no_network_view)
    }

    override fun initListener() {
        requestPermission()
    }

    override fun initData() {
    }

    override fun onResume() {
        super.onResume()
        mBinding.splashBgIv.visible()
        ApiHelper.getUseInfo(onError = { i, s ->
            mBinding.splashBgIv.gone()
            mNoNetWorkView.visible()
            mNoNetWorkView.showNetWork { onResume() }
        }).observe(this) {
            DataStoreUtils.putData(ConstantDataStore.IM_ACCOUNT_INFO, it.userId)
            login(it)
        }
    }

    private fun login(registerInfoModel: RegisterInfoModel) {
        TUILogin.login(
            App.getInstance(),
            registerInfoModel.appId,
            registerInfoModel.userId,
            registerInfoModel.userSig,
            TUIUtils.getLoginConfig(),
            object : TUICallback() {
                override fun onError(code: Int, desc: String) {
                    runOnUiThread {
                        mBinding.splashBgIv.gone()
                        mNoNetWorkView.visible()
                        mNoNetWorkView.showNetWork { onResume() }
                        ToastUtil.toastLongMessage(getString(R.string.failed_login_tip) + ", errCode = " + code + ", errInfo = " + desc)
                    }
                    DemoLog.i(TAG, "imLogin errorCode = $code, errorInfo = $desc")
                }

                override fun onSuccess() {
                    mNoNetWorkView.gone()
                    val isFirstSaveUserInfo =
                        DataStoreUtils.getData(ConstantDataStore.FIRST_SAVE_USER_INFO, false)
                    if (isFirstSaveUserInfo) {
                        startActivity<MainActivity>()
                    } else {
                        startActivity<SelectHeaderActivity>()
                    }
                    finish()

                }
            })
    }

    companion object {
        private const val TAG = "SplashActivity"
    }
}