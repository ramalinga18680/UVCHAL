package com.droidlogic.app.tv;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.content.Context;
import android.database.Cursor;
import android.media.tv.TvContentRating;
import android.media.tv.TvContract;
import android.media.tv.TvContract.Channels;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.Pair;
import android.util.SparseArray;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

public class TvDataBaseManager {
    private static final String TAG = "TvDataBaseManager";
    private static final boolean DEBUG = true;
    private static final int UPDATE_SUCCESS = -1;

    private static final SparseArray<String> CHANNEL_MODE_TO_TYPE_MAP = new SparseArray<String>();

    static {
        CHANNEL_MODE_TO_TYPE_MAP.put(TVChannelParams.MODE_DTMB, Channels.TYPE_DTMB);
        CHANNEL_MODE_TO_TYPE_MAP.put(TVChannelParams.MODE_QPSK, Channels.TYPE_DVB_S);
        CHANNEL_MODE_TO_TYPE_MAP.put(TVChannelParams.MODE_QAM, Channels.TYPE_DVB_C);
        CHANNEL_MODE_TO_TYPE_MAP.put(TVChannelParams.MODE_OFDM, Channels.TYPE_DVB_T);
        CHANNEL_MODE_TO_TYPE_MAP.put(TVChannelParams.MODE_ATSC, Channels.TYPE_ATSC_C);
        CHANNEL_MODE_TO_TYPE_MAP.put(TVChannelParams.MODE_ANALOG, Channels.TYPE_PAL);
        CHANNEL_MODE_TO_TYPE_MAP.put(TVChannelParams.MODE_ISDBT, Channels.TYPE_ISDB_T);
    }

    private static final Map<String, Integer> CHANNEL_TYPE_TO_MODE_MAP = new HashMap<String, Integer>();

    static {
        CHANNEL_TYPE_TO_MODE_MAP.put(Channels.TYPE_DTMB, TVChannelParams.MODE_DTMB);
        CHANNEL_TYPE_TO_MODE_MAP.put(Channels.TYPE_DVB_C, TVChannelParams.MODE_QAM);
        CHANNEL_TYPE_TO_MODE_MAP.put(Channels.TYPE_DVB_T, TVChannelParams.MODE_OFDM);
        CHANNEL_TYPE_TO_MODE_MAP.put(Channels.TYPE_PAL, TVChannelParams.MODE_ANALOG);
    }

    private Context mContext;
    private ContentResolver mContentResolver;

    public TvDataBaseManager (Context context) {
        mContext = context;
        mContentResolver = mContext.getContentResolver();
    }

    public void deleteChannels(String inputId) {
        Uri channelsUri = TvContract.buildChannelsUriForInput(inputId);
        try {
            mContentResolver.delete(channelsUri, Channels._ID + "!=-1", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int updateDtvChannel(ChannelInfo channel) {
        int ret = 0;
        Uri channelsUri = TvContract.buildChannelsUriForInput(channel.getInputId());
        String[] projection = {Channels._ID,
            Channels.COLUMN_SERVICE_ID,
            Channels.COLUMN_DISPLAY_NUMBER,
            Channels.COLUMN_DISPLAY_NAME,
            Channels.COLUMN_INTERNAL_PROVIDER_DATA};

        Cursor cursor = null;
        try {
            boolean found = false;
            cursor = mContentResolver.query(channelsUri, projection, Channels.COLUMN_SERVICE_TYPE + "=?", new String[]{channel.getServiceType()}, null);
            while (cursor != null && cursor.moveToNext()) {
                long rowId = cursor.getLong(findPosition(projection, Channels._ID));

                if (channel.getId() == -1) {
                    int serviceId = cursor.getInt(findPosition(projection, Channels.COLUMN_SERVICE_ID));
                    String name = cursor.getString(findPosition(projection, Channels.COLUMN_DISPLAY_NAME));
                    int frequency = 0;
                    int index = cursor.getColumnIndex(Channels.COLUMN_INTERNAL_PROVIDER_DATA);
                    if (index >= 0) {
                        Map<String, String> parsedMap = ChannelInfo.stringToMap(cursor.getString(index));
                        frequency = Integer.parseInt(parsedMap.get(ChannelInfo.KEY_FREQUENCY));
                    }
                    if (serviceId == channel.getServiceId() && frequency == channel.getFrequency()
                        && name.equals(channel.getDisplayName()))
                        found = true;
                } else if (rowId == channel.getId()) {
                    found = true;
                }

                if (found) {
                    Uri uri = TvContract.buildChannelUri(rowId);
                    mContentResolver.update(uri, buildDtvChannelData(channel), null, null);
                    insertLogo(channel.getLogoUrl(), uri);
                    ret = UPDATE_SUCCESS;
                    break;
                }
            }
            if (ret != UPDATE_SUCCESS)
                ret = cursor.getCount();
        } catch (Exception e) {
            //TODO
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (DEBUG)
            Log.d(TAG, "update " + ((ret == UPDATE_SUCCESS) ? "found" : "notfound")
                +" DTV CH: [_id:"+channel.getId()
                +"][sid:"+channel.getServiceId()
                +"][freq:"+channel.getFrequency()
                +"][name:"+channel.getDisplayName()
                +"][num:"+channel.getNumber()
                +"]");
        return ret;
    }


    /*update atv channel:
          if getId() == -1 (from searching): update that with same frequncy
          else               (from database): update that with same _ID
      */
    private int updateAtvChannel(ChannelInfo channel) {
        int ret = 0;

        Uri channelsUri = TvContract.buildChannelsUriForInput(channel.getInputId());
        String[] projection = {Channels._ID, Channels.COLUMN_INTERNAL_PROVIDER_DATA};

        Cursor cursor = null;
        try {
            boolean found = false;
            cursor = mContentResolver.query(channelsUri, projection, null, null, null);
            while (cursor != null && cursor.moveToNext()) {
                long rowId = cursor.getLong(findPosition(projection, Channels._ID));

                if (channel.getId() == -1) {
                    Map<String, String> parsedMap = parseInternalProviderData(cursor.getString(
                                findPosition(projection, Channels.COLUMN_INTERNAL_PROVIDER_DATA)));
                    int frequency = Integer.parseInt(parsedMap.get(ChannelInfo.KEY_FREQUENCY));
                    if (frequency == channel.getFrequency())
                        found = true;
                } else if (rowId == channel.getId()) {
                    found = true;
                }

                if (found) {
                    Uri uri = TvContract.buildChannelUri(rowId);
                    mContentResolver.update(uri, buildAtvChannelData(channel), null, null);
                    insertLogo(channel.getLogoUrl(), uri);
                    ret = UPDATE_SUCCESS;
                    break;
                }
            }
            if (ret != UPDATE_SUCCESS)
                ret = cursor.getCount();
        } catch (Exception e) {
            //TODO
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (DEBUG)
            Log.d(TAG, "update " + ((ret == UPDATE_SUCCESS) ? "found" : "notfound")
                +" ATV CH: [_id:"+channel.getId()
                +"][freq:"+channel.getFrequency()
                +"][name:"+channel.getDisplayName()
                +"][num:"+channel.getNumber()
                +"]");
        return ret;
    }

    /*update atv channel:
          if toBeUpdated == null (nothing to be replaced): just wait for insert
          else                       (channel to replace): replace the one with _ID
          toBeUpdated must come from database, toBeUpdated.getId() == -1 is NOT allowed.
      */
    private int updateAtvChannel(ChannelInfo toBeUpdated, ChannelInfo channel) {
        int ret = 0;
        Uri channelsUri = TvContract.buildChannelsUriForInput(channel.getInputId());
        String[] projection = {Channels._ID};
        Cursor cursor = null;

        try {
            if (toBeUpdated == null) {
                cursor = mContentResolver.query(channelsUri, projection, null, null, null);
                if (cursor != null)
                    ret = cursor.getCount();
            } else if (toBeUpdated.getId() != -1) {
                Uri uri = TvContract.buildChannelUri(toBeUpdated.getId());
                channel.setDisplayNumber(toBeUpdated.getDisplayNumber());
                mContentResolver.update(uri, buildAtvChannelData(channel), null, null);
                insertLogo(channel.getLogoUrl(), uri);
                ret = UPDATE_SUCCESS;
            } else {
                Log.w(TAG, "toBeUpdated is illegal, [_id:"+toBeUpdated.getId()+"]");
            }
        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (DEBUG)
            Log.d(TAG, "update " + ((ret == UPDATE_SUCCESS) ? "found" : "notfound")
                +" ATV CH: [_id:"+ ((ret == UPDATE_SUCCESS) ? toBeUpdated.getId() : channel.getId())
                +"][freq:"+channel.getFrequency()
                +"][name:"+channel.getDisplayName()
                +"][num:"+channel.getNumber()
                +"]");
        return ret;
    }

    public void insertDtvChannel(ChannelInfo channel, int channelNumber) {
        Uri channelsUri = TvContract.buildChannelsUriForInput(channel.getInputId());
        channel.setDisplayNumber(Integer.toString(channelNumber));
        Uri uri = mContentResolver.insert(TvContract.Channels.CONTENT_URI, buildDtvChannelData(channel));
        insertLogo(channel.getLogoUrl(), uri);

        if (DEBUG)
            Log.d(TAG, "Insert DTV CH: [sid:"+channel.getServiceId()
                    +"][freq:"+channel.getFrequency()
                    +"][name:"+channel.getDisplayName()
                    +"][num:"+channel.getNumber()
                    +"]");
    }

    public void insertAtvChannel(ChannelInfo channel, int channelNumber) {
        Uri channelsUri = TvContract.buildChannelsUriForInput(channel.getInputId());
        channel.setDisplayNumber(Integer.toString(channelNumber));
        Uri uri = mContentResolver.insert(TvContract.Channels.CONTENT_URI, buildAtvChannelData(channel));
        insertLogo(channel.getLogoUrl(), uri);

        if (DEBUG)
            Log.d(TAG, "Insert ATV CH: [freq:"+channel.getFrequency()
                +"][name:"+channel.getDisplayName()
                +"][num:"+channel.getNumber()
                +"]");
    }

    private Map<String, String>  buildAtvChannelMap (ChannelInfo channel){
        Map<String, String> map = new HashMap<String, String>();
        map.put(ChannelInfo.KEY_VFMT, String.valueOf(channel.getVfmt()));
        map.put(ChannelInfo.KEY_FREQUENCY, String.valueOf(channel.getFrequency()));
        map.put(ChannelInfo.KEY_VIDEO_STD, String.valueOf(channel.getVideoStd()));
        map.put(ChannelInfo.KEY_AUDIO_STD, String.valueOf(channel.getAudioStd()));
        map.put(ChannelInfo.KEY_IS_AUTO_STD, String.valueOf(channel.getIsAutoStd()));
        map.put(ChannelInfo.KEY_FINE_TUNE, String.valueOf(channel.getFineTune()));
        map.put(ChannelInfo.KEY_AUDIO_COMPENSATION, String.valueOf(channel.getAudioCompensation()));
        map.put(ChannelInfo.KEY_IS_FAVOURITE, String.valueOf(channel.isFavourite() ? 1 : 0));

        return map;
    }

    private ContentValues  buildDtvChannelData (ChannelInfo channel){
        ContentValues values = new ContentValues();
        values.put(Channels.COLUMN_INPUT_ID, channel.getInputId());
        values.put(Channels.COLUMN_DISPLAY_NAME, channel.getDisplayName());
        values.put(Channels.COLUMN_DISPLAY_NUMBER, channel.getNumber());
        values.put(Channels.COLUMN_ORIGINAL_NETWORK_ID, channel.getOriginalNetworkId());
        values.put(Channels.COLUMN_TRANSPORT_STREAM_ID, channel.getTransportStreamId());
        values.put(Channels.COLUMN_SERVICE_ID, channel.getServiceId());
        values.put(Channels.COLUMN_TYPE, channel.getType());
        values.put(Channels.COLUMN_BROWSABLE, channel.isBrowsable()? 1 : 0);
        values.put(Channels.COLUMN_SERVICE_TYPE, channel.getServiceType());

        Map<String, String> map = new HashMap<String, String>();
        map.put(ChannelInfo.KEY_VFMT, String.valueOf(channel.getVfmt()));
        map.put(ChannelInfo.KEY_FREQUENCY, String.valueOf(channel.getFrequency()));
        map.put(ChannelInfo.KEY_BAND_WIDTH, String.valueOf(channel.getBandwidth()));
        map.put(ChannelInfo.KEY_VIDEO_PID, String.valueOf(channel.getVideoPid()));
        map.put(ChannelInfo.KEY_AUDIO_PIDS, Arrays.toString(channel.getAudioPids()));
        map.put(ChannelInfo.KEY_AUDIO_FORMATS, Arrays.toString(channel.getAudioFormats()));
        map.put(ChannelInfo.KEY_AUDIO_LANGS, Arrays.toString(channel.getAudioLangs()));
        map.put(ChannelInfo.KEY_PCR_ID, String.valueOf(channel.getPcrPid()));
        map.put(ChannelInfo.KEY_AUDIO_TRACK_INDEX, String.valueOf(channel.getAudioTrackIndex()));
        map.put(ChannelInfo.KEY_AUDIO_COMPENSATION, String.valueOf(channel.getAudioCompensation()));
        map.put(ChannelInfo.KEY_AUDIO_CHANNEL, String.valueOf(channel.getAudioChannel()));
        map.put(ChannelInfo.KEY_IS_FAVOURITE, String.valueOf(channel.isFavourite() ? 1 : 0));
        map.put(ChannelInfo.KEY_SUBT_TYPES, Arrays.toString(channel.getSubtitleTypes()));
        map.put(ChannelInfo.KEY_SUBT_PIDS, Arrays.toString(channel.getSubtitlePids()));
        map.put(ChannelInfo.KEY_SUBT_STYPES, Arrays.toString(channel.getSubtitleStypes()));
        map.put(ChannelInfo.KEY_SUBT_ID1S, Arrays.toString(channel.getSubtitleId1s()));
        map.put(ChannelInfo.KEY_SUBT_ID2S, Arrays.toString(channel.getSubtitleId2s()));
        map.put(ChannelInfo.KEY_SUBT_LANGS, Arrays.toString(channel.getSubtitleLangs()));
        map.put(ChannelInfo.KEY_SUBT_TRACK_INDEX, String.valueOf(channel.getSubtitleTrackIndex()));
        map.put(ChannelInfo.KEY_MULTI_NAME, channel.getDisplayNameMulti());
        map.put(ChannelInfo.KEY_FREE_CA, String.valueOf(channel.getFreeCa()));
        map.put(ChannelInfo.KEY_SCRAMBLED, String.valueOf(channel.getScrambled()));
        map.put(ChannelInfo.KEY_SDT_VERSION, String.valueOf(channel.getSdtVersion()));
        String output = ChannelInfo.mapToString(map);
        values.put(TvContract.Channels.COLUMN_INTERNAL_PROVIDER_DATA, output);

        return values;
    }

    private ContentValues  buildAtvChannelData (ChannelInfo channel){
        ContentValues values = new ContentValues();
        values.put(Channels.COLUMN_INPUT_ID, channel.getInputId());
        values.put(Channels.COLUMN_DISPLAY_NUMBER, channel.getNumber());
        values.put(Channels.COLUMN_DISPLAY_NAME, channel.getDisplayName());
        values.put(Channels.COLUMN_TYPE, Channels.TYPE_PAL);// TODO: channel.type -> COLUMN_TYPE (PAL/NTSC/SECAM)?
        values.put(Channels.COLUMN_BROWSABLE, channel.isBrowsable() ? 1 : 0);
        values.put(Channels.COLUMN_SERVICE_TYPE, channel.getServiceType());

        String output = ChannelInfo.mapToString(buildAtvChannelMap(channel));
        values.put(TvContract.Channels.COLUMN_INTERNAL_PROVIDER_DATA, output);

        return values;
    }

    private void declineChannelNum( ChannelInfo channel) {
        Uri channelsUri = TvContract.buildChannelsUriForInput(channel.getInputId());
        String[] projection = {Channels._ID, Channels.COLUMN_DISPLAY_NUMBER};
        String condition = Channels.COLUMN_SERVICE_TYPE + " = " + channel.getServiceType() +
            " and " + Channels.COLUMN_DISPLAY_NUMBER + " > " + channel.getNumber();

        Cursor cursor = null;
        try {
            cursor = mContentResolver.query(channelsUri, projection, condition, null, null);
            while (cursor != null && cursor.moveToNext()) {
                long rowId = cursor.getLong(findPosition(projection, Channels._ID));
                ContentValues values = new ContentValues();
                values.put(Channels.COLUMN_DISPLAY_NUMBER, channel.getNumber() - 1);
                Uri uri = TvContract.buildChannelUri(rowId);
                mContentResolver.update(uri, values, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void setChannelName (ChannelInfo channel, String targetName) {
        if (channel != null) {
            channel.setDisplayName(targetName);
            updateChannelInfo(channel);
        }
    }

    public void deleteChannel(ChannelInfo channel) {
        Uri channelsUri = TvContract.buildChannelsUriForInput(channel.getInputId());
        String[] projection = {Channels._ID, Channels.COLUMN_DISPLAY_NUMBER, Channels.COLUMN_DISPLAY_NAME};
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        int deleteCount = 0;
        deleteCount = mContentResolver.delete(channelsUri, Channels._ID + "=?", new String[]{channel.getId() + ""});

        if (deleteCount > 0) {
            Cursor cursor = null;
            try {
                cursor = mContentResolver.query(channelsUri, projection,
                    Channels.COLUMN_SERVICE_TYPE + "=?", new String[]{channel.getServiceType()}, null);
                while (cursor != null && cursor.moveToNext()) {
                    long rowId = cursor.getLong(findPosition(projection, Channels._ID));
                    int number = cursor.getInt(findPosition(projection,Channels.COLUMN_DISPLAY_NUMBER));
                    String name = cursor.getString(findPosition(projection,Channels.COLUMN_DISPLAY_NAME));

                    if (number > channel.getNumber()) {
                        if (DEBUG)
                            Log.d(TAG, "deleteChannel: update channel: number=" + number + " name=" + name);
                        Uri uri = TvContract.buildChannelUri(rowId);
                        ContentValues updateValues = new ContentValues();
                        updateValues.put(Channels.COLUMN_DISPLAY_NUMBER, number -1);
                        ops.add(ContentProviderOperation.newUpdate(uri)
                            .withValues(updateValues)
                            .build());
                    }
                }
                mContentResolver.applyBatch(TvContract.AUTHORITY, ops);
            } catch (Exception e) {
                //TODO
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    public void deleteChannelsContinuous(ArrayList<ChannelInfo> channels) {
        int count = channels.size();
        if (count <= 0)
            return ;

        Uri channelsUri = TvContract.buildChannelsUriForInput(channels.get(0).getInputId());
        String[] projection = {Channels._ID, Channels.COLUMN_DISPLAY_NUMBER, Channels.COLUMN_DISPLAY_NAME};

        long startID = channels.get(0).getId();
        long endID = channels.get(count-1).getId();

        int deleteCount = 0;
        deleteCount = mContentResolver.delete(channelsUri, Channels._ID + ">=? and " + Channels._ID + "<=?", new String[]{startID + "", endID + ""});

        if (deleteCount > 0) {
            Cursor cursor = null;
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            try {
                cursor = mContentResolver.query(channelsUri, projection,
                    Channels.COLUMN_SERVICE_TYPE + "=?", new String[]{channels.get(0).getServiceType()}, null);
                while (cursor != null && cursor.moveToNext()) {
                    long rowId = cursor.getLong(findPosition(projection, Channels._ID));
                    int number = cursor.getInt(findPosition(projection,Channels.COLUMN_DISPLAY_NUMBER));
                    String name = cursor.getString(findPosition(projection,Channels.COLUMN_DISPLAY_NAME));

                    if (number > channels.get(count-1).getNumber()) {
                        if (DEBUG)
                            Log.d(TAG, "deleteChannel: update channel: number=" + number + " name=" + name);
                        Uri uri = TvContract.buildChannelUri(rowId);
                        ContentValues updateValues = new ContentValues();
                        updateValues.put(Channels.COLUMN_DISPLAY_NUMBER, number - count);
                        ops.add(ContentProviderOperation.newUpdate(uri)
                            .withValues(updateValues)
                            .build());
                    }
                }
                mContentResolver.applyBatch(TvContract.AUTHORITY, ops);
            } catch (Exception e) {
                //TODO
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }


    public void swapChannel (ChannelInfo sourceChannel, ChannelInfo targetChannel) {
        if (sourceChannel == null || targetChannel == null
            || sourceChannel.getNumber() == targetChannel.getNumber())
            return;

        Uri channelsUri = TvContract.buildChannelsUriForInput(sourceChannel.getInputId());
        String[] projection = {Channels._ID, Channels.COLUMN_DISPLAY_NUMBER, Channels.COLUMN_DISPLAY_NAME};

        Cursor cursor = null;
        try {
            cursor = mContentResolver.query(channelsUri, projection,
                Channels.COLUMN_SERVICE_TYPE + "=?", new String[]{sourceChannel.getServiceType()}, null);
            while (cursor != null && cursor.moveToNext()) {
                long rowId = cursor.getLong(findPosition(projection, Channels._ID));
                int number = cursor.getInt(findPosition(projection,Channels.COLUMN_DISPLAY_NUMBER));
                String name = cursor.getString(findPosition(projection,Channels.COLUMN_DISPLAY_NAME));

                Uri uri = TvContract.buildChannelUri(rowId);
                ContentValues updateValues = new ContentValues();
                if (number == sourceChannel.getNumber()) {
                    if (DEBUG)
                        Log.d(TAG, "swapChannel: update source channel: number=" + number + " name=" + name);
                    updateValues.put(Channels.COLUMN_DISPLAY_NUMBER, targetChannel.getNumber());
                    mContentResolver.update(uri, updateValues, null, null);
                } else if (number == targetChannel.getNumber()){
                    if (DEBUG)
                        Log.d(TAG, "@@@@@@@@@@ swapChannel: update target channel: number=" + number + " name=" + name);
                    updateValues.put(Channels.COLUMN_DISPLAY_NUMBER, sourceChannel.getNumber());
                    mContentResolver.update(uri, updateValues, null, null);
                }
            }
        } catch (Exception e) {
            //TODO
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void moveChannel (ArrayList<ChannelInfo> channelList, int channelNumber, int targetNumber) {
        if (channelList == null || channelList.size() == 0
                || channelNumber >= channelList.size() || targetNumber >= channelList.size())
            return;

        ChannelInfo channel = channelList.get(channelNumber);
        Uri channelsUri = TvContract.buildChannelsUriForInput(channel.getInputId());
        String[] projection = {Channels._ID, Channels.COLUMN_DISPLAY_NUMBER, Channels.COLUMN_DISPLAY_NAME};

        Cursor cursor = null;
        try {
            cursor = mContentResolver.query(channelsUri, projection,
                Channels.COLUMN_SERVICE_TYPE + "=?", new String[]{channel.getServiceType()}, null);
            while (cursor != null && cursor.moveToNext()) {
                long rowId = cursor.getLong(findPosition(projection, Channels._ID));
                int number = cursor.getInt(findPosition(projection,Channels.COLUMN_DISPLAY_NUMBER));
                String name = cursor.getString(findPosition(projection,Channels.COLUMN_DISPLAY_NAME));

                Uri uri = TvContract.buildChannelUri(rowId);
                ContentValues updateValues = new ContentValues();
                if (targetNumber < channelNumber) {
                    if (number >= targetNumber && number < channelNumber) {
                        updateValues.put(Channels.COLUMN_DISPLAY_NUMBER, number + 1);
                        mContentResolver.update(uri, updateValues, null, null);
                    }
                } else if (targetNumber > channelNumber){
                    if (number > channelNumber && number <= targetNumber) {
                        updateValues.put(Channels.COLUMN_DISPLAY_NUMBER, number - 1);
                        mContentResolver.update(uri, updateValues, null, null);
                    }
                }

                if (number == channelNumber) {
                    updateValues.put(Channels.COLUMN_DISPLAY_NUMBER, targetNumber);
                    mContentResolver.update(uri, updateValues, null, null);
                }
            }
        } catch (Exception e) {
            //TODO
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void skipChannel (ChannelInfo channel) {
        if (channel != null) {
            if (channel.isBrowsable()) {
                channel.setBrowsable(false);
                channel.setFavourite(false);
            } else
                channel.setBrowsable(true);
            updateChannelInfo(channel);
        }
    }

    public void setFavouriteChannel (ChannelInfo channel) {
        if (channel != null) {
            if (!channel.isFavourite()) {
                channel.setFavourite(true);
                channel.setBrowsable(true);
            } else
                channel.setFavourite(false);
            updateChannelInfo(channel);
        }
    }

    public void updateChannelInfo(ChannelInfo channel) {
        if (channel.getInputId() == null)
            return;

        if (channel.getInputId().contains("ATV")) {
            updateAtvChannel(channel);
        } else if (channel.getInputId().contains("DTV")) {
            updateDtvChannel(channel);
        }
    }

    // If a channel exists, update it. If not, insert a new one.
    public void updateOrinsertDtvChannel(ChannelInfo channel) {
        int updateRet = updateDtvChannel(channel);
        if (updateRet != UPDATE_SUCCESS) {
            insertDtvChannel(channel, updateRet);
        }
    }

    // If a channel exists, update it. If not, insert a new one.
    public void updateOrinsertAtvChannel(ChannelInfo channel) {
        int updateRet = updateAtvChannel(channel);
        if (updateRet != UPDATE_SUCCESS) {
            insertAtvChannel(channel, updateRet);
        }
    }

    public void updateOrinsertAtvChannel(ChannelInfo toBeUpdated, ChannelInfo channel) {
        int updateRet = updateAtvChannel(toBeUpdated, channel);
        if (updateRet != UPDATE_SUCCESS) {
            insertAtvChannel(channel, updateRet);
        }
    }

    private String getChannelType(int mode) {
        return CHANNEL_MODE_TO_TYPE_MAP.get(mode);
    }

    private int getChannelType(String type) {
        return CHANNEL_TYPE_TO_MODE_MAP.get(type);
    }

    public ArrayList<ChannelInfo> getChannelList(String curInputId, String srvType) {
        return getChannelList(curInputId, srvType, false);
    }

    public ArrayList<ChannelInfo> getChannelList(String curInputId, String srvType, boolean need_browserable) {
        ArrayList<ChannelInfo> channelList = new ArrayList<ChannelInfo>();
        Uri channelsUri = TvContract.buildChannelsUriForInput(curInputId);

        Cursor cursor = null;
        try {
            cursor = mContentResolver.query(channelsUri, ChannelInfo.COMMON_PROJECTION, null, null, null);
            while (cursor != null && cursor.moveToNext()) {
                int index = cursor.getColumnIndex(Channels.COLUMN_SERVICE_TYPE);
                if (srvType.equals(cursor.getString(index))) {
                    if (!need_browserable) {
                        channelList.add(ChannelInfo.fromCommonCursor(cursor));
                    } else {
                        boolean browserable = cursor.getInt(cursor.getColumnIndex(Channels.COLUMN_BROWSABLE)) == 1 ? true : false;
                        if (browserable) {
                            channelList.add(ChannelInfo.fromCommonCursor(cursor));
                        }
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        Collections.sort(channelList, new SortComparator());
        if (DEBUG)
            printList(channelList);
        return channelList;
    }

    public ChannelInfo getChannelInfo(Uri channelUri) {
        int matchId = DroidLogicTvUtils.matchsWhich(channelUri);
        if (matchId == DroidLogicTvUtils.NO_MATCH || matchId == DroidLogicTvUtils.MATCH_PASSTHROUGH_ID) {
            return null;
        }

        Uri uri = channelUri;
        Cursor cursor = null;
        ChannelInfo info = null;

        try {
            cursor = mContentResolver.query(uri, ChannelInfo.COMMON_PROJECTION, null, null, null);
            if (cursor != null) {
                cursor.moveToNext();
                info = ChannelInfo.fromCommonCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get channel info from TvProvider.", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return info;
    }

    public Map<String, String> parseInternalProviderData(String internalData){
        return ChannelInfo.stringToMap(internalData);
    }

    public void insertUrl( Uri contentUri, URL sourceUrl){
        if (DEBUG) {
            Log.d(TAG, "Inserting " + sourceUrl + " to " + contentUri);
        }
        InputStream is = null;
        OutputStream os = null;
        try{
            is = sourceUrl.openStream();
            os = mContentResolver.openOutputStream(contentUri);
            copy(is, os);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to write " + sourceUrl + "  to " + contentUri, ioe);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // Ignore exception.
                }
            } if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    // Ignore exception.
                }
            }
        }
    }

    public void copy(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
    }

    private void insertLogo (String logoUrl, Uri uri) {
        Map<Uri, String> logos = new HashMap<Uri, String>();
        if (!TextUtils.isEmpty(logoUrl))
            logos.put(TvContract.buildChannelLogoUri(uri), logoUrl);

        if (!logos.isEmpty())
            new InsertLogosTask(mContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, logos);
    }

    public class InsertLogosTask extends AsyncTask<Map<Uri, String>, Void, Void> {
        private final Context mContext;

        InsertLogosTask(Context context) {
            mContext = context;
        }

        @Override
            public Void doInBackground(Map<Uri, String>... logosList) {
                for (Map<Uri, String> logos : logosList) {
                    for (Uri uri : logos.keySet()) {
                        try {
                            insertUrl(uri, new URL(logos.get(uri)));
                        } catch (MalformedURLException e) {
                            Log.e(TAG, "Can't load " + logos.get(uri), e);
                        }
                    }
                }
                return null;
            }
    }

    public class SortComparator implements Comparator<ChannelInfo> {
        @Override
            public int compare(ChannelInfo a, ChannelInfo b) {
                return a.getNumber() - b.getNumber();
            }
    }

    private int findPosition(String[] list, String target) {
        int i;
        for (i = 0; i < list.length; i++) {
            if (list[i].equals(target))
                return i;
        }
        return -1;
    }

    private void printList(ArrayList<ChannelInfo> list) {
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                ChannelInfo info = list.get(i);
                Log.d(TAG, "printList: number=" + info.getNumber() + " name=" + info.getDisplayName());
            }
        }
    }

    // If a channel exists, update it. If not, insert a new one.
    public Uri updateOrinsertAtvChannel2(ChannelInfo channel) {
        Uri channelUri = null;
        int ret = 0;

        Uri channelsUri = TvContract.buildChannelsUriForInput(channel.getInputId());
        String[] projection = {Channels._ID, Channels.COLUMN_INTERNAL_PROVIDER_DATA};
        Cursor cursor = null;
        try {
            cursor = mContentResolver.query(channelsUri, projection, null, null, null);
            while (cursor != null && cursor.moveToNext()) {
                long rowId = cursor.getLong(findPosition(projection, Channels._ID));
                Map<String, String> parsedMap = parseInternalProviderData(cursor.getString(
                            findPosition(projection, Channels.COLUMN_INTERNAL_PROVIDER_DATA)));
                int frequency = Integer.parseInt(parsedMap.get(ChannelInfo.KEY_FREQUENCY));
                if (frequency == channel.getFrequency()) {
                    channelUri = TvContract.buildChannelUri(rowId);
                    mContentResolver.update(channelUri, buildAtvChannelData(channel), null, null);
                    insertLogo(channel.getLogoUrl(), channelUri);
                    ret = UPDATE_SUCCESS;
                } else {
                    ret = cursor.getCount();
                }
            }
            cursor.close();
        } catch (Exception e) {
            //TODO
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (DEBUG)
            Log.d(TAG, "update " + ((ret == UPDATE_SUCCESS) ? "found" : "notfound")
                +"][freq:"+channel.getFrequency()
                +"][name:"+channel.getDisplayName()
                +"][num:"+channel.getNumber()
                +"]");

        if (ret != UPDATE_SUCCESS) {
            channelUri = insertAtvChannel2(channel, ret);
        }
        return channelUri;
    }

    public Uri insertAtvChannel2(ChannelInfo channel, int channelNumber) {
        Uri channelsUri = TvContract.buildChannelsUriForInput(channel.getInputId());
        channel.setDisplayNumber(Integer.toString(channelNumber));
        Uri uri = mContentResolver.insert(TvContract.Channels.CONTENT_URI, buildAtvChannelData(channel));

        insertLogo(channel.getLogoUrl(), uri);

        return uri;
    }

    // If a channel exists, update it. If not, insert a new one.
    public Uri updateOrinsertDtvChannel2(ChannelInfo channel) {
        int ret = 0;
        Uri channelUri = null;

        Uri channelsUri = TvContract.buildChannelsUriForInput(channel.getInputId());
        String[] projection = {Channels._ID,
            Channels.COLUMN_SERVICE_ID,
            Channels.COLUMN_DISPLAY_NUMBER,
            Channels.COLUMN_DISPLAY_NAME,
            Channels.COLUMN_INTERNAL_PROVIDER_DATA};

        Cursor cursor = null;
        try {
            cursor = mContentResolver.query(channelsUri, projection, Channels.COLUMN_SERVICE_TYPE + "=?", new String[]{channel.getServiceType()}, null);
            while (cursor != null && cursor.moveToNext()) {
                long rowId = cursor.getLong(findPosition(projection, Channels._ID));
                int serviceId = cursor.getInt(findPosition(projection, Channels.COLUMN_SERVICE_ID));
                String name = cursor.getString(findPosition(projection, Channels.COLUMN_DISPLAY_NAME));
                int frequency = 0;
                int index = cursor.getColumnIndex(Channels.COLUMN_INTERNAL_PROVIDER_DATA);
                if (index >= 0) {
                    Map<String, String> parsedMap = ChannelInfo.stringToMap(cursor.getString(index));
                    frequency = Integer.parseInt(parsedMap.get(ChannelInfo.KEY_FREQUENCY));
                }
                if (serviceId == channel.getServiceId() && frequency == channel.getFrequency()
                    && name.equals(channel.getDisplayName())) {
                    channelUri = TvContract.buildChannelUri(rowId);
                    channel.setDisplayNumber(cursor.getString(findPosition(projection,Channels.COLUMN_DISPLAY_NUMBER)));
                    mContentResolver.update(channelUri, buildDtvChannelData(channel), null, null);
                    insertLogo(channel.getLogoUrl(), channelUri);
                    ret = UPDATE_SUCCESS;
                    break;
                }
            }

            if (ret != UPDATE_SUCCESS)
                ret = cursor.getCount();

        } catch (Exception e) {
            //TODO
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (DEBUG)
            Log.d(TAG, "update " + ((ret == UPDATE_SUCCESS) ? "found" : "notfound")
                +" DTV CH: [sid:"+channel.getServiceId()
                +"][freq:"+channel.getFrequency()
                +"][name:"+channel.getDisplayName()
                +"][num:"+channel.getNumber()
                +"]");

        if (ret != UPDATE_SUCCESS) {
            channelUri = insertDtvChannel2(channel, ret);
        }

        return channelUri;
    }

    public Uri insertDtvChannel2(ChannelInfo channel, int channelNumber) {
        Uri channelsUri = TvContract.buildChannelsUriForInput(channel.getInputId());
        channel.setDisplayNumber(Integer.toString(channelNumber));
        Uri uri = mContentResolver.insert(TvContract.Channels.CONTENT_URI, buildDtvChannelData(channel));
        insertLogo(channel.getLogoUrl(), uri);

        if (DEBUG)
            Log.d(TAG, "Insert DTV CH: [sid:"+channel.getServiceId()
                    +"][freq:"+channel.getFrequency()
                    +"][name:"+channel.getDisplayName()
                    +"][num:"+channel.getNumber()
                    +"]");

        return uri;
    }

    public ArrayList<ChannelInfo> getChannelList(String curInputId, String[] projection, String selection, String[] selectionArgs) {
        ArrayList<ChannelInfo> channelList = new ArrayList<>();
        Uri channelsUri = TvContract.buildChannelsUriForInput(curInputId);

        Cursor cursor = null;
        try {
            cursor = mContentResolver.query(channelsUri, projection, selection, selectionArgs, null);
            if (cursor == null || cursor.getCount() == 0) {
                return null;
            }

            while (cursor.moveToNext())
                channelList.add(ChannelInfo.fromCommonCursor(cursor));

        } catch (Exception e) {
            // TODO: handle exception
            Log.d(TAG, "Content provider query: " + e.getStackTrace());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return channelList;
    }


    private static final int BATCH_OPERATION_COUNT = 100;

    /**
     * Updates the system database, TvProvider, with the given programs.
     *
     * <p>If there is any overlap between the given and existing programs, the existing ones
     * will be updated with the given ones if they have the same title or replaced.
     *
     * @param channelUri The channel where the program info will be added.
     * @param newPrograms A list of {@link Program} instances which includes program
     *         information.
     */
    public void updatePrograms(Uri channelUri, List<Program> newPrograms) {
        final int fetchedProgramsCount = newPrograms.size();
        if (fetchedProgramsCount == 0) {
            return;
        }
        List<Program> oldPrograms = getPrograms(TvContract.buildProgramsUriForChannel(channelUri));

        Program firstNewProgram = newPrograms.get(0);
        int oldProgramsIndex = 0;
        int newProgramsIndex = 0;
        // Skip the past programs. They will be automatically removed by the system.
        for (Program program : oldPrograms) {
            if (program.getEndTimeUtcMillis() > firstNewProgram.getStartTimeUtcMillis())
                break;
            oldProgramsIndex++;
        }
        // Compare the new programs with old programs one by one and update/delete the old one or
        // insert new program if there is no matching program in the database.
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        while (newProgramsIndex < fetchedProgramsCount) {
            Program oldProgram = oldProgramsIndex < oldPrograms.size()
                    ? oldPrograms.get(oldProgramsIndex) : null;
            Program newProgram = newPrograms.get(newProgramsIndex);
            boolean addNewProgram = false;

            if (oldProgram != null) {

                if (oldProgram.equals(newProgram)) {
                    // Exact match. No need to update. Move on to the next programs.
                    oldProgramsIndex++;
                    newProgramsIndex++;
                    Log.d(TAG, "\tmatch");
                } else if (needsUpdate(oldProgram, newProgram)) {
                    // Partial match. Update the old program with the new one.
                    // NOTE: Use 'update' in this case instead of 'insert' and 'delete'. There could
                    // be application specific settings which belong to the old program.
                    ops.add(ContentProviderOperation.newUpdate(
                            TvContract.buildProgramUri(oldProgram.getProgramId()))
                            .withValues(newProgram.toContentValues())
                            .build());
                    oldProgramsIndex++;
                    newProgramsIndex++;
                    Log.d(TAG, "\tupdate");
                } else if (oldProgram.getEndTimeUtcMillis() < newProgram.getEndTimeUtcMillis()) {
                    // No match. Remove the old program first to see if the next program in
                    // {@code oldPrograms} partially matches the new program.
                    ops.add(ContentProviderOperation.newDelete(
                            TvContract.buildProgramUri(oldProgram.getProgramId()))
                            .build());
                    oldProgramsIndex++;
                    Log.d(TAG, "\tdelete old");
                } else {
                    // No match. The new program does not match any of the old programs. Insert it
                    // as a new program.
                    addNewProgram = true;
                    newProgramsIndex++;
                    Log.d(TAG, "\tnew insert");
                }
            } else {
                // No old programs. Just insert new programs.
                addNewProgram = true;
                newProgramsIndex++;
                Log.d(TAG, "no old, insert new");
            }
            if (addNewProgram) {
                ops.add(ContentProviderOperation
                        .newInsert(TvContract.Programs.CONTENT_URI)
                        .withValues(newProgram.toContentValues())
                        .build());
            }
            // Throttle the batch operation not to cause TransactionTooLargeException.
            if (ops.size() > BATCH_OPERATION_COUNT
                    || newProgramsIndex >= fetchedProgramsCount) {
                try {
                    mContentResolver.applyBatch(TvContract.AUTHORITY, ops);
                } catch (RemoteException | OperationApplicationException e) {
                    Log.e(TAG, "Failed to insert programs.", e);
                    return;
                }
                ops.clear();
            }
        }
    }

    public void updateProgram(Program program) {
        mContentResolver.update(TvContract.buildProgramUri(program.getProgramId()), program.toContentValues(), null, null);
    }

    /**
     * Returns {@code true} if the {@code oldProgram} program needs to be updated with the
     * {@code newProgram} program.
     */
    private static boolean needsUpdate(Program oldProgram, Program newProgram) {
        // NOTE: Here, we update the old program if it has the same title and overlaps with the new
        // program. The test logic is just an example and you can modify this. E.g. check whether
        // the both programs have the same program ID if your EPG supports any ID for the programs.
        return oldProgram.getTitle().equals(newProgram.getTitle())
                && !(oldProgram.getStartTimeUtcMillis() == newProgram.getStartTimeUtcMillis()
                    && oldProgram.getEndTimeUtcMillis() == newProgram.getEndTimeUtcMillis())
                && oldProgram.getStartTimeUtcMillis() < newProgram.getEndTimeUtcMillis()
                && newProgram.getStartTimeUtcMillis() < oldProgram.getEndTimeUtcMillis();
    }

    public List<Program> getPrograms(Uri uri) {
        Cursor cursor = null;
        List<Program> programs = new ArrayList<>();
        try {
            // TvProvider returns programs chronological order by default.
            cursor = mContentResolver.query(uri, null, null, null, null);
            if (cursor == null || cursor.getCount() == 0) {
                return programs;
            }
            while (cursor.moveToNext()) {
                programs.add(Program.fromCursor(cursor));
            }
        } catch (Exception e) {
            Log.w(TAG, "Unable to get programs for " + uri, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return programs;
    }

    public List<Program> getAppointedPrograms() {
        Uri uri = TvContract.Programs.CONTENT_URI;
        Cursor cursor = null;
        List<Program> programs = new ArrayList<>();
        try {
            // TvProvider returns programs chronological order by default.
            cursor = mContentResolver.query(uri, null, TvContract.Programs.COLUMN_INTERNAL_PROVIDER_FLAG1 + "=?", new String[]{"1"}, null);
            if (cursor == null || cursor.getCount() == 0) {
                return programs;
            }
            while (cursor.moveToNext()) {
                programs.add(Program.fromCursor(cursor));
            }
        } catch (Exception e) {
            Log.w(TAG, "Unable to get appointed programs ", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return programs;
    }

    public Program getProgram(long programId) {
        Uri uri = TvContract.buildProgramUri(programId);

        Cursor cursor = null;
        Program program = null;

        try {
            cursor = mContentResolver.query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToNext();
                program = Program.fromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get program from TvProvider.", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return program;
    }

    public Program getProgram(Uri channelUri, long nowtime) {
        List<Program> channel_programs = getPrograms(channelUri);
        Program program = null;
        int j = 0;
        for (j = 0; j < channel_programs.size(); j++) {
            program = channel_programs.get(j);
            if (nowtime >= program.getStartTimeUtcMillis() && nowtime < program.getEndTimeUtcMillis())
                break;
        }
        if (j == channel_programs.size())
            program = null;

        return program;
    }
}
