package com.dji.videostreamdecodingsample;
import okhttp3.OkHttpClient;

// okhttp 需要单例，否则会栈溢出导致内存泄露
public class OKHttpUtil {
    private static OkHttpClient singleton;

    private OKHttpUtil(){
    }

    public static OkHttpClient getInstanceOne(){
        if (singleton == null) {
            synchronized (OKHttpUtil.class) {
                if (singleton == null) {
                    singleton = new OkHttpClient();
                }
            }
        }
        return singleton;
    }
    public static OkHttpClient getInstance(){
        if (singleton == null) {
            synchronized (OKHttpUtil.class) {
                if (singleton == null) {
                    singleton = new OkHttpClient();
                }
            }
        }
        return singleton;
    }
}
