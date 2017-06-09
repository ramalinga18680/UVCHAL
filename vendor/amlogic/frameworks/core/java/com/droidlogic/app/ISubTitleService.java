package com.droidlogic.SubTitleService;

public interface ISubTitleService extends android.os.IInterface {
    /** Local-side IPC implementation stub class. */
    public static abstract class Stub extends android.os.Binder implements com.droidlogic.SubTitleService.ISubTitleService{
            private static final java.lang.String DESCRIPTOR = "com.droidlogic.SubTitleService.ISubTitleService";

            /** Construct the stub at attach it to the interface. */
            public Stub() {
                this.attachInterface (this, DESCRIPTOR);
            }

            /**
             * Cast an IBinder object into an com.droidlogic.SubTitleService.ISubTitleService interface,
             * generating a proxy if needed.
             */
            public static com.droidlogic.SubTitleService.ISubTitleService asInterface (android.os.IBinder obj) {
                if ( (obj == null) ) {
                    return null;
                }

                android.os.IInterface iin = obj.queryLocalInterface (DESCRIPTOR);

                if ( ( (iin != null) && (iin instanceof com.droidlogic.SubTitleService.ISubTitleService) ) ) {
                    return ( (com.droidlogic.SubTitleService.ISubTitleService) iin);
                }

                return new com.droidlogic.SubTitleService.ISubTitleService.Stub.Proxy (obj);
            }

            @Override
            public android.os.IBinder asBinder() {
                return this;
            }

            @Override
            public boolean onTransact (int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException{
                switch (code) {
                    case INTERFACE_TRANSACTION: {
                        reply.writeString (DESCRIPTOR);
                        return true;
                    }

                    case TRANSACTION_open: {
                        data.enforceInterface (DESCRIPTOR);
                        java.lang.String _arg0;
                        _arg0 = data.readString();
                        this.open (_arg0);
                        reply.writeNoException();
                        return true;
                    }

                    case TRANSACTION_openIdx: {
                        data.enforceInterface (DESCRIPTOR);
                        int _arg0;
                        _arg0 = data.readInt();
                        this.openIdx (_arg0);
                        reply.writeNoException();
                        return true;
                    }

                    case TRANSACTION_close: {
                        data.enforceInterface (DESCRIPTOR);
                        this.close();
                        reply.writeNoException();
                        return true;
                    }

                    case TRANSACTION_getSubTotal: {
                        data.enforceInterface (DESCRIPTOR);
                        int _result = this.getSubTotal();
                        reply.writeNoException();
                        reply.writeInt (_result);
                        return true;
                    }

                    case TRANSACTION_nextSub: {
                        data.enforceInterface (DESCRIPTOR);
                        this.nextSub();
                        reply.writeNoException();
                        return true;
                    }

                    case TRANSACTION_preSub: {
                        data.enforceInterface (DESCRIPTOR);
                        this.preSub();
                        reply.writeNoException();
                        return true;
                    }

                    case TRANSACTION_showSub: {
                        data.enforceInterface (DESCRIPTOR);
                        int _arg0;
                        _arg0 = data.readInt();
                        this.showSub (_arg0);
                        reply.writeNoException();
                        return true;
                    }

                    case TRANSACTION_option: {
                        data.enforceInterface (DESCRIPTOR);
                        this.option();
                        reply.writeNoException();
                        return true;
                    }

                    case TRANSACTION_getSubType: {
                        data.enforceInterface (DESCRIPTOR);
                        int _result = this.getSubType();
                        reply.writeNoException();
                        reply.writeInt (_result);
                        return true;
                    }

                    case TRANSACTION_getSubTypeStr: {
                        data.enforceInterface (DESCRIPTOR);
                        java.lang.String _result = this.getSubTypeStr();
                        reply.writeNoException();
                        reply.writeString (_result);
                        return true;
                    }

                    case TRANSACTION_getSubTypeDetial: {
                        data.enforceInterface (DESCRIPTOR);
                        int _result = this.getSubTypeDetial();
                        reply.writeNoException();
                        reply.writeInt (_result);
                        return true;
                    }

                    case TRANSACTION_setTextColor: {
                        data.enforceInterface (DESCRIPTOR);
                        int _arg0;
                        _arg0 = data.readInt();
                        this.setTextColor (_arg0);
                        reply.writeNoException();
                        return true;
                    }

                    case TRANSACTION_setTextSize: {
                        data.enforceInterface (DESCRIPTOR);
                        int _arg0;
                        _arg0 = data.readInt();
                        this.setTextSize (_arg0);
                        reply.writeNoException();
                        return true;
                    }

                    case TRANSACTION_setGravity: {
                        data.enforceInterface (DESCRIPTOR);
                        int _arg0;
                        _arg0 = data.readInt();
                        this.setGravity (_arg0);
                        reply.writeNoException();
                        return true;
                    }

                    case TRANSACTION_setTextStyle: {
                        data.enforceInterface (DESCRIPTOR);
                        int _arg0;
                        _arg0 = data.readInt();
                        this.setTextStyle (_arg0);
                        reply.writeNoException();
                        return true;
                    }

                    case TRANSACTION_setPosHeight: {
                        data.enforceInterface (DESCRIPTOR);
                        int _arg0;
                        _arg0 = data.readInt();
                        this.setPosHeight (_arg0);
                        reply.writeNoException();
                        return true;
                    }

                    case TRANSACTION_setImgSubRatio: {
                        data.enforceInterface (DESCRIPTOR);
                        float _arg0;
                        _arg0 = data.readFloat();
                        float _arg1;
                        _arg1 = data.readFloat();
                        int _arg2;
                        _arg2 = data.readInt();
                        int _arg3;
                        _arg3 = data.readInt();
                        this.setImgSubRatio (_arg0, _arg1, _arg2, _arg3);
                        reply.writeNoException();
                        return true;
                    }

                    case TRANSACTION_clear: {
                        data.enforceInterface (DESCRIPTOR);
                        this.clear();
                        reply.writeNoException();
                        return true;
                    }

                    case TRANSACTION_resetForSeek: {
                        data.enforceInterface (DESCRIPTOR);
                        this.resetForSeek();
                        reply.writeNoException();
                        return true;
                    }

                    case TRANSACTION_hide: {
                        data.enforceInterface (DESCRIPTOR);
                        this.hide();
                        reply.writeNoException();
                        return true;
                    }

                    case TRANSACTION_display: {
                        data.enforceInterface (DESCRIPTOR);
                        this.display();
                        reply.writeNoException();
                        return true;
                    }

                    case TRANSACTION_getCurName: {
                        data.enforceInterface (DESCRIPTOR);
                        java.lang.String _result = this.getCurName();
                        reply.writeNoException();
                        reply.writeString (_result);
                        return true;
                    }

                    case TRANSACTION_getSubName: {
                        data.enforceInterface (DESCRIPTOR);
                        int _arg0;
                        _arg0 = data.readInt();
                        java.lang.String _result = this.getSubName (_arg0);
                        reply.writeNoException();
                        reply.writeString (_result);
                        return true;
                    }

                    case TRANSACTION_getSubLanguage: {
                        data.enforceInterface (DESCRIPTOR);
                        int _arg0;
                        _arg0 = data.readInt();
                        java.lang.String _result = this.getSubLanguage (_arg0);
                        reply.writeNoException();
                        reply.writeString (_result);
                        return true;
                    }

                    case TRANSACTION_load: {
                        data.enforceInterface (DESCRIPTOR);
                        java.lang.String _arg0;
                        _arg0 = data.readString();
                        boolean _result = this.load (_arg0);
                        reply.writeNoException();
                        reply.writeInt ( ( (_result) ? (1) : (0) ) );
                        return true;
                    }
                }

                return super.onTransact (code, data, reply, flags);
            }

            private static class Proxy implements com.droidlogic.SubTitleService.ISubTitleService{
                    private android.os.IBinder mRemote;

                    Proxy (android.os.IBinder remote) {
                        mRemote = remote;
                    }

                    @Override
                    public android.os.IBinder asBinder() {
                        return mRemote;
                    }

                    public java.lang.String getInterfaceDescriptor() {
                        return DESCRIPTOR;
                    }

                    @Override
                    public void open (java.lang.String path) throws android.os.RemoteException{
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();

                        try {
                            _data.writeInterfaceToken (DESCRIPTOR);
                            _data.writeString (path);
                            mRemote.transact (Stub.TRANSACTION_open, _data, _reply, 0);
                            _reply.readException();
                        }
                        finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                    }

                    @Override
                    public void openIdx (int idx) throws android.os.RemoteException{
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();

                        try {
                            _data.writeInterfaceToken (DESCRIPTOR);
                            _data.writeInt (idx);
                            mRemote.transact (Stub.TRANSACTION_openIdx, _data, _reply, 0);
                            _reply.readException();
                        }
                        finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                    }

                    @Override
                    public void close() throws android.os.RemoteException{
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();

                        try {
                            _data.writeInterfaceToken (DESCRIPTOR);
                            mRemote.transact (Stub.TRANSACTION_close, _data, _reply, 0);
                            _reply.readException();
                        }
                        finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                    }

                    @Override
                    public int getSubTotal() throws android.os.RemoteException{
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();
                        int _result;

                        try {
                            _data.writeInterfaceToken (DESCRIPTOR);
                            mRemote.transact (Stub.TRANSACTION_getSubTotal, _data, _reply, 0);
                            _reply.readException();
                            _result = _reply.readInt();
                        }
                        finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                        return _result;
                    }

                    @Override
                    public void nextSub() throws android.os.RemoteException{
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();

                        try {
                            _data.writeInterfaceToken (DESCRIPTOR);
                            mRemote.transact (Stub.TRANSACTION_nextSub, _data, _reply, 0);
                            _reply.readException();
                        }
                        finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                    }

                    @Override
                    public void preSub() throws android.os.RemoteException{
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();

                        try {
                            _data.writeInterfaceToken (DESCRIPTOR);
                            mRemote.transact (Stub.TRANSACTION_preSub, _data, _reply, 0);
                            _reply.readException();
                        }
                        finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                    }
                    //void startInSubThread();
                    //void stopInSubThread();

                    @Override
                    public void showSub (int position) throws android.os.RemoteException{
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();

                        try {
                            _data.writeInterfaceToken (DESCRIPTOR);
                            _data.writeInt (position);
                            mRemote.transact (Stub.TRANSACTION_showSub, _data, _reply, 0);
                            _reply.readException();
                        }
                        finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                    }

                    @Override
                    public void option() throws android.os.RemoteException{
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();

                        try {
                            _data.writeInterfaceToken (DESCRIPTOR);
                            mRemote.transact (Stub.TRANSACTION_option, _data, _reply, 0);
                            _reply.readException();
                        }
                        finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                    }

                    @Override
                    public int getSubType() throws android.os.RemoteException{
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();
                        int _result;

                        try {
                            _data.writeInterfaceToken (DESCRIPTOR);
                            mRemote.transact (Stub.TRANSACTION_getSubType, _data, _reply, 0);
                            _reply.readException();
                            _result = _reply.readInt();
                        }
                        finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                        return _result;
                    }

                    @Override
                    public java.lang.String getSubTypeStr() throws android.os.RemoteException{
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();
                        java.lang.String _result;

                        try {
                            _data.writeInterfaceToken (DESCRIPTOR);
                            mRemote.transact (Stub.TRANSACTION_getSubTypeStr, _data, _reply, 0);
                            _reply.readException();
                            _result = _reply.readString();
                        }
                        finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                        return _result;
                    }

                    @Override public int getSubTypeDetial() throws android.os.RemoteException
                    {
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();
                        int _result;

                        try {
                            _data.writeInterfaceToken (DESCRIPTOR);
                            mRemote.transact (Stub.TRANSACTION_getSubTypeDetial, _data, _reply, 0);
                            _reply.readException();
                            _result = _reply.readInt();
                        }
                        finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                        return _result;
                    }

                    @Override
                    public void setTextColor (int color) throws android.os.RemoteException{
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();

                        try {
                            _data.writeInterfaceToken (DESCRIPTOR);
                            _data.writeInt (color);
                            mRemote.transact (Stub.TRANSACTION_setTextColor, _data, _reply, 0);
                            _reply.readException();
                        }
                        finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                    }

                    @Override
                    public void setTextSize (int size) throws android.os.RemoteException{
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();

                        try {
                            _data.writeInterfaceToken (DESCRIPTOR);
                            _data.writeInt (size);
                            mRemote.transact (Stub.TRANSACTION_setTextSize, _data, _reply, 0);
                            _reply.readException();
                        }
                        finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                    }

                    @Override
                    public void setGravity (int gravity) throws android.os.RemoteException{
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();

                        try {
                            _data.writeInterfaceToken (DESCRIPTOR);
                            _data.writeInt (gravity);
                            mRemote.transact (Stub.TRANSACTION_setGravity, _data, _reply, 0);
                            _reply.readException();
                        }
                        finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                    }

                    @Override
                    public void setTextStyle (int style) throws android.os.RemoteException{
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();

                        try {
                            _data.writeInterfaceToken (DESCRIPTOR);
                            _data.writeInt (style);
                            mRemote.transact (Stub.TRANSACTION_setTextStyle, _data, _reply, 0);
                            _reply.readException();
                        }
                        finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                    }

                    @Override
                    public void setPosHeight (int height) throws android.os.RemoteException{
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();

                        try {
                            _data.writeInterfaceToken (DESCRIPTOR);
                            _data.writeInt (height);
                            mRemote.transact (Stub.TRANSACTION_setPosHeight, _data, _reply, 0);
                            _reply.readException();
                        }
                        finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                    }

                    @Override
                    public void setImgSubRatio (float ratioW, float ratioH, int maxW, int maxH) throws android.os.RemoteException{
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();

                        try {
                            _data.writeInterfaceToken (DESCRIPTOR);
                            _data.writeFloat (ratioW);
                            _data.writeFloat (ratioH);
                            _data.writeInt (maxW);
                            _data.writeInt (maxH);
                            mRemote.transact (Stub.TRANSACTION_setImgSubRatio, _data, _reply, 0);
                            _reply.readException();
                        }
                        finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                    }

                    @Override
                    public void clear() throws android.os.RemoteException{
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();

                        try {
                            _data.writeInterfaceToken (DESCRIPTOR);
                            mRemote.transact (Stub.TRANSACTION_clear, _data, _reply, 0);
                            _reply.readException();
                        }
                        finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                    }

                    @Override
                    public void resetForSeek() throws android.os.RemoteException{
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();

                        try {
                            _data.writeInterfaceToken (DESCRIPTOR);
                            mRemote.transact (Stub.TRANSACTION_resetForSeek, _data, _reply, 0);
                            _reply.readException();
                        }
                        finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                    }

                    @Override
                    public void hide() throws android.os.RemoteException{
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();

                        try {
                            _data.writeInterfaceToken (DESCRIPTOR);
                            mRemote.transact (Stub.TRANSACTION_hide, _data, _reply, 0);
                            _reply.readException();
                        }
                        finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                    }

                    @Override
                    public void display() throws android.os.RemoteException{
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();

                        try {
                            _data.writeInterfaceToken (DESCRIPTOR);
                            mRemote.transact (Stub.TRANSACTION_display, _data, _reply, 0);
                            _reply.readException();
                        }
                        finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                    }

                    @Override
                    public java.lang.String getCurName() throws android.os.RemoteException{
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();
                        java.lang.String _result;

                        try {
                            _data.writeInterfaceToken (DESCRIPTOR);
                            mRemote.transact (Stub.TRANSACTION_getCurName, _data, _reply, 0);
                            _reply.readException();
                            _result = _reply.readString();
                        }
                        finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                        return _result;
                    }

                    @Override
                    public java.lang.String getSubName (int idx) throws android.os.RemoteException{
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();
                        java.lang.String _result;

                        try {
                            _data.writeInterfaceToken (DESCRIPTOR);
                            _data.writeInt (idx);
                            mRemote.transact (Stub.TRANSACTION_getSubName, _data, _reply, 0);
                            _reply.readException();
                            _result = _reply.readString();
                        }
                        finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                        return _result;
                    }

                    @Override
                    public java.lang.String getSubLanguage (int idx) throws android.os.RemoteException{
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();
                        java.lang.String _result;

                        try {
                            _data.writeInterfaceToken (DESCRIPTOR);
                            _data.writeInt (idx);
                            mRemote.transact (Stub.TRANSACTION_getSubLanguage, _data, _reply, 0);
                            _reply.readException();
                            _result = _reply.readString();
                        }
                        finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                        return _result;
                    }

                    @Override
                    public boolean load (java.lang.String path) throws android.os.RemoteException{
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();
                        boolean _result;

                        try {
                            _data.writeInterfaceToken (DESCRIPTOR);
                            _data.writeString (path);
                            mRemote.transact (Stub.TRANSACTION_load, _data, _reply, 0);
                            _reply.readException();
                            _result = (0 != _reply.readInt() );
                        }
                        finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                        return _result;
                    }
            }

            static final int TRANSACTION_open = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
            static final int TRANSACTION_openIdx = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
            static final int TRANSACTION_close = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
            static final int TRANSACTION_getSubTotal = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
            static final int TRANSACTION_nextSub = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
            static final int TRANSACTION_preSub = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
            static final int TRANSACTION_showSub = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
            static final int TRANSACTION_option = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
            static final int TRANSACTION_getSubType = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
            static final int TRANSACTION_getSubTypeStr = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
            static final int TRANSACTION_getSubTypeDetial = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
            static final int TRANSACTION_setTextColor = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
            static final int TRANSACTION_setTextSize = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
            static final int TRANSACTION_setGravity = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
            static final int TRANSACTION_setTextStyle = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
            static final int TRANSACTION_setPosHeight = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
            static final int TRANSACTION_setImgSubRatio = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
            static final int TRANSACTION_clear = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
            static final int TRANSACTION_resetForSeek = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
            static final int TRANSACTION_hide = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
            static final int TRANSACTION_display = (android.os.IBinder.FIRST_CALL_TRANSACTION + 20);
            static final int TRANSACTION_getCurName = (android.os.IBinder.FIRST_CALL_TRANSACTION + 21);
            static final int TRANSACTION_getSubName = (android.os.IBinder.FIRST_CALL_TRANSACTION + 22);
            static final int TRANSACTION_getSubLanguage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 23);
            static final int TRANSACTION_load = (android.os.IBinder.FIRST_CALL_TRANSACTION + 24);
    }

    public void open (java.lang.String path) throws android.os.RemoteException;
    public void openIdx (int idx) throws android.os.RemoteException;
    public void close() throws android.os.RemoteException;
    public int getSubTotal() throws android.os.RemoteException;
    public void nextSub() throws android.os.RemoteException;
    public void preSub() throws android.os.RemoteException;
    //void startInSubThread();
    //void stopInSubThread();

    public void showSub (int position) throws android.os.RemoteException;
    public void option() throws android.os.RemoteException;
    public int getSubType() throws android.os.RemoteException;
    public java.lang.String getSubTypeStr() throws android.os.RemoteException;
    public int getSubTypeDetial() throws android.os.RemoteException;
    public void setTextColor (int color) throws android.os.RemoteException;
    public void setTextSize (int size) throws android.os.RemoteException;
    public void setGravity (int gravity) throws android.os.RemoteException;
    public void setTextStyle (int style) throws android.os.RemoteException;
    public void setPosHeight (int height) throws android.os.RemoteException;
    public void setImgSubRatio (float ratioW, float ratioH, int maxW, int maxH) throws android.os.RemoteException;
    public void clear() throws android.os.RemoteException;
    public void resetForSeek() throws android.os.RemoteException;
    public void hide() throws android.os.RemoteException;
    public void display() throws android.os.RemoteException;
    public java.lang.String getCurName() throws android.os.RemoteException;
    public java.lang.String getSubName (int idx) throws android.os.RemoteException;
    public java.lang.String getSubLanguage (int idx) throws android.os.RemoteException;
    public boolean load (java.lang.String path) throws android.os.RemoteException;
}
