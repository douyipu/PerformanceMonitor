cmake_minimum_required(VERSION 3.22.1)

project("monitorsdk")

add_library(${CMAKE_PROJECT_NAME} SHARED
    signal_handler.cpp
)

target_link_libraries(${CMAKE_PROJECT_NAME}
    android
    log
)

target_link_options(${CMAKE_PROJECT_NAME} PRIVATE "-Wl,-z,max-page-size=16384")

