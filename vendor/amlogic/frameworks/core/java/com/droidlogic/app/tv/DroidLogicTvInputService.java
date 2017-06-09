package com.droidlogic.app.tv;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.droidlogic.app.tv.ChannelInfo;
import com.droidlogic.app.tv.DroidLogicTvUtils;
import com.droidlogic.app.tv.TvControlManager;
import com.droidlogic.app.tv.TVInSignalInfo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.media.tv.TvInputHardwareInfo;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.media.tv.TvInputService;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.media.tv.TvContract.Channels;

public class DroidLogicTvInputService extends TvInputService implements TVInSignalInfo.SigInfoChangeListener, TvControlManager.StorDBEventListener, TvControlManager.ScanningFrameStableListener
{
    private static final String TAG = DroidLogicTvInputService.class.getSimpleName();
    private static final boolean DEBUG = true;

    private SparseArray<TvInputInfo> mInfoList = new SparseArray<>();

    private TvInputBaseSession mSession;
    private String mCurrentInputId;
    private int c_displayNum = 0;
    private TvDataBaseManager mTvDataBaseManager;
    private TvControlManager mTvControlManager;

    /**
     * inputId should get from subclass which must invoke {@link super#onCreateSession(String)}
     */
    @Override
    public Session onCreateSession(String inputId)
    {
        mCurrentInputId = inputId;
        return null;
    }

    /**
     * get session has been created by {@code onCreateSession}, and input id of session.
     * @param session {@link HdmiInputSession} or {@link AVInputSession}
     */
    protected void registerInputSession(TvInputBaseSession session)
    {
        Log.d(TAG, "registerInputSession");
        mSession = session;
        mTvControlManager = TvControlManager.getInstance();
        mTvControlManager.SetSigInfoChangeListener(this);
        mTvControlManager.setStorDBListener(this);
        mTvControlManager.setScanningFrameStableListener(this);
        mTvDataBaseManager = new TvDataBaseManager(getApplicationContext());
    }

    /**
     * update {@code mInfoList} when hardware device is added or removed.
     * @param hInfo {@linkHardwareInfo} get from HAL.
     * @param info {@link TvInputInfo} will be added or removed.
     * @param isRemoved {@code true} if you want to remove info. {@code false} otherwise.
     */
    protected void updateInfoListIfNeededLocked(TvInputHardwareInfo hInfo,
            TvInputInfo info, boolean isRemoved)
    {
        if (isRemoved)
        {
            mInfoList.remove(hInfo.getDeviceId());
        }
        else
        {
            mInfoList.put(hInfo.getDeviceId(), info);
        }

        if (DEBUG)
            Log.d(TAG, "size of mInfoList is " + mInfoList.size());
    }

    protected boolean hasInfoExisted(TvInputHardwareInfo hInfo) {
        return mInfoList.get(hInfo.getDeviceId()) == null ? false : true;
    }

    protected TvInputInfo getTvInputInfo(TvInputHardwareInfo hardwareInfo)
    {
        return mInfoList.get(hardwareInfo.getDeviceId());
    }

    protected int getHardwareDeviceId(String input_id)
    {
        int id = 0;
        for (int i = 0; i < mInfoList.size(); i++)
        {
            if (input_id.equals(mInfoList.valueAt(i).getId()))
            {
                id = mInfoList.keyAt(i);
                break;
            }
        }

        if (DEBUG)
            Log.d(TAG, "device id is " + id);
        return id;
    }

    protected String getTvInputInfoLabel(int device_id)
    {
        String label = null;
        switch (device_id)
        {
        case DroidLogicTvUtils.DEVICE_ID_ATV:
            label = ChannelInfo.LABEL_ATV;
            break;
        case DroidLogicTvUtils.DEVICE_ID_DTV:
            label = ChannelInfo.LABEL_DTV;
            break;
        case DroidLogicTvUtils.DEVICE_ID_AV1:
            label = ChannelInfo.LABEL_AV1;
            break;
        case DroidLogicTvUtils.DEVICE_ID_AV2:
            label = ChannelInfo.LABEL_AV2;
            break;
        case DroidLogicTvUtils.DEVICE_ID_HDMI1:
            label = ChannelInfo.LABEL_HDMI1;
            break;
        case DroidLogicTvUtils.DEVICE_ID_HDMI2:
            label = ChannelInfo.LABEL_HDMI2;
            break;
        case DroidLogicTvUtils.DEVICE_ID_HDMI3:
            label = ChannelInfo.LABEL_HDMI3;
            break;
        default:
            break;
        }
        return label;
    }

    protected ResolveInfo getResolveInfo(String cls_name)
    {
        if (TextUtils.isEmpty(cls_name))
            return null;
        ResolveInfo ret_ri = null;
        Context context = getApplicationContext();

        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> services = pm.queryIntentServices(
                                         new Intent(TvInputService.SERVICE_INTERFACE),
                                         PackageManager.GET_SERVICES | PackageManager.GET_META_DATA);

        for (ResolveInfo ri : services)
        {
            ServiceInfo si = ri.serviceInfo;
            if (!android.Manifest.permission.BIND_TV_INPUT.equals(si.permission))
            {
                continue;
            }

            if (DEBUG)
                Log.d(TAG, "cls_name = " + cls_name + ", si.name = " + si.name);

            if (cls_name.equals(si.name))
            {
                ret_ri = ri;
                break;
            }
        }
        return ret_ri;
    }

    protected void stopTv()
    {
        Log.d(TAG, "stop tv, mCurrentInputId =" + mCurrentInputId);
        mTvControlManager.StopTv();
    }

    protected void releasePlayer()
    {
        mTvControlManager.StopPlayProgram();
    }

    private String getInfoLabel()
    {
        TvInputManager tim = (TvInputManager) getSystemService(Context.TV_INPUT_SERVICE);
        return tim.getTvInputInfo(mCurrentInputId).loadLabel(this).toString();
    }

    @Override
    public void onSigChange(TVInSignalInfo signal_info)
    {
        TVInSignalInfo.SignalStatus status = signal_info.sigStatus;

        if (DEBUG)
            Log.d(TAG, "onSigChange" + status.ordinal() + status.toString());

        if (status == TVInSignalInfo.SignalStatus.TVIN_SIG_STATUS_NOSIG
                || status == TVInSignalInfo.SignalStatus.TVIN_SIG_STATUS_NULL
                || status == TVInSignalInfo.SignalStatus.TVIN_SIG_STATUS_NOTSUP)
        {
            mSession.notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_UNKNOWN);
        }
        else if (status == TVInSignalInfo.SignalStatus.TVIN_SIG_STATUS_STABLE)
        {
            mSession.notifyVideoAvailable();
            int device_id = mSession.getDeviceId();
            String[] strings;
            Bundle bundle = new Bundle();
            switch (device_id)
            {
            case DroidLogicTvUtils.DEVICE_ID_HDMI1:
            case DroidLogicTvUtils.DEVICE_ID_HDMI2:
            case DroidLogicTvUtils.DEVICE_ID_HDMI3:
                if (DEBUG)
                    Log.d(TAG, "signal_info.fmt.toString() for hdmi=" + signal_info.sigFmt.toString());

                strings = signal_info.sigFmt.toString().split("_");
                TVInSignalInfo.SignalFmt fmt = signal_info.sigFmt;
                if (fmt == TVInSignalInfo.SignalFmt.TVIN_SIG_FMT_HDMI_1440X480I_60HZ
                        || fmt == TVInSignalInfo.SignalFmt.TVIN_SIG_FMT_HDMI_1440X480I_120HZ
                        || fmt == TVInSignalInfo.SignalFmt.TVIN_SIG_FMT_HDMI_1440X480I_240HZ
                        || fmt == TVInSignalInfo.SignalFmt.TVIN_SIG_FMT_HDMI_2880X480I_60HZ
                        || fmt == TVInSignalInfo.SignalFmt.TVIN_SIG_FMT_HDMI_2880X480I_60HZ)
                {
                    strings[4] = "480I";
                }
                else if (fmt == TVInSignalInfo.SignalFmt.TVIN_SIG_FMT_HDMI_1440X576I_50HZ
                         || fmt == TVInSignalInfo.SignalFmt.TVIN_SIG_FMT_HDMI_1440X576I_100HZ
                         || fmt == TVInSignalInfo.SignalFmt.TVIN_SIG_FMT_HDMI_1440X576I_200HZ)
                {
                    strings[4] = "576I";
                }

                bundle.putInt(DroidLogicTvUtils.SIG_INFO_TYPE, DroidLogicTvUtils.SIG_INFO_TYPE_HDMI);
                bundle.putString(DroidLogicTvUtils.SIG_INFO_LABEL, getInfoLabel());
                if (strings != null && strings.length <= 4)
                    bundle.putString(DroidLogicTvUtils.SIG_INFO_ARGS, "0_0HZ");
                else
                    bundle.putString(DroidLogicTvUtils.SIG_INFO_ARGS, strings[4]
                                     + "_" + signal_info.reserved + "HZ");
                mSession.notifySessionEvent(DroidLogicTvUtils.SIG_INFO_EVENT, bundle);
                break;
            case DroidLogicTvUtils.DEVICE_ID_AV1:
            case DroidLogicTvUtils.DEVICE_ID_AV2:
                if (DEBUG)
                    Log.d(TAG, "tmpInfo.fmt.toString() for av=" + signal_info.sigFmt.toString());

                strings = signal_info.sigFmt.toString().split("_");
                bundle.putInt(DroidLogicTvUtils.SIG_INFO_TYPE, DroidLogicTvUtils.SIG_INFO_TYPE_AV);
                bundle.putString(DroidLogicTvUtils.SIG_INFO_LABEL, getInfoLabel());
                if (strings != null && strings.length <= 4)
                    bundle.putString(DroidLogicTvUtils.SIG_INFO_ARGS, "");
                else
                    bundle.putString(DroidLogicTvUtils.SIG_INFO_ARGS, strings[4]);
                mSession.notifySessionEvent(DroidLogicTvUtils.SIG_INFO_EVENT, bundle);
                break;
            case DroidLogicTvUtils.DEVICE_ID_ATV:
                if (DEBUG)
                    Log.d(TAG, "tmpInfo.fmt.toString() for atv=" + signal_info.sigFmt.toString());

                mSession.notifySessionEvent(DroidLogicTvUtils.SIG_INFO_EVENT, null);
                break;
            case DroidLogicTvUtils.DEVICE_ID_DTV:
                if (DEBUG)
                    Log.d(TAG, "tmpInfo.fmt.toString() for dtv=" + signal_info.sigFmt.toString());

                mSession.notifySessionEvent(DroidLogicTvUtils.SIG_INFO_EVENT, null);
                break;
            default:
                break;
            }
        }
    }

    public static String mode2type(int mode)
    {
        String type = "";
        switch (mode)
        {
        case TVChannelParams.MODE_DTMB:
            type = Channels.TYPE_DTMB;
            break;
        case TVChannelParams.MODE_QPSK:
            type = Channels.TYPE_DVB_S;
            break;
        case TVChannelParams.MODE_QAM:
            type = Channels.TYPE_DVB_C;
            break;
        case TVChannelParams.MODE_OFDM:
            type = Channels.TYPE_DVB_T;
            break;
        case TVChannelParams.MODE_ATSC:
            type = Channels.TYPE_ATSC_C;
            break;
        case TVChannelParams.MODE_ANALOG:
            type = Channels.TYPE_PAL;
            break;
        case TVChannelParams.MODE_ISDBT:
            type = Channels.TYPE_ISDB_T;
            break;
        default:
            break;
        }
        return type;
    }
    private int getidxByDefLan(String[] strArray)
    {
        if (strArray == null)
            return 0;
        String[] defLanArray = {"chi", "zho", "ita", "spa", "ara"};
        for (int i = 0; i < strArray.length; i++)
        {
            for (String lan : defLanArray)
            {
                if (strArray[i].equals(lan))
                    return i;
            }
        }
        return 0;
    }
    private ChannelInfo createAtvChannelInfo (TvControlManager.ScannerEvent event)
    {
        String ATVName = "ATV program";
        return new ChannelInfo.Builder()
               .setInputId(mCurrentInputId == null ? "NULL" : mCurrentInputId)
               .setType(mode2type(event.mode))
               .setServiceType(Channels.SERVICE_TYPE_AUDIO_VIDEO)//default is SERVICE_TYPE_AUDIO_VIDEO
               .setServiceId(0)
               .setDisplayNumber(Integer.toString(c_displayNum))
               .setDisplayName(ATVName)
               .setLogoUrl(null)
               .setOriginalNetworkId(0)
               .setTransportStreamId(0)
               .setVideoPid(0)
               .setVideoStd(event.videoStd)
               .setVfmt(0)
               .setVideoWidth(0)
               .setVideoHeight(0)
               .setAudioPids(null)
               .setAudioFormats(null)
               .setAudioLangs(null)
               .setAudioStd(event.audioStd)
               .setIsAutoStd(event.isAutoStd)
               .setAudioTrackIndex(0)
               .setAudioCompensation(0)
               .setPcrPid(0)
               .setFrequency(event.freq)
               .setBandwidth(0)
               .setFineTune(0)
               .setBrowsable(true)
               .setIsFavourite(false)
               .setPassthrough(false)
               .setLocked(false)
               .setDisplayNameMulti("xxx" + ATVName)
               .build();
    }

    private ChannelInfo createDtvChannelInfo (TvControlManager.ScannerEvent event)
    {
        String name = null;
        String serviceType;

        try
        {
            name = TVMultilingualText.getText(event.programName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            name = "????";
        }

        if (event.srvType == 1)
        {
            serviceType = Channels.SERVICE_TYPE_AUDIO_VIDEO;
        }
        else if (event.srvType == 2)
        {
            serviceType = Channels.SERVICE_TYPE_AUDIO;
        }
        else
        {
            serviceType = Channels.SERVICE_TYPE_OTHER;
        }

        return new ChannelInfo.Builder()
               .setInputId(mCurrentInputId)
               .setType(mode2type(event.mode))
               .setServiceType(serviceType)
               .setServiceId(event.serviceID)
               .setDisplayNumber(Integer.toString(c_displayNum))
               .setDisplayName(name)
               .setLogoUrl(null)
               .setOriginalNetworkId(event.orig_net_id)
               .setTransportStreamId(event.ts_id)
               .setVideoPid(event.vid)
               .setVideoStd(0)
               .setVfmt(event.vfmt)
               .setVideoWidth(0)
               .setVideoHeight(0)
               .setAudioPids(event.aids)
               .setAudioFormats(event.afmts)
               .setAudioLangs(event.alangs)
               .setAudioStd(0)
               .setIsAutoStd(event.isAutoStd)
               //.setAudioTrackIndex(getidxByDefLan(event.alangs))
               .setAudioCompensation(0)
               .setPcrPid(event.pcr)
               .setFrequency(event.freq)
               .setBandwidth(event.bandwidth)
               .setFineTune(0)
               .setBrowsable(true)
               .setIsFavourite(false)
               .setPassthrough(false)
               .setLocked(false)
               .setSubtitleTypes(event.stypes)
               .setSubtitlePids(event.sids)
               .setSubtitleStypes(event.sstypes)
               .setSubtitleId1s(event.sid1s)
               .setSubtitleId2s(event.sid2s)
               .setSubtitleLangs(event.slangs)
               //.setSubtitleTrackIndex(getidxByDefLan(event.slangs))
               .setDisplayNameMulti(event.programName)
               .setFreeCa(event.free_ca)
               .setScrambled(event.scrambled)
               .setSdtVersion(event.sdtVersion)
               .build();
    }

    public static class ScanMode
    {
        private int scanMode;

        ScanMode(int ScanMode)
        {
            scanMode = ScanMode;
        }
        public int getMode()
        {
            return (scanMode >> 24) & 0xf;
        }
        public int getATVMode()
        {
            return (scanMode >> 16) & 0xf;
        }
        public int getDTVMode()
        {
            return (scanMode & 0xFFFF);
        }

        public boolean isDTVManulScan()
        {
            return (getATVMode() == 0x7) && (getDTVMode() == 0x2);
        }
        public boolean isATVScan()
        {
            return (getATVMode() != 0x7) && (getDTVMode() == 0x7);
        }
        public boolean isATVManualScan()
        {
            return (getATVMode() == 0x2) && (getDTVMode() == 0x7);
        }
    }

    private ScanMode mScanMode = null;


    @Override
    public void StorDBonEvent(TvControlManager.ScannerEvent event)
    {
        ChannelInfo channel = null;
        String name = null;
        Log.e(TAG, "onEvent:" + event.type + " :" + c_displayNum);
        Bundle bundle = null;
        switch (event.type)
        {
        case TvControlManager.EVENT_SCAN_BEGIN:
            Log.d(TAG, "Scan begin");
            mScanMode = new ScanMode(event.scan_mode);
            c_displayNum = 0;
            onTVChannelStoreBegin();//for ATV
            bundle = getBundleByScanEvent(event);
            mSession.notifySessionEvent(DroidLogicTvUtils.SIG_INFO_C_SCAN_BEGIN_EVENT, bundle);
            break;
        case TvControlManager.EVENT_DTV_PROG_DATA:
            channel = createDtvChannelInfo(event);
            onDTVChannelStore(event, channel);
            mTvDataBaseManager.insertDtvChannel(channel, c_displayNum);
            Log.d(TAG, "onEvent,displayNum: " + c_displayNum);
            channel.print();
            bundle = GetDisplayNumBunlde(c_displayNum);
            mSession.notifySessionEvent(DroidLogicTvUtils.SIG_INFO_C_DISPLAYNUM_EVENT, bundle);
            c_displayNum++;
            break;
        case TvControlManager.EVENT_SCAN_PROGRESS:
            Log.d(TAG, event.precent + "%\tfreq[" + event.freq + "] lock[" + event.lock + "] strength[" + event.strength + "] quality[" + event.quality + "]");
            bundle = getBundleByScanEvent(event);
            if ((event.mode == TVChannelParams.MODE_ANALOG) && (event.lock == 0x11))
            {
                bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_DISPLAYNUM, c_displayNum);
                c_displayNum++;
            }
            mSession.notifySessionEvent(DroidLogicTvUtils.SIG_INFO_C_PROCESS_EVENT, bundle);
            break;
        case TvControlManager.EVENT_ATV_PROG_DATA:
            channel = createAtvChannelInfo(event);
            if (mScanMode.isATVManualScan())
                onUpdateCurrentChannel(channel, true);
            else
                mTvDataBaseManager.insertAtvChannel(channel, c_displayNum);
            Log.d(TAG, "onEvent,displayNum:" + c_displayNum);
            channel.print();
            bundle = GetDisplayNumBunlde(c_displayNum);
            mSession.notifySessionEvent(DroidLogicTvUtils.SIG_INFO_C_DISPLAYNUM_EVENT, bundle);
            c_displayNum++;
            break;
        case TvControlManager.EVENT_STORE_BEGIN:
            Log.d(TAG, "Store begin");
            c_displayNum = 0;
            onTVChannelStoreBegin();
            bundle = getBundleByScanEvent(event);
            mSession.notifySessionEvent(DroidLogicTvUtils.SIG_INFO_C_STORE_BEGIN_EVENT, bundle);
            break;

        case TvControlManager.EVENT_STORE_END:
            Log.d(TAG, "Store end");
            c_displayNum = 0;
            onTVChannelStoreEnd();
            bundle = getBundleByScanEvent(event);
            mSession.notifySessionEvent(DroidLogicTvUtils.SIG_INFO_C_STORE_END_EVENT, bundle);
            break;
        case TvControlManager.EVENT_SCAN_END:
            Log.d(TAG, "Scan end");
            c_displayNum = 0;
            bundle = getBundleByScanEvent(event);
            mTvControlManager.DtvStopScan();
            mSession.notifySessionEvent(DroidLogicTvUtils.SIG_INFO_C_SCAN_END_EVENT, bundle);
            break;
        case TvControlManager.EVENT_SCAN_EXIT:
            Log.d(TAG, "Scan exit.");
            c_displayNum = 0;
            bundle = getBundleByScanEvent(event);
            mSession.notifySessionEvent(DroidLogicTvUtils.SIG_INFO_C_SCAN_EXIT_EVENT, bundle);
            break;
        default:
            break;
        }
    }

    private Bundle getBundleByScanEvent(TvControlManager.ScannerEvent mEvent)
    {
        Bundle bundle = new Bundle();
        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_TYPE, mEvent.type);
        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_PRECENT, mEvent.precent);
        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_TOTALCOUNT, mEvent.totalcount);
        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_LOCK, mEvent.lock);
        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_CNUM, mEvent.cnum);
        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_FREQ, mEvent.freq);
        bundle.putString(DroidLogicTvUtils.SIG_INFO_C_PROGRAMNAME, mEvent.programName);
        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_SRVTYPE, mEvent.srvType);
        bundle.putString(DroidLogicTvUtils.SIG_INFO_C_MSG, mEvent.msg);
        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_STRENGTH, mEvent.strength);
        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_QUALITY, mEvent.quality);
        //ATV
        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_VIDEOSTD, mEvent.videoStd);
        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_AUDIOSTD, mEvent.audioStd);
        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_ISAUTOSTD, mEvent.isAutoStd);
        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_FINETUNE, mEvent.fineTune);
        //DTV
        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_MODE, mEvent.mode);
        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_SR, mEvent.sr);
        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_MOD, mEvent.mod);
        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_BANDWIDTH, mEvent.bandwidth);
        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_OFM_MODE, mEvent.ofdm_mode);
        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_TS_ID, mEvent.ts_id);
        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_ORIG_NET_ID, mEvent.orig_net_id);
        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_SERVICEiD, mEvent.serviceID);
        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_VID, mEvent.vid);
        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_VFMT, mEvent.vfmt);
        bundle.putIntArray(DroidLogicTvUtils.SIG_INFO_C_AIDS, mEvent.aids);
        bundle.putIntArray(DroidLogicTvUtils.SIG_INFO_C_AFMTS, mEvent.afmts);
        bundle.putStringArray(DroidLogicTvUtils.SIG_INFO_C_ALANGS, mEvent.alangs);
        bundle.putIntArray(DroidLogicTvUtils.SIG_INFO_C_ATYPES, mEvent.atypes);
        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_PCR, mEvent.pcr);

        bundle.putIntArray(DroidLogicTvUtils.SIG_INFO_C_STYPES, mEvent.stypes);
        bundle.putIntArray(DroidLogicTvUtils.SIG_INFO_C_SIDS, mEvent.sids);
        bundle.putIntArray(DroidLogicTvUtils.SIG_INFO_C_SSTYPES, mEvent.sstypes);
        bundle.putIntArray(DroidLogicTvUtils.SIG_INFO_C_SID1S, mEvent.sid1s);
        bundle.putIntArray(DroidLogicTvUtils.SIG_INFO_C_SID2S, mEvent.sid2s);
        bundle.putStringArray(DroidLogicTvUtils.SIG_INFO_C_SLANGS, mEvent.slangs);

        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_DISPLAYNUM, -1);

        return bundle;
    }

    private Bundle GetDisplayNumBunlde(int displayNum)
    {
        Bundle bundle = new Bundle();

        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_DISPLAYNUM, displayNum);

        return bundle;
    }


    private boolean need_delete_channel = false;
    private ArrayList<ChannelInfo> mChannels = null;

    private boolean on_dtv_channel_store_firsttime = true;

    private void onTVChannelStoreBegin()
    {
        if (mScanMode == null
                || (!mScanMode.isDTVManulScan() && !mScanMode.isATVScan()))
            return ;

        String InputId = mSession.getInputId();
        mChannels = mTvDataBaseManager.getChannelList(InputId, Channels.SERVICE_TYPE_AUDIO_VIDEO);
        mChannels.addAll(mTvDataBaseManager.getChannelList(InputId, Channels.SERVICE_TYPE_AUDIO));

        on_dtv_channel_store_firsttime = true;

        c_displayNum = mChannels.size();
        Log.d(TAG, "Store> channels exist:" + c_displayNum);
    }

    private void onDTVChannelStore(TvControlManager.ScannerEvent event, ChannelInfo channel)
    {
        if (mScanMode == null || !mScanMode.isDTVManulScan())
            return ;

        if (on_dtv_channel_store_firsttime)
        {

            on_dtv_channel_store_firsttime = false;

            Iterator<ChannelInfo> iter = mChannels.iterator();
            while (iter.hasNext())
            {
                ChannelInfo c = iter.next();
                if (c.getFrequency() != channel.getFrequency())
                    iter.remove();
            }

            need_delete_channel = true;
        }
        Log.d(TAG, "insert [" + channel.getNumber() + "][" + channel.getFrequency() + "][" + channel.getServiceType() + "][" + channel.getDisplayName() + "]");
    }

    private void onTVChannelStoreEnd()
    {
        if (mScanMode == null
                || (!mScanMode.isDTVManulScan() && !mScanMode.isATVScan()))
            return ;

        if (need_delete_channel)
        {
            mTvDataBaseManager.deleteChannelsContinuous(mChannels);

            for (ChannelInfo c : mChannels)
                Log.d(TAG, "rm ch[" + c.getNumber() + "][" + c.getDisplayName() + "][" + c.getFrequency() + "]");
        }

        on_dtv_channel_store_firsttime = true;
        need_delete_channel = false;
        mChannels = null;
    }

    @Override
    public void onFrameStable(TvControlManager.ScanningFrameStableEvent event)
    {
        Log.d(TAG, "scanning frame stable!");
        Bundle bundle = new Bundle();
        bundle.putInt(DroidLogicTvUtils.SIG_INFO_C_FREQ, event.CurScanningFrq);
        mSession.notifySessionEvent(DroidLogicTvUtils.SIG_INFO_C_SCANNING_FRAME_STABLE_EVENT, bundle);
    }

    public void onUpdateCurrentChannel(ChannelInfo channel, boolean store) {
    }

}
