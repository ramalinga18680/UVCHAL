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
 *  - 1 IR remote
 */

#define LOG_TAG "remotecfg"
//#define LOG_NDEBUG 0

#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <memory.h>
#include <utils/Log.h>
#include "remote_config.h"
#include "keydefine.h"

#define FACTCUSTCODE_MAX        20
#define DEVICE_NAME             "/dev/amremote"
#define DEVICE_KP               "/dev/am_adc_kpd"

unsigned short key_map[256], repeat_key_map[256], mouse_map[4];
unsigned int factory_customercode_map[FACTCUSTCODE_MAX];

unsigned short default_key_map[256] = {
KEY_0, KEY_1, KEY_2, KEY_3, KEY_4, KEY_5, KEY_6, KEY_7, /*0~7*/
KEY_8, KEY_9, KEY_BACK, KEY_UP, KEY_BACKSPACE, KEY_ENTER, KEY_DOWN, KEY_MENU, /*8~f*/
KEY_LEFT, KEY_RIGHT, KEY_R, KEY_S, KEY_U, KEY_G, KEY_K, KEY_L, /*10~17*/
KEY_M, KEY_N, KEY_D, KEY_T, KEY_H, KEY_B, KEY_I, KEY_J, /*18~1f*/
KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, /*20~27*/
KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, /*28~2f*/
KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, /*30~37*/
KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, /*38~3f*/

KEY_C, KEY_F, KEY_E, KEY_P, KEY_V, KEY_A, KEY_Q, KEY_O, /*40~47*/
KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, /*48~4f*/
KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_W, KEY_Z, KEY_RESERVED, KEY_RESERVED, KEY_Y, /*50~57*/
KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_X, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, /*58~5f*/
KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, /*60~67*/
KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, /*68~6f*/
KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, /*70~77*/
KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, /*78~7f*/

KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, /*80~87*/
KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, /*88~8f*/
KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, /*90~97*/
KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, /*98~9f*/
KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, /*a0~a7*/
KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, /*a8~af*/
KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, /*b0~b7*/
KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, /*b8~bf*/

KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, /*c0~c7*/
KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, /*c8~cf*/
KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, /*d0~d7*/
KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, /*d8~df*/
KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, /*e0~e7*/
KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, /*e8~ef*/
KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, /*f0~f7*/
KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED, KEY_RESERVED /*f8~ff*/
};

unsigned short default_mouse_map[4] = {
 //0x10, 0x11, 0x0b, 0x0e
 0xffff, 0xffff, 0xffff, 0xffff
};

unsigned short adc_map[2] ={0xffff, 0xffff};//left,right
unsigned int adc_move_enable = 0;

#define ARRAY_SIZE(x) (sizeof(x) / sizeof((x)[0]))

int remoteinit(const char* path)
{
    int i;
    unsigned int val;
    remote_config_t *remote = NULL;
    int device_name_fd = -1;
    int device_kp_fd = -1;
    int ret = 0;

    for (i = 0; i < 256; i++)
        key_map[i] = KEY_RESERVED;
    for (i = 0; i < 256; i++)
        repeat_key_map[i] = KEY_RESERVED;
    for (i = 0; i < 4; i++)
        mouse_map[i] = 0xffff;
    remote = (remote_config_t *)malloc(sizeof(remote_config_t));
    if (!remote) {
        ALOGE("out of memory !\n");
        ret = -1;
        goto exit;
    }
    memset((unsigned char*)remote, 0xff, sizeof(remote_config_t));
    remote->key_map = key_map;
    remote->repeat_key_map = repeat_key_map;
    remote->mouse_map = mouse_map;
    remote->factory_customercode_map = factory_customercode_map;
    device_name_fd = open(DEVICE_NAME, O_RDWR);
    if (device_name_fd < 0) {
        ALOGE("Can't open %s .\n", DEVICE_NAME);
        ret = -2;
        goto exit;
    }

    FILE *fp = fopen(path, "r");
    if (!fp) {
        ALOGE("Open file %s is failed!!!\n", path);
        ret = -3;
        goto exit;
    }
    get_config_from_file(fp, remote);
    fclose(fp);

    remote->factory_code >>= 16;
    set_config(remote, device_name_fd);
    ioctl(device_name_fd, REMOTE_IOC_RESET_KEY_MAPPING, NULL);
    for (i = 0; i < 256; i++)
        if (key_map[i] != KEY_RESERVED) {
            val = (i<<16) | key_map[i];
            ioctl(device_name_fd, REMOTE_IOC_SET_KEY_MAPPING, &val);
        }

    for (i = 0; i < 256; i++)
        if (repeat_key_map[i] != KEY_RESERVED ) {
            val = (i<<16) | repeat_key_map[i];
            ioctl(device_name_fd, REMOTE_IOC_SET_REPEAT_KEY_MAPPING, &val);
        }

    for (i = 0; i < 4; i++)
        if (mouse_map[i] != 0xffff) {
            val = (i<<16) | mouse_map[i];
            ioctl(device_name_fd, REMOTE_IOC_SET_MOUSE_MAPPING, &val);
        }

    for (i = 0; i < FACTCUSTCODE_MAX; i++)
        if (factory_customercode_map[i] != 0xffffffff) {
           val = (i<<16) | factory_customercode_map[i];
            ioctl(device_name_fd, REMOTE_IOC_SET_FACTORY_CUSTOMCODE, &val);
        }

    device_kp_fd = open(DEVICE_KP, O_RDWR);
    if (device_kp_fd >= 0) {
        if (adc_move_enable != 0) {
            for (i = 0; i < (int)ARRAY_SIZE(adc_map); i++) {
                if (adc_map[i] != 0xffff) {
                    val = (i << 16) | adc_map[i];
                    ioctl(device_kp_fd, KEY_IOC_SET_MOVE_MAP, &val);
                    ALOGI("adc_map[%d] = %d ,val = %d \n", i, adc_map[i], val);
                }
            }
        }

        ioctl(device_kp_fd, KEY_IOC_SET_MOVE_ENABLE, &adc_move_enable);
        ALOGI("adc_move_enable = %d \n", adc_move_enable);
        close(device_kp_fd);
    }

exit:
    if (NULL != remote)
        free(remote);

    if (device_name_fd >= 0)
        close(device_name_fd);

    return ret;
}
