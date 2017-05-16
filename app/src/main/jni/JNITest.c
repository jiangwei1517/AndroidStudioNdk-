#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <android/log.h>

#define  LOG_TAG    "native-dev"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)

JNIEXPORT jstring JNICALL
Java_com_jiangwei_ndkbuild_JniUtils_testJni(JNIEnv *env, jobject instance) {
    jstring str = (*env)->NewStringUTF(env, "I am from C...");
    size_t i = strlen("abcd");
    LOGI("数组的长度是%ld", i);
    return str;
}
