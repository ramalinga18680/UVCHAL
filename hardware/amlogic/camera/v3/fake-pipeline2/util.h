#ifndef __UTIL_H
#define __UTIL_H

#ifdef __cplusplus
extern "C" {
#endif

void yuyv422_to_rgb24(unsigned char *buf, unsigned char *rgb, int width, int height);
void nv21_to_rgb24(unsigned char *buf, unsigned char *rgb, int width, int height);
void nv21_memcpy_align32(unsigned char *dst, unsigned char *src, int width, int height);
void yv12_memcpy_align32(unsigned char *dst, unsigned char *src, int width, int height);

#ifdef __cplusplus
}
#endif

#endif
