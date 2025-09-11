package com.jxw.twecall.base

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jxw.twecall.utils.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

internal abstract class BaseActivity : AppCompatActivity(), CoroutineScope by MainScope(){
    lateinit var mContext: AppCompatActivity
    private val permissionCode = 321

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    // 要申请的权限
    private val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(layoutId())
        requestPermission()
    }


    abstract fun layoutId(): Int
    abstract fun initView()


    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        cancel()
    }

    private fun requestPermission() {
        val isGranted = checkPermissionAllGranted(permissions)
        if (isGranted) {
            initView()
        } else {
            ActivityCompat.requestPermissions(this, permissions, permissionCode)
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
            LogUtil.e(msg = "判断是否所有的权限都已经授予了$isAllGranted")
            // 判断是否所有的权限都已经授予了
            for (grant in grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break
                }
            }
            if (isAllGranted) {
                LogUtil.e(msg = "所有的权限都已经授予$isAllGranted")
                initView()  // // 如果所有的权限都授予了, 则执行备份代码
            }
        }
    }

    private fun checkPermissionAllGranted(permissions: Array<out String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                LogUtil.e(msg = "checkPermissionAllGranted 权限")
                return false  // // 只要有一个权限没有被授予, 则直接返回 false
            }
        }
        return true
    }
}