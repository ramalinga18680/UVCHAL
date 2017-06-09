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

"""Emit extra commands needed for Group during OTA installation
(installing the bootloader)."""

import struct
import common

def SetBootloaderEnv(script, name, val):
  """Set bootloader env name with val."""
  script.AppendExtra('set_bootloader_env("%s", "%s");' % (name, val))

def GetBuildProp(prop, info_dict):
  """Return the fingerprint of the build of a given target-files info_dict."""
  try:
    return info_dict.get("build.prop", {})[prop]
  except KeyError:
    raise common.ExternalError("couldn't find %s in build.prop" % (prop,))

def HasTargetImage(target_files_zip, image_path):
  try:
    target_files_zip.getinfo(image_path)
    return True
  except KeyError:
    return False

def FullOTA_Assertions(info):
  print "amlogic extensions:FullOTA_Assertions"

def FullOTA_InstallBegin(info):
  print "amlogic extensions:FullOTA_InstallBegin"
  platform = GetBuildProp("ro.board.platform", info.info_dict)
  print "ro.board.platform: %s" % (platform)
  if "meson3" in platform:
    SetBootloaderEnv(info.script, "upgrade_step", "0")
  elif "meson6" in platform:
    SetBootloaderEnv(info.script, "upgrade_step", "0")
  else:
    SetBootloaderEnv(info.script, "upgrade_step", "3")

def FullOTA_InstallEnd(info):
  print "amlogic extensions:FullOTA_InstallEnd"
  bootloader_img_exist = 0
  try:
    bootloader_img_info = info.input_zip.getinfo("BOOTLOADER/bootloader")
    bootloader_img_exist = 1
    bootloader_img = common.File("bootloader.img", info.input_zip.read("BOOTLOADER/bootloader"));
  except KeyError:
    print 'WARNING: No BOOTLOADER found'

  if bootloader_img_exist:
    common.CheckSize(bootloader_img.data, "bootloader.img", info.info_dict)
    common.ZipWriteStr(info.output_zip, "bootloader.img", bootloader_img.data)
    info.script.WriteRawImage("/bootloader", "bootloader.img")
    SetBootloaderEnv(info.script, "upgrade_step", "1")
  else:
    SetBootloaderEnv(info.script, "upgrade_step", "1")

  SetBootloaderEnv(info.script, "force_auto_update", "false")


def IncrementalOTA_VerifyBegin(info):
  print "amlogic extensions:IncrementalOTA_VerifyBegin"

def IncrementalOTA_VerifyEnd(info):
  print "amlogic extensions:IncrementalOTA_VerifyEnd"

def IncrementalOTA_InstallBegin(info):
  print "amlogic extensions:IncrementalOTA_InstallBegin"

def IncrementalOTA_InstallEnd(info):
  print "amlogic extensions:IncrementalOTA_InstallEnd"
  source_bootloader = False; target_bootloader = False; updating_bootloader = False;
  if HasTargetImage(info.source_zip, "BOOTLOADER/bootloader"):
    source_bootloader = common.File("bootloader.img", info.source_zip.read("BOOTLOADER/bootloader"));

  if HasTargetImage(info.target_zip, "BOOTLOADER/bootloader"):
    target_bootloader = common.File("bootloader.img", info.target_zip.read("BOOTLOADER/bootloader"));

  if target_bootloader:
    if source_bootloader:
      updating_bootloader = (source_bootloader.data != target_bootloader.data);
    else:
      updating_bootloader = 1;

  if updating_bootloader:
    SetBootloaderEnv(info.script, "upgrade_step", "1")