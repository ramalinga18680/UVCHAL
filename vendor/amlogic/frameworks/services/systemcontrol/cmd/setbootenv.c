#include <stdio.h>
#include <stdlib.h>

#include "../ubootenv.h"

int main(int argc, char *argv[])
{
    if(argc != 3) {
        fprintf(stderr,"usage: setbootenv <key> <value>\n");
        return 1;
    }

    bootenv_init();
    if(bootenv_update(argv[1], argv[2])){
        fprintf(stderr,"could not set boot env\n");
        return 1;
    }

    return 0;
}
