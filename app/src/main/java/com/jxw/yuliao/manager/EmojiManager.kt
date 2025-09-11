package com.jxw.yuliao.manager

import android.content.Context
import android.content.res.AssetManager
import com.jxw.basemodel.base.IBaseManager
import com.jxw.yuliao.R
import com.tencent.qcloud.tuikit.timcommon.component.face.CustomFace
import com.tencent.qcloud.tuikit.timcommon.component.face.FaceGroup
import com.tencent.qcloud.tuikit.timcommon.component.face.FaceManager
import java.lang.ref.WeakReference

/**
 *
 * @author: jyq
 * @desc:  表情包初始化
 * @date: 2025/1/20
 */
object EmojiManager : IBaseManager {
    private var mContext: WeakReference<Context>? = null
    private var mAssetManager: AssetManager? = null
    private const val mFolderName = "emoji" // 获取的文件夹名
    override fun init(context: Context) {
        mContext = WeakReference(context)
        mAssetManager = context.assets
        initEmoji()
    }

    /**
     * 初始化表情包
     */
    private fun initEmoji() {
        val files = mAssetManager?.list(mFolderName)
        val folders = files?.filter { fileName ->
            // 检查每个文件是否是文件夹
            val isDirectory = mAssetManager?.list("$mFolderName/$fileName")?.isNotEmpty() == true
            isDirectory
        }
        createEmojiGroup(folders)
    }

    /**
     * 创建表情包 组
     * @param folders  emoji文件夹下的子文件夹 集合
     */
    private fun createEmojiGroup(folders: List<String>?) {
        // 获取每个文件夹下的所有子文件
        folders?.forEach { folder ->
            val programmerGroup = FaceGroup().apply {
                pageColumnCount = 5
                pageRowCount = 2
                groupName = folder
                groupID = folder.toInt()
            }
            createEmojiChild(programmerGroup, folder)
            FaceManager.addFaceGroup(programmerGroup.groupID, programmerGroup)
        }
    }

    /**
     * 创建真正的表情包并添加到 组中
     * @param programmerGroup  表情包组
     * @param folder  emoji文件夹下的子文件夹
     */
    private fun createEmojiChild(programmerGroup: FaceGroup, folder: String) {
        val subFiles = mAssetManager?.list("$mFolderName/$folder")
        // subFiles 现在包含该文件夹下的所有子文件
        subFiles?.forEachIndexed { index, subFile ->
            val fullPath = "$mFolderName/$folder/$subFile"
            if (index == 0) {
                programmerGroup.faceGroupIconUrl = "file:///android_asset/$fullPath"
            }
            val customFace = CustomFace().apply {
                faceKey = subFile
                setAssetPath(fullPath)
                width = mContext?.get()?.resources?.getDimensionPixelSize(R.dimen.x80) ?: 0
                height = mContext?.get()?.resources?.getDimensionPixelSize(R.dimen.x80) ?: 0
            }
            programmerGroup.addFace(subFile, customFace)
        }
    }


    override fun clear(context: Context?) {

    }
}