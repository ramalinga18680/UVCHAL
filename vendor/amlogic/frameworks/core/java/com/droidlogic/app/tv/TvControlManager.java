package com.droidlogic.app.tv;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.SystemProperties;

import android.util.Log;
import android.view.View;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.graphics.ImageFormat;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;

//import android.media.audiofx.Srs;
//import android.media.audiofx.Hpeq;

import static com.droidlogic.app.tv.TvControlCommand.*;

public class TvControlManager {
    private static final String TAG = "TvControlManager";
    private static final String OPEN_TV_LOG_FLG = "open.libtv.log.flg";
    private boolean tvLogFlg =false;

    public static final int AUDIO_MUTE_ON               = 0;
    public static final int AUDIO_MUTE_OFF              = 1;

    public static final int AUDIO_SWITCH_OFF            = 0;
    public static final int AUDIO_SWITCH_ON             = 1;

    //atv media
    public static final int ATV_AUDIO_STD_DK            = 0;
    public static final int ATV_AUDIO_STD_I             = 1;
    public static final int ATV_AUDIO_STD_BG            = 2;
    public static final int ATV_AUDIO_STD_M             = 3;
    public static final int ATV_AUDIO_STD_L             = 4;
    public static final int ATV_AUDIO_STD_AUTO          = 5;
    public static final int ATV_AUDIO_STD_MUTE          = 6;

    public static final int ATV_VIDEO_STD_AUTO          = 0;
    public static final int ATV_VIDEO_STD_PAL           = 1;
    public static final int ATV_VIDEO_STD_NTSC          = 2;
    public static final int ATV_VIDEO_STD_SECAM         = 3;

    //tv run status
    public static final int TV_RUN_STATUS_INIT_ED       = -1;
    public static final int TV_RUN_STATUS_OPEN_ED       = 0;
    public static final int TV_RUN_STATUS_START_ED      = 1;
    public static final int TV_RUN_STATUS_RESUME_ED     = 2;
    public static final int TV_RUN_STATUS_PAUSE_ED      = 3;
    public static final int TV_RUN_STATUS_STOP_ED       = 4;
    public static final int TV_RUN_STATUS_CLOSE_ED      = 5;

    //scene mode
    public static final int SCENE_MODE_STANDARD         = 0;
    public static final int SCENE_MODE_GAME             = 1;
    public static final int SCENE_MODE_FILM             = 2;
    public static final int SCENE_MODE_USER             = 3;
    public static final int SCENE_MODE_MAX              = 4;

    static {
        System.loadLibrary("tv_jni");
    }

    private int mNativeContext; // accessed by native methods
    private EventHandler mEventHandler;
    private ErrorCallback mErrorCallback;
    private TVInSignalInfo.SigInfoChangeListener mSigInfoChangeLister = null;
    private TVInSignalInfo.SigChannelSearchListener mSigChanSearchListener = null;
    private VGAAdjustChangeListener mVGAChangeListener = null;
    private Status3DChangeListener mStatus3DChangeListener = null;
    private StatusTVChangeListener mStatusTVChangeListener = null;
    private DreamPanelChangeListener mDreamPanelChangeListener = null;
    private AdcCalibrationListener mAdcCalibrationListener = null;
    private SourceSwitchListener mSourceSwitchListener = null;
    private ChannelSelectListener mChannelSelectListener = null;
    private SerialCommunicationListener mSerialCommunicationListener = null;
    private CloseCaptionListener mCloseCaptionListener = null;
    private StatusSourceConnectListener mSourceConnectChangeListener = null;
    private HDMIRxCECListener mHDMIRxCECListener = null;
    private UpgradeFBCListener mUpgradeFBCListener  = null;
    private SubtitleUpdateListener mSubtitleListener = null;
    private ScannerEventListener mScannerListener = null;
    private StorDBEventListener mStorDBListener = null;
    private ScanningFrameStableListener mScanningFrameStableListener = null;
    private VframBMPEventListener mVframBMPListener = null;
    private EpgEventListener mEpgListener = null;
    private AVPlaybackListener mAVPlaybackListener = null;
    private VchipLockStatusListener mLockStatusListener = null;

    private static TvControlManager mInstance;

    private native final void native_setup(Object tv_this);
    private native final void native_release();
    public native void addCallbackBuffer(byte cb[]);
    public native final void unlock();
    public native final void lock();
    public native final void reconnect() throws IOException;
    private native int processCmd(Parcel p, Parcel r);
    private native final void native_create_video_frame_bitmap(Object bmp);
    private native final void native_create_subtitle_bitmap(Object bmp);

    private static void postEventFromNative(Object tv_ref, int what, Parcel ext) {
        ext.setDataPosition(0);

        TvControlManager c = (TvControlManager)((WeakReference) tv_ref).get();
        if (c == null)
            return;
        if (c.mEventHandler != null) {
            Message m = c.mEventHandler.obtainMessage(what, 0, 0, ext);
            c.mEventHandler.sendMessage(m);
        }
    }

    private int sendCmdToTv(Parcel p, Parcel r) {
        p.setDataPosition(0);
        int ret = processCmd(p, r);
        r.setDataPosition(0);
        return ret;
    }

    public int sendCmd(int cmd) {
        libtv_log_open();
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeInt(cmd);
        request.setDataPosition(0);
        processCmd(request, reply);
        reply.setDataPosition(0);
        int ret = reply.readInt();

        request.recycle();
        reply.recycle();
        return ret;
    }

    public int sendCmdIntArray(int cmd, int[] values) {
        libtv_log_open();
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeInt(cmd);

        for (int i = 0; i < values.length; i++) {
            request.writeInt(values[i]);
        }
        request.setDataPosition(0);
        processCmd(request, reply);
        reply.setDataPosition(0);
        int ret = reply.readInt();

        request.recycle();
        reply.recycle();
        return ret;
    }

    public int sendCmdStringArray(int cmd, String[] values) {
        libtv_log_open();
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeInt(cmd);

        for (int i = 0; i < values.length; i++) {
            request.writeString(values[i]);
        }
        request.setDataPosition(0);
        processCmd(request, reply);
        reply.setDataPosition(0);
        int ret = reply.readInt();

        request.recycle();
        reply.recycle();
        return ret;
    }

    class EventHandler extends Handler {
        int dataArray[];
        int cmdArray[];
        int msgPdu[];

        public EventHandler(Looper looper) {
            super(looper);
            dataArray = new int[512];//max data buf
            cmdArray = new int[128];
            msgPdu = new int[1200];
        }

        private void readScanEvent(ScannerEvent scan_ev, Parcel p) {
            int i;
            scan_ev.type = p.readInt();
            scan_ev.precent = p.readInt();
            scan_ev.totalcount = p.readInt();
            scan_ev.lock = p.readInt();
            scan_ev.cnum = p.readInt();
            scan_ev.freq = p.readInt();
            scan_ev.programName = p.readString();
            scan_ev.srvType = p.readInt();
            scan_ev.msg = p.readString();
            scan_ev.strength = p.readInt();
            scan_ev.quality = p.readInt();
            scan_ev.videoStd = p.readInt();
            scan_ev.audioStd = p.readInt();
            scan_ev.isAutoStd = p.readInt();

            scan_ev.mode = p.readInt();
            scan_ev.sr = p.readInt();
            scan_ev.mod = p.readInt();
            scan_ev.bandwidth = p.readInt();
            scan_ev.ofdm_mode = p.readInt();
            scan_ev.ts_id = p.readInt();
            scan_ev.orig_net_id = p.readInt();
            scan_ev.serviceID = p.readInt();
            scan_ev.vid = p.readInt();
            scan_ev.vfmt = p.readInt();
            int acnt = p.readInt();
            if (acnt != 0) {
                scan_ev.aids = new int[acnt];
                for (i=0;i<acnt;i++)
                    scan_ev.aids[i] = p.readInt();
                scan_ev.afmts = new int[acnt];
                for (i=0;i<acnt;i++)
                    scan_ev.afmts[i] = p.readInt();
                scan_ev.alangs = new String[acnt];
                for (i=0;i<acnt;i++)
                    scan_ev.alangs[i] = p.readString();
                scan_ev.atypes = new int[acnt];
                for (i=0;i<acnt;i++)
                    scan_ev.atypes[i] = p.readInt();
            }
            scan_ev.pcr = p.readInt();
            int scnt = p.readInt();
            if (scnt != 0) {
                scan_ev.stypes = new int[scnt];
                for (i=0;i<scnt;i++)
                    scan_ev.stypes[i] = p.readInt();
                scan_ev.sids = new int[scnt];
                for (i=0;i<scnt;i++)
                    scan_ev.sids[i] = p.readInt();
                scan_ev.sstypes = new int[scnt];
                for (i=0;i<scnt;i++)
                    scan_ev.sstypes[i] = p.readInt();
                scan_ev.sid1s = new int[scnt];
                for (i=0;i<scnt;i++)
                    scan_ev.sid1s[i] = p.readInt();
                scan_ev.sid2s = new int[scnt];
                for (i=0;i<scnt;i++)
                    scan_ev.sid2s[i] = p.readInt();
                scan_ev.slangs = new String[scnt];
                for (i=0;i<scnt;i++)
                    scan_ev.slangs[i] = p.readString();
            }
            scan_ev.free_ca = p.readInt();
            scan_ev.scrambled = p.readInt();
            scan_ev.scan_mode = p.readInt();
            scan_ev.sdtVersion = p.readInt();
        }

        @Override
        public void handleMessage(Message msg) {
            int i = 0, loop_count = 0, tmp_val = 0;
            Parcel p;

            switch (msg.what) {
                case SUBTITLE_UPDATE_CALLBACK:
                    if (mSubtitleListener != null) {
                        mSubtitleListener.onUpdate();
                    }
                    break;
                case VFRAME_BMP_EVENT_CALLBACK:
                    p = ((Parcel) (msg.obj));
                    if (mVframBMPListener != null) {
                        VFrameEvent ev = new VFrameEvent();
                        mVframBMPListener.onEvent(ev);
                        ev.FrameNum = p.readInt();
                        ev.FrameSize= p.readInt();
                        ev.FrameWidth= p.readInt();
                        ev.FrameHeight= p.readInt();
                    }
                    break;
                case SCAN_EVENT_CALLBACK:
                    p = ((Parcel) (msg.obj));
                    if (mScannerListener != null) {
                        ScannerEvent scan_ev = new ScannerEvent();
                        readScanEvent(scan_ev, p);
                        mScannerListener.onEvent(scan_ev);
                        if (mStorDBListener != null) {
                            mStorDBListener.StorDBonEvent(scan_ev);
                        }
                    }else if (mStorDBListener != null) {
                        ScannerEvent scan_ev = new ScannerEvent();
                        readScanEvent(scan_ev, p);
                        mStorDBListener.StorDBonEvent(scan_ev);
                    }
                    break;
                case SCANNING_FRAME_STABLE_CALLBACK:
                    p = ((Parcel) (msg.obj));
                    if (mScanningFrameStableListener != null) {
                        ScanningFrameStableEvent ev = new ScanningFrameStableEvent();
                        ev.CurScanningFrq = p.readInt();
                        mScanningFrameStableListener.onFrameStable(ev);
                    }
                    break;
                case VCHIP_CALLBACK:
                    Log.i(TAG,"atsc ---VCHIP_CALLBACK-----------------");
                    p = ((Parcel) (msg.obj));
                    if (mLockStatusListener != null) {
                        VchipLockStatus lockStatus = new VchipLockStatus();
                        lockStatus.blockstatus = p.readInt();
                        lockStatus.blockType = p.readInt();
                        lockStatus.vchipDimension = p.readString();
                        lockStatus.vchipAbbrev = p.readString();
                        lockStatus.vchipText = p.readString();
                        mLockStatusListener.onLock(lockStatus);
                    }
                    break;
                case EPG_EVENT_CALLBACK:
                    p = ((Parcel) (msg.obj));
                    if (mEpgListener != null) {
                        EpgEvent ev = new EpgEvent();
                        ev.type = p.readInt();
                        ev.time = p.readInt();
                        ev.programID = p.readInt();
                        ev.channelID = p.readInt();
                        mEpgListener.onEvent(ev);
                    }
                    break;
                case DTV_AV_PLAYBACK_CALLBACK:
                    p = ((Parcel) (msg.obj));
                    if (mAVPlaybackListener != null) {
                        int msgType= p.readInt();
                        int programID= p.readInt();
                        mAVPlaybackListener.onEvent(msgType, programID);
                    }
                    break ;
                case SEARCH_CALLBACK:
                    if (mSigChanSearchListener != null) {
                        if (msgPdu != null) {
                            loop_count = ((Parcel) (msg.obj)).readInt();
                            for (i = 0; i < loop_count; i++) {
                                msgPdu[i] = ((Parcel) (msg.obj)).readInt();
                            }
                            mSigChanSearchListener.onChannelSearchChange(msgPdu);
                        }
                    }
                    break;
                case SIGLE_DETECT_CALLBACK:
                    if (mSigInfoChangeLister != null) {
                        TVInSignalInfo sigInfo = new TVInSignalInfo();
                        sigInfo.transFmt = TVInSignalInfo.TransFmt.values()[(((Parcel) (msg.obj)).readInt())];
                        sigInfo.sigFmt = TVInSignalInfo.SignalFmt.valueOf(((Parcel) (msg.obj)).readInt());
                        sigInfo.sigStatus = TVInSignalInfo.SignalStatus.values()[(((Parcel) (msg.obj)).readInt())];
                        sigInfo.reserved = ((Parcel) (msg.obj)).readInt();
                        mSigInfoChangeLister.onSigChange(sigInfo);
                    }
                    break;
                case VGA_CALLBACK:
                    if (mVGAChangeListener != null) {
                        mVGAChangeListener.onVGAAdjustChange(((Parcel) (msg.obj)).readInt());
                    }
                    break;
                case STATUS_3D_CALLBACK:
                    if (mStatus3DChangeListener != null) {
                        mStatus3DChangeListener.onStatus3DChange(((Parcel) (msg.obj)).readInt());
                    }
                    break;
                case SOURCE_CONNECT_CALLBACK:
                    if (mSourceConnectChangeListener != null) {
                        mSourceConnectChangeListener.onSourceConnectChange( SourceInput.values()[((Parcel) (msg.obj)).readInt()], ((Parcel) (msg.obj)).readInt());
                    }
                    break;
                case HDMIRX_CEC_CALLBACK:
                    if (mHDMIRxCECListener != null) {
                        if (msgPdu != null) {
                            loop_count = ((Parcel) (msg.obj)).readInt();
                            for (i = 0; i < loop_count; i++) {
                                msgPdu[i] = ((Parcel) (msg.obj)).readInt();
                            }
                            mHDMIRxCECListener.onHDMIRxCECMessage(loop_count, msgPdu);
                        }
                    }
                    break;
                case UPGRADE_FBC_CALLBACK:
                    if (mUpgradeFBCListener != null) {
                        loop_count = ((Parcel) (msg.obj)).readInt();
                        tmp_val = ((Parcel) (msg.obj)).readInt();
                        Log.d(TAG, "state = " + loop_count + "    param = " + tmp_val);
                        mUpgradeFBCListener.onUpgradeStatus(loop_count, tmp_val);
                    }
                    break;
                case DREAM_PANEL_CALLBACK:
                    break;
                case ADC_CALIBRATION_CALLBACK:
                    if (mAdcCalibrationListener != null) {
                        mAdcCalibrationListener.onAdcCalibrationChange(((Parcel) (msg.obj)).readInt());
                    }
                    break;
                case SOURCE_SWITCH_CALLBACK:
                    if (mSourceSwitchListener != null) {
                        mSourceSwitchListener.onSourceSwitchStatusChange(
                                SourceInput.values()[(((Parcel) (msg.obj)).readInt())], ((Parcel) (msg.obj)).readInt());
                    }
                    break;
                case CHANNEL_SELECT_CALLBACK:
                    if (mChannelSelectListener != null) {
                        if (msgPdu != null) {
                            loop_count = ((Parcel) (msg.obj)).readInt();
                            for (i = 0; i < loop_count; i++) {
                                msgPdu[i] = ((Parcel) (msg.obj)).readInt();
                            }
                            mChannelSelectListener.onChannelSelect(msgPdu);
                        }
                    }
                    break;
                case SERIAL_COMMUNICATION_CALLBACK:
                    if (mSerialCommunicationListener != null) {
                        if (msgPdu != null) {
                            int dev_id = ((Parcel) (msg.obj)).readInt();
                            loop_count = ((Parcel) (msg.obj)).readInt();
                            for (i = 0; i < loop_count; i++) {
                                msgPdu[i] = ((Parcel) (msg.obj)).readInt();
                            }
                            mSerialCommunicationListener.onSerialCommunication(dev_id, loop_count, msgPdu);
                        }
                    }
                    break;
                case CLOSE_CAPTION_CALLBACK:
                    if (mCloseCaptionListener != null) {
                        loop_count = ((Parcel) (msg.obj)).readInt();
                        Log.d(TAG, "cc listenner data count =" + loop_count);
                        for (i = 0; i < loop_count; i++) {
                            dataArray[i] = ((Parcel) (msg.obj)).readInt();
                        }
                        //data len write to end
                        dataArray[dataArray.length - 1] = loop_count;
                        loop_count = ((Parcel) (msg.obj)).readInt();
                        for (i = 0; i < loop_count; i++) {
                            cmdArray[i] = ((Parcel) (msg.obj)).readInt();
                        }
                        cmdArray[cmdArray.length - 1] =  loop_count;
                        mCloseCaptionListener.onCloseCaptionProcess(dataArray, cmdArray);
                    }
                    break;
                default:
                    Log.e(TAG, "Unknown message type " + msg.what);
                    break;
            }
        }
    }

    public static TvControlManager getInstance() {
        if (null == mInstance) mInstance = new TvControlManager();
        return mInstance;
    }

    public TvControlManager() {
        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            mEventHandler = new EventHandler(looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            mEventHandler = new EventHandler(looper);
        } else {
            mEventHandler = null;
        }
        native_setup(new WeakReference<TvControlManager>(this));
        String LogFlg = TvMiscConfigGet(OPEN_TV_LOG_FLG,null);
        if ("log_open".equals(TvMiscConfigGet(OPEN_TV_LOG_FLG,null)))
            tvLogFlg =true;
    }

    protected void finalize() {
        //native_release();
    }

    // when app exit, need release manual
    public final void release() {
        libtv_log_open();
        native_release();
    }

    // Tv function
    // public int OpenTv();

    /**
     * @Function: CloseTv
     * @Description: Close Tv module
     * @Param:
     * @Return: 0 success, -1 fail
     */
    public int CloseTv() {
        return sendCmd(CLOSE_TV);
    }

    /**
     * @Function: StopTv
     * @Description: Stop Tv module
     * @Param:
     * @Return: 0 success, -1 fail
     */
    public int StopTv() {
        return sendCmd(STOP_TV);
    }

    public int StartTv() {
        return sendCmd(START_TV);
    }

    public int GetTvRunStatus() {
        return sendCmd(GET_TV_STATUS);
    }

    /**
     * @Function: GetLastSourceInput
     * @Description: Get last source input
     * @Param:
     * @Return: refer to enum SourceInput
     */
    public int GetLastSourceInput() {
        return sendCmd(GET_LAST_SOURCE_INPUT);
    }

    /**
     * @Function: GetCurrentSourceInput
     * @Description: Get current source input
     * @Param:
     * @Return: refer to enum SourceInput
     */
    public int GetCurrentSourceInput() {
        return sendCmd(GET_CURRENT_SOURCE_INPUT);
    }

    /**
     * @Function: GetCurrentSourceInputType
     * @Description: Get current source input type
     * @Param:
     * @Return: refer to enum SourceInput_Type
     */
    public SourceInput_Type GetCurrentSourceInputType() {
        libtv_log_open();
        int source_input = GetCurrentSourceInput();
        if (source_input == SourceInput.TV.toInt()) {
            return SourceInput_Type.SOURCE_TYPE_TV;
        } else if (source_input == SourceInput.AV1.toInt() || source_input == SourceInput.AV2.toInt()) {
            return SourceInput_Type.SOURCE_TYPE_AV;
        } else if (source_input == SourceInput.YPBPR1.toInt() || source_input == SourceInput.YPBPR2.toInt()) {
            return SourceInput_Type.SOURCE_TYPE_COMPONENT;
        } else if (source_input == SourceInput.VGA.toInt()) {
            return SourceInput_Type.SOURCE_TYPE_VGA;
        } else if (source_input == SourceInput.HDMI1.toInt() || source_input == SourceInput.HDMI2.toInt() || source_input == SourceInput.HDMI3.toInt()) {
            return SourceInput_Type.SOURCE_TYPE_HDMI;
        } else if (source_input == SourceInput.DTV.toInt()) {
            return SourceInput_Type.SOURCE_TYPE_DTV;
        } else {
            return SourceInput_Type.SOURCE_TYPE_MPEG;
        }
    }

    /**
     * @Function: GetCurrentSignalInfo
     * @Description: Get current signal infomation
     * @Param:
     * @Return: refer to class tvin_info_t
     */
    public TVInSignalInfo GetCurrentSignalInfo() {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(GET_CURRENT_SIGNAL_INFO);
        sendCmdToTv(cmd, r);
        TVInSignalInfo info = new TVInSignalInfo();
        info.transFmt = TVInSignalInfo.TransFmt.values()[r.readInt()];
        info.sigFmt = TVInSignalInfo.SignalFmt.valueOf(r.readInt());
        info.sigStatus = TVInSignalInfo.SignalStatus.values()[r.readInt()];
        info.reserved = r.readInt();
        return info;
    }

    /**
     * @Function: SetSourceInput
     * @Description: Set source input to switch source,
     * @Param: source_input, refer to enum SourceInput; win_pos, refer to class window_pos_t
     * @Return: 0 success, -1 fail
     */
    public int SetSourceInput(SourceInput srcInput) {
        /*int tmp_res_info = GetDisplayResolutionInfo();
          cmd.writeInt(0);
          cmd.writeInt(0);
          cmd.writeInt(((tmp_res_info >> 16) & 0xFFFF) - 1);
          cmd.writeInt(((tmp_res_info >> 0) & 0xFFFF) - 1);*/
        int val[] = new int[]{srcInput.toInt()};
        return sendCmdIntArray(SET_SOURCE_INPUT, val);
    }

    /**
     * @Function: IsDviSignal
     * @Description: To check if current signal is dvi signal
     * @Param:
     * @Return: true, false
     */
    public boolean IsDviSignal() {
        int ret = sendCmd(IS_DVI_SIGNAL);
        return ((ret == 1) ? true:false);
    }

    /**
     * @Function: IsPcFmtTiming
     * @Description: To check if current hdmi signal is pc signal
     * @Param:
     * @Return: true, false
     */
    public boolean IsPcFmtTiming() {
        int ret = sendCmd(IS_VGA_TIMEING_IN_HDMI);
        return ((ret == 1) ? true:false);
    }

    /**
     * @Function: GetVideoStreamStatus
     * @Description: Get video stream status to check decoder is actvie or inactive.
     * @Param:
     * @Return: 1 active, 0 inactive
     */
    public int GetVideoStreamStatus() {
        return sendCmd(GET_VIDEO_STREAM_STATUS);
    }

    /**
     * @Function: GetFirstStartSwitchType
     * @Description: Get first start switch type.
     * @Param:
     * @Return: reference as enum first_start_type
     */
    public int GetFirstStartSwitchType() {
        return sendCmd(GET_FIRST_START_SWITCH_TYPE);
    }

    /**
     * @Function: SetPreviewWindow
     * @Description: Set source input preview window axis
     * @Param: win_pos, refer to class window_pos_t
     * @Return: 0 success, -1 fail
     */
    public int SetPreviewWindow(int x1, int y1, int x2, int y2) {
        int val[] = new int[]{x1, y1, x2, y2};
        return sendCmdIntArray(SET_PREVIEW_WINDOW, val);
    }

    /**
     * @Function: SetDisableVideo
     * @Description: to enable/disable video
     * @Param: value 0/1
     * @Return: 0 success, -1 fail
     */
    public int SetDisableVideo(int arg0) {
        int val[] = new int[]{arg0};
        return sendCmdIntArray(SET_VIDEO_DISABLE, val);
    }

    /**
     * @Function: GetSourceConnectStatus
     * @Description: Get source connect status
     * @Param: source_input, refer to enum SourceInput
     * @Return: 0:plug out 1:plug in
     */
    public int GetSourceConnectStatus(SourceInput srcInput) {
        int val[] = new int[]{srcInput.toInt()};
        return sendCmdIntArray(GET_SOURCE_CONNECT_STATUS, val);
    }

    public String GetSourceInputList() {
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(GET_SOURCE_INPUT_LIST);
        sendCmdToTv(cmd, r);
        return r.readString();
    }

    // Tv function END

    // VGA

    /**
     * @Function: RunVGAAutoAdjust
     * @Description: Do vag auto adjustment
     * @Param:
     * @Return: 0 success, -1 fail
     */
    public int RunVGAAutoAdjust() {
        return sendCmd(RUN_VGA_AUTO_ADJUST);
    }

    /**
     * @Function: GetVGAAutoAdjustStatus
     * @Description: Get vag auto adjust status
     * @Param:
     * @Return: refer to enum tvin_process_status_t
     */
    public int GetVGAAutoAdjustStatus() {
        return sendCmd(GET_VGA_AUTO_ADJUST_STATUS);
    }

    /**
     * @Function: IsVGAAutoAdjustDone
     * @Description: To check if vag auto adjustment is done.
     * @Param:
     * @Return: 0 success, -1 fail
     */
    public int IsVGAAutoAdjustDone(TVInSignalInfo.SignalFmt fmt) {
        int val[] = new int[]{fmt.toInt()};
        return sendCmdIntArray(IS_VGA_AUTO_ADJUST_DONE, val);
    }

    /**
     * @Function: SetVGAHPos
     * @Description: Adjust vag h pos
     * @Param: value h pos, fmt current signal fmt
     * @Return: 0 success, -1 fail
     */
    public int SetVGAHPos(int value, TVInSignalInfo.SignalFmt fmt) {
        int val[] = new int[]{value, fmt.toInt()};
        return sendCmdIntArray(SET_VGA_HPOS, val);
    }

    /**
     * @Function: GetVGAHPos
     * @Description: Get vag h pos
     * @Param: fmt current signal fmt
     * @Return: h pos
     */
    public int GetVGAHPos(TVInSignalInfo.SignalFmt fmt) {
        int val[] = new int[]{fmt.toInt()};
        return sendCmdIntArray(GET_VGA_HPOS, val);
    }

    /**
     * @Function: SetVGAVPos
     * @Description: Adjust vag v pos
     * @Param: value v pos, fmt current signal fmt
     * @Return: 0 success, -1 fail
     */
    public int SetVGAVPos(int value, TVInSignalInfo.SignalFmt fmt) {
        int val[] = new int[]{value, fmt.toInt()};
        return sendCmdIntArray(SET_VGA_VPOS, val);
    }

    /**
     * @Function: GetVGAVPos
     * @Description: Get vag v pos
     * @Param: fmt current signal fmt
     * @Return: v pos
     */
    public int GetVGAVPos(TVInSignalInfo.SignalFmt fmt) {
        int val[] = new int[]{fmt.toInt()};
        return sendCmdIntArray(GET_VGA_VPOS, val);
    }

    /**
     * @Function: SetVGAClock
     * @Description: Adjust vag clock
     * @Param: value clock, fmt current signal fmt
     * @Return: 0 success, -1 fail
     */
    public int SetVGAClock(int value, TVInSignalInfo.SignalFmt fmt) {
        int val[] = new int[]{value, fmt.toInt()};
        return sendCmdIntArray(SET_VGA_CLOCK, val);
    }

    /**
     * @Function: GetVGAClock
     * @Description: Get vag clock
     * @Param: fmt current signal fmt
     * @Return: vga clock
     */
    public int GetVGAClock(TVInSignalInfo.SignalFmt fmt) {
        int val[] = new int[]{fmt.toInt()};
        return sendCmdIntArray(GET_VGA_CLOCK, val);
    }

    /**
     * @Function: SetVGAPhase
     * @Description: Adjust vag phase
     * @Param: value clock, fmt current signal fmt
     * @Return: 0 success, -1 fail
     */
    public int SetVGAPhase(int value, TVInSignalInfo.SignalFmt fmt) {
        int val[] = new int[]{value, fmt.toInt()};
        return sendCmdIntArray(SET_VGA_PHASE, val);
    }

    /**
     * @Function: GetVGAPhase
     * @Description: Get vag phase
     * @Param: fmt current signal fmt
     * @Return: vga phase
     */
    public int GetVGAPhase(TVInSignalInfo.SignalFmt fmt) {
        int val[] = new int[]{fmt.toInt()};
        return sendCmdIntArray(GET_VGA_PHASE, val);
    }

    /**
     * @Function: SetVGAParamDefault
     * @Description: reset vag param
     * @Param:
     * @Return: 0 success, -1 fail
     */
    public int SetVGAParamDefault() {
        return sendCmd(SET_VGAPARAM_DEFAULT);
    }
    // VGA END


    // HDMI

    /**
     * @Function: SetHdmiEdidVersion
     * @Description: set hdmi edid version to 1.4 or 2.0
     * @Param: port_id is hdmi port id; ver is set version
     * @Return: 0 success, -1 fail
     */
    public int SetHdmiEdidVersion(HdmiPortID port_id, HdmiEdidVer ver) {
        int val[] = new int[]{port_id.toInt(), ver.toInt()};
        return sendCmdIntArray(SET_HDMI_EDID_VER, val);
    }

   /**
     * @Function: SetHdmiHdcpKeyEnable
     * @Description: enable or disable hdmi hdcp kdy
     * @Param: iSenable is enable or disable
     * @Return: 0 success, -1 fail
     */
    public int SetHdmiHdcpKeyEnable(HdcpKeyIsEnable iSenable) {
        int val[] = new int[]{iSenable.toInt()};
        return sendCmdIntArray(SET_HDCP_KEY_ENABLE, val);
    }
    // HDMI END

    // PQ

    /**
     * @Function: SetBrightness
     * @Description: Set current source brightness value
     * @Param: value brightness, source refer to enum SourceInput_Type, is_save 1 to save
     * @Return: 0 success, -1 fail
     */
    public int SetBrightness(int value, SourceInput_Type source,  int is_save) {
        int val[] = new int[]{value, source.toInt(), is_save};
        return sendCmdIntArray(SET_BRIGHTNESS, val);
    }

    /**
     * @Function: GetBrightness
     * @Description: Get current source brightness value
     * @Param: source refer to enum SourceInput_Type
     * @Return: value brightness
     */
    public int GetBrightness(SourceInput_Type source) {
        int val[] = new int[]{source.toInt()};
        return sendCmdIntArray(GET_BRIGHTNESS, val);
    }

    /**
     * @Function: SaveBrightness
     * @Description: Save current source brightness value
     * @Param: value brightness, source refer to enum SourceInput_Type
     * @Return: 0 success, -1 fail
     */
    public int SaveBrightness(int value, SourceInput_Type source) {
        int val[] = new int[]{value, source.toInt()};
        return sendCmdIntArray(SAVE_BRIGHTNESS, val);
    }

    /**
     * @Function: SetContrast
     * @Description: Set current source contrast value
     * @Param: value contrast, source refer to enum SourceInput_Type, is_save 1 to save
     * @Return: 0 success, -1 fail
     */
    public int SetContrast(int value, SourceInput_Type source, int is_save) {
        int val[] = new int[]{value, source.toInt(), is_save};
        return sendCmdIntArray(SET_CONTRAST, val);
    }

    /**
     * @Function: GetContrast
     * @Description: Get current source contrast value
     * @Param: source refer to enum SourceInput_Type
     * @Return: value contrast
     */
    public int GetContrast(SourceInput_Type source) {
        int val[] = new int[]{source.toInt()};
        return sendCmdIntArray(GET_CONTRAST, val);
    }

    /**
     * @Function: SaveContrast
     * @Description: Save current source contrast value
     * @Param: value contrast, source refer to enum SourceInput_Type
     * @Return: 0 success, -1 fail
     */
    public int SaveContrast(int value, SourceInput_Type source) {
        int val[] = new int[]{value, source.toInt()};
        return sendCmdIntArray(SAVE_CONTRAST, val);
    }

    /**
     * @Function: SetSatuation
     * @Description: Set current source saturation value
     * @Param: value saturation, source refer to enum SourceInput_Type, fmt current fmt refer to tvin_sig_fmt_e, is_save 1 to save
     * @Return: 0 success, -1 fail
     */
    public int SetSaturation(int value, SourceInput_Type source, TVInSignalInfo.SignalFmt fmt, int is_save) {
        int val[] = new int[]{value, source.toInt(), fmt.toInt(), is_save};
        return sendCmdIntArray(SET_SATURATION, val);
    }

    /**
     * @Function: GetSatuation
     * @Description: Get current source saturation value
     * @Param: source refer to enum SourceInput_Type
     * @Return: value saturation
     */
    public int GetSaturation(SourceInput_Type source) {
        int val[] = new int[]{source.toInt()};
        return sendCmdIntArray(GET_SATURATION, val);
    }

    /**
     * @Function: SaveSaturation
     * @Description: Save current source saturation value
     * @Param: value saturation, source refer to enum SourceInput_Type
     * @Return: 0 success, -1 fail
     */
    public int SaveSaturation(int value, SourceInput_Type source) {
        int val[] = new int[]{value, source.toInt()};
        return sendCmdIntArray(SAVE_SATURATION, val);
    }

    /**
     * @Function: SetHue
     * @Description: Set current source hue value
     * @Param: value saturation, source refer to enum SourceInput_Type, fmt current fmt refer to tvin_sig_fmt_e, is_save 1 to save
     * @Return: 0 success, -1 fail
     */
    public int SetHue(int value, SourceInput_Type source, TVInSignalInfo.SignalFmt fmt, int is_save) {
        int val[] = new int[]{value, source.toInt(), fmt.toInt(), is_save};
        return sendCmdIntArray(SET_HUE, val);
    }

    /**
     * @Function: GetHue
     * @Description: Get current source hue value
     * @Param: source refer to enum SourceInput_Type
     * @Return: value hue
     */
    public int GetHue(SourceInput_Type source) {
        int val[] = new int[]{source.toInt()};
        return sendCmdIntArray(GET_HUE, val);
    }

    /**
     * @Function: SaveHue
     * @Description: Save current source hue value
     * @Param: value hue, source refer to enum SourceInput_Type
     * @Return: 0 success, -1 fail
     */
    public int SaveHue(int value, SourceInput_Type source) {
        int val[] = new int[]{value, source.toInt()};
        return sendCmdIntArray(SAVE_HUE, val);
    }

    public int SetSceneMode(int scene_mode,int is_save) {
        int val[] = new int[]{scene_mode, is_save};
        return sendCmdIntArray(SET_SCENEMODE, val);
    }

    public int GetSCENEMode() {
        return sendCmd(GET_SCENEMODE);
    }

    public enum PQMode {
        PQ_MODE_STANDARD(0),
        PQ_MODE_BRIGHT(1),
        PQ_MODE_SOFTNESS(2),
        PQ_MODE_USER(3),
        PQ_MODE_MOVIE(4),
        PQ_MODE_COLORFUL(5);

        private int val;

        PQMode(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    /**
     * @Function: SetPQMode
     * @Description: Set current source picture mode
     * @Param: value mode refer to enum PQMode, source refer to enum SourceInput_Type, is_save 1 to save
     * @Return: 0 success, -1 fail
     */
    public int SetPQMode(PQMode pq_mode, SourceInput_Type source, int is_save) {
        int val[] = new int[]{pq_mode.toInt(), source.toInt(), is_save};
        return sendCmdIntArray(SET_PQMODE, val);
    }

    /**
     * @Function: GetPQMode
     * @Description: Get current source picture mode
     * @Param: source refer to enum SourceInput_Type
     * @Return: picture mode refer to enum PQMode
     */
    public int GetPQMode(SourceInput_Type source) {
        int val[] = new int[]{source.toInt()};
        return sendCmdIntArray(GET_PQMODE, val);
    }

    /**
     * @Function: SavePQMode
     * @Description: Save current source picture mode
     * @Param: picture mode refer to enum PQMode, source refer to enum SourceInput_Type
     * @Return: 0 success, -1 fail
     */
    public int SavePQMode(PQMode pq_mode, SourceInput_Type source) {
        int val[] = new int[]{pq_mode.toInt(), source.toInt()};
        return sendCmdIntArray(SAVE_PQMODE, val);
    }

    /**
     * @Function: SetSharpness
     * @Description: Set current source sharpness value
     * @Param: value saturation, source_type refer to enum SourceInput_Type, is_enable set 1 as default
     * @Param: status_3d refer to enum Tvin_3d_Status, is_save 1 to save
     * @Return: 0 success, -1 fail
     */
    public int SetSharpness(int value, SourceInput_Type source_type, int is_enable, int status_3d, int is_save) {
        int val[] = new int[]{value, source_type.toInt(), is_enable, status_3d, is_save};
        return sendCmdIntArray(SET_SHARPNESS, val);
    }

    /**
     * @Function: GetSharpness
     * @Description: Get current source sharpness value
     * @Param: source refer to enum SourceInput_Type
     * @Return: value sharpness
     */
    public int GetSharpness(SourceInput_Type source_type) {
        int val[] = new int[]{source_type.toInt()};
        return sendCmdIntArray(GET_SHARPNESS, val);
    }

    /**
     * @Function: SaveSharpness
     * @Description: Save current source sharpness value
     * @Param: value sharpness, source refer to enum SourceInput_Type, isEnable set 1 enable as default
     * @Return: 0 success, -1 fail
     */
    public int SaveSharpness(int value, SourceInput_Type sourceType, int isEnable) {
        int val[] = new int[]{value, sourceType.toInt(), 1};
        return sendCmdIntArray(SAVE_SHARPNESS, val);
    }

    /**
     * @Function: SetBacklight
     * @Description: Set current source backlight value
     * @Param: value backlight, source refer to enum SourceInput_Type, is_save 1 to save
     * @Return: 0 success, -1 fail
     */
    public int SetBacklight(int value, SourceInput_Type source_type, int is_save) {
        int val[] = new int[]{value, source_type.toInt(), is_save};
        return sendCmdIntArray(SET_BACKLIGHT, val);
    }

    /**
     * @Function: GetBacklight
     * @Description: Get current source backlight value
     * @Param: source refer to enum SourceInput_Type
     * @Return: value backlight
     */
    public int GetBacklight(SourceInput_Type source_type) {
        int val[] = new int[]{source_type.toInt()};
        return sendCmdIntArray(GET_BACKLIGHT, val);
    }

    /**
     * @Function: SetBacklight_Switch
     * @Description: Set current backlight switch
     * @Param: value onoff
     * @Return: 0 success, -1 fail
     */
    public int SetBacklight_Switch(int onoff) {
        int val[] = new int[]{onoff};
        return sendCmdIntArray(SET_BACKLIGHT_SWITCH, val);
    }

    /**
     * @Function: GetBacklight_Switch
     * @Description: Get current backlight switch
     * @Param:
     * @Return: 0 success, -1 fail
     */
    public int GetBacklight_Switch() {
        return sendCmd(GET_BACKLIGHT_SWITCH);
    }

    /**
     * @Function: SaveBacklight
     * @Description: Save current source backlight value
     * @Param: value backlight, source refer to enum SourceInput_Type
     * @Return: 0 success, -1 fail
     */
    public int SaveBacklight(int value, SourceInput_Type source_type) {
        int val[] = new int[]{value, source_type.toInt()};
        return sendCmdIntArray(SAVE_BACKLIGHT, val);
    }

    public enum color_temperature {
        COLOR_TEMP_STANDARD(0),
        COLOR_TEMP_WARM(1),
        COLOR_TEMP_COLD(2),
        COLOR_TEMP_MAX(3);
        private int val;

        color_temperature(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    /**
     * @Function: SetColorTemperature
     * @Description: Set current source color temperature mode
     * @Param: value mode refer to enum color_temperature, source refer to enum SourceInput_Type, is_save 1 to save
     * @Return: 0 success, -1 fail
     */
    public int SetColorTemperature(color_temperature mode, SourceInput_Type source, int is_save) {
        int val[] = new int[]{mode.toInt(), source.toInt(), is_save};
        return sendCmdIntArray(SET_COLOR_TEMPERATURE, val);
    }

    /**
     * @Function: GetColorTemperature
     * @Description: Get current source color temperature mode
     * @Param: source refer to enum SourceInput_Type
     * @Return: color temperature refer to enum color_temperature
     */
    public int GetColorTemperature(SourceInput_Type source) {
        int val[] = new int[]{source.toInt()};
        return sendCmdIntArray(GET_COLOR_TEMPERATURE, val);
    }

    /**
     * @Function: SaveColorTemperature
     * @Description: Save current source color temperature mode
     * @Param: color temperature mode refer to enum color_temperature, source refer to enum SourceInput_Type
     * @Return: 0 success, -1 fail
     */
    public int SaveColorTemp(color_temperature mode, SourceInput_Type source) {
        int val[] = new int[]{mode.toInt(), source.toInt()};
        return sendCmdIntArray(SAVE_COLOR_TEMPERATURE, val);
    }

    public enum Display_Mode {
        DISPLAY_MODE_169(0),
        DISPLAY_MODE_PERSON(1),
        DISPLAY_MODE_MOVIE(2),
        DISPLAY_MODE_CAPTION(3),
        DISPLAY_MODE_MODE43(4),
        DISPLAY_MODE_FULL(5),
        DISPLAY_MODE_NORMAL(6),
        DISPLAY_MODE_NOSCALEUP(7),
        DISPLAY_MODE_CROP_FULL(8),
        DISPLAY_MODE_CROP(9),
        DISPLAY_MODE_ZOOM(10),
        DISPLAY_MODE_MAX(11);
        private int val;

        Display_Mode(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    /**
     * @Function: SetDisplayMode
     * @Description: Set current source display mode
     * @Param: value mode refer to enum Display_Mode, source refer to enum SourceInput_Type, fmt refer to tvin_sig_fmt_e, is_save 1 to save
     * @Return: 0 success, -1 fail
     */
    public int SetDisplayMode(Display_Mode display_mode, SourceInput_Type source, TVInSignalInfo.SignalFmt fmt, int is_save) {
        int val[] = new int[]{display_mode.toInt(), source.toInt(), fmt.toInt(), is_save};
        return sendCmdIntArray(SET_DISPLAY_MODE, val);
    }

    /**
     * @Function: GetDisplayMode
     * @Description: Get current source display mode
     * @Param: source refer to enum SourceInput_Type
     * @Return: display mode refer to enum Display_Mode
     */
    public int GetDisplayMode(SourceInput_Type source) {
        int val[] = new int[]{source.toInt()};
        return sendCmdIntArray(GET_DISPLAY_MODE, val);
    }

    /**
     * @Function: SaveDisplayMode
     * @Description: Save current source display mode
     * @Param: display mode refer to enum Display_Mode, source refer to enum SourceInput_Type
     * @Return: 0 success, -1 fail
     */
    public int SaveDisplayMode(Display_Mode display_mode, SourceInput_Type source) {
        int val[] = new int[]{display_mode.toInt(), source.toInt()};
        return sendCmdIntArray(SAVE_DISPLAY_MODE, val);
    }

    public enum Noise_Reduction_Mode {
        REDUCE_NOISE_CLOSE(0),
        REDUCE_NOISE_WEAK(1),
        REDUCE_NOISE_MID(2),
        REDUCE_NOISE_STRONG(3),
        REDUCTION_MODE_AUTO(4);

        private int val;

        Noise_Reduction_Mode(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    /**
     * @Function: SetNoiseReductionMode
     * @Description: Set current source noise reduction mode
     * @Param: noise reduction mode refer to enum Noise_Reduction_Mode, source refer to enum SourceInput_Type, is_save 1 to save
     * @Return: 0 success, -1 fail
     */
    public int SetNoiseReductionMode(Noise_Reduction_Mode nr_mode, SourceInput_Type source, int is_save) {
        int val[] = new int[]{nr_mode.toInt(), source.toInt(), is_save};
        return sendCmdIntArray(SET_NOISE_REDUCTION_MODE, val);
    }

    /**
     * @Function: GetNoiseReductionMode
     * @Description: Get current source noise reduction mode
     * @Param: source refer to enum SourceInput_Type
     * @Return: noise reduction mode refer to enum Noise_Reduction_Mode
     */
    public int GetNoiseReductionMode(SourceInput_Type source) {
        int val[] = new int[]{source.toInt()};
        return sendCmdIntArray(GET_NOISE_REDUCTION_MODE, val);
    }

    /**
     * @Function: SaveNoiseReductionMode
     * @Description: Save current source noise reduction mode
     * @Param: noise reduction mode refer to enum Noise_Reduction_Mode, source refer to enum SourceInput_Type
     * @Return: 0 success, -1 fail
     */
    public int SaveNoiseReductionMode(Noise_Reduction_Mode nr_mode, SourceInput_Type source) {
        int val[] = new int[]{nr_mode.toInt(), source.toInt()};
        return sendCmdIntArray(SAVE_NOISE_REDUCTION_MODE, val);
    }

    // PQ END

    // FACTORY
    public enum TEST_PATTERN {
        TEST_PATTERN_NONE(0),
        TEST_PATTERN_RED(1),
        TEST_PATTERN_GREEN(2),
        TEST_PATTERN_BLUE(3),
        TEST_PATTERN_WHITE(4),
        TEST_PATTERN_BLACK(5),
        TEST_PATTERN_MAX(6);

        private int val;

        TEST_PATTERN(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    public enum NOLINE_PARAMS_TYPE {
        NOLINE_PARAMS_TYPE_BRIGHTNESS(0),
        NOLINE_PARAMS_TYPE_CONTRAST(1),
        NOLINE_PARAMS_TYPE_SATURATION(2),
        NOLINE_PARAMS_TYPE_HUE(3),
        NOLINE_PARAMS_TYPE_SHARPNESS(4),
        NOLINE_PARAMS_TYPE_VOLUME(5),
        NOLINE_PARAMS_TYPE_MAX(6);

        private int val;

        NOLINE_PARAMS_TYPE(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    public class noline_params_t {
        public int osd0;
        public int osd25;
        public int osd50;
        public int osd75;
        public int osd100;
    }

    public class tvin_cutwin_t {
        public int hs;
        public int he;
        public int vs;
        public int ve;
    }

    /**
     * @Function: FactorySetPQMode_Brightness
     * @Description: Adjust brightness value in corresponding pq mode for factory menu conctrol
     * @Param: source_type refer to enum SourceInput_Type, PQMode refer to enum Pq_Mode, brightness brightness value
     * @Return: 0 success, -1 fail
     */
    public int FactorySetPQMode_Brightness(SourceInput_Type source_type, int pq_mode, int brightness) {
        int val[] = new int[]{source_type.toInt(), pq_mode, brightness};
        return sendCmdIntArray(FACTORY_SETPQMODE_BRIGHTNESS, val);
    }

    /**
     * @Function: FactoryGetPQMode_Brightness
     * @Description: Get brightness value in corresponding pq mode for factory menu conctrol
     * @Param: source_type refer to enum SourceInput_Type, PQMode refer to enum Pq_Mode
     * @Return: 0 success, -1 fail
     */
    public int FactoryGetPQMode_Brightness(SourceInput_Type source_type, int pq_mode) {
        int val[] = new int[]{source_type.toInt(), pq_mode};
        return sendCmdIntArray(FACTORY_GETPQMODE_BRIGHTNESS, val);
    }

    /**
     * @Function: FactorySetPQMode_Contrast
     * @Description: Adjust contrast value in corresponding pq mode for factory menu conctrol
     * @Param: source_type refer to enum SourceInput_Type, PQMode refer to enum Pq_Mode, contrast contrast value
     * @Return: contrast value
     */
    public int FactorySetPQMode_Contrast(SourceInput_Type source_type, int pq_mode, int contrast) {
        int val[] = new int[]{source_type.toInt(), pq_mode, contrast};
        return sendCmdIntArray(FACTORY_SETPQMODE_CONTRAST, val);
    }

    /**
     * @Function: FactoryGetPQMode_Contrast
     * @Description: Get contrast value in corresponding pq mode for factory menu conctrol
     * @Param: source_type refer to enum SourceInput_Type, PQMode refer to enum Pq_Mode
     * @Return: 0 success, -1 fail
     */
    public int FactoryGetPQMode_Contrast(SourceInput_Type source_type, int pq_mode) {
        int val[] = new int[]{source_type.toInt(), pq_mode};
        return sendCmdIntArray(FACTORY_GETPQMODE_CONTRAST, val);
    }

    /**
     * @Function: FactorySetPQMode_Saturation
     * @Description: Adjust saturation value in corresponding pq mode for factory menu conctrol
     * @Param: source_type refer to enum SourceInput_Type, PQMode refer to enum Pq_Mode, saturation saturation value
     * @Return: 0 success, -1 fail
     */
    public int FactorySetPQMode_Saturation(SourceInput_Type source_type, int pq_mode, int saturation) {
        int val[] = new int[]{source_type.toInt(), pq_mode, saturation};
        return sendCmdIntArray(FACTORY_SETPQMODE_SATURATION, val);
    }

    /**
     * @Function: FactoryGetPQMode_Saturation
     * @Description: Get saturation value in corresponding pq mode for factory menu conctrol
     * @Param: source_type refer to enum SourceInput_Type, PQMode refer to enum Pq_Mode
     * @Return: saturation value
     */
    public int FactoryGetPQMode_Saturation(SourceInput_Type source_type, int pq_mode) {
        int val[] = new int[]{source_type.toInt(), pq_mode};
        return sendCmdIntArray(FACTORY_GETPQMODE_SATURATION, val);
    }

    /**
     * @Function: FactorySetPQMode_Hue
     * @Description: Adjust hue value in corresponding pq mode for factory menu conctrol
     * @Param: source_type refer to enum SourceInput_Type, PQMode refer to enum Pq_Mode, hue hue value
     * @Return: 0 success, -1 fail
     */
    public int FactorySetPQMode_Hue(SourceInput_Type source_type, int pq_mode, int hue) {
        int val[] = new int[]{source_type.toInt(), pq_mode, hue};
        return sendCmdIntArray(FACTORY_SETPQMODE_HUE, val);
    }

    /**
     * @Function: FactoryGetPQMode_Hue
     * @Description: Get hue value in corresponding pq mode for factory menu conctrol
     * @Param: source_type refer to enum SourceInput_Type, PQMode refer to enum Pq_Mode
     * @Return: hue value
     */
    public int FactoryGetPQMode_Hue(SourceInput_Type source_type, int pq_mode) {
        int val[] = new int[]{source_type.toInt(), pq_mode};
        return sendCmdIntArray(FACTORY_GETPQMODE_HUE, val);
    }

    /**
     * @Function: FactorySetPQMode_Sharpness
     * @Description: Adjust sharpness value in corresponding pq mode for factory menu conctrol
     * @Param: source_type refer to enum SourceInput_Type, PQMode refer to enum Pq_Mode, sharpness sharpness value
     * @Return: 0 success, -1 fail
     */
    public int FactorySetPQMode_Sharpness(SourceInput_Type source_type, int pq_mode, int sharpness) {
        int val[] = new int[]{source_type.toInt(), pq_mode, sharpness};
        return sendCmdIntArray(FACTORY_SETPQMODE_SHARPNESS, val);
    }

    /**
     * @Function: FactoryGetPQMode_Sharpness
     * @Description: Get sharpness value in corresponding pq mode for factory menu conctrol
     * @Param: source_type refer to enum SourceInput_Type, PQMode refer to enum Pq_Mode
     * @Return: sharpness value
     */
    public int FactoryGetPQMode_Sharpness(SourceInput_Type source_type, int pq_mode) {
        int val[] = new int[]{source_type.toInt(), pq_mode};
        return sendCmdIntArray(FACTORY_GETPQMODE_SHARPNESS, val);
    }

    /**
     * @Function: FactorySetTestPattern
     * @Description: Set test patten for factory menu conctrol
     * @Param: pattern refer to enum TEST_PATTERN
     * @Return: 0 success, -1 fail
     */
    public int FactorySetTestPattern(int pattern) {
        int val[] = new int[]{pattern};
        return sendCmdIntArray(FACTORY_SETTESTPATTERN, val);
    }

    /**
     * @Function: FactoryGetTestPattern
     * @Description: Get current test patten for factory menu conctrol
     * @Param:
     * @Return: patten value refer to enum TEST_PATTERN
     */
    public int FactoryGetTestPattern() {
        return sendCmd(FACTORY_GETTESTPATTERN);
    }

    /**
     * @Function: FactoryResetPQMode
     * @Description: Reset all values of PQ mode for factory menu conctrol
     * @Param:
     * @Return: 0 success, -1 fail
     */
    public int FactoryResetPQMode() {
        return sendCmd(FACTORY_RESETPQMODE);
    }

    /**
     * @Function: FactoryResetColorTemp
     * @Description: Reset all values of color temperature mode for factory menu conctrol
     * @Param:
     * @Return: 0 success, -1 fail
     */
    public int FactoryResetColorTemp() {
        return sendCmd(FACTORY_RESETCOLORTEMP);
    }

    /**
     * @Function: FactorySetParamsDefault
     * @Description: Reset all values of pq mode and color temperature mode for factory menu conctrol
     * @Param:
     * @Return: 0 success, -1 fail
     */
    public int FactorySetParamsDefault() {
        return sendCmd(FACTORY_RESETPAMAMSDEFAULT);
    }

    /**
     * @Function: FactorySetDDRSSC
     * @Description: Set ddr ssc level for factory menu conctrol
     * @Param: step ddr ssc level
     * @Return: 0 success, -1 fail
     */
    public int FactorySetDDRSSC(int step) {
        int val[] = new int[]{step};
        return sendCmdIntArray(FACTORY_SETDDRSSC, val);
    }

    /**
     * @Function: FactoryGetDDRSSC
     * @Description: Get ddr ssc level for factory menu conctrol
     * @Param:
     * @Return: ddr ssc level
     */
    public int FactoryGetDDRSSC() {
        return sendCmd(FACTORY_GETDDRSSC);
    }

    /**
     * @Function: FactorySetLVDSSSC
     * @Description: Set lvds ssc level for factory menu conctrol
     * @Param: step lvds ssc level
     * @Return: 0 success, -1 fail
     */
    public int FactorySetLVDSSSC(int step) {
        int val[] = new int[]{step};
        return sendCmdIntArray(FACTORY_SETLVDSSSC, val);
    }

    /**
     * @Function: FactoryGetLVDSSSC
     * @Description: Get lvds ssc level for factory menu conctrol
     * @Param:
     * @Return: lvds ssc level
     */
    public int FactoryGetLVDSSSC() {
        return sendCmd(FACTORY_GETLVDSSSC);
    }

    /**
     * @Function: FactorySetNolineParams
     * @Description: Nonlinearize the params of corresponding nolinear param type for factory menu conctrol
     * @Param: noline_params_type refer to enum NOLINE_PARAMS_TYPE, source_type refer to SourceInput_Type, params params value refer to class noline_params_t
     * @Return: 0 success, -1 fail
     */
    public int FactorySetNolineParams(NOLINE_PARAMS_TYPE noline_params_type, SourceInput_Type source_type,
            noline_params_t params) {
        int val[] = new int[]{noline_params_type.toInt(), source_type.toInt(),
            params.osd0, params.osd25, params.osd50, params.osd75, params.osd100};
        return sendCmdIntArray(FACTORY_SETNOLINEPARAMS, val);
    }

    /**
     * @Function: FactoryGetNolineParams
     * @Description: Nonlinearize the params of corresponding nolinear param type for factory menu conctrol
     * @Param: noline_params_type refer to enum NOLINE_PARAMS_TYPE, source_type refer to SourceInput_Type
     * @Return: params value refer to class noline_params_t
     */
    public noline_params_t FactoryGetNolineParams(NOLINE_PARAMS_TYPE noline_params_type, SourceInput_Type source_type) {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(FACTORY_GETNOLINEPARAMS);
        cmd.writeInt(noline_params_type.toInt());
        cmd.writeInt(source_type.toInt());
        sendCmdToTv(cmd, r);
        noline_params_t noline_params = new noline_params_t();
        noline_params.osd0 = r.readInt();
        noline_params.osd25 = r.readInt();
        noline_params.osd50 = r.readInt();
        noline_params.osd75 = r.readInt();
        noline_params.osd100 = r.readInt();
        return noline_params;
    }

    /**
     * @Function: FactorySetOverscanParams
     * @Description: Set overscan params of corresponding source type and fmt for factory menu conctrol
     * @Param: source_type refer to enum SourceInput_Type, fmt refer to enum tvin_sig_fmt_e, status_3d refer to enum Tvin_3d_Status
     * @Param: trans_fmt refer to enum tvin_trans_fmt, cutwin_t refer to class tvin_cutwin_t
     * @Return: 0 success, -1 fail
     */
    public int FactorySetOverscanParams(SourceInput_Type source_type, TVInSignalInfo.SignalFmt fmt, Tvin_3d_Status status_3d,
            TVInSignalInfo.TransFmt trans_fmt, tvin_cutwin_t cutwin_t) {
        int val[] = new int[]{source_type.toInt(), fmt.toInt(), status_3d.ordinal(),
            trans_fmt.ordinal(), cutwin_t.hs, cutwin_t.he, cutwin_t.vs, cutwin_t.ve};
        return sendCmdIntArray(FACTORY_SETOVERSCAN, val);
    }

    /**
     * @Function: FactoryGetOverscanParams
     * @Description: Get overscan params of corresponding source type and fmt for factory menu conctrol
     * @Param: source_type refer to enum SourceInput_Type, fmt refer to enum tvin_sig_fmt_e, status_3d refer to enum Tvin_3d_Status
     * @Param: trans_fmt refer to enum tvin_trans_fmt
     * @Return: cutwin_t value for overscan refer to class tvin_cutwin_t
     */
    public tvin_cutwin_t FactoryGetOverscanParams(SourceInput_Type source_type, TVInSignalInfo.SignalFmt fmt,
            Tvin_3d_Status status_3d, TVInSignalInfo.TransFmt trans_fmt) {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(FACTORY_GETOVERSCAN);
        cmd.writeInt(source_type.ordinal());
        cmd.writeInt(fmt.toInt());
        cmd.writeInt(status_3d.ordinal());
        cmd.writeInt(trans_fmt.ordinal());
        sendCmdToTv(cmd, r);
        tvin_cutwin_t cutwin_t = new tvin_cutwin_t();
        cutwin_t.hs = r.readInt();
        cutwin_t.he = r.readInt();
        cutwin_t.vs = r.readInt();
        cutwin_t.ve = r.readInt();
        return cutwin_t;
    }

    /**
     * @Function: FactorySSMSetOutDefault
     * @Description: Reset all factory params in SSM
     * @Param:
     * @Return: 0 success, -1 fail
     */
    public int FactorySSMSetOutDefault() {
        return sendCmd(FACTORY_SET_OUT_DEFAULT);
    }

    public int FactoryGetGlobal_OGO_Offset_RGain() {
        return sendCmd(FACTORY_GETGLOBALOGO_RGAIN);
    }

    public int FactoryGetGlobal_OGO_Offset_GGain() {
        return sendCmd(FACTORY_GETGLOBALOGO_GGAIN);
    }

    public int FactoryGetGlobal_OGO_Offset_BGain() {
        return sendCmd(FACTORY_GETGLOBALOGO_BGAIN);
    }

    public int FactoryGetGlobal_OGO_Offset_ROffset() {
        return sendCmd(FACTORY_GETGLOBALOGO_ROFFSET);
    }

    public int FactoryGetGlobal_OGO_Offset_GOffset() {
        return sendCmd(FACTORY_GETGLOBALOGO_GOFFSET);
    }

    public int FactoryGetGlobal_OGO_Offset_BOffset() {
        return sendCmd(FACTORY_GETGLOBALOGO_BOFFSET);
    }

    public int FactorySetGlobal_OGO_Offset_RGain(int rgain) {
        int val[] = new int[]{rgain};
        return sendCmdIntArray(FACTORY_SETGLOBALOGO_RGAIN, val);
    }

    public int FactorySetGlobal_OGO_Offset_GGain(int ggain) {
        int val[] = new int[]{ggain};
        return sendCmdIntArray(FACTORY_SETGLOBALOGO_GGAIN, val);
    }

    public int FactorySetGlobal_OGO_Offset_BGain(int bgain) {
        int val[] = new int[]{bgain};
        return sendCmdIntArray(FACTORY_SETGLOBALOGO_BGAIN, val);
    }

    public int FactorySetGlobal_OGO_Offset_ROffset(int roffset) {
        int val[] = new int[]{roffset};
        return sendCmdIntArray(FACTORY_SETGLOBALOGO_ROFFSET, val);
    }

    public int FactorySetGlobal_OGO_Offset_GOffset(int goffset) {
        int val[] = new int[]{goffset};
        return sendCmdIntArray(FACTORY_SETGLOBALOGO_GOFFSET, val);
    }

    public int FactorySetGlobal_OGO_Offset_BOffset(int boffset) {
        int val[] = new int[]{boffset};
        return sendCmdIntArray(FACTORY_SETGLOBALOGO_BOFFSET, val);
    }

    public int FactoryCleanAllTableForProgram() {
        return sendCmd(FACTORY_CLEAN_ALL_TABLE_FOR_PROGRAM);
    }

    /**
     * @Function: FactoryGetAdbdStatus
     * @Description: factory get adbd status
     * @Param: none
     * @Return: 0 off ; 1 on ; -1 error
     */
    public int FactoryGetAdbdStatus() {
        return sendCmd(FACTORY_GETADBD_STATUS);
    }

    /**
     * @Function: FactorySetAdbdSwitch
     * @Description: factory set adbd switch
     * @Param: adbd_switch(0 : off ; 1 : on)
     * @Return: 0 success ; -1 failed ; -2 param error
     */
    public int FactorySetAdbdSwitch(int adbd_switch) {
        int val[] = new int[]{adbd_switch};
        return sendCmdIntArray(FACTORY_SETADBD_SWITCH, val);
    }


    public int FactorySetPatternYUV(int mask, int y, int u, int v) {
        int val[] = new int[]{mask, y, u, v};
        return sendCmdIntArray(FACTORY_SETPATTERN_YUV, val);
    }

    // FACTORY END

    // AUDIO
    // Audio macro declare
    public enum Sound_Mode {
        SOUND_MODE_STD(0),
        SOUND_MODE_MUSIC(1),
        SOUND_MODE_NEWS(2),
        SOUND_MODE_THEATER(3),
        SOUND_MODE_USER(4);

        private int val;

        Sound_Mode(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    public enum EQ_Mode {
        EQ_MODE_NORMAL(0),
        EQ_MODE_POP(1),
        EQ_MODE_JAZZ(2),
        EQ_MODE_ROCK(3),
        EQ_MODE_CLASSIC(4),
        EQ_MODE_DANCE(5),
        EQ_MODE_PARTY(6),
        EQ_MODE_BASS(7),
        EQ_MODE_TREBLE(8),
        EQ_MODE_CUSTOM(9);

        private int val;

        EQ_Mode(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    public enum CC_AUD_SPDIF_MODE {
        CC_SPDIF_MODE_PCM(0),
        CC_SPDIF_MODE_SOURCE(1);

        private int val;

        CC_AUD_SPDIF_MODE(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    // Audio Mute

    /**
     * @Function: SetAudioMuteKeyStatus
     * @Description: Set audio mute or unmute according to mute key press up or press down
     * @Param: KeyStatus refer to enum CC_AUDIO_MUTE_KEY_STATUS
     * @Return: 0 success, -1 fail
     */
    public int SetAudioMuteKeyStatus(int KeyStatus) {
        int val[] = new int[]{KeyStatus};
        return sendCmdIntArray(SET_AUDIO_MUTEKEY_STATUS, val);
    }

    /**
     * @Function: GetAudioMuteKeyStatus
     * @Description: Get audio mute or unmute key
     * @Param:
     * @Return: KeyStatus value refer to enum CC_AUDIO_MUTE_KEY_STATUS
     */
    public int GetAudioMuteKeyStatus() {
        return sendCmd(GET_AUDIO_MUTEKEY_STATUS);
    }

    /**
     * @Function: SetAudioForceMuteStatus
     * @Description: Set audio mute or unmute by force
     * @Param: ForceMuteStatus AUDIO_MUTE_ON or AUDIO_MUTE_OFF
     * @Return: 0 success, -1 fail
     */
    public int SetAudioForceMuteStatus(int ForceMuteStatus) {
        int val[] = new int[]{ForceMuteStatus};
        return sendCmdIntArray(SET_AUDIO_FORCE_MUTE_STATUS, val);
    }

    /**
     * @Function: GetAudioForceMuteStatus
     * @Description: Get audio mute status
     * @Param:
     * @Return: AUDIO_MUTE_ON or AUDIO_MUTE_OFF
     */
    public int GetAudioForceMuteStatus() {
        return sendCmd(GET_AUDIO_FORCE_MUTE_STATUS);
    }

    /**
     * @Function: SetAudioAVoutMute
     * @Description: Set av out mute
     * @Param: AvoutMuteStatus AUDIO_MUTE_ON or AUDIO_MUTE_OFF
     * @Return: 0 success, -1 fail
     */
    public int SetAudioAVoutMute(int AvoutMuteStatus) {
        int val[] = new int[]{AvoutMuteStatus};
        return sendCmdIntArray(SET_AUDIO_AVOUT_MUTE_STATUS, val);
    }

    /**
     * @Function: GetAudioAVoutMute
     * @Description: Get av out mute status
     * @Param:
     * @Return: AUDIO_MUTE_ON or AUDIO_MUTE_OFF
     */
    public int GetAudioAVoutMute() {
        return sendCmd(GET_AUDIO_AVOUT_MUTE_STATUS);
    }

    /**
     * @Function: SetAudioSPDIFMute
     * @Description: Set spdif mute
     * @Param: SPDIFMuteStatus AUDIO_MUTE_ON or AUDIO_MUTE_OFF
     * @Return: 0 success, -1 fail
     */
    public int SetAudioSPDIFMute(int SPDIFMuteStatus) {
        int val[] = new int[]{SPDIFMuteStatus};
        return sendCmdIntArray(SET_AUDIO_SPDIF_MUTE_STATUS, val);
    }

    /**
     * @Function: GetAudioSPDIFMute
     * @Description: Get spdif mute status
     * @Param:
     * @Return: spdif mute status AUDIO_MUTE_ON or AUDIO_MUTE_OFF
     */
    public int GetAudioSPDIFMute() {
        return sendCmd(GET_AUDIO_SPDIF_MUTE_STATUS);
    }

    // Audio Master Volume

    /**
     * @Function: SetAudioMasterVolume
     * @Description: Set audio master volume
     * @Param: value between 0 and 100
     * @Return: 0 success, -1 fail
     */
    public int SetAudioMasterVolume(int tmp_vol) {
        int val[] = new int[]{tmp_vol};
        return sendCmdIntArray(SET_AUDIO_MASTER_VOLUME, val);
    }

    /**
     * @Function: GetAudioMasterVolume
     * @Description: Get audio master volume
     * @Param:
     * @Return: value between 0 and 100
     */
    public int GetSaveAudioMasterVolume() {
        return sendCmd(GET_AUDIO_MASTER_VOLUME);
    }

    /**
     * @Function: SaveCurAudioMasterVolume
     * @Description: Save audio master volume(stored in flash)
     * @Param: value between 0 and 100
     * @Return: 0 success, -1 fail
     */
    public int SaveCurAudioMasterVolume(int vol) {
        int val[] = new int[]{vol};
        return sendCmdIntArray(SAVE_CUR_AUDIO_MASTER_VOLUME, val);
    }

    /**
     * @Function: GetCurAudioMasterVolume
     * @Description: Get audio master volume(stored in flash)
     * @Param:
     * @Return: value between 0 and 100
     */
    public int GetCurAudioMasterVolume() {
        return sendCmd(GET_CUR_AUDIO_MASTER_VOLUME);
    }

    // Audio Balance

    /**
     * @Function: SetAudioBalance
     * @Description: Set audio banlance
     * @Param: value between 0 and 100
     * @Return: 0 success, -1 fail
     */
    public int SetAudioBalance(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(SET_AUDIO_BALANCE, val);
    }

    /**
     * @Function: GetAudioBalance
     * @Description: Get audio balance
     * @Param:
     * @Return: value between 0 and 100
     */
    public int GetSaveAudioBalance() {
        return sendCmd(GET_AUDIO_BALANCE);
    }

    /**
     * @Function: SaveCurAudioBalance
     * @Description: Save audio balance(stored in flash)
     * @Param: value between 0 and 100
     * @Return: 0 success, -1 fail
     */
    public int SaveCurAudioBalance(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(SAVE_CUR_AUDIO_BALANCE, val);
    }

    /**
     * @Function: GetCurAudioBalance
     * @Description: Get audio balance(stored in flash)
     * @Param:
     * @Return: value between 0 and 100
     */
    public int GetCurAudioBalance() {
        return sendCmd(GET_CUR_AUDIO_BALANCE);
    }

    // Audio SupperBass Volume

    /**
     * @Function: SetAudioSupperBassVolume
     * @Description: Get audio supperbass volume
     * @Param:
     * @Return: value between 0 and 100
     */
    public int SetAudioSupperBassVolume(int vol) {
        int val[] = new int[]{vol};
        return sendCmdIntArray(SET_AUDIO_SUPPER_BASS_VOLUME, val);
    }

    /**
     * @Function: GetAudioSupperBassVolume
     * @Description: Get audio supperbass volume
     * @Param:
     * @Return: value between 0 and 100
     */
    public int GetSaveAudioSupperBassVolume() {
        return sendCmd(GET_AUDIO_SUPPER_BASS_VOLUME);
    }

    /**
     * @Function: SaveCurAudioSupperBassVolume
     * @Description: Save audio supperbass volume(stored in flash)
     * @Param: value between 0 and 100
     * @Return: 0 success, -1 fail
     */
    public int SaveCurAudioSupperBassVolume(int vol) {
        int val[] = new int[]{vol};
        return sendCmdIntArray(SAVE_CUR_AUDIO_SUPPER_BASS_VOLUME, val);
    }

    /**
     * @Function: GetCurAudioSupperBassVolume
     * @Description: Get audio supperbass volume(stored in flash)
     * @Param:
     * @Return: value between 0 and 100
     */
    public int GetCurAudioSupperBassVolume() {
        return sendCmd(GET_CUR_AUDIO_SUPPER_BASS_VOLUME);
    }

    // Audio SupperBass Switch

    /**
     * @Function: SetAudioSupperBassSwitch
     * @Description: Set audio supperbass switch
     * @Param: value refer to AUDIO_SWITCH_OFF or AUDIO_SWITCH_ON
     * @Return: 0 success, -1 fail
     */
    public int SetAudioSupperBassSwitch(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(SET_AUDIO_SUPPER_BASS_SWITCH, val);
    }

    /**
     * @Function: GetAudioSupperBassSwitch
     * @Description: Get audio supperbass switch
     * @Param:
     * @Return: value refer to AUDIO_SWITCH_OFF or AUDIO_SWITCH_ON
     */
    public int GetSaveAudioSupperBassSwitch() {
        return sendCmd(GET_AUDIO_SUPPER_BASS_SWITCH);
    }

    /**
     * @Function: SaveCurAudioSupperBassSwitch
     * @Description: Save audio supperbass switch(stored in flash)
     * @Param: value refer to AUDIO_SWITCH_OFF or AUDIO_SWITCH_ON
     * @Return: 0 success, -1 fail
     */
    public int SaveCurAudioSupperBassSwitch(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(SAVE_CUR_AUDIO_SUPPER_BASS_SWITCH, val);
    }

    /**
     * @Function: GetCurAudioSupperBassSwitch
     * @Description: Get audio supperbass switch(stored in flash)
     * @Param:
     * @Return: value refer to AUDIO_SWITCH_OFF or AUDIO_SWITCH_ON
     */
    public int GetCurAudioSupperBassSwitch() {
        return sendCmd(GET_CUR_AUDIO_SUPPER_BASS_SWITCH);
    }

    // Audio SRS Surround switch

    /**
     * @Function: SetAudioSrsSurround
     * @Description: Set audio SRS Surround switch
     * @Param: value refer to AUDIO_SWITCH_OFF or AUDIO_SWITCH_ON
     * @Return: 0 success, -1 fail
     */
    public int SetAudioSrsSurround(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(SET_AUDIO_SRS_SURROUND, val);
    }

    /**
     * @Function: GetAudioSrsSurround
     * @Description: Get audio SRS Surround switch
     * @Param:
     * @Return: value refer to AUDIO_SWITCH_OFF or AUDIO_SWITCH_ON
     */
    public int GetSaveAudioSrsSurround() {
        return sendCmd(GET_AUDIO_SRS_SURROUND);
    }

    /**
     * @Function: SaveCurAudioSrsSurround
     * @Description: Save audio SRS Surround switch(stored in flash)
     * @Param: value refer to AUDIO_SWITCH_OFF or AUDIO_SWITCH_ON
     * @Return: 0 success, -1 fail
     */
    public int SaveCurAudioSrsSurround(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(SAVE_CUR_AUDIO_SRS_SURROUND, val);
    }

    /**
     * @Function: GetCurAudioSrsSurround
     * @Description: Get audio SRS Surround switch(stored in flash)
     * @Param:
     * @Return: value refer to AUDIO_SWITCH_OFF or AUDIO_SWITCH_ON
     */
    public int GetCurAudioSrsSurround() {
        return sendCmd(GET_CUR_AUDIO_SRS_SURROUND);
    }

    // Audio SRS Dialog Clarity

    /**
     * @Function: SetAudioSrsDialogClarity
     * @Description: Set audio SRS Dialog Clarity switch
     * @Param: value refer to AUDIO_SWITCH_OFF or AUDIO_SWITCH_ON
     * @Return: 0 success, -1 fail
     */
    public int SetAudioSrsDialogClarity(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(SET_AUDIO_SRS_DIALOG_CLARITY, val);
    }

    /**
     * @Function: GetAudioSrsDialogClarity
     * @Description: Get audio SRS Dialog Clarity switch
     * @Param:
     * @Return: value refer to AUDIO_SWITCH_OFF or AUDIO_SWITCH_ON
     */
    public int GetSaveAudioSrsDialogClarity() {
        return sendCmd(GET_AUDIO_SRS_DIALOG_CLARITY);
    }

    /**
     * @Function: SaveCurAudioSrsDialogClarity
     * @Description: Save audio SRS Dialog Clarity switch(stored in flash)
     * @Param: value refer to AUDIO_SWITCH_OFF or AUDIO_SWITCH_ON
     * @Return: 0 success, -1 fail
     */
    public int SaveCurAudioSrsDialogClarity(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(SAVE_CUR_AUDIO_SRS_DIALOG_CLARITY, val);
    }

    /**
     * @Function: GetCurAudioSrsDialogClarity
     * @Description: Get audio SRS Dialog Clarity switch(stored in flash)
     * @Param:
     * @Return: value refer to AUDIO_SWITCH_OFF or AUDIO_SWITCH_ON
     */
    public int GetCurAudioSrsDialogClarity() {
        return sendCmd(GET_CUR_AUDIO_SRS_DIALOG_CLARITY);
    }

    // Audio SRS Trubass

    /**
     * @Function: SetAudioSrsTruBass
     * @Description: Set audio SRS TruBass switch
     * @Param: value refer to AUDIO_SWITCH_OFF or AUDIO_SWITCH_ON
     * @Return: 0 success, -1 fail
     */
    public int SetAudioSrsTruBass(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(SET_AUDIO_SRS_TRU_BASS, val);
    }

    /**
     * @Function: GetAudioSrsTruBass
     * @Description: Get audio SRS TruBass switch
     * @Param:
     * @Return: value refer to AUDIO_SWITCH_OFF or AUDIO_SWITCH_ON
     */
    public int GetSaveAudioSrsTruBass() {
        return sendCmd(GET_AUDIO_SRS_TRU_BASS);
    }

    /**
     * @Function: SaveCurAudioSrsTruBass
     * @Description: Save audio SRS TruBass switch(stored in flash)
     * @Param: value refer to AUDIO_SWITCH_OFF or AUDIO_SWITCH_ON
     * @Return: 0 success, -1 fail
     */
    public int SaveCurAudioSrsTruBass(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(SAVE_CUR_AUDIO_SRS_TRU_BASS, val);
    }

    /**
     * @Function: GetCurAudioSrsTruBass
     * @Description: Get audio SRS TruBass switch(stored in flash)
     * @Param:
     * @Return: value refer to AUDIO_SWITCH_OFF or AUDIO_SWITCH_ON
     */
    public int GetCurAudioSrsTruBass() {
        return sendCmd(GET_CUR_AUDIO_SRS_TRU_BASS);
    }

    // Audio Bass

    /**
     * @Function: SetAudioBassVolume
     * @Description: Get audio bass volume
     * @Param:
     * @Return: value between 0 and 100
     */
    public int SetAudioBassVolume(int vol) {
        int val[] = new int[]{vol};
        return sendCmdIntArray(SET_AUDIO_BASS_VOLUME, val);
    }

    /**
     * @Function: GetAudioBassVolume
     * @Description: Get audio bass volume
     * @Param:
     * @Return: value between 0 and 100
     */
    public int GetSaveAudioBassVolume() {
        return sendCmd(GET_AUDIO_BASS_VOLUME);
    }

    /**
     * @Function: SaveCurAudioBassVolume
     * @Description: Save audio bass volume(stored in flash)
     * @Param: value between 0 and 100
     * @Return: 0 success, -1 fail
     */
    public int SaveCurAudioBassVolume(int vol) {
        int val[] = new int[]{vol};
        return sendCmdIntArray(SAVE_CUR_AUDIO_BASS_VOLUME, val);
    }

    /**
     * @Function: GetCurAudioBassVolume
     * @Description: Get audio bass volume(stored in flash)
     * @Param:
     * @Return: value between 0 and 100
     */
    public int GetCurAudioBassVolume() {
        return sendCmd(GET_CUR_AUDIO_BASS_VOLUME);
    }

    // Audio Treble

    /**
     * @Function: SetAudioTrebleVolume
     * @Description: Get audio Treble volume
     * @Param:
     * @Return: value between 0 and 100
     */
    public int SetAudioTrebleVolume(int vol) {
        int val[] = new int[]{vol};
        return sendCmdIntArray(SET_AUDIO_TREBLE_VOLUME, val);
    }

    /**
     * @Function: GetAudioTrebleVolume
     * @Description: Get audio Treble volume
     * @Param:
     * @Return: value between 0 and 100
     */
    public int GetSaveAudioTrebleVolume() {
        return sendCmd(GET_AUDIO_TREBLE_VOLUME);
    }

    /**
     * @Function: SaveCurAudioTrebleVolume
     * @Description: Save audio Treble volume(stored in flash)
     * @Param: value between 0 and 100
     * @Return: 0 success, -1 fail
     */
    public int SaveCurAudioTrebleVolume(int vol) {
        int val[] = new int[]{vol};
        return sendCmdIntArray(SAVE_CUR_AUDIO_TREBLE_VOLUME, val);
    }

    /**
     * @Function: GetCurAudioTrebleVolume
     * @Description: Get audio Treble volume(stored in flash)
     * @Param:
     * @Return: value between 0 and 100
     */
    public int GetCurAudioTrebleVolume() {
        return sendCmd(GET_CUR_AUDIO_TREBLE_VOLUME);
    }

    // Audio Sound Mode

    /**
     * @Function: SetAudioSoundMode
     * @Description: Get audio sound mode
     * @Param:
     * @Return: value refer to enum Sound_Mode
     */
    public int SetAudioSoundMode(Sound_Mode tmp_val) {
        int val[] = new int[]{tmp_val.toInt()};
        return sendCmdIntArray(SET_AUDIO_SOUND_MODE, val);
    }

    /**
     * @Function: GetAudioSoundMode
     * @Description: Get audio sound mode
     * @Param:
     * @Return: value refer to enum Sound_Mode
     */
    public int GetSaveAudioSoundMode() {
        return sendCmd(GET_AUDIO_SOUND_MODE);
    }

    /**
     * @Function: SaveCurAudioSoundMode
     * @Description: Save audio sound mode(stored in flash)
     * @Param: value refer to enum Sound_Mode
     * @Return: 0 success, -1 fail
     */
    public int SaveCurAudioSoundMode(int tmp_val) {
        int val[] = new int[]{tmp_val};
        return sendCmdIntArray(SAVE_CUR_AUDIO_SOUND_MODE, val);
    }

    /**
     * @Function: GetCurAudioSoundMode
     * @Description: Get audio sound mode(stored in flash)
     * @Param:
     * @Return: value refer to enum Sound_Mode
     */
    public int GetCurAudioSoundMode() {
        return sendCmd(GET_CUR_AUDIO_SOUND_MODE);
    }

    // Audio Wall Effect
    /**
     * @Function: SetAudioWallEffect
     * @Description: Set audio Wall Effect switch
     * @Param: value refer to AUDIO_SWITCH_OFF or AUDIO_SWITCH_ON
     * @Return: 0 success, -1 fail
     */
    public int SetAudioWallEffect(int tmp_val) {
        int val[] = new int[]{tmp_val};
        return sendCmdIntArray(SET_AUDIO_WALL_EFFECT, val);
    }

    /**
     * @Function: GetAudioWallEffect
     * @Description: Get audio Wall Effect switch
     * @Param:
     * @Return: value refer to AUDIO_SWITCH_OFF or AUDIO_SWITCH_ON
     */
    public int GetSaveAudioWallEffect() {
        return sendCmd(GET_AUDIO_WALL_EFFECT);
    }

    /**
     * @Function: SaveCurAudioWallEffect
     * @Description: Save audio Wall Effect switch(stored in flash)
     * @Param: value refer to AUDIO_SWITCH_OFF or AUDIO_SWITCH_ON
     * @Return: 0 success, -1 fail
     */
    public int SaveCurAudioWallEffect(int tmp_val) {
        int val[] = new int[]{tmp_val};
        return sendCmdIntArray(SAVE_CUR_AUDIO_WALL_EFFECT, val);
    }

    /**
     * @Function: GetCurAudioWallEffect
     * @Description: Get audio Wall Effect switch(stored in flash)
     * @Param:
     * @Return: value refer to AUDIO_SWITCH_OFF or AUDIO_SWITCH_ON
     */
    public int GetCurAudioWallEffect() {
        return sendCmd(GET_CUR_AUDIO_WALL_EFFECT);
    }

    // Audio EQ Mode
    /**
     * @Function: SetAudioEQMode
     * @Description: Set audio EQ Mode
     * @Param: value refer to enum EQ_Mode
     * @Return: 0 success, -1 fail
     */
    public int SetAudioEQMode(EQ_Mode tmp_val) {
        int val[] = new int[]{tmp_val.toInt()};
        return sendCmdIntArray(SET_AUDIO_EQ_MODE, val);
    }

    /**
     * @Function: GetAudioEQMode
     * @Description: Get audio EQ Mode
     * @Param:
     * @Return: value refer to enum EQ_Mode
     */
    public int GetSaveAudioEQMode() {
        return sendCmd(GET_AUDIO_EQ_MODE);
    }

    /**
     * @Function: SaveCurAudioEQMode
     * @Description: Save audio EQ Mode(stored in flash)
     * @Param: value refer to enum EQ_Mode
     * @Return: 0 success, -1 fail
     */
    public int SaveCurAudioEQMode(int tmp_val) {
        int val[] = new int[]{tmp_val};
        return sendCmdIntArray(SAVE_CUR_AUDIO_EQ_MODE, val);
    }

    /**
     * @Function: GetCurAudioEQMode
     * @Description: Get audio EQ Mode(stored in flash)
     * @Param:
     * @Return: value refer to enum EQ_Mode
     */
    public int GetCurAudioEQMode() {
        return sendCmd(GET_CUR_AUDIO_EQ_MODE);
    }

    // Audio EQ Gain
    /**
     * @Function: GetAudioEQRange
     * @Description: Get audio EQ Range
     * @Param:
     * @Return: value -128~127
     */
    public int GetAudioEQRange(int range_buf[]) {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(GET_AUDIO_EQ_RANGE);
        sendCmdToTv(cmd, r);
        range_buf[0] = r.readInt();
        range_buf[1] = r.readInt();
        int ret = r.readInt();
        return ret;
    }

    /**
     * @Function: GetAudioEQBandCount
     * @Description: Get audio EQ band count
     * @Param:
     * @Return: value 0~255
     */
    public int GetAudioEQBandCount() {
        return sendCmd(GET_AUDIO_EQ_BAND_COUNT);
    }

    /**
     * @Function: SetAudioEQGain
     * @Description: Set audio EQ Gain
     * @Param: value buffer of eq gain. (range --- get by GetAudioEQRange function)
     * @Return: 0 success, -1 fail
     */
    public int SetAudioEQGain(int gain_buf[]) {
        return sendCmdIntArray(SET_AUDIO_EQ_GAIN, gain_buf);
    }

    /**
     * @Function: GetAudioEQGain
     * @Description: Get audio EQ gain
     * @Param: value buffer of eq gain. (range --- get by GetAudioEQRange function)
     * @Return: 0 success, -1 fail
     */
    public int GetAudioEQGain(int gain_buf[]) {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(GET_AUDIO_EQ_GAIN);
        sendCmdToTv(cmd, r);

        int size = r.readInt();
        for (int i = 0; i < size; i++) {
            gain_buf[i] = r.readInt();
        }

        return r.readInt();
    }

    /**
     * @Function: SaveCurAudioEQGain
     * @Description: Get audio EQ Gain(stored in flash)
     * @Param: value buffer of eq gain. (range --- get by GetAudioEQRange function)
     * @Return: 0 success, -1 fail
     */
    public int SaveCurAudioEQGain(int gain_buf[]) {
        return sendCmdIntArray(SAVE_CUR_AUDIO_EQ_GAIN, gain_buf);
    }

    /**
     * @Function: GetCurEQGain
     * @Description: Save audio EQ Gain(stored in flash)
     * @Param: value buffer of eq gain. (range --- get by GetAudioEQRange function)
     * @Return: 0 success, -1 fail
     */
    public int GetCurEQGain(int gain_buf[]) {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(GET_CUR_EQ_GAIN);
        sendCmdToTv(cmd, r);

        int size = r.readInt();
        for (int i = 0; i < size; i++) {
            gain_buf[i] = r.readInt();
        }

        return r.readInt();
    }

    /**
     * @Function: SetAudioEQSwitch
     * @Description: Set audio EQ switch
     * @Param: value refer to AUDIO_SWITCH_OFF or AUDIO_SWITCH_ON
     * @Return: 0 success, -1 fail
     */
    public int SetAudioEQSwitch(int tmp_val) {
        int val[] = new int[]{tmp_val};
        return sendCmdIntArray(SET_AUDIO_EQ_SWITCH, val);
    }

    //Audio SPDIF Switch
    /**
     * @Function: SetAudioSPDIFSwitch
     * @Description: Set audio SPDIF Switch
     * @Param: value refer to AUDIO_SWITCH_OFF or AUDIO_SWITCH_ON
     * @Return: 0 success, -1 fail
     */
    public int SetAudioSPDIFSwitch(int tmp_val) {
        int val[] = new int[]{tmp_val};
        return sendCmdIntArray(SET_AUDIO_SPDIF_SWITCH, val);
    }

    /**
     * @Function: GetAudioSPDIFSwitch
     * @Description: Get audio SPDIF Switch
     * @Param:
     * @Return: value refer to AUDIO_SWITCH_OFF or AUDIO_SWITCH_ON
     */
    public int GetSaveAudioSPDIFSwitch() {
        return sendCmd(GET_AUDIO_SPDIF_SWITCH);
    }

    /**
     * @Function: SaveCurAudioSPDIFSwitch
     * @Description: Save audio SPDIF Switch(stored in flash)
     * @Param: value refer to AUDIO_SWITCH_OFF or AUDIO_SWITCH_ON
     * @Return: 0 success, -1 fail
     */
    public int SaveCurAudioSPDIFSwitch(int tmp_val) {
        int val[] = new int[]{tmp_val};
        return sendCmdIntArray(SAVE_CUR_AUDIO_SPDIF_SWITCH, val);
    }

    /**
     * @Function: GetCurAudioSPDIFSwitch
     * @Description: Get audio SPDIF Switch(stored in flash)
     * @Param:
     * @Return: value refer to AUDIO_SWITCH_OFF or AUDIO_SWITCH_ON
     */
    public int GetCurAudioSPDIFSwitch() {
        return sendCmd(GET_CUR_AUDIO_SPDIF_SWITCH);
    }

    //Audio SPDIF Mode
    /**
     * @Function: SetAudioSPDIFMode
     * @Description: Set audio SPDIF Mode
     * @Param: value refer to enum CC_AUD_SPDIF_MODE
     * @Return: 0 success, -1 fail
     */
    public int SetAudioSPDIFMode(int tmp_val) {
        int val[] = new int[]{tmp_val};
        return sendCmdIntArray(SET_AUDIO_SPDIF_MODE, val);
    }

    /**
     * @Function: GetAudioSPDIFMode
     * @Description: Get audio SPDIF Mode
     * @Param:
     * @Return: value refer to enum CC_AUD_SPDIF_MODE
     */
    public int GetSaveAudioSPDIFMode() {
        return sendCmd(GET_AUDIO_SPDIF_MODE);
    }

    /**
     * @Function: SaveCurAudioSPDIFMode
     * @Description: Save audio SPDIF Mode(stored in flash)
     * @Param: value refer to enum CC_AUD_SPDIF_MODE
     * @Return: 0 success, -1 fail
     */
    public int SaveCurAudioSPDIFMode(int tmp_val) {
        int val[] = new int[]{tmp_val};
        return sendCmdIntArray(SAVE_CUR_AUDIO_SPDIF_MODE, val);
    }

    /**
     * @Function: GetCurAudioSPDIFMode
     * @Description: Get audio SPDIF Mode(stored in flash)
     * @Param:
     * @Return: value refer to enum CC_AUD_SPDIF_MODE
     */
    public int GetCurAudioSPDIFMode() {
        return sendCmd(GET_CUR_AUDIO_SPDIF_MODE);
    }

    /**
     * @Function: OpenAmAudio
     * @Description: Open amaudio module
     * @Param: sr, input sample rate
     * @Return: 0 success, -1 fail
     */
    public int OpenAmAudio(int sr) {
        int val[] = new int[]{sr};
        return sendCmdIntArray(OPEN_AMAUDIO, val);
    }

    /**
     * @Function: CloseAmAudio
     * @Description: Close amaudio module
     * @Param:
     * @Return: 0 success, -1 fail
     */
    public int CloseAmAudio() {
        return sendCmd(CLOSE_AMAUDIO);
    }

    /**
     * @Function: SetAmAudioInputSr
     * @Description: set amaudio input sample rate
     * @Param: sr, input sample rate
     * @Return: 0 success, -1 fail
     */
    public int SetAmAudioInputSr(int sr) {
        int val[] = new int[]{sr};
        return sendCmdIntArray(SET_AMAUDIO_INPUT_SR, val);
    }

    /**
     * @Function: SetAmAudioOutputMode
     * @Description: set amaudio output mode
     * @Param: mode, amaudio output mode
     * @Return: 0 success, -1 fail
     */
    public int SetAmAudioOutputMode(int mode) {
        int val[] = new int[]{mode};
        return sendCmdIntArray(SET_AMAUDIO_OUTPUT_MODE, val);
    }

    /**
     * @Function: SetAmAudioMusicGain
     * @Description: set amaudio music gain
     * @Param: gain, gain value
     * @Return: 0 success, -1 fail
     */
    public int SetAmAudioMusicGain(int gain) {
        int val[] = new int[]{gain};
        return sendCmdIntArray(SET_AMAUDIO_MUSIC_GAIN, val);
    }

    /**
     * @Function: SetAmAudioLeftGain
     * @Description: set amaudio left gain
     * @Param: gain, gain value
     * @Return: 0 success, -1 fail
     */
    public int SetAmAudioLeftGain(int gain) {
        int val[] = new int[]{gain};
        return sendCmdIntArray(SET_AMAUDIO_LEFT_GAIN, val);
    }

    /**
     * @Function: SetAmAudioRightGain
     * @Description: set amaudio right gain
     * @Param: gain, gain value
     * @Return: 0 success, -1 fail
     */
    public int SetAmAudioRightGain(int gain) {
        int val[] = new int[]{gain};
        return sendCmdIntArray(SET_AMAUDIO_RIGHT_GAIN, val);
    }

    /**
     * @Function: AudioHandleHeadsetPlugIn
     * @Description: Audio Handle Headset PlugIn
     * @Param:
     * @Return: 0 success, -1 fail
     */
    public int AudioHandleHeadsetPlugIn(int tmp_val) {
        return sendCmd(HANDLE_AUDIO_HEADSET_PLUG_IN);
    }

    /**
     * @Function: AudioHandleHeadsetPullOut
     * @Description: Audio Handle Headset PullOut
     * @Param:
     * @Return: 0 success, -1 fail
     */
    public int AudioHandleHeadsetPullOut(int tmp_val) {
        return sendCmd(HANDLE_AUDIO_HEADSET_PULL_OUT);
    }

    /**
     * @Function: SetCurProgVolumeCompesition
     * @Description: SET Audio Volume Compesition
     * @Param: 0~10
     * @Return: 0 success, -1 fail
     */
    public int SetCurProgVolumeCompesition(int tmp_val) {
        int val[] = new int[]{tmp_val};
        return sendCmdIntArray(SET_AUDIO_VOL_COMP, val);
    }

    /**
     * empty api--------------------------------
     * @Function: ATVSaveVolumeCompesition
     * @Description: ATV SAVE Audio Volume Compesition
     * @Param: 0~10
     * @Return: 0 success, -1 fail
     */
    public int ATVSaveVolumeCompesition(int tmp_val) {
        int val[] = new int[]{tmp_val};
        return sendCmdIntArray(SAVE_AUDIO_VOL_COMP, val);
    }

    /**
     * @Function: ATVGetVolumeCompesition
     * @Description: Audio Handle Headset PullOut
     * @Param:
     * @Return: 0~10
     */
    public int GetVolumeCompesition() {
        return sendCmd(GET_AUDIO_VOL_COMP);
    }

    /**
     * @Function: SelectLineInChannel
     * @Description: select line in channel
     * @Param: value 0~7
     * @Return: 0 success, -1 fail
     */
    public int SelectLineInChannel(int channel) {
        int val[] = new int[]{channel};
        return sendCmdIntArray(SELECT_LINE_IN_CHANNEL, val);
    }

    /**
     * @Function: SetLineInCaptureVolume
     * @Description: set line in capture volume
     * @Param: left chanel volume(0~84)  right chanel volume(0~84)
     * @Return: 0 success, -1 fail
     */
    public int SetLineInCaptureVolume(int l_vol, int r_vol) {
        int val[] = new int[]{l_vol, r_vol};
        return sendCmdIntArray(SET_LINE_IN_CAPTURE_VOL, val);
    }

    /**
     * @Function: SetNoiseGateThreshold
     * @Description: set noise gate threshold
     * @Param: value (0~255)
     * @Return: 0 success, -1 fail
     */
    public void SetNoiseGateThreshold(int thresh) {
        int val[] = new int[]{thresh};
        sendCmdIntArray(SET_NOISE_GATE_THRESHOLD, val);
    }
    // AUDIO END

    // SSM
    /**
     * @Function: SSMInitDevice
     * @Description: Init ssm device
     * @Param:
     * @Return: 0 success, -1 fail
     */
    public int SSMInitDevice() {
        return sendCmd(SSM_INIT_DEVICE);
    }

    /**
     * @Function: SSMWriteOneByte
     * @Description: Write one byte to ssm
     * @Param: offset pos in ssm for this byte, val one byte value
     * @Return: 0 success, -1 fail
     */
    public int SSMWriteOneByte(int offset, int value) {
        int val[] = new int[]{offset, value};
        return sendCmdIntArray(SSM_SAVE_ONE_BYTE, val);
    }

    /**
     * @Function: SSMReadOneByte
     * @Description: Read one byte from ssm
     * @Param: offset pos in ssm for this byte to read
     * @Return: one byte read value
     */
    public int SSMReadOneByte(int offset) {
        int val[] = new int[]{offset};
        return sendCmdIntArray(SSM_READ_ONE_BYTE, val);
    }

    /**
     * @Function: SSMWriteNByte
     * @Description: Write n bytes to ssm
     * @Param: offset pos in ssm for the bytes, data_len how many bytes, data_buf n bytes write buffer
     * @Return: 0 success, -1 fail
     */
    public int SSMWriteNBytes(int offset, int data_len, int data_buf[]) {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();

        cmd.writeInt(SSM_SAVE_N_BYTES);
        cmd.writeInt(offset);
        cmd.writeInt(data_len);
        for (int i = 0; i < data_len; i++) {
            cmd.writeInt(data_buf[i]);
        }

        sendCmdToTv(cmd, r);
        return r.readInt();
    }

    /**
     * @Function: SSMReadNByte
     * @Description: Read one byte from ssm
     * @Param: offset pos in ssm for the bytes, data_len how many bytes, data_buf n bytes read buffer
     * @Return: 0 success, -1 fail
     */
    public int SSMReadNBytes(int offset, int data_len, int data_buf[]) {
        libtv_log_open();
        int i = 0, tmp_data_len = 0, ret = 0;
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();

        cmd.writeInt(SSM_READ_N_BYTES);
        cmd.writeInt(offset);
        cmd.writeInt(data_len);

        sendCmdToTv(cmd, r);

        data_len = r.readInt();
        for (i = 0; i < data_len; i++) {
            data_buf[i] = r.readInt();
        }

        ret = r.readInt();
        return ret;
    }

    /**
     * @Function: SSMSavePowerOnOffChannel
     * @Description: Save power on off channel num to ssm for last channel play
     * @Param: channel_type last channel value refer to enum POWERON_SOURCE_TYPE
     * @Return: 0 success, -1 fail
     */
    public int SSMSavePowerOnOffChannel(int channel_type) {
        int val[] = new int[]{channel_type};
        return sendCmdIntArray(SSM_SAVE_POWER_ON_OFF_CHANNEL, val);
    }

    /**
     * @Function: SSMReadPowerOnOffChannel
     * @Description: Read last channel num from ssm
     * @Param:
     * @Return: last channel num
     */
    public int SSMReadPowerOnOffChannel() {
        return sendCmd(SSM_READ_POWER_ON_OFF_CHANNEL);
    }

    /**
     * @Function: SSMSaveSourceInput
     * @Description: Save current source input to ssm for power on last source select
     * @Param: source_input refer to enum SourceInput.
     * @Return: 0 success, -1 fail
     */
    public int SSMSaveSourceInput(int source_input) {
        int val[] = new int[]{source_input};
        return sendCmdIntArray(SSM_SAVE_SOURCE_INPUT, val);
    }

    /**
     * @Function: SSMReadSourceInput
     * @Description: Read last source input from ssm
     * @Param:
     * @Return: source input value refer to enum SourceInput
     */
    public int SSMReadSourceInput() {
        return sendCmd(SSM_READ_SOURCE_INPUT);
    }

    /**
     * @Function: SSMSaveLastSelectSourceInput
     * @Description: Save last source input to ssm for power on last source select
     * @Param: source_input refer to enum SourceInput, if you wanna save as last source input, just set it as SourceInput.DUMMY.
     * @Return: 0 success, -1 fail
     */
    public int SSMSaveLastSelectSourceInput(int source_input) {
        int val[] = new int[]{source_input};
        return sendCmdIntArray(SSM_SAVE_LAST_SOURCE_INPUT, val);
    }

    /**
     * @Function: SSMReadLastSelectSourceInput
     * @Description: Read last source input from ssm
     * @Param:
     * @Return: source input value refer to enum SourceInput
     */
    public int SSMReadLastSelectSourceInput() {
        return sendCmd(SSM_READ_LAST_SOURCE_INPUT);
    }

    /**
     * @Function: SSMSaveSystemLanguage
     * @Description: Save system language
     * @Param: tmp_val language id
     * @Return: 0 success, -1 fail
     */
    public int SSMSaveSystemLanguage(int tmp_val) {
        int val[] = new int[]{tmp_val};
        return sendCmdIntArray(SSM_SAVE_SYS_LANGUAGE, val);
    }

    /**
     * @Function: SSMReadSystemLanguage
     * @Description: Read last source input from ssm
     * @Param:
     * @Return: language id value
     */
    public int SSMReadSystemLanguage() {
        return sendCmd(SSM_READ_SYS_LANGUAGE);
    }

    public int SSMSaveAgingMode(int tmp_val) {
        int val[] = new int[]{tmp_val};
        return sendCmdIntArray(SSM_SAVE_AGING_MODE, val);
    }

    public int SSMReadAgingMode() {
        return sendCmd(SSM_READ_AGING_MODE);
    }

    /**
     * @Function: SSMSavePanelType
     * @Description: Save panle type for multi-panel select
     * @Param: tmp_val panel type id
     * @Return: 0 success, -1 fail
     */
    public int SSMSavePanelType(int tmp_val) {
        int val[] = new int[]{tmp_val};
        return sendCmdIntArray(SSM_SAVE_PANEL_TYPE, val);
    }

    /**
     * @Function: SSMReadPanelType
     * @Description: Read panle type id
     * @Param:
     * @Return: panel type id
     */
    public int SSMReadPanelType() {
        return sendCmd(SSM_READ_PANEL_TYPE);
    }

    /**
     * @Function: SSMSaveMacAddress
     * @Description: Save mac address
     * @Param: data_buf write buffer for mac address
     * @Return: 0 success, -1 fail
     */
    public int SSMSaveMacAddress(int data_buf[]) {
        libtv_log_open();
        int i = 0, tmp_buf_size = 0, ret = 0;
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(SSM_SAVE_MAC_ADDR);

        tmp_buf_size = data_buf.length;
        cmd.writeInt(tmp_buf_size);
        for (i = 0; i < tmp_buf_size; i++) {
            cmd.writeInt(data_buf[i]);
        }

        sendCmdToTv(cmd, r);
        ret = r.readInt();
        return ret;
    }

    /**
     * @Function: SSMReadMacAddress
     * @Description: Read mac address
     * @Param: data_buf read buffer for mac address
     * @Return: 0 success, -1 fail
     */
    public int SSMReadMacAddress(int data_buf[]) {
        libtv_log_open();
        int i = 0, tmp_buf_size = 0, ret = 0;
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(SSM_READ_MAC_ADDR);
        sendCmdToTv(cmd, r);

        tmp_buf_size = r.readInt();

        for (i = 0; i < tmp_buf_size; i++) {
            data_buf[i] = r.readInt();
        }

        ret = r.readInt();
        return ret;
    }

    /**
     * @Function: SSMSaveBarCode
     * @Description: Save bar code
     * @Param: data_buf write buffer for bar code
     * @Return: 0 success, -1 fail
     */
    public int SSMSaveBarCode(int data_buf[]) {
        libtv_log_open();
        int i = 0, tmp_buf_size = 0, ret = 0;
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(SSM_SAVE_BAR_CODE);

        tmp_buf_size = data_buf.length;
        cmd.writeInt(tmp_buf_size);
        for (i = 0; i < tmp_buf_size; i++) {
            cmd.writeInt(data_buf[i]);
        }

        sendCmdToTv(cmd, r);
        ret = r.readInt();
        return ret;
    }

    /**
     * @Function: SSMReadBarCode
     * @Description: Read bar code
     * @Param: data_buf read buffer for bar code
     * @Return: 0 success, -1 fail
     */
    public int SSMReadBarCode(int data_buf[]) {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(SSM_READ_BAR_CODE);
        sendCmdToTv(cmd, r);

        int size = r.readInt();
        for (int i = 0; i < size; i++) {
            data_buf[i] = r.readInt();
        }

        return r.readInt();
    }

    /**
     * @Function: SSMSavePowerOnMusicSwitch
     * @Description: Save power on music on/off flag
     * @Param: tmp_val on off flag
     * @Return: 0 success, -1 fail
     */
    public int SSMSavePowerOnMusicSwitch(int tmp_val) {
        int val[] = new int[]{tmp_val};
        return sendCmdIntArray(SSM_SAVE_POWER_ON_MUSIC_SWITCH, val);
    }

    /**
     * @Function: SSMReadPowerOnMusicSwitch
     * @Description: Read power on music on/off flag
     * @Param:
     * @Return: on off flag
     */
    public int SSMReadPowerOnMusicSwitch() {
        return sendCmd(SSM_READ_POWER_ON_MUSIC_SWITCH);
    }

    /**
     * @Function: SSMSavePowerOnMusicVolume
     * @Description: Save power on music volume value
     * @Param: tmp_val volume value
     * @Return: 0 success, -1 fail
     */
    public int SSMSavePowerOnMusicVolume(int tmp_val) {
        int val[] = new int[]{tmp_val};
        return sendCmdIntArray(SSM_SAVE_POWER_ON_MUSIC_VOL, val);
    }

    /**
     * @Function: SSMReadPowerOnMusicVolume
     * @Description: Read power on music volume value
     * @Param:
     * @Return: volume value
     */
    public int SSMReadPowerOnMusicVolume() {
        return sendCmd(SSM_READ_POWER_ON_MUSIC_VOL);
    }

    /**
     * @Function: SSMSaveSystemSleepTimer
     * @Description: Save system sleep timer value
     * @Param: tmp_val sleep timer value
     * @Return: 0 success, -1 fail
     */
    public int SSMSaveSystemSleepTimer(int tmp_val) {
        int val[] = new int[]{tmp_val};
        return sendCmdIntArray(SSM_SAVE_SYS_SLEEP_TIMER, val);
    }

    /**
     * @Function: SSMReadSystemSleepTimer
     * @Description: Read system sleep timer value
     * @Param:
     * @Return: volume value
     */
    public int SSMReadSystemSleepTimer() {
        return sendCmd(SSM_READ_SYS_SLEEP_TIMER);
    }

    /**
     * @Function: SSMSetBusStatus
     * @Description: Set i2c bus status
     * @Param: tmp_val bus status value
     * @Return: 0 success, -1 fail
     */
    public int SSMSetBusStatus(int tmp_val) {
        int val[] = new int[]{tmp_val};
        return sendCmdIntArray(SSM_SET_BUS_STATUS, val);
    }

    /**
     * @Function: SSMGetBusStatus
     * @Description: Get i2c bus status value
     * @Param:
     * @Return: status value
     */
    public int SSMGetBusStatus() {
        return sendCmd(SSM_GET_BUS_STATUS);
    }

    /**
     * @Function: SSMSaveInputSourceParentalControl
     * @Description: Save parental control flag to corresponding source input
     * @Param: source_input refer to enum SourceInput, ctl_flag enable or disable this source input
     * @Return: 0 success, -1 fail
     */
    public int SSMSaveInputSourceParentalControl(int source_input, int ctl_flag) {
        int val[] = new int[]{source_input, ctl_flag};
        return sendCmdIntArray(SSM_SAVE_INPUT_SRC_PARENTAL_CTL, val);
    }

    /**
     * @Function: SSMReadInputSourceParentalControl
     * @Description: Read parental control flag of corresponding source input
     * @Param: source_input refer to enum SourceInput
     * @Return: parental control flag
     */
    public int SSMReadInputSourceParentalControl(int source_input) {
        int val[] = new int[]{source_input};
        return sendCmdIntArray(SSM_READ_INPUT_SRC_PARENTAL_CTL, val);
    }

    /**
     * @Function: SSMSaveInputSourceParentalControl
     * @Description: Save parental control on off flag
     * @Param: switch_flag on off flag
     * @Return: 0 success, -1 fail
     */
    public int SSMSaveParentalControlSwitch(int switch_flag) {
        int val[] = new int[]{switch_flag};
        return sendCmdIntArray(SSM_SAVE_PARENTAL_CTL_SWITCH, val);
    }

    /**
     * @Function: SSMReadParentalControlSwitch
     * @Description: Read parental control on off flag
     * @Param:
     * @Return: on off flag
     */
    public int SSMReadParentalControlSwitch() {
        return sendCmd(SSM_READ_PARENTAL_CTL_SWITCH);
    }

    /**
     * @Function: SSMSaveParentalControlPassWord
     * @Description: Save parental control password
     * @Param: pass_wd_str password string
     * @Return: 0 success, -1 fail
     */
    public int SSMSaveParentalControlPassWord(String pass_wd_str) {
        String val[] = new String[]{pass_wd_str};
        return sendCmdStringArray(SSM_SAVE_PARENTAL_CTL_PASS_WORD, val);
    }

    /**
     * @Function: SSMReadParentalControlPassWord
     * @Description: Read parental control password
     * @Param:
     * @Return: password string
     */
    public String SSMReadParentalControlPassWord() {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(SSM_READ_PARENTAL_CTL_PASS_WORD);
        sendCmdToTv(cmd, r);
        return r.readString();
    }

    /**
     * @Function: SSMSaveUsingDefaultHDCPKeyFlag
     * @Description: Save use default HDCP key flag
     * @Param: switch_flag enable or disable default key
     * @Return: 0 success, -1 fail
     */
    public int SSMSaveUsingDefaultHDCPKeyFlag(int switch_flag) {
        int val[] = new int[]{switch_flag};
        return sendCmdIntArray(SSM_SAVE_USING_DEF_HDCP_KEY_FLAG, val);
    }

    /**
     * @Function: SSMReadUsingDefaultHDCPKeyFlag
     * @Description: Read use default HDCP key flag
     * @Param:
     * @Return: use flag
     */
    public int SSMReadUsingDefaultHDCPKeyFlag() {
        return sendCmd(SSM_READ_USING_DEF_HDCP_KEY_FLAG);
    }

    /**
     * @Function: SSMGetCustomerDataStart
     * @Description: Get ssm customer data segment start pos
     * @Param:
     * @Return: start offset pos in ssm data segment
     */
    public int SSMGetCustomerDataStart() {
        return sendCmd(SSM_GET_CUSTOMER_DATA_START);
    }

    /**
     * @Function: SSMGetCustomerDataLen
     * @Description: Get ssm customer data segment length
     * @Param:
     * @Return: length
     */
    public int SSMGetCustomerDataLen() {
        return sendCmd(SSM_GET_CUSTOMER_DATA_LEN);
    }

    /**
     * @Function: SSMSaveStandbyMode
     * @Description: Save standby mode, suspend/resume mode or reboot mode
     * @Param: flag standby mode flag
     * @Return: 0 success, -1 fail
     */
    public int SSMSaveStandbyMode(int flag) {
        int val[] = new int[]{flag};
        return sendCmdIntArray(SSM_SAVE_STANDBY_MODE, val);
    }

    /**
     * @Function: SSMReadStandbyMode
     * @Description: Read standby mode, suspend/resume mode or reboot mode
     * @Param:
     * @Return: standby mode flag
     */
    public int SSMReadStandbyMode() {
        return sendCmd(SSM_READ_STANDBY_MODE);
    }

    /**
     * @Function: SSMSaveLogoOnOffFlag
     * @Description: Save standby logo on off flag
     * @Param: flag on off
     * @Return: 0 success, -1 fail
     */
    public int SSMSaveLogoOnOffFlag(int flag) {
        int val[] = new int[]{flag};
        return sendCmdIntArray(SSM_SAVE_LOGO_ON_OFF_FLAG, val);
    }

    /**
     * @Function: SSMReadStandbyMode
     * @Description: Read standby logo on off flag
     * @Param:
     * @Return: on off flag
     */
    public int SSMReadLogoOnOffFlag() {
        return sendCmd(SSM_READ_LOGO_ON_OFF_FLAG);
    }

    /**
     * @Function: SSMSaveHDMIEQMode
     * @Description: Save hdmi eq mode
     * @Param: flag eq mode
     * @Return: 0 success, -1 fail
     */
    public int SSMSaveHDMIEQMode(int flag) {
        int val[] = new int[]{flag};
        return sendCmdIntArray(SSM_SAVE_HDMIEQ_MODE, val);
    }

    /**
     * @Function: SSMReadHDMIEQMode
     * @Description: Read hdmi eq mode
     * @Param:
     * @Return: hdmi eq mode
     */
    public int SSMReadHDMIEQMode() {
        return sendCmd(SSM_READ_HDMIEQ_MODE);
    }

    /**
     * @Function: SSMSaveHDMIInternalMode
     * @Description: Save hdmi internal mode
     * @Param: flag internal mode
     * @Return: 0 success, -1 fail
     */
    public int SSMSaveHDMIInternalMode(int flag) {
        int val[] = new int[]{flag};
        return sendCmdIntArray(SSM_SAVE_HDMIINTERNAL_MODE, val);
    }

    /**
     * @Function: SSMReadHDMIInternalMode
     * @Description: Read hdmi internal mode
     * @Param:
     * @Return: hdmi internal mode
     */
    public int SSMReadHDMIInternalMode() {
        return sendCmd(SSM_READ_HDMIINTERNAL_MODE);
    }

    /**
     * @Function: SSMSaveDisable3D
     * @Description: Save disable 3D flag
     * @Param: flag 3d disable flag
     * @Return: 0 success, -1 fail
     */
    public int SSMSaveDisable3D(int flag) {
        int val[] = new int[]{flag};
        return sendCmdIntArray(SSM_SAVE_DISABLE_3D, val);
    }

    /**
     * @Function: SSMReadDisable3D
     * @Description: Read disable 3D flag
     * @Param:
     * @Return: disable flag
     */
    public int SSMReadDisable3D() {
        return sendCmd(SSM_READ_DISABLE_3D);
    }

    /**
     * @Function: SSMSaveGlobalOgoEnable
     * @Description: Save enable global ogo flag
     * @Param: flag enable flag
     * @Return: 0 success, -1 fail
     */
    public int SSMSaveGlobalOgoEnable(int enable) {
        int val[] = new int[]{enable};
        return sendCmdIntArray(SSM_SAVE_GLOBAL_OGOENABLE, val);
    }

    /**
     * @Function: SSMReadDisable3D
     * @Description: Read enable global ogo flag
     * @Param:
     * @Return: enable flag
     */
    public int SSMReadGlobalOgoEnable() {
        return sendCmd(SSM_READ_GLOBAL_OGOENABLE);
    }

    /**
     * @Function: SSMSaveAdbSwitchStatus
     * @Description: Save adb debug enable flag
     * @Param: flag enable flag
     * @Return: 0 success, -1 fail
     */
    public int SSMSaveAdbSwitchStatus(int flag) {
        int val[] = new int[]{flag};
        return sendCmdIntArray(SSM_SAVE_ADB_SWITCH_STATUS, val);
    }

    /**
     * @Function: SSMSaveSerialCMDSwitchValue
     * @Description: Save serial cmd switch value
     * @Param:
     * @Return: 0 success, -1 fail
     */
    public int SSMSaveSerialCMDSwitchValue(int switch_val) {
        int val[] = new int[]{switch_val};
        return sendCmdIntArray(SSM_SAVE_SERIAL_CMD_SWITCH_STATUS, val);
    }

    /**
     * @Function: SSMReadSerialCMDSwitchValue
     * @Description: Save serial cmd switch value
     * @Param:
     * @Return: enable flag
     */
    public int SSMReadSerialCMDSwitchValue() {
        return sendCmd(SSM_READ_SERIAL_CMD_SWITCH_STATUS);
    }

    /**
     * @Function: SSMSetHDCPKey
     * @Description: Save hdmi hdcp key
     * @Param:
     * @Return: 0 success, -1 fail
     */
    public int SSMSetHDCPKey() {
        return sendCmd(SSM_SET_HDCP_KEY);
    }

    /**
     * @Function: SSMRefreshHDCPKey
     * @Description: Refresh hdmi hdcp key after burn
     * @Param:
     * @Return: 0 success, -1 fail
     */
    public int SSMRefreshHDCPKey() {
        return sendCmd(SSM_REFRESH_HDCPKEY);
    }

    /**
     * @Function: SSMSaveChromaStatus
     * @Description: Save chroma status
     * @Param: flag chroma status on off
     * @Return: 0 success, -1 fail
     */
    public int SSMSaveChromaStatus(int flag) {
        int val[] = new int[]{flag};
        return sendCmdIntArray(SSM_SAVE_CHROMA_STATUS, val);
    }

    /**
     * @Function: SSMSaveCABufferSize
     * @Description: Save dtv ca buffer size
     * @Param: buffersize ca buffer size
     * @Return: 0 success, -1 fail
     */
    public int SSMSaveCABufferSize(int buffersize) {
        int val[] = new int[]{buffersize};
        return sendCmdIntArray(SSM_SAVE_CA_BUFFER_SIZE, val);
    }

    /**
     * @Function: SSMReadCABufferSize
     * @Description: Read dtv ca buffer size
     * @Param:
     * @Return: size
     */
    public int SSMReadCABufferSize() {
        return sendCmd(SSM_READ_CA_BUFFER_SIZE);
    }

    /**
     * @Function: SSMSaveNoiseGateThreshold
     * @Description: Save noise gate threshold
     * @Param: flag noise gate threshold flag
     * @Return: 0 success, -1 fail
     */
    public int SSMSaveNoiseGateThreshold(int flag) {
        int val[] = new int[]{flag};
        return sendCmdIntArray(SSM_SAVE_NOISE_GATE_THRESHOLD_STATUS, val);
    }

    /**
     * @Function: SSMReadNoiseGateThreshold
     * @Description: Read noise gate threshold flag
     * @Param:
     * @Return: flag
     */
    public int SSMReadNoiseGateThreshold() {
        return sendCmd(SSM_READ_NOISE_GATE_THRESHOLD_STATUS);
    }

    /**
     * @Function: SSMSaveHdmiEdidVer
     * @Description: save hdmi edid version
     * @Param: port_id is hdmi port id
                      ver is save version.
     * @Return: 0 success, -1 fail
     */
    public int SSMSaveHdmiEdidVer(HdmiPortID port_id, HdmiEdidVer ver) {
        int val[] = new int[]{port_id.toInt(), port_id.toInt()};
        return sendCmdIntArray(SSM_SAVE_HDMI_EDID_VER, val);
    }

    /**
     * @Function: SSMReadHdmiEdidVer
     * @Description: Read hdmi edid version
     * @Param: port_id is hdmi port id
     * @Return: hdmi edid version
     */
    public int SSMReadHdmiEdidVer(HdmiPortID port_id) {
        int val[] = new int[]{port_id.toInt()};
        return sendCmdIntArray(SSM_READ_HDMI_EDID_VER, val);
    }

    /**
     * @Function: SSMSaveHDCPKeyEnable
     * @Description: save hdmi HDCP key enable or disable
     * @Param: iSenable
     * @Return: 0 success, -1 fail
     */
    public int SSMSaveHDCPKeyEnable(HdcpKeyIsEnable iSenable) {
        int val[] = new int[]{iSenable.toInt()};
        return sendCmdIntArray(SSM_SAVE_HDCP_KEY_ENABLE, val);
    }

        /**
     * @Function: SSMReadHDCPKeyEnable
     * @Description: Read hdmi HDCP key enable or disable
     * @Param:
     * @Return: enable or enable
     */
    public int SSMReadHDCPKeyEnable() {
        return sendCmd(SSM_READ_HDCP_KEY_ENABLE);
    }

    public enum CC_TV_TYPE {
        TV_TYPE_ATV(0),
        TV_TYPE_DVBC(1),
        TV_TYPE_DTMB(2),
        TV_TYPE_ATSC(3),
        TV_TYPE_ATV_DVBC(4),
        TV_TYPE_ATV_DTMB(5),
        TV_TYPE_DVBC_DTMB(6),
        TV_TYPE_ATV_DVBC_DTMB(7);

        private int val;

        CC_TV_TYPE(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    /**
     * @Function: SSMSaveProjectID
     * @Description: Save project id
     * @Param: project id
     * @Return: 0 success, -1 fail
     */
    public int SSMSaveProjectID(int tmp_id) {
        int val[] = new int[]{tmp_id};
        return sendCmdIntArray(SSM_SAVE_PROJECT_ID, val);
    }

    /**
     * @Function: SSMReadProjectID
     * @Description: Read project id
     * @Param:
     * @Return: return project id
     */
    public int SSMReadProjectID() {
        return sendCmd(SSM_READ_PROJECT_ID);
    }

    /**
     * @Function: SSMSaveHDCPKey
     * @Description: save hdcp key
     * @Param: data_buf write buffer hdcp key
     * @Return: 0 success, -1 fail
     */
    public int SSMSaveHDCPKey(int data_buf[]) {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(SSM_SAVE_HDCPKEY);

        int size = data_buf.length;
        cmd.writeInt(size);
        for (int i = 0; i < size; i++) {
            cmd.writeInt(data_buf[i]);
        }

        sendCmdToTv(cmd, r);
        return r.readInt();
    }

    /**
     * @Function: SSMReadHDCPKey
     * @Description: read hdcp key
     * @Param: data_buf read buffer hdcp key
     * @Return: 0 success, -1 fail
     */
    public int SSMReadHDCPKey(int data_buf[]) {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(SSM_READ_HDCPKEY);
        sendCmdToTv(cmd, r);

        int size = r.readInt();
        for (int i = 0; i < size; i++) {
            data_buf[i] = r.readInt();
        }

        return r.readInt();
    }
    // SSM END

    //MISC

    /**
     * @Function: TvMiscPropertySet
     * @Description: Set android property
     * @Param: key_str property name string, value_str property set value string
     * @Return: 0 success, -1 fail
     */
    public int TvMiscPropertySet(String key_str, String value_str) {
        String val[] = new String[]{key_str, value_str};
        return sendCmdStringArray(MISC_PROP_SET, val);
    }

    /**
     * @Function: TvMiscPropertySet
     * @Description: Get android property
     * @Param: key_str property name string, value_str property get value string
     * @Return: 0 success, -1 fail
     */
    public String TvMiscPropertyGet(String key_str, String def_str) {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(MISC_PROP_GET);
        cmd.writeString(key_str);
        cmd.writeString(def_str);
        sendCmdToTv(cmd, r);
        return r.readString();
    }

    /**
     * @Function: TvMiscConfigSet
     * @Description: Set tv config
     * @Param: key_str tv config name string, value_str tv config set value string
     * @Return: 0 success, -1 fail
     */
    public int TvMiscConfigSet(String key_str, String value_str) {
        String val[] = new String[]{key_str, value_str};
        return sendCmdStringArray(MISC_CFG_SET, val);
    }

    /**
     * @Function: TvMiscConfigGet
     * @Description: Get tv config
     * @Param: key_str tv config name string, value_str tv config get value string
     * @Return: 0 success, -1 fail
     */
    public String TvMiscConfigGet(String key_str, String def_str) {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(MISC_CFG_GET);
        cmd.writeString(key_str);
        cmd.writeString(def_str);
        sendCmdToTv(cmd, r);
        return r.readString();
    }

    /**
     * @Function: TvMiscSetGPIOCtrl
     * @Description: Set gpio level
     * @Param: op_cmd_str gpio set cmd string
     * @Return: 0 success, -1 fail
     */
    public int TvMiscSetGPIOCtrl(String op_cmd_str) {
        String val[] = new String[]{op_cmd_str};
        return sendCmdStringArray(MISC_SET_GPIO_CTL, val);
    }

    /**
     * @Function: TvMiscGetGPIOCtrl
     * @Description: Get gpio level
     * @Param: key_str gpio read cmd string, def_str gpio read status string
     * @Return: 0 success, -1 fail
     */
    public int TvMiscGetGPIOCtrl(String key_str, String def_str) {
        return sendCmd(MISC_GET_GPIO_CTL);
    }

    /**
     * @Function: TvMiscReadADCVal
     * @Description: Read adc channel status
     * @Param: chan_num for adc channel select
     * @Return: adc read value
     */
    public int TvMiscReadADCVal(int chan_num) {
        int val[] = new int[]{chan_num};
        return sendCmdIntArray(MISC_READ_ADC_VAL, val);
    }

    /**
     * @Function: TvMiscSetUserCounter
     * @Description: Enable user counter
     * @Param: counter 1 enable or 0 disable user counter
     * @Return: 0 success, -1 fail
     */
    public int TvMiscSetUserCounter(int counter) {
        int val[] = new int[]{counter};
        return sendCmdIntArray(MISC_SET_WDT_USER_PET, val);
    }

    /**
     * @Function: TvMiscSetUserCounterTimeOut
     * @Description: Set user counter timeout
     * @Param: counter_timer_out time out number
     * @Return: 0 success, -1 fail
     */
    public int TvMiscSetUserCounterTimeOut(int counter_timer_out) {
        int val[] = new int[]{counter_timer_out};
        return sendCmdIntArray(MISC_SET_WDT_USER_COUNTER, val);
    }

    /**
     * @Function: TvMiscSetUserPetResetEnable
     * @Description: Enable or disable user pet reset
     * @Param: enable 1 enable or 0 disable
     * @Return: 0 success, -1 fail
     */
    public int TvMiscSetUserPetResetEnable(int enable) {
        int val[] = new int[]{enable};
        return sendCmdIntArray(MISC_SET_WDT_USER_PET_RESET_ENABLE, val);
    }

    /**
     * @Function: TvMiscSetI2CBusStatus
     * @Description: Enable or disable i2c bus
     * @Param: tmp_val 1 enable or 0 disable
     * @Return: 0 success, -1 fail
     */
    public int TvMiscSetI2CBusStatus(int tmp_val) {
        int val[] = new int[]{tmp_val};
        return sendCmdIntArray(MISC_SET_I2C_BUS_STATUS, val);
    }

    /**
     * @Function: TvMiscGetI2CBusStatus
     * @Description: Get i2c bus status
     * @Param:
     * @Return: value 1 enable or 0 disable
     */
    public int TvMiscGetI2CBusStatus() {
        return sendCmd(MISC_GET_I2C_BUS_STATUS);
    }

    // tv version info
    public class android_ver_info {
        public String build_release_ver;
        public String build_number_ver;
    }

    public static class kernel_ver_info {
        public String linux_ver_info;
        public String build_usr_info;
        public String build_time_info;
    }

    public class tvapi_ver_info {
        public String git_branch_info;
        public String git_commit_info;
        public String last_change_time_info;
        public String build_time_info;
        public String build_usr_info;
    }

    public class dvb_ver_info {
        public String git_branch_info;
        public String git_commit_info;
        public String last_change_time_info;
        public String build_time_info;
        public String build_usr_info;
    }

    public class version_info {
        public android_ver_info android_ver;
        public String ubootVer;
        public kernel_ver_info kernel_ver;
        public tvapi_ver_info tvapi_ver;
        public dvb_ver_info dvb_ver;
    }

    public class project_info {
        public String version;
        public String panel_type;
        public String panel_outputmode;
        public String panel_rev;
        public String panel_name;
        public String amp_curve_name;
    }

    /**
     * @Function: TvMiscGetAndroidVersion
     * @Description: Get android version
     * @Param: none
     * @Return: android_ver_info
     */
    public android_ver_info TvMiscGetAndroidVersion() {
        libtv_log_open();
        android_ver_info tmpInfo = new android_ver_info();

        tmpInfo.build_release_ver = Build.VERSION.RELEASE;
        tmpInfo.build_number_ver = Build.DISPLAY;

        return tmpInfo;
    }

    /**
     * @Function: TvMiscGetUbootVersion
     * @Description: Get uboot version
     * @Param: none
     * @Return:
     */
    public String TvMiscGetUbootVersion() {
        libtv_log_open();
        String ubootvar = TvMiscPropertyGet("ro.ubootenv.varible.prefix",
                "ubootenv.var");
        String ver = TvMiscPropertyGet(ubootvar + "." + "ubootversion", "VERSION_ERROR");
        return ver;
    }

    /**
     * @Function: TvMiscGetKernelVersion
     * @Description: Get kernel version
     * @Param: none
     * @Return: kernel_ver_info
     */
    public kernel_ver_info TvMiscGetKernelVersion() {
        libtv_log_open();
        kernel_ver_info tmpInfo = new kernel_ver_info();
        String info = "";
        InputStream inputStream = null;

        tmpInfo.linux_ver_info = "unkown";
        tmpInfo.build_usr_info = "unkown";
        tmpInfo.build_time_info = "unkown";

        try {
            inputStream = new FileInputStream("/proc/version");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "Regex did not match on /proc/version: " + info);
            return tmpInfo;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                    inputStream), 8 * 1024);
        try {
            info = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return tmpInfo;
        }
        finally {
            try {
                reader.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                return tmpInfo;
            }
        }

        final String PROC_VERSION_REGEX =
            "Linux version (\\S+) " + /* group 1: "3.0.31-g6fb96c9" */
            "\\((\\S+?)\\) " +        /* group 2: "x@y.com" (kernel builder) */
            "(?:\\(gcc.+? \\)) " +    /* ignore: GCC version information */
            "([^\\s]+)\\s+" +         /* group 3: "#1" */
            "(?:.*?)?" +              /* ignore: optional SMP, PREEMPT, and any CONFIG_FLAGS */
            "((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)"; /* group 4: "Thu Jun 28 11:02:39 PDT 2012" */

        Matcher m = Pattern.compile(PROC_VERSION_REGEX).matcher(info);

        if (!m.matches()) {
            Log.e(TAG, "Regex did not match on /proc/version: " + info);
            return tmpInfo;
        } else if (m.groupCount() < 4) {
            Log.e(TAG,
                    "Regex match on /proc/version only returned " + m.groupCount()
                    + " groups");
            return tmpInfo;
        }

        tmpInfo.linux_ver_info = m.group(1);
        tmpInfo.build_usr_info = m.group(2) + " " + m.group(3);
        tmpInfo.build_time_info = m.group(4);

        return tmpInfo;
    }

    /**
     * @Function: TvMiscGetTVAPIVersion
     * @Description: Get TV API version
     * @Param: none
     * @Return: tvapi_ver_info
     */
    public tvapi_ver_info TvMiscGetTVAPIVersion() {
        libtv_log_open();
        tvapi_ver_info tmpInfo = new tvapi_ver_info();

        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(MISC_GET_TV_API_VERSION);
        sendCmdToTv(cmd, r);

        tmpInfo.git_branch_info = r.readString();
        tmpInfo.git_commit_info = r.readString();
        tmpInfo.last_change_time_info = r.readString();
        tmpInfo.build_time_info = r.readString();
        tmpInfo.build_usr_info = r.readString();

        return tmpInfo;
    }

    /**
     * @Function: TvMiscGetDVBAPIVersion
     * @Description: Get DVB API version
     * @Param: none
     * @Return: dvb_ver_info
     */
    public dvb_ver_info TvMiscGetDVBAPIVersion() {
        libtv_log_open();
        dvb_ver_info tmpInfo = new dvb_ver_info();

        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(MISC_GET_DVB_API_VERSION);
        sendCmdToTv(cmd, r);

        tmpInfo.git_branch_info = r.readString();
        tmpInfo.git_commit_info = r.readString();
        tmpInfo.last_change_time_info = r.readString();
        tmpInfo.build_time_info = r.readString();
        tmpInfo.build_usr_info = r.readString();

        return tmpInfo;
    }

    /**
     * @Function: TvMiscGetVersion
     * @Description: Get version
     * @Param: none
     * @Return: version_info
     */
    public version_info TvMiscGetVersion() {
        libtv_log_open();
        version_info tmpInfo = new version_info();

        tmpInfo.android_ver = TvMiscGetAndroidVersion();
        tmpInfo.ubootVer = TvMiscGetUbootVersion();
        tmpInfo.kernel_ver = TvMiscGetKernelVersion();
        tmpInfo.tvapi_ver = TvMiscGetTVAPIVersion();
        tmpInfo.dvb_ver = TvMiscGetDVBAPIVersion();

        return tmpInfo;
    }

    /**
     * @Function: TvMiscGetProjectInfo
     * @Description: Get project info
     * @Param: none
     * @Return: project_info
     */
    public project_info TvMiscGetProjectInfo() {
        libtv_log_open();
        project_info tmpInfo = new project_info();

        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(MISC_GET_PROJECT_INFO);
        sendCmdToTv(cmd, r);

        tmpInfo.version = r.readString();
        tmpInfo.panel_type = r.readString();
        tmpInfo.panel_outputmode = r.readString();
        tmpInfo.panel_rev = r.readString();
        tmpInfo.panel_name = r.readString();
        tmpInfo.amp_curve_name = r.readString();

        return tmpInfo;
    }

    /**
     * @Function: TvMiscGetPlatformType
     * @Description: Get platform type
     * @Param: none
     * @Return: 0: T868 no fbc 1:T866 has fbc
     */
    public int TvMiscGetPlatformType() {
        return sendCmd(MISC_GET_PLATFORM_TYPE);
    }

    public enum SerialDeviceID {
        SERIAL_A(0),
        SERIAL_B(1),
        SERIAL_C(2);

        private int val;

        SerialDeviceID(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    /**
     * @Function: SetSerialSwitch
     * @Description: Set speical serial switch
     * @Param: dev_id, refer to enum SerialDeviceID
     *         tmp_val, 1 is enable speical serial, 0 is disable speical serial
     * @Return: 0 success, -1 fail
     */
    public int SetSerialSwitch(SerialDeviceID dev_id, int tmp_val) {
        int val[] = new int[]{dev_id.toInt(), tmp_val};
        return sendCmdIntArray(MISC_SERIAL_SWITCH, val);
    }

    /**
     * @Function: SendSerialData
     * @Description: send serial data
     * @Param: dev_id, refer to enum SerialDeviceID
     *         data_len, the length will be send
     *         data_buf, the data will be send
     * @Return: 0 success, -1 fail
     */
    public int SendSerialData(SerialDeviceID dev_id, int data_len, int data_buf[]) {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();

        if (data_len > data_buf.length) {
            return -1;
        }

        cmd.writeInt(MISC_SERIAL_SEND_DATA);

        cmd.writeInt(dev_id.toInt());
        cmd.writeInt(data_len);
        for (int i = 0; i < data_len; i++) {
            cmd.writeInt(data_buf[i]);
        }

        sendCmdToTv(cmd, r);
        return r.readInt();
    }

    /**
     * @Function: TvMiscDeleteDirFiles
     * @Description: Delete dir files
     * @Param: par_str dir path string, flag -f, -fr...
     * @Return: 0 success, -1 fail
     */
    public int TvMiscDeleteDirFiles(String path_str, int flag) {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        int tmpRet;
        cmd.writeInt(DELETE_DIR_FILES);
        cmd.writeString(path_str);
        cmd.writeInt(flag);
        sendCmdToTv(cmd, r);
        tmpRet = r.readInt();
        return tmpRet;
    }

    /**
     * @Function: TvMiscSetPowerLedIndicator
     * @Description: Set power led indicator, red or green.
     * @Param: onoff: 1 on, 0 off
     * @Return: 0 success, -1 fail
     */
    public int TvMiscSetPowerLedIndicator(int onoff) {
        int val[] = new int[]{onoff};
        return sendCmdIntArray(MISC_SET_POWER_LED_INDICATOR, val);
    }

    /**
     * @Function: TvMiscMutePanel
     * @Description: Mute panel or unmute panel, power on or off panel and backlight.
     * @Param: par_str dir path string, flag -f, -fr...
     * @Return: 0 success, -1 fail
     */
    public int TvMiscMutePanel(int onoff) {
        int val[] = new int[]{onoff};
        return sendCmdIntArray(MISC_SET_PANEL_MUTE, val);
    }
    //MISC END

    public int DtvAutoScan() {
        return sendCmd(DTV_SCAN_AUTO);
    }

    public int DtvAutoScanAtsc(int attenna, int videoStd,int audioStd) {
        int val[] = new int[]{attenna, videoStd, audioStd};
        return sendCmdIntArray(DTV_SCAN_AUTO_ATSC, val);
    }

    public int DtvManualScan(int beginFreq, int endFreq, int modulation) {
        int val[] = new int[]{beginFreq, endFreq, modulation};
        return sendCmdIntArray(DTV_SCAN_MANUAL_BETWEEN_FREQ, val);
    }

    public int DtvManualScan(int freq) {
        int val[] = new int[]{freq};
        return sendCmdIntArray(DTV_SCAN_MANUAL, val);
    }

    public int AtvAutoScan(int videoStd, int audioStd) {
        return AtvAutoScan(videoStd, audioStd, 0, 0);
    }

    public int AtvAutoScan(int videoStd, int audioStd, int storeType) {
        return AtvAutoScan(videoStd, audioStd, storeType, 0);
    }

    public int AtvAutoScan(int videoStd, int audioStd, int storeType, int procMode) {
        int val[] = new int[]{videoStd, audioStd, storeType, procMode};
        return sendCmdIntArray(ATV_SCAN_AUTO, val);
    }

    /**
     * @Function: AtvManualScan
     * @Description: atv manual scan
     * @Param: currentNum:current Channel Number
     * @Param: starFreq:start frequency
     * @Param: endFreq:end frequency
     * @Param: videoStd:scan video standard
     * @Param: audioStd:scan audio standard
     * @Return: 0 ok or -1 error
     */
    public int AtvManualScan(int startFreq, int endFreq, int videoStd,
            int audioStd, int storeType, int currentNum) {
        int val[] = new int[]{startFreq, endFreq, videoStd, audioStd, storeType, currentNum};
        return sendCmdIntArray(ATV_SCAN_MANUAL_BY_NUMBER, val);
    }

    /**
     * @Function: AtvManualScan
     * @Description: atv manual scan
     * @Param: starFreq:start frequency
     * @Param: endFreq:end frequency
     * @Param: videoStd:scan video standard
     * @Param: audioStd:scan audio standard
     * @Return: 0 ok or -1 error
     */
    public int AtvManualScan(int startFreq, int endFreq, int videoStd,
            int audioStd) {
        int val[] = new int[]{startFreq, endFreq, videoStd, audioStd};
        return sendCmdIntArray(ATV_SCAN_MANUAL, val);
    }

    public int AtvDtvPauseScan() {
        return sendCmd(ATV_DTV_SCAN_PAUSE);
    }

    public int AtvDtvResumeScan() {
        return sendCmd(ATV_DTV_SCAN_RESUME);
    }

    /**
     * @Function: clearAllProgram
     * @Description: clearAllProgram
     * @Param: arg0, not used currently
     * @Return: 0 ok or -1 error
     */
    public int clearAllProgram(int arg0){
        int val[] = new int[]{arg0};
        return sendCmdIntArray(TV_CLEAR_ALL_PROGRAM, val);
    }

    //enable: 0  is disable , 1  is enable.      when enable it , can black video for switching program
    public int setBlackoutEnable(int enable){
        int val[] = new int[]{enable};
        return sendCmdIntArray(SET_BLACKOUT_ENABLE, val);
    }

    public void startAutoBacklight() {
        sendCmd(START_AUTO_BACKLIGHT);
    }

    public void stopAutoBacklight() {
        sendCmd(STOP_AUTO_BACKLIGHT);
    }

    /**
     * @return 1:on,0:off
     */
    public int isAutoBackLighting() {
        return sendCmd(IS_AUTO_BACKLIGHTING);
    }

    public int getAverageLut() {
        return sendCmd(GET_AVERAGE_LUMA);
    }

    public int getAutoBacklightData(int data[]) {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(GET_AUTO_BACKLIGHT_DATA);
        sendCmdToTv(cmd, r);

        int size = r.readInt();
        for (int i = 0; i < size; i++) {
            data[i] = r.readInt();
        }

        return size;
    }

    public int setAutoBacklightData(HashMap<String,Integer> map) {
        String data ="opcSwitch:" + map.get("opcSwitch") +
            ",MinBacklight:"+ map.get("MinBacklight") +
            ",Offset:" + map.get("Offset") +
            ",MaxStep:" + map.get("MaxStep") +
            ",MinStep:" + map.get("MinStep");
        String val[] = new String[]{data};
        sendCmdStringArray(SET_AUTO_BACKLIGHT_DATA, val);
        return 0;
    }

    //ref to setBlackoutEnable fun
    public int SSMReadBlackoutEnalbe() {
        return sendCmd(SSM_READ_BLACKOUT_ENABLE);
    }

    /**
     * @Function: SSMEEPROMWriteOneByte_N310_N311
     * @Description: Write one byte to eerpom
     * @Param: offset pos in eeprom for this byte, val one byte value
     * @Return: 0 success, -1 fail
     */
    public int SSMEEPROMWriteOneByte_N310_N311(int offset, int value) {
        int val[] = new int[]{offset, value};
        return sendCmdIntArray(SSM_EEPROM_SAVE_ONE_BYTE_N310_N311, val);
    }

    /**
     * @Function: SSMEEPROMReadOneByte_N310_N311
     * @Description: Read one byte from eeprom
     * @Param: offset pos in eeprom for this byte to read
     * @Return: one byte read value
     */
    public int SSMEEPROMReadOneByte_N310_N311(int offset) {
        int val[] = new int[]{offset};
        return sendCmdIntArray(SSM_EEPROM_READ_ONE_BYTE_N310_N311, val);
    }

    /**
     * @Function: SSMEEPROMWriteNBytes
     * @Description: Write n bytes to eeprom
     * @Param: offset pos in eeprom for the bytes, data_len how many bytes, data_buf n bytes write buffer
     * @Return: 0 success, -1 fail
     */
    public int SSMEEPROMWriteNBytes_N310_N311(int offset, int data_len, int data_buf[]) {
        libtv_log_open();
        int i = 0, ret = 0;
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();

        cmd.writeInt(SSM_EEPROM_SAVE_N_BYTES_N310_N311);
        cmd.writeInt(offset);
        cmd.writeInt(data_len);
        for (i = 0; i < data_len; i++) {
            cmd.writeInt(data_buf[i]);
        }

        sendCmdToTv(cmd, r);
        ret = r.readInt();
        return ret;
    }

    /**
     * @Function: SSMEEPROMReadNBytes_N310_N311
     * @Description: Read one byte from eeprom
     * @Param: offset pos in eeprom for the bytes, data_len how many bytes, data_buf n bytes read buffer
     * @Return: 0 success, -1 fail
     */
    public int SSMEEPROMReadNBytes_N310_N311(int offset, int data_len, int data_buf[]) {
        libtv_log_open();
        int i = 0, tmp_data_len = 0, ret = 0;
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();

        cmd.writeInt(SSM_EEPROM_READ_N_BYTES_N310_N311);
        cmd.writeInt(offset);
        cmd.writeInt(data_len);

        sendCmdToTv(cmd, r);

        data_len = r.readInt();
        for (i = 0; i < data_len; i++) {
            data_buf[i] = r.readInt();
        }

        ret = r.readInt();
        return ret;
    }

    /**
     * @Function: SSMFlashWriteOneByte
     * @Description: Write one byte to flash
     * @Param: offset pos in flash for this byte, val one byte value
     * @Return: 0 success, -1 fail
     */
    public int SSMFlashWriteOneByte_N310_N311(int offset, int value) {
        int val[] = new int[]{offset, value};
        return sendCmdIntArray(SSM_FLASH_SAVE_ONE_BYTE_N310_N311, val);
    }

    /**
     * @Function: SSMFlashReadOneByte
     * @Description: Read one byte from flash
     * @Param: offset pos in flash for this byte to read
     * @Return: one byte read value
     */
    public int SSMFlashReadOneByte_N310_N311(int offset) {
        int val[] = new int[]{offset};
        return sendCmdIntArray(SSM_FLASH_READ_ONE_BYTE_N310_N311, val);
    }

    /**
     * @Function: SSMFlashWriteNBytes
     * @Description: Write n bytes to flash
     * @Param: offset pos in flash for the bytes, data_len how many bytes, data_buf n bytes write buffer
     * @Return: 0 success, -1 fail
     */
    public int SSMFlashWriteNBytes_N310_N311(int offset, int data_len, int data_buf[]) {
        libtv_log_open();
        int i = 0, ret = 0;
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();

        cmd.writeInt(SSM_FLASH_SAVE_N_BYTES_N310_N311);
        cmd.writeInt(offset);
        cmd.writeInt(data_len);
        for (i = 0; i < data_len; i++) {
            cmd.writeInt(data_buf[i]);
        }

        sendCmdToTv(cmd, r);
        ret = r.readInt();
        return ret;
    }

    /**
     * @Function: SSMFlashReadNBytes
     * @Description: Read one byte from flash
     * @Param: offset pos in flash for the bytes, data_len how many bytes, data_buf n bytes read buffer
     * @Return: 0 success, -1 fail
     */
    public int SSMFlashReadNBytes_N310_N311(int offset, int data_len, int data_buf[]) {
        libtv_log_open();
        int i = 0, tmp_data_len = 0, ret = 0;
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();

        cmd.writeInt(SSM_FLASH_READ_N_BYTES_N310_N311);
        cmd.writeInt(offset);
        cmd.writeInt(data_len);

        sendCmdToTv(cmd, r);

        data_len = r.readInt();
        for (i = 0; i < data_len; i++) {
            data_buf[i] = r.readInt();
        }

        ret = r.readInt();
        return ret;
    }

    /**
     * @Function: ATVGetChanInfo
     * @Description: Get atv current channel info
     * @Param: dbID,program's in the srv_table of DB
     * @out: dataBuf[0]:freq
     * @out: dataBuf[1]  finefreq
     * @out: dataBuf[2]:video standard
     * @out: dataBuf[3]:audeo standard
     * @out: dataBuf[4]:is auto color std? 1, is auto,   0  is not auto
     * @Return: 0 ok or -1 error
     */
    public int ATVGetChanInfo(int dbID, int dataBuf[]) {
        libtv_log_open();
        int tmpRet = -1,tmp_buf_size = 0, i = 0;
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();

        cmd.writeInt(ATV_GET_CHANNEL_INFO);
        cmd.writeInt(dbID);
        sendCmdToTv(cmd, r);

        dataBuf[0] = r.readInt();
        dataBuf[1] = r.readInt();
        dataBuf[2] = r.readInt();
        dataBuf[3] = r.readInt();
        dataBuf[4] = r.readInt();

        tmpRet = r.readInt();
        return tmpRet;
    }

    /**
     * @Function: ATVGetVideoCenterFreq
     * @Description: Get atv current channel video center freq
     * @Param: dbID,program's in the srv_table of DB
     * @Return: 0 ok or -1 error
     */
    public int ATVGetVideoCenterFreq(int dbID) {
        int val[] = new int[]{dbID};
        return sendCmdIntArray(ATV_GET_VIDEO_CENTER_FREQ, val);
    }

    /**
     * @Function: ATVGetLastProgramID
     * @Description: ATV Get Last Program's ID
     * @Return: ATV Last Program's ID
     */
    public int ATVGetLastProgramID() {
        return sendCmd(ATV_GET_CURRENT_PROGRAM_ID);
    }

    /**
     * @Function: DTVGetLastProgramID
     * @Description: DTV Get Last Program's ID
     * @Return: DTV Last Program's ID
     */
    public int DTVGetLastProgramID() {
        return sendCmd(DTV_GET_CURRENT_PROGRAM_ID);
    }

    /**
     * @Function: ATVGetMinMaxFreq
     * @Description: ATV Get Min Max Freq
     * @Param:dataBuf[0]:min freq
     * @Param:dataBuf[1]:max freq
     * @Return: 0 or -1
     */
    public int ATVGetMinMaxFreq(int dataBuf[]) {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(ATV_GET_MIN_MAX_FREQ);
        sendCmdToTv(cmd, r);
        dataBuf[0] = r.readInt();
        dataBuf[1] = r.readInt();
        int tmpRet = r.readInt();
        return tmpRet;
    }

    /**
     * @Function: DTVGetScanFreqList
     * @Description: DTVGetScanFreqList
     * @Param:
     * @Return: FreqList
     */
    public ArrayList<FreqList> DTVGetScanFreqList() {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(DTV_GET_SCAN_FREQUENCY_LIST);
        sendCmdToTv(cmd, r);
        int size = r.readInt();
        int base = 1 ;
        ArrayList<FreqList> FList = new ArrayList<FreqList>();
        FreqList bpl = new FreqList();
        base = r.readInt() - 1;
        bpl.ID = 1 ;
        bpl.freq= r.readInt();
        FList.add(bpl);
        for (int i = 1; i < size; i++) {
            FreqList pl = new FreqList();
            pl.ID = r.readInt() - base;
            pl.freq= r.readInt();
            FList.add(pl);
        }
        return FList;

    }

    /**
     * @Function: DTVGetChanInfo
     * @Description: Get dtv current channel info
     * @Param: dbID:program's in the srv_table of DB
     * @Param: dataBuf[0]:freq
     * @Param: dataBuf[1]:strength
     * @Param: dataBuf[2]:snr
     * @Param: dataBuf[2]:ber
     * @Return: 0 ok or -1 error
     */
    public int DTVGetChanInfo(int dbID, int dataBuf[]) {
        libtv_log_open();
        int tmpRet = -1,tmp_buf_size = 0, i = 0;
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();

        cmd.writeInt(DTV_GET_CHANNEL_INFO);
        cmd.writeInt(dbID);
        sendCmdToTv(cmd, r);

        dataBuf[0] = r.readInt();
        dataBuf[1] = r.readInt();
        dataBuf[2] = r.readInt();
        dataBuf[3] = r.readInt();

        tmpRet = r.readInt();
        return tmpRet;
    }

    public int TvSubtitleDrawUnlock() {
        return sendCmd(TV_SUBTITLE_DRAW_END);
    }

    public Bitmap CreateSubtitleBitmap() {
        libtv_log_open();
        Bitmap subtitleFrame = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888);
        native_create_subtitle_bitmap(subtitleFrame);
        return subtitleFrame;
    }

    public void setSubtitleUpdateListener(SubtitleUpdateListener l) {
        libtv_log_open();
        mSubtitleListener = l;
    }
    //scanner
    public void setScannerListener(ScannerEventListener l) {
        libtv_log_open();
        mScannerListener = l;
    }

    public void setStorDBListener(StorDBEventListener l) {
        libtv_log_open();
        mStorDBListener = l;
    }

    public void setScanningFrameStableListener(ScanningFrameStableListener l) {
        libtv_log_open();
        mScanningFrameStableListener = l;
    }

    public Bitmap CreateVideoFrameBitmap(int inputSourceMode) {
        libtv_log_open();
        Bitmap videoFrame = Bitmap.createBitmap(1280, 720, Bitmap.Config.ARGB_8888);
        native_create_video_frame_bitmap(videoFrame);
        return videoFrame;
    }

    public final static int EVENT_SCAN_PROGRESS             = 0;
    public final static int EVENT_STORE_BEGIN               = 1;
    public final static int EVENT_STORE_END                 = 2;
    public final static int EVENT_SCAN_END                  = 3;
    public final static int EVENT_BLINDSCAN_PROGRESS        = 4;
    public final static int EVENT_BLINDSCAN_NEWCHANNEL      = 5;
    public final static int EVENT_BLINDSCAN_END             = 6;
    public final static int EVENT_ATV_PROG_DATA             = 7;
    public final static int EVENT_DTV_PROG_DATA             = 8;
    public final static int EVENT_SCAN_EXIT                 = 9;
    public final static int EVENT_SCAN_BEGIN                = 10;

    public class ScannerEvent {
        public int type;
        public int precent;
        public int totalcount;
        public int lock;
        public int cnum;
        public int freq;
        public String programName;
        public int srvType;
        public String msg;
        public int strength;
        public int quality;

        //for ATV
        public int videoStd;
        public int audioStd;
        public int isAutoStd;
        public int fineTune;

        //for DTV
        public int mode;
        public int sr;
        public int mod;
        public int bandwidth;
        public int ofdm_mode;
        public int ts_id;
        public int orig_net_id;

        public int serviceID;
        public int vid;
        public int vfmt;
        public int[] aids;
        public int[] afmts;
        public String[] alangs;
        public int[] atypes;
        public int pcr;

        public int[] stypes;
        public int[] sids;
        public int[] sstypes;
        public int[] sid1s;
        public int[] sid2s;
        public String[] slangs;

        public int free_ca;
        public int scrambled;

        public int scan_mode;

        public int sdtVersion;
    }

    public interface ScannerEventListener {
        void onEvent(ScannerEvent ev);
    }

    public interface StorDBEventListener {
        void StorDBonEvent(ScannerEvent ev);
    }


    // frame stable when scanning
    public class ScanningFrameStableEvent {
        public int CurScanningFrq;
    }

    public interface ScanningFrameStableListener {
        void onFrameStable(ScanningFrameStableEvent ev);
    }


    //epg
    public void setEpgListener(EpgEventListener l) {
        libtv_log_open();
        mEpgListener = l;
    }

    public class EpgEvent {
        public int type;
        public int channelID;
        public int programID;
        public int dvbOrigNetID;
        public int dvbTSID;
        public int dvbServiceID;
        public long time;
        public int dvbVersion;
    }

    public interface EpgEventListener {
        void onEvent(EpgEvent ev);
    }

    public class VFrameEvent{
        public int FrameNum;
        public int FrameSize;
        public int FrameWidth;
        public int FrameHeight;
    }

    public interface VframBMPEventListener{
        void onEvent(VFrameEvent ev);
    }

    public void setGetVframBMPListener(VframBMPEventListener l) {
        libtv_log_open();
        mVframBMPListener = l;
    }

    public interface SubtitleUpdateListener {
        void onUpdate();
    }

    public int DtvStopScan() {
        return sendCmd(DTV_STOP_SCAN);
    }

    public int DtvGetSignalSNR() {
        return sendCmd(DTV_GET_SNR);
    }

    public int DtvGetSignalBER() {
        return sendCmd(DTV_GET_BER);
    }

    public int DtvGetSignalStrength() {
        return sendCmd(DTV_GET_STRENGTH);
    }

    /**
     * @Function: DtvGetAudioTrackNum
     * @Description: Get number audio track of program
     * @Param: [in] prog_id is in db srv table
     * @Return: number audio track
     */
    public int DtvGetAudioTrackNum(int prog_id) {
        int val[] = new int[]{prog_id};
        return sendCmdIntArray(DTV_GET_AUDIO_TRACK_NUM, val);
    }

    /**
     * @Function: DtvGetCurrAudioTrackIndex
     * @Description: Get number audio track of program
     * @Param: [in] prog_id is in db srv table
     * @Return: current audio track index
     */

    public int DtvGetCurrAudioTrackIndex(int prog_id) {
        int val[] = new int[]{prog_id};
        return sendCmdIntArray(DTV_GET_CURR_AUDIO_TRACK_INDEX, val);
    }

    public class DtvAudioTrackInfo {
        public String language;
        public int audio_fmt;
        public int aPid;
    }

    public DtvAudioTrackInfo DtvGetAudioTrackInfo(int prog_id, int audio_ind) {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(DTV_GET_AUDIO_TRACK_INFO);
        cmd.writeInt(prog_id);
        cmd.writeInt(audio_ind);
        sendCmdToTv(cmd, r);

        DtvAudioTrackInfo tmpRet = new DtvAudioTrackInfo();
        tmpRet.audio_fmt = r.readInt();
        tmpRet.language = r.readString();

        return tmpRet;
    }

    /**
     * @Function: DtvSetAudioChannleMod
     * @Description: set audio channel mod
     * @Param: [in] audioChannelMod is [0 Stereo] [1 left] [2 right ] [3 swap left right]
     * @Return:
     */
    public int DtvSetAudioChannleMod(int audioChannelMod) {
        int val[] = new int[]{audioChannelMod};
        sendCmdIntArray(DTV_SET_AUDIO_CHANNEL_MOD, val);
        return 0;
    }

    /**
     * @Function: DtvGetAudioChannleMod
     * @Description: set audio channel mod
     * @Param:
     * @Return: [OUT] audioChannelMod is [0 Stereo] [1 left] [2 right ] [3 swap left right]
     */
    public int DtvGetAudioChannleMod() {
        return sendCmd(DTV_GET_AUDIO_CHANNEL_MOD);
    }


    public int DtvGetFreqByProgId(int progId) {
        int val[] = new int[]{progId};
        return sendCmdIntArray(DTV_GET_FREQ_BY_PROG_ID, val);
    }

    public class EpgInfoEvent {
        public String programName;
        public String programDescription;
        public String programExtDescription;
        public long startTime;
        public long endTime;
        public int subFlag;
        public int evtId;
    }

    public EpgInfoEvent DtvEpgInfoPointInTime(int progId, long iUtcTime) {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        EpgInfoEvent epgInfoEvent = new EpgInfoEvent();

        cmd.writeInt(DTV_GET_EPG_INFO_POINT_IN_TIME);
        cmd.writeInt(progId);
        cmd.writeInt((int)iUtcTime);
        sendCmdToTv(cmd, r);
        epgInfoEvent.programName = r.readString();
        epgInfoEvent.programDescription = r.readString();
        epgInfoEvent.programExtDescription = r.readString();
        epgInfoEvent.startTime = r.readInt();
        epgInfoEvent.endTime = r.readInt();
        epgInfoEvent.subFlag = r.readInt();
        epgInfoEvent.evtId =  r.readInt();
        return epgInfoEvent;
    }

    public ArrayList<EpgInfoEvent> GetEpgInfoEventDuration(int progId,long iStartTime,long iDuration) {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(DTV_GET_EPG_INFO_DURATION);
        cmd.writeInt(progId);
        cmd.writeInt((int)iStartTime);
        cmd.writeInt((int)iDuration);
        sendCmdToTv(cmd, r);
        int size = r.readInt();
        ArrayList<EpgInfoEvent> pEpgInfoList = new ArrayList<EpgInfoEvent>();
        for (int i = 0; i < size; i++) {
            EpgInfoEvent pl = new EpgInfoEvent();
            pl.programName = r.readString();
            pl.programDescription = r.readString();
            pl.programExtDescription = r.readString();
            pl.startTime = r.readInt();
            pl.endTime = r.readInt();
            pl.subFlag = r.readInt();
            pl.evtId =  r.readInt();
            pEpgInfoList.add(pl);
        }
        return pEpgInfoList;
    }

    public int DtvSwitchAudioTrack(int audio_pid, int audio_format, int audio_param) {
        int val[] = new int[]{audio_pid, audio_format, audio_param};
        return sendCmdIntArray(DTV_SWITCH_AUDIO_TRACK, val);
    }

    public int DtvSwitchAudioTrack(int prog_id, int audio_track_id) {
        int val[] = new int[]{prog_id, audio_track_id};
        return sendCmdIntArray(DTV_SWITCH_AUDIO_TRACK, val);
    }

    public long DtvGetEpgUtcTime() {
        return sendCmd(DTV_GET_EPG_UTC_TIME);
    }

    public class VideoFormatInfo {
        public int width;
        public int height;
        public int fps;
        public int interlace;
    }

    public VideoFormatInfo DtvGetVideoFormatInfo() {
        libtv_log_open();
        VideoFormatInfo pVideoFormatInfo = new VideoFormatInfo();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();

        cmd.writeInt(DTV_GET_VIDEO_FMT_INFO);
        sendCmdToTv(cmd, r);
        pVideoFormatInfo.width = r.readInt();
        pVideoFormatInfo.height= r.readInt();
        pVideoFormatInfo.fps= r.readInt();
        pVideoFormatInfo.interlace= r.readInt();
        pVideoFormatInfo.width = r.readInt();
        return pVideoFormatInfo;
    }

    public class BookEventInfo {
        public String programName;
        public String envName;
        public long startTime;
        public long durationTime;
        public int bookId;
        public int progId;
        public int evtId;
    }

    public ArrayList<BookEventInfo> getBookedEvent() {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(DTV_GET_BOOKED_EVENT);

        int size = r.readInt();
        ArrayList<BookEventInfo> pBookEventInfoList = new ArrayList<BookEventInfo>();
        for (int i = 0; i < size; i++) {
            BookEventInfo pl = new BookEventInfo();
            pl.programName = r.readString();
            pl.envName = r.readString();
            pl.startTime = r.readInt();
            pl.durationTime = r.readInt();
            pl.bookId = r.readInt();
            pl.progId = r.readInt();
            pl.evtId = r.readInt();
            pBookEventInfoList.add(pl);
        }
        return pBookEventInfoList;
    }

    public int setEventBookFlag(int id, int bookFlag) {
        int val[] = new int[]{id, bookFlag};
        sendCmdIntArray(DTV_SET_BOOKING_FLAG, val);
        return 0;
    }

    public int setProgramName(int id, String name) {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(DTV_SET_PROGRAM_NAME);
        cmd.writeInt(id);
        cmd.writeString(name);
        sendCmdToTv(cmd, r);
        return 0;
    }

    public int setProgramSkipped(int id, int skipped) {
        int val[] = new int[]{id, skipped};
        sendCmdIntArray(DTV_SET_PROGRAM_SKIPPED, val);
        return 0;
    }

    public int setProgramFavorite(int id, int favorite) {
        int val[] = new int[]{id, favorite};
        sendCmdIntArray(DTV_SET_PROGRAM_FAVORITE, val);
        return 0;
    }

    public int deleteProgram(int id) {
        int val[] = new int[]{id};
        sendCmdIntArray(DTV_DETELE_PROGRAM, val);
        return 0;
    }

    public int swapProgram(int first_id, int second_id) {
        int val[] = new int[]{first_id, second_id};
        sendCmdIntArray(DTV_SWAP_PROGRAM, val);
        return 0;
    }

    public int setProgramLocked (int id, int locked) {
        int val[] = new int[]{id, locked};
        sendCmdIntArray(DTV_SET_PROGRAM_LOCKED, val);
        return 0;
    }

    /*public int PlayProgram(int progid) {
      libtv_log_open();
      Parcel cmd = Parcel.obtain();
      Parcel r = Parcel.obtain();
      int tmpRet ;
      cmd.writeInt(PLAY_PROGRAM);
      cmd.writeInt(progid);
      sendCmdToTv(cmd, r);
      tmpRet = r.readInt();

      return tmpRet;
      }*/

    public int PlayATVProgram(int freq, int videoStd, int audioStd, int fineTune, int audioCompetation) {
        int val[] = new int[]{4, freq, videoStd, audioStd, fineTune, audioCompetation};
        return sendCmdIntArray(PLAY_PROGRAM, val);
    }

    public int PlayDTVProgram(int mode, int freq, int para1, int para2, int vid, int vfmt, int aid, int afmt, int pcr, int audioCompetation) {
        int val[] = new int[]{mode, freq, para1, para2, vid, vfmt, aid, afmt, pcr, audioCompetation};
        return sendCmdIntArray(PLAY_PROGRAM, val);
    }

    public int StopPlayProgram() {
        return sendCmd(STOP_PROGRAM_PLAY);
    }

    public enum tv_fe_type_e {
        TV_FE_QPSK(0),
        TV_FE_QAM(1),
        TV_FE_OFDM(2),
        TV_FE_ATSC(3),
        TV_FE_ANALOG(4),
        TV_FE_DTMB(5);

        private int val;

        tv_fe_type_e(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    /**
     * @Function:SetFrontendParms
     * @Description:set frontend parameters
     * @Param:dataBuf[0]:feType, tv_fe_type_e
     * @Param:dataBuf[1]:freq, set freq to tuner
     * @Param:dataBuf[2]:videoStd, video std
     * @Param:dataBuf[3]:audioStd, audio std
     * @Param:dataBuf[4]:parm1
     * @Param:dataBuf[5]:parm2
     * @Return: 0 ok or -1 error
     */
    public int SetFrontendParms(tv_fe_type_e feType, int freq, int vStd, int aStd, int p1, int p2) {
        int val[] = new int[]{feType.toInt(), freq, vStd, aStd, p1, p2};
        return sendCmdIntArray(SET_FRONTEND_PARA, val);
    }

    public enum CC_PARAM_COUNTRY {
        CC_PARAM_COUNTRY_USA(0),
        CC_PARAM_COUNTRY_KOREA(1);

        private int val;

        CC_PARAM_COUNTRY(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    public enum CC_PARAM_SOURCE_TYPE {
        CC_PARAM_SOURCE_VBIDATA(0),
        CC_PARAM_SOURCE_USERDATA(1);

        private int val;

        CC_PARAM_SOURCE_TYPE(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    public enum CC_PARAM_CAPTION_TYPE {
        CC_PARAM_ANALOG_CAPTION_TYPE_CC1(0),
        CC_PARAM_ANALOG_CAPTION_TYPE_CC2(1),
        CC_PARAM_ANALOG_CAPTION_TYPE_CC3(2),
        CC_PARAM_ANALOG_CAPTION_TYPE_CC4(3),
        CC_PARAM_ANALOG_CAPTION_TYPE_TEXT1(4),
        CC_PARAM_ANALOG_CAPTION_TYPE_TEXT2(5),
        CC_PARAM_ANALOG_CAPTION_TYPE_TEXT3(6),
        CC_PARAM_ANALOG_CAPTION_TYPE_TEXT4(7),
        //
        CC_PARAM_DIGITAL_CAPTION_TYPE_SERVICE1(8),
        CC_PARAM_DIGITAL_CAPTION_TYPE_SERVICE2(9),
        CC_PARAM_DIGITAL_CAPTION_TYPE_SERVICE3(10),
        CC_PARAM_DIGITAL_CAPTION_TYPE_SERVICE4(11),
        CC_PARAM_DIGITAL_CAPTION_TYPE_SERVICE5(12),
        CC_PARAM_DIGITAL_CAPTION_TYPE_SERVICE6(13);

        private int val;

        CC_PARAM_CAPTION_TYPE(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }


    /*
     * 1, Set the country first and parameters should be either USA or KOREA
    #define CMD_SET_COUNTRY_USA                 0x5001
    #define CMD_SET_COUNTRY_KOREA            0x5002

    2, Set the source type which including
    a)VBI data(for analog program only)
    b)USER data(for AIR or Cable service)
    CMD_CC_SET_VBIDATA   = 0x7001,
    CMD_CC_SET_USERDATA = 0x7002,
    2.1 If the frontend type is Analog we must set the channel Index
    with command 'CMD_CC_SET_CHAN_NUM' and the parameter is like 57M
    we set 0x20000, this should according to USA standard frequency
    table.

    3, Next is to set the CC service type

    #define CMD_CC_1                        0x3001
    #define CMD_CC_2                        0x3002
    #define CMD_CC_3                        0x3003
    #define CMD_CC_4                        0x3004

        //this doesn't support currently
    #define CMD_TT_1                        0x3005
    #define CMD_TT_2                        0x3006
    #define CMD_TT_3                        0x3007
    #define CMD_TT_4                        0x3008

    #define CMD_SERVICE_1                 0x4001
    #define CMD_SERVICE_2                 0x4002
    #define CMD_SERVICE_3                 0x4003
    #define CMD_SERVICE_4                 0x4004
    #define CMD_SERVICE_5                 0x4005
    #define CMD_SERVICE_6                 0x4006

    4, Then set CMD_CC_START to start the CC service, and you needn't to stop

    CC service while switching services

    5, CMD_CC_STOP should be called in some cases like switch source, change

    program, no signal, blocked...*/

    //channel_num == 0 ,if frontend is dtv
    //else != 0
    public int StartCC(CC_PARAM_COUNTRY country, CC_PARAM_SOURCE_TYPE src_type, int channel_num, CC_PARAM_CAPTION_TYPE caption_type) {
        int val[] = new int[]{country.toInt(), src_type.toInt(), channel_num, caption_type.toInt()};
        return sendCmdIntArray(DTV_START_CC, val);
    }

    public int StopCC() {
        return sendCmd(DTV_STOP_CC);
    }

    public String Test1(int progid) {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        int tmpRet ;
        cmd.writeInt(DTV_TEST_1);
        cmd.writeInt(progid);
        sendCmdToTv(cmd, r);
        return r.readString();
    }

    public String Test2(int c) {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        int tmpRet ;
        cmd.writeInt(DTV_TEST_2);
        cmd.writeInt(c);
        sendCmdToTv(cmd, r);
        return r.readString();
    }

    public int tvAutoScan() {
        return sendCmd(TV_AUTO_SCAN);
    }

    /* public int tvSetScanSource(scan_source_t source) {
       Parcel cmd = Parcel.obtain();
       Parcel r = Parcel.obtain();
       int tmpRet ;
       cmd.writeInt(TV_SET_SCAN_SOURCE);
       cmd.writeInt(source.toInt());
       sendCmdToTv(cmd, r);
       tmpRet = r.readInt();

       return tmpRet;
       }*/

    public int tvSetAttennaType(atsc_attenna_type_t source) {
        int val[] = new int[]{source.toInt()};
        return sendCmdIntArray(TV_SET_ATSC_ATTENNA_TYPE, val);
    }

    public atsc_attenna_type_t tvGetAttennaType( ) {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        int tmpRet ;
        cmd.writeInt(TV_GET_ATSC_ATTENNA_TYPE);
        sendCmdToTv(cmd, r);
        tmpRet = r.readInt();
        for (atsc_attenna_type_t t:atsc_attenna_type_t.values()) {
            if (t.toInt() == tmpRet)
                return t;
        }
        return null;
    }

    public int HistogramGet_AVE() {
        return sendCmd(GET_HISTGRAM_AVE);
    }

    public int GetHistGram(int hist_gram_buf[]) {
        libtv_log_open();
        int i = 0, tmp_buf_size = 0, ret = 0;
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();

        cmd.writeInt(GET_HISTGRAM);
        sendCmdToTv(cmd, r);

        tmp_buf_size = r.readInt();
        for (i = 0; i < tmp_buf_size; i++) {
            hist_gram_buf[i] = r.readInt();
        }

        ret = r.readInt();
        return ret;
    }

    public int atscSetVchipLockstatus(String dimensioName,int ratingRegionId,int arr_length,int[] arr) {
        libtv_log_open();
        int tmpRet  = -1;
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(DTV_SET_VCHIP_LOCKSTATUS);
        cmd.writeString(dimensioName);
        cmd.writeInt(ratingRegionId);
        cmd.writeInt(arr_length);
        for (int i=0; i<arr_length; i++) {
            cmd.writeInt(arr[i]);
        }
        sendCmdToTv(cmd, r);
        tmpRet = r.readInt();
        return tmpRet;
    }

    public int[] atscGetVchipLockstatus(String dimensioName,int ratingRegionId) {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(DTV_GET_VCHIP_LOCKSTATUS);
        cmd.writeString(dimensioName);
        cmd.writeInt(ratingRegionId);
        sendCmdToTv(cmd, r);
        int size = r.readInt();
        int[] arr =new int[size];
        for (int i=0; i<size; i++) {
            arr[i]=r.readInt();
        }
        return arr;
    }

    public int atscSetVchipUbblock() {
        return sendCmd(DTV_SET_VCHIP_UNBLOCK);
    }

    public VchipLockStatus atscGetCurrentVchipBlock() {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(DTV_GET_CURRENT_VCHIP_BLOCK);
        sendCmdToTv(cmd, r);
        VchipLockStatus lockStatus = new VchipLockStatus();
        lockStatus.blockstatus = r.readInt();
        lockStatus.vchipDimension = r.readString();
        lockStatus.vchipAbbrev = r.readString();
        return lockStatus;
    }

    private void libtv_log_open(){
        if (tvLogFlg) {
            StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
            Log.i(TAG, traceElement.getMethodName());
        }
    }

    public int dtvSetVchipBlockOnOff(int onOff ,int regin_id) {
        int val[] = new int[]{onOff, regin_id};
        return sendCmdIntArray(DTV_SET_VCHIP_BLOCK_ON_OFF, val);
    }

    //Vchip Lock Status
    public class VchipLockStatus {
        public int blockType;
        public int blockstatus;
        public String vchipDimension;
        public String vchipText;
        public String vchipAbbrev;
    }

    public void setVchipLockStatusListener(VchipLockStatusListener l) {
        libtv_log_open();
        mLockStatusListener = l;
    }

    public interface VchipLockStatusListener {
        void onLock(VchipLockStatus lockStatus);
    }

    public enum SOUND_TRACK_MODE {
        SOUND_TRACK_MODE_MONO(0),
        SOUND_TRACK_MODE_STEREO(1),
        SOUND_TRACK_MODE_SAP(2);
        private int val;

        SOUND_TRACK_MODE(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    public int Tv_SetSoundTrackMode(SOUND_TRACK_MODE mode) {
        int val[] = new int[]{mode.toInt()};
        return sendCmdIntArray(TV_SET_SOUND_TRACK_MODE, val);
    }

    public int Tv_GetSoundTrackMode() {
        return sendCmd(TV_GET_SOUND_TRACK_MODE);
    }

    public enum LEFT_RIGHT_SOUND_CHANNEL {
        LEFT_RIGHT_SOUND_CHANNEL_STEREO(0),
        LEFT_RIGHT_SOUND_CHANNEL_LEFT(1),
        LEFT_RIGHT_SOUND_CHANNEL_RIGHT(2),
        LEFT_RIGHT_SOUND_CHANNEL_SWAP(3);
        private int val;

        LEFT_RIGHT_SOUND_CHANNEL(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    public int setLeftRightSondChannel(LEFT_RIGHT_SOUND_CHANNEL mode) {
        int val[] = new int[]{mode.toInt()};
        return sendCmdIntArray(SET_LEFT_RIGHT_SOUND_CHANNEL, val);
    }

    public int getLeftRightSondChannel() {
        return sendCmd(GET_LEFT_RIGHT_SOUND_CHANNEL);
    }

    public class ProgList {
        public String name;
        public int Id;
        public int chanOrderNum;
        public int major;
        public int minor;
        public int type;//service_type
        public int skipFlag;
        public int favoriteFlag;
        public int videoFmt;
        public int tsID;
        public int serviceID;
        public int pcrID;
        public int vPid;
        public ArrayList<DtvAudioTrackInfo> audioInfoList;
        public int chFreq;
    }

    public class FreqList {
        public int ID;
        public int freq;
    }

    /**
     * @Function:GetProgramList
     * @Description,get program list
     * @Param:serType,get diff program list by diff service type
     * @Param:skip,default 0(it shows no skip)
     * @Return:ProgList
     */
    public ArrayList<ProgList> GetProgramList(tv_program_type serType, program_skip_type_e skip) {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        int tmpRet ;
        cmd.writeInt(GET_PROGRAM_LIST);
        cmd.writeInt(serType.toInt());
        cmd.writeInt(skip.toInt());
        sendCmdToTv(cmd, r);
        int size = r.readInt();
        ArrayList<ProgList> pList = new ArrayList<ProgList>();
        for (int i = 0; i < size; i++) {
            ProgList pl = new ProgList();
            pl.Id = r.readInt();
            pl.chanOrderNum = r.readInt();
            pl.major = r.readInt();
            pl.minor = r.readInt();
            pl.type = r.readInt();
            pl.name = r.readString();
            pl.skipFlag = r.readInt();
            pl.favoriteFlag = r.readInt();
            pl.videoFmt = r.readInt();
            pl.tsID = r.readInt();
            pl.serviceID = r.readInt();
            pl.pcrID = r.readInt();
            pl.vPid = r.readInt();
            int trackSize = r.readInt();
            pl.audioInfoList = new ArrayList<DtvAudioTrackInfo>();
            for (int j = 0; j < trackSize; j++) {
                DtvAudioTrackInfo info = new DtvAudioTrackInfo();
                info.language =r.readString();
                info.audio_fmt =r.readInt();
                info.aPid = r.readInt();
                pl.audioInfoList.add(info);
            }
            pl.chFreq = r.readInt();
            pList.add(pl);
        }
        Log.i(TAG,"get prog list size = "+pList.size());
        return pList;
    }

    public enum vpp_display_resolution_t {
        VPP_DISPLAY_RESOLUTION_1366X768(0),
        VPP_DISPLAY_RESOLUTION_1920X1080(1),
        VPP_DISPLAY_RESOLUTION_3840X2160(2),
        VPP_DISPLAY_RESOLUTION_MAX(3);
        private int val;

        vpp_display_resolution_t(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    /**
     * @Function:GetDisplayResolutionConfig
     * @Description, get the display resolution config
     * @Param: none
     * @Return: value refer to enum vpp_display_resolution_t
     */
    public int GetDisplayResolutionConfig() {
        return sendCmd(GET_DISPLAY_RESOLUTION_CONFIG);
    }

    /**
     * @Function:GetDisplayResolutionInfo
     * @Description, get the display resolution info
     * @Param: none
     * @Return: high 16 bits is width, low 16 bits is height
     */
    public int GetDisplayResolutionInfo() {
        return sendCmd(GET_DISPLAY_RESOLUTION_INFO);
    }

    public int GetHdmiHdcpKeyKsvInfo(int data_buf[]) {
        int ret = 0;
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(HDMIRX_GET_KSV_INFO);
        sendCmdToTv(cmd, r);

        ret = r.readInt();
        data_buf[0] = r.readInt();
        data_buf[1] = r.readInt();
        return ret;
    }

    public enum FBCUpgradeState {
        STATE_STOPED(0),
        STATE_RUNNING(1),
        STATE_FINISHED(2),
        STATE_ABORT(3);

        private int val;

        FBCUpgradeState(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    public enum FBCUpgradeErrorCode {
        ERR_SERIAL_CONNECT(-1),
        ERR_OPEN_BIN_FILE(-2),
        ERR_BIN_FILE_SIZE(-3);

        private int val;

        FBCUpgradeErrorCode(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    /**
     * @Function: StartUpgradeFBC
     * @Description: start upgrade fbc
     * @Param: file_name: upgrade bin file name
     *         mode: value refer to enum FBCUpgradeState
     * @Return: 0 success, -1 fail
     */
    public int StartUpgradeFBC(String file_name, int mode) {
        return StartUpgradeFBC(file_name, mode, 0x10000);
    }

    /**
     * @Function: StartUpgradeFBC
     * @Description: start upgrade fbc
     * @Param: file_name: upgrade bin file name
     *         mode: value refer to enum FBCUpgradeState
     *         upgrade_blk_size: upgrade block size (min is 4KB)
     * @Return: 0 success, -1 fail
     */
    public int StartUpgradeFBC(String file_name, int mode, int upgrade_blk_size) {
        libtv_log_open();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        int tmpRet;
        cmd.writeInt(FACTORY_FBC_UPGRADE);
        cmd.writeString(file_name);
        cmd.writeInt(mode);
        cmd.writeInt(upgrade_blk_size);
        sendCmdToTv(cmd, r);
        tmpRet = r.readInt();
        return tmpRet;
    }

    /**
     * @Function: FactorySet_FBC_Brightness
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactorySet_FBC_Brightness(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(FACTORY_FBC_SET_BRIGHTNESS, val);
    }

    /**
     * @Function: FactoryGet_FBC_Brightness
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactoryGet_FBC_Brightness() {
        return sendCmd(FACTORY_FBC_GET_BRIGHTNESS);
    }

    /**
     * @Function: FactorySet_FBC_Contrast
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactorySet_FBC_Contrast(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(FACTORY_FBC_SET_CONTRAST, val);
    }

    /**
     * @Function: FactoryGet_FBC_Contrast
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactoryGet_FBC_Contrast() {
        return sendCmd(FACTORY_FBC_GET_CONTRAST);
    }

    /**
     * @Function: FactorySet_FBC_Saturation
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactorySet_FBC_Saturation(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(FACTORY_FBC_SET_SATURATION, val);
    }

    /**
     * @Function: FactoryGet_FBC_Saturation
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactoryGet_FBC_Saturation() {
        return sendCmd(FACTORY_FBC_GET_SATURATION);
    }

    /**
     * @Function: FactorySet_FBC_HueColorTint
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactorySet_FBC_HueColorTint(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(FACTORY_FBC_SET_HUE, val);
    }

    /**
     * @Function: FactoryGet_FBC_HueColorTint
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactoryGet_FBC_HueColorTint() {
        return sendCmd(FACTORY_FBC_GET_HUE);
    }

    /**
     * @Function: FactorySet_FBC_Backlight
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactorySet_FBC_Backlight(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(FACTORY_FBC_SET_BACKLIGHT, val);
    }

    /**
     * @Function: FactoryGet_FBC_Backlight
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactoryGet_FBC_Backlight() {
        return sendCmd(FACTORY_FBC_GET_BACKLIGHT);
    }

    /**
     * @Function: FactorySet_backlight_onoff
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactorySet_backlight_onoff(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(FACTORY_FBC_SET_BACKLIGHT_EN, val);
    }

    /**
     * @Function: FactoryGet_FBC_Backlight
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactoryGet_backlight_onoff() {
        return sendCmd(FACTORY_FBC_GET_BACKLIGHT_EN);
    }

    /**
     * @Function: FactorySet_SET_LVDS_SSG
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactorySet_SET_LVDS_SSG(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(FACTORY_FBC_SET_LVDS_SSG, val);
    }

    /**
     * @Function: FactorySet_AUTO_ELEC_MODE
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactorySet_AUTO_ELEC_MODE(int mode) {
        int val[] = new int[]{mode};
        return sendCmdIntArray(FACTORY_FBC_SET_ELEC_MODE, val);
    }


    /**
     * @Function: FactoryGet_AUTO_ELEC_MODE
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactoryGet_AUTO_ELEC_MODE() {
        return sendCmd(FACTORY_FBC_GET_ELEC_MODE);
    }

    /**
     * @Function: FactorySet_FBC_Backlight_N360
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactorySet_FBC_Backlight_N360(int mode) {
        int val[] = new int[]{mode};
        return sendCmdIntArray(FACTORY_FBC_SET_BACKLIFHT_N360, val);
    }

    /**
     * @Function: FactoryGet_FBC_Backlight_N360
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactoryGet_FBC_Backlight_N360() {
        return sendCmd(FACTORY_FBC_GET_BACKLIFHT_N360);
    }


    /**
     * @Function: FactorySet_FBC_Picture_Mode
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactorySet_FBC_Picture_Mode(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(FACTORY_FBC_SET_PIC_MODE, val);
    }

    /**
     * @Function: FactoryGet_FBC_Picture_Mode
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactoryGet_FBC_Picture_Mode() {
        return sendCmd(FACTORY_FBC_GET_PIC_MODE);
    }

    /**
     * @Function: FactorySet_FBC_Test_Pattern
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactorySet_FBC_Test_Pattern(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(FACTORY_FBC_SET_TEST_PATTERN, val);
    }

    /**
     * @Function: FactoryGet_FBC_Test_Pattern
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactoryGet_FBC_Test_Pattern() {
        return sendCmd(FACTORY_FBC_GET_TEST_PATTERN);
    }

    /**
     * @Function: FactorySet_FBC_Gain_Red
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactorySet_FBC_Gain_Red(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(FACTORY_FBC_SET_GAIN_RED, val);
    }

    /**
     * @Function: FactoryGet_FBC_Gain_Red
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactoryGet_FBC_Gain_Red() {
        return sendCmd(FACTORY_FBC_GET_GAIN_RED);
    }

    /**
     * @Function: FactorySet_FBC_Gain_Green
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactorySet_FBC_Gain_Green(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(FACTORY_FBC_SET_GAIN_GREEN, val);
    }

    /**
     * @Function: FactoryGet_FBC_Gain_Green
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactoryGet_FBC_Gain_Green() {
        return sendCmd(FACTORY_FBC_GET_GAIN_GREEN);
    }

    /**
     * @Function: FactorySet_FBC_Gain_Blue
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactorySet_FBC_Gain_Blue(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(FACTORY_FBC_SET_GAIN_BLUE, val);
    }

    /**
     * @Function: FactoryGet_FBC_Gain_Blue
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactoryGet_FBC_Gain_Blue() {
        return sendCmd(FACTORY_FBC_GET_GAIN_BLUE);
    }

    /**
     * @Function: FactorySet_FBC_Offset_Red
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactorySet_FBC_Offset_Red(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(FACTORY_FBC_SET_OFFSET_RED, val);
    }

    /**
     * @Function: FactoryGet_FBC_Offset_Red
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactoryGet_FBC_Offset_Red() {
        return sendCmd(FACTORY_FBC_GET_OFFSET_RED);
    }

    /**
     * @Function: FactorySet_FBC_Offset_Green
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactorySet_FBC_Offset_Green(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(FACTORY_FBC_SET_OFFSET_GREEN, val);
    }

    /**
     * @Function: FactoryGet_FBC_Offset_Green
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactoryGet_FBC_Offset_Green() {
        return sendCmd(FACTORY_FBC_GET_OFFSET_GREEN);
    }

    /**
     * @Function: FactorySet_FBC_Offset_Blue
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactorySet_FBC_Offset_Blue(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(FACTORY_FBC_SET_OFFSET_BLUE, val);
    }

    /**
     * @Function: FactoryGet_FBC_Offset_Blue
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactoryGet_FBC_Offset_Blue() {
        return sendCmd(FACTORY_FBC_GET_OFFSET_BLUE);
    }

    /**
     * @Function: FactorySet_FBC_ColorTemp_Mode
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactorySet_FBC_ColorTemp_Mode(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(FACTORY_FBC_SET_COLORTEMP_MODE, val);
    }

    /**
     * @Function: FactoryGet_FBC_ColorTemp_Mode
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactoryGet_FBC_ColorTemp_Mode() {
        return sendCmd(FACTORY_FBC_GET_COLORTEMP_MODE);
    }

    /**
     * @Function: FactorySet_FBC_WB_Initial
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactorySet_FBC_WB_Initial(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(FACTORY_FBC_SET_WB_INIT, val);
    }

    /**
     * @Function: FactoryGet_FBC_WB_Initial
     * @Description:
     * @Param:
     * @Return:
     */
    public int FactoryGet_FBC_WB_Initial() {
        return sendCmd(FACTORY_FBC_GET_WB_INIT);
    }

    public class FBC_MAINCODE_INFO {
        public String Version;
        public String LastBuild;
        public String GitVersion;
        public String GitBranch;
        public String BuildName;
    }

    public FBC_MAINCODE_INFO FactoryGet_FBC_Get_MainCode_Version() {
        FBC_MAINCODE_INFO  info = new FBC_MAINCODE_INFO();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(FACTORY_FBC_GET_MAINCODE_VERSION);
        sendCmdToTv(cmd, r);
        info.Version = r.readString();
        info.LastBuild = r.readString();
        info.GitVersion = r.readString();
        info.GitBranch = r.readString();
        info.BuildName = r.readString();
        return info;
    }

    /**
     * @Function: FactorySet_FBC_Panel_Power_Switch
     * @Description: set fbc panel power switch
     * @Param: value, 0 is fbc panel power off, 1 is panel power on.
     * @Return:
     */
    public int FactorySet_FBC_Panel_Power_Switch(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(FACTORY_FBC_PANEL_POWER_SWITCH, val);
    }

    /**
     * @Function: FactorySet_FBC_SN_Info
     * @Description: set SN info to FBC save
     * @Param: strFactorySN is string to set. len is SN length
     * @Return 0 is success,else is fail:
     */
    public int FactorySet_FBC_SN_Info(String strFactorySN,int len) {
        String val[] = new String[]{strFactorySN};
        return sendCmdStringArray(FACTORY_SET_SN, val);
    }

    public String FactoryGet_FBC_SN_Info() {
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(FACTORY_GET_SN);
        sendCmdToTv(cmd, r);
        return r.readString();
    }

    public String FactorySet_FBC_Panel_Get_Info() {
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(FACTORY_FBC_PANEL_GET_INFO);
        sendCmdToTv(cmd, r);
        return r.readString();
    }

    //@:value ,default 0
    public int FactorySet_FBC_Panel_Suspend(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(FACTORY_FBC_PANEL_SUSPEND, val);
    }

    public int SetKalaokIOLevel(int level) {
        int val[] = new int[]{level};
        return sendCmdIntArray(SET_KALAOK_IO_LEVEL, val);
    }

    //@:value ,default 0
    public int FactorySet_FBC_Panel_User_Setting_Default(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(FACTORY_FBC_PANEL_USER_SETTING_DEFAULT, val);
    }

    /**
     * @Function: Read the red gain with specified souce and color temperature
     * @Param:
     * @ Return value: the red gain value
     * */
    public int FactoryWhiteBalanceSetRedGain(int sourceType, int colorTemp_mode, int value) {
        int val[] = new int[]{sourceType, colorTemp_mode, value};
        return sendCmdIntArray(FACTORY_WHITE_BALANCE_SET_GAIN_RED, val);
    }

    public int FactoryWhiteBalanceSetGreenGain(int sourceType, int colorTemp_mode, int value) {
        int val[] = new int[]{sourceType, colorTemp_mode, value};
        return sendCmdIntArray(FACTORY_WHITE_BALANCE_SET_GAIN_GREEN, val);
    }

    public int FactoryWhiteBalanceSetBlueGain(int sourceType, int colorTemp_mode, int value) {
        int val[] = new int[]{sourceType, colorTemp_mode, value};
        return sendCmdIntArray(FACTORY_WHITE_BALANCE_SET_GAIN_BLUE, val);
    }

    public int FactoryWhiteBalanceGetRedGain(int sourceType, int colorTemp_mode) {
        int val[] = new int[]{sourceType, colorTemp_mode};
        return sendCmdIntArray(FACTORY_WHITE_BALANCE_GET_GAIN_RED, val);
    }

    public int FactoryWhiteBalanceGetGreenGain(int sourceType, int colorTemp_mode) {
        int val[] = new int[]{sourceType, colorTemp_mode};
        return sendCmdIntArray(FACTORY_WHITE_BALANCE_GET_GAIN_GREEN, val);
    }

    public int FactoryWhiteBalanceGetBlueGain(int sourceType, int colorTemp_mode) {
        int val[] = new int[]{sourceType, colorTemp_mode};
        return sendCmdIntArray(FACTORY_WHITE_BALANCE_GET_GAIN_BLUE, val);
    }

    public int FactoryWhiteBalanceSetRedOffset(int sourceType, int colorTemp_mode, int value) {
        int val[] = new int[]{sourceType, colorTemp_mode, value};
        return sendCmdIntArray(FACTORY_WHITE_BALANCE_SET_OFFSET_RED, val);
    }

    public int FactoryWhiteBalanceSetGreenOffset(int sourceType, int colorTemp_mode, int value) {
        int val[] = new int[]{sourceType, colorTemp_mode, value};
        return sendCmdIntArray(FACTORY_WHITE_BALANCE_SET_OFFSET_GREEN, val);
    }

    public int FactoryWhiteBalanceSetBlueOffset(int sourceType, int colorTemp_mode, int value) {
        int val[] = new int[]{sourceType, colorTemp_mode, value};
        return sendCmdIntArray(FACTORY_WHITE_BALANCE_SET_OFFSET_BLUE, val);
    }

    public int FactoryWhiteBalanceGetRedOffset(int sourceType, int colorTemp_mode) {
        int val[] = new int[]{sourceType, colorTemp_mode};
        return sendCmdIntArray(FACTORY_WHITE_BALANCE_GET_OFFSET_RED, val);
    }

    public int FactoryWhiteBalanceGetGreenOffset(int sourceType, int colorTemp_mode) {
        int val[] = new int[]{sourceType, colorTemp_mode};
        return sendCmdIntArray(FACTORY_WHITE_BALANCE_GET_OFFSET_GREEN, val);
    }

    public int FactoryWhiteBalanceGetBlueOffset(int sourceType, int colorTemp_mode) {
        int val[] = new int[]{sourceType, colorTemp_mode};
        return sendCmdIntArray(FACTORY_WHITE_BALANCE_GET_OFFSET_BLUE, val);
    }

    public int FactoryWhiteBalanceSetColorTemperature(int sourceType, int colorTemp_mode, int is_save) {
        int val[] = new int[]{sourceType, colorTemp_mode, is_save};
        return sendCmdIntArray(FACTORY_WHITE_BALANCE_SET_COLOR_TMP, val);
    }

    public int FactoryWhiteBalanceGetColorTemperature(int sourceType) {
        int val[] = new int[]{sourceType};
        return sendCmdIntArray(FACTORY_WHITE_BALANCE_GET_COLOR_TMP, val);
    }

    /**
     * @Function: Save the white balance data to fbc or g9
     * @Param:
     * @Return value: save OK: 0 , else -1
     *
     * */
    public int FactoryWhiteBalanceSaveParameters(int sourceType, int colorTemp_mode, int r_gain, int g_gain, int b_gain, int r_offset, int g_offset, int b_offset) {
        int val[] = new int[]{sourceType, colorTemp_mode,
            r_gain, g_gain, b_gain, r_offset, g_offset, b_offset};
        return sendCmdIntArray(FACTORY_WHITE_BALANCE_SAVE_PRAMAS, val);
    }

    public class WhiteBalanceParams {
        public int r_gain;        // u1.10, range 0~2047, default is 1024 (1.0x)
        public int g_gain;        // u1.10, range 0~2047, default is 1024 (1.0x)
        public int b_gain;        // u1.10, range 0~2047, default is 1024 (1.0x)
        public int r_offset;      // s11.0, range -1024~+1023, default is 0
        public int g_offset;      // s11.0, range -1024~+1023, default is 0
        public int b_offset;      // s11.0, range -1024~+1023, default is 0
    }

    public WhiteBalanceParams FactoryWhiteBalanceGetAllParams(int colorTemp_mode) {
        WhiteBalanceParams params = new WhiteBalanceParams();
        Parcel cmd = Parcel.obtain();
        Parcel r = Parcel.obtain();
        cmd.writeInt(FACTORY_WHITE_BALANCE_GET_ALL_PRAMAS);
        cmd.writeInt(colorTemp_mode);
        sendCmdToTv(cmd, r);
        int ret = r.readInt();
        if (ret == 0) {
            params.r_gain = r.readInt();
            params.g_gain = r.readInt();
            params.b_gain = r.readInt();
            params.r_offset = r.readInt();
            params.g_offset = r.readInt();
            params.b_offset = r.readInt();
        }

        return params;
    }

    public int FactoryWhiteBalanceOpenGrayPattern() {
        return sendCmd(FACTORY_WHITE_BALANCE_OPEN_GRAY_PATTERN);
    }

    public int FactoryWhiteBalanceCloseGrayPattern() {
        return sendCmd(FACTORY_WHITE_BALANCE_CLOSE_GRAY_PATTERN);
    }

    public int FactoryWhiteBalanceSetGrayPattern(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(FACTORY_WHITE_BALANCE_SET_GRAY_PATTERN, val);
    }

    public int FactoryWhiteBalanceGetGrayPattern(int value) {
        return sendCmd(FACTORY_WHITE_BALANCE_GET_GRAY_PATTERN);
    }

    public int Factory_FBC_Get_LightSensor_Status_N310() {
        return sendCmd(FACTROY_FBC_GET_LIGHT_SENSOR_STATUS_N310);
    }

    public int Factory_FBC_Set_LightSensor_Status_N310(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(FACTROY_FBC_SET_LIGHT_SENSOR_STATUS_N310, val);
    }

    public int Factory_FBC_Get_DreamPanel_Status_N310() {
        return sendCmd(FACTROY_FBC_GET_DREAM_PANEL_STATUS_N310);
    }

    public int Factory_FBC_Set_DreamPanel_Status_N310(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(FACTROY_FBC_SET_DREAM_PANEL_STATUS_N310, val);
    }

    public int Factory_FBC_Get_MULT_PQ_Status_N310() {
        return sendCmd(FACTROY_FBC_GET_MULT_PQ_STATUS_N310);
    }

    public int Factory_FBC_Set_MULT_PQ_Status_N310(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(FACTROY_FBC_SET_MULT_PQ_STATUS_N310, val);
    }

    public int Factory_FBC_Get_MEMC_Status_N310() {
        return sendCmd(FACTROY_FBC_GET_MEMC_STATUS_N310);
    }

    public int Factory_FBC_Set_MEMC_Status_N310(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(FACTROY_FBC_SET_MEMC_STATUS_N310, val);
    }

    //value:
    /*
    #define REBOOT_FLAG_NORMAL              0x00000000
    #define REBOOT_FLAG_UPGRADE             0x80808080
    #define REBOOT_FLAG_UPGRADE2            0x88888888      // reserved
    #define REBOOT_FLAG_SUSPEND             0x12345678*/
    public int FactorySet_FBC_Power_Reboot(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(FACTORY_FBC_POWER_REBOOT, val);
    }

    //ref to FBC include/key_const.h
    public static final int AML_FBC_KEY_NOP                                 = 0;
    public static final int AML_FBC_KEY_NUM_PLUS10                          = 1;
    public static final int AML_FBC_KEY_NUM_0                               = 2;
    public static final int AML_FBC_KEY_NUM_1                               = 3;
    public static final int AML_FBC_KEY_NUM_2                               = 4;
    public static final int AML_FBC_KEY_NUM_3                               = 5;
    public static final int AML_FBC_KEY_NUM_4                               = 6;
    public static final int AML_FBC_KEY_NUM_5                               = 7;
    public static final int AML_FBC_KEY_NUM_6                               = 8;
    public static final int AML_FBC_KEY_NUM_7                               = 9;
    public static final int AML_FBC_KEY_NUM_8                               = 10;
    public static final int AML_FBC_KEY_NUM_9                               = 11;
    public static final int AML_FBC_KEY_UP                                  = 12;
    public static final int AML_FBC_KEY_DOWN                                = 13;
    public static final int AML_FBC_KEY_LEFT                                = 14;
    public static final int AML_FBC_KEY_RIGHT                               = 15;
    public static final int AML_FBC_KEY_ENTER                               = 16;
    public static final int AML_FBC_KEY_EXIT                                = 17;
    public static final int AML_FBC_KEY_PAGE_UP                             = 18;
    public static final int AML_FBC_KEY_PAGE_DOWN                           = 19;
    public static final int AML_FBC_KEY_POWER                               = 20;
    public static final int AML_FBC_KEY_SLEEP                               = 21;
    public static final int AML_FBC_KEY_HOME                                = 22;
    public static final int AML_FBC_KEY_SETUP                               = 23;
    public static final int AML_FBC_KEY_OSD                                 = 24;
    public static final int AML_FBC_KEY_MENU                                = 25;
    public static final int AML_FBC_KEY_DISPLAY                             = 26;
    public static final int AML_FBC_KEY_MARK                                = 27;
    public static final int AML_FBC_KEY_CLEAR                               = 28;
    public static final int AML_FBC_KEY_PLAY_PAUSE                          = 29;
    public static final int AML_FBC_KEY_STOP                                = 30;
    public static final int AML_FBC_KEY_PAUSE                               = 31;
    public static final int AML_FBC_KEY_NEXT_CHAP                           = 32;
    public static final int AML_FBC_KEY_PREVIOUS_CHAP                       = 33;
    public static final int AML_FBC_KEY_FAST_FORWARD                        = 34;
    public static final int AML_FBC_KEY_FAST_BACKWARD                       = 35;
    public static final int AML_FBC_KEY_REPEAT                              = 36;
    public static final int AML_FBC_KEY_PLAY_MODE                           = 37;
    public static final int AML_FBC_KEY_SLIDE_SHOW                          = 38;
    public static final int AML_FBC_KEY_MUTE                                = 39;
    public static final int AML_FBC_KEY_VOL_MINUS                           = 40;
    public static final int AML_FBC_KEY_VOL_PLUS                            = 41;
    public static final int AML_FBC_KEY_ZOOM                                = 42;
    public static final int AML_FBC_KEY_ROTATE                              = 43;
    public static final int AML_FBC_KEY_MOUSE_L_DOWN                        = 44;
    public static final int AML_FBC_KEY_MOUSE_L_UP                          = 45;
    public static final int AML_FBC_KEY_MOUSE_R_DOWN                        = 46;
    public static final int AML_FBC_KEY_MOUSE_R_UP                          = 47;
    public static final int AML_FBC_KEY_MOUSE_M_DOWN                        = 48;
    public static final int AML_FBC_KEY_MOUSE_M_UP                          = 49;
    public static final int AML_FBC_KEY_MOUSE_ROLL_DOWN                     = 50;
    public static final int AML_FBC_KEY_MOUSE_ROLL_UP                       = 51;
    public static final int AML_FBC_KEY_MOUSE_MOVE                          = 52;
    public static final int AML_FBC_KEY_LONG_EXIT                           = 53;
    public static final int AML_FBC_KEY_LONG_RIGHT                          = 54;
    public static final int AML_FBC_KEY_LONG_LEFT                           = 55;
    public static final int AML_FBC_KEY_LONG_DOWN                           = 56;
    public static final int AML_FBC_KEY_LONG_UP                             = 57;
    public static final int AML_FBC_KEY_LONG_ENTER                          = 58;
    public static final int AML_FBC_KEY_LONG_MENU                           = 59;
    public static final int AML_FBC_KEY_OPEN_CLOSE                          = 60;
    public static final int AML_FBC_KEY_NTSC_PAL                            = 61;
    public static final int AML_FBC_KEY_PROGRESSIVE                         = 62;
    public static final int AML_FBC_KEY_TITLE_CALL                          = 63;
    public static final int AML_FBC_KEY_AUDIO                               = 64;
    public static final int AML_FBC_KEY_SUBPICTURE                          = 65;
    public static final int AML_FBC_KEY_ANGLE                               = 66;
    public static final int AML_FBC_KEY_AB_PLAY                             = 67;
    public static final int AML_FBC_KEY_RECODE                              = 68;
    public static final int AML_FBC_KEY_SHORTCUT                            = 69;
    public static final int AML_FBC_KEY_ORIGINAL                            = 70;
    public static final int AML_FBC_KEY_BOOKING                             = 71;
    public static final int AML_FBC_KEY_ORDER_SYSTEM                        = 72;
    public static final int AML_FBC_KEY_SOUND_CTRL                          = 73;
    public static final int AML_FBC_KEY_FUNCTION                            = 74;
    public static final int AML_FBC_KEY_SCHEDULE                            = 75;
    public static final int AML_FBC_KEY_FAVOR                               = 76;
    public static final int AML_FBC_KEY_RELATION                            = 77;
    public static final int AML_FBC_KEY_FIRST                               = 78;
    public static final int AML_FBC_KEY_DELETE                              = 79;
    public static final int AML_FBC_KEY_SLIDE_RELEASE                       = 80;
    public static final int AML_FBC_KEY_SLIDE_TOUCH                         = 81;
    public static final int AML_FBC_KEY_SLIDE_LEFT                          = 82;
    public static final int AML_FBC_KEY_SLIDE_RIGHT                         = 83;
    public static final int AML_FBC_KEY_SLIDE_UP                            = 84;
    public static final int AML_FBC_KEY_SLIDE_DOWN                          = 85;
    public static final int AML_FBC_KEY_SLIDE_CLOCKWISE                     = 86;
    public static final int AML_FBC_KEY_SLIDE_ANTI_CLOCKWISE                = 87;
    public static final int AML_FBC_KEY_SLIDE_UP_LEFT                       = 88;
    public static final int AML_FBC_KEY_SLIDE_UP_RIGHT                      = 89;
    public static final int AML_FBC_KEY_SLIDE_DOWN_LEFT                     = 90;
    public static final int AML_FBC_KEY_SLIDE_DOWN_RIGHT                    = 91;
    public static final int AML_FBC_KEY_SLIDE_NULL                          = 92;
    public static final int AML_FBC_KEY_MENU_CALL                           = 93;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_A                  = 94;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_B                  = 95;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_C                  = 96;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_D                  = 97;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_E                  = 98;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_F                  = 99;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_G                  = 100;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_H                  = 101;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_I                  = 102;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_J                  = 103;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_K                  = 104;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_L                  = 105;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_M                  = 106;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_N                  = 107;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_O                  = 108;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_P                  = 109;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_Q                  = 110;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_R                  = 111;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_S                  = 112;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_T                  = 113;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_U                  = 114;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_V                  = 115;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_W                  = 116;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_X                  = 117;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_Y                  = 118;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_Z                  = 119;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_0                  = 120;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_1                  = 121;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_2                  = 122;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_3                  = 123;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_4                  = 124;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_5                  = 125;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_6                  = 126;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_7                  = 127;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_8                  = 128;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_9                  = 129;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_ENTER              = 130;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_SPACE              = 131;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_BACKSPACE          = 132;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_ESC                = 133;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_CAESURA_SIGN       = 134;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_SUBTRACTION_SIGN   = 135;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_EQUALS_SIGN        = 136;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_LEFT_BRACKET       = 137;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_RIGHT_BRACKET      = 138;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_BACKSLASH          = 139;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_SEMICOLON          = 140;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_QUOTATION_MARK     = 141;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_COMMA              = 142;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_POINT              = 143;
    public static final int AML_FBC_KEY_AML_FBC_KEYBOARD_SLASH              = 144;
    public static final int AML_FBC_KEY_ALARM_SET                           = 145;
    public static final int AML_FBC_KEY_ALARM_OFF                           = 146;
    public static final int AML_FBC_KEY_IPOD_PLAY_PAUSE                     = 147;
    public static final int AML_FBC_KEY_B_TIME_SET_MINUS                    = 148;
    public static final int AML_FBC_KEY_B_TIME_SET_PLUS                     = 149;
    public static final int AML_FBC_KEY_ALARM_B_ONOFF                       = 150;
    public static final int AML_FBC_KEY_SNOOZE_BRIGHTNESS                   = 151;
    public static final int AML_FBC_KEY_ALARM_A_ONOFF                       = 152;
    public static final int AML_FBC_KEY_A_TIME_SET_MINUS                    = 153;
    public static final int AML_FBC_KEY_A_TIME_SET_PLUS                     = 154;
    public static final int AML_FBC_KEY_BACK                                = 155;
    public static final int AML_FBC_KEY_RADIO_BAND                          = 156;
    public static final int AML_FBC_KEY_OPTION                              = 157;
    public static final int AML_FBC_KEY_LONG_B_TIME_SET_MINUS               = 158;
    public static final int AML_FBC_KEY_LONG_B_TIME_SET_PLUS                = 159;
    public static final int AML_FBC_KEY_LONG_A_TIME_SET_MINUS               = 160;
    public static final int AML_FBC_KEY_LONG_A_TIME_SET_PLUS                = 161;
    public static final int AML_FBC_KEY_LONG2_B_TIME_SET_MINUS              = 162;
    public static final int AML_FBC_KEY_LONG2_B_TIME_SET_PLUS               = 163;
    public static final int AML_FBC_KEY_LONG2_A_TIME_SET_MINUS              = 164;
    public static final int AML_FBC_KEY_LONG2_A_TIME_SET_PLUS               = 165;
    public static final int AML_FBC_KEY_LONG2_LEFT                          = 166;
    public static final int AML_FBC_KEY_LONG2_RIGHT                         = 167;
    public static final int AML_FBC_KEY_LONGRLS_LEFT                        = 168;
    public static final int AML_FBC_KEY_LONGRLS_RIGHT                       = 169;
    public static final int AML_FBC_KEY_LONGRLS_B_TIME_SET_MINUS            = 170;
    public static final int AML_FBC_KEY_LONGRLS_B_TIME_SET_PLUS             = 171;
    public static final int AML_FBC_KEY_LONGRLS_A_TIME_SET_MINUS            = 172;
    public static final int AML_FBC_KEY_LONGRLS_A_TIME_SET_PLUS             = 173;
    public static final int AML_FBC_KEY_LONG2RLS_LEFT                       = 174;
    public static final int AML_FBC_KEY_LONG2RLS_RIGHT                      = 175;
    public static final int AML_FBC_KEY_LONG2RLS_B_TIME_SET_MINUS           = 176;
    public static final int AML_FBC_KEY_LONG2RLS_B_TIME_SET_PLUS            = 177;
    public static final int AML_FBC_KEY_LONG2RLS_A_TIME_SET_MINUS           = 178;
    public static final int AML_FBC_KEY_LONG2RLS_A_TIME_SET_PLUS            = 179;

    //@param:keyCode  AML_FBC_KEY_XXX   param:default 0
    public int FactorySet_FBC_SEND_KEY_TO_FBC(int keyCode, int param) {
        int val[] = new int[]{keyCode, param};
        return sendCmdIntArray(FACTORY_FBC_SEND_KEY_TO_FBC, val);
    }

    /**
     * @Description:copy srcPath to desPath
     * @Return:0 success,-1 fail
     */
    public int CopyFile (String srcPath,String desPath) {
        String val[] = new String[]{srcPath, desPath};
        return sendCmdStringArray(FACTORY_COPY_FILE, val);
    }

    /**
     * @Function: TvMiscChannelExport
     * @Description: export the /param/dtv.db file to the udisk
     * @Param: none
     * @Return: 0 success , -1 copy fail , -2 other
     */
    public int TvMiscChannelExport(String destPath) {
        String val[] = new String[]{destPath};
        return sendCmdStringArray(MISC_CHANNEL_EXPORT, val);
    }

    /**
     * @Function: TvMiscChannelImport
     * @Description: import the dtv.db file from the udisk to the /param directory
     * @Param: none
     * @Return: 0 success , -1 copy fail , -2 other
     */
    public int TvMiscChannelImport(String srcPath) {
        String val[] = new String[]{srcPath};
        return sendCmdStringArray(MISC_CHANNEL_IMPORT, val);
    }

    // set listener when not need to listen set null

    public final static int EVENT_AV_PLAYBACK_NODATA            = 1;
    public final static int EVENT_AV_PLAYBACK_RESUME            = 2;
    public final static int EVENT_AV_SCRAMBLED                  = 3;
    public final static int EVENT_AV_UNSUPPORT                  = 4;

    public interface AVPlaybackListener {
        void onEvent(int msgType, int programID);
    };

    public void SetAVPlaybackListener(AVPlaybackListener l) {
        libtv_log_open();
        mAVPlaybackListener = l;
    }

    public static final int TV_ERROR_UNKNOWN = 1;
    public static final int TV_ERROR_SERVER_DIED = 100;

    public interface ErrorCallback {
        void onError(int error, TvControlManager tv);
    };

    public void SetSigInfoChangeListener(TVInSignalInfo.SigInfoChangeListener l) {
        libtv_log_open();
        mSigInfoChangeLister = l;
    }

    public void SetSigChannelSearchListener(TVInSignalInfo.SigChannelSearchListener l) {
        libtv_log_open();
        mSigChanSearchListener = l;
    }

    public enum CC_VGA_AUTO_ADJUST_STATUS {
        CC_VGA_AUTO_ADJUST_START(0),
        CC_VGA_AUTO_ADJUST_SUCCESS(1),
        CC_VGA_AUTO_ADJUST_FAILED(-1),
        CC_VGA_AUTO_ADJUST_CURTIMMING_FAILED(-2),
        CC_VGA_AUTO_ADJUST_PARA_FAILED(-3),
        CC_VGA_AUTO_ADJUST_TERMINATED(-4),
        CC_VGA_AUTO_ADJUST_IDLE(-5);
        private int val;
        CC_VGA_AUTO_ADJUST_STATUS(int val) {
            this.val = val;
        }
        public int toInt() {
            return this.val;
        }
    }

    public interface VGAAdjustChangeListener {
        void onVGAAdjustChange(int state);
    };

    public void SetVGAChangeListener(VGAAdjustChangeListener l) {
        libtv_log_open();
        mVGAChangeListener = l;
    }

    public interface Status3DChangeListener {
        void onStatus3DChange(int state);
    }

    public interface StatusTVChangeListener {
        void onStatusTVChange(int type,int state,int mode,int freq,int para1,int para2);
    }

    public interface StatusSourceConnectListener {
        void onSourceConnectChange(SourceInput source, int msg);
    }

    public void SetSourceConnectListener(StatusSourceConnectListener l) {
        libtv_log_open();
        mSourceConnectChangeListener = l;
    }

    public interface HDMIRxCECListener {
        void onHDMIRxCECMessage(int msg_len, int msg_buf[]);
    }

    public void SetHDMIRxCECListener(HDMIRxCECListener l) {
        libtv_log_open();
        mHDMIRxCECListener = l;
    }

    public interface UpgradeFBCListener {
        void onUpgradeStatus(int state, int param);
    }

    public void SetUpgradeFBCListener(UpgradeFBCListener l) {
        libtv_log_open();
        mUpgradeFBCListener = l;
    }

    public void SetStatus3DChangeListener(Status3DChangeListener l) {
        libtv_log_open();
        mStatus3DChangeListener = l;
    }

    public void SetStatusTVChangeListener(StatusTVChangeListener l) {
        libtv_log_open();
        mStatusTVChangeListener = l;
    }

    public interface DreamPanelChangeListener {
        void onDreamPanelChange(int msg_pdu[]);
    };

    public void SetDreamPanelChangeListener(DreamPanelChangeListener l) {
        libtv_log_open();
        mDreamPanelChangeListener = l;
    }

    public interface AdcCalibrationListener {
        void onAdcCalibrationChange(int state);
    }

    public void SetAdcCalibrationListener(AdcCalibrationListener l) {
        libtv_log_open();
        mAdcCalibrationListener = l;
    }

    public interface SourceSwitchListener {
        void onSourceSwitchStatusChange(SourceInput input, int state);
    }

    public void SetSourceSwitchListener(SourceSwitchListener l) {
        libtv_log_open();
        mSourceSwitchListener = l;
    }

    public interface ChannelSelectListener {
        void onChannelSelect(int msg_pdu[]);
    }

    public void SetChannelSelectListener(ChannelSelectListener l) {
        libtv_log_open();
        mChannelSelectListener = l;
    }

    public final void setErrorCallback(ErrorCallback cb) {
        libtv_log_open();
        mErrorCallback = cb;
    }

    public interface SerialCommunicationListener {
        //dev_id, refer to enum SerialDeviceID
        void onSerialCommunication(int dev_id, int msg_len, int msg_pdu[]);
    };

    public void SetSerialCommunicationListener(SerialCommunicationListener l) {
        libtv_log_open();
        mSerialCommunicationListener = l;
    }

    public interface CloseCaptionListener {
        void onCloseCaptionProcess(int data_buf[], int cmd_buf[]);
    };

    public void SetCloseCaptionListener(CloseCaptionListener l) {
        libtv_log_open();
        mCloseCaptionListener = l;
    }

    // 3D
    public enum Mode_3D {
        MODE_3D_CLOSE(0),
        MODE_3D_AUTO(1),
        //        MODE_3D_2D_TO_3D(2),
        MODE_3D_LEFT_RIGHT(2),
        MODE_3D_UP_DOWN(3),
        MODE_3D_LINE_ALTERNATIVE(4),
        MODE_3D_FRAME_ALTERNATIVE(5),
        MODE_3D_MAX(6);

        private int val;

        Mode_3D(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    public enum Tvin_3d_Status {
        STATUS3D_DISABLE(0),
        STATUS3D_AUTO(1),
        //        STATUS3D_2D_TO_3D(2),
        STATUS3D_LR(2),
        STATUS3D_BT(3),
        STATUS3D_LINE_ALTERNATIVE(4),
        STATUS3D_FRAME_ALTERNATIVE(5),
        STATUS3D_MAX(6);
        private int val;

        Tvin_3d_Status(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    public enum Mode_3D_2D {
        MODE_3D_2D_CLOSE(0),
        MODE_3D_2D_LEFT(1),
        MODE_3D_2D_RIGHT(2);

        private int val;

        Mode_3D_2D(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    public int Get3DStatus() {
        return sendCmd(GET_3D_STATUS);
    }

    public int Set3DMode(Mode_3D mode, Tvin_3d_Status status) {
        int val[] = new int[]{mode.toInt(), status.toInt()};
        return sendCmdIntArray(SET_3D_MODE, val);
    }


    public int Get3DMode() {
        return sendCmd(GET_3D_MODE);
    }

    public int Set3DLRSwith(int on_off, Tvin_3d_Status status) {
        int val[] = new int[]{on_off, status.toInt()};
        return sendCmdIntArray(SET_3D_LR_SWITH, val);
    }

    public int Get3DLRSwith() {
        return sendCmd(GET_3D_LR_SWITH);
    }

    public int Set3DTo2DMode(Mode_3D_2D mode, Tvin_3d_Status status) {
        int val[] = new int[]{mode.toInt(), status.toInt()};
        return sendCmdIntArray(SET_3D_TO_2D_MODE, val);
    }

    public int Get3DTo2DMode() {
        return sendCmd(GET_3D_TO_2D_MODE);
    }

    public int Set3DDepth(int value) {
        int val[] = new int[]{value};
        return sendCmdIntArray(SET_3D_DEPTH, val);
    }

    public int Get3DDepth() {
        return sendCmd(GET_3D_DEPTH);
    }
    // 3D END

    public enum SourceInput {
        TV(0),
        AV1(1),
        AV2(2),
        YPBPR1(3),
        YPBPR2(4),
        HDMI1(5),
        HDMI2(6),
        HDMI3(7),
        VGA(8),
        XXXX(9),//not use MPEG source
        DTV(10),
        SVIDEO(11),
        HDMI4K2K(12),
        USB4K2K(13),
        IPTV(14),
        DUMMY(15),
        MAX(16);
        private int val;

        SourceInput(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    public enum SourceInput_Type {
        SOURCE_TYPE_TV(0),
        SOURCE_TYPE_AV(1),
        SOURCE_TYPE_COMPONENT(2),
        SOURCE_TYPE_HDMI(3),
        SOURCE_TYPE_VGA(4),
        SOURCE_TYPE_MPEG(5),//only use for vpp, for display ,not a source
        SOURCE_TYPE_DTV(6),
        SOURCE_TYPE_SVIDEO(7),
        SOURCE_TYPE_HDMI_4K2K(8),
        SOURCE_TYPE_USB_4K2K(9),
        SOURCE_TYPE_MAX(7);

        private int val;

        SourceInput_Type(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    public enum tvin_color_system_e {
        COLOR_SYSTEM_AUTO(0),
        COLOR_SYSTEM_PAL(1),
        COLOR_SYSTEM_NTSC(2),
        COLOR_SYSTEM_SECAM(3),
        COLOR_SYSTEM_MAX(4);
        private int val;

        tvin_color_system_e(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    public enum tv_program_type {//program_type
        TV_PROGRAM_UNKNOWN(0),
        TV_PROGRAM_DTV(1),
        TV_PROGRAM_DRADIO(2),
        TV_PROGRAM_ATV(3);
        private int val;

        tv_program_type(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    public enum program_skip_type_e {
        TV_PROGRAM_SKIP_NO(0),
        TV_PROGRAM_SKIP_YES(1),
        TV_PROGRAM_SKIP_UNKNOWN(2);

        private int val;

        program_skip_type_e(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    public enum atsc_attenna_type_t {
        AM_ATSC_ATTENNA_TYPE_MIX(0),
        AM_ATSC_ATTENNA_TYPE_AIR(1),
        AM_ATSC_ATTENNA_TYPE_CABLE_STD(2),
        AM_ATSC_ATTENNA_TYPE_CABLE_IRC(3),
        AM_ATSC_ATTENNA_TYPE_CABLE_HRC(4),
        AM_ATSC_ATTENNA_TYPE_MAX(5);

        private int val;
        atsc_attenna_type_t(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    public enum HdmiPortID {
        HDMI_PORT_1(1),
        HDMI_PORT_2(2),
        HDMI_PORT_3(3);
        private int val;

        HdmiPortID(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    public enum HdmiEdidVer {
        HDMI_EDID_VER_14(0),
        HDMI_EDID_VER_20(1);
        private int val;

        HdmiEdidVer(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }

    public enum HdcpKeyIsEnable {
        hdcpkey_enable(0),
        hdcpkey_disable(1);
        private int val;

        HdcpKeyIsEnable(int val) {
            this.val = val;
        }

        public int toInt() {
            return this.val;
        }
    }
}

