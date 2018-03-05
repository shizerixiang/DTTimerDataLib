#include <jni.h>

#include "android/log.h"

static const char *TAG = "native_lib";
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

/**
 * 显示 Toast
 * @param env JNIEnv
 * @param thiz this
 * @param context 上下文
 * @param msg 消息体
 * @return 消息体
 */
//extern "C" // .cpp 文件中没有该语句将无法在 Java 代码中调用 Native 方法，.c 文件无需添加该语句
JNIEXPORT jstring JNICALL Java_com_beviswang_datalibrary_util_JNIHelper_showToast
        (JNIEnv *env, jobject thiz, jobject context, jobject msg) {
    jclass jclazz;
    jclazz = (*env)->FindClass(env, "android/widget/Toast");
    jmethodID makeText = (*env)->GetStaticMethodID(
            env, jclazz, "makeText",
            "(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;");
    jobject toast = (*env)->CallStaticObjectMethod(env, jclazz, makeText, context, msg, 1);
    jmethodID show = (*env)->GetMethodID(env, jclazz, "show", "()V");
    (*env)->CallVoidMethod(env, toast, show);
    jstring returnJstring = (jstring) msg;
    LOGD("当前 Toast 的消息为：%s", (*env)->GetStringUTFChars(env, returnJstring, 0));
    return returnJstring;
}

/**
 * 获取手机的厂商
 * @param env JNIEnv
 * @param thiz this
 * @return 手机的厂商
 */
JNIEXPORT jstring JNICALL Java_com_beviswang_datalibrary_util_JNIHelper_getPhoneModel
        (JNIEnv *env, jobject thiz) {
    jclass jclazz;
    jclazz = (*env)->FindClass(env, "android/os/Build");
    jfieldID modelID = (*env)->GetStaticFieldID(env, jclazz, "MODEL", "Ljava/lang/String;");
    jstring modelJString = (jstring) (*env)->GetStaticObjectField(env, jclazz, modelID);
    LOGD("当前手机型号为：%s", (*env)->GetStringUTFChars(env, modelJString, 0));
    return modelJString;
}

/**
 * 获取手机的 IMEI
 * @param env JNIEnv
 * @param thiz this 当前对象
 * @param context 上下文对象
 * @return 获取到的 IMEI
 */
JNIEXPORT jstring JNICALL Java_com_beviswang_datalibrary_util_JNIHelper_getIMEI
        (JNIEnv *env, jobject thiz, jobject context) {
    jclass jclazz;
    // 注意：对于 Kotlin 的 object ，在获取时必须先获取该 object 的实例，即：INSTANCE 之后才能调用 object 中的方法
    jclazz = (*env)->FindClass(env, "com/beviswang/datalibrary/util/SystemHelper");
    jfieldID systemHelperID = (*env)->GetStaticFieldID(env, jclazz, "INSTANCE",
                                                       "Lcom/beviswang/datalibrary/util/SystemHelper;");
    jobject jObj = (*env)->GetStaticObjectField(env, jclazz, systemHelperID);
    jmethodID phoneIMEIID = (*env)->GetMethodID(env, jclazz, "getIMEI",
                                                 "(Landroid/content/Context;)Ljava/lang/String;");
    jstring jStr = (jstring) (*env)->CallObjectMethod(env, jObj, phoneIMEIID, context);
    return jStr;
}