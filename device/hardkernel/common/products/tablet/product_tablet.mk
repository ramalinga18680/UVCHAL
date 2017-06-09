$(call inherit-product, device/amlogic/common/core_amlogic.mk)

PRODUCT_PACKAGES += \
    camera.amlogic \
    sensors.amlogic \
    busybox \
    utility_busybox

PRODUCT_PACKAGES += \
    HdmiSwitch \
    Calculator \
    Calendar \
    Email \
    PicoTts \
    PrintSpooler \
    QuickSearchBox \
    Telecom \
    TeleService \
    MmsService \
    DownloadProviderUi

#USB PM
PRODUCT_PACKAGES += \
    usbtestpm \
    usbpower

# USB
PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.usb.host.xml:system/etc/permissions/android.hardware.usb.host.xml \
    frameworks/native/data/etc/android.hardware.usb.accessory.xml:system/etc/permissions/android.hardware.usb.accessory.xml

#copy lowmemorykiller.txt
ifeq ($(BUILD_WITH_LOWMEM_COMMON_CONFIG),true)
PRODUCT_COPY_FILES += \
	device/amlogic/common/config/lowmemorykiller_2G.txt:system/etc/lowmemorykiller_2G.txt \
	device/amlogic/common/config/lowmemorykiller.txt:system/etc/lowmemorykiller.txt \
	device/amlogic/common/config/lowmemorykiller_512M.txt:system/etc/lowmemorykiller_512M.txt
endif
