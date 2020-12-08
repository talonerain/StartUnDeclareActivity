package com.xunmeng.pinduoduo.startundeclareactivity;

import android.app.Instrumentation;
import android.os.Build;
import android.os.Handler;

import java.lang.reflect.Proxy;

/**
 * @author 茗烟
 * @date 2020/11/22
 */
public class AMSHookHelper {
    public static final String EXTRA_TARGET_INTENT = "extra_target_intent";

    /**
     * Hook将Activity信息穿给AMS之前
     * 主要完成的操作是  "把真正要启动的Activity临时替换为在AndroidManifest.xml中声明的替身Activity",进而骗过AMS
     *
     * @throws ClassNotFoundException
     */
    public static void hookAMS() throws ClassNotFoundException {
        //获取gDefault单例
        Object gDefault;
        if (Build.VERSION.SDK_INT <= 25) {
            gDefault = RefInvoke.getStaticFiledObject("android.app.ActivityManagerNative", "gDefault");
        } else if (Build.VERSION.SDK_INT <= 28) {
            gDefault = RefInvoke.getStaticFiledObject("android.app.ActivityManager", "IActivityManagerSingleton");
        } else {
            //Instrumentation源码可发现API29及以上ActivityManager被ActivityTaskManager代替，接口类型也改为了IActivityTaskManager
            gDefault = RefInvoke.getStaticFiledObject("android.app.ActivityTaskManager", "IActivityTaskManagerSingleton");
        }
        //gDefault是一个Singleton<T>对象；我们取出这个单例里面的mInstance字段
        Object mInstance = RefInvoke.getFieldObject("android.util.Singleton", gDefault, "mInstance");
        //创建一个代理对象proxy，替换mInstance
        Class<?> classB2Interface;
        if (Build.VERSION.SDK_INT >= 29) {
            classB2Interface = Class.forName("android.app.IActivityTaskManager");
        } else {
            classB2Interface = Class.forName("android.app.IActivityManager");
        }
        Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{classB2Interface},
                new MockClass1(mInstance));
        //把gDefault的mInstance字段修改为proxy
        RefInvoke.setFieldObject("android.util.Singleton", gDefault, "mInstance", proxy);
    }

    /**
     * MockClass代码有问题，行不通！
     *
     * @throws Exception
     */
    public static void attachBaseContext() throws Exception {
        //先获取到当前的ActivityThread对象
        Object currentActivityThread = RefInvoke.getStaticFiledObject("android.app.ActivityThread", "sCurrentActivityThread");
        //由于ActivityThread一个进程只有一个，我们获取这个对象的mH
        Handler mH = (Handler) RefInvoke.getFieldObject("android.app.ActivityThread", currentActivityThread, "mH");
        RefInvoke.setFieldObject(Handler.class.getName(), mH, "mCallback", new MockClass2(mH));
    }

    /**
     * Hook AMS通知App启动Activity
     */
    public static void attachContext() {
        //先获取到当前的ActivityThread对象
        Object currentActivityThread = RefInvoke.getStaticFiledObject("android.app.ActivityThread", "sCurrentActivityThread");
        //拿到原始的mInstrumentation字段
        Instrumentation mInstrumentation = (Instrumentation) RefInvoke.getFieldObject(currentActivityThread.getClass().getName(), currentActivityThread, "mInstrumentation");
        //创建代理对象
        Instrumentation evilInstrumentation = new EvilInstrumentation(mInstrumentation);
        RefInvoke.setFieldObject(currentActivityThread.getClass().getName(), currentActivityThread, "mInstrumentation", evilInstrumentation);
    }
}
