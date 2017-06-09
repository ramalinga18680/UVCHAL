LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES:= \
	com_droidlogic_app_tv_TvControlManager.cpp
LOCAL_SHARED_LIBRARIES := \
	libcutils \
	libutils \
	libbinder \
	libtvbinder \
	libnativehelper \
	libandroid_runtime \
	liblog \
	libskia

LOCAL_C_INCLUDES += \
    frameworks/base/core/jni \
    frameworks/base/core/jni/android/graphics \
    frameworks/base/libs/hwui \
    vendor/amlogic/frameworks/libtvbinder/include \
    external/skia/include \

LOCAL_MODULE:= libtv_jni
LOCAL_PRELINK_MODULE := false
include $(BUILD_SHARED_LIBRARY)




#cfbc communication jni lib
#include $(CLEAR_VARS)
#LOCAL_MODULE_TAGS := optional

#LOCAL_C_INCLUDES += \
#    frameworks/base/core/jni \
#    $(LOCAL_PATH)/../../libtv \
#    external/skia/include \
#    $(LOCAL_PATH)/../../libtv/tv \
#    bionic/libc/include \
#    bionic/libc/private \
#    system/extras/ext4_utils \
#    bionic/libc/include

#LOCAL_SRC_FILES := \
#    ../../libtv/tv/CTvLog.cpp \
#    ../../libtv/tvutils/CFile.cpp \
#    ../../libtv/tvutils/CThread.cpp \
#    ../../libtv/tvutils/CMsgQueue.cpp \
#    ../../libtv/tvutils/zepoll.cpp \
#    ../../libtv/tv/CFbcCommunication.cpp \
#    ../../libtv/tvutils/serial_base.cpp \
#    ../../libtv/tvutils/CSerialPort.cpp \
#    ../../libtv/tvutils/CHdmiCecCmd.cpp \
#    cfbc_jni.cpp \
#    cfbc_test.cpp

#LOCAL_SHARED_LIBRARIES := \
#        libcutils \
#        libutils \
#        libandroid_runtime \
#        liblog \
#	libdl

#LOCAL_MODULE:= libcfbc_jni
#LOCAL_PRELINK_MODULE := false
#include $(BUILD_SHARED_LIBRARY)
