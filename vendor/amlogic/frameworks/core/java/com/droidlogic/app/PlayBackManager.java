package com.droidlogic.app;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

public class PlayBackManager {
    private static final String TAG             = "PlayBackManager";
    private static final boolean DEBUG          = false;

    private static final String KEY_HDMI_SELFADAPTION = "key_hdmi_selfadaption";
    private static final String SYSF_HDMI_SELFADAPTION = "/sys/class/tv/policy_fr_auto";

    private Context mContext;
    private SystemControlManager mSystenControl;

    public PlayBackManager(Context context){
        mContext = context;
        mSystenControl = new SystemControlManager(context);
    }

    public void initHdmiSelfadaption () {
        if (Settings.System.getInt(mContext.getContentResolver(), KEY_HDMI_SELFADAPTION, 0) == 1) {
            mSystenControl.writeSysFs(SYSF_HDMI_SELFADAPTION, "1");
        } else {
            mSystenControl.writeSysFs(SYSF_HDMI_SELFADAPTION, "0");
        }
    }

    public boolean isHdmiSelfadaptionOn() {
        return Settings.System.getInt(mContext.getContentResolver(), KEY_HDMI_SELFADAPTION, 0) == 1 ? true : false;
    }

    public void setHdmiSelfadaption(boolean on) {
        if (on) {
            mSystenControl.writeSysFs(SYSF_HDMI_SELFADAPTION, "1");
            Settings.System.putInt(mContext.getContentResolver(), KEY_HDMI_SELFADAPTION, 1);
        } else {
            mSystenControl.writeSysFs(SYSF_HDMI_SELFADAPTION, "0");
            Settings.System.putInt(mContext.getContentResolver(), KEY_HDMI_SELFADAPTION, 0);
        }
    }
}
