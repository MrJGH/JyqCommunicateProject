package com.jxw.twecall.utils.ext

import com.jxw.twecall.utils.LogUtil


/**
 *
 * description:
 * <pre>
 *      try...catch简写
 * </pre>
 **/
inline fun tryCatch(tryBlock: () -> Unit, catchBlock: (Throwable) -> Unit = {}) {
    try {
        tryBlock()
    } catch (t: Throwable) {
        LogUtil.e(msg = t.toString())
        catchBlock(t)
    }
}

inline fun <T> tryCatch(tryBlock: () -> Unit) {
    try {
        tryBlock()
    } catch (t: Throwable) {
        t.printStackTrace()
        LogUtil.e(msg = t.toString())
    } finally {

    }
}

inline fun tryCatch(tryBlock: () -> Unit, noinline finallyBlock: () -> Unit) {
    try {
        tryBlock()
    } catch (t: Throwable) {
        LogUtil.e(msg = t.toString())
    } finally {
        finallyBlock.invoke()
    }
}