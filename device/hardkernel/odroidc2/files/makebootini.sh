#!/bin/sh
if [ -e "/storage/internal/boot.ini" ]
then
    break
else
    cp /system/etc/boot.ini.template /storage/internal/boot.ini
fi

if [ -e "/storage/internal/Image" ]
then
    break
else
    cp /system/etc/Image /storage/internal/Image
fi
