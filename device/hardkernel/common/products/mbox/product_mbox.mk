$(call inherit-product, device/hardkernel/common/core_odroid.mk)


#    imageserver 
PRODUCT_PACKAGES += \
    busybox \
    utility_busybox

# DLNA
PRODUCT_PACKAGES += \
    DLNA

PRODUCT_PACKAGES += \
    remotecfg

#USE_CUSTOM_AUDIO_POLICY := 1

# NativeImagePlayer
PRODUCT_PACKAGES += \
    NativeImagePlayer

#RemoteControl Service
PRODUCT_PACKAGES += \
    RC_Service

# Camera Hal
PRODUCT_PACKAGES += \
    camera.$(TARGET_PRODUCT)

# HDMITX CEC HAL
PRODUCT_PACKAGES += \
    hdmi_cec.$(TARGET_PRODUCT)

PRODUCT_PROPERTY_OVERRIDES += ro.hdmi.device_type=4

#Tvsettings
#PRODUCT_PACKAGES += \
    TvSettings

#MboxLauncher
#PRODUCT_PACKAGES += \
    MboxLauncher

#USB PM
PRODUCT_PACKAGES += \
    usbtestpm \
    usbpower

PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.software.app_widgets.xml:system/etc/permissions/android.software.app_widgets.xml \
    frameworks/native/data/etc/android.software.backup.xml:system/etc/permissions/android.software.backup.xml \
    frameworks/native/data/etc/android.software.device_admin.xml:system/etc/permissions/android.software.device_admin.xml \
    frameworks/native/data/etc/android.hardware.hdmi.cec.xml:system/etc/permissions/android.hardware.hdmi.cec.xml

#copy lowmemorykiller.txt
ifeq ($(BUILD_WITH_LOWMEM_COMMON_CONFIG),true)
PRODUCT_COPY_FILES += \
	device/hardkernel/common/config/lowmemorykiller_2G.txt:system/etc/lowmemorykiller_2G.txt \
	device/hardkernel/common/config/lowmemorykiller.txt:system/etc/lowmemorykiller.txt \
	device/hardkernel/common/config/lowmemorykiller_512M.txt:system/etc/lowmemorykiller_512M.txt
endif

# USB
PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.usb.host.xml:system/etc/permissions/android.hardware.usb.host.xml \
    frameworks/native/data/etc/android.hardware.usb.accessory.xml:system/etc/permissions/android.hardware.usb.accessory.xml

custom_keylayouts := $(wildcard $(LOCAL_PATH)/keyboards/*.kl)
PRODUCT_COPY_FILES += $(foreach file,$(custom_keylayouts),\
    $(file):system/usr/keylayout/$(notdir $(file)))

# hdcp_tx22
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/../../hdcp_tx22/hdcp_tx22:system/bin/hdcp_tx22

# bootanimation
#PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/bootanimation.zip:system/media/bootanimation.zip

#PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/mbox.mp4:system/etc/bootvideo

# default wallpaper for mbox to fix bug 106225
#PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/default_wallpaper.png:system/etc/default_wallpaper.png

#ADDITIONAL_BUILD_PROPERTIES += \
    ro.config.wallpaper=/system/etc/default_wallpaper.png

DISPLAY_BUILD_NUMBER := true
