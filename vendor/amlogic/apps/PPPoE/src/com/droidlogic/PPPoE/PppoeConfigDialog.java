package com.droidlogic.PPPoE;

import java.util.Timer;
import java.util.TimerTask;
import android.os.Message;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemProperties;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.ArrayList;
import java.net.SocketException;
import com.droidlogic.pppoe.PppoeManager;
import com.droidlogic.pppoe.PppoeStateTracker;
import com.droidlogic.app.SystemControlManager;
import com.amlogic.pppoe.PppoeOperation;

public class PppoeConfigDialog extends AlertDialog implements DialogInterface.OnClickListener, TextWatcher
{
    private static final String PPPOE_DIAL_RESULT_ACTION =
            "PppoeConfigDialog.PPPOE_DIAL_RESULT";
    public static final int EVENT_HW_PHYCONNECTED = 5;
    public static final int EVENT_HW_DISCONNECTED = 4;
    private static final int PPPOE_STATE_UNDEFINED = 0;
    private static final int PPPOE_STATE_DISCONNECTED = 1;
    private static final int PPPOE_STATE_CONNECTING = 2;
    private static final int PPPOE_STATE_DISCONNECTING = 3;
    private static final int PPPOE_STATE_CONNECT_FAILED = 4;
    private static final int PPPOE_STATE_CONNECTED = 5;
    private static final int SHOWWAITDIALOG = 6;
    private static final int CONNECT = 7;
    private static final int DISCONNECT = 8;
    public static final String ETH_STATE_CHANGED_ACTION =
            "android.net.ethernet.ETH_STATE_CHANGED";
    public static final String EXTRA_ETH_STATE = "eth_state";
    private static final String EXTRA_NAME_STATUS = "status";
    private static final String EXTRA_NAME_ERR_CODE = "err_code";

    public static final String INFO_USERNAME = "name";
    public static final String INFO_PASSWORD = "passwd";
    public static final String INFO_NETWORK_INTERFACE_SELECTED = "network_if_selected";
    public static final String INFO_AUTO_DIAL_FLAG = "auto_dial_flag";

    private final String TAG = "PppoeConfigDialog";
    private View mView;
    private EditText mPppoeName;
    private EditText mPppoePasswd;
    private String user_name = null;
    private String user_passwd = null;
    private ProgressDialog waitDialog = null;
    private PppoeOperation operation = null;
    Context context = null;
    private AlertDialog alertDia = null;
    private PppoeReceiver pppoeReceiver = null;
    private CheckBox mCbAutoDial;
    private SystemControlManager sw = null;

    Timer connect_timer = null;
    Timer disconnect_timer = null;

    private int pppoe_state = PPPOE_STATE_UNDEFINED;
    private boolean isDialogOfDisconnect = false;
    private static final String eth_device_sysfs = "/sys/class/ethernet/linkspeed";
    public static final String pppoe_running_flag = "net.pppoe.running";
    public static final String ethernet_dhcp_repeat_flag = "net.dhcp.repeat";

    private TextView mNetworkInterfaces;
    private Spinner spinner;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> network_if_list;
    private String mNetIfSelected = null;
    private String tmp_name, tmp_passwd;
    private boolean mAtuoDialFlag = false;
    private Button mDial = null;
    private Handler mHandler = null;
    private final Handler mTextViewChangedHandler;
    private ConnectThread mHandlerThread;
    private boolean is_pppoe_running()
    {
        String propVal = SystemProperties.get(pppoe_running_flag);
        int n = 0;
        if (propVal.length() != 0) {
            try {
                n = Integer.parseInt(propVal);
            } catch (NumberFormatException e) {}
        } else {
            Log.d(TAG, "net.pppoe.running not FOUND");
        }
        return (n != 0);
    }

    private void set_pppoe_running_flag()
    {
        SystemProperties.set(ethernet_dhcp_repeat_flag, "disabled");
        SystemProperties.set(pppoe_running_flag, "100");
        String propVal = SystemProperties.get(pppoe_running_flag);
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

    private void clear_pppoe_running_flag()
    {
        SystemProperties.set(ethernet_dhcp_repeat_flag, "enabled");
        SystemProperties.set(pppoe_running_flag, "0");
        String propVal = SystemProperties.get(pppoe_running_flag);
        int n = 0;
        if (propVal.length() != 0) {
            try {
                n = Integer.parseInt(propVal);
                Log.d(TAG, "clear_pppoe_running_flag as " + n);
            } catch (NumberFormatException e) {}
        } else {
            Log.d(TAG, "failed to clear_pppoe_running_flag");
        }
        return;
    }


    public PppoeConfigDialog(Context context)
    {
        super(context);
        this.context = context;
        mHandlerThread = new ConnectThread();
        mHandlerThread.start();
        mHandler = new PppoeDialogHandler();
        sw = new SystemControlManager(context);
        mTextViewChangedHandler = new Handler();
        operation = new PppoeOperation();
        buildDialog(context);
        waitDialog = new ProgressDialog(context);
        pppoeReceiver = new PppoeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PppoeManager.PPPOE_STATE_CHANGED_ACTION);
        filter.addAction(ETH_STATE_CHANGED_ACTION);
        Log.d(TAG, "registerReceiver pppoeReceiver");
        context.registerReceiver(pppoeReceiver, filter);
    }

    class SpinnerSelectedListener implements OnItemSelectedListener{
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                long arg3) {
            mNetIfSelected = network_if_list.get(arg2);
            Log.d(TAG, "interface selected: " + mNetIfSelected);
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private boolean isEthDeviceAdded() {
        String str = sw.readSysFs(eth_device_sysfs);
        if (str == null)
            return false ;
        if (str.contains("unlink")) {
            return false;
        } else {
            return true;
        }
    }

    private void buildDialog(Context context)
    {
        String eth_name = null;
        String wifi_name = null;
        Log.d(TAG, "buildDialog");
        setTitle(R.string.pppoe_config_title);
        this.setView(mView = getLayoutInflater().inflate(R.layout.pppoe_configure, null));
        mPppoeName = (EditText)mView.findViewById(R.id.pppoe_name_edit);
        mPppoePasswd = (EditText)mView.findViewById(R.id.pppoe_passwd_edit);
        mCbAutoDial = (CheckBox)mView.findViewById(R.id.auto_dial_checkbox);
        mCbAutoDial.setVisibility(View.VISIBLE);
        mCbAutoDial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
                        //saveAutoDialFlag();
                    if (isChecked) {
                        Log.d(TAG, "AutoDial Selected");
                    }else{
                        Log.d(TAG, "AutoDial Unselected");
                    }
                }
            });
        mPppoeName.setEnabled(true);
        mPppoeName.addTextChangedListener(this);
        mPppoePasswd.setEnabled(true);
        mPppoePasswd.addTextChangedListener(this);
        this.setInverseBackgroundForced(true);
        network_if_list=new ArrayList<String>();
        try {
            for (Enumeration<NetworkInterface> list = NetworkInterface.getNetworkInterfaces(); list.hasMoreElements();)
            {
                NetworkInterface netIf = list.nextElement();
                if (netIf.isUp() &&
                    !netIf.isLoopback() &&
                    !netIf.isPointToPoint() &&
                    !netIf.getDisplayName().startsWith("sit") &&
                    !netIf.getDisplayName().startsWith("ip6tnl") &&
                    !netIf.getDisplayName().startsWith("p2p")) {
                        Log.d(TAG, "network_interface: " + netIf.getDisplayName());
                        Log.d(TAG, "Wire Intert: " + isEthDeviceAdded());
                        if (netIf.getDisplayName().startsWith("eth") && isEthDeviceAdded())
                            eth_name = netIf.getDisplayName();
                        else if (netIf.getDisplayName().startsWith("wlan"))
                            wifi_name = netIf.getDisplayName();
                    }
            }
        } catch (SocketException e) {
            return;
        }
        getInfoData();
        Log.d(TAG, "History network interface " + mNetIfSelected);
        if (mNetIfSelected != null) {
            if (eth_name != null && mNetIfSelected.equals(eth_name)) {
                network_if_list.add(eth_name);
                eth_name = null;
            }
            else if (wifi_name != null && mNetIfSelected.equals(wifi_name)) {
                network_if_list.add(wifi_name);
                wifi_name = null;
            }
        }
        if (eth_name != null)
            network_if_list.add(eth_name);
        if (wifi_name != null)
            network_if_list.add(wifi_name);
        mNetworkInterfaces = (TextView) mView.findViewById(R.id.network_interface_list_text);
        spinner = (Spinner) mView.findViewById(R.id.network_inteface_list_spinner);
        adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item,network_if_list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new SpinnerSelectedListener());
        spinner.setVisibility(View.VISIBLE);
        if (connectStatus() != PppoeOperation.PPP_STATUS_CONNECTED)
        {
            if (connectStatus() == PppoeOperation.PPP_STATUS_CONNECTING) {
                isDialogOfDisconnect = false;
                showWaitDialog(R.string.pppoe_dial_waiting_msg);
            } else {
                isDialogOfDisconnect = false;
                this.setButton(BUTTON_POSITIVE, context.getText(R.string.pppoe_dial), this);
            }
        }
        else {
            Log.d(TAG, "connectStatus is CONNECTED");
            isDialogOfDisconnect = true;
            //hide AutoDial CheckBox
            mCbAutoDial.setVisibility(View.GONE);
            //hide network interfaces
            mNetworkInterfaces.setVisibility(View.GONE);
            spinner.setVisibility(View.GONE);
            //hide Username
            mView.findViewById(R.id.user_pppoe_text).setVisibility(View.GONE);
            mPppoeName.setVisibility(View.GONE);
            //hide Password
            mView.findViewById(R.id.passwd_pppoe_text).setVisibility(View.GONE);
            mPppoePasswd.setVisibility(View.GONE);
            this.setButton(BUTTON_POSITIVE, context.getText(R.string.pppoe_disconnect), this);
            /*
            if (!is_pppoe_running()) {
                showAlertDialog(context.getResources().getString(R.string.pppoe_ppp_interface_occupied));
                return;
            }
            */
        }
        this.setButton(BUTTON_NEGATIVE, context.getText(R.string.menu_cancel), this);
        if (user_name != null
          && user_passwd != null
          && user_name.equals("")== false) {
            mPppoeName.setText(user_name);
            mPppoePasswd.setText(user_passwd);
        } else {
            mPppoeName.setText("");
            mPppoePasswd.setText("");
        }
        checkDialEnable();
        mCbAutoDial.setChecked(mAtuoDialFlag);
    }

    private void checkDialEnable() {
        Button mDial = this.getButton(android.content.DialogInterface.BUTTON_POSITIVE);
        if (mDial == null)
            return;
        if (mPppoeName.getVisibility() != View.VISIBLE || mPppoePasswd.getVisibility() != View.VISIBLE)
            return;
        if (!TextUtils.isEmpty(mPppoeName.getText().toString()) && !TextUtils.isEmpty(mPppoePasswd.getText().toString()))
            mDial.setEnabled(true);
        else
            mDial.setEnabled(false);
    }

    @Override
    public void show()
    {
        Log.d(TAG, "show");
        super.show();
        if (connectStatus() != PppoeOperation.PPP_STATUS_CONNECTED) {
            mDial = this.getButton(android.content.DialogInterface.BUTTON_POSITIVE);
            if (mDial != null && 0 == network_if_list.size())
                mDial.setEnabled(false);
        }
    }
    @Override
    public void dismiss() {
        Log.d(TAG, "dismiss");
        super.dismiss();
    }
    class ConnectThread extends Thread{
        public Handler thander;
        @Override
        public void run() {
            Looper.prepare();
            thander = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case CONNECT:
                            Log.d(TAG, "handleMessage: MSG_START_DIAL");
                            set_pppoe_running_flag();
                            operation.connect(mNetIfSelected, tmp_name, tmp_passwd);
                            break;
                        case DISCONNECT:
                            Log.d(TAG, "handleMessage: MSG_MANDATORY_DIAL");
                            set_pppoe_running_flag();
                            operation.terminate();
                            operation.disconnect();
                            break;
                        default:
                            Log.d(TAG, "handleMessage: " + msg.what);
                        break;
                    }
                }
            };
            Looper.loop();
        }
    }

    private class PppoeDialogHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PPPoEActivity.MSG_DISCONNECT_TIMEOUT:
                    waitDialog.cancel();
                    showAlertDialog(context.getResources().getString(R.string.pppoe_disconnect_failed));
                    pppoe_state = PPPOE_STATE_DISCONNECTED;
                    clear_pppoe_running_flag();
                    SystemProperties.set("net.pppoe.isConnected", "false");
                    break;
                case PPPoEActivity.MSG_CONNECT_TIMEOUT:
                    Log.d(TAG, "handleMessage: MSG_CONNECT_TIMEOUT");
                    pppoe_state = PPPOE_STATE_CONNECT_FAILED;
                    if (waitDialog != null)
                        waitDialog.cancel();
                    showAlertDialog(context.getResources().getString(R.string.pppoe_connect_failed));
                    SystemProperties.set("net.pppoe.isConnected", "false");
                    break;
                case SHOWWAITDIALOG:
                    int id = msg.getData().getInt("id");
                    waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    waitDialog.setTitle("");
                    waitDialog.setMessage(context.getResources().getString(id));
                    waitDialog.setIcon(null);
                    if (id == R.string.pppoe_dial_waiting_msg) {
                        waitDialog.setButton(android.content.DialogInterface.BUTTON_POSITIVE,context.getResources().getString(R.string.menu_cancel),cancelBtnClickListener);
                        pppoe_state = PPPOE_STATE_CONNECTING;
                        set_pppoe_running_flag();
                    }
                    waitDialog.setIndeterminate(false);
                    waitDialog.setCancelable(true);
                    waitDialog.show();
                    Button button = waitDialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE);
                    button.setFocusable(true);
                    button.setFocusableInTouchMode(true);
                    button.requestFocus();
                    button.requestFocusFromTouch();
                    break;
                default:
                    Log.d(TAG, "handleMessage: " + msg.what);
                    break;
            }
        }
    }

    void showWaitDialog(int id)
    {
        if (waitDialog == null) {
            return;
        }
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        Log.d(TAG, "Send SHOWWAITDIALOG");
        message.what = SHOWWAITDIALOG;
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    private void handleStopDial()
    {
        Log.d(TAG, "handleStopDial");
        pppoe_state = PPPOE_STATE_DISCONNECTING;
        disconnect_timer = new Timer();
        TimerTask check_task = new TimerTask()
        {
            public void run()
            {
                Message message = new Message();
                message.what = PPPoEActivity.MSG_DISCONNECT_TIMEOUT;
                Log.d(TAG, "Send MSG_DISCONNECT_TIMEOUT");
                mHandler.sendMessage(message);
            }
        };
        //Timeout after 50 seconds
        disconnect_timer.schedule(check_task, 50000);
        Message cmsgs = new Message();
        cmsgs.what = DISCONNECT;
        mHandlerThread.thander.sendMessage(cmsgs);
        showWaitDialog(R.string.pppoe_hangup_waiting_msg);
    }
    private void handleStartDial()
    {
        Log.d(TAG, "handleStartDial");
        tmp_name = mPppoeName.getText().toString();
        tmp_passwd = mPppoePasswd.getText().toString();
        if (!TextUtils.isEmpty(tmp_name) && !TextUtils.isEmpty(tmp_passwd))
        {
            saveInfoData();
            connect_timer = new Timer();
            TimerTask check_task = new TimerTask()
            {
                public void run()
                {
                    Message message = new Message();
                    Log.d(TAG, "Send MSG_CONNECT_TIMEOUT");
                    message.what = PPPoEActivity.MSG_CONNECT_TIMEOUT;
                    mHandler.sendMessage(message);
                }
            };
            connect_timer.schedule(check_task, 60000);
            Message cmsgs = new Message();
            cmsgs.what = CONNECT;
            mHandlerThread.thander.sendMessage(cmsgs);
            showWaitDialog(R.string.pppoe_dial_waiting_msg);
        }
    }

    private void saveInfoData()
    {
        SharedPreferences.Editor sharedata = context.getSharedPreferences("inputdata", 0).edit();
        sharedata.clear();
        sharedata.putString(INFO_USERNAME, mPppoeName.getText().toString());
        sharedata.putString(INFO_PASSWORD, mPppoePasswd.getText().toString());
        sharedata.putString(INFO_NETWORK_INTERFACE_SELECTED, mNetIfSelected);
        sharedata.putBoolean(INFO_AUTO_DIAL_FLAG, mCbAutoDial.isChecked());
        sharedata.commit();
    }

    private void getInfoData()
    {
        SharedPreferences sharedata = context.getSharedPreferences("inputdata", 0);
        if (sharedata != null && sharedata.getAll().size() > 0)
        {
            user_name = sharedata.getString(INFO_USERNAME, null);
            user_passwd = sharedata.getString(INFO_PASSWORD, null);
            mNetIfSelected = sharedata.getString(INFO_NETWORK_INTERFACE_SELECTED, null);
            mAtuoDialFlag = sharedata.getBoolean(INFO_AUTO_DIAL_FLAG, false);
        }
        else
        {
            user_name = null;
            user_passwd = null;
        }
    }

    private void saveAutoDialFlag()
    {
        SharedPreferences.Editor sharedata = context.getSharedPreferences("inputdata", 0).edit();
        //sharedata.clear();
        sharedata.putBoolean(INFO_AUTO_DIAL_FLAG, mCbAutoDial.isChecked());
        sharedata.commit();
    }

    private int connectStatus()
    {
        if (null == mNetIfSelected) {
            Log.d(TAG, "mNetIfSelected is null");
            return PppoeOperation.PPP_STATUS_DISCONNECTED;
        }
        return operation.status(mNetIfSelected);
    }


    private void showAlertDialog(final String msg)
    {
        Log.d(TAG, "showAlertDialog");
        AlertDialog.Builder ab = new AlertDialog.Builder(context);
        alertDia = ab.create();
        alertDia.setTitle(" ");
        alertDia.setMessage(msg);
        alertDia.setIcon(null);
        alertDia.setButton(android.content.DialogInterface.BUTTON_POSITIVE,context.getResources().getString(R.string.amlogic_ok),AlertClickListener);
        alertDia.setCancelable(true);
        alertDia.setInverseBackgroundForced(true);
        alertDia.show();
        Button button = alertDia.getButton(android.content.DialogInterface.BUTTON_POSITIVE);
        button.setFocusable(true);
        button.setFocusableInTouchMode(true);
        button.requestFocus();
        button.requestFocusFromTouch();
    }

    OnClickListener AlertClickListener = new OnClickListener()
    {
        public void onClick(DialogInterface dialog, int which)
        {
            switch (which) {
            case android.content.DialogInterface.BUTTON_POSITIVE:
                {
                    alertDia.cancel();
                    Log.d(TAG, "User click OK button, exit APK");
                    clearSelf();
                }
                break;
            case android.content.DialogInterface.BUTTON_NEGATIVE:
                break;
            default:
                break;
            }

        }
    };





    private void handleCancelDial()
    {
        operation.disconnect();
    }


    OnClickListener cancelBtnClickListener = new OnClickListener()
    {
        public void onClick(DialogInterface dialog, int which)
        {
            Log.d(TAG, "Cancel button is clicked");
            if (disconnect_timer != null)
                disconnect_timer.cancel();
            handleCancelDial();
            waitDialog.cancel();
            clearSelf();
        }
    };


    //@Override
    public void onClick(DialogInterface dialog, int which)
    {
        switch (which) {
        case BUTTON_POSITIVE:
            saveAutoDialFlag();
            if (isDialogOfDisconnect) {
                if (connectStatus() != PppoeOperation.PPP_STATUS_CONNECTED) {
                    Log.d(TAG, "DANGER: SHOULD CONNECTED when in DialogOfDisconnect");
                }
                handleStopDial();
            }
            else
                handleStartDial();
            break;
        case BUTTON_NEGATIVE:
            Log.d(TAG, "User click Discard button, exit APK");
            saveAutoDialFlag();
            clearSelf();
            break;
        default:
            break;
        }
    }

    public class PppoeReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            Log.d(TAG, "PppoeReceiver: " + action);

            if (action.equals(PppoeManager.PPPOE_STATE_CHANGED_ACTION)) {
                int event = intent.getIntExtra(PppoeManager.EXTRA_PPPOE_STATE,PppoeManager.PPPOE_STATE_UNKNOWN);
                Log.d(TAG, "event " + event);
                if (event == PppoeStateTracker.EVENT_CONNECTED) {
                    if (pppoe_state == PPPOE_STATE_CONNECTING) {
                        waitDialog.cancel();
                        connect_timer.cancel();
                    }
                    pppoe_state = PPPOE_STATE_CONNECTED;
                    showAlertDialog(context.getResources().getString(R.string.pppoe_connect_ok));
                    SystemProperties.set("net.pppoe.isConnected", "true");
                }
                if (event == PppoeStateTracker.EVENT_DISCONNECTED) {
                    if (pppoe_state == PPPOE_STATE_DISCONNECTING) {
                        waitDialog.cancel();
                        disconnect_timer.cancel();
                    }
                    clear_pppoe_running_flag();
                    pppoe_state = PPPOE_STATE_DISCONNECTED;
                    showAlertDialog(context.getResources().getString(R.string.pppoe_disconnect_ok));
                    SystemProperties.set("net.pppoe.isConnected", "false");
                }
                if (event == PppoeStateTracker.EVENT_CONNECT_FAILED) {
                    String ppp_err = intent.getStringExtra(PppoeManager.EXTRA_PPPOE_ERRCODE);
                    Log.d(TAG, "errcode: " + ppp_err);
                    if (pppoe_state == PPPOE_STATE_CONNECTING) {
                        waitDialog.cancel();
                        connect_timer.cancel();
                        clear_pppoe_running_flag();
                    }
                    pppoe_state = PPPOE_STATE_CONNECT_FAILED;
                    String reason = "";
                    if (ppp_err.equals("691"))
                        reason = context.getResources().getString(R.string.pppoe_connect_failed_auth);
                    else if (ppp_err.equals("650"))
                        reason = context.getResources().getString(R.string.pppoe_connect_failed_server_no_response);
                    showAlertDialog(context.getResources().getString(R.string.pppoe_connect_failed) + "\n" + reason);
                    SystemProperties.set("net.pppoe.isConnected", "false");
                }
            }
            else if (action.equals(ETH_STATE_CHANGED_ACTION)) {
                int event = intent.getIntExtra(EXTRA_ETH_STATE, -1);
                if (event == EVENT_HW_PHYCONNECTED &&
                    isEthDeviceAdded()) {
                    Log.d(TAG, "Ether.EVENT_HW_PHYCONNECTED");
                    network_if_list.add("eth0");
                    if (connectStatus() != PppoeOperation.PPP_STATUS_CONNECTED) {
                        if (mDial != null && network_if_list.size() > 0)
                            mDial.setEnabled(true);
                    }
                    adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item,network_if_list);
                    spinner.setAdapter(adapter);
                }
                else if (event == EVENT_HW_DISCONNECTED) {
                        Log.d(TAG, "Ether.EVENT_HW_DISCONNECTED");

                    network_if_list.remove("eth0");
                    if (connectStatus() != PppoeOperation.PPP_STATUS_CONNECTED) {
                        if (mDial != null && 0 == network_if_list.size())
                            mDial.setEnabled(false);
                    }
                    adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item,network_if_list);
                    spinner.setAdapter(adapter);
                }
            }
        }
    }

    private void clearSelf()
    {
        if (pppoeReceiver != null) {
            Log.d(TAG, "unregisterReceiver pppoeReceiver");
            context.unregisterReceiver(pppoeReceiver);
        }
        ((PPPoEActivity)context).onDestroy();
        ((PPPoEActivity)context).finish();

        /*
        Log.d(TAG, "killProcess");
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
        */
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(TAG, "User BACK operation, exit APK");
            clearSelf();
            return true;
        }
        Log.d(TAG, "keyCode " + keyCode + " is down, do nothing");
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void afterTextChanged(Editable s) {
        mTextViewChangedHandler.post(new Runnable() {
            public void run() {
                checkDialEnable();
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

}
