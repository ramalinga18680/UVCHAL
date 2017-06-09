/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  @author   Tellen Yu
 *  @version  2.0
 *  @date     2015/06/01
 *  @par function description:
 *  - 1 IR remote config set config to kernel driver
 */

#define LOG_TAG "remotecfg"
#define LOG_NDEBUG 0

#include <stdio.h>
#include <fcntl.h>
#include <utils/Log.h>
#include "remote_config.h"

int set_config(remote_config_t *remote, int device_fd)
{
    unsigned int i;
    unsigned int *para = (unsigned int*)&remote->repeat_delay;

    for (i = 0; i < ARRAY_SIZE(config_item); i++) {
        if (para[i] != 0xffffffff) {
            switch (i) {
                case 4:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                case 15:
                case 16:
                case 17:
                    ALOGV("%20s = 0x%x\n", config_item[i], para[i]);
                    break;
                default:
                    ALOGV("%20s = %d\n", config_item[i], para[i]);
                    break;
            }

            ioctl(device_fd, remote_ioc_table[i], &para[i]);
        }
    }
    return 0;
}
