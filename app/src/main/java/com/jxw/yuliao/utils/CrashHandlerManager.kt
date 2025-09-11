package com.jxw.yuliao.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager.BadTokenException
import androidx.annotation.Keep
import com.google.gson.Gson
import com.jxw.validate.utils.LauncherDataUtils
import com.jxw.yuliao.App
import com.jxw.yuliao.BuildConfig
import com.jxw.basemodel.base.IBaseManager
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 奔溃兜底策略
 */
object CrashHandlerManager : Thread.UncaughtExceptionHandler, IBaseManager {
    private const val TAG = "CrashHandlerManager"
    private const val AUTHORITIES_NORMAL = "com.jxw.xueqing.StudyProvider"

    // 系统默认的UncaughtException处理类
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    override fun init(context: Context) {
        //设置该MyCrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this)
    }


    override fun uncaughtException(thread: Thread, ex: Throwable) {
        //记录错误信息
        saveCrashInfoToFile(ex)
        //判断异常是否需要兜底
        if (needBandage(ex)) {
            bandage()
            return
        }
        // 如果用户没有处理则让系统默认的异常处理器来处理 目的是判断异常是否已经被处理
        mDefaultHandler?.uncaughtException(thread, ex)
    }

    private fun needBandage(ex: Throwable): Boolean {
        val message = ex.message
        Log.e(TAG,"崩溃信息:$message")
        //如果是没磁盘空间了，尝试清理一波缓存
        return if (!TextUtils.isEmpty(message) && message!!.contains("No space left on device")) {
            true
        } else ex is BadTokenException
    }

    /**
     * 让当前线程恢复运行
     */
    private fun bandage() {
        try {
            if (Looper.myLooper() == null) {
                Looper.prepare()
            }
            Looper.loop()
        } catch (e: Exception) {
            uncaughtException(Thread.currentThread(), e)
        }
    }

    /**
     * 保存错误信息到文件中
     */
    private fun saveCrashInfoToFile(ex: Throwable) {
        //获取错误原因
        val writer: Writer = StringWriter()
        val printWriter = PrintWriter(writer)
        ex.printStackTrace(printWriter)
        var exCause = ex.cause
        while (exCause != null) {
            exCause.printStackTrace(printWriter)
            exCause = exCause.cause
        }
        printWriter.close()
        insertError(writer.toString())
    }


    //错误上传
    private fun insertError(message: String?) {
        val contentValues = ContentValues()
        val errorBean = ErrorBean()
        errorBean.extraIsbn = LauncherDataUtils.apkv //尾标
        errorBean.serialNo = LauncherDataUtils.serialNumber //SN
        errorBean.deviceModel = Build.MODEL //设备型号
        errorBean.sysName = "Android" //系统名称
        errorBean.sysVersion = Build.VERSION.RELEASE //系统版本
        errorBean.appName = "语聊" //app名称
        errorBean.packageName = BuildConfig.APPLICATION_ID //app包名
        errorBean.packageVersion = BuildConfig.VERSION_NAME //app版本号
        errorBean.message = message
        errorBean.occurTime = getCurrentTime()
        errorBean.category = "kdxxj" //品类:平板、智能相机、海外翻译机（定义pb、znxj、hwfyj）
        contentValues.put("appId", "ERROR-android")
        contentValues.put("data", Gson().toJson(errorBean))
        Log.e(TAG,"异常上报详情:$contentValues")
        insert(contentValues)
    }

    private fun getCurrentTime(): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) // yyyy-MM-dd HH:mm:ss
        //获取当前时间
        val date = Date(System.currentTimeMillis())
        return simpleDateFormat.format(date)
    }

    private fun insert(contentValues: ContentValues) {
        try {
            val uri = Uri.parse("content://" + AUTHORITIES_NORMAL)
            val insertUri = App.getInstance().contentResolver.insert(uri, contentValues)
            //            LogUtil.d(TAG, "插入结果:: " + insertUri);
        } catch (e: Exception) {
//            LogUtil.e(TAG, "缺少 学情应用程序");
//            e.printStackTrace();
        }
    }


    override fun clear(context: Context?) {

    }

    @Keep
    data class ErrorBean(
        //    public String registerIsbn;//注册标签
        var extraIsbn: String? = null, //尾标
        var serialNo: String? = null, //SN号
        var deviceModel: String? = null, //设备型号
        var sysName: String? = null, //系统名称
        var sysVersion: String? = null, //系统版本
        var appName: String? = null,
        var packageName: String? = null, //应用包名
        var packageVersion: String? = null, //应用版本
        var message: String? = null, //详细错误信息
        var occurTime: String? = null, //错误发生时间
        var category: String? = null, //品类:平板、智能相机、海外翻译机（定义pb、znxj、hwfyj）
    )
}