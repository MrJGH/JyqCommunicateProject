package com.jxw.basemodel.base

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.jxw.basemodel.BuildConfig
import com.jxw.basemodel.ext.hideStatusNavigationBar
import com.jxw.basemodel.utils.NotchUtils
import com.jxw.validate.utils.LauncherDataUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

abstract class BaseActivity<T : ViewDataBinding> : AppCompatActivity(),
    CoroutineScope by MainScope() {
    lateinit var mBinding: T
    lateinit var mContext: AppCompatActivity
    private val permissionCode = 321
    var isStartService = false

    init {
        //兼容
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    // 要申请的权限
    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        mBinding = DataBindingUtil.setContentView<ViewDataBinding>(this, layoutId()) as T
        setStatusNavigationBar()
        if (BuildConfig.DEVICE_TYPE != 0) {
            LauncherDataUtils.validateActivate(this)
        }
        initView()
        initListener()
        initData()
    }


    abstract fun layoutId(): Int
    abstract fun initView()
    abstract fun initListener()
    abstract fun initData()


    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    fun setStatusNavigationBar() {
        NotchUtils.addFlags(this) { hideStatusNavigationBar() }
    }

    fun requestPermission() {
        val isGranted = checkPermissionAllGranted(permissions)
        if (isGranted) {
            initData()
        } else {
            ActivityCompat.requestPermissions(
                this,
                permissions,
                permissionCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionCode) {
            var isAllGranted = true
            // 判断是否所有的权限都已经授予了
            for (grant in grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break
                }
            }
            if (isAllGranted) {
                // 如果所有的权限都授予了, 则执行备份代码
                initData()

            }
        }
    }

    private fun checkPermissionAllGranted(permissions: Array<out String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }
}