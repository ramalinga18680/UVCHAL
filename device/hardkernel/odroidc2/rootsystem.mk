#
# Copyright (C) 2015 Hardkernel Co,. Ltd.
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

.PHONY: $(PRODUCT_OUT)/rootsystem2/fstab.$(TARGET_PRODUCT)
$(PRODUCT_OUT)/rootsystem2/fstab.$(TARGET_PRODUCT): $(PRODUCT_OUT)/rootsystem2
	sed -i "`grep -nE '/system.ext4' $@ | cut -d : -f 1` s/ro/rw/" $@

.PHONY: $(PRODUCT_OUT)/rootsystem2
$(PRODUCT_OUT)/rootsystem2: droidcore
	echo combine the directories system/ and root/ into rootsystem2/.
	rm -rf $@ && mkdir -p $@
	cp -arp $(PRODUCT_OUT)/root/* $@
	cp -arp $(PRODUCT_OUT)/system/* $@
	mv $(PRODUCT_OUT)/rootsystem2/init $(PRODUCT_OUT)/rootsystem2/bin/
	mv $(PRODUCT_OUT)/rootsystem2/sbin/healthd $(PRODUCT_OUT)/rootsystem2/bin/
	mv $(PRODUCT_OUT)/rootsystem2/sbin/adbd $(PRODUCT_OUT)/rootsystem2/bin/
	ln -sf /bin/init $(PRODUCT_OUT)/rootsystem2/init
	ln -sf /bin/healthd $(PRODUCT_OUT)/rootsystem2/sbin/healthd
	ln -sf /bin/adbd $(PRODUCT_OUT)/rootsystem2/sbin/adbd
	sed -i "s,^ro.secure=*,ro.secure=0,g" $(PRODUCT_OUT)/rootsystem2/default.prop
#	mkdir -p $(PRODUCT_OUT)/rootsystem2/cache
#	mkdir -p $(PRODUCT_OUT)/rootsystem2/mnt/shell/emulated

$(PRODUCT_OUT)/rootsystem.img: $(PRODUCT_OUT)/rootsystem2 \
	$(PRODUCT_OUT)/rootsystem2/fstab.$(TARGET_PRODUCT)
	echo Creating Android single root file system.
	$(MAKE_EXT4FS) -s -l $(BOARD_SYSTEMIMAGE_PARTITION_SIZE) \
		-L rootsystem -a system $@ $<

.PHONY: rootsystem
rootsystem: $(PRODUCT_OUT)/rootsystem.img
