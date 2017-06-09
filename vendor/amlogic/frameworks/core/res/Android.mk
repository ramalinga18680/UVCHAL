
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_JAVA_LIBRARIES := droidlogic droidlogic.external.pppoe
#LOCAL_SDK_VERSION := current

LOCAL_PACKAGE_NAME := droidlogic-res
LOCAL_CERTIFICATE := platform

LOCAL_MODULE_TAGS := optional

# Install thie to system/priv-app
LOCAL_PRIVILEGED_MODULE := true
LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)


