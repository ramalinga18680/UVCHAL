
package com.droidlogic;

import android.content.Context;
import android.hardware.hdmi.HdmiControlManager;
import android.hardware.hdmi.HdmiPlaybackClient;
import android.hardware.hdmi.HdmiHotplugEvent;
import android.hardware.hdmi.HdmiPortInfo;
import android.hardware.hdmi.HdmiDeviceInfo;
import android.hardware.hdmi.HdmiControlManager.VendorCommandListener;
import android.hardware.hdmi.HdmiPlaybackClient.OneTouchPlayCallback;
import android.hardware.hdmi.IHdmiControlService;
import android.util.Slog;
import android.os.ServiceManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.Handler;
import android.os.Build;
import com.android.internal.app.LocalePicker;

import libcore.util.EmptyArray;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;
import java.io.UnsupportedEncodingException;
import android.content.res.Configuration;

public class HdmiCecExtend {
    private final String TAG = "HdmiCecExtend";
    static final int MESSAGE_FEATURE_ABORT = 0x00;
    static final int MESSAGE_IMAGE_VIEW_ON = 0x04;
    static final int MESSAGE_TUNER_STEP_INCREMENT = 0x05;
    static final int MESSAGE_TUNER_STEP_DECREMENT = 0x06;
    static final int MESSAGE_TUNER_DEVICE_STATUS = 0x07;
    static final int MESSAGE_GIVE_TUNER_DEVICE_STATUS = 0x08;
    static final int MESSAGE_RECORD_ON = 0x09;
    static final int MESSAGE_RECORD_STATUS = 0x0A;
    static final int MESSAGE_RECORD_OFF = 0x0B;
    static final int MESSAGE_TEXT_VIEW_ON = 0x0D;
    static final int MESSAGE_RECORD_TV_SCREEN = 0x0F;
    static final int MESSAGE_GIVE_DECK_STATUS = 0x1A;
    static final int MESSAGE_DECK_STATUS = 0x1B;
    static final int MESSAGE_SET_MENU_LANGUAGE = 0x32;
    static final int MESSAGE_CLEAR_ANALOG_TIMER = 0x33;
    static final int MESSAGE_SET_ANALOG_TIMER = 0x34;
    static final int MESSAGE_TIMER_STATUS = 0x35;
    static final int MESSAGE_STANDBY = 0x36;
    static final int MESSAGE_PLAY = 0x41;
    static final int MESSAGE_DECK_CONTROL = 0x42;
    static final int MESSAGE_TIMER_CLEARED_STATUS = 0x043;
    static final int MESSAGE_USER_CONTROL_PRESSED = 0x44;
    static final int MESSAGE_USER_CONTROL_RELEASED = 0x45;
    static final int MESSAGE_GIVE_OSD_NAME = 0x46;
    static final int MESSAGE_SET_OSD_NAME = 0x47;
    static final int MESSAGE_SET_OSD_STRING = 0x64;
    static final int MESSAGE_SET_TIMER_PROGRAM_TITLE = 0x67;
    static final int MESSAGE_SYSTEM_AUDIO_MODE_REQUEST = 0x70;
    static final int MESSAGE_GIVE_AUDIO_STATUS = 0x71;
    static final int MESSAGE_SET_SYSTEM_AUDIO_MODE = 0x72;
    static final int MESSAGE_REPORT_AUDIO_STATUS = 0x7A;
    static final int MESSAGE_GIVE_SYSTEM_AUDIO_MODE_STATUS = 0x7D;
    static final int MESSAGE_SYSTEM_AUDIO_MODE_STATUS = 0x7E;
    static final int MESSAGE_ROUTING_CHANGE = 0x80;
    static final int MESSAGE_ROUTING_INFORMATION = 0x81;
    static final int MESSAGE_ACTIVE_SOURCE = 0x82;
    static final int MESSAGE_GIVE_PHYSICAL_ADDRESS = 0x83;
    static final int MESSAGE_REPORT_PHYSICAL_ADDRESS = 0x84;
    static final int MESSAGE_REQUEST_ACTIVE_SOURCE = 0x85;
    static final int MESSAGE_SET_STREAM_PATH = 0x86;
    static final int MESSAGE_DEVICE_VENDOR_ID = 0x87;
    static final int MESSAGE_VENDOR_COMMAND = 0x89;
    static final int MESSAGE_VENDOR_REMOTE_BUTTON_DOWN = 0x8A;
    static final int MESSAGE_VENDOR_REMOTE_BUTTON_UP = 0x8B;
    static final int MESSAGE_GIVE_DEVICE_VENDOR_ID = 0x8C;
    static final int MESSAGE_MENU_REQUEST = 0x8D;
    static final int MESSAGE_MENU_STATUS = 0x8E;
    static final int MESSAGE_GIVE_DEVICE_POWER_STATUS = 0x8F;
    static final int MESSAGE_REPORT_POWER_STATUS = 0x90;
    static final int MESSAGE_GET_MENU_LANGUAGE = 0x91;
    static final int MESSAGE_SELECT_ANALOG_SERVICE = 0x92;
    static final int MESSAGE_SELECT_DIGITAL_SERVICE = 0x93;
    static final int MESSAGE_SET_DIGITAL_TIMER = 0x97;
    static final int MESSAGE_CLEAR_DIGITAL_TIMER = 0x99;
    static final int MESSAGE_SET_AUDIO_RATE = 0x9A;
    static final int MESSAGE_INACTIVE_SOURCE = 0x9D;
    static final int MESSAGE_CEC_VERSION = 0x9E;
    static final int MESSAGE_GET_CEC_VERSION = 0x9F;
    static final int MESSAGE_VENDOR_COMMAND_WITH_ID = 0xA0;
    static final int MESSAGE_CLEAR_EXTERNAL_TIMER = 0xA1;
    static final int MESSAGE_SET_EXTERNAL_TIMER = 0xA2;
    static final int MESSAGE_REPORT_SHORT_AUDIO_DESCRIPTOR = 0xA3;
    static final int MESSAGE_REQUEST_SHORT_AUDIO_DESCRIPTOR = 0xA4;
    static final int MESSAGE_INITIATE_ARC = 0xC0;
    static final int MESSAGE_REPORT_ARC_INITIATED = 0xC1;
    static final int MESSAGE_REPORT_ARC_TERMINATED = 0xC2;
    static final int MESSAGE_REQUEST_ARC_INITIATION = 0xC3;
    static final int MESSAGE_REQUEST_ARC_TERMINATION = 0xC4;
    static final int MESSAGE_TERMINATE_ARC = 0xC5;
    static final int MESSAGE_CDC_MESSAGE = 0xF8;
    static final int MESSAGE_ABORT = 0xFF;

    // Send result codes. It should be consistent with hdmi_cec.h's send_message error code.
    static final int SEND_RESULT_SUCCESS = 0;
    static final int SEND_RESULT_NAK = 1;
    static final int SEND_RESULT_BUSY = 2;
    static final int SEND_RESULT_FAILURE = 3;

    /** Logical address for TV */
    public static final int ADDR_TV = 0;
    /** Logical address for recorder 1 */
    public static final int ADDR_RECORDER_1 = 1;
    /** Logical address for recorder 2 */
    public static final int ADDR_RECORDER_2 = 2;
    /** Logical address for tuner 1 */
    public static final int ADDR_TUNER_1 = 3;
    /** Logical address for playback 1 */
    public static final int ADDR_PLAYBACK_1 = 4;
    /** Logical address for audio system */
    public static final int ADDR_AUDIO_SYSTEM = 5;
    /** Logical address for tuner 2 */
    public static final int ADDR_TUNER_2 = 6;
    /** Logical address for tuner 3 */
    public static final int ADDR_TUNER_3 = 7;
    /** Logical address for playback 2 */
    public static final int ADDR_PLAYBACK_2 = 8;
    /** Logical address for recorder 3 */
    public static final int ADDR_RECORDER_3 = 9;
    /** Logical address for tuner 4 */
    public static final int ADDR_TUNER_4 = 10;
    /** Logical address for playback 3 */
    public static final int ADDR_PLAYBACK_3 = 11;
    /** Logical address reserved for future usage */
    public static final int ADDR_RESERVED_1 = 12;
    /** Logical address reserved for future usage */
    public static final int ADDR_RESERVED_2 = 13;
    /** Logical address for TV other than the one assigned with {@link #ADDR_TV} */
    public static final int ADDR_SPECIFIC_USE = 14;
    /** Logical address for devices to which address cannot be allocated */
    public static final int ADDR_UNREGISTERED = 15;
    /** Logical address used in the destination address field for broadcast messages */
    public static final int ADDR_BROADCAST = 15;
    /** Logical address used to indicate it is not initialized or invalid. */
    public static final int ADDR_INVALID = -1;

    static final int MENU_STATE_ACTIVATED = 0;
    static final int MENU_STATE_DEACTIVATED = 1;
    private static final int OSD_NAME_MAX_LENGTH = 13;

    private Context mContext = null;
    private HdmiControlManager mControl = null;
    private HdmiPlaybackClient mPlayback = null;
    private List<HdmiPortInfo> mPortInfo = null;
    private boolean mLanguangeChanged = false;
    private boolean mInitFinished = false;
    private IHdmiControlService mService = null;
    private int mPhyAddr = 0x1000;
    private int mVendorId = 0;
    private Handler mHandler;

    public HdmiCecExtend(Context ctx) {
        Slog.d(TAG, "HdmiCecExtend start");
        System.load("/system/lib/hw/hdmi_cec.odroidc2.so");
        nativeInit(this);
        Slog.d(TAG, "nativeInit:" + this);
        mContext = ctx;
        mControl = (HdmiControlManager) mContext.getSystemService(Context.HDMI_CONTROL_SERVICE);
        mService = IHdmiControlService.Stub.asInterface(ServiceManager.getService(Context.HDMI_CONTROL_SERVICE));
        mHandler = new Handler();
        if (mControl != null) {
            mPlayback = mControl.getPlaybackClient();
            Slog.d(TAG, "mHasPlaybackDevice:" + mPlayback);
            if (mPlayback != null) {
                mPlayback.setVendorCommandListener(mVendorCmdListener);
                mPlayback.oneTouchPlay(mOneTouchPlay);
                mVendorId = nativeGetVendorId();
                Slog.d(TAG, "vendorId:" + mVendorId);
                if (mLanguangeChanged == false) {
                    mHandler.postDelayed(mDelayedRun, 100);
                }
            }
            mControl.addHotplugEventListener(new HdmiControlManager.HotplugEventListener() {
                    @Override
                    public void onReceived(HdmiHotplugEvent event) {
                        Slog.d(TAG, "HdmiHotplugEvent, connected:" + event.isConnected());
                        if (mPlayback != null) {
                            if (event.isConnected() == true && mLanguangeChanged == false && mInitFinished == true) {
                                /* TODO: */
                            } else {
                                mLanguangeChanged = false;
                                mPortInfo = null;
                            }
                        }
                    }
                });
        } else {
            Slog.d(TAG, "can't find HdmiControlManager");
        }
    }

    public void updatePortInfo() {
        int timeout = 0;
        if (mService != null) {
            mPhyAddr = nativeGetPhysicalAddr();
        }
    }

    private final OneTouchPlayCallback mOneTouchPlay = new OneTouchPlayCallback () {
        @Override
        public void onComplete(int result) {
            Slog.d(TAG, "oneTouchPlay:" + result);
            switch (result) {
            case HdmiControlManager.RESULT_SUCCESS:
            case HdmiControlManager.RESULT_TIMEOUT:
                if (mLanguangeChanged == false) {
                    SendGetMenuLanguage(ADDR_TV);
                }
                break;
            }
        }
    };

    public void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {

        }
    }

    public void oneTouchPlayExt(int flag) {
        /*
         * menually start one touch play
         */
        Slog.d(TAG, "oneTouchPlayExt started, flag:" + String.format("0x%02X", flag));
        ReportPhysicalAddr(ADDR_BROADCAST, mPhyAddr, HdmiDeviceInfo.DEVICE_PLAYBACK);
        SendVendorId(ADDR_BROADCAST, mVendorId);
        SendImageViewOn(ADDR_TV);
        SendActiveSource(ADDR_BROADCAST, mPhyAddr);
        SendReportMenuStatus(ADDR_TV, MENU_STATE_ACTIVATED);
        sleep(100);
        SendGetMenuLanguage(ADDR_TV);
        mInitFinished = true;
    }

    public boolean updateLanguage(Locale locale) {
        try {
            LocalePicker.updateLocale(locale);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void onLanguageChange(String lan) {
        Slog.d(TAG, "onLanguageChange:" + lan);

        if (lan == null)
            return;

        HdmiCecLanguageHelp cecLanguage = new HdmiCecLanguageHelp(lan);
        Locale l = new Locale(cecLanguage.LanguageCode(), cecLanguage.CountryCode());
        mLanguangeChanged = updateLanguage(l);
        Slog.w(TAG, "set menu launage:" + lan + ", " + l.getLanguage() +
                "." + l.getCountry() + ":" + mLanguangeChanged);
    }

    private void onCecMessageRx(byte[] msg) {
        int dest, init;
        int opcode, size, addr;
        size = msg.length;
        if (size == 0) {
            return ;
        }
        init = (msg[0] >> 4) & 0xf;
        dest = (msg[0] >> 0) & 0xf;
        if (size > 1) {
            opcode = (msg[1] & 0xFF);
            /* TODO: process messages service can't process */
            switch (opcode) {
            case MESSAGE_GIVE_OSD_NAME:
                SetOsdName(init);
                break;

            case MESSAGE_GIVE_PHYSICAL_ADDRESS:
                ReportPhysicalAddr(ADDR_BROADCAST, mPhyAddr, HdmiDeviceInfo.DEVICE_PLAYBACK);
                break;

            case MESSAGE_GIVE_DEVICE_VENDOR_ID:
                SendVendorId(ADDR_BROADCAST, mVendorId);
                break;

            case MESSAGE_GIVE_DEVICE_POWER_STATUS:
                SendReportPowerStatus(init, HdmiControlManager.POWER_STATUS_ON);
                break;

            case MESSAGE_SET_MENU_LANGUAGE:
                try {
                    byte lan[] = new byte[3];
                    System.arraycopy(msg, 2, lan, 0, 3);
                    String iso3Language = new String(lan, 0, 3, "US-ASCII");
                    onLanguageChange(iso3Language);
                } catch (UnsupportedEncodingException e) {
                    Slog.d(TAG, "process MESSAGE_SET_MENU_LANGUAGE failed");
                }
                break;

            case MESSAGE_SET_STREAM_PATH:
                addr = ((msg[2] & 0xff) << 8) | (msg[3] & 0xff);
                if (addr == mPhyAddr) {
                    SendActiveSource(ADDR_BROADCAST, mPhyAddr);
                }
                break;


            case MESSAGE_GET_CEC_VERSION:
                int version = nativeGetCecVersion();
                SendCecVersion(init, version);
                break;

            case MESSAGE_REQUEST_ACTIVE_SOURCE:
                SendActiveSource(ADDR_BROADCAST, mPhyAddr);
                break;

            case MESSAGE_ACTIVE_SOURCE:
                addr = ((msg[2] & 0xff) << 8) | (msg[3] & 0xff);
                if (addr != mPhyAddr) {
                    Slog.d(TAG, "other source " + String.format("0x%02X", addr) +
                                " actived, may confilct");
                }
                break;

            case MESSAGE_ROUTING_CHANGE:
                addr = ((msg[4] & 0xff) << 8) | (msg[5] & 0xff);
                if (addr == mPhyAddr) {
                    Slog.d(TAG, "routing change to current source");
                } else {
                    Slog.d(TAG, "routing change to other source");
                }
                break;

            case MESSAGE_GIVE_DECK_STATUS:
                /* deck stopped */
                SendDeckStatus(init, 0x1A);
                break;

            /* these command will processed by system service */
            case MESSAGE_USER_CONTROL_PRESSED:
            case MESSAGE_USER_CONTROL_RELEASED:
            case MESSAGE_STANDBY:
                break;

            default:
                Slog.d(TAG, "can't Process message:" + String.format("0x%02x", (byte)opcode));
                break;
            }
        }
    }

    private void onAddAddress(int addr) {
        Slog.d(TAG, "onAddressAllocated:" + String.format("0x%02x", addr));
        mHandler.postDelayed(mDelayedRun, 200);
    }

    private final VendorCommandListener mVendorCmdListener = new VendorCommandListener() {
        @Override
        public void onReceived(int srcAddress, int destAddress, byte[] params, boolean hasVendorId) {
            Slog.d(TAG, "VendorCommandReceived, src:" + srcAddress + ", dst:"
                        + destAddress + ",para:" + params + ", hasVendorId:" + hasVendorId);
        }

        @Override
        public void onControlStateChanged(boolean enabled, int reason) {
            Slog.d(TAG, "VendorCmd State changed:" + enabled + ", reason:" + reason);
            /* TODO: */
            switch (reason) {
            case HdmiControlManager.CONTROL_STATE_CHANGED_REASON_STANDBY:
                break;
            case HdmiControlManager.CONTROL_STATE_CHANGED_REASON_WAKEUP:
                break;
            }
        }
    };

    private final Runnable mDelayedRun = new Runnable() {
        @Override
        public void run() {
            updatePortInfo();
            oneTouchPlayExt(0);
        }
    };

    private byte[] buildCecMsg(int opcode, byte[] operands) {
        byte[] params = new byte[operands.length + 1];
        params[0] = (byte)(opcode & 0xff);
        System.arraycopy(operands, 0, params, 1, operands.length);
        return params;
    }

    private void SetOsdName(int dest) {
        String name = Build.MODEL;
        int length = Math.min(name.length(), OSD_NAME_MAX_LENGTH);
        byte[] params;
        try {
            params = name.substring(0, length).getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            Slog.d(TAG, "can't append osd name");
            return ;
        }
        byte[] body = new byte[1 + params.length];
        body[0] = (byte)MESSAGE_SET_OSD_NAME & 0xff;
        System.arraycopy(params, 0, body, 1, params.length);
        SendCecMessage(dest, body);
    }

    private void SendActiveSource(int dest, int physicalAddr) {
        byte[] msg = new byte[] {(byte)(MESSAGE_ACTIVE_SOURCE & 0xff),
                                 (byte)((physicalAddr >> 8) & 0xff),
                                 (byte) (physicalAddr & 0xff)
                                };
        SendCecMessage(dest, msg);
    }

    private void ReportPhysicalAddr(int dest, int phyAddr, int type) {
        byte[] msg = new byte[] {(byte)(MESSAGE_REPORT_PHYSICAL_ADDRESS & 0xff),
                                 (byte)((phyAddr >> 8) & 0xff),
                                 (byte) (phyAddr & 0xff),
                                 (byte) (type & 0xff)
                                };
        SendCecMessage(dest, msg);
    }

    private void SendVendorId(int dest, int id) {
        byte[] msg = new byte[] {(byte)(MESSAGE_DEVICE_VENDOR_ID & 0xff),
                                 (byte)((id >> 16) & 0xff),
                                 (byte)((id >>  8) & 0xff),
                                 (byte)((id >>  0) & 0xff)
                                };
        SendCecMessage(dest, msg);
    }

    private void SendImageViewOn(int dest) {
        SendCecMessage(dest, buildCecMsg(MESSAGE_IMAGE_VIEW_ON, EmptyArray.BYTE));
    }

    private void SendReportMenuStatus(int dest, int status) {
        byte[] para = new byte[] {(byte) (status& 0xFF)};
        byte[] msg = buildCecMsg(MESSAGE_MENU_STATUS, para);
        SendCecMessage(dest, msg);
    }

    private void SendReportPowerStatus(int dest, int status) {
        byte[] para = new byte[] {(byte) (status& 0xFF)};
        byte[] msg = buildCecMsg(MESSAGE_REPORT_POWER_STATUS, para);
        SendCecMessage(dest, msg);
    }

    private void SendGetMenuLanguage(int dest) {
        SendCecMessage(dest, buildCecMsg(MESSAGE_GET_MENU_LANGUAGE, EmptyArray.BYTE));
    }

    private void SendCecVersion(int dest, int version) {
        byte body[] = new byte[2];
        body[0] = (byte)(MESSAGE_CEC_VERSION & 0xff);
        body[1] = (byte)(version & 0xff);
        SendCecMessage(dest, body);
    }

    private void SendDeckStatus(int dest, int status) {
        byte body[] = new byte[2];
        body[0] = (byte)(MESSAGE_DECK_STATUS & 0xff);
        body[1] = (byte)(status & 0xff);
        SendCecMessage(dest, body);
    }

    class HdmiCecLanguageHelp {
        private final String [][] mCecLanguage = {
            {"chi", "zh", "CN"},
            {"zho", "zh", "TW"},
            {"eng", "en", "US"},
            {"jpn", "ja", "JP"},
            {"kor", "ko", "KR"},
            {"fra", "fr", "FR"},
            {"fre", "fr", "FR"},
            {"ger", "de", "DE"},
            {"deu", "de", "DE"},
            {"cpp", "pt", "PT"},
            {"spa", "es", "ES"},
            {"rus", "ru", "RU"},
            {"ara", "ar", "SA"},
            {"hin", "en", "IN"}
        };

        private int mIndex;

        HdmiCecLanguageHelp(String str) {
            int size;
            for (size = 0; size < mCecLanguage.length; size++) {
                if (mCecLanguage[size][0].equals(str)) {
                    mIndex = size;
                    break;
                }
            }
            if (size == mCecLanguage.length) {
                mIndex = -1;
            }
        }

        public String LanguageCode() {
            if (mIndex != -1) {
                return mCecLanguage[mIndex][1];
            }
            return null;
        }

        public String CountryCode() {
            if (mIndex != -1) {
                return mCecLanguage[mIndex][2];
            }
            return null;
        }

        /*
         * get android language code for cec language code
         */
        public final String getCecLanguageCode(String cecLanguage) {
            int size;
            for (size = 0; size < mCecLanguage.length; size++) {
                if (mCecLanguage[size][0].equals(cecLanguage))
                    return mCecLanguage[size][1];
            }
            return null;
        }

        /*
         * get android country code for cec language code
         */
        public final String getCecCountryCode(String cecLanguage) {
            int size;
            for (size = 0; size < mCecLanguage.length; size++) {
                if (mCecLanguage[size][0].equals(cecLanguage))
                    return mCecLanguage[size][2];
            }
            return null;
        }
    }

    public int SendCecMessage(int dest, byte[] body) {
        int retry = 2, ret = 0;
        while (retry > 0) {
            ret = nativeSendCecMessage(dest, body);
            if (ret == SEND_RESULT_SUCCESS) {
                break;
            }
            retry--;
        }
        return ret;
    }

    /* for native */
    public native int  nativeSendCecMessage(int dest, byte[] body);
    public native void nativeInit(HdmiCecExtend ext);
    public native int  nativeGetPhysicalAddr();
    public native int  nativeGetVendorId();
    public native int  nativeGetCecVersion();
}
