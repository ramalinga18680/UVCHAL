/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  @author   tellen
 *  @version  1.0
 *  @date     2013/04/26
 *  @par function description:
 *  - 1 write property or sysfs from native to java service
 */

#define LOG_TAG "ISystemControl"
//#define LOG_NDEBUG 0

#include <utils/Log.h>
#include <stdint.h>
#include <sys/types.h>
#include <binder/Parcel.h>
#include <ISystemControlService.h>

namespace android {

class BpSystemControlService : public BpInterface<ISystemControlService>
{
public:
    BpSystemControlService(const sp<IBinder>& impl)
        : BpInterface<ISystemControlService>(impl)
    {
    }

    virtual bool getProperty(const String16& key, String16& value)
    {
        Parcel data, reply;
        data.writeInterfaceToken(ISystemControlService::getInterfaceDescriptor());
        data.writeString16(key);
        ALOGV("getProperty key:%s\n", String8(key).string());

        if (remote()->transact(GET_PROPERTY, data, &reply) != NO_ERROR) {
            ALOGD("getProperty could not contact remote\n");
            return false;
        }

        int32_t err = reply.readInt32();
        if (err == 0) {
            ALOGE("getProperty caught exception %d\n", err);
            return false;
        }
        value = reply.readString16();
        return true;
    }

    virtual bool getPropertyString(const String16& key, String16& value, String16& def)
    {
        Parcel data, reply;
        data.writeInterfaceToken(ISystemControlService::getInterfaceDescriptor());
        data.writeString16(key);
        data.writeString16(def);
        ALOGV("getPropertyString key:%s\n", String8(key).string());

        if (remote()->transact(GET_PROPERTY_STRING, data, &reply) != NO_ERROR) {
            ALOGD("getPropertyString could not contact remote\n");
            return false;
        }

        int32_t err = reply.readInt32();
        if (err == 0) {
            ALOGE("getPropertyString caught exception %d\n", err);
            return false;
        }

        value = reply.readString16();
        return true;
    }

    virtual int32_t getPropertyInt(const String16& key, int32_t def)
    {
        Parcel data, reply;
        data.writeInterfaceToken(ISystemControlService::getInterfaceDescriptor());
        data.writeString16(key);
        data.writeInt32(def);
        ALOGV("getPropertyInt key:%s\n", String8(key).string());

        if (remote()->transact(GET_PROPERTY_INT, data, &reply) != NO_ERROR) {
            ALOGE("getPropertyInt could not contact remote\n");
            return -1;
        }

        return reply.readInt32();
    }

    virtual int64_t getPropertyLong(const String16& key, int64_t def)
    {
        Parcel data, reply;
        data.writeInterfaceToken(ISystemControlService::getInterfaceDescriptor());
        data.writeString16(key);
        data.writeInt64(def);
        ALOGV("getPropertyLong key:%s\n", String8(key).string());

        if (remote()->transact(GET_PROPERTY_LONG, data, &reply) != NO_ERROR) {
            ALOGE("getPropertyLong could not contact remote\n");
            return -1;
        }

        return reply.readInt64();
    }

    virtual bool getPropertyBoolean(const String16& key, bool def)
    {
        Parcel data, reply;
        data.writeInterfaceToken(ISystemControlService::getInterfaceDescriptor());
        data.writeString16(key);
        data.writeInt32(def?1:0);

        ALOGV("getPropertyBoolean key:%s\n", String8(key).string());

        if (remote()->transact(GET_PROPERTY_BOOL, data, &reply) != NO_ERROR) {
            ALOGE("getPropertyBoolean could not contact remote\n");
            return false;
        }

        return reply.readInt32() != 0;
    }

    virtual void setProperty(const String16& key, const String16& value)
    {
        Parcel data, reply;
        data.writeInterfaceToken(ISystemControlService::getInterfaceDescriptor());
        data.writeString16(key);
        data.writeString16(value);
        ALOGV("setProperty key:%s, value:%s\n", String8(key).string(), String8(value).string());

        if (remote()->transact(SET_PROPERTY, data, &reply) != NO_ERROR) {
            ALOGE("setProperty could not contact remote\n");
            return;
        }
    }

    virtual bool readSysfs(const String16& path, String16& value)
    {
        Parcel data, reply;
        data.writeInterfaceToken(ISystemControlService::getInterfaceDescriptor());
        data.writeString16(path);
        ALOGV("setProperty path:%s\n", String8(path).string());

        if (remote()->transact(READ_SYSFS, data, &reply) != NO_ERROR) {
            ALOGE("readSysfs could not contact remote\n");
            return false;
        }

        value = reply.readString16();
        return true;
    }

    virtual bool writeSysfs(const String16& path, const String16& value)
    {
        Parcel data, reply;
        data.writeInterfaceToken(ISystemControlService::getInterfaceDescriptor());
        data.writeString16(path);
        data.writeString16(value);
        ALOGV("writeSysfs path:%s, value:%s\n", String8(path).string(), String8(value).string());

        if (remote()->transact(WRITE_SYSFS, data, &reply) != NO_ERROR) {
            ALOGE("writeSysfs could not contact remote\n");
            return false;
        }

        return reply.readInt32() != 0;
    }


    virtual bool getBootEnv(const String16& key, String16& value)
    {
        Parcel data, reply;
        data.writeInterfaceToken(ISystemControlService::getInterfaceDescriptor());
        data.writeString16(key);
        ALOGV("getBootEnv key:%s\n", String8(key).string());

        if (remote()->transact(GET_BOOT_ENV, data, &reply) != NO_ERROR) {
            ALOGD("getBootEnv could not contact remote\n");
            return false;
        }

        int32_t err = reply.readInt32();
        if (err == 0) {
            ALOGE("getBootEnv caught exception %d\n", err);
            return false;
        }
        value = reply.readString16();
        return true;
    }

    virtual void setBootEnv(const String16& key, const String16& value)
    {
        Parcel data, reply;
        data.writeInterfaceToken(ISystemControlService::getInterfaceDescriptor());
        data.writeString16(key);
        data.writeString16(value);
        ALOGV("setBootEnv key:%s, value:%s\n", String8(key).string(), String8(value).string());

        if (remote()->transact(SET_BOOT_ENV, data, &reply) != NO_ERROR) {
            ALOGE("setBootEnv could not contact remote\n");
            return;
        }
    }

    virtual void getDroidDisplayInfo(int &type, String16& socType, String16& defaultUI,
        int &fb0w, int &fb0h, int &fb0bits, int &fb0trip,
        int &fb1w, int &fb1h, int &fb1bits, int &fb1trip)
    {
        Parcel data, reply;
        data.writeInterfaceToken(ISystemControlService::getInterfaceDescriptor());
        ALOGV("getDroidDisplayInfo\n");

        if (remote()->transact(GET_DISPLAY_INFO, data, &reply) != NO_ERROR) {
            ALOGE("getDroidDisplayInfo could not contact remote\n");
            return;
        }

        type = reply.readInt32();
        socType = reply.readString16();
        defaultUI = reply.readString16();
        fb0w = reply.readInt32();
        fb0h = reply.readInt32();
        fb0bits = reply.readInt32();
        fb0trip = reply.readInt32();
        fb1w = reply.readInt32();
        fb1h = reply.readInt32();
        fb1bits = reply.readInt32();
        fb1trip = reply.readInt32();
    }

    virtual void loopMountUnmount(int &isMount, String16& path)
    {
        Parcel data, reply;
        data.writeInterfaceToken(ISystemControlService::getInterfaceDescriptor());
        data.writeInt32(isMount);
        data.writeString16(path);
        ALOGV("loop mount unmount isMount:%d, path:%s\n", isMount, String8(path).string());

        if (remote()->transact(LOOP_MOUNT_UNMOUNT, data, &reply) != NO_ERROR) {
            ALOGE("loopMountUnmount could not contact remote\n");
            return;
        }
    }

    virtual void setMboxOutputMode(const String16& mode)
    {
        Parcel data, reply;
        data.writeInterfaceToken(ISystemControlService::getInterfaceDescriptor());
        data.writeString16(mode);
        ALOGV("set mbox output mode:%s\n", String8(mode).string());

        if (remote()->transact(MBOX_OUTPUT_MODE, data, &reply) != NO_ERROR) {
            ALOGE("set mbox output mode could not contact remote\n");
            return;
        }
    }

    virtual int32_t set3DMode(const String16& mode3d)
    {
        Parcel data, reply;
        data.writeInterfaceToken(ISystemControlService::getInterfaceDescriptor());
        data.writeString16(mode3d);

        if (remote()->transact(SET_3D_MODE, data, &reply) != NO_ERROR) {
            ALOGE("set 3d mode could not contact remote\n");
            return -1;
        }

        return reply.readInt32();
    }

    virtual void setDigitalMode(const String16& mode)
    {
        Parcel data, reply;
        data.writeInterfaceToken(ISystemControlService::getInterfaceDescriptor());
        data.writeString16(mode);
        ALOGV("set digital mode:%s\n", String8(mode).string());

        if (remote()->transact(SET_DIGITAL_MODE, data, &reply) != NO_ERROR) {
            ALOGE("set digital mode could not contact remote\n");
            return;
        }
    }

    virtual void setOsdMouseMode(const String16& mode)
    {
        Parcel data, reply;
        data.writeInterfaceToken(ISystemControlService::getInterfaceDescriptor());
        data.writeString16(mode);
        ALOGV("set osd mouse mode:%s\n", String8(mode).string());

        if (remote()->transact(OSD_MOUSE_MODE, data, &reply) != NO_ERROR) {
            ALOGE("set osd mouse mode could not contact remote\n");
            return;
        }
    }

    virtual void setOsdMousePara(int x, int y, int w, int h)
    {
        Parcel data, reply;
        data.writeInterfaceToken(ISystemControlService::getInterfaceDescriptor());
        data.writeInt32(x);
        data.writeInt32(y);
        data.writeInt32(w);
        data.writeInt32(h);
        ALOGV("set osd mouse parameter x:%d, y:%d, w:%d, h:%d\n", x, y, w, h);

        if (remote()->transact(OSD_MOUSE_PARA, data, &reply) != NO_ERROR) {
            ALOGE("set osd mouse parameter could not contact remote\n");
            return;
        }
    }

    virtual void setPosition(int left, int top, int width, int height)
    {
        Parcel data, reply;
        data.writeInterfaceToken(ISystemControlService::getInterfaceDescriptor());
        data.writeInt32(left);
        data.writeInt32(top);
        data.writeInt32(width);
        data.writeInt32(height);
        ALOGV("set position x:%d, y:%d, w:%d, h:%d\n", left, top, width, height);

        if (remote()->transact(SET_POSITION, data, &reply) != NO_ERROR) {
            ALOGE("set position could not contact remote\n");
            return;
        }
    }

    virtual void getPosition(const String16& mode, int &x, int &y, int &w, int &h)
    {
        Parcel data, reply;
        data.writeInterfaceToken(ISystemControlService::getInterfaceDescriptor());
        data.writeString16(mode);

        if (remote()->transact(GET_POSITION, data, &reply) != NO_ERROR) {
            ALOGE("get position could not contact remote\n");
            return;
        }

        x = reply.readInt32();
        y = reply.readInt32();
        w = reply.readInt32();
        h = reply.readInt32();
        ALOGV("get position x:%d, y:%d, w:%d, h:%d\n", x, y, w, h);
    }

    virtual void setNativeWindowRect(int x, int y, int w, int h)
    {
        Parcel data, reply;
        data.writeInterfaceToken(ISystemControlService::getInterfaceDescriptor());
        data.writeInt32(x);
        data.writeInt32(y);
        data.writeInt32(w);
        data.writeInt32(h);
        ALOGV("set native window rect x:%d, y:%d, w:%d, h:%d\n", x, y, w, h);

        if (remote()->transact(SET_NATIVE_WIN_RECT, data, &reply) != NO_ERROR) {
            ALOGE("set native window rect could not contact remote\n");
            return;
        }
    }

    virtual void setVideoPlayingAxis(void)
    {
        Parcel data, reply;
        data.writeInterfaceToken(ISystemControlService::getInterfaceDescriptor());

        ALOGV("setVideoPlayingAxis\n");

        if (remote()->transact(SET_VIDEO_PLAYING, data, &reply) != NO_ERROR) {
            ALOGE("setVideoPlayingAxis could not contact remote\n");
            return;
        }
    }

    virtual void setListener(const sp<ISystemControlNotify>& listener)
    {
        Parcel data, reply;
        data.writeInterfaceToken(ISystemControlService::getInterfaceDescriptor());
        data.writeStrongBinder(IInterface::asBinder(listener));
        if (remote()->transact(SET_LISTENER, data, &reply) != NO_ERROR) {
            ALOGE("set listener could not contact remote\n");
            return;
        }
    }
};

IMPLEMENT_META_INTERFACE(SystemControlService, "droidlogic.ISystemControlService");

// ----------------------------------------------------------------------------

status_t BnISystemControlService::onTransact(
    uint32_t code, const Parcel& data, Parcel* reply, uint32_t flags)
{
    switch(code) {
        case GET_PROPERTY: {
            CHECK_INTERFACE(ISystemControlService, data, reply);
            String16 value;
            bool result = getProperty(data.readString16(), value);
            reply->writeInt32(result);
            reply->writeString16(value);
            return NO_ERROR;
        }
        case GET_PROPERTY_STRING: {
            CHECK_INTERFACE(ISystemControlService, data, reply);
            String16 key = data.readString16();
            String16 def = data.readString16();
            String16 value;
            bool result = getPropertyString(key, value, def);
            reply->writeInt32(result);
            reply->writeString16(value);
            return NO_ERROR;
        }
        case GET_PROPERTY_INT: {
            CHECK_INTERFACE(ISystemControlService, data, reply);
            String16 key = data.readString16();
            int32_t def = data.readInt32();
            int result = getPropertyInt(key, def);
            reply->writeInt32(result);
            return NO_ERROR;
        }
        case GET_PROPERTY_LONG: {
            CHECK_INTERFACE(ISystemControlService, data, reply);
            String16 key = data.readString16();
            int64_t def = data.readInt64();

            int64_t result = getPropertyLong(key, def);
            reply->writeInt64(result);
            return NO_ERROR;
        }
        case GET_PROPERTY_BOOL: {
            CHECK_INTERFACE(ISystemControlService, data, reply);

            String16 key = data.readString16();
            int32_t def = data.readInt32();
            bool result = getPropertyBoolean(key, (def!=0));
            reply->writeInt32(result?1:0);
            return NO_ERROR;
        }
        case SET_PROPERTY: {
            CHECK_INTERFACE(ISystemControlService, data, reply);
            String16 key = data.readString16();
            String16 val = data.readString16();
            setProperty(key, val);
            return NO_ERROR;
        }
        case READ_SYSFS: {
            CHECK_INTERFACE(ISystemControlService, data, reply);
            String16 sys = data.readString16();
            String16 value;
            readSysfs(sys, value);
            reply->writeString16(value);
            return NO_ERROR;
        }
        case WRITE_SYSFS: {
            CHECK_INTERFACE(ISystemControlService, data, reply);
            String16 sys = data.readString16();
            String16 value = data.readString16();
            bool result = writeSysfs(sys, value);
            reply->writeInt32(result?1:0);
            return NO_ERROR;
        }
        case GET_BOOT_ENV: {
            CHECK_INTERFACE(ISystemControlService, data, reply);
            String16 value;
            bool result = getBootEnv(data.readString16(), value);
            reply->writeInt32(result);
            reply->writeString16(value);
            return NO_ERROR;
        }
        case SET_BOOT_ENV: {
            CHECK_INTERFACE(ISystemControlService, data, reply);
            String16 key = data.readString16();
            String16 val = data.readString16();
            setBootEnv(key, val);
            return NO_ERROR;
        }
        case GET_DISPLAY_INFO: {
            String16 socType;
            String16 defaultUI;
            int type, fb0w, fb0h, fb0bits, fb0trip, fb1w, fb1h, fb1bits, fb1trip;

            CHECK_INTERFACE(ISystemControlService, data, reply);
            getDroidDisplayInfo(type, socType, defaultUI, fb0w, fb0h, fb0bits, fb0trip, fb1w, fb1h, fb1bits, fb1trip);

            reply->writeInt32(type);
            reply->writeString16(socType);
            reply->writeString16(defaultUI);
            reply->writeInt32(fb0w);
            reply->writeInt32(fb0h);
            reply->writeInt32(fb0bits);
            reply->writeInt32(fb0trip);
            reply->writeInt32(fb1w);
            reply->writeInt32(fb1h);
            reply->writeInt32(fb1bits);
            reply->writeInt32(fb1trip);
            return NO_ERROR;
        }
        case LOOP_MOUNT_UNMOUNT: {
            CHECK_INTERFACE(ISystemControlService, data, reply);
            int isMount = data.readInt32();
            String16 path = data.readString16();
            loopMountUnmount(isMount, path);
            return NO_ERROR;
        }
        case MBOX_OUTPUT_MODE: {
            CHECK_INTERFACE(ISystemControlService, data, reply);
            String16 mode = data.readString16();
            setMboxOutputMode(mode);
            return NO_ERROR;
        }
        case SET_3D_MODE: {
            CHECK_INTERFACE(ISystemControlService, data, reply);
            String16 mode3d = data.readString16();
            int result = set3DMode(mode3d);
            reply->writeInt32(result);
            return NO_ERROR;
        }
        case SET_DIGITAL_MODE: {
            CHECK_INTERFACE(ISystemControlService, data, reply);
            String16 mode = data.readString16();
            setDigitalMode(mode);
            return NO_ERROR;
        }
        case OSD_MOUSE_MODE: {
            CHECK_INTERFACE(ISystemControlService, data, reply);
            String16 mode = data.readString16();
            setOsdMouseMode(mode);
            return NO_ERROR;
        }
        case OSD_MOUSE_PARA: {
            CHECK_INTERFACE(ISystemControlService, data, reply);
            int32_t x = data.readInt32();
            int32_t y = data.readInt32();
            int32_t w = data.readInt32();
            int32_t h = data.readInt32();
            setOsdMousePara(x, y, w, h);
            return NO_ERROR;
        }
        case SET_POSITION: {
            CHECK_INTERFACE(ISystemControlService, data, reply);
            int32_t x = data.readInt32();
            int32_t y = data.readInt32();
            int32_t w = data.readInt32();
            int32_t h = data.readInt32();
            setPosition(x, y, w, h);
            return NO_ERROR;
        }
        case GET_POSITION: {
            int x, y, w, h;
            CHECK_INTERFACE(ISystemControlService, data, reply);
            String16 mode = data.readString16();
            getPosition(mode, x, y, w, h);
            reply->writeInt32(x);
            reply->writeInt32(y);
            reply->writeInt32(w);
            reply->writeInt32(h);
            return NO_ERROR;
        }
        case SET_NATIVE_WIN_RECT:{
            CHECK_INTERFACE(ISystemControlService, data, reply);
            int32_t x = data.readInt32();
            int32_t y = data.readInt32();
            int32_t w = data.readInt32();
            int32_t h = data.readInt32();
            setNativeWindowRect(x, y, w, h);
            return NO_ERROR;
        }
        case SET_VIDEO_PLAYING:{
            CHECK_INTERFACE(ISystemControlService, data, reply);
            setVideoPlayingAxis();
            return NO_ERROR;
        }
        case SET_LISTENER: {
            CHECK_INTERFACE(ISystemControlService, data, reply);
            sp<ISystemControlNotify> listener = interface_cast<ISystemControlNotify>(data.readStrongBinder());
            setListener(listener);
            return NO_ERROR;
        }

        default: {
            return BBinder::onTransact(code, data, reply, flags);
        }
    }
    // should be unreachable
    return NO_ERROR;
}

}; // namespace android
