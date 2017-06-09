package com.droidlogic.app.tv;

import android.content.UriMatcher;
import android.media.tv.TvContract;
import android.media.tv.TvContract.Channels;
import android.media.tv.TvInputInfo;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.SparseArray;


public class DroidLogicTvUtils
{

    /**
     * final parameters for {@link TvInpuptService.Session.notifySessionEvent}
     */
    public static final String SIG_INFO_EVENT = "sig_info_event";
    public static final String SIG_INFO_TYPE  = "sig_info_type";
    public static final String SIG_INFO_LABEL  = "sig_info_label";
    public static final String SIG_INFO_ARGS  = "sig_info_args";
    public static final String AV_SIG_SCRAMBLED  = "av_sig_scambled";

    public static final String SIG_INFO_C_DISPLAYNUM_EVENT = "sig_info_c_displaynum_event";
    public static final String SIG_INFO_C_DISPLAYNUM = "sig_info_c_displaynum";

    public static final String SIG_INFO_C_PROCESS_EVENT = "sig_info_c_process_event";
    public static final String SIG_INFO_C_STORE_BEGIN_EVENT = "sig_info_c_store_begin_event";
    public static final String SIG_INFO_C_STORE_END_EVENT = "sig_info_c_store_end_event";
    public static final String SIG_INFO_C_SCAN_BEGIN_EVENT = "sig_info_c_scan_begin_event";
    public static final String SIG_INFO_C_SCAN_END_EVENT = "sig_info_c_scan_end_event";
    public static final String SIG_INFO_C_SCAN_EXIT_EVENT = "sig_info_c_scan_exit_event";
    public static final String SIG_INFO_C_SCANNING_FRAME_STABLE_EVENT = "sig_info_c_scanning_frame_stable_event";

    public static final String SIG_INFO_C_TYPE = "type";
    public static final String SIG_INFO_C_PRECENT = "precent";
    public static final String SIG_INFO_C_TOTALCOUNT = "totalcount";
    public static final String SIG_INFO_C_LOCK = "lock";
    public static final String SIG_INFO_C_CNUM = "cnum";
    public static final String SIG_INFO_C_FREQ = "freq";
    public static final String SIG_INFO_C_PROGRAMNAME = "programName";
    public static final String SIG_INFO_C_SRVTYPE = "srvType";
    public static final String SIG_INFO_C_MSG = "msg";
    public static final String SIG_INFO_C_STRENGTH = "strength";
    public static final String SIG_INFO_C_QUALITY = "quality";
    //ATV
    public static final String SIG_INFO_C_VIDEOSTD = "videoStd";
    public static final String SIG_INFO_C_AUDIOSTD = "audioStd";
    public static final String SIG_INFO_C_ISAUTOSTD = "isAutoStd";
    public static final String SIG_INFO_C_FINETUNE = "fineTune";

    //DTV
    public static final String SIG_INFO_C_MODE = "mode";
    public static final String SIG_INFO_C_SR = "sr";
    public static final String SIG_INFO_C_MOD = "mod";
    public static final String SIG_INFO_C_BANDWIDTH = "bandwidth";
    public static final String SIG_INFO_C_OFM_MODE = "ofdm_mode";
    public static final String SIG_INFO_C_TS_ID = "ts_id";
    public static final String SIG_INFO_C_ORIG_NET_ID = "orig_net_id";

    public static final String SIG_INFO_C_SERVICEiD = "serviceID";
    public static final String SIG_INFO_C_VID = "vid";
    public static final String SIG_INFO_C_VFMT = "vfmt";
    public static final String SIG_INFO_C_AIDS = "aids";
    public static final String SIG_INFO_C_AFMTS = "afmts";
    public static final String SIG_INFO_C_ALANGS = "alangs";
    public static final String SIG_INFO_C_ATYPES = "atypes";
    public static final String SIG_INFO_C_PCR = "pcr";

    public static final String SIG_INFO_C_STYPES = "stypes";
    public static final String SIG_INFO_C_SIDS = "sids";
    public static final String SIG_INFO_C_SSTYPES = "sstypes";
    public static final String SIG_INFO_C_SID1S = "sid1s";
    public static final String SIG_INFO_C_SID2S = "sid2s";
    public static final String SIG_INFO_C_SLANGS = "slangs";



    public static final int SIG_INFO_TYPE_ATV    = 0;
    public static final int SIG_INFO_TYPE_DTV    = 1;
    public static final int SIG_INFO_TYPE_HDMI   = 2;
    public static final int SIG_INFO_TYPE_AV     = 3;
    public static final int SIG_INFO_TYPE_OTHER  = 4;

    /**
     * source input type need to switch
     */
    private static final int SOURCE_TYPE_START  = 0;
    private static final int SOURCE_TYPE_END    = 7;

    public static final int SOURCE_TYPE_ATV     = SOURCE_TYPE_START;
    public static final int SOURCE_TYPE_DTV     = SOURCE_TYPE_START + 1;
    public static final int SOURCE_TYPE_AV1     = SOURCE_TYPE_START + 2;
    public static final int SOURCE_TYPE_AV2     = SOURCE_TYPE_START + 3;
    public static final int SOURCE_TYPE_HDMI1   = SOURCE_TYPE_START + 4;
    public static final int SOURCE_TYPE_HDMI2   = SOURCE_TYPE_START + 5;
    public static final int SOURCE_TYPE_HDMI3   = SOURCE_TYPE_START + 6;
    public static final int SOURCE_TYPE_OTHER   = SOURCE_TYPE_END;

    /**
     * source input id sync with {@link CTvin.h}
     */
    public static final int DEVICE_ID_ATV        = 0;
    public static final int DEVICE_ID_AV1        = 1;
    public static final int DEVICE_ID_AV2        = 2;
    public static final int DEVICE_ID_HDMI1      = 5;
    public static final int DEVICE_ID_HDMI2      = 6;
    public static final int DEVICE_ID_HDMI3      = 7;
    public static final int DEVICE_ID_DTV        = 10;

    public static final int RESULT_OK = 1;
    public static final int RESULT_UPDATE = 2;
    public static final int RESULT_FAILED = 3;

    /**
     * action for {@link TvInputService.Session.onAppPrivateCommand}
     */
    public static final String ACTION_STOP_TV = "stop_tv";

    public static final String ACTION_STOP_PLAY = "stop_play";

    public static final String ACTION_TIMEOUT_SUSPEND = "android.intent.action.suspend";

    public static final String ACTION_UPDATE_TV_PLAY = "android.intent.action.update_tv";

    public static final String ACTION_DELETE_CHANNEL = "android.intent.action.tv_delete_channel";

    public static final String ACTION_SWITCH_CHANNEL = "android.intent.action.tv_switch_channel";

    public static final String ACTION_SUBTITLE_SWITCH = "android.intent.action.subtitle_switch";

    public static final String ACTION_CHANNEL_CHANGED = "android.intent.action.tv_channel_changed";

    public static final String ACTION_PROGRAM_APPOINTED = "android.intent.action.tv_appointed_program";

    public static final String ACTION_ATV_AUTO_SCAN = "atv_auto_scan";
    public static final String ACTION_DTV_AUTO_SCAN = "dtv_auto_scan";
    public static final String ACTION_DTV_MANUAL_SCAN = "dtv_manual_scan";
    public static final String ACTION_ATV_PAUSE_SCAN = "atv_pause_scan";
    public static final String ACTION_ATV_RESUME_SCAN = "atv_resume_scan";
    public static final String ACTION_STOP_SCAN = "stop_scan";
    public static final String PARA_MANUAL_SCAN = "scan_freq";

    /**
     * Other extra names for {@link TvInputInfo.createSetupIntent#intent} except for input id.
     */
    public static final String EXTRA_CHANNEL_ID = "tv_channel_id";

    public static final String EXTRA_CHANNEL_DEVICE_ID = "channel_device_id";

    public static final String EXTRA_PROGRAM_ID = "tv_program_id";

    public static final String EXTRA_KEY_CODE = "key_code";

    public static final String EXTRA_CHANNEL_NUMBER = "channel_number";

    public static final String EXTRA_IS_RADIO_CHANNEL = "is_radio_channel";

    public static final String EXTRA_SUBTITLE_SWITCH_VALUE = "sub_switch_val";

    /**
     * used for table {@link Settings#System}.
     * {@link #TV_START_UP_ENTER_APP} indicates whether enter into TV but not home activity,
     * and enter into it when value is {@value >0}.
     * {@link #TV_START_UP_APP_NAME} indicates the class name of TV, format is pkg_name/.class_name
     */
    public static final String TV_START_UP_ENTER_APP = "tv_start_up_enter_app";
    public static final String TV_START_UP_APP_NAME = "tv_start_up_app_name";
    public static final String TV_KEY_DEFAULT_LANGUAGE = "default_language";
    public static final String TV_KEY_SUBTITLE_SWITCH = "sub_switch";

    public static final String TV_CURRENT_DEVICE_ID = "tv_current_device_id";
    public static final String TV_ATV_CHANNEL_INDEX = "tv_atv_channel_index";
    public static final String TV_DTV_CHANNEL_INDEX  = "tv_dtv_channel_index";
    public static final String TV_CURRENT_CHANNEL_IS_RADIO = "tv_current_channel_is_radio";

    /**
     * message for {@link TvInputBaseSession#handleMessage(android.os.Message)}
     */
    public static final int SESSION_DO_RELEASE          = 1;
    public static final int SESSION_DO_SET_SURFACE      = 3;
    public static final int SESSION_DO_SURFACE_CHANGED  = 4;
    public static final int SESSION_DO_TUNE             = 6;
    public static final int SESSION_DO_APP_PRIVATE      = 9;
    public static final int SESSION_UNBLOCK_CONTENT     = 13;

    private static final UriMatcher sUriMatcher;
    public static final int NO_MATCH = UriMatcher.NO_MATCH;
    public static final int MATCH_CHANNEL = 1;
    public static final int MATCH_CHANNEL_ID = 2;
    public static final int MATCH_CHANNEL_ID_LOGO = 3;
    public static final int MATCH_PASSTHROUGH_ID = 4;
    public static final int MATCH_PROGRAM = 5;
    public static final int MATCH_PROGRAM_ID = 6;
    public static final int MATCH_WATCHED_PROGRAM = 7;
    public static final int MATCH_WATCHED_PROGRAM_ID = 8;
    static {
        sUriMatcher = new UriMatcher(NO_MATCH);
        sUriMatcher.addURI(TvContract.AUTHORITY, "channel", MATCH_CHANNEL);
        sUriMatcher.addURI(TvContract.AUTHORITY, "channel/#", MATCH_CHANNEL_ID);
        sUriMatcher.addURI(TvContract.AUTHORITY, "channel/#/logo", MATCH_CHANNEL_ID_LOGO);
        sUriMatcher.addURI(TvContract.AUTHORITY, "passthrough/*", MATCH_PASSTHROUGH_ID);
        sUriMatcher.addURI(TvContract.AUTHORITY, "program", MATCH_PROGRAM);
        sUriMatcher.addURI(TvContract.AUTHORITY, "program/#", MATCH_PROGRAM_ID);
        sUriMatcher.addURI(TvContract.AUTHORITY, "watched_program", MATCH_WATCHED_PROGRAM);
        sUriMatcher.addURI(TvContract.AUTHORITY, "watched_program/#", MATCH_WATCHED_PROGRAM_ID);
    }

    public static int matchsWhich(Uri uri) {
        return sUriMatcher.match(uri);
    }

    public static int getChannelId(Uri uri) {
        if (sUriMatcher.match(uri) == MATCH_CHANNEL_ID)
            return Integer.parseInt(uri.getLastPathSegment());
        return -1;
    }

    public static boolean isHardwareInput(TvInputInfo info) {
        if (info == null)
            return false;

        String[] temp = info.getId().split("/");
        return temp.length==3 ? true : false;
    }

    public static boolean isHardwareInput(String input_id) {
        if (TextUtils.isEmpty(input_id))
            return false;
        String[] temp = input_id.split("/");
        return temp.length==3 ? true : false;
    }

    public static int getHardwareDeviceId(TvInputInfo info) {
        if (info == null)
            return -1;

        String[] temp = info.getId().split("/");
        return temp.length==3 ? Integer.parseInt(temp[2].substring(2)) : -1;
    }

    public static int getHardwareDeviceId(String input_id) {
        if (TextUtils.isEmpty(input_id))
            return -1;
        String[] temp = input_id.split("/");
        return temp.length==3 ? Integer.parseInt(temp[2].substring(2)) : -1;
    }

    public static int getSourceType(int device_id) {
        int ret = SOURCE_TYPE_OTHER;
        switch (device_id) {
            case DEVICE_ID_ATV:
                ret = SOURCE_TYPE_ATV;
                break;
            case DEVICE_ID_DTV:
                ret = SOURCE_TYPE_DTV;
                break;
            case DEVICE_ID_AV1:
                ret = SOURCE_TYPE_AV1;
                break;
            case DEVICE_ID_AV2:
                ret = SOURCE_TYPE_AV2;
                break;
            case DEVICE_ID_HDMI1:
                ret = SOURCE_TYPE_HDMI1;
                break;
            case DEVICE_ID_HDMI2:
                ret = SOURCE_TYPE_HDMI2;
                break;
            case DEVICE_ID_HDMI3:
                ret = SOURCE_TYPE_HDMI3;
                break;
            default:
                break;
        }
        return ret;
    }

    public static int getSigType(int source_type) {
        int ret = 0;
        switch (source_type) {
            case SOURCE_TYPE_ATV:
                ret = SIG_INFO_TYPE_ATV;
                break;
            case SOURCE_TYPE_DTV:
                ret = SIG_INFO_TYPE_DTV;
                break;
            case SOURCE_TYPE_AV1:
            case SOURCE_TYPE_AV2:
                ret = SIG_INFO_TYPE_AV;
                break;
            case SOURCE_TYPE_HDMI1:
            case SOURCE_TYPE_HDMI2:
            case SOURCE_TYPE_HDMI3:
                ret = SIG_INFO_TYPE_HDMI;
                break;
            default:
                ret = SIG_INFO_TYPE_OTHER;
                break;
        }
        return ret;
    }

}
