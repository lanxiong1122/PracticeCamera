cmake_minimum_required(VERSION 3.22.1)
find_package(shadowhook REQUIRED CONFIG)

if(${ANDROID_ABI} STREQUAL "arm64-v8a")
    set(ARCH "arm64")
elseif(${ANDROID_ABI} STREQUAL "armeabi-v7a")
    set(ARCH "arm")
endif()

set(TARGET "mylib")
add_library(mylib SHARED mylib.c)
target_link_libraries(mylib shadowhook::shadowhook)
target_link_libraries(${TARGET} log)