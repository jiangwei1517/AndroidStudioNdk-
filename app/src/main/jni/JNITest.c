#include <jni.h>
#include <stdlib.h>
#include <android/log.h>
#include <stdio.h>

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

JNIEXPORT void JNICALL
Java_com_jiangwei_ndkbuild_JniUtils_oneFileToTwo(JNIEnv *env, jobject instance, jstring localPath_,
                                                 jstring onePath_, jstring secondPath_) {
    const char *localPath = (*env)->GetStringUTFChars(env, localPath_, 0);
    const char *onePath = (*env)->GetStringUTFChars(env, onePath_, 0);
    const char *secondPath = (*env)->GetStringUTFChars(env, secondPath_, 0);

    FILE *read_file = fopen(localPath, "r");
    FILE *one_file = fopen(onePath, "w");
    FILE *two_file = fopen(secondPath, "w");
    if (read_file != NULL) {
        int len = 0;
        fseek(read_file, 0, SEEK_END);
        long length = ftell(read_file);
        long oneLength = length / 2;
        LOGE("first_file_length:%ld\n", oneLength);
        long secondLength = length - oneLength;
        long count = 0;
        fseek(read_file, 0, SEEK_SET);
        while ((len = fgetc(read_file)) != EOF) {
            fputc(len, one_file);
            count++;
            if (count == oneLength) {
                break;
            }
        }
        while ((len = fgetc(read_file)) != EOF) {
            fputc(len, two_file);
        }
        if (read_file != NULL) {
            fclose(read_file);
            read_file = NULL;
        }
        if (one_file != NULL) {
            fclose(one_file);
            one_file = NULL;
        }
        if (two_file != NULL) {
            fclose(two_file);
            two_file = NULL;
        }
    } else {
        LOGI("没有找到目标文件\n");
    }

    (*env)->ReleaseStringUTFChars(env, localPath_, localPath);
    (*env)->ReleaseStringUTFChars(env, onePath_, onePath);
    (*env)->ReleaseStringUTFChars(env, secondPath_, secondPath);
}
