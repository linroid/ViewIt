cmake_minimum_required(VERSION 3.6)
project(search_image)

set(CMAKE_C_STANDARD 99)

set(SOURCE_FILES main.c)
set(CMAKE_INSTALL_PREFIX ../app)
set(CMAKE_BUILD_TYPE "Release")

add_executable(search_image ${SOURCE_FILES})
install (TARGETS search_image DESTINATION src/main/assets)