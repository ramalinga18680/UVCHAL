/*
 * Copyright (C) 2010 The Android Open Source Project
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
 */

#define LOG_TAG "SystemWriteTest"

#include <../ISystemControlService.h>

#include <binder/Binder.h>
#include <binder/IServiceManager.h>
#include <utils/Atomic.h>
#include <utils/Log.h>
#include <utils/RefBase.h>
#include <utils/String8.h>
#include <utils/String16.h>

using namespace android;

class DeathNotifier: public IBinder::DeathRecipient
{
public:
    DeathNotifier() {
    }

    void binderDied(const wp<IBinder>& who) {
	    ALOGW("system_write died!");
	}
};

int main(int argc, char** argv)
{
    sp<IServiceManager> sm = defaultServiceManager();
    if (sm == NULL) {
        ALOGE("Couldn't get default ServiceManager\n");
        return false;
    }

#if 1
    sp<ISystemControlService> sysWrite = interface_cast<ISystemControlService>(sm->getService(String16("system_control")));
    if (sysWrite == NULL) {
        ALOGE("Couldn't get connection to sys write service\n");
        return -1;
    }
#endif

#if 0
	sp<DeathNotifier> mDeathNotifier;
	sp<IBinder> binder;
	binder = sm->getService(String16("system_write"));
	if (mDeathNotifier == NULL) {
        mDeathNotifier = new DeathNotifier();
    }
	binder->linkToDeath(mDeathNotifier);
	sp<ISystemControlService> mSystemWriteService = interface_cast<ISystemControlService>(binder);
	if (mSystemWriteService == NULL) {
        ALOGE("Couldn't get connection to SystemWriteService\n");
        return -1;
    }
#endif

    String16 value;
	sysWrite->getProperty(String16("ro.ui.cursor"), value);
	ALOGI("getProperty ro.ui.cursor: %s\n", String8(value).string());

	String16 test("test");
	sysWrite->getPropertyString(String16("ro.ui.cursor"), value, test);
	ALOGI("getPropertyString ro.ui.cursor: %s\n", String8(value).string());

	int32_t int_value = sysWrite->getPropertyInt(String16("ro.usbstorage.maxfreq"), 1212);
	ALOGI("getPropertyInt ro.usbstorage.maxfreq : %d\n", int_value);

	int64_t long_value = sysWrite->getPropertyLong(String16("ro.usbstorage.maxfreq"), 212121);
	ALOGI("getPropertyLong ro.usbstorage.maxfreq : %ld\n", (long int)long_value);

	bool bool_value = sysWrite->getPropertyBoolean(String16("ro.statusbar.button"), false);
	ALOGI("getPropertyBoolean ro.statusbar.button: %d\n", bool_value);

	sysWrite->setProperty(String16("vplayer.hideStatusBar.enable"), String16("true"));
	ALOGI("setProperty vplayer.hideStatusBar.enable true\n");

	String16 read_value;
	sysWrite->readSysfs(String16("/sys/power/state"), read_value);
	ALOGI("readSysfs /sys/power/state :%s\n", String8(read_value).string());

	String16 bool_true("1");
	sysWrite->writeSysfs(String16("/sys/class/graphics/fb0/blank"), bool_true);
	ALOGI("writeSysfs /sys/class/graphics/fb0/blank:1\n");

    sysWrite->getBootEnv(String16("ubootenv.var.cvbsmode"), value);
    ALOGI("getBootEnv ubootenv.var.cvbsmode: %s\n", String8(value).string());

    sysWrite->setBootEnv(String16("ubootenv.var.cvbsmode"), String16("576cvbs"));
    ALOGI("getBootEnv ubootenv.var.cvbsmode: 576cvbs\n");
    return 0;
}
