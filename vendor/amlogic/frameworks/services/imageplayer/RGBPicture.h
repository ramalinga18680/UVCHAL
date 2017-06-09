/** @file RGBPicture.h
 *  @par Copyright:
 *  - Copyright 2011 Amlogic Inc as unpublished work
 *  All Rights Reserved
 *  - The information contained herein is the confidential property
 *  of Amlogic.  The use, copying, transfer or disclosure of such information
 *  is prohibited except by express written agreement with Amlogic Inc.
 *  @author   Tellen Yu
 *  @version  1.0
 *  @date     2014/04/26
 *  @par function description:
 *  - 1 save rgb data to picture
 *  @warning This class may explode in your face.
 *  @note If you inherit anything from this class, you're doomed.
 */

#ifndef _RGB_PICTURE_H_
#define _RGB_PICTURE_H_

#ifdef __cplusplus
extern "C" {
#endif

typedef struct
{
    unsigned int width;
    unsigned int height;
#define FB_FORMAT_RGB565    0
#define FB_FORMAT_ARGB8888  1
    unsigned int format;
    char* data;
}rc_fb_t;

typedef struct rgb888 {
    char r;
    char g;
    char b;
} rc_rgb888_t;

typedef rc_rgb888_t rc_rgb24_t;

typedef struct rgb565 {
    short b:5;
    short g:6;
    short r:5;
} rc_rgb565_t;

#pragma pack(1) // 按照1字节方式进行对齐
typedef struct
{
    unsigned short  bf_type;/*位图文件的类型，必须为BMP(0-1字节)*/
    unsigned long   bf_size;/*位图文件的大小，以字节为单位(2-5字节)*/
    unsigned short  bf_reserved1;/*位图文件保留字，必须为0(6-7字节)*/
    unsigned short  bf_reserved2; /*位图文件保留字，必须为0(8-9字节)*/
    unsigned long   bf_offbits; /* 位图数据的起始位置，以相对于位图(10-13字节)*/
}BmpFileHeader_t;

typedef struct
{
    unsigned long   bi_size; /* 本结构所占用字节数(14-17字节)*/
    unsigned long   bi_width;/*位图的宽度，以像素为单位(18-21字节)*/
    unsigned long   bi_height;/* 位图的高度，以像素为单位(22-25字节)*/

    unsigned short  bi_planes;/* 目标设备的级别，必须为1(26-27字节)*/
    unsigned short  bi_bitcount;/* 每个像素所需的位数，必须是1(双色),(28-29字节)
                                4(16色)，8(256色)或24(真彩色)之一*/

    unsigned long   bi_compression; /*位图压缩类型，必须是 0(不压缩),(30-33字节)
                                    1(BI_RLE8压缩类型)或2(BI_RLE4压缩类型)或者3(BI_BITFIELDS)*/

    unsigned long   bi_sizeimage;/*位图的大小，以字节为单位(34-37字节)*/
    unsigned long   bi_xpelspermeter;/* 位图水平分辨率，每米像素数(38-41字节)*/
    unsigned long   bi_ypelspermeter;/*位图垂直分辨率，每米像素数(42-45字节)*/
    unsigned long   bi_clrused;/*位图实际使用的颜色表中的颜色数(46-49字节)*/
    unsigned long   bi_clrimportant;/* 位图显示过程中重要的颜色数(50-53字节)*/
}BmpInfoHeader_t;

/*
颜色表中rgb_color_table_t结构数据的个数有bi_bitcount来确定:
当biBitCount=1,4,8时，分别有2,16,256个表项;
当biBitCount=16时，bi_compression = BI_BITFIELDS时有三组掩码:0xf800, 0x7e0, 0x1f
    PhotoRGBColorTable_t bmp_colors[3];

    bmp_colors[0].rgb_blue      =   0;
    bmp_colors[0].rgb_green     =   0xF8;
    bmp_colors[0].rgb_red       =   0;
    bmp_colors[0].rgb_reserved  =   0;
    bmp_colors[1].rgb_blue      =   0xE0;
    bmp_colors[1].rgb_green     =   0x07;
    bmp_colors[1].rgb_red       =   0;
    bmp_colors[1].rgb_reserved  =   0;
    bmp_colors[2].rgb_blue      =   0x1F;
    bmp_colors[2].rgb_green     =   0;
    bmp_colors[2].rgb_red       =   0;
    bmp_colors[2].rgb_reserved  =   0;
当biBitCount=24时，没有颜色表项。
*/
typedef struct//color table
{
    unsigned char rgb_blue;// 蓝色的亮度(值范围为0-255)
    unsigned char rgb_green; // 绿色的亮度(值范围为0-255)
    unsigned char rgb_red; // 红色的亮度(值范围为0-255)
    unsigned char rgb_reserved;// 保留，必须为0
}BmpColorTable_t;
#pragma pack() // 取消1字节对齐方式

int RGBA2bmp(char *buf, int width, int height, char* filePath);

#ifdef __cplusplus
}
#endif

#endif/*_RGB_PICTURE_H_*/
