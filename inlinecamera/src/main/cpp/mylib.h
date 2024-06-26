// mylib.h
#ifndef MYLIB_H
#define MYLIB_H

// 声明类型别名，匹配startPreview函数的签名
typedef void (*type_t)(void *);

// 代理函数声明，用于在调用原始函数前后执行额外的操作
void proxy(void *);

// 执行Hook操作的函数声明
void do_hook();

// 撤销Hook操作的函数声明
void do_unhook();
//执行Hook操作的函数回调
void unittest_hooked(int error_number, const char *lib_name, const char *sym_name, void *sym_addr,
                            void *new_addr, void *orig_addr, void *arg);

#endif /* MYLIB_H */