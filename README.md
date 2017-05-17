## AndroidStudio NDK环境搭建

* 记JNI补充(GetStringUTFChars---RealeaseStringUTFChars):文档建议搭配使用
* GetStringUTFChars(...*isCopy). isCopy是JNI内部决定false/true，文档建议传入NULL。
* C抛出的异常是Throwable(Error,不是Exception)类型，因此Java的Exception异常无法补获（上一次写错了。。。）ExceptionOccured->ExceptionClear。文档建议用Throwable或者Error获取异常。

## 参考链接
使用Android Studio 进行NDK开发和调试
<http://www.jianshu.com/p/2690c9964110>

Android Studio使用gradle-experimental构建NDK工程(无需Android.mk、Application.mk文件)
<http://www.jianshu.com/p/7844aafe897d>

## 注意事项
默认C下printf不会在AS控制台下输出，因此通过JNI打印log。
	
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
	
## 文件拆分

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
	
## 文件合并

	JNIEXPORT void JNICALL
	Java_com_jiangwei_ndkbuild_JniUtils_twoFileInOne(JNIEnv *env, jobject instance, jstring onePath_,
	                                                 jstring secondPath_, jstring combinePath_) {
	    const char *onePath = (*env)->GetStringUTFChars(env, onePath_, 0);
	    const char *secondPath = (*env)->GetStringUTFChars(env, secondPath_, 0);
	    const char *combinePath = (*env)->GetStringUTFChars(env, combinePath_, 0);
	
	    FILE *one_file = fopen(onePath, "r");
	    FILE *two_file = fopen(secondPath, "r");
	    FILE *combine_file = fopen(combinePath, "w");
	    if (one_file != NULL && two_file != NULL) {
	        int len = 0;
	        while ((len = fgetc(one_file)) != EOF) {
	            fputc(len, combine_file);
	        }
	        while ((len = fgetc(two_file)) != EOF) {
	            fputc(len, combine_file);
	        }
	        if (one_file != NULL) {
	            fclose(one_file);
	            one_file = NULL;
	        }
	        if (two_file != NULL) {
	            fclose(two_file);
	            two_file = NULL;
	        }
	        if (combine_file != NULL) {
	            fclose(combine_file);
	            combine_file = NULL;
	        }
	    } else {
	        LOGI("没有找到目标文件\n");
	    }
	    (*env)->ReleaseStringUTFChars(env, onePath_, onePath);
	    (*env)->ReleaseStringUTFChars(env, secondPath_, secondPath);
	    (*env)->ReleaseStringUTFChars(env, combinePath_, combinePath);
	}
