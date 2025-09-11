package com.jxw.basemodel.base

import androidx.multidex.MultiDexApplication

/**
 *
 * @author: jyq
 * @desc:
 * @date: 2024/12/15
 */
open class BaseApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }


    companion object {
        private lateinit var instance: BaseApplication
        fun getInstance() = instance
    }
}