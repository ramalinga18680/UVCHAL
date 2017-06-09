package com.droidlogic.app;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

public class HdrManager {
    private static final String TAG                 = "HdrManager";

    private static final String KEY_HDR_MODE        = "key_hdr_mode";
    private static final String SYSF_HDR_MODE       = "/sys/module/am_vecm/parameters/hdr_mode";

    public static final int MODE_OFF = 0;
    public static final int MODE_ON = 1;
    public static final int MODE_AUTO = 2;

    private Context mContext;
    private SystemControlManager mSystenControl;

    public HdrManager(Context context){
        mContext = context;
        mSystenControl = new SystemControlManager(context);
    }

    public void initHdrMode() {
        switch (Settings.System.getInt(mContext.getContentResolver(), KEY_HDR_MODE, MODE_AUTO)) {
            case MODE_OFF:
                mSystenControl.writeSysFs(SYSF_HDR_MODE, Integer.toString(MODE_OFF));
                break;
            case MODE_ON:
                mSystenControl.writeSysFs(SYSF_HDR_MODE, Integer.toString(MODE_ON));
                break;
            case MODE_AUTO:
                mSystenControl.writeSysFs(SYSF_HDR_MODE, Integer.toString(MODE_AUTO));
                break;
            default:
                mSystenControl.writeSysFs(SYSF_HDR_MODE, Integer.toString(MODE_AUTO));
                break;
        }
    }

    public int getHdrMode() {
        switch (Settings.System.getInt(mContext.getContentResolver(), KEY_HDR_MODE, MODE_AUTO)) {
            case 0:
                return MODE_OFF;
            case 1:
                return MODE_ON;
            case 2:
                return MODE_AUTO;
            default:
                return MODE_AUTO;
        }
    }

    public void setHdrMode(int mode) {
        switch (mode) {
            case MODE_OFF:
                mSystenControl.writeSysFs(SYSF_HDR_MODE, Integer.toString(MODE_OFF));
                Settings.System.putInt(mContext.getContentResolver(), KEY_HDR_MODE, MODE_OFF);
                break;
            case MODE_ON:
                mSystenControl.writeSysFs(SYSF_HDR_MODE, Integer.toString(MODE_ON));
                Settings.System.putInt(mContext.getContentResolver(), KEY_HDR_MODE, MODE_ON);
                break;
            case MODE_AUTO:
                mSystenControl.writeSysFs(SYSF_HDR_MODE, Integer.toString(MODE_AUTO));
                Settings.System.putInt(mContext.getContentResolver(), KEY_HDR_MODE, MODE_AUTO);
                break;
            default:
                mSystenControl.writeSysFs(SYSF_HDR_MODE, Integer.toString(MODE_AUTO));
                Settings.System.putInt(mContext.getContentResolver(), KEY_HDR_MODE, MODE_AUTO);
                break;
        }
    }
}
