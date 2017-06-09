#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <assert.h>
#include "jni.h"
#include "JNIHelp.h"
#include "android_runtime/AndroidRuntime.h"
#include "tv/CFbcCommunication.h"

#define TAG      "joey_jni"
#define JNI_DBG(a...) __android_log_print(ANDROID_LOG_INFO, TAG, a)
#define MAX_CNT 128

static JNIEnv *jni_local_env = NULL;
static jclass jni_local_clz = NULL;
static jobject jni_local_obj = NULL;
static jmethodID jni_local_mid = NULL;
static JavaVM *m_vm = NULL;
static CFbcCommunication *g_cfbc_handle = NULL;

JNIEnv *getJNIEnv(bool *needsDetach)
{
    JNIEnv *env = NULL;
    jint result = -1;
    if (m_vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        __android_log_print(ANDROID_LOG_INFO, TAG, "ERROR: GetEnv failed\n");

        int status = m_vm->AttachCurrentThread(&env, NULL);
        if (status < 0) {
            __android_log_print(ANDROID_LOG_INFO, TAG, "callback_handler: failed to attach current thread");
            return NULL;
        }

        *needsDetach = true;
    }

    __android_log_print(ANDROID_LOG_INFO, TAG, "GetEnv Success");
    return env;
}

void detachJNI()
{
    int result = m_vm->DetachCurrentThread();
    if (result != JNI_OK) {
        __android_log_print(ANDROID_LOG_INFO, TAG, "thread detach failed: %#x", result);
    }
}

//this data buf is same as cmd buf
void java_jni_callback(char *str, int cnt, int data_buf[])
{
    char temp_str[MAX_CNT];
    int idx = 0;
    if (str != NULL && cnt > 0) {
        memset(temp_str, 0, sizeof(temp_str));
        JNI_DBG("java jni string is:\n%s, cnt:%d.", str, cnt);
        //strcpy(temp_str, "Call From C/C++!");
        memcpy(temp_str, str, strlen(str) % MAX_CNT);

        if (NULL != jni_local_obj) {
            bool needsDetach = false;
            jint j_cnt = data_buf[1];
            jint j_data_buf[MAX_CNT];
            for (idx = 0; idx < j_cnt; idx++) {
                idx %= MAX_CNT;
                j_data_buf[idx] = data_buf[idx];
                JNI_DBG("java_jni_callback the %d data is:0x%x, %d.", idx, j_data_buf[idx], data_buf[idx]);
            }

            //jobject obj;
            jni_local_env = getJNIEnv(&needsDetach);
            //obj = (*jni_local_env)->NewGlobalRef(jni_local_env,jni_local_obj);
            jni_local_clz = jni_local_env->GetObjectClass(jni_local_obj);
            //this func name and parameters should be same as the callback defined in java code
            jni_local_mid = jni_local_env->GetMethodID(jni_local_clz, "android_java_callback", "(Ljava/lang/String;[I)I");

            jstring str1 = jni_local_env->NewStringUTF(temp_str);

            jintArray cc_data_arr = jni_local_env->NewIntArray(j_cnt);
            jni_local_env->SetIntArrayRegion(cc_data_arr, 0, cnt, j_data_buf);
            //jint *temp_data = jni_local_env->GetIntArrayElements(cc_data_arr, NULL);

            jint cnt = jni_local_env->CallIntMethod(jni_local_obj, jni_local_mid, str1, cc_data_arr);

            //jni_local_env->ReleaseIntArrayElements(cc_data_arr, temp_data, 0);
            //jni_local_env->ReleaseIntArrayElements(cc_cmd_arr, temp_cmd, 0);
            JNI_DBG("%s %d be called.", __FUNCTION__, __LINE__);

            if (needsDetach) {
                detachJNI();
            }
        }
    }
}

//here we needn't to match the java package name
static jint jni_java_exec_cmd(JNIEnv *env, jobject obj, jintArray cmdArray)
{
    jint *arry = env->GetIntArrayElements(cmdArray, NULL);
    jint length = env->GetArrayLength(cmdArray);

    int cmd_cnt = arry[1], idx = 0;
    int cmd_array[MAX_CNT];
    memset(cmd_array, 0, sizeof(cmd_array));
    for (idx = 0; idx < cmd_cnt; idx++)
        cmd_array[idx] = arry[idx];

    JNI_DBG("%s %s %d be called.", __FILE__, __FUNCTION__, __LINE__);

    if (g_cfbc_handle == NULL) {
        g_cfbc_handle = new CFbcCommunication();
        g_cfbc_handle->run("cfbc_thread", 0, 0);
    }

    //g_cfbc_handle->handleCmd(COMM_DEV_CEC, cmd_array);
    //c_exec_cmd(cmd_array);

    if (NULL == jni_local_obj) {
        jni_local_obj = env->NewGlobalRef(obj);
    }

#if 0
    /* this is used to terminate the jni call if needed
    ** and we should handle the pthread we create in c layer
    */
    if (cmd_array[0] == 0x1002) {
        if (NULL != jni_local_obj)
            env->DeleteGlobalRef(jni_local_obj);
    }
#endif
    return 0;
}

//the name of 'exec_cmd' should be same as the native func in java code
static JNINativeMethod gMethods[] = {
    {"exec_cmd", "([I)I", (void *)jni_java_exec_cmd},
};

static int register_android_MyFunc(JNIEnv *env)
{
    JNI_DBG("%s %s %d be called.", __FILE__, __FUNCTION__, __LINE__);
    //the name below should be same as the class name in which native method declared in Java layer
    return android::AndroidRuntime::registerNativeMethods(env, "com/fbc/MyFunc", gMethods, NELEM(gMethods));
}

jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JNIEnv *env = NULL;
    JNI_DBG("%s %s %d be called.", __FILE__, __FUNCTION__, __LINE__);
    //c_set_callback(&java_jni_callback);
    if (vm->GetEnv((void **)&env, JNI_VERSION_1_4) != JNI_OK) {
        JNI_DBG("Error GetEnv\n");
        return -1;
    }

    assert(env != NULL);
    if (register_android_MyFunc(env) < 0) {
        JNI_DBG("register_android_test_hdi error.\n");
        return -1;
    }

    m_vm = vm;
    return JNI_VERSION_1_4;
}
