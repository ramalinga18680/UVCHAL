package com.droidlogic.app;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import java.io.File;
import java.util.Locale;
import java.lang.reflect.Method;


public class HdmiCecManager {
    private static final boolean DEBUG = true;
    private static final String TAG = "HdmiCecManager";

    public static final String CEC_DEVICE_FILE = "/sys/devices/virtual/switch/lang_config/state";
    public static final String CEC_SYS = "/sys/class/amhdmitx/amhdmitx0/cec_config";
    public static final String CEC_SUPPORT = "/sys/class/amhdmitx/amhdmitx0/tv_support_cec";
    public static final String CEC_PROP = "ubootenv.var.cecconfig";
    public static final String CEC_TAG = "cec0x";
    public static final String CEC_0   = "cec0x0";

    public static final int FUN_CEC = 0x00;
    public static final int FUN_ONE_KEY_POWER_OFF = 0x01;
    public static final int FUN_ONE_KEY_PLAY = 0x02;
    public static final int FUN_AUTO_CHANGE_LANGUAGE = 0x03;
    public static final int FUN_AUTO_POWER_ON = 0x04;

    public static final int MASK_FUN_CEC = 0x01;                   // bit 0
    public static final int MASK_ONE_KEY_PLAY = 0x02;              // bit 1
    public static final int MASK_ONE_KEY_STANDBY = 0x04;           // bit 2
    public static final int MASK_AUTO_POWER_ON = 0x08;
    public static final int MASK_AUTO_CHANGE_LANGUAGE = 0x20;      // bit 5
    public static final int MASK_ALL = 0x2f;                       // all mask

    public static final boolean FUN_OPEN = true;
    public static final boolean FUN_CLOSE = false;

    private static String[] mCecLanguageList;
    private static String[] mLanguageList;
    private static String[] mCountryList;

    private Context mContext;
    private SystemControlManager mSystemControlManager;


    public HdmiCecManager(Context context) {
        mContext = context;
        mSystemControlManager = new SystemControlManager(context);
    }

    public void initCec() {
        if (mSystemControlManager != null) {
            String fun = mSystemControlManager.readSysFs(CEC_SYS);
            if (fun != null) {
                Log.d(TAG, "set cec fun = " + fun);
                mSystemControlManager.writeSysFs(CEC_SYS, fun);
            }
        }
    }

    public boolean remoteSupportCec() {
        if ((new File(CEC_SUPPORT).exists())) {
            return !mSystemControlManager.readSysFs(CEC_SUPPORT).equals("0");
        }
        return true;
    }

    public int[] getBinaryArray(String binaryString) {
        int[] tmp = new int[4];
        for (int i = 0; i < binaryString.length(); i++) {
            String tmpString = String.valueOf(binaryString.charAt(i));
            tmp[i] = Integer.parseInt(tmpString);
        }
        return tmp;
    }

    public String getCurConfig() {
        return mSystemControlManager.readSysFs(CEC_SYS);
    }

    public void setCecEnv(int cfg) {
        int cec_config = cfg & MASK_ALL;
        String str = CEC_TAG + Integer.toHexString(cec_config);
        Log.d(TAG, "save env:" + str);
        mSystemControlManager.setBootenv(CEC_PROP, str);
    }

    public String getCurLanguage() {
        return mSystemControlManager.readSysFs(CEC_DEVICE_FILE);
    }

    public void setLanguageList(String[] cecLanguageList, String[] languageList, String[] countryList) {
        mCecLanguageList = cecLanguageList;
        mLanguageList = languageList;
        mCountryList = countryList;
    }

    public boolean isChangeLanguageOpen() {
        boolean exist = new File(CEC_SYS).exists();
        if (exist) {
            String cec_cfg = mSystemControlManager.readSysFs(CEC_SYS);
            if (cec_cfg == null)
                return false;
            int value = Integer.valueOf(cec_cfg.substring(2, cec_cfg.length()), 16);
            Log.d(TAG, "cec_cfg:" + cec_cfg + ", value:" + value);
            return (value & MASK_AUTO_CHANGE_LANGUAGE) != 0;
        } else {
            return false;
        }
    }

    public void doUpdateCECLanguage(String curLanguage) {
        int i = -1;
        for (int j = 0; j < mCecLanguageList.length; j++) {
            if (curLanguage != null && curLanguage.trim().equals(mCecLanguageList[j])) {
                i = j;
                break;
            }
        }
        if (i >= 0) {
            String able = mContext.getResources().getConfiguration().locale.getCountry();
            if (able.equals(mCountryList[i])) {
                if (DEBUG) Log.d(TAG, "no need to change language");
                return;
            } else {
                Locale l = new Locale(mLanguageList[i], mCountryList[i]);
                if (DEBUG) Log.d(TAG, "change the language right now !!!");
                updateLanguage(l);
            }
        } else {
            Log.d(TAG, "the language code is not support right now !!!");
        }
    }

    public void updateLanguage(Locale locale) {
        try {
            Object objIActMag;
            Class clzIActMag = Class.forName("android.app.IActivityManager");
            Class clzActMagNative = Class.forName("android.app.ActivityManagerNative");
            Method mtdActMagNative$getDefault = clzActMagNative.getDeclaredMethod("getDefault");

            objIActMag = mtdActMagNative$getDefault.invoke(clzActMagNative);
            Method mtdIActMag$getConfiguration = clzIActMag.getDeclaredMethod("getConfiguration");
            Configuration config = (Configuration) mtdIActMag$getConfiguration.invoke(objIActMag);
            config.locale = locale;

            Class[] clzParams = { Configuration.class };
            Method mtdIActMag$updateConfiguration = clzIActMag.getDeclaredMethod("updateConfiguration", clzParams);
            mtdIActMag$updateConfiguration.invoke(objIActMag, config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCecSysfsValue(int fun, boolean isOn) {
        String cec_config = mSystemControlManager.getBootenv(CEC_PROP, CEC_0);
        String writeConfig, s;
        // get rid of '0x' prefix
        int cec_cfg_value = Integer.valueOf(cec_config.substring(5, cec_config.length()), 16);
        Log.d(TAG, "cec config str:" + cec_config + ", value:" + cec_cfg_value);
        if (fun != FUN_CEC) {
            if (CEC_0.equals(cec_config)) {
                return;
            }
        }

        if (fun == FUN_CEC) {
            if (isOn) {
                mSystemControlManager.setBootenv(CEC_PROP, CEC_TAG + "2f");
                mSystemControlManager.writeSysFs(CEC_SYS, "2f");
            } else {
                mSystemControlManager.setBootenv(CEC_PROP, CEC_0);
                mSystemControlManager.writeSysFs(CEC_SYS, "0");
            }
            return ;
        } else if (fun == FUN_ONE_KEY_PLAY) {
            if (isOn) {
                cec_cfg_value |= MASK_ONE_KEY_PLAY;
            } else {
                cec_cfg_value &= ~MASK_ONE_KEY_PLAY;
            }
        } else if (fun == FUN_ONE_KEY_POWER_OFF) {
            if (isOn) {
                cec_cfg_value |= MASK_ONE_KEY_STANDBY;
            } else {
                cec_cfg_value &= ~MASK_ONE_KEY_STANDBY;
            }
        } else if (fun == FUN_AUTO_POWER_ON) {
            if (isOn) {
                cec_cfg_value |= MASK_AUTO_POWER_ON;
            } else {
                cec_cfg_value &= ~MASK_AUTO_POWER_ON;
            }
        }else if(fun == FUN_AUTO_CHANGE_LANGUAGE){
            if (isOn) {
                cec_cfg_value |= MASK_AUTO_CHANGE_LANGUAGE;
            } else {
                cec_cfg_value &= ~MASK_AUTO_CHANGE_LANGUAGE;
            }
        }
        writeConfig = CEC_TAG + Integer.toHexString(cec_cfg_value);
        mSystemControlManager.setBootenv(CEC_PROP, writeConfig);
        s = writeConfig.substring(3, writeConfig.length());
        mSystemControlManager.writeSysFs(CEC_SYS, s);
        Log.d(TAG, "==== cec set config : " + writeConfig);
    }
}

