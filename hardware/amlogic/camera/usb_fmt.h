#ifndef __USB_FMT_H__
#define __USB_FMT_H__
#include <linux/videodev2.h>

#ifndef ARRAY_SIZE
#define ARRAY_SIZE(x) (sizeof(x) / sizeof((x)[0]))
#endif

extern "C" uint32_t query_aml_usb_pixelfmt(uint16_t idVendor, uint16_t idProduct,
                uint16_t w, uint16_t h);
#endif
