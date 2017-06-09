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
 *  @version  2.0
 *  @date     2014/09/09
 *  @par function description:
 *  - 1 write property or sysfs in daemon
 */

#define LOG_TAG "SystemControl"
//#define LOG_NDEBUG 0
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <errno.h>
#include <cutils/properties.h>
#include <stdint.h>
#include <sys/types.h>
#include <SysWrite.h>
#include <common.h>

SysWrite::SysWrite()
    :mLogLevel(LOG_LEVEL_DEFAULT){
}

SysWrite::~SysWrite() {
}

bool SysWrite::getProperty(const char *key, char *value){
    property_get(key, value, "");
    /*
    char buf[PROPERTY_VALUE_MAX] = {0};
    property_get(key, buf, "");
    value.setTo(String16(buf));
    */
    return true;
}

bool SysWrite::getPropertyString(const char *key, char *value,  const char *def){
    property_get(key, value, def);
    return true;
}

int32_t SysWrite::getPropertyInt(const char *key, int32_t def){
    int len;
    char* end;
    char buf[PROPERTY_VALUE_MAX] = {0};
    int32_t result = def;

    len = property_get(key, buf, "");
    if (len > 0) {
        result = strtol(buf, &end, 0);
        if (end == buf) {
            result = def;
        }
    }

    return result;
}

int64_t SysWrite::getPropertyLong(const char *key, int64_t def){

    int len;
    char buf[PROPERTY_VALUE_MAX] = {0};
    char* end;
    int64_t result = def;

    len = property_get(key, buf, "");
    if (len > 0) {
        result = strtoll(buf, &end, 0);
        if (end == buf) {
            result = def;
        }
    }

    return result;
}

bool SysWrite::getPropertyBoolean(const char *key, bool def){

    int len;
    char buf[PROPERTY_VALUE_MAX] = {0};
    bool result = def;

    len = property_get(key, buf, "");
    if (len == 1) {
        char ch = buf[0];
        if (ch == '0' || ch == 'n')
            result = false;
        else if (ch == '1' || ch == 'y')
            result = true;
    } else if (len > 1) {
         if (!strcmp(buf, "no") || !strcmp(buf, "false") || !strcmp(buf, "off")) {
            result = false;
        } else if (!strcmp(buf, "yes") || !strcmp(buf, "true") || !strcmp(buf, "on")) {
            result = true;
        }
    }

    return result;
}

void SysWrite::setProperty(const char *key, const char *value){
    int err;
    err = property_set(key, value);
    if (err < 0) {
        SYS_LOGE("failed to set system property \n");
    }
}

bool SysWrite::readSysfs(const char *path, char *value){
    char buf[MAX_STR_LEN+1] = {0};
    readSys(path, (char*)buf, MAX_STR_LEN, false);
    strcpy(value, buf);
    return true;
}

// get the original data from sysfs without any change.
bool SysWrite::readSysfsOriginal(const char *path, char *value){
    char buf[MAX_STR_LEN+1] = {0};
    readSys(path, (char*)buf, MAX_STR_LEN, true);
    strcpy(value, buf);
    return true;
}

bool SysWrite::writeSysfs(const char *path, const char *value){
    writeSys(path, value);
    return true;
}

void SysWrite::setLogLevel(int level){
    mLogLevel = level;
}

void SysWrite::writeSys(const char *path, const char *val){
    int fd;

    if((fd = open(path, O_RDWR)) < 0) {
        SYS_LOGE("writeSysFs, open %s fail.", path);
        goto exit;
    }

    if(mLogLevel > LOG_LEVEL_1)
        SYS_LOGI("write %s, val:%s\n", path, val);

    write(fd, val, strlen(val));

exit:
    close(fd);
}

void SysWrite::readSys(const char *path, char *buf, int count, bool needOriginalData){
    int fd, len;
    char *pos;

    if ( NULL == buf ) {
        SYS_LOGE("buf is NULL");
        return;
    }

    if ((fd = open(path, O_RDONLY)) < 0) {
        SYS_LOGE("readSysFs, open %s fail.", path);
        goto exit;
    }

    len = read(fd, buf, count);
    if (len < 0) {
        SYS_LOGE("read error: %s, %s\n", path, strerror(errno));
        goto exit;
    }

    if (!needOriginalData) {
        int i , j;
        for (i = 0, j = 0; i <= len -1; i++) {
            /*change '\0' to 0x20(spacing), otherwise the string buffer will be cut off
             * if the last char is '\0' should not replace it
             */
            if (0x0 == buf[i] && i < len - 1) {
                buf[i] = 0x20;

                if (mLogLevel > LOG_LEVEL_1)
                    SYS_LOGI("read buffer index:%d is a 0x0, replace to spacing \n", i);
            }

            /* delete all the character of '\n' */
            if (0x0a != buf[i]) {
                buf[j++] = buf[i];
            }
        }

        buf[j] = 0x0;
    }

    if (mLogLevel > LOG_LEVEL_1)
        SYS_LOGI("read %s, result length:%d, val:%s\n", path, len, buf);

exit:
    close(fd);
}

#if 0
status_t SysWrite::dump(int fd, const Vector<String16>& args){
    const size_t SIZE = 256;
    char buffer[SIZE];
    String8 result;
    if (checkCallingPermission(String16("android.permission.DUMP")) == false) {
        snprintf(buffer, SIZE, "Permission Denial: "
                "can't dump sys.write from pid=%d, uid=%d\n",
                IPCThreadState::self()->getCallingPid(),
                IPCThreadState::self()->getCallingUid());
        result.append(buffer);
    } else {
        Mutex::Autolock lock(mLock);

        result.appendFormat("sys write service wrote by multi-user mode, normal process will have not system privilege\n");
        /*
        int n = args.size();
        for (int i = 0; i + 1 < n; i++) {
            String16 verboseOption("-v");
            if (args[i] == verboseOption) {
                String8 levelStr(args[i+1]);
                int level = atoi(levelStr.string());
                result = String8::format("\nSetting log level to %d.\n", level);
                setLogLevel(level);
                write(fd, result.string(), result.size());
            }
        }*/
    }
    write(fd, result.string(), result.size());
    return NO_ERROR;
}
#endif
