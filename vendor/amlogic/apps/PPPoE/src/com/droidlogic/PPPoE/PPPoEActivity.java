package com.droidlogic.PPPoE;

import com.droidlogic.pppoe.PppoeManager;
import com.droidlogic.pppoe.IPppoeManager;
import com.droidlogic.pppoe.PppoeStateTracker;
import com.droidlogic.pppoe.PppoeDevInfo;
import com.droidlogic.app.SystemControlManager;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import android.view.Gravity;
import android.provider.Settings;
import android.os.ServiceManager;


public class PPPoEActivity extends Activity {
    private final String TAG = "PPPoEActivity";
    private PppoeConfigDialog mPppoeConfigDialog;
    private SystemControlManager mSystemControlManager = null;
    public static final int MSG_START_DIAL = 0xabcd0000;
    public static final int MSG_MANDATORY_DIAL = 0xabcd0010;
    public static final int MSG_CONNECT_TIMEOUT = 0xabcd0020;
    public static final int MSG_DISCONNECT_TIMEOUT = 0xabcd0040;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Create PppoeConfigDialog");
        mSystemControlManager = new SystemControlManager(this);
        String eth_link = mSystemControlManager.readSysFs("/sys/class/ethernet/linkspeed");
        if (eth_link.contains("unlink")) {
            Toast toast = Toast.makeText(this,this.getResources().getString(R.string.please_insert_the_cable),Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            finish();
        } else {
            setContentView(R.layout.main);
            mPppoeConfigDialog = new PppoeConfigDialog(this);
            ConnectivityManager cm = (ConnectivityManager)this.getSystemService( Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null) {
               Log.d(TAG, info.toString());
            }
            if (mPppoeConfigDialog != null) {
                Log.d(TAG, "Show PppoeConfigDialog");
                mPppoeConfigDialog.show();
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPppoeConfigDialog != null)
            mPppoeConfigDialog.dismiss();
    }
    @Override
    public void finish() {
        super.finish();
        mPppoeConfigDialog = null;
    }

}
