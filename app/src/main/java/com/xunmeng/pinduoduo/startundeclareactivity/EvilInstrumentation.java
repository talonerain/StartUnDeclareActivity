package com.xunmeng.pinduoduo.startundeclareactivity;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;

/**
 * @author 茗烟
 * @date 2020/11/22
 */
public class EvilInstrumentation extends Instrumentation {
    private static final String TAG = "EvilInstrumentation";

    private final Instrumentation mBase;

    public EvilInstrumentation(Instrumentation base) {
        this.mBase = base;
    }

    /**
     * 一定要在newActivity方法中判断targetIntent是否为空，空则说明要启动的是一个AndroidManifest中声明的Activity，将走正常的流程
     */
    @Override
    public Activity newActivity(ClassLoader classLoader, String className, Intent intent) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        Intent targetIntent = intent.getParcelableExtra(AMSHookHelper.EXTRA_TARGET_INTENT);
        if (targetIntent == null) {
            return mBase.newActivity(classLoader, className, intent);
        }
        String newClassName = targetIntent.getComponent().getClassName();
        return mBase.newActivity(classLoader, newClassName, targetIntent);
    }
}
