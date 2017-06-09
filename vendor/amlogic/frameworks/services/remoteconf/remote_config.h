#ifndef  _REMOTE_CONFIG_H
#define  _REMOTE_CONFIG_H

#include <asm/ioctl.h>
typedef unsigned int u32;

#define REMOTE_IOC_UNFCODE_CONFIG                   _IOW('I',12, u32)
#define REMOTE_IOC_INFCODE_CONFIG                   _IOW('I',13, u32)
#define REMOTE_IOC_RESET_KEY_MAPPING                _IOW('I',3,u32)
#define REMOTE_IOC_SET_KEY_MAPPING                  _IOW('I',4,u32)
#define REMOTE_IOC_SET_MOUSE_MAPPING                _IOW('I',5,u32)
#define REMOTE_IOC_SET_REPEAT_DELAY                 _IOW('I',6,u32)
#define REMOTE_IOC_SET_REPEAT_PERIOD                _IOW('I',7,u32)

#define REMOTE_IOC_SET_REPEAT_ENABLE                _IOW('I',8,u32)
#define REMOTE_IOC_SET_DEBUG_ENABLE                 _IOW('I',9,u32)
#define REMOTE_IOC_SET_MODE                         _IOW('I',10,u32)
#define REMOTE_IOC_SET_MOUSE_SPEED                  _IOW('I',11,u32)

#define REMOTE_IOC_SET_REPEAT_KEY_MAPPING           _IOW('I',20,u32)
#define REMOTE_IOC_SET_RELEASE_FDELAY               _IOW('I',97,u32)
#define REMOTE_IOC_SET_RELEASE_SDELAY               _IOW('I',98,u32)
#define REMOTE_IOC_SET_RELEASE_DELAY                _IOW('I',99,u32)
#define REMOTE_IOC_SET_CUSTOMCODE                   _IOW('I',100,u32)
//reg
#define REMOTE_IOC_SET_REG_BASE_GEN                 _IOW('I',101,u32)
#define REMOTE_IOC_SET_REG_CONTROL                  _IOW('I',102,u32)
#define REMOTE_IOC_SET_REG_LEADER_ACT               _IOW('I',103,u32)
#define REMOTE_IOC_SET_REG_LEADER_IDLE              _IOW('I',104,u32)
#define REMOTE_IOC_SET_REG_REPEAT_LEADER            _IOW('I',105,u32)
#define REMOTE_IOC_SET_REG_BIT0_TIME                _IOW('I',106,u32)

//sw
#define REMOTE_IOC_SET_BIT_COUNT                    _IOW('I',107,u32)
#define REMOTE_IOC_SET_TW_LEADER_ACT                _IOW('I',108,u32)
#define REMOTE_IOC_SET_TW_BIT0_TIME                 _IOW('I',109,u32)
#define REMOTE_IOC_SET_TW_BIT1_TIME                 _IOW('I',110,u32)
#define REMOTE_IOC_SET_TW_REPEATE_LEADER            _IOW('I',111,u32)

#define REMOTE_IOC_GET_TW_LEADER_ACT                _IOR('I',112,u32)
#define REMOTE_IOC_GET_TW_BIT0_TIME                 _IOR('I',113,u32)
#define REMOTE_IOC_GET_TW_BIT1_TIME                 _IOR('I',114,u32)
#define REMOTE_IOC_GET_TW_REPEATE_LEADER            _IOR('I',115,u32)


#define REMOTE_IOC_GET_REG_BASE_GEN                 _IOR('I',121,u32)
#define REMOTE_IOC_GET_REG_CONTROL                  _IOR('I',122,u32)
#define REMOTE_IOC_GET_REG_LEADER_ACT               _IOR('I',123,u32)
#define REMOTE_IOC_GET_REG_LEADER_IDLE              _IOR('I',124,u32)
#define REMOTE_IOC_GET_REG_REPEAT_LEADER            _IOR('I',125,u32)
#define REMOTE_IOC_GET_REG_BIT0_TIME                _IOR('I',126,u32)
#define REMOTE_IOC_GET_REG_FRAME_DATA               _IOR('I',127,u32)
#define REMOTE_IOC_GET_REG_FRAME_STATUS             _IOR('I',128,u32)

#define REMOTE_IOC_SET_FN_KEY_SCANCODE              _IOW('I', 131, u32)
#define REMOTE_IOC_SET_LEFT_KEY_SCANCODE            _IOW('I', 132, u32)
#define REMOTE_IOC_SET_RIGHT_KEY_SCANCODE           _IOW('I', 133, u32)
#define REMOTE_IOC_SET_UP_KEY_SCANCODE              _IOW('I', 134, u32)
#define REMOTE_IOC_SET_DOWN_KEY_SCANCODE            _IOW('I', 135, u32)
#define REMOTE_IOC_SET_OK_KEY_SCANCODE              _IOW('I', 136, u32)
#define REMOTE_IOC_SET_PAGEUP_KEY_SCANCODE          _IOW('I', 137, u32)
#define REMOTE_IOC_SET_PAGEDOWN_KEY_SCANCODE        _IOW('I', 138, u32)

#define REMOTE_IOC_SET_TW_BIT2_TIME                 _IOW('I',129,u32)
#define REMOTE_IOC_SET_TW_BIT3_TIME                 _IOW('I',130,u32)
#define REMOTE_IOC_SET_FACTORY_CUSTOMCODE           _IOW('I',139,u32)
#define ADC_KP_MAGIC 'P'
#define KEY_IOC_SET_MOVE_MAP                        _IOW(ADC_KP_MAGIC,0X02,int)
#define KEY_IOC_SET_MOVE_ENABLE                     _IOW(ADC_KP_MAGIC,0X03,int)

#define ARRAY_SIZE(x) (sizeof(x) / sizeof((x)[0]))

typedef struct {
    unsigned short *key_map;
    unsigned short *repeat_key_map;
    unsigned short *mouse_map;
    unsigned int *factory_customercode_map;
    unsigned int repeat_delay;
    unsigned int repeat_peroid;
    unsigned int work_mode ;
    unsigned int mouse_speed;
    unsigned int repeat_enable;
    unsigned int factory_infcode;
    unsigned int factory_unfcode;
    unsigned int factory_code;
    unsigned int release_delay;
    unsigned int release_fdelay;
    unsigned int release_sdelay;
    unsigned int debug_enable;
    //sw
    unsigned int bit_count;
    unsigned int tw_leader_act;
    unsigned int tw_bit0;
    unsigned int tw_bit1;
    unsigned int tw_bit2;
    unsigned int tw_bit3;
    unsigned int tw_repeat_leader;
    //reg
    unsigned int reg_base_gen;
    unsigned int reg_control;
    unsigned int reg_leader_act;
    unsigned int reg_leader_idle;
    unsigned int reg_repeat_leader;
    unsigned int reg_bit0_time;

    unsigned int fn_key_scancode;
    unsigned int left_key_scancode;
    unsigned int right_key_scancode;
    unsigned int up_key_scancode;
    unsigned int down_key_scancode;
    unsigned int ok_key_scancode;
    unsigned int pageup_key_scancode;
    unsigned int pagedown_key_scancode;
}remote_config_t;

//these string must in this order and sync with struct remote_config_t
static char*  config_item[33]={
    "repeat_delay",
    "repeat_peroid",
    "work_mode",
    "mouse_speed",
    "repeat_enable",
    "factory_infcode",
    "factory_unfcode",
    "factory_code",
    "release_delay",
    "release_fdelay",
    "release_sdelay",
    "debug_enable",
//sw
    "bit_count",
    "tw_leader_act",
    "tw_bit0",
    "tw_bit1",
    "tw_bit2",
    "tw_bit3",
    "tw_repeat_leader",
//reg
    "reg_base_gen",
    "reg_control",
    "reg_leader_act",
    "reg_leader_idle",
    "reg_repeat_leader",
    "reg_bit0_time",

    "fn_key_scancode",
    "left_key_scancode",
    "right_key_scancode",
    "up_key_scancode",
    "down_key_scancode",
    "ok_key_scancode",
    "pageup_key_scancode",
    "pagedown_key_scancode",
};

static int remote_ioc_table[33]={
    REMOTE_IOC_SET_REPEAT_DELAY,
    REMOTE_IOC_SET_REPEAT_PERIOD,
    REMOTE_IOC_SET_MODE,
    REMOTE_IOC_SET_MOUSE_SPEED,

    REMOTE_IOC_SET_REPEAT_ENABLE,
    REMOTE_IOC_INFCODE_CONFIG,
    REMOTE_IOC_UNFCODE_CONFIG,
    REMOTE_IOC_SET_CUSTOMCODE,
    REMOTE_IOC_SET_RELEASE_DELAY,
    REMOTE_IOC_SET_RELEASE_FDELAY,
    REMOTE_IOC_SET_RELEASE_SDELAY,
    REMOTE_IOC_SET_DEBUG_ENABLE,
//sw
    REMOTE_IOC_SET_BIT_COUNT,
    REMOTE_IOC_SET_TW_LEADER_ACT,
    REMOTE_IOC_SET_TW_BIT0_TIME,
    REMOTE_IOC_SET_TW_BIT1_TIME,
    REMOTE_IOC_SET_TW_BIT2_TIME,
    REMOTE_IOC_SET_TW_BIT3_TIME,
    REMOTE_IOC_SET_TW_REPEATE_LEADER,
//reg
    REMOTE_IOC_SET_REG_BASE_GEN,
    REMOTE_IOC_SET_REG_CONTROL	,
    REMOTE_IOC_SET_REG_LEADER_ACT,
    REMOTE_IOC_SET_REG_LEADER_IDLE,
    REMOTE_IOC_SET_REG_REPEAT_LEADER,
    REMOTE_IOC_SET_REG_BIT0_TIME,

    REMOTE_IOC_SET_FN_KEY_SCANCODE,
    REMOTE_IOC_SET_LEFT_KEY_SCANCODE,
    REMOTE_IOC_SET_RIGHT_KEY_SCANCODE,
    REMOTE_IOC_SET_UP_KEY_SCANCODE,
    REMOTE_IOC_SET_DOWN_KEY_SCANCODE,
    REMOTE_IOC_SET_OK_KEY_SCANCODE,
    REMOTE_IOC_SET_PAGEUP_KEY_SCANCODE,
    REMOTE_IOC_SET_PAGEDOWN_KEY_SCANCODE,
};

extern int set_config(remote_config_t *remote, int device_fd);
extern int get_config_from_file(FILE *fp, remote_config_t *remote);

#endif
