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
 *  @date     2014/10/23
 *  @par function description:
 *  - 1 set display mode
 */

#ifndef ANDROID_DISPLAY_MODE_H
#define ANDROID_DISPLAY_MODE_H

#include "SysWrite.h"
#include "common.h"

#include <map>
#include <cmath>
#include <string>
#include <pthread.h>
#include <linux/fb.h>
#include <semaphore.h>

#ifndef RECOVERY_MODE
#include "ISystemControlNotify.h"

using namespace android;
#endif

//#define USE_BEST_MODE

#define DEVICE_STR_MID                  "MID"
#define DEVICE_STR_MBOX                 "MBOX"
#define DEVICE_STR_TV                   "TV"

#define DESITY_720P                     "160"
#define DESITY_1080P                    "240"
#define DESITY_2160P                    "480"

#define DEFAULT_EDID_CRCHEAD            "checkvalue: "
#define DEFAULT_OUTPUT_MODE             "720p60hz"
#define DISPLAY_CFG_FILE                "/system/etc/mesondisplay.cfg"
#define DISPLAY_FB0                     "/dev/graphics/fb0"
#define DISPLAY_FB1                     "/dev/graphics/fb1"
#define SYSFS_DISPLAY_MODE              "/sys/class/display/mode"
#define SYSFS_DISPLAY_MODE2             "/sys/class/display2/mode"
//when close freescale, will enable display axis, cut framebuffer output
//when open freescale, will enable window axis, scale framebuffer output
#define SYSFS_DISPLAY_AXIS              "/sys/class/display/axis"
#define SYSFS_VIDEO_AXIS                "/sys/class/video/axis"
#define SYSFS_BOOT_TYPE                 "/sys/power/boot_type"
#define SYSFS_VIDEO_LAYER_STATE         "/sys/class/video/video_layer1_state"
#define DISPLAY_FB0_BLANK               "/sys/class/graphics/fb0/blank"
#define DISPLAY_FB1_BLANK               "/sys/class/graphics/fb1/blank"
#define DISPLAY_LOGO_INDEX              "/sys/module/fb/parameters/osd_logo_index"
#define SYS_DISABLE_VIDEO               "/sys/class/video/disable_video"

#define DISPLAY_FB0_FREESCALE           "/sys/class/graphics/fb0/free_scale"
#define DISPLAY_FB1_FREESCALE           "/sys/class/graphics/fb1/free_scale"
#define DISPLAY_FB0_FREESCALE_MODE      "/sys/class/graphics/fb0/freescale_mode"
#define DISPLAY_FB1_FREESCALE_MODE      "/sys/class/graphics/fb1/freescale_mode"
#define DISPLAY_FB0_SCALE_AXIS          "/sys/class/graphics/fb0/scale_axis"
#define DISPLAY_FB1_SCALE_AXIS          "/sys/class/graphics/fb1/scale_axis"
#define DISPLAY_FB1_SCALE               "/sys/class/graphics/fb1/scale"

#define DISPLAY_FB0_FREESCALE_AXIS      "/sys/class/graphics/fb0/free_scale_axis"
#define DISPLAY_FB0_WINDOW_AXIS         "/sys/class/graphics/fb0/window_axis"

#define DISPLAY_HDMI_HDCP_STOP          "stop14" //stop HDCP authenticate
#define DISPLAY_HDMI_HDCP_14            "1"
#define DISPLAY_HDMI_HDCP_22            "2"
#define DISPLAY_HDMI_HDCP_VER           "/sys/class/amhdmitx/amhdmitx0/hdcp_ver"//RX support HDCP version
#define DISPLAY_HDMI_HDCP_MODE          "/sys/class/amhdmitx/amhdmitx0/hdcp_mode"//set HDCP mode
#define DISPLAY_HDMI_HDCP_AUTH          "/sys/module/hdmitx20/parameters/hdmi_authenticated"//HDCP Authentication
#define DISPLAY_HDMI_HDCP_CONF          "/sys/class/amhdmitx/amhdmitx0/hdcp_ctrl" //HDCP config
#define DISPLAY_HDMI_HDCP_KEY           "/sys/class/amhdmitx/amhdmitx0/hdcp_lstore"//TX have 22 or 14 or none key

#define DISPLAY_HPD_STATE               "/sys/class/amhdmitx/amhdmitx0/hpd_state"
#define DISPLAY_HDMI_EDID               "/sys/class/amhdmitx/amhdmitx0/disp_cap"//RX support display mode
#define DISPLAY_HDMI_AVMUTE             "/sys/devices/virtual/amhdmitx/amhdmitx0/avmute"
#define DISPLAY_EDID_VALUE              "/sys/class/amhdmitx/amhdmitx0/edid"
#define DISPLAY_HDMI_PHY                "/sys/class/amhdmitx/amhdmitx0/phy"

#define AUDIO_DSP_DIGITAL_RAW           "/sys/class/audiodsp/digital_raw"
#define AV_HDMI_CONFIG                  "/sys/class/amhdmitx/amhdmitx0/config"
#define AV_HDMI_3D_SUPPORT              "/sys/class/amhdmitx/amhdmitx0/support_3d"

#define HDMI_UEVENT                     "DEVPATH=/devices/virtual/switch/hdmi"
#define HDMI_POWER_UEVENT               "DEVPATH=/devices/virtual/switch/hdmi_power"

//HDCP RX
#define HDMI_RX_PLUG_IN                 "2"
#define HDMI_RX_PLUG_OUT                "0"
//if state = 2 means ok, if state = 0 plug out
#define HDMI_RX_PLUG_UEVENT             "DEVPATH=/devices/virtual/switch/hdmirx/state"
//1:plugin 0:plug out
#define HDMI_RX_HPD_STATE               "/sys/module/tvin_hdmirx/parameters/hpd_to_esm"

#define VIDEO_LAYER1_UEVENT             "DEVPATH=/devices/virtual/switch/video_layer1"

#define PROP_HDMIONLY                   "ro.platform.hdmionly"
#define PROP_LCD_DENSITY                "ro.sf.lcd_density"
#define PROP_WINDOW_WIDTH               "const.window.w"
#define PROP_WINDOW_HEIGHT              "const.window.h"
#define PROP_HAS_CVBS_MODE              "ro.platform.has.cvbsmode"
#define PROP_BEST_OUTPUT_MODE           "ro.platform.best_outputmode"
#define PROP_BOOTANIM                   "init.svc.bootanim"
#define PROP_FS_MODE                    "const.filesystem.mode"
#define PROP_BOOTANIM_DELAY             "const.bootanim.delay"
#define PROP_BOOTVIDEO_SERVICE          "service.bootvideo"


#define ENV_480I_X                      "ubootenv.var.480i_x"
#define ENV_480I_Y                      "ubootenv.var.480i_y"
#define ENV_480I_W                      "ubootenv.var.480i_w"
#define ENV_480I_H                      "ubootenv.var.480i_h"
#define ENV_480P_X                      "ubootenv.var.480p_x"
#define ENV_480P_Y                      "ubootenv.var.480p_y"
#define ENV_480P_W                      "ubootenv.var.480p_w"
#define ENV_480P_H                      "ubootenv.var.480p_h"
#define ENV_576I_X                      "ubootenv.var.576i_x"
#define ENV_576I_Y                      "ubootenv.var.576i_y"
#define ENV_576I_W                      "ubootenv.var.576i_w"
#define ENV_576I_H                      "ubootenv.var.576i_h"
#define ENV_576P_X                      "ubootenv.var.576p_x"
#define ENV_576P_Y                      "ubootenv.var.576p_y"
#define ENV_576P_W                      "ubootenv.var.576p_w"
#define ENV_576P_H                      "ubootenv.var.576p_h"
#define ENV_720P_X                      "ubootenv.var.720p_x"
#define ENV_720P_Y                      "ubootenv.var.720p_y"
#define ENV_720P_W                      "ubootenv.var.720p_w"
#define ENV_720P_H                      "ubootenv.var.720p_h"
#define ENV_1080I_X                     "ubootenv.var.1080i_x"
#define ENV_1080I_Y                     "ubootenv.var.1080i_y"
#define ENV_1080I_W                     "ubootenv.var.1080i_w"
#define ENV_1080I_H                     "ubootenv.var.1080i_h"
#define ENV_1080P_X                     "ubootenv.var.1080p_x"
#define ENV_1080P_Y                     "ubootenv.var.1080p_y"
#define ENV_1080P_W                     "ubootenv.var.1080p_w"
#define ENV_1080P_H                     "ubootenv.var.1080p_h"
#define ENV_4K2K24HZ_X                  "ubootenv.var.4k2k24hz_x"
#define ENV_4K2K24HZ_Y                  "ubootenv.var.4k2k24hz_y"
#define ENV_4K2K24HZ_W                  "ubootenv.var.4k2k24hz_w"
#define ENV_4K2K24HZ_H                  "ubootenv.var.4k2k24hz_h"
#define ENV_4K2K25HZ_X                  "ubootenv.var.4k2k25hz_x"
#define ENV_4K2K25HZ_Y                  "ubootenv.var.4k2k25hz_y"
#define ENV_4K2K25HZ_W                  "ubootenv.var.4k2k25hz_w"
#define ENV_4K2K25HZ_H                  "ubootenv.var.4k2k25hz_h"
#define ENV_4K2K30HZ_X                  "ubootenv.var.4k2k30hz_x"
#define ENV_4K2K30HZ_Y                  "ubootenv.var.4k2k30hz_y"
#define ENV_4K2K30HZ_W                  "ubootenv.var.4k2k30hz_w"
#define ENV_4K2K30HZ_H                  "ubootenv.var.4k2k30hz_h"
#define ENV_4K2K50HZ_X                  "ubootenv.var.4k2k50hz_x"
#define ENV_4K2K50HZ_Y                  "ubootenv.var.4k2k50hz_y"
#define ENV_4K2K50HZ_W                  "ubootenv.var.4k2k50hz_w"
#define ENV_4K2K50HZ_H                  "ubootenv.var.4k2k50hz_h"
#define ENV_4K2K60HZ_X                  "ubootenv.var.4k2k60hz_x"
#define ENV_4K2K60HZ_Y                  "ubootenv.var.4k2k60hz_y"
#define ENV_4K2K60HZ_W                  "ubootenv.var.4k2k60hz_w"
#define ENV_4K2K60HZ_H                  "ubootenv.var.4k2k60hz_h"
#define ENV_4K2KSMPTE_X                 "ubootenv.var.4k2ksmpte_x"
#define ENV_4K2KSMPTE_Y                 "ubootenv.var.4k2ksmpte_y"
#define ENV_4K2KSMPTE_W                 "ubootenv.var.4k2ksmpte_w"
#define ENV_4K2KSMPTE_H                 "ubootenv.var.4k2ksmpte_h"

#define UBOOTENV_DIGITAUDIO             "ubootenv.var.digitaudiooutput"
#define UBOOTENV_HDMIMODE               "ubootenv.var.hdmimode"
#define UBOOTENV_CVBSMODE               "ubootenv.var.cvbsmode"
#define UBOOTENV_OUTPUTMODE             "ubootenv.var.outputmode"
#define UBOOTENV_ISBESTMODE             "ubootenv.var.is.bestmode"
#define UBOOTENV_EDIDCRCVALUE           "ubootenv.var.edid.crcvalue"

#define UBOOTENV_OVERSCAN_LEFT          "ubootenv.var.overscan_left"
#define UBOOTENV_OVERSCAN_TOP           "ubootenv.var.overscan_top"
#define UBOOTENV_OVERSCAN_RIGHT         "ubootenv.var.overscan_right"
#define UBOOTENV_OVERSCAN_BOTTOM        "ubootenv.var.overscan_bottom"
#define UBOOTENV_CUSTOMWIDTH            "ubootenv.var.customwidth"
#define UBOOTENV_CUSTOMHEIGHT           "ubootenv.var.customheight"

#define FULL_WIDTH_480                  720
#define FULL_HEIGHT_480                 480
#define FULL_WIDTH_576                  720
#define FULL_HEIGHT_576                 576
#define FULL_WIDTH_720                  1280
#define FULL_HEIGHT_720                 720
#define FULL_WIDTH_1080                 1920
#define FULL_HEIGHT_1080                1080
#define FULL_WIDTH_4K2K                 3840
#define FULL_HEIGHT_4K2K                2160
#define FULL_WIDTH_4K2KSMPTE            4096
#define FULL_HEIGHT_4K2KSMPTE           2160

//should sync with SurfaceFlinger.h
#define SURFACE_3D_OFF                  0
#define SURFACE_3D_SIDE_BY_SIDE         8
#define SURFACE_3D_TOP_BOTTOM           16

#define VIDEO_3D_OFF                    "3doff"
#define VIDEO_3D_SIDE_BY_SIDE           "3dlr"
#define VIDEO_3D_TOP_BOTTOM             "3dtb"

enum {
    VIDEO_3D_MODE_OFF                   = 0,
    VIDEO_3D_MODE_SIDE_BY_SIDE          = 1,
    VIDEO_3D_MODE_TOP_BOTTOM            = 2,
    VIDEO_3D_MODE_TOTAL                 = 3
};

enum {
    EVENT_OUTPUT_MODE_CHANGE            = 0,
    EVENT_DIGITAL_MODE_CHANGE           = 1,
};

enum {
    DISPLAY_TYPE_NONE                   = 0,
    DISPLAY_TYPE_TABLET                 = 1,
    DISPLAY_TYPE_MBOX                   = 2,
    DISPLAY_TYPE_TV                     = 3
};

#define MODE_480I                       "480i60hz"
#define MODE_480P                       "480p60hz"
#define MODE_480CVBS                    "480cvbs"
#define MODE_576I                       "576i50hz"
#define MODE_576P                       "576p50hz"
#define MODE_576CVBS                    "576cvbs"
#define MODE_720P50HZ                   "720p50hz"
#define MODE_720P                       "720p60hz"
#define MODE_1080P24HZ                  "1080p24hz"
#define MODE_1080I50HZ                  "1080i50hz"
#define MODE_1080P50HZ                  "1080p50hz"
#define MODE_1080I                      "1080i60hz"
#define MODE_1080P                      "1080p60hz"
#define MODE_4K2K24HZ                   "2160p24hz"
#define MODE_4K2K25HZ                   "2160p25hz"
#define MODE_4K2K30HZ                   "2160p30hz"
#define MODE_4K2K50HZ                   "2160p50hz"
#define MODE_4K2K50HZ420                "2160p50hz420"
#define MODE_4K2K60HZ                   "2160p60hz"
#define MODE_4K2K60HZ420                "2160p60hz420"
#define MODE_4K2KSMPTE                  "smpte24hz"
#define MODE_640X480P60HZ               "640x480p60hz"
#define MODE_800X600P60HZ               "800x600p60hz"
#define MODE_800X480P60HZ               "800x480p60hz"
#define MODE_1024X600P60HZ              "1024x600p60hz"
#define MODE_1024X768P60HZ              "1024x768p60hz"
#define MODE_1280X800P60HZ              "1280x800p60hz"
#define MODE_1280X1024P60HZ             "1280x1024p60hz"
#define MODE_1360X768P60HZ              "1360x768p60hz"
#define MODE_1366X768P60HZ              "1366x768p60hz"
#define MODE_1440X900P60HZ              "1440x900p60hz"
#define MODE_1600X900P60HZ              "1600x900p60hz"
#define MODE_1600X1200P60HZ             "1600x1200p60hz"
#define MODE_1680X1050P60HZ             "1680x1050p60hz"
#define MODE_1920X1200P60HZ             "1920x1200p60hz"
#define MODE_2560X1440P60HZ             "2560x1440p60hz"
#define MODE_2560X1600P60HZ             "2560x1600p60hz"
#define MODE_2560X1080P60HZ             "2560x1080p60hz"
#define MODE_3440X1440P60HZ             "3440x1440p60hz"
#define MODE_CUSTOMBUILT                "custombuilt"

enum {
    DISPLAY_MODE_480I                   = 0,
    DISPLAY_MODE_480P                   = 1,
    DISPLAY_MODE_480CVBS                = 2,
    DISPLAY_MODE_576I                   = 3,
    DISPLAY_MODE_576P                   = 4,
    DISPLAY_MODE_576CVBS                = 5,
    DISPLAY_MODE_720P50HZ               = 6,
    DISPLAY_MODE_720P                   = 7,
    DISPLAY_MODE_1080P24HZ              = 8,
    DISPLAY_MODE_1080I50HZ              = 9,
    DISPLAY_MODE_1080P50HZ              = 10,
    DISPLAY_MODE_1080I                  = 11,
    DISPLAY_MODE_1080P                  = 12,
    DISPLAY_MODE_4K2K24HZ               = 13,
    DISPLAY_MODE_4K2K25HZ               = 14,
    DISPLAY_MODE_4K2K30HZ               = 15,
    DISPLAY_MODE_4K2K50HZ               = 16,
    DISPLAY_MODE_4K2K50HZ420            = 17,
    DISPLAY_MODE_4K2K60HZ               = 18,
    DISPLAY_MODE_4K2K60HZ420            = 19,
    DISPLAY_MODE_4K2KSMPTE              = 20,
    DISPLAY_MODE_640X480P60HZ           = 21,
    DISPLAY_MODE_800X600P60HZ           = 22,
    DISPLAY_MODE_800X480P60HZ           = 23,
    DISPLAY_MODE_1024X600P60HZ          = 24,
    DISPLAY_MODE_1024X768P60HZ          = 25,
    DISPLAY_MODE_1280X800P60HZ          = 26,
    DISPLAY_MODE_1280X1024P60HZ         = 27,
    DISPLAY_MODE_1360X768P60HZ          = 28,
    DISPLAY_MODE_1366X768P60HZ          = 29,
    DISPLAY_MODE_1440X900P60HZ          = 30,
    DISPLAY_MODE_1600X900P60HZ          = 31,
    DISPLAY_MODE_1600X1200P60HZ         = 32,
    DISPLAY_MODE_1680X1050P60HZ         = 33,
    DISPLAY_MODE_1920X1200P60HZ         = 34,
    DISPLAY_MODE_2560X1440P60HZ         = 35,
    DISPLAY_MODE_2560X1600P60HZ         = 36,
    DISPLAY_MODE_2560X1080P60HZ         = 37,
    DISPLAY_MODE_3440X1440P60HZ         = 38,
    DISPLAY_MODE_CUSTOMBUILT            = 39,
    DISPLAY_MODE_TOTAL                  = 40
};

typedef enum {
    OUPUT_MODE_STATE_INIT               = 0,
    OUPUT_MODE_STATE_POWER              = 1,
    OUPUT_MODE_STATE_SWITCH             = 2,
    OUPUT_MODE_STATE_RESERVE            = 3
}output_mode_state;

typedef struct hdmi_data {
    char edid[MAX_STR_LEN];
    char hpd_state[10];//"0" or "1", hdmi pluged or not
    char current_mode[MODE_LEN];
    char ubootenv_hdmimode[MODE_LEN];
    char custom_width[5];
    char custom_height[5];
}hdmi_data_t;

typedef struct overscan_data {
    char left[5];
    char top[5];
    char right[5];
    char bottom[5];
}overscan_data_t;

typedef struct axis_s {
    int x;
    int y;
    int w;
    int h;
} axis_t;

typedef struct uevent_data {
    int len;
    char buf[1024];
    char name[128];
    char state[128];
} uevent_data_t;

// ----------------------------------------------------------------------------

class DisplayMode
{
public:
    DisplayMode(const char *path);
    ~DisplayMode();

    void init();
    void reInit();

    void getDisplayInfo(int &type, char* socType, char* defaultUI);
    void getFbInfo(int &fb0w, int &fb0h, int &fb0bits, int &fb0trip,
        int &fb1w, int &fb1h, int &fb1bits, int &fb1trip);

    void setLogLevel(int level);
    int dump(char *result);
    void setMboxOutputMode(const char* outputmode);
    int set3DMode(const char* mode3d);
    void setDigitalMode(const char* mode);
    void setOsdMouse(const char* curMode);
    void setOsdMouse(int x, int y, int w, int h);
    void setOverscan(const char* curMode);
    void setPosition(int left, int top, int width, int height);
    void getPosition(const char* curMode, int *position);
    static void* bootanimDetect(void *data);

    void setMboxDisplay(char* hpdstate, output_mode_state state);

    void setNativeWindowRect(int x, int y, int w, int h);
    void setVideoPlayingAxis();

    void hdcpSwitch();

#ifndef RECOVERY_MODE
    void notifyEvent(int event);
    void setListener(const sp<ISystemControlNotify>& listener);
#endif
private:

    bool getBootEnv(const char* key, char* value);
    void setBootEnv(const char* key, char* value);

    int parseConfigFile();
    void setTabletDisplay();
    void getBestHdmiMode(char * mode, hdmi_data_t* data);
    void getHighestHdmiMode(char* mode, hdmi_data_t* data);
    void filterHdmiMode(char * mode, hdmi_data_t* data);
    void getHdmiOutputMode(char *mode, hdmi_data_t* data);
    bool isEdidChange();
    bool isBestOutputmode();
    void initHdmiData(hdmi_data_t* data, char* hpdstate);
    void setMboxOutputMode(const char* outputmode, output_mode_state state);
    void setTVOutputMode(const char* outputmode, bool initState);
    int modeToIndex(const char *mode);
    void startHdmiPlugDetectThread();
    void startBootanimDetectThread();
    void setTVDisplay(bool initState);
    void fbset(int width, int height, int bits);
    void setFbParameter(const char* fbdev, struct fb_var_screeninfo var_set);

    int getBootenvInt(const char* key, int defaultVal);
    static bool hdcpInit(SysWrite *pSysWrite, bool *pHdcp22, bool *pHdcp14);
    static void hdcpAuthenticate(DisplayMode *disMode, SysWrite *pSysWrite, bool useHdcp22, bool useHdcp14);
    static void* hdcpThreadLoop(void* data);
    static void* hdcpRxThreadLoop(void* data);
    int hdcpThreadStart();
    int hdcpThreadExit(pthread_t thread_id);
    void hdcpRxAuthenticate(bool plugIn);

    int modeToIndex3D(const char *mode3d);
    void mode3DImpl();

    const char* pConfigPath;
    int mDisplayType;
    int mFb0Width;
    int mFb0Height;
    int mFb0FbBits;
    bool mFb0TripleEnable;//Triple Buffer enable or not

    int mFb1Width;
    int mFb1Height;
    int mFb1FbBits;
    bool mFb1TripleEnable;//Triple Buffer enable or not
    bool mVideoPlaying;
    int mNativeWinX;
    int mNativeWinY;
    int mNativeWinW;
    int mNativeWinH;

    int mDisplayWidth;
    int mDisplayHeight;

    char mSocType[MAX_STR_LEN];
    char mDefaultUI[MAX_STR_LEN];//this used for mbox
    int mLogLevel;
    SysWrite *pSysWrite = NULL;
    char mMode3d[32];//this used for video 3d set
    bool m3dModeSet;

    pthread_mutex_t pthreadMutex;
    sem_t pthreadSem;
    pthread_t pthreadIdHdcp;
    bool mExitHdcpThread;

#ifndef RECOVERY_MODE
    sp<ISystemControlNotify> mNotifyListener;
#endif
};

#endif // ANDROID_DISPLAY_MODE_H
