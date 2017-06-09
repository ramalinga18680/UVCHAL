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
 *  - 1 control system sysfs proc env & property
 */

#ifndef ANDROID_ISYSTEMCONTROLSERVICE_H
#define ANDROID_ISYSTEMCONTROLSERVICE_H

#include <utils/Errors.h>
#include <binder/IInterface.h>
#include <utils/String8.h>
#include <utils/String16.h>
#include "ISystemControlNotify.h"

namespace android {

// must be kept in sync with ISystemControlService.java
enum {
    GET_PROPERTY            = IBinder::FIRST_CALL_TRANSACTION,
    GET_PROPERTY_STRING     = IBinder::FIRST_CALL_TRANSACTION + 1,
    GET_PROPERTY_INT        = IBinder::FIRST_CALL_TRANSACTION + 2,
    GET_PROPERTY_LONG       = IBinder::FIRST_CALL_TRANSACTION + 3,
    GET_PROPERTY_BOOL       = IBinder::FIRST_CALL_TRANSACTION + 4,
    SET_PROPERTY            = IBinder::FIRST_CALL_TRANSACTION + 5,

    READ_SYSFS              = IBinder::FIRST_CALL_TRANSACTION + 6,
    WRITE_SYSFS             = IBinder::FIRST_CALL_TRANSACTION + 7,

    GET_BOOT_ENV            = IBinder::FIRST_CALL_TRANSACTION + 8,
    SET_BOOT_ENV            = IBinder::FIRST_CALL_TRANSACTION + 9,
    GET_DISPLAY_INFO        = IBinder::FIRST_CALL_TRANSACTION + 10,
    LOOP_MOUNT_UNMOUNT      = IBinder::FIRST_CALL_TRANSACTION + 11,

    MBOX_OUTPUT_MODE        = IBinder::FIRST_CALL_TRANSACTION + 12,
    OSD_MOUSE_MODE          = IBinder::FIRST_CALL_TRANSACTION + 13,
    OSD_MOUSE_PARA          = IBinder::FIRST_CALL_TRANSACTION + 14,
    SET_POSITION            = IBinder::FIRST_CALL_TRANSACTION + 15,
    GET_POSITION            = IBinder::FIRST_CALL_TRANSACTION + 16,

    //used by video playback
    SET_NATIVE_WIN_RECT     = IBinder::FIRST_CALL_TRANSACTION + 18,
    SET_VIDEO_PLAYING       = IBinder::FIRST_CALL_TRANSACTION + 19,

    SET_POWER_MODE          = IBinder::FIRST_CALL_TRANSACTION + 20,
    SET_DIGITAL_MODE        = IBinder::FIRST_CALL_TRANSACTION + 22,
    SET_3D_MODE             = IBinder::FIRST_CALL_TRANSACTION + 23,
    SET_LISTENER            = IBinder::FIRST_CALL_TRANSACTION + 24,
};

// ----------------------------------------------------------------------------

// must be kept in sync with interface defined in ISystemControlService.aidl
class ISystemControlService : public IInterface
{
public:
    DECLARE_META_INTERFACE(SystemControlService);

    virtual bool getProperty(const String16& key, String16& value) = 0;
    virtual bool getPropertyString(const String16& key, String16& value, String16& def) = 0;
    virtual int32_t getPropertyInt(const String16& key, int32_t def) = 0;
    virtual int64_t getPropertyLong(const String16& key, int64_t def) = 0;

    virtual bool getPropertyBoolean(const String16& key, bool def) = 0;
    virtual void setProperty(const String16& key, const String16& value) = 0;

    virtual bool readSysfs(const String16& path, String16& value) = 0;
    virtual bool writeSysfs(const String16& path, const String16& value) = 0;

    virtual void setBootEnv(const String16& key, const String16& value) = 0;
    virtual bool getBootEnv(const String16& key, String16& value) = 0;
    virtual void getDroidDisplayInfo(int &type, String16& socType, String16& defaultUI,
        int &fb0w, int &fb0h, int &fb0bits, int &fb0trip,
        int &fb1w, int &fb1h, int &fb1bits, int &fb1trip) = 0;

    virtual void loopMountUnmount(int &isMount, String16& path) = 0;

    virtual void setMboxOutputMode(const String16& mode) = 0;
    virtual int32_t set3DMode(const String16& mode3d) = 0;
    virtual void setDigitalMode(const String16& mode) = 0;
    virtual void setListener(const sp<ISystemControlNotify>& listener) = 0;
    virtual void setOsdMouseMode(const String16& mode) = 0;
    virtual void setOsdMousePara(int x, int y, int w, int h) = 0;
    virtual void setPosition(int left, int top, int width, int height) = 0;
    virtual void getPosition(const String16& mode, int &x, int &y, int &w, int &h) = 0;

    virtual void setNativeWindowRect(int x, int y, int w, int h) = 0;
    virtual void setVideoPlayingAxis(void) = 0;
    //virtual void setPowerMode(int mode) = 0;
};

// ----------------------------------------------------------------------------
class BnISystemControlService: public BnInterface<ISystemControlService> {
public:
    virtual status_t onTransact(uint32_t code, const Parcel& data,
            Parcel* reply, uint32_t flags = 0);
};

}; // namespace android

#endif // ANDROID_ISYSTEMCONTROLSERVICE_H
