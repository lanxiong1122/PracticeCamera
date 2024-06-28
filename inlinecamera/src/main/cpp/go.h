

#ifndef GO_H_
#define GO_H_

#include <sys/types.h>

#ifdef __cplusplus
extern "C" {
#endif

// 定义必要的结构和常量

typedef struct {
    unsigned l_dlopen;
    unsigned l_dlclose;
    unsigned l_dlsym;
} dl_fl_t;

typedef struct pt_regs {
    unsigned long uregs[18];
} pt_regs;

// 函数声明
int ptrace_attach(pid_t pid);
int ptrace_detach(pid_t pid);
dl_fl_t *ptrace_find_dlinfo(pid_t pid);
void *ptrace_dlopen(pid_t pid, const char *filename, int flag);
void *ptrace_dlsym(pid_t pid, void *handle, const char *symbol);
void replace_all_rels(pid_t pid, const char *symbol_name, long new_func, char *sos[]);

#ifdef __cplusplus
}
#endif

#endif /* GO_H_ */
