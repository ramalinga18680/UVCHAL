package com.droidlogic.app.tv;

import android.database.Cursor;
import android.media.tv.TvContract;
import android.media.tv.TvContract.Channels;
import android.net.Uri;
import android.util.Log;
import android.text.TextUtils;

import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

public class ChannelInfo {
    private static final String TAG = "ChannelInfo";

    public static final String[] COMMON_PROJECTION = {
        Channels._ID,
        Channels.COLUMN_INPUT_ID,
        Channels.COLUMN_TYPE,
        Channels.COLUMN_SERVICE_TYPE,
        Channels.COLUMN_SERVICE_ID,
        Channels.COLUMN_DISPLAY_NUMBER,
        Channels.COLUMN_DISPLAY_NAME,
        Channels.COLUMN_ORIGINAL_NETWORK_ID,
        Channels.COLUMN_TRANSPORT_STREAM_ID,
        Channels.COLUMN_VIDEO_FORMAT,
        Channels.COLUMN_INTERNAL_PROVIDER_DATA,
        Channels.COLUMN_BROWSABLE,
    };

    public static final String[] SIMPLE_PROJECTION = {
        TvContract.Channels._ID,
        TvContract.Channels.COLUMN_DISPLAY_NUMBER,
        TvContract.Channels.COLUMN_ORIGINAL_NETWORK_ID,
        TvContract.Channels.COLUMN_TRANSPORT_STREAM_ID,
        TvContract.Channels.COLUMN_SERVICE_ID
    };

    public static final String KEY_DISPLAY_NUMBER = "display_number";

    public static final String KEY_TYPE = "type";
    public static final String KEY_SERVICE_TYPE = "service_type";

    //need sync with TvContract, different from KEY_VFMT
    public static final String KEY_VIDEO_FORMAT = "video_format";
    public static final String KEY_VFMT = "vfmt";//used for play programs
    public static final String KEY_VIDEO_PID = "video_pid";
    public static final String KEY_VIDEO_STD = "video_std";

    public static final String KEY_AUDIO_PIDS = "audio_pids";
    public static final String KEY_AUDIO_FORMATS = "AUDIO_FORMATS";
    public static final String KEY_AUDIO_LANGS = "audio_langs";
    public static final String KEY_AUDIO_TRACK_INDEX = "audio_track_index";
    public static final String KEY_AUDIO_COMPENSATION = "audio_compensation";
    public static final String KEY_AUDIO_CHANNEL = "audio_channel";
    public static final String KEY_AUDIO_STD = "audio_std";
    public static final String KEY_IS_AUTO_STD = "is_auto_std";

    public static final String KEY_PCR_ID = "pcr_id";

    public static final String KEY_SUBT_TYPES = "subt_types";
    public static final String KEY_SUBT_PIDS = "subt_pids";
    public static final String KEY_SUBT_STYPES = "subt_stypes";
    public static final String KEY_SUBT_ID1S = "subt_id1s";
    public static final String KEY_SUBT_ID2S = "subt_id2s";
    public static final String KEY_SUBT_LANGS = "subt_langs";
    public static final String KEY_SUBT_TRACK_INDEX = "subt_track_index";

    public static final String KEY_MULTI_NAME = "multi_name";

    public static final String KEY_FREE_CA = "free_ca";
    public static final String KEY_SCRAMBLED = "scrambled";

    public static final String KEY_SDT_VERSION = "sdt_version";

    public static final String KEY_FREQUENCY = "frequency";
    public static final String KEY_BAND_WIDTH = "band_width";
    public static final String KEY_FINE_TUNE = "fine_tune";
    public static final String KEY_IS_FAVOURITE = "is_favourite";

    public static final String EXTRA_CHANNEL_INFO = "extra_channel_info";

    public static final String LABEL_ATV = "ATV";
    public static final String LABEL_DTV = "DTV";
    public static final String LABEL_AV1 = "AV1";
    public static final String LABEL_AV2 = "AV2";
    public static final String LABEL_HDMI1 = "HDMI1";
    public static final String LABEL_HDMI2 = "HDMI2";
    public static final String LABEL_HDMI3 = "HDMI3";

    private long mId;
    private String mInputId;
    private String mType;
    private String mServiceType;
    private int mServiceId;
    private String mDisplayNumber;
    private int mNumber;
    private String mDisplayName;
    private String mVideoFormat;
    private String mLogoUrl;
    private int mOriginalNetworkId;
    private int mTransportStreamId;

    private int mVideoPid;
    private int mVideoStd;
    private int mVfmt;
    private int mVideoWidth;
    private int mVideoHeight;

    private int mAudioPids[];
    private int mAudioFormats[];
    private String mAudioLangs[];
    private int mAudioStd;
    private int mIsAutoStd;
    private int mAudioTrackIndex; //-1:not select, -2:off, >=0:index
    private int mAudioCompensation;
    private int mAudioChannel;

    private int mPcrPid;

    private int mSubtitleTypes[];
    private int mSubtitlePids[];
    private int mSubtitleStypes[];
    private int mSubtitleId1s[];
    private int mSubtitleId2s[];
    private String mSubtitleLangs[];
    private int mSubtitleTrackIndex;//-1:not select, -2:off, >=0:index

    private int mFrequency;
    private int mBandwidth;
    private int mFineTune;

    private boolean mBrowsable;
    private boolean mIsFavourite;
    private boolean mLocked;
    private boolean mIsPassthrough;

    private String mDisplayNameMulti;//multi-language

    private int mFreeCa;
    private int mScrambled;

    private int mSdtVersion;

    private ChannelInfo() {}

    public static ChannelInfo createPassthroughChannel(Uri paramUri) {
        if (!TvContract.isChannelUriForPassthroughInput(paramUri))
            throw new IllegalArgumentException("URI is not a passthrough channel URI");
        return createPassthroughChannel((String)paramUri.getPathSegments().get(1));
    }

    public static ChannelInfo createPassthroughChannel(String input_id) {
        return new Builder().setInputId(input_id).setPassthrough(true).build();
    }

    public static ChannelInfo fromCommonCursor(Cursor cursor) {
        Builder builder = new Builder();

        int index = cursor.getColumnIndex(Channels._ID);
        if (index >= 0)
            builder.setId(cursor.getLong(index));
        index = cursor.getColumnIndex(Channels.COLUMN_INPUT_ID);
        if (index >= 0)
            builder.setInputId(cursor.getString(index));
        index = cursor.getColumnIndex(Channels.COLUMN_TYPE);
        if (index >= 0)
            builder.setType(cursor.getString(index));
        index = cursor.getColumnIndex(Channels.COLUMN_SERVICE_TYPE);
        if (index >= 0)
            builder.setServiceType(cursor.getString(index));
        index = cursor.getColumnIndex(Channels.COLUMN_SERVICE_ID);
        if (index >= 0)
            builder.setServiceId(cursor.getInt(index));
        index = cursor.getColumnIndex(Channels.COLUMN_DISPLAY_NUMBER);
        if (index >= 0)
            builder.setDisplayNumber(cursor.getString(index));
        index = cursor.getColumnIndex(Channels.COLUMN_DISPLAY_NAME);
        if (index >= 0)
            builder.setDisplayName(cursor.getString(index));
        index = cursor.getColumnIndex(Channels.COLUMN_ORIGINAL_NETWORK_ID);
        if (index >= 0)
            builder.setOriginalNetworkId(cursor.getInt(index));
        index = cursor.getColumnIndex(Channels.COLUMN_TRANSPORT_STREAM_ID);
        if (index >= 0)
            builder.setTransportStreamId(cursor.getInt(index));
        index = cursor.getColumnIndex(Channels.COLUMN_VIDEO_FORMAT);
        if (index >= 0)
            builder.setVideoFormat(cursor.getString(index));
        index = cursor.getColumnIndex(Channels.COLUMN_INTERNAL_PROVIDER_DATA);
        if (index >= 0) {
            Map<String, String> parsedMap = stringToMap(cursor.getString(index));
            if (parsedMap.get(KEY_AUDIO_PIDS) != null) {
                String[] str_audioPids = parsedMap.get(KEY_AUDIO_PIDS).replace("[", "").replace("]", "").split(", ");
                String[] str_audioFormats = parsedMap.get(KEY_AUDIO_FORMATS).replace("[", "").replace("]", "").split(", ");
                int number = (str_audioPids[0].compareTo("null") == 0)? 0 : str_audioPids.length;
                int[] audioPids = null;
                int[] audioFormats = null;
                String[] audioLangs = null;
                if (number > 0) {
                    audioPids = new int[number];
                    audioFormats = new int[number];
                    audioLangs = new String[number];
                    for (int i=0; i < str_audioPids.length; i++) {
                        audioPids[i] = Integer.parseInt(str_audioPids[i]);
                        audioFormats[i] = Integer.parseInt(str_audioFormats[i]);
                    }
                    audioLangs = parsedMap.get(KEY_AUDIO_LANGS).replace("[", "").replace("]", "").split(", ");
                    builder.setAudioPids(audioPids);
                    builder.setAudioFormats(audioFormats);
                    builder.setAudioLangs(audioLangs);
                }
            }else {
                builder.setAudioPids(null);
                builder.setAudioFormats(null);
                builder.setAudioLangs(null);
            }
            if (parsedMap.get(KEY_VFMT) != null)
                builder.setVfmt(Integer.parseInt(parsedMap.get(KEY_VFMT)));
            if (parsedMap.get(KEY_FREQUENCY) != null)
                builder.setFrequency(Integer.parseInt(parsedMap.get(KEY_FREQUENCY)));
            if (parsedMap.get(KEY_BAND_WIDTH) != null)
            builder.setBandwidth(Integer.parseInt(parsedMap.get(KEY_BAND_WIDTH)));
            if (parsedMap.get(KEY_VIDEO_PID) != null)
                builder.setVideoPid(Integer.parseInt(parsedMap.get(KEY_VIDEO_PID)));
            if (parsedMap.get(KEY_PCR_ID) != null)
            builder.setPcrPid(Integer.parseInt(parsedMap.get(KEY_PCR_ID)));

            if (parsedMap.get(KEY_AUDIO_TRACK_INDEX) != null)
                builder.setAudioTrackIndex(Integer.parseInt(parsedMap.get(KEY_AUDIO_TRACK_INDEX)));
            if (parsedMap.get(KEY_AUDIO_COMPENSATION) != null)
                builder.setAudioCompensation(Integer.parseInt(parsedMap.get(KEY_AUDIO_COMPENSATION)));
            if (parsedMap.get(KEY_AUDIO_CHANNEL) != null)
                builder.setAudioChannel(Integer.parseInt(parsedMap.get(KEY_AUDIO_CHANNEL)));
            if (parsedMap.get(KEY_IS_FAVOURITE) != null)
                builder.setIsFavourite(parsedMap.get(KEY_IS_FAVOURITE).equals("1") ? true : false);
            if (parsedMap.get(KEY_VIDEO_STD) != null)
                builder.setVideoStd(Integer.parseInt(parsedMap.get(KEY_VIDEO_STD)));
            if (parsedMap.get(KEY_AUDIO_STD) != null)
                builder.setAudioStd(Integer.parseInt(parsedMap.get(KEY_AUDIO_STD)));
            if (parsedMap.get(KEY_IS_AUTO_STD) != null)
                builder.setIsAutoStd(Integer.parseInt(parsedMap.get(KEY_IS_AUTO_STD)));
            if (parsedMap.get(KEY_FINE_TUNE) != null)
                builder.setFineTune(Integer.parseInt(parsedMap.get(KEY_FINE_TUNE)));

            if (parsedMap.get(KEY_SUBT_PIDS) != null) {
                String[] str_subtPids = parsedMap.get(KEY_SUBT_PIDS).replace("[", "").replace("]", "").split(", ");
                int subtNumber = (str_subtPids[0].compareTo("null") == 0)? 0 : str_subtPids.length;
                if (subtNumber > 0) {
                    String[] str_subtTypes = parsedMap.get(KEY_SUBT_TYPES).replace("[", "").replace("]", "").split(", ");
                    String[] str_subtStypes = parsedMap.get(KEY_SUBT_STYPES).replace("[", "").replace("]", "").split(", ");
                    String[] str_subtId1s = parsedMap.get(KEY_SUBT_ID1S).replace("[", "").replace("]", "").split(", ");
                    String[] str_subtId2s = parsedMap.get(KEY_SUBT_ID2S).replace("[", "").replace("]", "").split(", ");
                    int[] subtTypes = new int[subtNumber];
                    int[] subtStypes = new int[subtNumber];
                    int[] subtPids = new int[subtNumber];
                    int[] subtId1s = new int[subtNumber];
                    int[] subtId2s = new int[subtNumber];


                    for (int i=0; i < subtNumber; i++) {
                        subtTypes[i] = Integer.parseInt(str_subtTypes[i]);
                        subtStypes[i] = Integer.parseInt(str_subtStypes[i]);
                        subtPids[i] = Integer.parseInt(str_subtPids[i]);
                        subtId1s[i] = Integer.parseInt(str_subtId1s[i]);
                        subtId2s[i] = Integer.parseInt(str_subtId2s[i]);
                    }
                    String[] subtLangs = parsedMap.get(KEY_SUBT_LANGS).replace("[", "").replace("]", "").split(", ");

                    builder.setSubtitlePids(subtPids)
                            .setSubtitleId1s(subtId1s)
                            .setSubtitleId2s(subtId2s)
                            .setSubtitleTypes(subtTypes)
                            .setSubtitleStypes(subtStypes)
                            .setSubtitleLangs(subtLangs);
                }
            }

            if (parsedMap.get(KEY_SUBT_TRACK_INDEX) != null)
                builder.setSubtitleTrackIndex(Integer.parseInt(parsedMap.get(KEY_SUBT_TRACK_INDEX)));

            if (parsedMap.get(KEY_MULTI_NAME) != null)
                builder.setDisplayNameMulti(parsedMap.get(KEY_MULTI_NAME));

            if (parsedMap.get(KEY_FREE_CA) != null)
                builder.setFreeCa(Integer.parseInt(parsedMap.get(KEY_FREE_CA)));
            if (parsedMap.get(KEY_SCRAMBLED) != null)
                builder.setScrambled(Integer.parseInt(parsedMap.get(KEY_SCRAMBLED)));
            if (parsedMap.get(KEY_SDT_VERSION) != null)
                builder.setSdtVersion(Integer.parseInt(parsedMap.get(KEY_SDT_VERSION)));
        }

        index = cursor.getColumnIndex(Channels.COLUMN_BROWSABLE);
        if (index >= 0)
            builder.setBrowsable(cursor.getInt(index)==1 ? true : false);

        return builder.build();
    }

    public long getId() {
        return mId;
    }

    public String getInputId() {
        return mInputId;
    }

    public String getType() {
        return mType;
    }

    public String getServiceType() {
        return mServiceType;
    }

    public int getServiceId() {
        return mServiceId;
    }

    public String getDisplayNumber() {
        return mDisplayNumber;
    }

    public int getNumber() {
        return mNumber;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public String getLogoUrl() {
        return mLogoUrl;
    }

    public int getOriginalNetworkId() {
        return mOriginalNetworkId;
    }

    public int getTransportStreamId() {
        return mTransportStreamId;
    }

    public int getVfmt() {
        return mVfmt;
    }

    public int getVideoPid() {
        return mVideoPid;
    }

    public int getVideoStd() {
        return mVideoStd;
    }

    public String getVideoFormat() {
        return mVideoFormat;
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    public int[] getAudioPids() {
        return mAudioPids;
    }

    public int[] getAudioFormats() {
        return mAudioFormats;
    }

    public String[] getAudioLangs() {
        return mAudioLangs;
    }

    public int getAudioStd() {
        return mAudioStd;
    }

    public int getIsAutoStd() {
        return mIsAutoStd;
    }

    public int getAudioTrackIndex() {
        return mAudioTrackIndex;
    }

    public int getAudioCompensation() {
        return mAudioCompensation;
    }

    public int getAudioChannel() {
        return mAudioChannel;
    }

    public int getPcrPid() {
        return mPcrPid;
    }

    public int getFrequency() {
        return mFrequency;
    }

    public int getBandwidth() {
        return mBandwidth;
    }

    public int getFineTune() {
        return mFineTune;
    }

    public int[] getSubtitleTypes() {
        return mSubtitleTypes;
    }

    public int[] getSubtitlePids() {
        return mSubtitlePids;
    }

    public int[] getSubtitleStypes() {
        return mSubtitleStypes;
    }

    public int[] getSubtitleId1s() {
        return mSubtitleId1s;
    }

    public int[] getSubtitleId2s() {
        return mSubtitleId2s;
    }

    public String[] getSubtitleLangs() {
        return mSubtitleLangs;
    }

    public int getSubtitleTrackIndex() {
        return mSubtitleTrackIndex;
    }

    public String getDisplayNameMulti() {
        return mDisplayNameMulti;
    }

    public String getDisplayNameLocal() {
        return TVMultilingualText.getText(getDisplayNameMulti());
    }

    public String getDisplayName(String local) {
        return TVMultilingualText.getText(getDisplayNameMulti(), local);
    }

    public int getFreeCa() {
        return mFreeCa;
    }

    public int getScrambled() {
        return mScrambled;
    }

    public int getSdtVersion() {
        return mSdtVersion;
    }

    public boolean isBrowsable() {
        return this.mBrowsable;
    }

    public boolean isFavourite() {
        return mIsFavourite;
    }

    public boolean isPassthrough() {
        return mIsPassthrough;
    }

    public boolean isLocked() {
        return mLocked;
    }

    public Uri getUri() {
        if (isPassthrough()) {
            return TvContract.buildChannelUriForPassthroughInput(this.mInputId);
        }
        return TvContract.buildChannelUri(this.mId);
    }

    public void setType(String type) {
        mType = type;
    }

    public void setDisplayNumber(String number) {
        mDisplayNumber = number;
        mNumber = stringToInteger(number);
    }
    public void setDisplayName(String name) {
        mDisplayName = name;
    }

    public void setVideoStd(int std) {
        mVideoStd = std;
    }

    public void setVideoPid(int pid) {
        mVideoPid = pid;
    }

    public void setVfmt(int vfmt) {
        mVfmt = vfmt;
    }

    public void setVideoFormat(String format) {
        mVideoFormat = format;
    }

    public void setAudioPids(int[] pids) {
        mAudioPids = pids;
    }

    public void setAudioFormats(int[] formats) {
        mAudioFormats = formats;
    }

    public void setAudioLangs(String[] langs) {
        mAudioLangs = langs;
    }

    public void setAudioStd(int std) {
        mAudioStd = std;
    }

    public void setAudioTrackIndex(int index) {
        mAudioTrackIndex = index;
    }

    public void setAudioCompensation(int value) {
        mAudioCompensation = value;
    }

    public void setAudioChannel(int channel) {
        mAudioChannel = channel;
    }

    public void setPcrPid(int pid) {
        mPcrPid = pid;
    }

    public void setBrowsable(boolean enable) {
        mBrowsable = enable;
    }

    public void setFavourite(boolean enable) {
        mIsFavourite = enable;
    }

    public void setSubtitleTrackIndex(int index) {
        mSubtitleTrackIndex = index;
    }

    public void setFinetune(int finetune) {
        mFineTune = finetune;
    }

    public void setFreeCa(int free_ca) {
        mFreeCa = free_ca;
    }

    public void setScrambled(int scrambled) {
        mScrambled = scrambled;
    }

    public void setSdtVersion(int version) {
        mSdtVersion = version;
    }

    public void setOriginalNetworkId(int id) {
        mOriginalNetworkId = id;
    }

    public void copyFrom(ChannelInfo channel) {
        if (this == channel)
            return;
        this.mId = channel.mId;
        this.mInputId = channel.mInputId;
        this.mType = channel.mType;
        this.mServiceType = channel.mServiceType;
        this.mServiceId = channel.mServiceId;
        this.mDisplayNumber = channel.mDisplayNumber;
        this.mDisplayName = channel.mDisplayName;
        this.mVideoFormat = channel.mVideoFormat;
        this.mBrowsable = channel.mBrowsable;
        this.mLocked = channel.mLocked;
        this.mIsPassthrough = channel.mIsPassthrough;
    }

    public static final class Builder {
        private final ChannelInfo mChannel = new ChannelInfo();

        public Builder() {
            mChannel.mId = -1L;
            mChannel.mInputId = "";
            mChannel.mType = "";
            mChannel.mServiceType= "";
            mChannel.mServiceId = -1;
            mChannel.mDisplayNumber = "-1";
            mChannel.mNumber = -1;
            mChannel.mDisplayName = "";
            mChannel.mVideoFormat = "";
            mChannel.mLogoUrl = "";
            mChannel.mOriginalNetworkId = -1;
            mChannel.mTransportStreamId = -1;

            mChannel.mVideoPid = -1;
            mChannel.mVideoStd = -1;
            mChannel.mVfmt = -1;
            mChannel.mVideoWidth = -1;
            mChannel.mVideoHeight = -1;

            mChannel.mAudioPids = null;
            mChannel.mAudioFormats = null;
            mChannel.mAudioLangs = null;
            mChannel.mAudioStd = -1;
            mChannel.mIsAutoStd = -1;
            mChannel.mAudioTrackIndex = -1;
            mChannel.mAudioCompensation = -1;
            mChannel.mAudioChannel = 0;

            mChannel.mPcrPid = -1;
            mChannel.mFrequency = -1;
            mChannel.mBandwidth = -1;
            mChannel.mFineTune = 0;

            mChannel.mBrowsable = false;
            mChannel.mIsFavourite = false;
            mChannel.mLocked = false;
            mChannel.mIsPassthrough = false;

            mChannel.mSubtitlePids = null;
            mChannel.mSubtitleTypes = null;
            mChannel.mSubtitleStypes = null;
            mChannel.mSubtitleLangs = null;
            mChannel.mSubtitleId1s = null;
            mChannel.mSubtitleId2s = null;
            mChannel.mSubtitleTrackIndex = -1;

            mChannel.mFreeCa = 0;
            mChannel.mScrambled = 0;

            mChannel.mSdtVersion = 0xff;
        }

        public Builder setId(long id) {
            mChannel.mId = id;
            return this;
        }

        public Builder setInputId(String input_id) {
            mChannel.mInputId = input_id;
            return this;
        }

        public Builder setType(String type) {
            mChannel.mType = type;
            return this;
        }

        public Builder setServiceType(String type) {
            mChannel.mServiceType = type;
            return this;
        }

        public Builder setServiceId(int id) {
            mChannel.mServiceId = id;
            return this;
        }

        public Builder setDisplayNumber(String number) {
            mChannel.mDisplayNumber = number;
            mChannel.mNumber = stringToInteger(number);
            return this;
        }

        public Builder setDisplayName(String name) {
            mChannel.mDisplayName = name;
            return this;
        }

        public Builder setLogoUrl(String url) {
            mChannel.mLogoUrl = url;
            return this;
        }

        public Builder setOriginalNetworkId(int id) {
            mChannel.mOriginalNetworkId = id;
            return this;
        }

        public Builder setTransportStreamId(int id) {
            mChannel.mTransportStreamId = id;
            return this;
        }

        public Builder setVfmt(int value) {
            mChannel.mVfmt = value;
            return this;
        }

        public Builder setVideoPid(int id) {
            mChannel.mVideoPid = id;
            return this;
        }

        public Builder setVideoStd(int std) {
            mChannel.mVideoStd = std;
            return this;
        }

        public Builder setVideoFormat(String format) {
            mChannel.mVideoFormat = format;
            return this;
        }

        public Builder setVideoWidth(int w) {
            mChannel.mVideoWidth = w;
            return this;
        }

        public Builder setVideoHeight(int h) {
            mChannel.mVideoWidth = h;
            return this;
        }

        public Builder setAudioPids(int[] pids) {
            mChannel.mAudioPids = pids;
            return this;
        }

        public Builder setAudioFormats(int[] formats) {
            mChannel.mAudioFormats = formats;
            return this;
        }

        public Builder setAudioLangs(String[] langs) {
            mChannel.mAudioLangs = langs;
            return this;
        }

        public Builder setAudioStd(int std) {
            mChannel.mAudioStd = std;
            return this;
        }

        public Builder setIsAutoStd(int isStd) {
            mChannel.mIsAutoStd = isStd;
            return this;
        }

        public Builder setAudioTrackIndex(int index) {
            mChannel.mAudioTrackIndex = index;
            return this;
        }

        public Builder setAudioCompensation(int c) {
            mChannel.mAudioCompensation = c;
            return this;
        }

        public Builder setAudioChannel(int c) {
            mChannel.mAudioChannel = c;
            return this;
        }

        public Builder setPcrPid(int pid) {
            mChannel.mPcrPid = pid;
            return this;
        }

        public Builder setFrequency(int freq) {
            mChannel.mFrequency = freq;
            return this;
        }

        public Builder setBandwidth(int w) {
            mChannel.mBandwidth = w;
            return this;
        }

        public Builder setFineTune(int f) {
            mChannel.mFineTune = f;
            return this;
        }

        public Builder setBrowsable(boolean flag) {
            mChannel.mBrowsable = flag;
            return this;
        }

        public Builder setIsFavourite(boolean fav) {
            mChannel.mIsFavourite = fav;
            return this;
        }

        public Builder setPassthrough(boolean flag) {
            mChannel.mIsPassthrough = flag;
            return this;
        }

        public Builder setLocked(boolean flag) {
            mChannel.mLocked = flag;
            return this;
        }

        public Builder setSubtitleTypes(int[] types) {
            mChannel.mSubtitleTypes = types;
            return this;
        }

        public Builder setSubtitlePids(int[] pids) {
            mChannel.mSubtitlePids = pids;
            return this;
        }

        public Builder setSubtitleStypes(int[] types) {
            mChannel.mSubtitleStypes = types;
            return this;
        }

        public Builder setSubtitleId1s(int[] id1s) {
            mChannel.mSubtitleId1s = id1s;
            return this;
        }

        public Builder setSubtitleId2s(int[] id2s) {
            mChannel.mSubtitleId2s = id2s;
            return this;
        }

        public Builder setSubtitleLangs(String[] langs) {
            mChannel.mSubtitleLangs = langs;
            return this;
        }

        public Builder setSubtitleTrackIndex(int index) {
            mChannel.mSubtitleTrackIndex = index;
            return this;
        }

        public Builder setDisplayNameMulti(String name) {
            mChannel.mDisplayNameMulti = name;
            return this;
        }

        public Builder setFreeCa(int free_ca) {
            mChannel.mFreeCa = free_ca;
            return this;
        }

        public Builder setScrambled(int scrambled) {
            mChannel.mScrambled = scrambled;
            return this;
        }

        public Builder setSdtVersion(int version) {
            mChannel.mSdtVersion = version;
            return this;
        }

        public ChannelInfo build() {
            return mChannel;
        }
    }

    private static int stringToInteger(String string) {
        String s = string.replaceAll("\\D", "");
        if (!TextUtils.isEmpty(s))
            return Integer.valueOf(s);
        else
            return -1;
    }

    public static String mapToString(Map<String, String> map) {
        StringBuilder stringBuilder = new StringBuilder();

        for (String key : map.keySet()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append("&");
            }
            String value = map.get(key);
            try {
                stringBuilder.append((key != null ? URLEncoder.encode(key, "UTF-8") : ""));
                stringBuilder.append("=");
                stringBuilder.append(value != null ? URLEncoder.encode(value, "UTF-8") : "");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("This method requires UTF-8 encoding support", e);
            }
        }

        return stringBuilder.toString();
    }

    public static Map<String, String> stringToMap(String input) {
        Map<String, String> map = new HashMap<String, String>();

        String[] nameValuePairs = input.split("&");
        for (String nameValuePair : nameValuePairs) {
            String[] nameValue = nameValuePair.split("=");
            try {
                map.put(URLDecoder.decode(nameValue[0], "UTF-8"), nameValue.length > 1 ? URLDecoder.decode(
                            nameValue[1], "UTF-8") : "");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("This method requires UTF-8 encoding support", e);
            }
        }

        return map;
    }

    public static boolean isSameChannel (ChannelInfo a, ChannelInfo b) {
        if (a == null || b== null )
            return false;

        if (a.getInputId().equals(b.getInputId()) && a.getId() == b.getId()) {
            return true;
        }

        return false;
    }

    public static boolean isVideoChannel(ChannelInfo channel) {
        if (channel != null)
            return TextUtils.equals(channel.getServiceType(), Channels.SERVICE_TYPE_AUDIO_VIDEO);

        return false;
    }

    public static boolean isRadioChannel(ChannelInfo channel) {
        if (channel != null)
            return TextUtils.equals(channel.getServiceType(), Channels.SERVICE_TYPE_AUDIO);

        return false;
    }

    public void print () {
        Log.d(TAG, "Id = " + mId +
                "\n InputId = " + mInputId +
                "\n Type = " + mType +
                "\n ServiceType = " + mServiceType +
                "\n ServiceId = " + mServiceId +
                "\n DisplayNumber = " + mDisplayNumber+
                "\n DisplayName = " + mDisplayName +
                "\n LogoUrl = " + mLogoUrl +
                "\n OriginalNetworkId = " + mOriginalNetworkId +
                "\n TransportStreamId = " + mTransportStreamId +
                "\n VideoStd = " + mVideoStd +
                "\n VideoFormat = " + mVideoFormat +
                "\n VideoWidth = " + mVideoWidth +
                "\n VideoHeight = " + mVideoHeight +
                "\n VideoPid = " + mVideoPid +
                "\n Vfmt = " + mVfmt +
                "\n AudioPids = " + Arrays.toString(mAudioPids) +
                "\n AudioFormats = " + Arrays.toString(mAudioFormats) +
                "\n AudioLangs = " + Arrays.toString(mAudioLangs) +
                "\n AudioStd = " + mAudioStd +
                "\n IsAutoStd = " + mIsAutoStd +
                "\n AudioTrackIndex = " + mAudioTrackIndex +
                "\n AudioCompensation = " + mAudioCompensation +
                "\n AudioChannel = " + mAudioChannel +
                "\n PcrPid = " + mPcrPid +
                "\n mFrequency = " + mFrequency +
                "\n mFinetune = " + mFineTune +
                "\n Browsable = " + mBrowsable +
                "\n IsFavourite = " + mIsFavourite +
                "\n IsPassthrough = " + mIsPassthrough +
                "\n mLocked = " + mLocked +
                "\n SubtitlePids = " + Arrays.toString(mSubtitlePids) +
                "\n SubtitleTypes = " + Arrays.toString(mSubtitleTypes) +
                "\n SubtitleStypes = " + Arrays.toString(mSubtitleStypes) +
                "\n SubtitleId1s = " + Arrays.toString(mSubtitleId1s) +
                "\n SubtitleId2s = " + Arrays.toString(mSubtitleId2s) +
                "\n SubtitleLangs = " + Arrays.toString(mSubtitleLangs) +
                "\n SubtitleTrackIndex = " + mSubtitleTrackIndex +
                "\n FreeCa = " + mFreeCa +
                "\n Scrambled = " + mScrambled +
                "\n SdtVersion = " + mSdtVersion
               );
    }
}
