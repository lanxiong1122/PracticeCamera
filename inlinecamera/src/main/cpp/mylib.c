
// mylib.c
#include <stdio.h>
#include <shadowhook.h>
#include <jni.h>
#include "mylib.h"
#include <android/log.h>



#define LOG_TAG "MyLib"


void *orig = NULL;
void *stub = NULL;
// 假设Camera类的实例指针类型为void *
typedef void *CameraInstance;

// 定义类型别名，匹配startPreview函数的签名
typedef void (*type_t)(CameraInstance);

// 代理函数，用于在调用原始函数前后执行额外的操作
void proxy(CameraInstance camera)
{
    // 在调用原始函数之前执行的操作
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "Before calling startPreview");
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,"Before calling startPreview\n");

    // 调用原始的startPreview函数
    ((type_t)orig)(camera);

    // 在调用原始函数之后执行的操作
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "After calling startPreview\n");
}
JNIEXPORT void JNICALL Java_com_example_inlinecamera_MyLib_startPreview(JNIEnv *env, jobject thiz) {
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "startPreview called\n");
    do_hook();
}
JNIEXPORT void JNICALL Java_com_example_inlinecamera_MyLib_cancelPreview(JNIEnv *env, jobject thiz) {
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "cancelPreview called\n");
    do_unhook();
}
typedef void (*shadowhook_hooked_t)(int error_number, const char *lib_name, const char *sym_name,
                                    void *sym_addr, void *new_addr, void *orig_addr, void *arg);
// 执行Hook操作的函数
void do_hook() {
    if (orig != NULL) {
        // 已经Hook过，无需再次Hook
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "Already hooked, skipping.");
        return;
    }
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "do_hook");

    // Hook libcamera_client.so中的startPreview函数
    stub = shadowhook_hook_sym_name_callback(
            "libcamera_client.so",
            "_ZN7android6Camera12startPreviewEv",
            (void *)proxy,
            (void **)&orig, unittest_hooked, (void *)0x123456);

    if (stub == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "stub == NULL");
        int err_num = shadowhook_get_errno();
        const char *err_msg = shadowhook_to_errmsg(err_num);
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "hook error %d - %s\n", err_num, err_msg);
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "stub != NULL");
    }
}
void unittest_hooked(int error_number, const char *lib_name, const char *sym_name, void *sym_addr,
                            void *new_addr, void *orig_addr, void *arg) {
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,"unittest: hooked callback: error_number %d, lib_name %s, sym_name %s, sym_addr %p, new_addr %p, "
        "orig_addr %p, arg %p",
        error_number, lib_name, sym_name, sym_addr, new_addr, orig_addr, arg);
}
// 撤销Hook操作的函数
void do_unhook() {
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "do_unhook");

    if (stub != NULL) {
        // 取消Hook
        shadowhook_unhook(stub);
        // 重置状态
        stub = NULL;
        orig = NULL;
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "Unhooked successfully");
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "No hook to unhook");
    }
}