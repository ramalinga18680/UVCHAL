package com.droidlogic.app;

import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

public class SystemControlManager {
    private static final String TAG                 = "SysControlManager";

    //must sync with DisplayMode.h
    public static final boolean USE_BEST_MODE       = false;
    public static final int DISPLAY_TYPE_NONE       = 0;
    public static final int DISPLAY_TYPE_TABLET     = 1;
    public static final int DISPLAY_TYPE_MBOX       = 2;
    public static final int DISPLAY_TYPE_TV         = 3;

    private static final String SYS_TOKEN           = "droidlogic.ISystemControlService";
    private static final int REMOTE_EXCEPTION       = -0xffff;

    private static final int GET_PROPERTY           = IBinder.FIRST_CALL_TRANSACTION;
    private static final int GET_PROPERTY_STRING    = IBinder.FIRST_CALL_TRANSACTION + 1;
    private static final int GET_PROPERTY_INT       = IBinder.FIRST_CALL_TRANSACTION + 2;
    private static final int GET_PROPERTY_LONG      = IBinder.FIRST_CALL_TRANSACTION + 3;
    private static final int GET_PROPERTY_BOOL      = IBinder.FIRST_CALL_TRANSACTION + 4;
    private static final int SET_PROPERTY           = IBinder.FIRST_CALL_TRANSACTION + 5;
    private static final int READ_SYSFS             = IBinder.FIRST_CALL_TRANSACTION + 6;
    private static final int WRITE_SYSFS            = IBinder.FIRST_CALL_TRANSACTION + 7;

    private static final int GET_BOOT_ENV           = IBinder.FIRST_CALL_TRANSACTION + 8;
    private static final int SET_BOOT_ENV           = IBinder.FIRST_CALL_TRANSACTION + 9;
    private static final int GET_DISPLAY_INFO       = IBinder.FIRST_CALL_TRANSACTION + 10;
    private static final int LOOP_MOUNT_UNMOUNT     = IBinder.FIRST_CALL_TRANSACTION + 11;

    private static final int MBOX_OUTPUT_MODE       = IBinder.FIRST_CALL_TRANSACTION + 12;
    private static final int OSD_MOUSE_MODE         = IBinder.FIRST_CALL_TRANSACTION + 13;
    private static final int OSD_MOUSE_PARA         = IBinder.FIRST_CALL_TRANSACTION + 14;
    private static final int SET_POSITION           = IBinder.FIRST_CALL_TRANSACTION + 15;
    private static final int GET_POSITION           = IBinder.FIRST_CALL_TRANSACTION + 16;

    private static final int SET_NATIVE_WIN_RECT     = IBinder.FIRST_CALL_TRANSACTION + 18;
    private static final int SET_VIDEO_PLAYING       = IBinder.FIRST_CALL_TRANSACTION + 19;
    private static final int SET_POWER_MODE          = IBinder.FIRST_CALL_TRANSACTION + 20;
    private static final int SET_DIGITAL_MODE        = IBinder.FIRST_CALL_TRANSACTION + 22;
    private static final int SET_3D_MODE             = IBinder.FIRST_CALL_TRANSACTION + 23;
    private static final int SET_LISTENER            = IBinder.FIRST_CALL_TRANSACTION + 24;

    private Context mContext;
    private IBinder mIBinder = null;
    public SystemControlManager(Context context){
        mContext = context;

        try {
            Object object = Class.forName("android.os.ServiceManager")
                    .getMethod("getService", new Class[] { String.class })
                    .invoke(null, new Object[] { "system_control" });
            mIBinder = (IBinder)object;
        }
        catch (Exception ex) {
            Log.e(TAG, "system control manager init fail:" + ex);
        }
    }

    public String getProperty(String prop) {
        try {
            if (null != mIBinder) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken(SYS_TOKEN);
                data.writeString(prop);
                mIBinder.transact(GET_PROPERTY, data, reply, 0);
                int result = reply.readInt();
                String value = reply.readString();
                reply.recycle();
                data.recycle();
                return value;
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "getProperty:" + ex);
        }

        return null;
    }

    public String getPropertyString(String prop, String def) {
        try {
            if (null != mIBinder) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken(SYS_TOKEN);
                data.writeString(prop);
                data.writeString(def);
                mIBinder.transact(GET_PROPERTY_STRING, data, reply, 0);
                int result = reply.readInt();
                String value = reply.readString();
                reply.recycle();
                data.recycle();
                return value;
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "getPropertyString:" + ex);
        }

        return null;
    }

    public int getPropertyInt(String prop, int def) {
        try {
            if (null != mIBinder) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken(SYS_TOKEN);
                data.writeString(prop);
                data.writeInt(def);
                mIBinder.transact(GET_PROPERTY_INT, data, reply, 0);
                int result = reply.readInt();
                reply.recycle();
                data.recycle();
                return result;
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "getPropertyInt:" + ex);
        }

        return 0;
    }

    public long getPropertyLong(String prop, long def) {
        try {
            if (null != mIBinder) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken(SYS_TOKEN);
                data.writeString(prop);
                data.writeLong(def);
                mIBinder.transact(GET_PROPERTY_LONG, data, reply, 0);
                long result = reply.readLong();
                reply.recycle();
                data.recycle();
                return result;
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "getPropertyLong:" + ex);
        }

        return 0;
    }

    public boolean getPropertyBoolean(String prop, boolean def) {
        try {
            if (null != mIBinder) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken(SYS_TOKEN);
                data.writeString(prop);
                data.writeInt(def?1:0);
                mIBinder.transact(GET_PROPERTY_BOOL, data, reply, 0);
                int result = reply.readInt();
                reply.recycle();
                data.recycle();
                return result!=0;
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "getPropertyBoolean:" + ex);
        }

        return false;
    }

    public void setProperty(String prop, String val) {
        try {
            if (null != mIBinder) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken(SYS_TOKEN);
                data.writeString(prop);
                data.writeString(val);
                mIBinder.transact(SET_PROPERTY, data, reply, 0);
                reply.recycle();
                data.recycle();
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "setProperty:" + ex);
        }
    }

    public String readSysFs(String path) {
        try {
            if (null != mIBinder) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken(SYS_TOKEN);
                data.writeString(path);
                mIBinder.transact(READ_SYSFS, data, reply, 0);
                String value = reply.readString();
                reply.recycle();
                data.recycle();
                return value;
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "readSysFs:" + ex);
        }

        return null;
    }

    public boolean writeSysFs(String path, String val) {
        try {
            if (null != mIBinder) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken(SYS_TOKEN);
                data.writeString(path);
                data.writeString(val);
                mIBinder.transact(WRITE_SYSFS, data, reply, 0);
                int result = reply.readInt();
                reply.recycle();
                data.recycle();
                return result!=0;
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "writeSysFs:" + ex);
        }

        return false;
    }

    public String getBootenv(String prop, String def) {
        try {
            if (null != mIBinder) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken(SYS_TOKEN);
                data.writeString(prop);
                mIBinder.transact(GET_BOOT_ENV, data, reply, 0);
                int result = reply.readInt();
                String value = reply.readString();
                reply.recycle();
                data.recycle();
                if (0 == result)
                    return def;//have some error
                else
                    return value;
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "get boot env:" + ex);
        }

        return null;
    }

    public void setBootenv(String prop, String val) {
        try {
            if (null != mIBinder) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken(SYS_TOKEN);
                data.writeString(prop);
                data.writeString(val);
                mIBinder.transact(SET_BOOT_ENV, data, reply, 0);
                reply.recycle();
                data.recycle();
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "set boot env:" + ex);
        }
    }

    public DisplayInfo getDisplayInfo() {
        DisplayInfo info = null;
        try {
            if (null != mIBinder) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken(SYS_TOKEN);
                mIBinder.transact(GET_DISPLAY_INFO, data, reply, 0);
                info = new DisplayInfo();
                info.type = reply.readInt();
                info.socType = reply.readString();
                info.defaultUI = reply.readString();
                info.fb0Width = reply.readInt();
                info.fb0Height = reply.readInt();
                info.fb0FbBits = reply.readInt();
                info.fb0TripleEnable = (reply.readInt()==0)?false:true;
                info.fb1Width = reply.readInt();
                info.fb1Height = reply.readInt();
                info.fb1FbBits = reply.readInt();
                info.fb1TripleEnable = (reply.readInt()==0)?false:true;

                reply.recycle();
                data.recycle();
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "get display info:" + ex);
        }

        return info;
    }

    public void loopMountUnmount(boolean isMount, String path){
        try {
            if (null != mIBinder) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken(SYS_TOKEN);
                data.writeInt(isMount?1:0);
                data.writeString(path);
                mIBinder.transact(LOOP_MOUNT_UNMOUNT, data, reply, 0);
                reply.recycle();
                data.recycle();
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "loop mount unmount:" + ex);
        }
    }

    public void setMboxOutputMode(String mode) {
        try {
            if (null != mIBinder) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken(SYS_TOKEN);
                data.writeString(mode);
                mIBinder.transact(MBOX_OUTPUT_MODE, data, reply, 0);
                reply.recycle();
                data.recycle();
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "set mbox output mode:" + ex);
        }
    }

    public int set3DMode(String mode3d) {
        int ret = -1;
        Log.e(TAG, "[set3DMode]mode3d:" + mode3d);
        try {
            if (null != mIBinder) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken(SYS_TOKEN);
                data.writeString(mode3d);
                mIBinder.transact(SET_3D_MODE, data, reply, 0);
                ret = reply.readInt();
                reply.recycle();
                data.recycle();
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "set 3d mode:" + ex);
        }

        return ret;
    }

    public void setDigitalMode(String mode) {
        try {
            if (null != mIBinder) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken(SYS_TOKEN);
                data.writeString(mode);
                mIBinder.transact(SET_DIGITAL_MODE, data, reply, 0);
                reply.recycle();
                data.recycle();
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "set digital mode:" + ex);
        }
    }

    public void setOsdMouseMode(String mode) {
        try {
            if (null != mIBinder) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken(SYS_TOKEN);
                data.writeString(mode);
                mIBinder.transact(OSD_MOUSE_MODE, data, reply, 0);
                reply.recycle();
                data.recycle();
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "set osd mouse mode:" + ex);
        }
    }

    public void setOsdMousePara(int x, int y, int w, int h) {
        try {
            if (null != mIBinder) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken(SYS_TOKEN);
                data.writeInt(x);
                data.writeInt(y);
                data.writeInt(w);
                data.writeInt(h);
                mIBinder.transact(OSD_MOUSE_PARA, data, reply, 0);
                reply.recycle();
                data.recycle();
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "set osd mouse parameter:" + ex);
        }
    }

    public void setPosition(int x, int y, int w, int h) {
        try {
            if (null != mIBinder) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken(SYS_TOKEN);
                data.writeInt(x);
                data.writeInt(y);
                data.writeInt(w);
                data.writeInt(h);
                mIBinder.transact(SET_POSITION, data, reply, 0);
                reply.recycle();
                data.recycle();
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "set position:" + ex);
        }
    }

    public int[] getPosition(String mode) {
        int[] curPosition = { 0, 0, 1280, 720 };
        try {
            if (null != mIBinder) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken(SYS_TOKEN);
                data.writeString(mode);
                mIBinder.transact(GET_POSITION, data, reply, 0);
                curPosition[0] = reply.readInt();
                curPosition[1] = reply.readInt();
                curPosition[2] = reply.readInt();
                curPosition[3] = reply.readInt();
                reply.recycle();
                data.recycle();
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "get position:" + ex);
        }
        return curPosition;
    }

    public void setListener(ISystemControlNotify listener) {
        try {
            if (null != mIBinder) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken(SYS_TOKEN);
                data.writeStrongInterface(listener);
                mIBinder.transact(SET_LISTENER, data, reply, 0);
                reply.recycle();
                data.recycle();
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "set callback:" + ex);
        }
    }

    public static class DisplayInfo{
        //1:tablet 2:MBOX 3:TV
        public int type;
        public String socType;
        public String defaultUI;
        public int fb0Width;
        public int fb0Height;
        public int fb0FbBits;
        public boolean fb0TripleEnable;//Triple Buffer enable or not

        public int fb1Width;
        public int fb1Height;
        public int fb1FbBits;
        public boolean fb1TripleEnable;//Triple Buffer enable or not
    }
}
