#include <jni.h>
#include <string.h>
#include <android/log.h>

#define LOG_TAG "FaceAlignment_Native"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

#ifndef _Included_com_compilesense_liuyi_faceu_FaceAlignment
#define _Included_com_compilesense_liuyi_faceu_FaceAlignment
#ifdef __cplusplus
extern "C" {
#endif


JNIEXPORT void JNICALL
Java_com_compilesense_liuyi_faceu_FaceAlignment_initModel(JNIEnv *, jclass, jstring);

JNIEXPORT jobjectArray JNICALL
Java_com_compilesense_liuyi_faceu_FaceAlignment_detectKeyPoints(JNIEnv *, jclass,
                                                                jlong,
                                                                jobjectArray);
#ifdef __cplusplus
}
#endif
#endif