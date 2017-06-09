LOCAL_PATH:= $(call my-dir)

PPPOE_VERSION="\"3.0\""

#MAKE_JAR
include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_JNI_SHARED_LIBRARIES := libpppoejni
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := droidlogic.external.pppoe
include $(BUILD_JAVA_LIBRARY)

#MAKE_XML
include $(CLEAR_VARS)
LOCAL_MODULE := droidlogic.external.pppoe.xml
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := ETC
LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/permissions
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)


include $(CLEAR_VARS)
LOCAL_SRC_FILES := jni/src/pppoe.c \
                   jni/src/if.c \
                   jni/src/debug.c \
                   jni/src/common.c \
                   jni/src/ppp.c \
                   jni/src/discovery.c \
                   jni/src/netwrapper.c
#LOCAL_C_INCLUDES := $(KERNEL_HEADERS)
LOCAL_SHARED_LIBRARIES := libcutils
LOCAL_MODULE = pppoe
LOCAL_MODULE_TAGS := optional
LOCAL_CFLAGS := -DVERSION=$(PPPOE_VERSION)
include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)
LOCAL_SRC_FILES:= jni/src/pppoe_status.c \
        jni/pppoe_jni.cpp \
        jni/src/netwrapper.c


LOCAL_SHARED_LIBRARIES := \
        libcutils \
        liblog \
        libselinux


LOCAL_SHARED_LIBRARIES += libandroid_runtime   libnativehelper
LOCAL_SHARED_LIBRARIES += libc libcutils libnetutils
LOCAL_C_INCLUDES :=  $(JNI_H_INCLUDE) $(LOCAL_PATH)/jni/src
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := libpppoejni
LOCAL_PRELINK_MODULE := false
include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_SRC_FILES:= jni/src/pppoe_cli.c \
        jni/src/common.c \
        jni/src/netwrapper.c

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := pcli
LOCAL_SHARED_LIBRARIES := libcutils libnetutils libselinux
include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)
LOCAL_SRC_FILES:= jni/src/pppoe_wrapper.c \
        jni/src/common.c \
        jni/src/netwrapper.c

LOCAL_SHARED_LIBRARIES := \
        libcutils libcrypto libnetutils libselinux

LOCAL_C_INCLUDES := \
        $(LOCAL_PATH)/include

LOCAL_CFLAGS := -DANDROID_CHANGES

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE:= pppoe_wrapper

include $(BUILD_EXECUTABLE)

