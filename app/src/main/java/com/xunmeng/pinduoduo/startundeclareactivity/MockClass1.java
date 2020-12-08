package com.xunmeng.pinduoduo.startundeclareactivity;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author 茗烟
 * @date 2020/11/22
 */
public class MockClass1 implements InvocationHandler {
    private static final String TAG = "MockClass";
    private final Object mBase;

    public MockClass1(Object base) {
        mBase = base;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Log.i(TAG, "method name == " + methodName);
        if ("startActivity".equals(methodName)) {
            //取目标activity包名
            Intent targetIntent = null;
            int index = 0;
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Intent) {
                    index = i;
                    targetIntent = (Intent) args[i];
                    break;
                }
            }
            if (targetIntent == null) {
                return null;
            }
            //替换操作
            Intent newIntent = new Intent();
            String stubPackage = targetIntent.getComponent().getPackageName();
            ComponentName componentName = new ComponentName(stubPackage, StubActivity.class.getName());
            newIntent.setComponent(componentName);
            //把原始要启动的TargetActivity信息先存起来
            newIntent.putExtra(AMSHookHelper.EXTRA_TARGET_INTENT, targetIntent);
            args[index] = newIntent;
        }
        return method.invoke(mBase, args);
    }
}
