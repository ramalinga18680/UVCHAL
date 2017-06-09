#
# Copyright (C) 2015 The Android Open Source Project
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

MALI=hardware/arm/gpu/mali
MALI_OUT=hardware/arm/gpu/mali
KERNEL_ARCH ?= arm

define gpu-modules

$(MALI_KO):
	@echo "make mali module KERNEL_ARCH is $(KERNEL_ARCH)"
	$(MAKE) -C $(PRODUCT_OUT)/obj/KERNEL_OBJ M=$(shell pwd)/$(MALI)		\
	ARCH=$(KERNEL_ARCH) CROSS_COMPILE=$(PREFIX_CROSS_COMPILE) CONFIG_MALI400=m  CONFIG_MALI450=m 	\
	CONFIG_GPU_THERMAL=y CONFIG_AM_VDEC_H264_4K2K=y modules

	mkdir -p $(PRODUCT_OUT)/root/boot
	cp $(MALI_OUT)/mali.ko $(PRODUCT_OUT)/root/boot
endef

define ump-modules
$(UMP_KO):
	@echo "make ump module"
	$(MAKE) -C $(PRODUCT_OUT)/obj/KERNEL_OBJ M=$(shell pwd)/$(UMP)     	\
	ARCH=arm CROSS_COMPILE=$(PREFIX_CROSS_COMPILE) CONFIG_MALI400=m CONFIG_MALI450=m	\
	CONFIG_GPU_THERMAL=y CONFIG_AM_VDEC_H264_4K2K=y modules

	mkdir -p $(PRODUCT_OUT)/root/boot
	cp $(UMP_OUT)/ump.ko $(PRODUCT_OUT)/root/boot
endef
