package com.droidlogic.PPPoE;

import java.util.Timer;
import java.util.TimerTask;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.content.SharedPreferences;
import com.amlogic.pppoe.PppoeOperation;
import com.droidlogic.pppoe.PppoeStateTracker;
import com.droidlogic.pppoe.PppoeService;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemProperties;


public class PppoeBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "PppoeBroadcastReceiver";
    private static final String ACTION_BOOT_COMPLETED =
        "android.intent.action.BOOT_COMPLETED";
    public static final String ETH_STATE_CHANGED_ACTION =
        "android.net.ethernet.ETH_STATE_CHANGED";
    public static final int DELAY_TIME = 2000;
    private Handler mHandler = null;
    private boolean mAutoDialFlag = false;
    private String mInterfaceSelected = null;
    private String mUserName = null;
    private String mPassword = null;
    private PppoeOperation operation = null;
    private static boolean mFirstAutoDialDone = false;
    private Timer mMandatoryDialTimer = null;
    public static final int TYPE_PPPOE = 0;
    private static final String PPPOE_SERVICE = "pppoe";
    void StartPppoeService(Context context)
    {
        boolean needPppoe = true;
        if (needPppoe) {
            final PppoeStateTracker pppoetracker;
            HandlerThread handlerThread = new HandlerThread("myHandlerThread");
            handlerThread.start();
            Handler handler = new Handler(handlerThread.getLooper());
            try {
                pppoetracker = new PppoeStateTracker(handlerThread.getLooper(),TYPE_PPPOE, PPPOE_SERVICE);
                PppoeService pppoe = new PppoeService(context,pppoetracker);
                Log.e(TAG, "start add service pppoe");
                try {
                    Class.forName("android.os.ServiceManager").getMethod("addService", new Class[] {String.class, IBinder.class })
                    .invoke(null, new Object[] { PPPOE_SERVICE, pppoe });
                }catch (Exception ex) {
                    Log.e(TAG, "addService " + PPPOE_SERVICE + " fail:" + ex);
                }
                Log.d(TAG, "end add service pppoe");
                pppoetracker.startMonitoring(context,handler);
                //  if (config.isDefault()) {
                    pppoetracker.reconnect();
                //  }
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Problem creating TYPE_PPPOE tracker: " + e);
            }
        }
    }

    private String getNetworkInterfaceSelected(Context context)
    {
        SharedPreferences sharedata = context.getSharedPreferences("inputdata", 0);
        if (sharedata != null && sharedata.getAll().size() > 0)
        {
            return sharedata.getString(PppoeConfigDialog.INFO_NETWORK_INTERFACE_SELECTED, null);
        }
        return null;
    }


    private boolean getAutoDialFlag(Context context)
    {
        SharedPreferences sharedata = context.getSharedPreferences("inputdata", 0);
        if (sharedata != null && sharedata.getAll().size() > 0)
        {
            return sharedata.getBoolean(PppoeConfigDialog.INFO_AUTO_DIAL_FLAG, false);
        }
        return false;
    }

    private String getUserName(Context context)
    {
        SharedPreferences sharedata = context.getSharedPreferences("inputdata", 0);
        if (sharedata != null && sharedata.getAll().size() > 0)
        {
            return sharedata.getString(PppoeConfigDialog.INFO_USERNAME, null);
        }
        return null;
    }

    private String getPassword(Context context)
    {
        SharedPreferences sharedata = context.getSharedPreferences("inputdata", 0);
        if (sharedata != null && sharedata.getAll().size() > 0)
        {
            return sharedata.getString(PppoeConfigDialog.INFO_PASSWORD, null);
        }
        return null;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        mInterfaceSelected = getNetworkInterfaceSelected(context);
        mAutoDialFlag = getAutoDialFlag(context);

        mUserName = getUserName(context);
        mPassword = getPassword(context);
        if (ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            StartPppoeService(context);
            context.startService(new Intent(context,
                 MyPppoeService.class));
            mFirstAutoDialDone = true;
        }

        if (null == mInterfaceSelected
            || !mAutoDialFlag
            || null == mUserName
            || null == mPassword) {
            mFirstAutoDialDone = false;
            return;
        }

        if (mHandler == null) {
            mHandler = new PppoeHandler();
        }
        if (operation == null) {
            operation = new PppoeOperation();
        }
        Log.d(TAG , "onReceive :" +intent.getAction());
        if ("com.droidlogic.linkchange".equals(action) || mFirstAutoDialDone) {
            if (mFirstAutoDialDone) {
                mHandler.sendEmptyMessageDelayed(PPPoEActivity.MSG_MANDATORY_DIAL, DELAY_TIME);
                mFirstAutoDialDone = false;
            } else {
                if (!mInterfaceSelected.startsWith("eth") )
                    return;
                //Timeout after 5 seconds
                mHandler.sendEmptyMessageDelayed(PPPoEActivity.MSG_START_DIAL, DELAY_TIME);
            }
        }
    }


    void set_pppoe_running_flag()
    {
        SystemProperties.set(PppoeConfigDialog.ethernet_dhcp_repeat_flag, "disabled");

        SystemProperties.set(PppoeConfigDialog.pppoe_running_flag, "100");
        String propVal = SystemProperties.get(PppoeConfigDialog.pppoe_running_flag);
        int n = 0;
        if (propVal.length() != 0) {
            try {
                n = Integer.parseInt(propVal);
                Log.d(TAG, "set_pppoe_running_flag as " + n);
            } catch (NumberFormatException e) {}
        } else {
            Log.d(TAG, "failed to set_pppoe_running_flag");
        }

        return;
    }

    private class PppoeHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case PPPoEActivity.MSG_MANDATORY_DIAL:
                    Log.d(TAG, "handleMessage: MSG_MANDATORY_DIAL");
                    set_pppoe_running_flag();
                    operation.terminate();
                    operation.disconnect();
                    mHandler.sendEmptyMessageDelayed(PPPoEActivity.MSG_START_DIAL, DELAY_TIME);
                break;

                case PPPoEActivity.MSG_START_DIAL:
                    Log.d(TAG, "handleMessage: MSG_START_DIAL");
                    set_pppoe_running_flag();
                    operation.connect(mInterfaceSelected, mUserName, mPassword);
                break;

                default:
                    Log.d(TAG, "handleMessage: " + msg.what);
                break;
            }
        }
    }
}

