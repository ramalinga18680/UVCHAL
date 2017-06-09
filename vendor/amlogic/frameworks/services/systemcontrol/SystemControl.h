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
 *  @author   Tellen Yu
 *  @version  1.0
 *  @date     2014/10/22
 *  @par function description:
 *  - 1 control system sysfs proc env & property
 */

#ifndef ANDROID_SYSTEM_CONTROL_H
#define ANDROID_SYSTEM_CONTROL_H

#include <utils/Errors.h>
#include <utils/String8.h>
#include <utils/String16.h>
#include <utils/Mutex.h>
#include <ISystemControlService.h>

#include "SysWrite.h"
#include "common.h"
#include "DisplayMode.h"

extern "C" int vdc_loop(int argc, char **argv);

namespace android {
// ----------------------------------------------------------------------------

class SystemControl : public BnISystemControlService
{
public:
    SystemControl(const char *path);
    virtual ~SystemControl();

    virtual bool getProperty(const String16& key, String16& value);
    virtual bool getPropertyString(const String16& key, String16& value, String16& def);
    virtual int32_t getPropertyInt(const String16& key, int32_t def);
    virtual int64_t getPropertyLong(const String16& key, int64_t def);

    virtual bool getPropertyBoolean(const String16& key, bool def);
    virtual void setProperty(const String16& key, const String16& value);

    virtual bool readSysfs(const String16& path, String16& value);
    virtual bool writeSysfs(const String16& path, const String16& value);

    virtual void setBootEnv(const String16& key, const String16& value);
    virtual bool getBootEnv(const String16& key, String16& value);

    virtual void getDroidDisplayInfo(int &type, String16& socType, String16& defaultUI,
        int &fb0w, int &fb0h, int &fb0bits, int &fb0trip,
        int &fb1w, int &fb1h, int &fb1bits, int &fb1trip);

    virtual void loopMountUnmount(int &isMount, String16& path);

    virtual void setMboxOutputMode(const String16& mode);
    virtual int32_t set3DMode(const String16& mode3d);
    virtual void setDigitalMode(const String16& mode);
    virtual void setListener(const sp<ISystemControlNotify>& listener);
    virtual void setOsdMouseMode(const String16& mode);
    virtual void setOsdMousePara(int x, int y, int w, int h);
    virtual void setPosition(int left, int top, int width, int height);
    virtual void getPosition(const String16& mode, int &x, int &y, int &w, int &h);
    virtual void setNativeWindowRect(int x, int y, int w, int h);
    virtual void setVideoPlayingAxis();

    static void instantiate(const char *cfgpath);

    virtual status_t dump(int fd, const Vector<String16>& args);

    int getLogLevel();

private:
    int permissionCheck();
    void setLogLevel(int level);
    void traceValue(const String16& type, const String16& key, const String16& value);
    int getProcName(pid_t pid, String16& procName);

    mutable Mutex mLock;

    int mLogLevel;

    SysWrite *pSysWrite;
    DisplayMode *pDisplayMode;
};

// ----------------------------------------------------------------------------

} // namespace android

#endif // ANDROID_SYSTEM_CONTROL_H
