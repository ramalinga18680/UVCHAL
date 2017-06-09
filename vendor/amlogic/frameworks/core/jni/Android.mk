LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
    droid_logic_server_HDMIIN.cpp \
    onload.cpp \
    HDMIIN/audio_utils_ctl.cpp \
    HDMIIN/mAlsa.cpp \
    HDMIIN/audiodsp_ctl.cpp \

LOCAL_C_INCLUDES += \
    $(JNI_H_INCLUDE) \
    frameworks/base/libs/hwui \
    frameworks/base/services \
    frameworks/base/core/jni \
    frameworks/native/services \
    external/skia/include/core \
    libcore/include \
    libcore/include/libsuspend \
	$(call include-path-for, libhardware)/hardware \
	$(call include-path-for, libhardware_legacy)/hardware_legacy

ifeq ($(strip $(BOARD_ALSA_AUDIO)),tiny)
    LOCAL_C_INCLUDES += external/tinyalsa/include
    LOCAL_CFLAGS += -DBOARD_ALSA_AUDIO_TINY
else
    LOCAL_C_INCLUDES += external/alsa-lib/include
endif

LOCAL_SHARED_LIBRARIES := \
    libbinder \
    libcutils \
    libutils \
    libgui \
    libandroid_runtime \
    liblog \
    libhardware \
    libhardware_legacy \
    libnativehelper \
    libmedia

LOCAL_CFLAGS += -Wno-unused-parameter
LOCAL_CFLAGS += -DEGL_EGLEXT_PROTOTYPES -DGL_GLEXT_PROTOTYPES

ifeq ($(strip $(BOARD_ALSA_AUDIO)),tiny)
    LOCAL_SHARED_LIBRARIES += libtinyalsa
else
    LOCAL_SHARED_LIBRARIES += libasound
endif

ifeq ($(WITH_MALLOC_LEAK_CHECK),true)
    LOCAL_CFLAGS += -DMALLOC_LEAK_CHECK
endif

LOCAL_MODULE:= libhdmiin

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
	droid_logic_DisplaySetting.cpp

LOCAL_C_INCLUDES := $(JNI_H_INCLUDE)

LOCAL_MODULE    := libdisplaysetting

LOCAL_SHARED_LIBRARIES := \
    liblog \
    libcutils \
    libgui \
    libnativehelper

include $(BUILD_SHARED_LIBRARY)

########### build libsurfaceoverlay_jni
include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
	droid_logic_SurfaceOverlay.cpp

LOCAL_C_INCLUDES := $(JNI_H_INCLUDE)

LOCAL_MODULE    := libsurfaceoverlay_jni

LOCAL_SHARED_LIBRARIES := \
    liblog \
    libcutils \
    libutils \
    libgui \
    libnativehelper \
    libandroid_runtime \
    libui

LOCAL_C_INCLUDES += \
    frameworks/base/include \
    frameworks/native/include \
    $(JNI_H_INCLUDE)

include $(BUILD_SHARED_LIBRARY)

########### build libgifdecode_jni
include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
	droid_logic_GIFDecode.cpp

LOCAL_C_INCLUDES := $(JNI_H_INCLUDE)

LOCAL_MODULE    := libgifdecode_jni

LOCAL_SHARED_LIBRARIES := \
    liblog \
    libcutils \
    libutils \
    libskia \
    libnativehelper \
    libandroid_runtime

LOCAL_C_INCLUDES += \
  external/giflib \
  external/skia/include/core \
  external/skia/include/effects \
  external/skia/include/images \
  external/skia/src/ports \
  external/skia/include/utils \
  frameworks/base/libs/hwui \
  frameworks/base/core/jni/android/graphics

LOCAL_STATIC_LIBRARIES := \
  libgif

include $(BUILD_SHARED_LIBRARY)

include $(LOCAL_PATH)/tv/Android.mk
