#include <stdio.h>
#include <stdlib.h>

#include "../ubootenv.h"

static void list_bootenvs(void)
{
    env_attribute *attr = bootenv_get_attr();
	while(attr != NULL) {
        printf("[%s]: [%s]\n", attr->key, attr->value);
		attr = attr->next;
	}
}

int main(int argc, char *argv[])
{
    bootenv_init();

    if (argc == 1) {
        list_bootenvs();
    } else {
        //printf("key:%s\n", argv[1]);
        const char* p_value = bootenv_get(argv[1]);
    	if (p_value) {
            printf("%s\n", p_value);
    	}
    }
    return 0;
}
