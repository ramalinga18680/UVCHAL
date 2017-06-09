#!/bin/sh
SYSFS_GPIO_DIR="/sys/class/gpio"

retval=""

gpio_export()
{
	[ -e "$SYSFS_GPIO_DIR/gpio$1" ] && return 0
	echo $1 > "$SYSFS_GPIO_DIR/export"
	echo $1
}

gpio_getvalue()
{
	echo in > "$SYSFS_GPIO_DIR/gpio$1/direction"
	val=`cat "$SYSFS_GPIO_DIR/gpio$1/value"`
	retval=$val
}

gpio_setvalue()
{
	echo out > "$SYSFS_GPIO_DIR/gpio$1/direction"
	echo $2 > "$SYSFS_GPIO_DIR/gpio$1/value"
}

check()
{
	gpio_export $AC_OK_GPIO
	gpio_export $BAT_OK_GPIO
	gpio_getvalue $AC_OK_GPIO

	if [ $retval -eq  1 ]
	then
		echo "DC Input Okay"
	else
		echo "Power is shutdown or AC Adaptor is disconnected"
		gpio_getvalue $BAT_OK_GPIO
		echo $retval
		if [ $retval -eq 0 ]
			then
			echo "battery is low than 3.7V"
			poweroff -d 5
		else
			echo "battery is good"
		fi
	fi
}

MODEL=`getprop ro.product.board`
echo $MODEL

if [ `echo $MODEL | grep -c "odroidc2"` -gt 0 ]
then
	AC_OK_GPIO=247
	BAT_OK_GPIO=239
	LATCH_GPIO=225
	gpio_export $LATCH_GPIO
	gpio_setvalue $LATCH_GPIO 1
elif [ `echo $MODEL | grep -c "odroidc"` -gt 0 ]
then
	AC_OK_GPIO=88
	BAT_OK_GPIO=116
	LATCH_GPIO=115
	gpio_export $LATCH_GPIO
	gpio_setvalue $LATCH_GPIO 1
else
	AC_OK_GPIO=199
	BAT_OK_GPIO=200
	LATCH_GPIO=204
	gpio_export $LATCH_GPIO
	gpio_setvalue $LATCH_GPIO 1
fi

while true
do
	check
	sleep 2
done
