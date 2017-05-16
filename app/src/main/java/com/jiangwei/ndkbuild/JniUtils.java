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

    public native void oneFileToTwo(String localPath, String onePath, String secondPath);

    public native void twoFileInOne(String onePath, String secondPath, String combinePath);
}
