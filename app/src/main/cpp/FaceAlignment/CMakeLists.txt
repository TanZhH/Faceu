# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

# 查找当前目录下的所有源文件
# 并将名称保存到 DIR_SRCS 变量
aux_source_directory(. DIR_SRCS)
aux_source_directory(./liblinear DIR_SUB)

# 添加 liblinear 子目录
# add_subdirectory(liblinear)

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add.library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.

add_library( # Specifies the name of the library.
             facedetectionkeypoint-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             ${DIR_SRCS}
             ${DIR_SUB})

# Specifies a path to native header files.
include_directories(src/main/cpp/FaceAlignment)
include_directories(/Users/shenjingyuan002/work/SDK/OpenCV-android-sdk/sdk/native/jni/include)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because system libraries are included in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.
target_link_libraries( # Specifies the target library.
                       facedetectionkeypoint-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib}
                       /Users/shenjingyuan002/work/SDK/OpenCV-android-sdk/sdk/native/libs/arm64-v8a/libopencv_java3.so
                       )