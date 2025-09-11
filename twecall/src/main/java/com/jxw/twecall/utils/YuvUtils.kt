package com.jxw.twecall.utils

/**
 *
 * @author: jyq
 * @desc: 视频流数据 转换或者旋转 工具类
 * @date: 2025/4/22
 */
object YuvUtils {

    const val ROTATE_0 = 0
    const val ROTATE_90 = 90
    const val ROTATE_180 = 180
    const val ROTATE_270 = 270

    /**
     * nv21转换成Yv12
     * @param nv21 nv21源数据
     * @param width 转换的宽度   宽必须为 16 的倍数，否则会导致显示异常或编码器崩溃
     * @param height 转换的高度  高必须为 16 的倍数，否则会导致显示异常或编码器崩溃
     * @return  转换旋转后的yv12数据
     */
    fun nv21ToYV12(nv21: ByteArray, width: Int, height: Int): ByteArray {
        if (width % 16 != 0 || height % 16 != 0) {
            throw IllegalArgumentException("宽高必须为 16 的倍数")
        }
        val frameSize = width * height
        val qFrameSize = frameSize / 4
        val yv12 = ByteArray(frameSize + 2 * qFrameSize)

        // 1. 拷贝 Y 分量
        System.arraycopy(nv21, 0, yv12, 0, frameSize)

        // 2. 提取 VU 并分别存入 V 和 U 平面
        var uvPos = frameSize
        val vStart = frameSize
        val uStart = frameSize + qFrameSize

        var i = 0
        while (i < qFrameSize) {
            yv12[vStart + i] = nv21[uvPos]     // V
            yv12[uStart + i] = nv21[uvPos + 1] // U
            uvPos += 2
            i++
        }
        return yv12
    }
    /**
     * 将 NV21 数据旋转指定角度后再转换为 YV12 格式
     * @param nv21 原始 NV21 数据
     * @param width 原始图像宽度（需为16的倍数）
     * @param height 原始图像高度（需为16的倍数）
     * @param rotation 旋转角度，只能是 0、90、180、270
     * @return 转换后的 YV12 数据
     */
    fun nv21ToYV12Rotated(nv21: ByteArray, width: Int, height: Int, rotation: Int): ByteArray {
        if (width % 16 != 0 || height % 16 != 0) {
            throw IllegalArgumentException("宽高必须为 16 的倍数")
        }

        val rotatedNV21 = when (rotation) {
            0 -> nv21
            90 -> rotateNV21Degree90(nv21, width, height)
            180 -> rotateNV21Degree180(nv21, width, height)
            270 -> rotateNV21Degree270(nv21, width, height)
            else -> throw IllegalArgumentException("只支持 0、90、180、270 度旋转")
        }

        val newWidth = if (rotation == 90 || rotation == 270) height else width
        val newHeight = if (rotation == 90 || rotation == 270) width else height

        return nv21ToYV12(rotatedNV21, newWidth, newHeight)
    }
    /**
     * nv21转换成Nv12
     * @param nv21 nv21源数据
     * @param width 转换的宽度
     * @param height 转换的高度
     * @param rotation 转换后旋转的角度
     * @return  转换旋转后的nv12数据
     */
    fun nv21ToNv12Rotated(nv21: ByteArray, width: Int, height: Int, rotation: Int): ByteArray {
        return when (rotation) {
            ROTATE_0 -> convertNv21ToNv12(nv21, width, height)
            ROTATE_90, ROTATE_180, ROTATE_270 -> rotateNv21ToNv12(nv21, width, height, rotation)
            else -> throw IllegalArgumentException("Unsupported rotation angle: $rotation")
        }
    }

    /**
     * nv21转换成Nv12
     */
    private fun convertNv21ToNv12(nv21: ByteArray, width: Int, height: Int): ByteArray {
        val frameSize = width * height
        val nv12 = ByteArray(nv21.size)

        // 拷贝 Y
        System.arraycopy(nv21, 0, nv12, 0, frameSize)

        // 转换 UV
        for (i in 0 until frameSize / 2 step 2) {
            nv12[frameSize + i] = nv21[frameSize + i + 1] // U
            nv12[frameSize + i + 1] = nv21[frameSize + i] // V
        }

        return nv12
    }

    /**
     * nv21转换成Nv12 并且旋转不同的角度
     */
    private fun rotateNv21ToNv12(nv21: ByteArray, width: Int, height: Int, degree: Int): ByteArray {
        val frameSize = width * height
        val output = ByteArray(nv21.size)

        // Y 旋转
        for (y in 0 until height) {
            for (x in 0 until width) {
                val srcIndex = y * width + x
                val destIndex = when (degree) {
                    ROTATE_90 -> x * height + (height - y - 1)
                    ROTATE_180 -> (height - y - 1) * width + (width - x - 1)
                    ROTATE_270 -> (width - x - 1) * height + y
                    else -> srcIndex
                }
                output[destIndex] = nv21[srcIndex]
            }
        }

        // UV 旋转（并转换为 NV12）
        val uvHeight = height / 2
        val uvWidth = width

        for (y in 0 until uvHeight) {
            for (x in 0 until uvWidth step 2) {
                val srcIndex = frameSize + y * uvWidth + x
                val v = nv21[srcIndex]
                val u = nv21[srcIndex + 1]

                var uvX = x / 2
                var uvY = y
                var destX = uvX
                var destY = uvY

                when (degree) {
                    ROTATE_90 -> {
                        destX = uvY
                        destY = uvWidth / 2 - uvX - 1
                    }

                    ROTATE_180 -> {
                        destX = uvWidth / 2 - uvX - 1
                        destY = uvHeight - uvY - 1
                    }

                    ROTATE_270 -> {
                        destX = uvHeight - uvY - 1
                        destY = uvX
                    }
                }

                val destIndex = frameSize + (destY * uvWidth) + (destX * 2)
                output[destIndex] = u
                output[destIndex + 1] = v
            }
        }

        return output
    }

    /**
     * nv21 旋转函数
     * @param data nv21源数据
     * @param width 转换的宽度
     * @param height 转换的高度
     * @param rotation 转换后旋转的角度
     * @return  转换旋转后的nv12数据
     */
    fun rotateNV21(data: ByteArray, width: Int, height: Int, rotation: Int): ByteArray {
        val output = ByteArray(data.size)

        when (rotation) {
            0 -> {
                // 无旋转，直接返回原数据副本
                System.arraycopy(data, 0, output, 0, data.size)
            }

            90 -> {
                rotate90(data, output, width, height)
            }

            180 -> {
                rotate180(data, output, width, height)
            }

            270 -> {
                rotate270(data, output, width, height)
            }

            else -> {
                throw IllegalArgumentException("Only support rotation of 0, 90, 180, or 270 degrees.")
            }
        }

        return output
    }

    private fun rotate90(input: ByteArray, output: ByteArray, width: Int, height: Int) {
        val ySize = width * height
        var i = 0

        // Rotate Y
        for (x in 0 until width) {
            for (y in height - 1 downTo 0) {
                output[i++] = input[y * width + x]
            }
        }

        // Rotate UV
        var uvOffset = ySize
        for (x in 0 until width step 2) {
            for (y in height / 2 - 1 downTo 0) {
                val u = input[ySize + y * width + x]
                val v = input[ySize + y * width + x + 1]
                output[i++] = u
                output[i++] = v
            }
        }
    }

    private fun rotate180(input: ByteArray, output: ByteArray, width: Int, height: Int) {
        val ySize = width * height
        val totalSize = ySize * 3 / 2

        for (i in 0 until ySize) {
            output[i] = input[ySize - 1 - i]
        }

        for (i in ySize until totalSize step 2) {
            output[i] = input[totalSize - (i - ySize) - 2]
            output[i + 1] = input[totalSize - (i - ySize) - 1]
        }
    }

    private fun rotate270(input: ByteArray, output: ByteArray, width: Int, height: Int) {
        val ySize = width * height
        var i = 0

        // Rotate Y
        for (x in width - 1 downTo 0) {
            for (y in 0 until height) {
                output[i++] = input[y * width + x]
            }
        }

        // Rotate UV
        for (x in width - 1 downTo 0 step 2) {
            for (y in 0 until height / 2) {
                val u = input[ySize + y * width + x - 1]
                val v = input[ySize + y * width + x]
                output[i++] = u
                output[i++] = v
            }
        }
    }

    private fun rotateNV21Degree90(data: ByteArray, width: Int, height: Int): ByteArray {
        val ySize = width * height
        val uvHeight = height / 2
        val output = ByteArray(data.size)
        var i = 0

        // Rotate Y
        for (x in 0 until width) {
            for (y in height - 1 downTo 0) {
                output[i++] = data[y * width + x]
            }
        }

        // Rotate UV
        val uvStart = ySize
        for (x in 0 until width step 2) {
            for (y in uvHeight - 1 downTo 0) {
                val pos = uvStart + y * width + x
                output[i++] = data[pos]
                output[i++] = data[pos + 1]
            }
        }

        return output
    }

    private fun rotateNV21Degree180(data: ByteArray, width: Int, height: Int): ByteArray {
        val total = width * height * 3 / 2
        val output = ByteArray(total)

        // Rotate Y
        var i = 0
        var j = width * height - 1
        while (i < width * height) {
            output[i++] = data[j--]
        }

        // Rotate UV
        j = total - 2
        while (i < total) {
            output[i++] = data[j]
            output[i++] = data[j + 1]
            j -= 2
        }

        return output
    }

    private fun rotateNV21Degree270(data: ByteArray, width: Int, height: Int): ByteArray {
        val ySize = width * height
        val uvHeight = height / 2
        val output = ByteArray(data.size)
        var i = 0

        // Rotate Y
        for (x in width - 1 downTo 0) {
            for (y in 0 until height) {
                output[i++] = data[y * width + x]
            }
        }

        // Rotate UV
        val uvStart = ySize
        for (x in width - 2 downTo 0 step 2) {
            for (y in 0 until uvHeight) {
                val pos = uvStart + y * width + x
                output[i++] = data[pos]
                output[i++] = data[pos + 1]
            }
        }

        return output
    }


}
