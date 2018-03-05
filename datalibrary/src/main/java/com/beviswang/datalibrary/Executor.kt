package com.beviswang.datalibrary

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

/**
 * 执行消息任务
 * Created by shize on 2018/2/6.
 */
class ExecutorContext<T>(val weakRef: WeakReference<T>)

/**
 * 只存在一个线程的异步任务
 * 可以通过 [uiThread] 返回到主线程中
 */
fun <T> T.doSend(exService: ExecutorService = Publish.getInstance().exServer,
                 task: ExecutorContext<T>.() -> Unit): Future<*> {
    val context = ExecutorContext(WeakReference(this))
    return exService.submit { context.task() }
}

/**
 * 在 [doSend] 子线程中返回主线程执行任务
 */
fun <T> ExecutorContext<T>.uiThread(f: (T) -> Unit): Boolean {
    val ref = weakRef.get() ?: return false
    if (ContextHelper.mainThread == Thread.currentThread()) {
        f(ref)
    } else {
        ContextHelper.handler.post { f(ref) }
    }
    return true
}

fun <T> T.logI(msg: String?) {
    val clazzName = (this as Any).javaClass.simpleName
    if (msg == null){
        Log.i(clazzName, "null")
        return
    }
    Log.i(clazzName, msg)
}

fun <T> T.logE(msg: String?) {
    val clazzName = (this as Any).javaClass.simpleName
    if (msg == null){
        Log.e(clazzName, "null")
        return
    }
    Log.e(clazzName, msg)
}

fun <T> T.logD(msg: String?) {
    val clazzName = (this as Any).javaClass.simpleName
    if (msg == null){
        Log.d(clazzName, "null")
        return
    }
    Log.d(clazzName, msg)
}

fun <T> T.logW(msg: String?) {
    val clazzName = (this as Any).javaClass.simpleName
    if (msg == null){
        Log.w(clazzName, "null")
        return
    }
    Log.w(clazzName, msg)
}

private object ContextHelper {
    val handler = Handler(Looper.getMainLooper())
    val mainThread: Thread = Looper.getMainLooper().thread
}