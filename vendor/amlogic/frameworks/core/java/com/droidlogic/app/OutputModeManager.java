package com.droidlogic.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

public class OutputModeManager {
    private static final String TAG                         = "OutputModeManager";
    private static final boolean DEBUG                      = false;

    /**
     * The saved value for Outputmode auto-detection.
     * One integer
     * @hide
     */
    public static final String DISPLAY_OUTPUTMODE_AUTO      = "display_outputmode_auto";

    /**
     *  broadcast of the current HDMI output mode changed.
     */
    public static final String ACTION_HDMI_MODE_CHANGED     = "android.intent.action.HDMI_MODE_CHANGED";

    /**
     * Extra in {@link #ACTION_HDMI_MODE_CHANGED} indicating the mode:
     */
    public static final String EXTRA_HDMI_MODE              = "mode";

    public static final String SYS_DIGITAL_RAW              = "/sys/class/audiodsp/digital_raw";
    public static final String SYS_AUDIO_CAP                = "/sys/class/amhdmitx/amhdmitx0/aud_cap";
    public static final String SYS_AUIDO_HDMI               = "/sys/class/amhdmitx/amhdmitx0/config";
    public static final String SYS_AUIDO_SPDIF              = "/sys/devices/platform/spdif-dit.0/spdif_mute";

    public static final String AUIDO_DSP_AC3_DRC            = "/sys/class/audiodsp/ac3_drc_control";
    public static final String AUIDO_DSP_DTS_DEC            = "/sys/class/audiodsp/dts_dec_control";

    public static final String HDMI_STATE                   = "/sys/class/amhdmitx/amhdmitx0/hpd_state";
    public static final String HDMI_SUPPORT_LIST            = "/sys/class/amhdmitx/amhdmitx0/disp_cap";

    public static final String DISPLAY_MODE                 = "/sys/class/display/mode";
    public static final String DISPLAY_AXIS                 = "/sys/class/display/axis";

    public static final String VIDEO_AXIS                   = "/sys/class/video/axis";

    public static final String FB0_FREE_SCALE_AXIS          = "/sys/class/graphics/fb0/free_scale_axis";
    public static final String FB0_FREE_SCALE_MODE          = "/sys/class/graphics/fb0/freescale_mode";
    public static final String FB0_FREE_SCALE               = "/sys/class/graphics/fb0/free_scale";
    public static final String FB1_FREE_SCALE               = "/sys/class/graphics/fb1/free_scale";

    public static final String FB0_WINDOW_AXIS              = "/sys/class/graphics/fb0/window_axis";
    public static final String FB0_BLANK                    = "/sys/class/graphics/fb0/blank";

    public static final String ENV_CVBS_MODE                = "ubootenv.var.cvbsmode";
    public static final String ENV_HDMI_MODE                = "ubootenv.var.hdmimode";
    public static final String ENV_OUTPUT_MODE              = "ubootenv.var.outputmode";
    public static final String ENV_DIGIT_AUDIO              = "ubootenv.var.digitaudiooutput";
    public static final String ENV_IS_BEST_MODE             = "ubootenv.var.is.bestmode";

    public static final String PROP_BEST_OUTPUT_MODE        = "ro.platform.best_outputmode";
    public static final String PROP_HDMI_ONLY               = "ro.platform.hdmionly";

    public static final String FULL_WIDTH_480               = "720";
    public static final String FULL_HEIGHT_480              = "480";
    public static final String FULL_WIDTH_576               = "720";
    public static final String FULL_HEIGHT_576              = "576";
    public static final String FULL_WIDTH_720               = "1280";
    public static final String FULL_HEIGHT_720              = "720";
    public static final String FULL_WIDTH_1080              = "1920";
    public static final String FULL_HEIGHT_1080             = "1080";
    public static final String FULL_WIDTH_4K2K              = "3840";
    public static final String FULL_HEIGHT_4K2K             = "2160";
    public static final String FULL_WIDTH_4K2KSMPTE         = "4096";
    public static final String FULL_HEIGHT_4K2KSMPTE        = "2160";

    public static final String PCM                          = "PCM";
    public static final String RAW                          = "RAW";
    public static final String HDMI                         = "HDMI";
    public static final String SPDIF                        = "SPDIF";
    public static final String HDMI_RAW                     = "HDMI passthrough";
    public static final String SPDIF_RAW                    = "SPDIF passthrough";
    public static final int IS_AUTO                         = 0x10;
    public static final int IS_PCM                          = 0x01;
    public static final int IS_HDMI                         = 0x02;
    public static final int IS_SPDIF                        = 0x04;

    public static final String REAL_OUTPUT_SOC              = "meson8,meson8b,meson8m2,meson9b";
    public static final String UI_720P                      = "720p";
    public static final String UI_1080P                     = "1080p";
    public static final String UI_2160P                     = "2160p";
    public static final String HDMI_480                     = "480";
    public static final String HDMI_576                     = "576";
    public static final String HDMI_720                     = "720p";
    public static final String HDMI_1080                    = "1080";
    public static final String HDMI_4K2K                    = "2160p";
    public static final String HDMI_SMPTE                   = "smpte";

    private String DEFAULT_OUTPUT_MODE                      = "720p60hz";
    private static String currentOutputmode = null;
    private boolean ifModeSetting = false;
    private final Context mContext;
    final Object mLock = new Object[0];

    private SystemControlManager mSystenControl;
    SystemControlManager.DisplayInfo mDisplayInfo;

    public OutputModeManager(Context context) {
        mContext = context;

        mSystenControl = new SystemControlManager(context);
        mDisplayInfo = mSystenControl.getDisplayInfo();
        if (DEBUG && mDisplayInfo.defaultUI != null) {
            Log.d(TAG, "output mode, display type [1:tablet 2:MBOX 3:TV]: "
                + mDisplayInfo.type + ", default output:" + mDisplayInfo.defaultUI);
        }

        currentOutputmode = readSysfs(DISPLAY_MODE);
    }

    public void setOutputMode(final String mode) {
        setOutputModeNowLocked(mode);
    }

    public void setBestMode(String mode) {
        if (mode == null) {
            if (!isBestOutputmode()) {
                mSystenControl.setBootenv(ENV_IS_BEST_MODE, "true");
                if (SystemControlManager.USE_BEST_MODE) {
                    setOutputMode(getBestMatchResolution());
                } else {
                    setOutputMode(getHighestMatchResolution());
                }
            } else {
                mSystenControl.setBootenv(ENV_IS_BEST_MODE, "false");
            }
        } else {
            mSystenControl.setBootenv(ENV_IS_BEST_MODE, "false");
            setOutputModeNowLocked(mode);
        }
    }

    public void setOutputModeNowLocked(final String newMode){
        synchronized (mLock) {
            String oldMode = currentOutputmode;
            currentOutputmode = newMode;

            if (oldMode == null || oldMode.length() < 4) {
                Log.e(TAG, "get display mode error, oldMode:" + oldMode + " set to default " + DEFAULT_OUTPUT_MODE);
                oldMode = DEFAULT_OUTPUT_MODE;
            }

            if (DEBUG)
                Log.d(TAG, "change mode from " + oldMode + " -> " + newMode);

            if (newMode.equals(oldMode)) {
                if (DEBUG)
                    Log.d(TAG,"The same mode as current , do nothing !");
                return ;
            }

            shadowScreen();
            mSystenControl.setMboxOutputMode(newMode);
            saveNewModeProp(newMode);

            Intent intent = new Intent(ACTION_HDMI_MODE_CHANGED);
            //intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);
            intent.putExtra(EXTRA_HDMI_MODE, newMode);
            mContext.sendStickyBroadcast(intent);
        }
    }

    public void setOsdMouse(String curMode) {
        if (DEBUG)
            Log.d(TAG, "set osd mouse curMode " + curMode);
        mSystenControl.setOsdMouseMode(curMode);
    }

    public void setOsdMouse(int x, int y, int w, int h) {
        mSystenControl.setOsdMousePara(x, y, w, h);
    }

    private void saveNewModeProp(String newMode) {
        setBootenv(ENV_OUTPUT_MODE, newMode);
        if ((newMode != null) && newMode.contains("cvbs")) {
            setBootenv(ENV_CVBS_MODE, newMode);
        }
        else {
            setBootenv(ENV_HDMI_MODE, newMode);
        }
    }

    public String getCurrentOutputMode(){
        return readSysfs(DISPLAY_MODE);
    }

    public int[] getPosition(String mode) {
        return mSystenControl.getPosition(mode);
    }

    public void savePosition(int left, int top, int width, int height) {
        mSystenControl.setPosition(left, top, width, height);
    }

    public String getHdmiSupportList() {
        String list = readSupportList(HDMI_SUPPORT_LIST).replaceAll("[*]", "");

        if (DEBUG)
            Log.d(TAG, "getHdmiSupportList :" + list);
        return list;
    }

    public String getHighestMatchResolution() {
        final String KEY = "hz";
        final String FORMAT_P = "p";
        final String FORMAT_I = "i";

        String[] supportList = null;
        String value = readSupportList(HDMI_SUPPORT_LIST);
        if (value.indexOf(HDMI_480) >= 0 || value.indexOf(HDMI_576) >= 0
            || value.indexOf(HDMI_720) >= 0 || value.indexOf(HDMI_1080) >= 0
            || value.indexOf(HDMI_4K2K) >= 0 || value.indexOf(HDMI_SMPTE) >= 0) {
            supportList = (value.substring(0, value.length()-1)).split(",");
        }

        int type = -1;
        int intMode = -1;
        int higMode = 0;
        String outputMode = null;
        if (supportList != null) {
            for (int i = 0; i < supportList.length; i++) {
                String[] pref = supportList[i].split(KEY);
                if (pref != null) {
                    if ((type = supportList[i].indexOf(FORMAT_P)) >= 3) {          //p
                        intMode = Integer.parseInt(pref[0].replace(FORMAT_P, "1"));
                    } else if (type > 0) {                                          //smpte
                        outputMode = "smpte24hz";
                        break;
                    } else if ((type = supportList[i].indexOf(FORMAT_I)) > 0) {    //i
                        intMode = Integer.parseInt(pref[0].replace(FORMAT_I, "0"));
                    } else {                                                        //other
                        continue;
                    }
                    if (intMode > higMode) {
                        higMode = intMode;
                        outputMode = pref[0] + KEY;
                    }
                }
            }
        }
        if (outputMode != null) return outputMode;
        return getPropertyString(PROP_BEST_OUTPUT_MODE, DEFAULT_OUTPUT_MODE);
    }

    public String getBestMatchResolution() {
        if (DEBUG)
            Log.d(TAG, "get best mode, if support mode contains *, that is best mode, otherwise use:" + PROP_BEST_OUTPUT_MODE);

        String[] supportList = null;
        String value = readSupportList(HDMI_SUPPORT_LIST);
        if (value.indexOf(HDMI_480) >= 0 || value.indexOf(HDMI_576) >= 0
            || value.indexOf(HDMI_720) >= 0 || value.indexOf(HDMI_1080) >= 0
            || value.indexOf(HDMI_4K2K) >= 0 || value.indexOf(HDMI_SMPTE) >= 0) {
            supportList = (value.substring(0, value.length()-1)).split(",");
        }

        if (supportList != null) {
            for (int i = 0; i < supportList.length; i++) {
                if (supportList[i].contains("*")) {
                    return supportList[i].substring(0, supportList[i].length()-1);
                }
            }
        }

        return getPropertyString(PROP_BEST_OUTPUT_MODE, DEFAULT_OUTPUT_MODE);
    }

    public String getSupportedResolution() {
        String curMode = getBootenv(ENV_HDMI_MODE, DEFAULT_OUTPUT_MODE);

        if (DEBUG)
            Log.d(TAG, "get supported resolution curMode:" + curMode);

        String value = readSupportList(HDMI_SUPPORT_LIST);
        String[] supportList = null;

        if (value.indexOf(HDMI_480) >= 0 || value.indexOf(HDMI_576) >= 0
            || value.indexOf(HDMI_720) >= 0 || value.indexOf(HDMI_1080) >= 0
            || value.indexOf(HDMI_4K2K) >= 0 || value.indexOf(HDMI_SMPTE) >= 0) {
            supportList = (value.substring(0, value.length()-1)).split(",");
        }

        if (supportList == null) {
            return curMode;
        }

        for (int i = 0; i < supportList.length; i++) {
            if (supportList[i].equals(curMode)) {
                return curMode;
            }
        }

        if (SystemControlManager.USE_BEST_MODE) {
            return getBestMatchResolution();
        }
        return getHighestMatchResolution();
    }

    private String readSupportList(String path) {
        String str = null;
        String value = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            while ((str = br.readLine()) != null) {
                if (str != null)
                    value += str + ",";
            }
            br.close();

            Log.d(TAG, "TV support list is :" + value);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }

    public void initOutputMode(){
        if (isHDMIPlugged()) {
            setHdmiPlugged();
        } else {
            if (!currentOutputmode.contains("cvbs"))
                setHdmiUnPlugged();
        }

        //there can not set osd mouse parameter, otherwise bootanimation logo will shake
        //because set osd1 scaler will shake
    }

    public void setHdmiUnPlugged(){
        Log.d(TAG, "setHdmiUnPlugged");

        if (REAL_OUTPUT_SOC.contains(mDisplayInfo.socType)) {
            if (getPropertyBoolean(PROP_HDMI_ONLY, true)) {
                String cvbsmode = getBootenv(ENV_CVBS_MODE, "576cvbs");
                setOutputMode(cvbsmode);
            }
        }
    }

    public void setHdmiPlugged() {
        boolean isAutoMode = isBestOutputmode() || readSupportList(HDMI_SUPPORT_LIST).contains("null edid");

        Log.d(TAG, "setHdmiPlugged auto mode: " + isAutoMode);
        if (REAL_OUTPUT_SOC.contains(mDisplayInfo.socType)) {
            if (getPropertyBoolean(PROP_HDMI_ONLY, true)) {
                if (isAutoMode) {
                    if (SystemControlManager.USE_BEST_MODE) {
                        setOutputMode(getBestMatchResolution());
                    } else {
                        setOutputMode(getHighestMatchResolution());
                    }
                } else {
                    String mode = getSupportedResolution();
                    setOutputMode(mode);
                }
            }
            switchHdmiPassthough();
        }
    }

    public boolean isBestOutputmode() {
        return Boolean.parseBoolean(mSystenControl.getBootenv(ENV_IS_BEST_MODE, "true"));
    }

    public boolean isHDMIPlugged() {
        String status = readSysfs(HDMI_STATE);
        if ("1".equals(status))
            return true;
        else
            return false;
    }

    public boolean ifModeIsSetting() {
        return ifModeSetting;
    }

    private void shadowScreen() {
        writeSysfs(FB0_BLANK, "1");
        Thread task = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ifModeSetting = true;
                    Thread.sleep(1000);
                    writeSysfs(FB0_BLANK, "0");
                    ifModeSetting = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        task.start();
    }

    private void switchHdmiPassthough() {
        setDigitalMode(getBootenv(ENV_DIGIT_AUDIO, PCM));
    }

    public String getDigitalVoiceMode(){
        return getBootenv(ENV_DIGIT_AUDIO, PCM);
    }

    public int autoSwitchHdmiPassthough () {
        String mAudioCapInfo = readSysfsTotal(SYS_AUDIO_CAP);
        if (mAudioCapInfo.contains("Dobly_Digital+")) {
            setDigitalMode(HDMI_RAW);
            return 2;
        } else if (mAudioCapInfo.contains("AC-3")) {
            setDigitalMode(SPDIF_RAW);
            return 1;
        } else {
            setDigitalMode(PCM);
            return 0;
        }
    }

    public void setDigitalMode(String mode) {
        // value : "PCM" ,"RAW","SPDIF passthrough","HDMI passthrough"
        setBootenv(ENV_DIGIT_AUDIO, mode);
        mSystenControl.setDigitalMode(mode);
    }

    public void enableDobly_DRC (boolean enable) {
        if (enable) {       //open DRC
            writeSysfs(AUIDO_DSP_AC3_DRC, "drchighcutscale 0x64");
            writeSysfs(AUIDO_DSP_AC3_DRC, "drclowboostscale 0x64");
        } else {           //close DRC
            writeSysfs(AUIDO_DSP_AC3_DRC, "drchighcutscale 0");
            writeSysfs(AUIDO_DSP_AC3_DRC, "drclowboostscale 0");
        }
    }

    public void setDoblyMode (String mode) {
        //"CUSTOM_0","CUSTOM_1","LINE","RF"; default use "LINE"
        int i = Integer.parseInt(mode);
        if (i >= 0 && i <= 3) {
            writeSysfs(AUIDO_DSP_AC3_DRC, "drcmode" + " " + mode);
        } else {
            writeSysfs(AUIDO_DSP_AC3_DRC, "drcmode" + " " + "2");
        }
    }

    public void setDTS_DownmixMode(String mode) {
        // 0: Lo/Ro;   1: Lt/Rt;  default 0
        int i = Integer.parseInt(mode);
        if (i >= 0 && i <= 1) {
            writeSysfs(AUIDO_DSP_DTS_DEC, "dtsdmxmode" + " " + mode);
        } else {
            writeSysfs(AUIDO_DSP_DTS_DEC, "dtsdmxmode" + " " + "0");
        }
    }

    public void enableDTS_DRC_scale_control (boolean enable) {
        if (enable) {
            writeSysfs(AUIDO_DSP_DTS_DEC, "dtsdrcscale 0x64");
        } else {
            writeSysfs(AUIDO_DSP_DTS_DEC, "dtsdrcscale 0");
        }
    }

    public void enableDTS_Dial_Norm_control (boolean enable) {
        if (enable) {
            writeSysfs(AUIDO_DSP_DTS_DEC, "dtsdialnorm 1");
        } else {
            writeSysfs(AUIDO_DSP_DTS_DEC, "dtsdialnorm 0");
        }
    }

    private String getProperty(String key) {
        if (DEBUG)
            Log.i(TAG, "getProperty key:" + key);
        return mSystenControl.getProperty(key);
    }

    private String getPropertyString(String key, String def) {
        if (DEBUG)
            Log.i(TAG, "getPropertyString key:" + key + " def:" + def);
        return mSystenControl.getPropertyString(key, def);
    }

    private int getPropertyInt(String key,int def) {
        if (DEBUG)
            Log.i(TAG, "getPropertyInt key:" + key + " def:" + def);
        return mSystenControl.getPropertyInt(key, def);
    }

    private long getPropertyLong(String key,long def) {
        if (DEBUG)
            Log.i(TAG, "getPropertyLong key:" + key + " def:" + def);
        return mSystenControl.getPropertyLong(key, def);
    }

    private boolean getPropertyBoolean(String key,boolean def) {
        if (DEBUG)
            Log.i(TAG, "getPropertyBoolean key:" + key + " def:" + def);
        return mSystenControl.getPropertyBoolean(key, def);
    }

    private void setProperty(String key, String value) {
        if (DEBUG)
            Log.i(TAG, "setProperty key:" + key + " value:" + value);
        mSystenControl.setProperty(key, value);
    }

    private String getBootenv(String key, String value) {
        if (DEBUG)
            Log.i(TAG, "getBootenv key:" + key + " def value:" + value);
        return mSystenControl.getBootenv(key, value);
    }

    private int getBootenvInt(String key, String value) {
        if (DEBUG)
            Log.i(TAG, "getBootenvInt key:" + key + " def value:" + value);
        return Integer.parseInt(mSystenControl.getBootenv(key, value));
    }

    private void setBootenv(String key, String value) {
        if (DEBUG)
            Log.i(TAG, "setBootenv key:" + key + " value:" + value);
        mSystenControl.setBootenv(key, value);
    }

    private String readSysfsTotal(String path) {
        return mSystenControl.readSysFs(path).replaceAll("\n", "");
    }

    private String readSysfs(String path) {

        return mSystenControl.readSysFs(path).replaceAll("\n", "");
        /*
        if (!new File(path).exists()) {
            Log.e(TAG, "File not found: " + path);
            return null;
        }

        String str = null;
        StringBuilder value = new StringBuilder();

        if (DEBUG)
            Log.i(TAG, "readSysfs path:" + path);

        try {
            FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);
            try {
                while ((str = br.readLine()) != null) {
                    if (str != null)
                        value.append(str);
                };
                fr.close();
                br.close();
                if (value != null)
                    return value.toString();
                else
                    return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        */
    }

    private boolean writeSysfs(String path, String value) {
        if (DEBUG)
            Log.i(TAG, "writeSysfs path:" + path + " value:" + value);

        return mSystenControl.writeSysFs(path, value);
        /*
        if (!new File(path).exists()) {
            Log.e(TAG, "File not found: " + path);
            return false;
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path), 64);
            try {
                writer.write(value);
            } finally {
                writer.close();
            }
            return true;

        } catch (IOException e) {
            Log.e(TAG, "IO Exception when write: " + path, e);
            return false;
        }
        */
    }
}

