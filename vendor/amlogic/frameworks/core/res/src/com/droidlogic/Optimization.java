package com.droidlogic;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemProperties;
import android.util.Log;

import java.lang.Runnable;
import java.lang.Thread;

public class Optimization extends Service {
    private static String TAG = "Optimization";
    private Context mContext;

    static {
        System.loadLibrary("optimization");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (SystemProperties.getBoolean("ro.app.optimization", true)) {
            new Thread(runnable).start();
        }
        else {
            Log.i(TAG, "Optimization service not start");
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Runnable runnable = new Runnable() {
        public void run() {
            ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);

            while (true) {
                try {
                    ComponentName cn = am.getRunningTasks (1).get (0).topActivity;
                    String pkg = cn.getPackageName();
                    String cls = cn.getClassName();

                    nativeOptimization(pkg, cls);//bench match

                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private native int nativeOptimization(String pkg, String cls);
}
