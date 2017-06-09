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

#Supported modules:
#                bcm40183
#                bcm40181
#		 bcm43458
#                rtl8188eu
#                rt5370
#                rt8189es
#                rt8723bs
#                rtl8723au
#                mt7601
#                mt5931
#                AP62x2
#                AP6335
#                AP6441
#                AP6234
#                AP6181
#                AP6210
#                bcm43341
#                bcm43241
#                rtl8192du
#                rtl8192eu
#                rtl8192cu
#                rtl88x1au
#                rtl8812au


PRODUCT_PACKAGES += wpa_supplicant.conf

PRODUCT_COPY_FILES += \
	frameworks/native/data/etc/android.hardware.wifi.xml:system/etc/permissions/android.hardware.wifi.xml

PRODUCT_PROPERTY_OVERRIDES += \
	ro.carrier=wifi-only

################################################################################## bcm4354
ifeq ($(WIFI_MODULE),bcm4354)
WIFI_DRIVER := bcm4354
WIFI_DRIVER_MODULE_PATH := /system/lib/dhd.ko
WIFI_DRIVER_MODULE_NAME := dhd
WIFI_DRIVER_MODULE_ARG  := "firmware_path=/etc/wifi/4354/fw_bcm4354a1_ag.bin nvram_path=/etc/wifi/4354/nvram_ap6354.txt"
WIFI_DRIVER_FW_PATH_STA := /etc/wifi/4354/fw_bcm4354a1_ag.bin
WIFI_DRIVER_FW_PATH_AP  := /etc/wifi/4354/fw_bcm4354a1_ag_apsta.bin
WIFI_DRIVER_FW_PATH_P2P := /etc/wifi/4354/fw_bcm4354a1_ag_p2p.bin

BOARD_WLAN_DEVICE := bcmdhd
WIFI_DRIVER_FW_PATH_PARAM   := "/sys/module/dhd/parameters/firmware_path"

WPA_SUPPLICANT_VERSION := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_bcmdhd
BOARD_HOSTAPD_DRIVER        := NL80211
BOARD_HOSTAPD_PRIVATE_LIB   := lib_driver_cmd_bcmdhd

PRODUCT_PACKAGES += \
	4354/nvram_ap6354.txt \
	4354/fw_bcm4354a1_ag.bin \
	4354/fw_bcm4354a1_ag_apsta.bin \
	4354/fw_bcm4354a1_ag_p2p.bin \
	wl \
	p2p_supplicant_overlay.conf \
	dhd

PRODUCT_COPY_FILES += \
	frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml

ifneq ($(wildcard $(TARGET_PRODUCT_DIR)/dhd.ko),)
PRODUCT_COPY_FILES += $(TARGET_PRODUCT_DIR)/dhd.ko:system/lib/dhd.ko
endif

PRODUCT_PROPERTY_OVERRIDES += \
	wifi.interface=wlan0

endif



################################################################################## bcm4356
ifeq ($(WIFI_MODULE),bcm4356)
WIFI_DRIVER := bcm4356
WIFI_DRIVER_MODULE_PATH := /system/lib/dhd.ko
WIFI_DRIVER_MODULE_NAME := dhd
WIFI_DRIVER_MODULE_ARG  := "firmware_path=/etc/wifi/4356/fw_bcm4356a2_ag.bin nvram_path=/etc/wifi/4356/nvram_ap6356.txt"
WIFI_DRIVER_FW_PATH_STA := /etc/wifi/4356/fw_bcm4356a2_ag.bin
WIFI_DRIVER_FW_PATH_AP  := /etc/wifi/4356/fw_bcm4356a2_ag_apsta.bin
WIFI_DRIVER_FW_PATH_P2P := /etc/wifi/4356/fw_bcm4356a2_ag_p2p.bin

BOARD_WLAN_DEVICE := bcmdhd
WIFI_DRIVER_FW_PATH_PARAM   := "/sys/module/dhd/parameters/firmware_path"

WPA_SUPPLICANT_VERSION := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_bcmdhd
BOARD_HOSTAPD_DRIVER        := NL80211
BOARD_HOSTAPD_PRIVATE_LIB   := lib_driver_cmd_bcmdhd

PRODUCT_PACKAGES += \
        4356/nvram_ap6356.txt \
        4356/fw_bcm4356a2_ag.bin \
	4356/fw_bcm4356a2_ag_apsta.bin \
	4356/fw_bcm4356a2_ag_p2p.bin \
        wl \
        p2p_supplicant_overlay.conf \
        dhd

PRODUCT_COPY_FILES += \
        frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml

ifneq ($(wildcard $(TARGET_PRODUCT_DIR)/dhd.ko),)
PRODUCT_COPY_FILES += $(TARGET_PRODUCT_DIR)/dhd.ko:system/lib/dhd.ko
endif

PRODUCT_PROPERTY_OVERRIDES += \
        wifi.interface=wlan0

endif


################################################################################## bcm43458
ifeq ($(WIFI_MODULE),bcm43458)
WIFI_DRIVER := bcm43458
WIFI_DRIVER_MODULE_PATH := /system/lib/dhd.ko
WIFI_DRIVER_MODULE_NAME := dhd
WIFI_DRIVER_MODULE_ARG  := "firmware_path=/etc/wifi/43458/fw_bcm43455c0_ag.bin nvram_path=/etc/wifi/43458/nvram_43458.txt"
WIFI_DRIVER_FW_PATH_STA := /etc/wifi/43458/fw_bcm43455c0_ag.bin
WIFI_DRIVER_FW_PATH_AP  := /etc/wifi/43458/fw_bcm43455c0_ag_apsta.bin
WIFI_DRIVER_FW_PATH_P2P := /etc/wifi/43458/fw_bcm43455c0_ag_p2p.bin

BOARD_WLAN_DEVICE := bcmdhd
WIFI_DRIVER_FW_PATH_PARAM   := "/sys/module/dhd/parameters/firmware_path"

WPA_SUPPLICANT_VERSION := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_bcmdhd
BOARD_HOSTAPD_DRIVER        := NL80211
BOARD_HOSTAPD_PRIVATE_LIB   := lib_driver_cmd_bcmdhd

PRODUCT_PACKAGES += \
        43458/nvram_43458.txt \
        43458/fw_bcm43455c0_ag.bin \
	 43458/fw_bcm43455c0_ag_apsta.bin \
	 43458/fw_bcm43455c0_ag_p2p.bin \
        wl \
	p2p_supplicant_overlay.conf \
        dhd

PRODUCT_COPY_FILES += \
        frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml

ifneq ($(wildcard $(TARGET_PRODUCT_DIR)/dhd.ko),)
PRODUCT_COPY_FILES += $(TARGET_PRODUCT_DIR)/dhd.ko:system/lib/dhd.ko
endif

PRODUCT_PROPERTY_OVERRIDES += \
        wifi.interface=wlan0

endif

################################################################################## 8189es
ifeq ($(WIFI_MODULE),rtl8189es)

WIFI_DRIVER := 8189es
BOARD_WIFI_VENDOR       := realtek
WIFI_DRIVER_MODULE_PATH := /system/lib/8189es.ko
WIFI_DRIVER_MODULE_NAME := 8189es
WIFI_DRIVER_MODULE_ARG  := "ifname=wlan0 if2name=p2p0"
WIFI_FIRMWARE_LOADER :=""
WIFI_DRIVER_FW_PATH_PARAM :=""

BOARD_WLAN_DEVICE := rtl8189es
LIB_WIFI_HAL := libwifi-hal-rtl
WIFI_DRIVER_FW_PATH_PARAM   := ""

WPA_SUPPLICANT_VERSION := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_rtl
BOARD_HOSTAPD_DRIVER        := NL80211
BOARD_HOSTAPD_PRIVATE_LIB   := lib_driver_cmd_rtl

PRODUCT_PACKAGES += \
       wpa_supplicant_overlay.conf \
       p2p_supplicant_overlay.conf
PRODUCT_COPY_FILES += \
	frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml

ifneq ($(wildcard $(TARGET_PRODUCT_DIR)/8189es.ko),)
PRODUCT_COPY_FILES += $(TARGET_PRODUCT_DIR)/8189es.ko:system/lib/8189es.ko
endif

PRODUCT_PROPERTY_OVERRIDES += \
	wifi.interface=wlan0

endif
################################################################################## 8189ftv
ifeq ($(WIFI_MODULE),rtl8189fs)

WIFI_DRIVER := 8189fs
BOARD_WIFI_VENDOR       := realtek
WIFI_DRIVER_MODULE_PATH := /system/lib/8189fs.ko
WIFI_DRIVER_MODULE_NAME := 8189fs
WIFI_DRIVER_MODULE_ARG  := "ifname=wlan0 if2name=p2p0"
WIFI_FIRMWARE_LOADER :=""
WIFI_DRIVER_FW_PATH_PARAM :=""

BOARD_WLAN_DEVICE := rtl8189fs
LIB_WIFI_HAL := libwifi-hal-rtl
WIFI_DRIVER_FW_PATH_PARAM   := ""

WPA_SUPPLICANT_VERSION := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_rtl
BOARD_HOSTAPD_DRIVER        := NL80211
BOARD_HOSTAPD_PRIVATE_LIB   := lib_driver_cmd_rtl


PRODUCT_PACKAGES += \
	wpa_supplicant_overlay.conf \
	p2p_supplicant_overlay.conf

PRODUCT_COPY_FILES += \
	frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml

ifneq ($(wildcard $(TARGET_PRODUCT_DIR)/8189fs.ko),)
PRODUCT_COPY_FILES += $(TARGET_PRODUCT_DIR)/8189fs.ko:system/lib/8189fs.ko
endif

PRODUCT_PROPERTY_OVERRIDES += \
	wifi.interface=wlan0

endif

################################################################################## 8723bs
ifeq ($(WIFI_MODULE),rtl8723bs)

WIFI_DRIVER := 8723bs
BOARD_WIFI_VENDOR       := realtek
WIFI_DRIVER_MODULE_PATH := /system/lib/8723bs.ko
WIFI_DRIVER_MODULE_NAME := 8723bs
WIFI_DRIVER_MODULE_ARG  := "ifname=wlan0 if2name=p2p0"
WIFI_FIRMWARE_LOADER :=""
WIFI_DRIVER_FW_PATH_PARAM :=""

BOARD_WLAN_DEVICE := rtl8723bs
LIB_WIFI_HAL := libwifi-hal-rtl
WIFI_DRIVER_FW_PATH_PARAM   := ""

WPA_SUPPLICANT_VERSION := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_rtl
BOARD_HOSTAPD_DRIVER        := NL80211
BOARD_HOSTAPD_PRIVATE_LIB   := lib_driver_cmd_rtl

PRODUCT_PACKAGES += \
       wpa_supplicant_overlay.conf \
       p2p_supplicant_overlay.conf
PRODUCT_COPY_FILES += \
	frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml

ifneq ($(wildcard $(TARGET_PRODUCT_DIR)/8723bs.ko),)
PRODUCT_COPY_FILES += $(TARGET_PRODUCT_DIR)/8723bs.ko:system/lib/8723bs.ko
endif

PRODUCT_PROPERTY_OVERRIDES += \
	wifi.interface=wlan0
endif
################################################################################## rtl8723bu
ifeq ($(WIFI_MODULE),rtl8723bu)

WIFI_DRIVER             := rtl8723bu
BOARD_WIFI_VENDOR       := realtek
WIFI_DRIVER_MODULE_PATH := /system/lib/8723bu.ko
WIFI_DRIVER_MODULE_NAME := 8723bu
WIFI_DRIVER_MODULE_ARG  := "ifname=wlan0 if2name=p2p0"

WPA_SUPPLICANT_VERSION           := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER      := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_rtl
BOARD_HOSTAPD_DRIVER             := NL80211
BOARD_HOSTAPD_PRIVATE_LIB        := lib_driver_cmd_rtl

BOARD_WLAN_DEVICE := rtl8723bu
PRODUCT_PACKAGES += \
    wpa_supplicant_overlay.conf \
    p2p_supplicant_overlay.conf

WIFI_FIRMWARE_LOADER      := ""
WIFI_DRIVER_FW_PATH_STA   := ""
WIFI_DRIVER_FW_PATH_AP    := ""
WIFI_DRIVER_FW_PATH_P2P   := ""
WIFI_DRIVER_FW_PATH_PARAM := ""

PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml

PRODUCT_PROPERTY_OVERRIDES += \
    wifi.interface=wlan0

endif
################################################################################## bcm40183
ifeq ($(WIFI_MODULE),bcm40183)

WIFI_DRIVER := bcm40183
WIFI_DRIVER_MODULE_PATH := /system/lib/dhd.ko
WIFI_DRIVER_MODULE_NAME := dhd
WIFI_DRIVER_MODULE_ARG  := "firmware_path=/etc/wifi/40183/fw_bcm40183b2.bin nvram_path=/etc/wifi/40183/nvram.txt"
WIFI_DRIVER_FW_PATH_STA :=/etc/wifi/40183/fw_bcm40183b2.bin
WIFI_DRIVER_FW_PATH_AP  :=/etc/wifi/40183/fw_bcm40183b2_apsta.bin
WIFI_DRIVER_FW_PATH_P2P :=/etc/wifi/40183/fw_bcm40183b2_p2p.bin

BOARD_WLAN_DEVICE := bcmdhd
LIB_WIFI_HAL := libwifi-hal-bcm
WIFI_DRIVER_FW_PATH_PARAM   := "/sys/module/dhd/parameters/firmware_path"

WPA_SUPPLICANT_VERSION := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_bcmdhd_ampak
BOARD_HOSTAPD_DRIVER        := NL80211
BOARD_HOSTAPD_PRIVATE_LIB   := lib_driver_cmd_bcmdhd_ampak

PRODUCT_PACKAGES += \
	40183/nvram.txt \
	40183/fw_bcm40183b2.bin \
	40183/fw_bcm40183b2_apsta.bin \
	40183/fw_bcm40183b2_p2p.bin \
	wl \
	p2p_supplicant_overlay.conf \
	dhd

PRODUCT_COPY_FILES += \
	frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml

ifneq ($(wildcard $(TARGET_PRODUCT_DIR)/dhd.ko),)
PRODUCT_COPY_FILES += $(TARGET_PRODUCT_DIR)/dhd.ko:system/lib/dhd.ko
endif

PRODUCT_PROPERTY_OVERRIDES += \
	wifi.interface=wlan0

endif

################################################################################## bcm40181
ifeq ($(WIFI_MODULE),bcm40181)
WIFI_DRIVER := bcm40181
WIFI_DRIVER_MODULE_PATH := /system/lib/dhd.ko
WIFI_DRIVER_MODULE_NAME := dhd
WIFI_DRIVER_MODULE_ARG  := "firmware_path=/etc/wifi/40181/fw_bcm40181a2.bin nvram_path=/etc/wifi/40181/nvram.txt"
WIFI_DRIVER_FW_PATH_STA :=/etc/wifi/40181/fw_bcm40181a2.bin
WIFI_DRIVER_FW_PATH_AP  :=/etc/wifi/40181/fw_bcm40181a2_apsta.bin
WIFI_DRIVER_FW_PATH_P2P :=/etc/wifi/40181/fw_bcm40181a2_p2p.bin

BOARD_WLAN_DEVICE := bcmdhd
LIB_WIFI_HAL := libwifi-hal-bcm
WIFI_DRIVER_FW_PATH_PARAM   := "/sys/module/dhd/parameters/firmware_path"

WPA_SUPPLICANT_VERSION := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_bcmdhd_ampak
BOARD_HOSTAPD_DRIVER        := NL80211
BOARD_HOSTAPD_PRIVATE_LIB   := lib_driver_cmd_bcmdhd_ampak

PRODUCT_PACKAGES += \
	40181/nvram.txt \
	40181/fw_bcm40181a0.bin \
	40181/fw_bcm40181a0_apsta.bin \
	40181/fw_bcm40181a2.bin \
	40181/fw_bcm40181a2_apsta.bin \
	40181/fw_bcm40181a2_p2p.bin \
	wl \
	p2p_supplicant_overlay.conf \
	dhd

PRODUCT_COPY_FILES += \
	frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml

ifneq ($(wildcard $(TARGET_PRODUCT_DIR)/dhd.ko),)
PRODUCT_COPY_FILES += $(TARGET_PRODUCT_DIR)/dhd.ko:system/lib/dhd.ko
endif

PRODUCT_PROPERTY_OVERRIDES += \
	wifi.interface=wlan0

endif
################################################################################## AP62x2
ifeq ($(WIFI_MODULE),AP62x2)
WIFI_DRIVER := AP62x2
WIFI_DRIVER_MODULE_PATH := /system/lib/dhd.ko
WIFI_DRIVER_MODULE_NAME := dhd
WIFI_DRIVER_MODULE_ARG  := "firmware_path=/etc/wifi/62x2/fw_bcm43241b4_ag.bin nvram_path=/etc/wifi/62x2/nvram.txt"
WIFI_DRIVER_FW_PATH_STA :=/etc/wifi/62x2/fw_bcm43241b4_ag.bin
WIFI_DRIVER_FW_PATH_AP  :=/etc/wifi/62x2/fw_bcm43241b4_ag_apsta.bin
WIFI_DRIVER_FW_PATH_P2P :=/etc/wifi/62x2/fw_bcm43241b4_ag_p2p.bin

BOARD_WLAN_DEVICE := bcmdhd
LIB_WIFI_HAL := libwifi-hal-bcm
WIFI_DRIVER_FW_PATH_PARAM   := "/sys/module/dhd/parameters/firmware_path"

WPA_SUPPLICANT_VERSION := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_bcmdhd_ampak
BOARD_HOSTAPD_DRIVER        := NL80211
BOARD_HOSTAPD_PRIVATE_LIB   := lib_driver_cmd_bcmdhd_ampak

PRODUCT_PACKAGES += \
	62x2/nvram.txt \
	62x2/fw_bcm43241b4_ag.bin \
	62x2/fw_bcm43241b4_ag_apsta.bin \
	62x2/fw_bcm43241b4_ag_p2p.bin \
	wl \
	p2p_supplicant_overlay.conf \
	dhd

PRODUCT_COPY_FILES += \
	frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml

ifneq ($(wildcard $(TARGET_PRODUCT_DIR)/dhd.ko),)
PRODUCT_COPY_FILES += $(TARGET_PRODUCT_DIR)/dhd.ko:system/lib/dhd.ko
endif

PRODUCT_PROPERTY_OVERRIDES += \
	wifi.interface=wlan0

endif
################################################################################## AP6335
ifeq ($(WIFI_MODULE),AP6335)
WIFI_DRIVER := AP6335
WIFI_DRIVER_MODULE_PATH := /system/lib/dhd.ko
WIFI_DRIVER_MODULE_NAME := dhd
WIFI_DRIVER_MODULE_ARG  := "firmware_path=/etc/wifi/6335/fw_bcm4339a0_ag.bin nvram_path=/etc/wifi/6335/nvram.txt"
WIFI_DRIVER_FW_PATH_STA :=/etc/wifi/6335/fw_bcm4339a0_ag.bin
WIFI_DRIVER_FW_PATH_AP  :=/etc/wifi/6335/fw_bcm4339a0_ag_apsta.bin
WIFI_DRIVER_FW_PATH_P2P :=/etc/wifi/6335/fw_bcm4339a0_ag_p2p.bin

BOARD_WLAN_DEVICE := bcmdhd
LIB_WIFI_HAL := libwifi-hal-bcm
WIFI_DRIVER_FW_PATH_PARAM   := "/sys/module/dhd/parameters/firmware_path"

WPA_SUPPLICANT_VERSION := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_bcmdhd_ampak
BOARD_HOSTAPD_DRIVER        := NL80211
BOARD_HOSTAPD_PRIVATE_LIB   := lib_driver_cmd_bcmdhd_ampak
PRODUCT_PACKAGES += \
	6335/nvram.txt \
	6335/fw_bcm4339a0_ag.bin \
	6335/fw_bcm4339a0_ag_apsta.bin \
	6335/fw_bcm4339a0_ag_p2p.bin \
	6335/nvram_ap6335e.txt   \
	6335/fw_bcm4339a0e_ag.bin \
	6335/fw_bcm4339a0e_ag_apsta.bin \
	6335/fw_bcm4339a0e_ag_p2p.bin \
	wl \
	p2p_supplicant_overlay.conf \
	dhd

PRODUCT_COPY_FILES += \
	frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml

ifneq ($(wildcard $(TARGET_PRODUCT_DIR)/dhd.ko),)
PRODUCT_COPY_FILES += $(TARGET_PRODUCT_DIR)/dhd.ko:system/lib/dhd.ko
endif

PRODUCT_PROPERTY_OVERRIDES += \
	wifi.interface=wlan0

endif
################################################################################## AP6441
ifeq ($(WIFI_MODULE),AP6441)
WIFI_DRIVER := AP6441
WIFI_DRIVER_MODULE_PATH := /system/lib/dhd.ko
WIFI_DRIVER_MODULE_NAME := dhd
WIFI_DRIVER_MODULE_ARG  := "firmware_path=/etc/wifi/6441/fw_bcm43341b0_ag.bin nvram_path=/etc/wifi/6441/nvram.txt"
WIFI_DRIVER_FW_PATH_STA :=/etc/wifi/6441/fw_bcm43341b0_ag.bin
WIFI_DRIVER_FW_PATH_AP  :=/etc/wifi/6441/fw_bcm43341b0_ag_apsta.bin
WIFI_DRIVER_FW_PATH_P2P :=/etc/wifi/6441/fw_bcm43341b0_ag_p2p.bin

BOARD_WLAN_DEVICE := bcmdhd
LIB_WIFI_HAL := libwifi-hal-bcm
WIFI_DRIVER_FW_PATH_PARAM   := "/sys/module/dhd/parameters/firmware_path"

WPA_SUPPLICANT_VERSION := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_bcmdhd_ampak
BOARD_HOSTAPD_DRIVER        := NL80211
BOARD_HOSTAPD_PRIVATE_LIB   := lib_driver_cmd_bcmdhd_ampak
PRODUCT_PACKAGES += \
	6441/nvram.txt    \
	6441/fw_bcm43341b0_ag.bin \
	6441/fw_bcm43341b0_ag_apsta.bin \
	6441/fw_bcm43341b0_ag_p2p.bin \
	wl \
	p2p_supplicant_overlay.conf \
	dhd

PRODUCT_COPY_FILES += \
	frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml

ifneq ($(wildcard $(TARGET_PRODUCT_DIR)/dhd.ko),)
PRODUCT_COPY_FILES += $(TARGET_PRODUCT_DIR)/dhd.ko:system/lib/dhd.ko
endif

PRODUCT_PROPERTY_OVERRIDES += \
	wifi.interface=wlan0

endif

################################################################################## AP6234
ifeq ($(WIFI_MODULE),AP6234)
WIFI_DRIVER := AP6234
WIFI_DRIVER_MODULE_PATH := /system/lib/dhd.ko
WIFI_DRIVER_MODULE_NAME := dhd
WIFI_DRIVER_MODULE_ARG  := "firmware_path=/etc/wifi/6234/fw_bcm43341b0_ag.bin nvram_path=/etc/wifi/6234/nvram.txt"
WIFI_DRIVER_FW_PATH_STA :=/etc/wifi/6234/fw_bcm43341b0_ag.bin
WIFI_DRIVER_FW_PATH_AP  :=/etc/wifi/6234/fw_bcm43341b0_ag_apsta.bin
WIFI_DRIVER_FW_PATH_P2P :=/etc/wifi/6234/fw_bcm43341b0_ag_p2p.bin

BOARD_WLAN_DEVICE := bcmdhd
LIB_WIFI_HAL := libwifi-hal-bcm
WIFI_DRIVER_FW_PATH_PARAM   := "/sys/module/dhd/parameters/firmware_path"

WPA_SUPPLICANT_VERSION := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_bcmdhd_ampak
BOARD_HOSTAPD_DRIVER        := NL80211
BOARD_HOSTAPD_PRIVATE_LIB   := lib_driver_cmd_bcmdhd_ampak
PRODUCT_PACKAGES += \
	6234/nvram.txt    \
	6234/fw_bcm43341b0_ag.bin \
	6234/fw_bcm43341b0_ag_apsta.bin \
	6234/fw_bcm43341b0_ag_p2p.bin \
	p2p_supplicant_overlay.conf \
	wl \
	dhd

PRODUCT_COPY_FILES += \
	frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml

ifneq ($(wildcard $(TARGET_PRODUCT_DIR)/dhd.ko),)
PRODUCT_COPY_FILES += $(TARGET_PRODUCT_DIR)/dhd.ko:system/lib/dhd.ko
endif

PRODUCT_PROPERTY_OVERRIDES += \
	wifi.interface=wlan0
endif

################################################################################## AP6212
ifeq ($(WIFI_MODULE),AP6212)
WIFI_DRIVER := AP6212
WIFI_DRIVER_MODULE_PATH := /system/lib/dhd.ko
WIFI_DRIVER_MODULE_NAME := dhd
WIFI_DRIVER_MODULE_ARG  := "firmware_path=/etc/wifi/6212/fw_bcm43438a0.bin nvram_path=/etc/wifi/6212/nvram.txt"
WIFI_DRIVER_FW_PATH_STA := /etc/wifi/6212/fw_bcm43438a0.bin
WIFI_DRIVER_FW_PATH_AP  := /etc/wifi/6212/fw_bcm43438a0_apsta.bin
WIFI_DRIVER_FW_PATH_P2P := /etc/wifi/6212/fw_bcm43438a0_p2p.bin

BOARD_WLAN_DEVICE := bcmdhd
WIFI_DRIVER_FW_PATH_PARAM   := "/sys/module/dhd/parameters/firmware_path"

WPA_SUPPLICANT_VERSION := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_bcmdhd_ampak
BOARD_HOSTAPD_DRIVER        := NL80211
BOARD_HOSTAPD_PRIVATE_LIB   := lib_driver_cmd_bcmdhd_ampak
PRODUCT_PACKAGES += \
	6212/nvram.txt    \
	6212/fw_bcm43438a0.bin \
	6212/fw_bcm43438a0_apsta.bin \
	6212/fw_bcm43438a0_p2p.bin \
	wl \
	p2p_supplicant_overlay.conf \
	dhd

PRODUCT_COPY_FILES += \
	frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml

ifneq ($(wildcard $(TARGET_PRODUCT_DIR)/dhd.ko),)
PRODUCT_COPY_FILES += $(TARGET_PRODUCT_DIR)/dhd.ko:system/lib/dhd.ko
endif

PRODUCT_PROPERTY_OVERRIDES += \
	wifi.interface=wlan0
endif


################################################################################## bcm43341
ifeq ($(WIFI_MODULE),bcm43341)
WIFI_DRIVER := bcm43341
WIFI_DRIVER_MODULE_PATH := /system/lib/bcmdhd.ko
WIFI_DRIVER_MODULE_NAME := bcmdhd
WIFI_DRIVER_MODULE_ARG  := "iface_name=wlan0 firmware_path=/etc/wifi/fw_bcmdhd_43341.bin nvram_path=/etc/wifi/nvram_43341.bin"
WIFI_DRIVER_FW_PATH_STA :=/etc/wifi/fw_bcmdhd_43341.bin
WIFI_DRIVER_FW_PATH_AP  :=/etc/wifi/fw_bcmdhd_43341.bin
WIFI_DRIVER_FW_PATH_P2P :=/etc/wifi/fw_bcmdhd_43341.bin

BOARD_WLAN_DEVICE := bcmdhd
LIB_WIFI_HAL := libwifi-hal-bcm
WIFI_DRIVER_FW_PATH_PARAM   := "/sys/module/bcmdhd/parameters/firmware_path"

WPA_SUPPLICANT_VERSION := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_bcmdhd_usi
BOARD_HOSTAPD_DRIVER        := NL80211
BOARD_HOSTAPD_PRIVATE_LIB   := lib_driver_cmd_bcmdhd_usi
PRODUCT_PACKAGES += \
	nvram_43341.bin   \
	fw_bcmdhd_43341.bin \
	wl \
	p2p_supplicant_overlay.conf \
	dhd

PRODUCT_COPY_FILES += \
	frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml

ifneq ($(wildcard $(TARGET_PRODUCT_DIR)/bcmdhd.ko),)
PRODUCT_COPY_FILES += $(TARGET_PRODUCT_DIR)/bcmdhd.ko:system/lib/bcmdhd.ko
endif

PRODUCT_PROPERTY_OVERRIDES += \
	wifi.interface=wlan0

endif
################################################################################## bcm43241
ifeq ($(WIFI_MODULE),bcm43241)
WIFI_DRIVER := bcm43241
WIFI_DRIVER_MODULE_PATH := /system/lib/bcmdhd.ko
WIFI_DRIVER_MODULE_NAME := bcmdhd
WIFI_DRIVER_MODULE_ARG  := "iface_name=wlan0 firmware_path=/etc/wifi/fw_bcmdhd_43241.bin nvram_path=/etc/wifi/nvram_43241.bin"
WIFI_DRIVER_FW_PATH_STA :=/etc/wifi/fw_bcmdhd_43241.bin
WIFI_DRIVER_FW_PATH_AP  :=/etc/wifi/fw_bcmdhd_43241.bin
WIFI_DRIVER_FW_PATH_P2P :=/etc/wifi/fw_bcmdhd_43241.bin

BOARD_WLAN_DEVICE := bcmdhd
LIB_WIFI_HAL := libwifi-hal-bcm
WIFI_DRIVER_FW_PATH_PARAM   := "/sys/module/bcmdhd/parameters/firmware_path"

WPA_SUPPLICANT_VERSION := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_bcmdhd_usi
BOARD_HOSTAPD_DRIVER        := NL80211
BOARD_HOSTAPD_PRIVATE_LIB   := lib_driver_cmd_bcmdhd_usi
PRODUCT_PACKAGES += \
	nvram_43241.bin   \
	fw_bcmdhd_43241.bin \
	wl \
	p2p_supplicant_overlay.conf \
	dhd

PRODUCT_COPY_FILES += \
	frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml

ifneq ($(wildcard $(TARGET_PRODUCT_DIR)/bcmdhd.ko),)
PRODUCT_COPY_FILES += $(TARGET_PRODUCT_DIR)/bcmdhd.ko:system/lib/bcmdhd.ko
endif

PRODUCT_PROPERTY_OVERRIDES += \
	wifi.interface=wlan0

endif
################################################################################## rtl8192cu
ifeq ($(WIFI_MODULE),rtl8192cu)

WIFI_DRIVER             := rtl8192cu
BOARD_WIFI_VENDOR       := realtek
WIFI_DRIVER_MODULE_PATH := /system/lib/8192cu.ko
WIFI_DRIVER_MODULE_NAME := 8192cu
WIFI_DRIVER_MODULE_ARG  := "ifname=wlan0 if2name=p2p0"

WPA_SUPPLICANT_VERSION           := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER      := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_rtl
BOARD_HOSTAPD_DRIVER             := NL80211
BOARD_HOSTAPD_PRIVATE_LIB        := lib_driver_cmd_rtl

BOARD_WLAN_DEVICE := rtl8192cu
LIB_WIFI_HAL := libwifi-hal-rtl

WIFI_FIRMWARE_LOADER      := ""
WIFI_DRIVER_FW_PATH_PARAM := ""

PRODUCT_COPY_FILES += \
	frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml

PRODUCT_PACKAGES += \
	wpa_supplicant_overlay.conf \
	p2p_supplicant_overlay.conf

PRODUCT_PROPERTY_OVERRIDES += \
	wifi.interface=wlan0

endif
################################################################################## rtl8188eu
ifeq ($(WIFI_MODULE),rtl8188eu)

WIFI_DRIVER             := rtl8188eu
BOARD_WIFI_VENDOR       := realtek
WIFI_DRIVER_MODULE_PATH := /system/lib/8188eu.ko
WIFI_DRIVER_MODULE_NAME := 8188eu

WIFI_DRIVER_MODULE_ARG    := "ifname=wlan0 if2name=p2p0"
WIFI_DRIVER_FW_PATH_PARAM := "/dev/null"
WIFI_DRIVER_FW_PATH_STA   := ""
WIFI_DRIVER_FW_PATH_AP    := ""
WIFI_DRIVER_FW_PATH_P2P   := ""

WPA_SUPPLICANT_VERSION           := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER      := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_rtl
BOARD_HOSTAPD_DRIVER             := NL80211
BOARD_HOSTAPD_PRIVATE_LIB        := lib_driver_cmd_rtl

BOARD_WLAN_DEVICE := rtl8189es
LIB_WIFI_HAL := libwifi-hal-rtl

WIFI_FIRMWARE_LOADER      := "wlan_fwloader"


PRODUCT_COPY_FILES += \
	frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml

PRODUCT_PACKAGES += \
	wpa_supplicant_overlay.conf \
	p2p_supplicant_overlay.conf \
	wlan_fwloader

PRODUCT_PROPERTY_OVERRIDES += \
	wifi.interface=wlan0
endif
################################################################################## rtl8188ftv
ifeq ($(WIFI_MODULE),rtl8188ftv)

WIFI_DRIVER             := rtl8188ftv
BOARD_WIFI_VENDOR       := realtek
WIFI_DRIVER_MODULE_PATH := /system/lib/8188fu.ko
WIFI_DRIVER_MODULE_NAME := 8188fu

WIFI_DRIVER_MODULE_ARG    := "ifname=wlan0 if2name=p2p0"
WIFI_DRIVER_FW_PATH_PARAM := "/dev/null"
WIFI_DRIVER_FW_PATH_STA   := ""
WIFI_DRIVER_FW_PATH_AP    := ""
WIFI_DRIVER_FW_PATH_P2P   := ""

WPA_SUPPLICANT_VERSION           := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER      := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_rtl
BOARD_HOSTAPD_DRIVER             := NL80211
BOARD_HOSTAPD_PRIVATE_LIB        := lib_driver_cmd_rtl

BOARD_WLAN_DEVICE := rtl8189es
LIB_WIFI_HAL := libwifi-hal-rtl

WIFI_FIRMWARE_LOADER      := ""


PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml

PRODUCT_PACKAGES += \
    wpa_supplicant_overlay.conf \
    p2p_supplicant_overlay.conf

PRODUCT_PROPERTY_OVERRIDES += \
    wifi.interface=wlan0

endif
################################################################################## rtl8192du
ifeq ($(WIFI_MODULE),rtl8192du)

WIFI_DRIVER             := rtl8192du
BOARD_WIFI_VENDOR       := realtek
WIFI_DRIVER_MODULE_PATH := /system/lib/8192du.ko
WIFI_DRIVER_MODULE_NAME := 8192du
WIFI_DRIVER_MODULE_ARG  := "ifname=wlan0 if2name=p2p0"

WPA_SUPPLICANT_VERSION           := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER      := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_rtl
BOARD_HOSTAPD_DRIVER             := NL80211
BOARD_HOSTAPD_PRIVATE_LIB        := lib_driver_cmd_rtl

BOARD_WLAN_DEVICE := rtl8192du
LIB_WIFI_HAL := libwifi-hal-rtl

WIFI_FIRMWARE_LOADER      := ""
WIFI_DRIVER_FW_PATH_PARAM := ""

PRODUCT_COPY_FILES += \
	frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml

PRODUCT_PACKAGES += \
	wpa_supplicant_overlay.conf \
	p2p_supplicant_overlay.conf

PRODUCT_PROPERTY_OVERRIDES += \
	wifi.interface=wlan0

endif
################################################################################## rtl8192eu
ifeq ($(WIFI_MODULE),rtl8192eu)

WIFI_DRIVER             := rtl8192eu
BOARD_WIFI_VENDOR       := realtek
WIFI_DRIVER_MODULE_PATH := /system/lib/8192eu.ko
WIFI_DRIVER_MODULE_NAME := 8192eu
WIFI_DRIVER_MODULE_ARG  := "ifname=wlan0 if2name=p2p0"

WPA_SUPPLICANT_VERSION           := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER      := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_rtl
BOARD_HOSTAPD_DRIVER             := NL80211
BOARD_HOSTAPD_PRIVATE_LIB        := lib_driver_cmd_rtl

BOARD_WLAN_DEVICE := rtl8192eu
LIB_WIFI_HAL := libwifi-hal-rtl

WIFI_FIRMWARE_LOADER      := ""
WIFI_DRIVER_FW_PATH_PARAM := ""

PRODUCT_COPY_FILES += \
	frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml

PRODUCT_PACKAGES += \
	wpa_supplicant_overlay.conf \
	p2p_supplicant_overlay.conf

PRODUCT_PROPERTY_OVERRIDES += \
	wifi.interface=wlan0

endif
################################################################################## rtl8723au
ifeq ($(WIFI_MODULE),rtl8723au)

WIFI_DRIVER             := rtl8723au
BOARD_WIFI_VENDOR       := realtek
WIFI_DRIVER_MODULE_PATH := /system/lib/8723au.ko
WIFI_DRIVER_MODULE_NAME := 8723au
WIFI_DRIVER_MODULE_ARG  := "ifname=wlan0 if2name=p2p0"

WPA_SUPPLICANT_VERSION           := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER      := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_rtl
BOARD_HOSTAPD_DRIVER             := NL80211
BOARD_HOSTAPD_PRIVATE_LIB        := lib_driver_cmd_rtl

BOARD_WLAN_DEVICE := rtl8723au
LIB_WIFI_HAL := libwifi-hal-rtl

WIFI_FIRMWARE_LOADER      := ""
WIFI_DRIVER_FW_PATH_PARAM := ""

PRODUCT_COPY_FILES += \
	frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml

PRODUCT_PROPERTY_OVERRIDES += \
	wifi.interface=wlan0

endif
################################################################################## rtl8811au,rtl8821au
ifeq ($(WIFI_MODULE),rtl88x1au)

WIFI_DRIVER             := rtl88x1au
BOARD_WIFI_VENDOR       := realtek
WIFI_DRIVER_MODULE_PATH := /system/lib/8821au.ko
WIFI_DRIVER_MODULE_NAME := 8821au
WIFI_DRIVER_MODULE_ARG  := "ifname=wlan0 if2name=p2p0"

WPA_SUPPLICANT_VERSION           := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER      := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_rtl
BOARD_HOSTAPD_DRIVER             := NL80211
BOARD_HOSTAPD_PRIVATE_LIB        := lib_driver_cmd_rtl

BOARD_WLAN_DEVICE := rtl88x1au
LIB_WIFI_HAL := libwifi-hal-rtl

WIFI_FIRMWARE_LOADER      := ""
WIFI_DRIVER_FW_PATH_PARAM := ""

PRODUCT_COPY_FILES += \
	frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml

PRODUCT_PACKAGES += \
	wpa_supplicant_overlay.conf \
	p2p_supplicant_overlay.conf

PRODUCT_PROPERTY_OVERRIDES += \
	wifi.interface=wlan0

endif
################################################################################## rtl8812au
ifeq ($(WIFI_MODULE),rtl8812au)

WIFI_DRIVER             := rtl8812au
BOARD_WIFI_VENDOR       := realtek
WIFI_DRIVER_MODULE_PATH := /system/lib/8812au.ko
WIFI_DRIVER_MODULE_NAME := 8812au
WIFI_DRIVER_MODULE_ARG  := "ifname=wlan0 if2name=p2p0"

WPA_SUPPLICANT_VERSION           := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER      := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_rtl
BOARD_HOSTAPD_DRIVER             := NL80211
BOARD_HOSTAPD_PRIVATE_LIB        := lib_driver_cmd_rtl

BOARD_WLAN_DEVICE := rtl8812au
LIB_WIFI_HAL := libwifi-hal-rtl

WIFI_FIRMWARE_LOADER      := ""
WIFI_DRIVER_FW_PATH_PARAM := ""

PRODUCT_COPY_FILES += \
	frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml

PRODUCT_PACKAGES += \
	wpa_supplicant_overlay.conf \
	p2p_supplicant_overlay.conf

# 89976: Add Realtek USB WiFi support
PRODUCT_PROPERTY_OVERRIDES += \
	wifi.interface=wlan0

endif
################################################################################## rt5370
ifeq ($(WIFI_MODULE),rt5370)

WIFI_DRIVER             := rt5370
WIFI_DRIVER_MODULE_PATH := /system/lib/rt5370sta.ko
WIFI_DRIVER_MODULE_NAME := rt5370sta

WPA_SUPPLICANT_VERSION  := VER_0_8_X

BOARD_WPA_SUPPLICANT_PRIVATE_LIB  := lib_driver_cmd_nl80211
BOARD_WPA_SUPPLICANT_DRIVER       := NL80211

LIB_WIFI_HAL := libwifi-hal-rtl

ifneq ($(wildcard $(TARGET_PRODUCT_DIR)/rt5370sta.ko),)
PRODUCT_COPY_FILES += $(TARGET_PRODUCT_DIR)/rt5370sta.ko:system/lib/rt5370sta.ko
endif

PRODUCT_PROPERTY_OVERRIDES += \
	wifi.interface=wlan0

endif

################################################################################## mt7601u
ifeq ($(WIFI_MODULE),mt7601u)

WIFI_DRIVER             := mt7601u
WIFI_DRIVER_MODULE_PATH := /system/lib/mt7601usta.ko
WIFI_DRIVER_MODULE_NAME := mt7601usta
BOARD_WIFI_VENDOR		:= mtk
WPA_SUPPLICANT_VERSION  := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER       := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB  := lib_driver_cmd_mtk
BOARD_HOSTAPD_DRIVER             := NL80211
BOARD_HOSTAPD_PRIVATE_LIB        := lib_driver_cmd_mtk
BOARD_WLAN_DEVICE := mtk
PRODUCT_COPY_FILES += hardware/amlogic/wifi/mediatek/iwpriv:system/bin/iwpriv
PRODUCT_COPY_FILES += hardware/amlogic/wifi/mediatek/RT2870STA_7601.dat:system/etc/wifi/RT2870STA_7601.dat
PRODUCT_COPY_FILES += hardware/amlogic/wifi/mediatek/init.mtk.rc:root/init.mtk.rc
PRODUCT_COPY_FILES += hardware/amlogic/wifi/mediatek/wpa_supplicant.conf:system/etc/wifi/wpa_supplicant.conf
PRODUCT_COPY_FILES += hardware/amlogic/wifi/mediatek/p2p_supplicant_overlay.conf:system/etc/wifi/p2p_supplicant_overlay.conf
PRODUCT_COPY_FILES += hardware/amlogic/wifi/mediatek/mt7601usta.ko:system/lib/mt7601usta.ko
PRODUCT_COPY_FILES += frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml
PRODUCT_COPY_FILES += hardware/amlogic/wifi/mediatek/mtprealloc.ko:system/lib/mtprealloc.ko
PRODUCT_COPY_FILES += hardware/amlogic/wifi/mediatek/dhcpcd.conf:system/etc/dhcpcd/dhcpcd.conf

PRODUCT_PROPERTY_OVERRIDES += \
	wifi.interface=wlan0

endif

################################################################################## mt7603u
ifeq ($(WIFI_MODULE),mt7603u)

WIFI_DRIVER             := mt7603u
WIFI_DRIVER_MODULE_PATH := /system/lib/mt7603usta.ko
WIFI_DRIVER_MODULE_NAME := mt7603usta
BOARD_WIFI_VENDOR       := mtk
WPA_SUPPLICANT_VERSION  := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER       := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB  := lib_driver_cmd_mtk
BOARD_HOSTAPD_DRIVER             := NL80211
BOARD_HOSTAPD_PRIVATE_LIB        := lib_driver_cmd_mtk
BOARD_WLAN_DEVICE := mtk
PRODUCT_COPY_FILES += hardware/amlogic/wifi/mediatek/iwpriv:system/bin/iwpriv
PRODUCT_COPY_FILES += hardware/amlogic/wifi/mediatek/RT2870STA_7601.dat:system/etc/wifi/RT2870STA_7603.dat
PRODUCT_COPY_FILES += hardware/amlogic/wifi/mediatek/init.mtk.rc:root/init.mtk.rc
PRODUCT_COPY_FILES += hardware/amlogic/wifi/mediatek/wpa_supplicant.conf:system/etc/wifi/wpa_supplicant.conf
PRODUCT_COPY_FILES += hardware/amlogic/wifi/mediatek/p2p_supplicant_overlay.conf:system/etc/wifi/p2p_supplicant_overlay.conf
PRODUCT_COPY_FILES += hardware/amlogic/wifi/mediatek/mt7603usta.ko:system/lib/mt7603usta.ko
PRODUCT_COPY_FILES += hardware/amlogic/wifi/mediatek/mtprealloc.ko:system/lib/mtprealloc.ko
PRODUCT_COPY_FILES += hardware/amlogic/wifi/mediatek/dhcpcd.conf:system/etc/dhcpcd/dhcpcd.conf
PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml
PRODUCT_PROPERTY_OVERRIDES += \
    wifi.interface=wlan0

endif
################################################################################## mt5931
ifeq ($(WIFI_MODULE),mt5931)

MTK_WLAN_SUPPORT        := true
WIFI_DRIVER             := mt5931
WIFI_DRIVER_MODULE_PATH := /system/lib/wlan.ko
WIFI_DRIVER_MODULE_NAME := wlan
P2P_SUPPLICANT_VERSION  := VER_0_8_X_MTK
BOARD_P2P_SUPPLICANT_DRIVER       := NL80211

LIB_WIFI_HAL := libwifi-hal-rtl

PRODUCT_PACKAGES += \
	p2p_supplicant.conf

ifneq ($(wildcard $(TARGET_PRODUCT_DIR)/wlan.ko),)
PRODUCT_COPY_FILES += $(TARGET_PRODUCT_DIR)/wlan.ko:system/lib/wlan.ko
endif

PRODUCT_COPY_FILES += hardware/amlogic/wifi/mt5931/WIFI_RAM_CODE:system/etc/firmware/WIFI_RAM_CODE

PRODUCT_COPY_FILES += \
	frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml

PRODUCT_PROPERTY_OVERRIDES += \
	wifi.interface=wlan0

endif

################################################################################## qualcom9377

ifeq ($(WIFI_MODULE),QCOM9377)

WIFI_DRIVER := qcom9377
BOARD_WIFI_VENDOR       := qualcomm
WIFI_DRIVER_MODULE_PATH := /system/lib/wlan.ko
WIFI_DRIVER_MODULE_NAME := wlan
WIFI_DRIVER_MODULE_ARG  :=
WIFI_FIRMWARE_LOADER :=""
WIFI_DRIVER_FW_PATH_PARAM :=""

BOARD_WLAN_DEVICE := qcom9377
WIFI_DRIVER_FW_PATH_PARAM   := ""

WPA_SUPPLICANT_VERSION := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_qcom9377
BOARD_HOSTAPD_DRIVER        := NL80211
BOARD_HOSTAPD_PRIVATE_LIB   := lib_driver_cmd_qcom9377

PRODUCT_COPY_FILES += \
	hardware/amlogic/wifi/qcom9377/config/p2p_supplicant_overlay.conf:system/etc/wifi/p2p_supplicant_overlay.conf

PRODUCT_COPY_FILES += \
	hardware/amlogic/wifi/qcom9377/config/wpa_supplicant_overlay.conf:system/etc/wifi/wpa_supplicant_overlay.conf

PRODUCT_COPY_FILES += \
	frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml


PRODUCT_COPY_FILES += \
    hardware/amlogic/wifi/qcom9377/config/bt/nvm_tlv_tf_1.1.bin:system/etc/bluetooth/firmware/ar3k/nvm_tlv_tf_1.1.bin \
    hardware/amlogic/wifi/qcom9377/config/bt/rampatch_tlv_tf_1.1.tlv:system/etc/bluetooth/firmware/ar3k/rampatch_tlv_tf_1.1.tlv \
    hardware/amlogic/wifi/qcom9377/config/wifi/bdwlan30.bin:system/etc/wifi/firmware/bdwlan30.bin \
    hardware/amlogic/wifi/qcom9377/config/wifi/otp30.bin:system/etc/wifi/firmware/otp30.bin \
    hardware/amlogic/wifi/qcom9377/config/wifi/qwlan30.bin:system/etc/wifi/firmware/qwlan30.bin \
    hardware/amlogic/wifi/qcom9377/config/wifi/utf30.bin:system/etc/wifi/firmware/utf30.bin \
    hardware/amlogic/wifi/qcom9377/config/wifi/wlan/cfg.dat:system/etc/wifi/firmware/wlan/cfg.dat \
    hardware/amlogic/wifi/qcom9377/config/wifi/wlan/qcom_cfg.ini:system/etc/wifi/firmware/wlan/qcom_cfg.ini \
    hardware/amlogic/wifi/qcom9377/config/wifi/wlan/qcom_wlan_nv.bin:system/etc/wifi/firmware/wlan/qcom_wlan_nv.bin \

PRODUCT_PROPERTY_OVERRIDES += wifi.interface=wlan0
endif
################################################################################## AP6xxx
ifeq ($(WIFI_AP6xxx_MODULE),AP6181)

PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/AP6181/Wi-Fi/fw_bcm40181a2.bin:system/etc/wifi/40181/fw_bcm40181a2.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/AP6181/Wi-Fi/fw_bcm40181a2_apsta.bin:system/etc/wifi/40181/fw_bcm40181a2_apsta.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/AP6181/Wi-Fi/fw_bcm40181a2_p2p.bin:system/etc/wifi/40181/fw_bcm40181a2_p2p.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/AP6181/Wi-Fi/nvram_ap6181.txt:system/etc/wifi/40181/nvram.txt

endif

ifeq ($(WIFI_AP6xxx_MODULE),AP6210)

PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/AP6210/Wi-Fi/fw_bcm40181a2.bin:system/etc/wifi/40181/fw_bcm40181a2.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/AP6210/Wi-Fi/fw_bcm40181a2_apsta.bin:system/etc/wifi/40181/fw_bcm40181a2_apsta.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/AP6210/Wi-Fi/fw_bcm40181a2_p2p.bin:system/etc/wifi/40181/fw_bcm40181a2_p2p.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/AP6210/Wi-Fi/nvram_ap6210.txt:system/etc/wifi/40181/nvram.txt

endif

ifeq ($(WIFI_AP6xxx_MODULE),AP6476)

PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/AP6476/Wi-Fi/fw_bcm40181a2.bin:system/etc/wifi/40181/fw_bcm40181a2.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/AP6476/Wi-Fi/fw_bcm40181a2_apsta.bin:system/etc/wifi/40181/fw_bcm40181a2_apsta.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/AP6476/Wi-Fi/fw_bcm40181a2_p2p.bin:system/etc/wifi/40181/fw_bcm40181a2_p2p.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/AP6476/Wi-Fi/nvram_ap6476.txt:system/etc/wifi/40181/nvram.txt

endif

ifeq ($(WIFI_AP6xxx_MODULE),AP6493)

PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/AP6493/Wi-Fi/fw_bcm40183b2.bin:system/etc/wifi/40183/fw_bcm40183b2.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/AP6493/Wi-Fi/fw_bcm40183b2_apsta.bin:system/etc/wifi/40183/fw_bcm40183b2_apsta.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/AP6493/Wi-Fi/fw_bcm40183b2_p2p.bin:system/etc/wifi/40183/fw_bcm40183b2_p2p.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/AP6493/Wi-Fi/nvram_ap6493.txt:system/etc/wifi/40183/nvram.txt

endif

ifeq ($(WIFI_AP6xxx_MODULE),AP6330)

PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/AP6330/Wi-Fi/fw_bcm40183b2.bin:system/etc/wifi/40183/fw_bcm40183b2.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/AP6330/Wi-Fi/fw_bcm40183b2_apsta.bin:system/etc/wifi/40183/fw_bcm40183b2_apsta.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/AP6330/Wi-Fi/fw_bcm40183b2_p2p.bin:system/etc/wifi/40183/fw_bcm40183b2_p2p.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/AP6330/Wi-Fi/nvram_ap6330.txt:system/etc/wifi/40183/nvram.txt

endif
ifeq ($(MULTI_WIFI_SUPPORT), true)

WIFI_DRIVER_MODULE_PATH := /system/lib/
WIFI_DRIVER_MODULE_NAME := dhd

WPA_SUPPLICANT_VERSION			:= VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER	:= NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_multi
BOARD_HOSTAPD_PRIVATE_LIB   := lib_driver_cmd_multi
BOARD_HOSTAPD_DRIVER				:= NL80211

WIFI_DRIVER_FW_PATH_PARAM   := "/sys/module/dhd/parameters/firmware_path"
PRODUCT_COPY_FILES += \
        frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml
PRODUCT_PROPERTY_OVERRIDES += \
        wifi.interface=wlan0

PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/6212/fw_bcm43438a0.bin:system/etc/wifi/6212/fw_bcm43438a0.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/6212/fw_bcm43438a0_apsta.bin:system/etc/wifi/6212/fw_bcm43438a0_apsta.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/6212/fw_bcm43438a0_p2p.bin:system/etc/wifi/6212/fw_bcm43438a0_p2p.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/6212/nvram.txt:system/etc/wifi/6212/nvram.txt
PRODUCT_COPY_FILES += device/amlogic/p200/wifi/config.txt:system/etc/wifi/6212/config.txt
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/62x2/fw_bcm43241b4_ag.bin:system/etc/wifi/62x2/fw_bcm43241b4_ag.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/62x2/fw_bcm43241b4_ag_apsta.bin:system/etc/wifi/62x2/fw_bcm43241b4_ag_apsta.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/62x2/fw_bcm43241b4_ag_p2p.bin:system/etc/wifi/62x2/fw_bcm43241b4_ag_p2p.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/62x2/nvram.txt:system/etc/wifi/62x2/nvram.txt
PRODUCT_COPY_FILES += device/amlogic/p200/wifi/config.txt:system/etc/wifi/62X2/config.txt
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/6335/fw_bcm4339a0_ag.bin:system/etc/wifi/6335/fw_bcm4339a0_ag.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/6335/fw_bcm4339a0_ag_apsta.bin:system/etc/wifi/6335/fw_bcm4339a0_ag_apsta.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/6335/fw_bcm4339a0_ag_p2p.bin:system/etc/wifi/6335/fw_bcm4339a0_ag_p2p.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/6335/nvram.txt:system/etc/wifi/6335/nvram.txt
PRODUCT_COPY_FILES += device/amlogic/p200/wifi/config.txt:system/etc/wifi/6335/config.txt
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/4356/fw_bcm4356a2_ag.bin:system/etc/wifi/4356/fw_bcm4356a2_ag.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/4356/fw_bcm4356a2_ag_apsta.bin:system/etc/wifi/4356/fw_bcm4356a2_ag_apsta.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/4356/fw_bcm4356a2_ag_p2p.bin:system/etc/wifi/4356/fw_bcm4356a2_ag_p2p.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/4356/nvram_ap6356.txt:system/etc/wifi/4356/nvram_ap6356.txt
PRODUCT_COPY_FILES += device/amlogic/p200/wifi/config.txt:system/etc/wifi/4356/config.txt
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/4354/fw_bcm4354a1_ag.bin:system/etc/wifi/4354/fw_bcm4354a1_ag.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/4354/fw_bcm4354a1_ag_apsta.bin:system/etc/wifi/4354/fw_bcm4354a1_ag_apsta.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/4354/fw_bcm4354a1_ag_p2p.bin:system/etc/wifi/4354/fw_bcm4354a1_ag_p2p.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/4354/nvram_ap6354.txt:system/etc/wifi/4354/nvram_ap6354.txt
PRODUCT_COPY_FILES += device/amlogic/p200/wifi/config.txt:system/etc/wifi/4354/config.txt
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/43458/fw_bcm43455c0_ag.bin:system/etc/wifi/43458/fw_bcm43455c0_ag.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/43458/fw_bcm43455c0_ag_apsta.bin:system/etc/wifi/43458/fw_bcm43455c0_ag_apsta.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/43458/fw_bcm43455c0_ag_p2p.bin:system/etc/wifi/43458/fw_bcm43455c0_ag_p2p.bin
PRODUCT_COPY_FILES += hardware/amlogic/wifi/bcm_ampak/config/43458/nvram_43458.txt:system/etc/wifi/43458/nvram_43458.txt
PRODUCT_COPY_FILES += device/amlogic/p200/wifi/config.txt:system/etc/wifi/43458/config.txt
PRODUCT_COPY_FILES += device/amlogic/common/init.amlogic.wifi.rc:root/init.amlogic.wifi.rc
PRODUCT_COPY_FILES += hardware/amlogic/wifi/multi_wifi/config/bcm_supplicant.conf:system/etc/wifi/bcm_supplicant.conf
PRODUCT_COPY_FILES += hardware/amlogic/wifi/multi_wifi/config/bcm_supplicant_overlay.conf:system/etc/wifi/bcm_supplicant_overlay.conf
PRODUCT_COPY_FILES += hardware/amlogic/wifi/multi_wifi/config/wpa_supplicant.conf:system/etc/wifi/wpa_supplicant.conf
PRODUCT_COPY_FILES += hardware/amlogic/wifi/multi_wifi/config/wpa_supplicant_overlay.conf:system/etc/wifi/wpa_supplicant_overlay.conf
PRODUCT_COPY_FILES += hardware/amlogic/wifi/multi_wifi/config/p2p_supplicant_overlay.conf:system/etc/wifi/p2p_supplicant_overlay.conf
PRODUCT_COPY_FILES += hardware/amlogic/wifi/mediatek/iwpriv:system/bin/iwpriv
PRODUCT_COPY_FILES += hardware/amlogic/wifi/mediatek/RT2870STA_7601.dat:system/etc/wifi/RT2870STA_7601.dat
PRODUCT_COPY_FILES += hardware/amlogic/wifi/mediatek/mt7601usta.ko:system/lib/mt7601usta.ko
PRODUCT_COPY_FILES += hardware/amlogic/wifi/mediatek/RT2870STA_7601.dat:system/etc/wifi/RT2870STA_7603.dat
PRODUCT_COPY_FILES += hardware/amlogic/wifi/mediatek/mt7603usta.ko:system/lib/mt7603usta.ko
PRODUCT_COPY_FILES += hardware/amlogic/wifi/mediatek/mtprealloc.ko:system/lib/mtprealloc.ko
PRODUCT_COPY_FILES += hardware/amlogic/wifi/mediatek/dhcpcd.conf:system/etc/dhcpcd/dhcpcd.conf
endif
