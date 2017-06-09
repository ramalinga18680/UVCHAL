LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_SRC_FILES:= fw_env_main.c fw_env.c
LOCAL_C_INCLUDES += external/zlib
LOCAL_MODULE := fw_printenv
LOCAL_MODULE_TAGS := optional
LOCAL_STATIC_LIBRARIES := libcutils libc libz
LOCAL_FORCE_STATIC_EXECUTABLE := true
include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)
LOCAL_SRC_FILES:= fw_env_main.c fw_env.c
LOCAL_C_INCLUDES += external/zlib
LOCAL_MODULE := fw_setenv
LOCAL_MODULE_TAGS := optional
LOCAL_STATIC_LIBRARIES := libcutils libc libz
LOCAL_FORCE_STATIC_EXECUTABLE := true
include $(BUILD_EXECUTABLE)
