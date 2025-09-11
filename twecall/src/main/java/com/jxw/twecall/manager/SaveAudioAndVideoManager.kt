package com.jxw.twecall.manager

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Environment
import android.util.Log
import com.jxw.basemodel.utils.LauncherDataUtil
import com.jxw.twecall.utils.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.Executors

/**
 *
 * @author: jyq
 * @desc: 保存音频或视频到本地sdcard 下   一般调试时使用
 * @date: 2025/3/26
 */
internal class SaveAudioAndVideoManager {
    private val TAG = "SaveAudioAndVideoManage"
    private val encoderVideoH264FilePath = "/sdcard/编码后的视频.h264"
    private val encoderVideoH264Fos: FileOutputStream by lazy { FileOutputStream(getFile(encoderVideoH264FilePath)) }

    private val encoderVideoNv21FilePath = "/sdcard/预览时原视频.nv21"  //这里的格式nv21 需要和camera的配置相同
    private val encoderVideoNv21Fos: FileOutputStream by lazy { FileOutputStream(getFile(encoderVideoNv21FilePath)) }

    private val decoderVideoH264FilePath = "/sdcard/解码后的视频.h264"
    private val decoderVideoH264Fos: FileOutputStream by lazy { FileOutputStream(getFile(decoderVideoH264FilePath)) }

    private val encoderAudioAccFilePath = "/sdcard/编码后的音频.pcm"
    private val encoderAudioAccFos: FileOutputStream by lazy { FileOutputStream(getFile(encoderAudioAccFilePath)) }

    private val mSaveLocalVideoExecutor by lazy { Executors.newSingleThreadExecutor() }
    private val mSaveLocalAudioExecutor by lazy { Executors.newSingleThreadExecutor() }
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var isSaveFrame = false  //保存图片帧标记
    private var mPicIndex = 0 //保存图片名字 前缀

    /**
     * 保存 编码后的视频到本地 测试时使用
     */
    fun saveLocalEncodedVideo(data: ByteArray) {
        if (mSaveLocalVideoExecutor.isShutdown) return
        mSaveLocalVideoExecutor.submit {
            try {
                encoderVideoH264Fos.write(data)
                encoderVideoH264Fos.flush()
            } catch (e: IOException) {
                Log.e(TAG, "Error saving video", e)
                e.printStackTrace()
            }
        }
    }

    /**
     * 保存 解码后的视频到本地 测试时使用
     */
    fun saveLocalDecodedVideo(data: ByteArray) {
        if (mSaveLocalVideoExecutor.isShutdown) return
        mSaveLocalVideoExecutor.submit {
            try {
                decoderVideoH264Fos.write(data)
                decoderVideoH264Fos.flush()
            } catch (e: IOException) {
                Log.e(TAG, "Error saving video", e)
                e.printStackTrace()
            }
        }
    }

    /**
     * 保存预览的视频到本地 测试时使用
     */
    fun saveLocalPreViewVideo(data: ByteArray) {
        if (mSaveLocalVideoExecutor.isShutdown) return
        mSaveLocalVideoExecutor.submit {
            try {
                encoderVideoNv21Fos.write(data)
                encoderVideoNv21Fos.flush()
            } catch (e: IOException) {
                Log.e(TAG, "Error saving video", e)
                e.printStackTrace()
            }
        }
    }

    /**
     * 开始保存预览的视频的图片 任务
     */
    fun startSavingFrames() {
        coroutineScope.launch {
            while (isActive) {
                delay(10000) // 每隔 10 秒保存一帧
                isSaveFrame = true
                mPicIndex++
            }
        }
    }

    /**
     *  保存预览的视频的图片到本地 测试时使用
     */
    fun saveLocalPreViewVideoBitMap(
        data: ByteArray,
        width: Int = LauncherDataUtil.getCameraPreviewWidthSize(),
        height: Int = LauncherDataUtil.getCameraPreviewHeightSize()
    ) {

        if (!isSaveFrame) return // 只有在标志为 true 时才保存
        isSaveFrame = false
        val yuvImage = YuvImage(data, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out) // 质量 100%
        val imageBytes = out.toByteArray()
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val fileName = mPicIndex.toString() + "_preview_frame.jpg"
        saveBitmap(bitmap, fileName)
    }


    /**
     * 保存 编码的音频到本地 测试时使用
     */
    fun saveLocalEncodedAudio(data: ByteArray) {
        if (mSaveLocalAudioExecutor.isShutdown) return
        mSaveLocalAudioExecutor.submit {
            try {
                encoderAudioAccFos.write(data)
                encoderAudioAccFos.flush()
            } catch (e: IOException) {
                Log.e(TAG, "Error saving audio", e)
                e.printStackTrace()
            }
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        if (mSaveLocalVideoExecutor != null) {
            mSaveLocalVideoExecutor.shutdown()
            encoderVideoH264Fos.close()
            encoderVideoNv21Fos.close()
            coroutineScope.cancel()
        }
        if (mSaveLocalAudioExecutor != null) {
            mSaveLocalAudioExecutor.shutdown()
            encoderAudioAccFos.close()
        }
    }

    private fun saveBitmap(bitmap: Bitmap, fileName: String = "preview_frame.jpg") {
        val file = File(Environment.getExternalStorageDirectory(), fileName)
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            LogUtil.d(TAG, "saveBitmap 保存成功: ${file.absolutePath}")
        } catch (e: IOException) {
            Log.e(TAG, "Error saving bitmap", e)
            e.printStackTrace()
        }
    }


    private fun getFile(path: String): File {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        return file
    }

    companion object {
        private const val TAG = "SaveAudioAndVideoManage"
    }

}