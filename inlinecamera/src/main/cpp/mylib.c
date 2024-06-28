
// mylib.c
// mylib.c

#include <stdio.h>
#include <shadowhook.h>
#include <jni.h>
#include "mylib.h"
#include <android/log.h>
#include <stdio.h>
#include <dlfcn.h>
#include <string.h>
#include <malloc.h>

#define LOG_TAG "MyLib"

void *orig_setPreviewSurface = NULL;
void *stub_setPreviewSurface = NULL;

// 定义类型别名，匹配 setPreviewSurface 方法的签名
typedef void (*setPreviewSurface_t)(JNIEnv *env, jobject thiz, jobject surface);

// 代理函数，用于在调用原始函数前后执行额外的操作
void proxy_setPreviewSurface(JNIEnv *env, jobject thiz, jobject surface)
{
    // 在调用原始函数之前执行的操作
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "Before calling setPreviewSurface");

    // 调用原始的 setPreviewSurface 函数
    ((setPreviewSurface_t)orig_setPreviewSurface)(env, thiz, surface);

    // 在调用原始函数之后执行的操作
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "After calling setPreviewSurface\n");
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

// 执行 Hook 操作的函数
void do_hook() {
    if (orig_setPreviewSurface != NULL) {
        // 已经 Hook 过，无需再次 Hook
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "Already hooked, skipping.");
        return;
    }
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "do_hook");

    // Hook libcamera_client.so 中的 setPreviewSurface 方法
    stub_setPreviewSurface = shadowhook_hook_sym_name_callback(
            "libcamera_client.so",
            "_ZN7android16CameraParameters19previewFormatToEnumEPKc", // 这里替换成 setPreviewSurface 的符号名
            (void *)proxy_setPreviewSurface,
            (void **)&orig_setPreviewSurface, unittest_hooked, (void *)0x123456);

    if (stub_setPreviewSurface == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "stub_setPreviewSurface == NULL");
        int err_num = shadowhook_get_errno();
        const char *err_msg = shadowhook_to_errmsg(err_num);
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "hook error %d - %s\n", err_num, err_msg);
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "stub_setPreviewSurface != NULL");
    }
}

void unittest_hooked(int error_number, const char *lib_name, const char *sym_name, void *sym_addr,
                     void *new_addr, void *orig_addr, void *arg) {
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,"unittest: hooked callback: error_number %d, lib_name %s, sym_name %s, sym_addr %p, new_addr %p, "
                                                   "orig_addr %p, arg %p",
                        error_number, lib_name, sym_name, sym_addr, new_addr, orig_addr, arg);
}

// 撤销 Hook 操作的函数
void do_unhook() {
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "do_unhook");

    if (stub_setPreviewSurface != NULL) {
        // 取消 Hook
        shadowhook_unhook(stub_setPreviewSurface);
        // 重置状态
        stub_setPreviewSurface = NULL;
        orig_setPreviewSurface = NULL;
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "Unhooked successfully");
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "No hook to unhook");
    }
}

typedef int (*main_func_t)(int, char*[]);
JNIEXPORT void JNICALL
Java_com_example_inlinecamera_MyLib_loadAndExecuteMain(JNIEnv* env, jobject obj, jint pid) {
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "加载 libgo.so pid：%d\n",pid);
    void* handle;
    main_func_t main_func;

    // 加载 libgo.so
    handle = dlopen("libgo.so", RTLD_NOW);
    if (!handle) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "加载 libgo.so 出错：%s\n", dlerror());
        return;
    }
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "查找并执行 main 函数 \n");
    // 查找并执行 main 函数
    main_func = (main_func_t)dlsym(handle, "main");
    if (!main_func) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "在 libgo.so 中找不到 main 函数：%s\n", dlerror());
        dlclose(handle);
        return;
    }
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, " 准备参数（如果需要的话）\n");
    // 准备参数，包括程序名和进程ID
        int argc = 2;
        char* argv[argc];
        argv[0] = strdup("mylib"); // 或者直接使用"mylib"，但注意这会改变实际行为，因为原始字符串是常量
        argv[1] = (char*)malloc(sizeof(char) * 12); // 假设PID为最大32位正整数，加上'\0'
        snprintf(argv[1], 12, "%d", pid); // 注意检查snprintf的返回值以确认成功

    // 调用 main 函数
    main_func(argc, argv);
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, " 调用 main 函数后");
    // 清理
    dlclose(handle);
}


