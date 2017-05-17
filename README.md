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

# NDK查分包生成合并
之前NDK环境一直都是基于gradle-experimental，这里有个坑，一直以为找不到生成的so文件，其实build后的so文件存在与intermediates/binares下面:

* Android Studio使用gradle-experimental构建NDK工程(无需Android.mk、Application.mk文件)
<http://www.jianshu.com/p/7844aafe897d>

### app/gradle下配置：
	//apply plugin: 'com.android.application'

	// 用com.android.model.application 代替 com.android.application
	apply plugin: 'com.android.model.application'

	// 将原来的配置用 model{}包起来
	model {
	    android {
	        // 取值必须使用 “=” 形式
	        // 否则会报 “Error:Cause: org.gradle.api.internal.ExtensibleDynamicObject” 错误
	        compileSdkVersion = 25
	        buildToolsVersion = '23.0.3'

	        defaultConfig {
	            // 取值必须使用 “=” 形式
	            applicationId = "com.jiangwei.jnitest"
	            //这里要注意是 xxSdkVersion.apiLevel
	            // 否则会报 “Unable to load class com.android.build.gradle.managed.ProductFlavor_Impl”错误
	            minSdkVersion.apiLevel = 15
	            targetSdkVersion.apiLevel = 23
	            versionCode =  1
	            versionName = "1.0"
	        }

	        // 配置NDK
	        ndk {
	            // 生成so的名字，是必须的
	            moduleName ="bspatch"
	            toolchain = 'clang'
	            CFlags.add('-std=c99')
	            // 添加依赖库
	            ldLibs.addAll(['android','OpenSLES', 'log'])
	            // 生成不同abi体系的so库
	            abiFilters.addAll(['armeabi', 'armeabi-v7a', 'arm64-v8a',
	                               'x86', 'x86_64',
	                               'mips', 'mips64'])
	        }
	        sources {
	            main {
	                jniLibs {
	                    source {
	                        srcDirs = ['src/main/jniLibs']
	                    }
	                }
	            }
	        }
	        buildTypes {
	            release {
	                minifyEnabled = false
	                // 这里注意：使用proguardFiles.add()方法
	                proguardFiles.add(file('proguard-rules.txt'))
	            }
	        }
	    }
	}
	dependencies {
	    compile fileTree(dir: 'libs', include: ['*.jar'])
	    androidTestCompile('com.android.support.test.espresso:espresso-core:2.2.2', {
	        exclude group: 'com.android.support', module: 'support-annotations'
	    })
	    compile 'com.android.support:appcompat-v7:25.3.1'
	    testCompile 'junit:junit:4.12'
	}

## 参考资料
 Android 增量更新完全解析 是增量不是热修复
<http://blog.csdn.net/lmj623565791/article/details/52761658>
React Native 飞行日记——bsdiff源码编译
<http://www.jianshu.com/p/e745e28516a1>

## 实现原理
增量更新的原理非常简单，就是将本地apk与服务器端最新版本比对，并得到差异包，用户更新App时只需要下载差异包。例如，当前安装新浪微博V3.5，12.8 MB，新浪微博最新版V4.0，15.4MB，经过对两个版本比较之后，发现差异只有7、8M，这时候用户更新的时候只需要下载一个7、8M的差异包便可，不需要整包下载15.4M的新版微博客户端。下载差异包后，在手机端使用旧版apk+差异包，合成得到微博最新版V4.0，提醒用户安装即可。

### 项目依赖BsDiff和Bzip2这两个开源项目（BsDiff依赖Bzip2）

*  BsDiff下载链接：<http://www.daemonology.net/bsdiff/>
*  Bzip2下载链接：<http://www.bzip.org/downloads.html>