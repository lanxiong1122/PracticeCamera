// go.c
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/ptrace.h>
#include <sys/wait.h>
#include <dlfcn.h>
#include <errno.h>
#include <sys/mman.h>
#include <android/log.h>
#include <dirent.h>
#include <ctype.h>
#include <jni.h>
#include "go.h"

#define LOG_TAG "MyLib----CameraHook"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

// Function prototypes
int ptrace_attach(pid_t pid);
int ptrace_detach(pid_t pid);
void *ptrace_dlopen(pid_t pid, const char *filename, int flag);
void *ptrace_dlsym(pid_t pid, void *handle, const char *symbol);
void replace_all_rels(pid_t pid, const char *symbol_name, long new_func, char *sos[]);
void inject_into_process(pid_t pid, char *sos[]);

// Main function
int main(int argc, char *argv[]) {
    LOGD("Executing main function");
    pid_t pid;
    DIR *dir;
    struct dirent *entry;
    char *sos[] = {
            "libcameraservice.so",
            NULL
    };

    // Check usage
    if (argc != 2) {
        LOGD("Usage: %s\n", argv[0]);
        return 1;
    }

    // Open the /proc directory to read process IDs
    if ((dir = opendir("/proc")) == NULL) {
        LOGD("Cannot open /proc directory");
        return 1;
    }

    // Loop through all entries in the /proc directory
    while ((entry = readdir(dir)) != NULL) {
        // Skip non-numeric entries
        if (!isdigit(*entry->d_name)) {
            continue;
        }

        // Convert entry name to PID
        pid = atoi(entry->d_name);

        // Inject into the process
        inject_into_process(pid, sos);
    }

    closedir(dir);
    LOGD("Completed");
    return 0;
}

// Inject into a specific process
void inject_into_process(pid_t pid, char *sos[]) {
    // Attach to the target process
    LOGD("Attaching to process %d", pid);
    if (ptrace_attach(pid) < 0) {
        //LOGD("Cannot attach to process %d", pid);
        return;
    }

    // Inject libbinder.so
    void *handle = ptrace_dlopen(pid, "/system/lib/libbinder.so", RTLD_NOW);
    if (handle == NULL) {
        LOGD("Cannot dlopen in process %d", pid);
        ptrace_detach(pid);
        return;
    }

    // Find MemoryBase::MemoryBase constructor address
    long proc = (long)ptrace_dlsym(pid, handle, "_ZN7android10MemoryBaseC1ERKNS_2spINS_11IMemoryHeapEEElj");

    // Replace constructor with custom implementation
    LOGD("Replacing function %s", "_ZN7android10MemoryBaseC1ERKNS_2spINS_11IMemoryHeapEEElj");
    replace_all_rels(pid, "_ZN7android10MemoryBaseC1ERKNS_2spINS_11IMemoryHeapEEElj", proc, sos);

    // Detach from the target process
    LOGD("Detaching from process %d", pid);
    ptrace_detach(pid);
}

// Implementations of ptrace_attach, ptrace_detach, ptrace_dlopen, ptrace_dlsym, replace_all_rels...
int ptrace_attach(pid_t pid) {
    if (ptrace(PTRACE_ATTACH, pid, NULL, 0) < 0) {
        LOGD("ptrace_attach error");
        return -1;
    }
    waitpid(pid, NULL, WUNTRACED);
    return 0;
}

int ptrace_detach(pid_t pid) {
    if (ptrace(PTRACE_DETACH, pid, NULL, 0) < 0) {
        LOGD("ptrace_detach error");
        return -1;
    }
    return 0;
}

void *ptrace_dlopen(pid_t pid, const char *filename, int flag) {
    // Implementation for injecting a library into the target process
}

void *ptrace_dlsym(pid_t pid, void *handle, const char *symbol) {
    // Implementation for finding a symbol address in the target process
}

void replace_all_rels(pid_t pid, const char *symbol_name, long new_func, char *sos[]) {
    // Implementation for replacing all symbols in the target process
}
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)  {
    LOGD("插件so注入成功");
 return 0;
}

