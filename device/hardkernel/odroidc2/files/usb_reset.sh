#!/bin/sh

for x in $(lsusb); do
	echo $x
	if [ "$x" == "16b4:0703" ]; then

		result=`getprop wlan.driver.status`

		if [ "$result" == "ok" ]; then
			svc wifi disable
			while [ "$result" != "unloaded" ]; do
				sleep 1
				result=`getprop wlan.driver.status`
			done
		fi

		echo 126 > /sys/class/gpio/export
		echo out > /sys/class/gpio/gpio126/direction
		sleep 1
		echo 0 > /sys/class/gpio/gpio126/value
		sleep 1
		echo 1 > /sys/class/gpio/gpio126/value
		echo 126 > /sys/class/gpio/unexport

		sleep 5

		if [ "$result" == "unloaded" ]; then
			svc wifi enable
			while [ "$result" != "ok" ]; do
				sleep 1
				result=`getprop wlan.driver.status`
			done
		fi

fi
done
