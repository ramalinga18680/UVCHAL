/*
 * AMLOGIC Media Player client.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the named License,
 * or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA
 *
 * Author:  XiaoLiang.Wang <xiaoliang.wang@amlogic.com>
 * Date: 20160205
 */
package com.droidlogic.app;

import android.os.IBinder;
import android.os.Parcel;
import android.util.Log;

import com.droidlogic.app.MediaPlayerExt;

/** @hide */
public class MediaPlayerClient extends IMediaPlayerClient.Stub {
    private static final String TAG = "MediaPlayerClient";
    private static final boolean DEBUG = false;
    private MediaPlayerExt mMp;

    public MediaPlayerClient(MediaPlayerExt mp) {
        mMp = mp;
    }

    static IBinder create(MediaPlayerExt mp) {
        return (new MediaPlayerClient(mp)).asBinder();
    }

    public void notify(int msg, int ext1, int ext2, Parcel parcel) {
        if (DEBUG) Log.i(TAG, "[notify] msg:" + msg + ", ext1:" + ext1 + ", ext2:" + ext2 + ", parcel:" + parcel);
        mMp.postEvent(msg, ext1, ext2, parcel);
    }
}