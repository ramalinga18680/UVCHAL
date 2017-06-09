# Copyright (C) 2011 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_RELATIVE_PATH := hw
LOCAL_CFLAGS += -fno-short-enums -DQEMU_HARDWARE
LOCAL_CFLAGS += -Wno-unused-parameter -Wno-missing-field-initializers

########################################################################################################
CAMHAL_GIT_VERSION="$(shell cd $(LOCAL_PATH);git log | grep commit -m 1 | cut -d' ' -f 2)"
CAMHAL_GIT_UNCOMMIT_FILE_NUM=$(shell cd $(LOCAL_PATH);git diff | grep +++ -c)
CAMHAL_LAST_CHANGED="$(shell cd $(LOCAL_PATH);git log | grep Date -m 1)"
CAMHAL_BUILD_TIME=" $(shell date)"
CAMHAL_BUILD_NAME=" $(shell echo ${LOGNAME})"
CAMHAL_BRANCH_NAME="$(shell cd $(LOCAL_PATH);git branch -a | sed -n '/'*'/p')"
CAMHAL_BUILD_MODE=$(shell echo ${TARGET_BUILD_VARIANT})
CAMHAL_HOSTNAME="$(shell hostname)"
CAMHAL_IP="$(shell ifconfig eth0|grep -oE '([0-9]{1,3}\.?){4}'|head -n 1)"
CAMHAL_PATH="$(shell pwd)/$(LOCAL_PATH)"

LOCAL_CFLAGS+=-DHAVE_VERSION_INFO
LOCAL_CFLAGS+=-DCAMHAL_GIT_VERSION=\"${CAMHAL_GIT_VERSION}${CAMHAL_GIT_DIRTY}\"
LOCAL_CFLAGS+=-DCAMHAL_BRANCH_NAME=\"${CAMHAL_BRANCH_NAME}\"
LOCAL_CFLAGS+=-DCAMHAL_LAST_CHANGED=\"${CAMHAL_LAST_CHANGED}\"
LOCAL_CFLAGS+=-DCAMHAL_BUILD_TIME=\"${CAMHAL_BUILD_TIME}\"
LOCAL_CFLAGS+=-DCAMHAL_BUILD_NAME=\"${CAMHAL_BUILD_NAME}\"
LOCAL_CFLAGS+=-DCAMHAL_GIT_UNCOMMIT_FILE_NUM=${CAMHAL_GIT_UNCOMMIT_FILE_NUM}
LOCAL_CFLAGS+=-DCAMHAL_HOSTNAME=\"${CAMHAL_HOSTNAME}\"
LOCAL_CFLAGS+=-DCAMHAL_IP=\"${CAMHAL_IP}\"
LOCAL_CFLAGS+=-DCAMHAL_PATH=\"${CAMHAL_PATH}\"
########################################################################################################

LOCAL_SHARED_LIBRARIES:= \
    libbinder \
    liblog \
    libutils \
    libcutils \
    libcamera_client \
    libion \
    libui \
    libdl \
    libjpeg \
    libjhead \
    libexpat

# JPEG conversion libraries and includes.
LOCAL_SHARED_LIBRARIES += \
    libjpeg \
    libcamera_metadata

LOCAL_STATIC_LIBRARIES := \
                         libyuv_static \

ifeq ($(GPU_TYPE),t83x)
	LOCAL_CFLAGS += -DMALI_AFBC_GRALLOC=1
else
	LOCAL_CFLAGS += -DMALI_AFBC_GRALLOC=0
endif

MESON_GRALLOC_DIR ?= hardware/amlogic/gralloc

LOCAL_C_INCLUDES += external/jpeg \
                    external/jhead/ \
                    frameworks/native/include/media/hardware \
                    external/libyuv/files/include/ \
                    $(TOP)/system/core/libion/include \
                    $(TOP)/system/core/libion/kernel-headers \
                    $(TOP)/$(MESON_GRALLOC_DIR) \
                    $(LOCAL_PATH)/inc \
                    $(call include-path-for, camera) \
                    $(TOP)/external/expat/lib \

LOCAL_SRC_FILES := \
    EmulatedCameraHal.cpp \
    EmulatedCameraFactory.cpp \
    EmulatedCameraHotplugThread.cpp \
    EmulatedBaseCamera.cpp \
    EmulatedCamera.cpp \
        EmulatedCameraDevice.cpp \
        EmulatedQemuCamera.cpp \
        EmulatedQemuCameraDevice.cpp \
        EmulatedFakeCamera.cpp \
        EmulatedFakeCameraDevice.cpp \
        Converters.cpp \
        PreviewWindow.cpp \
        CallbackNotifier.cpp \
        QemuClient.cpp \
        JpegCompressor.cpp \
        fake-pipeline2/Scene.cpp \
        fake-pipeline2/Sensor.cpp \
        fake-pipeline2/ge2d_stream.cpp \
        fake-pipeline2/JpegCompressor.cpp \
        fake-pipeline2/NV12_resize.c \
        fake-pipeline2/util.c \
    EmulatedCamera3.cpp \
        EmulatedFakeCamera3.cpp \
        EmulatedFakeCamera3Info.cpp \
        fake-pipeline2/camera_hw.cpp \
        VendorTags.cpp \
        LoadXml.cpp \

ifeq ($(TARGET_PRODUCT),vbox_x86)
LOCAL_MODULE := camera.vbox_x86
else
LOCAL_MODULE:= camera.$(TARGET_PRODUCT)
endif

include $(BUILD_SHARED_LIBRARY)

include $(call all-makefiles-under,$(LOCAL_PATH))

$(info ************************************************************)

$(info pdk or not:    $(TARGET_BUILD_PDK))
$(info target board:  $(TARGET_BOARD_PLATFORM))
$(info git version:   $(CAMHAL_GIT_VERSION))
$(info uncomment:     $(CAMHAL_GIT_UNCOMMIT_FILE_NUM) files)
$(info last changed:  $(CAMHAL_LAST_CHANGED))
$(info build time:    $(CAMHAL_BUILD_TIME))
$(info builder name:  $(CAMHAL_BUILD_NAME))
$(info branch name:   $(CAMHAL_BRANCH_NAME))
$(info build mode:    $(CAMHAL_BUILD_MODE))
$(info host name:     $(CAMHAL_HOSTNAME))
$(info host IP:       $(CAMHAL_IP))
$(info path:          $(CAMHAL_PATH))
$(info ************************************************************)
$(shell sleep 1)

#################################################################
ifneq (true,true)

include $(CLEAR_VARS)

LOCAL_MODULE_RELATIVE_PATH := hw
LOCAL_CFLAGS += -fno-short-enums -DQEMU_HARDWARE
LOCAL_CFLAGS += -Wno-unused-parameter
LOCAL_SHARED_LIBRARIES:= \
    libcutils \
    liblog \
    libskia \
    libandroid_runtime

LOCAL_C_INCLUDES += external/jpeg \
                    external/skia/include/core/ \
                    frameworks/base/core/jni/android/graphics \
                    frameworks/native/include

LOCAL_SRC_FILES := JpegStub.cpp

LOCAL_MODULE := camera.goldfish.jpeg

include $(BUILD_SHARED_LIBRARY)

endif # !PDK
