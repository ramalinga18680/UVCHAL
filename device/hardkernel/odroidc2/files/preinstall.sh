#!/bin/sh

pkgs=`ls /cache/*.apk`

for apk in $pkgs; do
        echo "Installing package: " $apk
#        pm install $apk
        rm -f $apk
done
