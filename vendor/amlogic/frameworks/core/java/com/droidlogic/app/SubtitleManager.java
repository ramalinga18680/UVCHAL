package com.droidlogic.app;

import android.content.Context;
import android.content.ContentResolver;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.util.Log;
import java.lang.Integer;
import java.lang.Thread;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import com.droidlogic.SubTitleService.ISubTitleService;

public class SubtitleManager {
        private String TAG = "SubtitleManager";
        private boolean mDebug = false;
        private MediaPlayer mMediaPlayer = null;
        private ISubTitleService mService = null;
        private boolean mInvokeFromMp = false;
        private boolean mThreadStop = false;
        private String mPath = null;
        private Thread mThread = null;
        private int RETRY_MAX = 10;

        public SubtitleManager (MediaPlayer mp) {
            mMediaPlayer = mp;
            mDebug = false;
            checkDebug();
            if (!disable()) {
                getService();
            }
        }

        private boolean disable() {
            boolean ret = false;
            if (SystemProperties.getBoolean ("sys.subtitle.disable", false) ) {
                ret = true;
            }
            return ret;
        }

        private void checkDebug() {
            if (SystemProperties.getBoolean ("sys.subtitle.debug", false) ) {
                mDebug = true;
            }
        }

        private boolean optionEnable() {
            boolean ret = false;
            if (SystemProperties.getBoolean ("sys.subtitleOption.enable", false) ) {
                ret = true;
            }
            return ret;
        }

        private void LOGI(String msg) {
            if (mDebug) Log.i(TAG, msg);
        }

        private void LOGE(String msg) {
            /*if (mDebug)*/ Log.e(TAG, msg);
        }

        public void setInvokeFromMp (boolean fromMediaPlayer) {
            mInvokeFromMp = fromMediaPlayer;
        }

        public boolean getInvokeFromMp() {
            return mInvokeFromMp;
        }

        public void setSource (Context context, Uri uri) {
            if (context == null) {
                return;
            }

            if (uri == null) {
                return;
            }

            mPath = uri.getPath();

            String scheme = uri.getScheme();
            if (scheme == null || scheme.equals ("file") ) {
                mPath = uri.getPath();
                return;
            }

            try {
                ContentResolver resolver = context.getContentResolver();
                //add for subtitle service
                String mediaStorePath = uri.getPath();
                String[] cols = new String[] {
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DATA
                };

                if (scheme.equals ("content") ) {
                    int idx_check = (uri.toString() ).indexOf ("media/external/video/media");

                    if (idx_check > -1) {
                        int idx = mediaStorePath.lastIndexOf ("/");
                        String idStr = mediaStorePath.substring (idx + 1);
                        int id = Integer.parseInt (idStr);
                        LOGI("[setSource]id:" + id);

                        String where = MediaStore.Video.Media._ID + "=" + id;
                        Cursor cursor = resolver.query (MediaStore.Video.Media.EXTERNAL_CONTENT_URI, cols, where , null, null);
                        if (cursor != null && cursor.getCount() == 1) {
                            int colidx = cursor.getColumnIndexOrThrow (MediaStore.Video.Media.DATA);
                            cursor.moveToFirst();
                            mPath = cursor.getString (colidx);
                            LOGI("[setSource]mediaStorePath:" + mediaStorePath + ",mPath:" + mPath);
                        }
                    }
                }
            } catch (SecurityException ex) {
                LOGE("[setSource]SecurityException ex:" + ex);
            }
        }

        public void setSource (String path) {
            if (path == null) {
                return;
            }

            final Uri uri = Uri.parse (path);
            if ("file".equals (uri.getScheme() ) ) {
                path = uri.getPath();
            }
            mPath = path;
        }

        private int open (String path) {
            int ret = -1;
            LOGI("[open] path:" + path + ", mService:" + mService);
            if (path.startsWith ("/data/") || path.equals ("") ) {
                ret = -1;
            }

            try {
                if (mService != null) {
                    mService.open (path);
                    ret = 0;
                }
            } catch (RemoteException e) {
                throw new RuntimeException (e);
            }

            return ret;
        }

        public void openIdx (int idx) {
            LOGI("[openIdx] idx:" + idx +", mService:" + mService);
            if (idx < 0) {
                return;
            }

            try {
                if (mService != null) {
                    mService.openIdx (idx);
                }
            } catch (RemoteException e) {
                throw new RuntimeException (e);
            }
        }

        public void start() {
            LOGI("[start]mPath:" + mPath);
            mThreadStop = false;
            if (mPath != null) {
                int ret = open (mPath);
                if (ret == 0) {
                    show();

                    if (optionEnable() ) {
                        option();//show subtitle select option add for debug
                    }
                }
            }
        }

        public void close() {
            LOGI("[close]mService:" + mService + ", mThread:" + mThread);
            if (mThread != null) {
                mThreadStop = true;
                mThread = null;
            }

            try {
                if (mService != null) {
                    mService.close();
                }
            } catch (RemoteException e) {
                throw new RuntimeException (e);
            }
        }

        private void show() {
            LOGI("[show]total:" + total() + ", mThread:" + mThread);
            if (total() > 0) {
                if (mThread == null) {
                    mThread = new Thread (runnable);
                    mThread.start();
                }
            }
        }

        public void option() {
            LOGI("[option]mService:" + mService);
            try {
                if (mService != null) {
                    mService.option();
                }
            } catch (RemoteException e) {
                throw new RuntimeException (e);
            }
        }

        public int total() {
            LOGI("[total]mService:" + mService);
            int ret = 0;

            try {
                if (mService != null) {
                    ret = mService.getSubTotal();
                }
            } catch (RemoteException e) {
                throw new RuntimeException (e);
            }
            LOGI("[total]ret:" + ret);
            return ret;
        }

        public void next() {
            LOGI("[next]mService:" + mService);
            try {
                if (mService != null) {
                    mService.nextSub();
                }
            } catch (RemoteException e) {
                throw new RuntimeException (e);
            }
        }

        public void previous() {
            LOGI("[previous]mService:" + mService);
            try {
                if (mService != null) {
                    mService.preSub();
                }
            } catch (RemoteException e) {
                throw new RuntimeException (e);
            }
        }

        public void hide() {
            LOGI("[hide]mService:" + mService);
            try {
                if (mService != null) {
                    mService.hide();
                }
            } catch (RemoteException e) {
                throw new RuntimeException (e);
            }
        }

        public void display() {
            LOGI("[display]mService:" + mService);
            try {
                if (mService != null) {
                    mService.display();
                }
            } catch (RemoteException e) {
                throw new RuntimeException (e);
            }
        }

        public void clear() {
            LOGI("[clear]mService:" + mService);
            try {
                if (mService != null) {
                    mService.clear();
                }
            } catch (RemoteException e) {
                throw new RuntimeException (e);
            }
        }

        public void resetForSeek() {
            LOGI("[resetForSeek]mService:" + mService);
            try {
                if (mService != null) {
                    mService.resetForSeek();
                }
            } catch (RemoteException e) {
                throw new RuntimeException (e);
            }
        }

        public int getSubType() {
            LOGI("[getSubType]mService:" + mService);
            int ret = 0;

            try {
                if (mService != null) {
                    ret = mService.getSubType();
                }
            } catch (RemoteException e) {
                throw new RuntimeException (e);
            }

            LOGI("[getSubType]ret:" + ret);
            return ret;
        }

        public String getSubTypeStr() {
            LOGI("[getSubTypeStr]mService:" + mService);
            String type = null;

            try {
                if (mService != null) {
                    type = mService.getSubTypeStr();
                }
            } catch (RemoteException e) {
                throw new RuntimeException (e);
            }

            LOGI("[getSubTypeStr]type:" + type);
            return type;
        }

        public String getSubName (int idx) {
            LOGI("[getSubName]mService:" + mService);
            String name = null;

            try {
                if (mService != null) {
                    name = mService.getSubName (idx);
                }
            } catch (RemoteException e) {
                throw new RuntimeException (e);
            }

            LOGI("[getSubName]name[" + idx + "]:" + name);
            return name;
        }

        public String getSubLanguage (int idx) {
            LOGI("[getSubLanguage]mService:" + mService);
            String language = null;

            try {
                if (mService != null) {
                    language = mService.getSubLanguage (idx);
                }
            } catch (RemoteException e) {
                throw new RuntimeException (e);
            }

            LOGI("[getSubLanguage]language[" + idx + "]:" + language);
            return language;
        }

        public String getCurName() {
            LOGI("[getCurName]mService:" + mService);
            String name = null;

            try {
                if (mService != null) {
                    name = mService.getCurName();
                }
            } catch (RemoteException e) {
                throw new RuntimeException (e);
            }

            LOGI("[getCurName] name:" + name);
            return name;
        }

        private void getService() {
            int retry = RETRY_MAX;
            try {
                synchronized (this) {
                    while (true) {
                        IBinder b = ServiceManager.getService ("subtitle_service"/*Context.SUBTITLE_SERVICE*/);
                        mService = ISubTitleService.Stub.asInterface (b);
                        LOGI("[getService] mService:" + mService + ", retry:" + retry);
                        if (null != mService || retry <= 0) {
                            break;
                        }
                        retry --;
                        Thread.sleep(500);
                    }
                }
            }catch(InterruptedException e){}
        }

        public void release() {
            close();
        }

        public int getSubTypeDetial() {
            LOGI("[getSubTypeDetial] mService:" + mService);
            int ret = 0;

            try {
                if (mService != null) {
                    ret = mService.getSubTypeDetial();
                }
            } catch (RemoteException e) {
                throw new RuntimeException (e);
            }

            LOGI("[getSubTypeDetial] ret:" + ret);
            return ret;
        }

        public void setTextColor (int color) {
            LOGI("[setTextColor] color:" + color + ", mService:" + mService);
            try {
                if (mService != null) {
                    mService.setTextColor (color);
                }
            } catch (RemoteException e) {
                throw new RuntimeException (e);
            }
        }

        public void setTextSize (int size) {
            LOGI("[setTextSize] size:" + size + ", mService:" + mService);
            try {
                if (mService != null) {
                    mService.setTextSize (size);
                }
            } catch (RemoteException e) {
                throw new RuntimeException (e);
            }
        }

        public void setGravity (int gravity) {
            LOGI("[setGravity] gravity:" + gravity + ", mService:" + mService);
            try {
                if (mService != null) {
                    mService.setGravity (gravity);
                }
            } catch (RemoteException e) {
                throw new RuntimeException (e);
            }
        }

        public void setTextStyle (int style) {
            LOGI("[setTextStyle] style:" + style + ", mService:" + mService);
            try {
                if (mService != null) {
                    mService.setTextStyle (style);
                }
            } catch (RemoteException e) {
                throw new RuntimeException (e);
            }
        }

        public void setPosHeight (int height) {
            LOGI("[setPosHeight] height:" + height + ", mService:" + mService);
            try {
                if (mService != null) {
                    mService.setPosHeight (height);
                }
            } catch (RemoteException e) {
                throw new RuntimeException (e);
            }
        }

        public void setImgSubRatio (float ratioW, float ratioH, int maxW, int maxH) {
            LOGI("[setImgSubRatio] ratioW:" + ratioW + ", ratioH:" + ratioH + ",maxW:" + maxW + ",maxH:" + maxH + ", mService:" + mService);
            try {
                if (mService != null) {
                    mService.setImgSubRatio (ratioW, ratioH, maxW, maxH);
                }
            } catch (RemoteException e) {
                throw new RuntimeException (e);
            }
        }

        private static final int AML_SUBTITLE_START = 800; // random value
        private class EventHandler extends Handler {
                public EventHandler (Looper looper) {
                    super (looper);
                }

                @Override
                public void handleMessage (Message msg) {
                    switch (msg.arg1) {
                        case AML_SUBTITLE_START:
                            LOGI("[handleMessage]AML_SUBTITLE_START mPath:" + mPath);
                            if (mPath != null) {
                                int ret = open (mPath);

                                if (ret == 0) {
                                    show();

                                    if (optionEnable() ) {
                                        option();//show subtitle select option add for debug
                                    }
                                }
                            }
                        break;
                    }
                }
        }

        private String readSysfs (String path) {
            if (!new File (path).exists() ) {
                Log.e (TAG, "File not found: " + path);
                return null;
            }

            String str = null;
            StringBuilder value = new StringBuilder();

            try {
                FileReader fr = new FileReader (path);
                BufferedReader br = new BufferedReader (fr);

                try {
                    while ( (str = br.readLine() ) != null) {
                        if (str != null) {
                            value.append (str);
                        }
                    };

                    fr.close();

                    br.close();

                    if (value != null) {
                        return value.toString();
                    } else {
                        return null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }

        private int getCurrentPcr() {
            int pcr = 0;
            long pcrl = 0;
            String str = readSysfs ("/sys/class/tsync/pts_pcrscr");
            LOGI("[getCurrentPcr]readSysfs str:" + str);
            str = str.substring (2); // skip 0x

            if (str != null) {
                //pcr = (Integer.parseInt(str, 16));//90;// change to ms
                pcrl = (Long.parseLong (str, 16) );
                pcr = (int) (pcrl / 90);
            }

            LOGI("[getCurrentPcr]pcr:" + pcr);
            return pcr;
        }

        private Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int pos = 0;

                while (!mThreadStop) {
                    if (disable()) {
                        mThreadStop = true;
                        break;
                    }
                    LOGI("[runnable]showSub mService:" + mService);

                    //show subtitle
                    try {
                        if (mService != null) {
                            if (mMediaPlayer != null && mMediaPlayer.isPlaying() ) {
                                if (getSubTypeDetial() == 6) { //6:dvb type
                                    pos = getCurrentPcr();
                                } else {
                                    pos = mMediaPlayer.getCurrentPosition();
                                }
                                LOGI("[runnable]showSub:" + pos);
                            }

                            mService.showSub (pos);
                        } else {
                            mThreadStop = true;
                            break;
                        }
                    } catch (RemoteException e) {
                        throw new RuntimeException (e);
                    }

                    try {
                        Thread.sleep (300 - (pos % 300) );
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
}
