IMGPACK := $(BUILD_OUT_EXECUTABLES)/logo_img_packer$(BUILD_EXECUTABLE_SUFFIX)
TARGET_PRODUCT_DIR := device/amlogic/$(TARGET_PRODUCT)
PRODUCT_UPGRADE_OUT := $(PRODUCT_OUT)/upgrade

BUILT_IMAGES := system.img userdata.img cache.img
ifeq ($(PRODUCT_BUILD_SECURE_BOOT_IMAGE_DIRECTLY),true)
	BUILT_IMAGES += boot.img.encrypt recovery.img.encrypt u-boot.bin.encrypt
else
	BUILT_IMAGES += boot.img recovery.img u-boot.bin
endif#ifeq ($(PRODUCT_BUILD_SECURE_BOOT_IMAGE_DIRECTLY),true)

ifdef KERNEL_DEVICETREE
DTBTOOL := vendor/amlogic/tools/dtbTool

ifdef KERNEL_DEVICETREE_CUSTOMER_DIR
KERNEL_DEVICETREE_DIR := $(KERNEL_DEVICETREE_CUSTOMER_DIR)
else
KERNEL_DEVICETREE_DIR := arch/$(KERNEL_ARCH)/boot/dts/
endif

KERNEL_DEVICETREE_SRC := $(addprefix $(KERNEL_ROOTDIR)/$(KERNEL_DEVICETREE_DIR), $(KERNEL_DEVICETREE) )
KERNEL_DEVICETREE_SRC := $(wildcard $(addsuffix .dtd, $(KERNEL_DEVICETREE_SRC)) $(addsuffix .dts, $(KERNEL_DEVICETREE_SRC)))

KERNEL_DEVICETREE_BIN := $(addprefix $(KERNEL_OUT)/$(KERNEL_DEVICETREE_DIR), $(KERNEL_DEVICETREE))
KERNEL_DEVICETREE_BIN := $(addsuffix .dtb, $(KERNEL_DEVICETREE_BIN))

INSTALLED_BOARDDTB_TARGET := $(PRODUCT_OUT)/dtb.img

$(INSTALLED_BOARDDTB_TARGET) : $(KERNEL_DEVICETREE_SRC) $(INSTALLED_KERNEL_TARGET)
	$(foreach aDts, $(KERNEL_DEVICETREE), \
		if [ -f "$(KERNEL_ROOTDIR)/$(KERNEL_DEVICETREE_DIR)/$(aDts).dtd" ]; then \
			$(MAKE) -C $(KERNEL_ROOTDIR) O=../$(KERNEL_OUT) ARCH=$(KERNEL_ARCH) CROSS_COMPILE=$(PREFIX_CROSS_COMPILE) $(strip $(aDts)).dtd; \
		fi;\
		$(MAKE) -C $(KERNEL_ROOTDIR) O=../$(KERNEL_OUT) ARCH=$(KERNEL_ARCH) CROSS_COMPILE=$(PREFIX_CROSS_COMPILE) $(strip $(aDts)).dtb; \
	)
ifneq ($(strip $(word 2, $(KERNEL_DEVICETREE)) ),)
	$(hide) $(DTBTOOL) -o $@ -p $(KERNEL_OUT)/scripts/dtc/ $(KERNEL_OUT)/$(KERNEL_DEVICETREE_DIR)
else# elif dts num == 1
	cp -f $(KERNEL_DEVICETREE_BIN) $@
endif
	@echo "Instaled $@"

.PHONY: dtbimage
dtbimage: $(INSTALLED_BOARDDTB_TARGET)

else
INSTALLED_BOARDDTB_TARGET	   :=
endif # ifdef KERNEL_DEVICETREE


UPGRADE_FILES := \
        aml_sdc_burn.ini \
        ddr_init.bin \
	u-boot.bin.sd.bin  u-boot.bin.usb.bl2 u-boot.bin.usb.tpl \
        u-boot-comp.bin

ifneq ($(TARGET_USE_SECURITY_MODE),true)
UPGRADE_FILES += \
        platform.conf \
        aml_upgrade_package.conf
else # secureboot mode
UPGRADE_FILES += \
        u-boot-usb.bin.aml \
        platform_enc.conf \
        aml_upgrade_package_enc.conf
endif

UPGRADE_FILES := $(addprefix $(TARGET_DEVICE_DIR)/upgrade/,$(UPGRADE_FILES))
UPGRADE_FILES := $(wildcard $(UPGRADE_FILES)) #extract only existing files for burnning

ifneq ($(TARGET_AMLOGIC_RES_PACKAGE),)
INSTALLED_AML_LOGO := $(PRODUCT_UPGRADE_OUT)/logo.img
$(INSTALLED_AML_LOGO): $(IMGPACK) $(shell find $(TARGET_AMLOGIC_RES_PACKAGE) -type f)
	@echo "generate $(INSTALLED_AML_LOGO)"
	$(hide) mkdir -p $(PRODUCT_UPGRADE_OUT)/logo
	$(hide) rm -rf $(PRODUCT_UPGRADE_OUT)/logo/*
	@cp -rf $(TARGET_AMLOGIC_RES_PACKAGE)/* $(PRODUCT_UPGRADE_OUT)/logo
	$(hide) $(foreach bmpf, $(wildcard $(TARGET_AMLOGIC_RES_PACKAGE)/*.bmp), \
		$(eval targetBmpSz := $(shell stat -L -c %s $(bmpf))) \
		if [ $(targetBmpSz) -gt 524288 ]; then \
			echo "gzip -c $(bmpf) > $(PRODUCT_UPGRADE_OUT)/logo/$(notdir $(bmpf))" ;\
			gzip -c $(bmpf) > $(PRODUCT_UPGRADE_OUT)/logo/$(notdir $(bmpf)); \
		fi; \
		)
	$(hide) $(IMGPACK) -r $(PRODUCT_UPGRADE_OUT)/logo $@
	@echo "Installed $@"
else
INSTALLED_AML_LOGO :=
endif

.PHONY: logoimg
logoimg: $(INSTALLED_AML_LOGO)

ifneq ($(BOARD_AUTO_COLLECT_MANIFEST),false)
BUILD_TIME := $(shell date +%Y-%m-%d--%H-%M)
INSTALLED_MANIFEST_XML := $(PRODUCT_OUT)/manifests/manifest-$(BUILD_TIME).xml
$(INSTALLED_MANIFEST_XML):
	$(hide) mkdir -p $(PRODUCT_OUT)/manifests
	$(hide) mkdir -p $(PRODUCT_OUT)/upgrade
	repo manifest -r -o $(INSTALLED_MANIFEST_XML)
	$(hide) cp $(INSTALLED_MANIFEST_XML) $(PRODUCT_OUT)/upgrade/manifest.xml

.PHONY:build_manifest
build_manifest:$(INSTALLED_MANIFEST_XML)
else
INSTALLED_MANIFEST_XML :=
endif

ifeq ($(TARGET_SUPPORT_USB_BURNING_V2),true)
INSTALLED_AML_UPGRADE_PACKAGE_TARGET := $(PRODUCT_OUT)/aml_upgrade_package.img

ifeq ($(TARGET_USE_SECURITY_MODE),true)
  PACKAGE_CONFIG_FILE := $(PRODUCT_UPGRADE_OUT)/aml_upgrade_package_enc.conf
else
  PACKAGE_CONFIG_FILE := $(PRODUCT_UPGRADE_OUT)/aml_upgrade_package.conf
endif

ifeq ($(TARGET_USE_SECURITY_DM_VERITY_MODE_WITH_TOOL),true)
  SYSTEMIMG_INTERMEDIATES := $(PRODUCT_OUT)/obj/PACKAGING/systemimage_intermediates
  SYSTEMIMG_INTERMEDIATES := $(SYSTEMIMG_INTERMEDIATES)/verity_table.bin $(SYSTEMIMG_INTERMEDIATES)/verity.img
  define security_dm_verity_conf
	  @echo "copy verity.img and verity_table.bin"
	  @sed -i "/verity_table.bin/d" $(PACKAGE_CONFIG_FILE)
	  @sed -i "/verity.img/d" $(PACKAGE_CONFIG_FILE)
	  $(hide) \
		sed -i "/aml_sdc_burn\.ini/ s/.*/&\nfile=\"verity.img\"\t\tmain_type=\"img\"\t\tsub_type=\"verity\"/" $(PACKAGE_CONFIG_FILE); \
		sed -i "/aml_sdc_burn\.ini/ s/.*/&\nfile=\"verity_table.bin\"\t\tmain_type=\"bin\"\t\tsub_type=\"verity_table\"/" $(PACKAGE_CONFIG_FILE);
	  cp $(SYSTEMIMG_INTERMEDIATES) $(PRODUCT_UPGRADE_OUT)/
  endef #define security_dm_verity_conf
endif # ifeq ($(TARGET_USE_SECURITY_DM_VERITY_MODE_WITH_TOOL),true)

ifeq ($(PRODUCT_BUILD_SECURE_BOOT_IMAGE_DIRECTLY),true)
define aml-secureboot-sign-bootloader
	@echo -----aml-secureboot-sign-bootloader ------
	$(hide) $(PRODUCT_AML_SECUREBOOT_SIGNBOOTLOADER) --input $(basename $(1)) --output $(1)
	@echo ----- Made aml secure-boot singed bootloader: $(1) --------
endef #define aml-secureboot-sign-bootloader
define aml-secureboot-sign-kernel
	@echo -----aml-secureboot-sign-kernel ------
	$(hide) mv -f $(1) $(basename $(1))
	$(hide) $(PRODUCT_AML_SECUREBOOT_SIGNIMAGE) --input $(basename $(1)) --output $(1)
	@echo ----- Made aml secure-boot singed bootloader: $(1) --------
endef #define aml-secureboot-sign-kernel
endif# ifeq ($(PRODUCT_BUILD_SECURE_BOOT_IMAGE_DIRECTLY),true)

.PHONY:aml_upgrade
aml_upgrade:$(INSTALLED_AML_UPGRADE_PACKAGE_TARGET)
$(INSTALLED_AML_UPGRADE_PACKAGE_TARGET): \
	$(addprefix $(PRODUCT_OUT)/,$(BUILT_IMAGES)) \
	$(UPGRADE_FILES) \
	$(INSTALLED_AML_LOGO) \
	$(INSTALLED_BOARDDTB_TARGET) \
	$(INSTALLED_MANIFEST_XML) \
	$(TARGET_USB_BURNING_V2_DEPEND_MODULES)
	mkdir -p $(PRODUCT_UPGRADE_OUT)
ifneq ($(TARGET_USE_SECURITY_MODE),true)
	$(hide) $(foreach file,$(UPGRADE_FILES), \
			echo cp $(file) $(PRODUCT_UPGRADE_OUT)/$(notdir $(file)); \
			cp -f $(file) $(PRODUCT_UPGRADE_OUT)/$(notdir $(file)); \
		)
else # secureboot mode
	$(hide) $(foreach file,$(UPGRADE_FILES), \
		echo cp $(file) $(PRODUCT_UPGRADE_OUT)/$(notdir $(file)); \
		cp -f $(file) $(PRODUCT_UPGRADE_OUT)/$(notdir $(file)); \
		if [ "$(file)" == "ddr_init.bin" ]; then \
			echo cp $(file) $(PRODUCT_UPGRADE_OUT)/DDR_ENC.USB; \
			cp $(file) $(PRODUCT_UPGRADE_OUT)/DDR_ENC.USB; \
		fi; \
		)
	-cp $(TARGET_DEVICE_DIR)/u-boot.bin.aml $(PRODUCT_UPGRADE_OUT)
endif
	$(hide) $(foreach file,$(BUILT_IMAGES), \
		echo ln -s $(PRODUCT_OUT)/$(file) $(PRODUCT_UPGRADE_OUT)/$(file); \
		rm -f $(PRODUCT_UPGRADE_OUT)/$(file); \
		ln -s $(ANDROID_BUILD_TOP)/$(PRODUCT_OUT)/$(file) $(PRODUCT_UPGRADE_OUT)/$(file); \
		)
	ln -sf $(ANDROID_BUILD_TOP)/$(INSTALLED_BOARDDTB_TARGET) $(PRODUCT_UPGRADE_OUT)/meson.dtb
ifeq ($(PRODUCT_BUILD_SECURE_BOOT_IMAGE_DIRECTLY),true)
	$(hide) rm -f $(PRODUCT_UPGRADE_OUT)/u-boot.bin.encrypt.*
	$(hide) rm -f $(PACKAGE_CONFIG_FILE)
	$(hide) $(ACP) $(PRODUCT_OUT)/u-boot.bin.encrypt.* $(PRODUCT_UPGRADE_OUT)/
	$(hide) $(ACP) $(TARGET_DEVICE_DIR)/upgrade/aml_upgrade_package_enc.conf $(PACKAGE_CONFIG_FILE)
endif#ifneq ($(TARGET_USE_SECURITY_MODE),true)
	$(update-aml_upgrade-conf)
	$(security_dm_verity_conf)
	@echo "Package: $@"
	@echo ./vendor/amlogic/tools/aml_upgrade/aml_image_v2_packer -r \
		$(PACKAGE_CONFIG_FILE) \
		$(PRODUCT_UPGRADE_OUT)/ \
		$(INSTALLED_AML_UPGRADE_PACKAGE_TARGET)
	$(hide) ./vendor/amlogic/tools/aml_upgrade/aml_image_v2_packer -r \
		$(PACKAGE_CONFIG_FILE) \
		$(PRODUCT_UPGRADE_OUT)/ \
		$(INSTALLED_AML_UPGRADE_PACKAGE_TARGET)
	@echo " $@ installed"
else
#none
INSTALLED_AML_UPGRADE_PACKAGE_TARGET :=
endif

droidcore: $(INSTALLED_AML_UPGRADE_PACKAGE_TARGET) $(INSTALLED_MANIFEST_XML)
otapackage: $(INSTALLED_AML_UPGRADE_PACKAGE_TARGET) $(INSTALLED_MANIFEST_XML)

