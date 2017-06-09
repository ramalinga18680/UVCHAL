LOCAL_PATH:= $(call my-dir)

# the library
# ============================================================
include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
  $(call all-subdir-java-files)

LOCAL_MODULE := droidlogic
LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_DX_FLAGS := --core-library

include $(BUILD_JAVA_LIBRARY)
