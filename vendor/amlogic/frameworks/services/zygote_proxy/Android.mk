# Copyright 2005 The Android Open Source Project

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	zygote_proxy.c

LOCAL_MODULE := zygote_proxy

LOCAL_CFLAGS += -DUSE_KERNEL_LOG

LOCAL_SHARED_LIBRARIES := \
	libcutils \
	libc \
	libz \
	libselinux

include $(BUILD_EXECUTABLE)

