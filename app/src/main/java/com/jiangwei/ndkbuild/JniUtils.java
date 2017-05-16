package com.jiangwei.ndkbuild;

/**
 * author:  jiangwei18 on 17/5/16 15:12
 * email:  jiangwei18@baidu.com
 * Hi:   jwill金牛
 */

public class JniUtils {
    static {
        System.loadLibrary("JNITest");
    }

    public native String testJni();
}
