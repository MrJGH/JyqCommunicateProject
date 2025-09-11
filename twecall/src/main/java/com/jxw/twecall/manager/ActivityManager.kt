package com.jxw.twecall.manager

import android.app.Activity
import android.content.Context
import com.jxw.basemodel.base.IBaseManager
import com.jxw.twecall.utils.ext.tryCatch

/**
 *
 * @author: jyq
 * @date: 2024/1/2
 * @descp: Activity管理类
 */
internal object ActivityManager : IBaseManager {

    private val tasks = mutableListOf<Activity>()

    override fun init(context: Context) {

    }

    override fun clear(context: Context?) {
        tryCatch<ActivityManager> {
            val it = tasks.iterator()
            while (it.hasNext()) {
                val item = it.next()
                it.remove()
                item.finish()
            }
        }
    }

    fun add(activity: Activity) {
        tasks.add(activity)
    }

    fun remove(activity: Activity) {
        tasks.remove(activity)
    }

    fun top(): Activity? {
        if (tasks.isNotEmpty()) {
            return tasks.last()
        }
        return null
    }


}