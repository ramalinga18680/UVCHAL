#================================
#getbootenv
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

ifeq ($(TARGET_BOARD_PLATFORM), meson8)
LOCAL_CFLAGS += -DMESON8_ENVSIZE
endif

LOCAL_SRC_FILES:= \
	getbootenv.c \
	../ubootenv.c

LOCAL_MODULE:= getbootenv

LOCAL_STATIC_LIBRARIES := \
	libcutils \
	liblog \
	libz \
	libc

LOCAL_C_INCLUDES := \
    external/zlib

LOCAL_FORCE_STATIC_EXECUTABLE := true

LOCAL_MODULE_TAGS := optional

include $(BUILD_EXECUTABLE)


#================================
#setbootenv

include $(CLEAR_VARS)

ifeq ($(TARGET_BOARD_PLATFORM), meson8)
LOCAL_CFLAGS += -DMESON8_ENVSIZE
endif

LOCAL_SRC_FILES:= \
	setbootenv.c \
	../ubootenv.c

LOCAL_MODULE:= setbootenv

LOCAL_STATIC_LIBRARIES := \
	libcutils \
	liblog \
	libz \
	libc

LOCAL_C_INCLUDES := \
    external/zlib

LOCAL_FORCE_STATIC_EXECUTABLE := true

LOCAL_MODULE_TAGS := optional

include $(BUILD_EXECUTABLE)