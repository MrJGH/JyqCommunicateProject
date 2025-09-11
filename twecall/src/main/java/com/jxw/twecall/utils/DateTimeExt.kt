package com.jxw.twecall.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 *  字符串日期格式（比如：2018-4-6)转为毫秒
 *  @param format 时间的格式，默认是按照yyyy-MM-dd HH:mm:ss来转换，如果您的格式不一样，则需要传入对应的格式
 */
fun String.toDateMills(format: String = "yyyy-MM-dd HH:mm:ss") =
    SimpleDateFormat(format, Locale.getDefault()).parse(this).time

/**
 * Long类型时间戳转为字符串的日期格式
 * @param format 时间的格式，默认是按照yyyy-MM-dd HH:mm:ss来转换，如果您的格式不一样，则需要传入对应的格式
 */
fun Long.toDateString(format: String = "yyyy-MM-dd HH:mm:ss") =
    SimpleDateFormat(format, Locale.getDefault()).format(Date(this))

fun Int.formatSecondsTo00(): String? {
    val second = this % 60
    val minuteTemp = this / 60
    return if (minuteTemp > 0) {
        val minute = minuteTemp % 60
        val hour = minuteTemp / 60
        if (hour > 0) {
            ((if (hour >= 10) hour.toString() + "" else "0$hour") + ":" + (if (minute >= 10) minute.toString() + "" else "0$minute") + ":"
                    + if (second >= 10) second.toString() + "" else "0$second")
        } else {
            (if (minute >= 10) minute.toString() + "" else "0$minute") + ":" + if (second >= 10) second.toString() + "" else "0$second"
        }
    } else {
        "00:" + if (second >= 10) second.toString() + "" else "0$second"
    }
}

