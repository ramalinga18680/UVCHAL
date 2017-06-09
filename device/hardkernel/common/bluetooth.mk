#
# Copyright (C) 2012 The Android Open Source Project
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
#

ifeq ($(BOARD_HAVE_BLUETOOTH),true)
PRODUCT_PROPERTY_OVERRIDES += \
    config.disable_bluetooth=false
PRODUCT_PACKAGES += \
    libbt-hci \
    bluetooth.default \
    audio.a2dp.default \
    libbt-client-api

ifneq ($(wildcard device/hardkernel/$(TARGET_PRODUCT)/bluetooth),)
BOARD_BLUETOOTH_BDROID_BUILDCFG_INCLUDE_DIR := device/hardkernel/$(TARGET_PRODUCT)/bluetooth
endif
else
PRODUCT_PROPERTY_OVERRIDES += \
    config.disable_bluetooth=true
endif

PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.bluetooth.xml:system/etc/permissions/android.hardware.bluetooth.xml \
    frameworks/native/data/etc/android.hardware.bluetooth_le.xml:system/etc/permissions/android.hardware.bluetooth_le.xml

PRODUCT_PACKAGES += libbt-vendor
