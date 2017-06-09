#include "stdlib.h"
#include "stdio.h"
#include "tv/CFbcCommunication.h"

int main(int argc, char **argv)
{
    int idx = 0, send_buf[128], recv_buf[128], cmd_value = 1, run_flag = 0, run_cnt = 0, cmd_type = 0, read_flag = 0;
    printf("run begin.......\n");
    if (argc < 4) {
        printf("usage:./libcfbc_jni cmd_type cmd_value run_cnt(all hex mode)\n");
        return 0;
    }

    cmd_type = strtol(argv[1], NULL, 16);
    cmd_value = strtol(argv[2], NULL, 16);
    run_cnt = strtol(argv[3], NULL, 16);

    CFbcCommunication *cfbcHandle = new CFbcCommunication();
    printf("to test.......\n");
    cfbcHandle->run();
    sleep(0.2);

    switch (cmd_type) {
    default:
        for (idx = 0; idx < run_cnt; idx++) {
            memset(send_buf, 0, sizeof(send_buf));
            memset(recv_buf, 0, sizeof(recv_buf));
            send_buf[0] = cmd_type;
            send_buf[1] = 3;
            send_buf[2] = cmd_value;
            printf("\n\n======%d to set value is:0x%02x\n", idx, cmd_value);
            cfbcHandle->handleCmd(COMM_DEV_SERIAL, send_buf, recv_buf);
            sleep(1);
        }
        break;
    }

    printf("wait to exit......\n");
    cfbcHandle->requestExitAndWait();
    delete cfbcHandle;
    printf("program exited\n");
    return 0;
}
