LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	systemwritetest.cpp

LOCAL_SHARED_LIBRARIES := \
	libcutils \
	libutils  \
	libbinder \
	libsystemwriteservice

LOCAL_MODULE:= test-systemwrite

LOCAL_MODULE_TAGS := optional

include $(BUILD_EXECUTABLE)
