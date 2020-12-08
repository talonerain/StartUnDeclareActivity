package com.xunmeng.pinduoduo.startundeclareactivity;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * @author 茗烟
 * @date 2020/11/22
 */
public class MockClass2 implements Handler.Callback {
    private final static String TAG = "MockClass2";
    Handler mBase;

    public MockClass2(Handler base) {
        this.mBase = base;
    }

    /**
     * intent.setComponent？这样set有什么意义？
     *
     * @param msg
     * @return
     */
    @Override
    public boolean handleMessage(@NonNull Message msg) {
        Log.i(TAG, "msg what == " + msg.what);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            //消息值为100的消息是startActivity
            if (msg.what == 100) {
                Object object = msg.obj;
                Intent intent = (Intent) RefInvoke.getFieldObject(object.getClass().getName(), object, "intent");
                if (intent != null) {
                    Intent targetIntent = intent.getParcelableExtra(AMSHookHelper.EXTRA_TARGET_INTENT);
                    intent.setComponent(targetIntent.getComponent());
                }
            }
        } else {
            if (msg.what == 159) {
                Log.i(TAG, "msg.what == 159");
                Object object = msg.obj;
                Intent intent = (Intent) RefInvoke.getFieldObject(object.getClass().getName(), object, "intent");
                if (intent != null) {
                    Intent targetIntent = intent.getParcelableExtra(AMSHookHelper.EXTRA_TARGET_INTENT);
                    intent.setComponent(targetIntent.getComponent());
                }
            }
        }

        mBase.handleMessage(msg);
        return true;
    }
}
