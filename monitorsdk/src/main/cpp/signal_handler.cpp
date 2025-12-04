#include <jni.h>
#include <signal.h>
#include <pthread.h>
#include <unistd.h>
#include <android/log.h>
#include <string>
#include <cstring>

#define LOG_TAG "AnrMonitor"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static JavaVM* g_jvm = nullptr;
static jobject g_callback = nullptr;
static struct sigaction g_old_action;
static bool g_is_initialized = false;

void notifyAnrDetected() {
    if (g_jvm == nullptr || g_callback == nullptr) {
        LOGE("JVM or callback is null");
        return;
    }

    JNIEnv* env = nullptr;
    bool needDetach = false;
    
    int status = g_jvm->GetEnv((void**)&env, JNI_VERSION_1_6);
    if (status == JNI_EDETACHED) {
        if (g_jvm->AttachCurrentThread(&env, nullptr) != JNI_OK) {
            LOGE("Failed to attach thread");
            return;
        }
        needDetach = true;
    }

    jclass clazz = env->GetObjectClass(g_callback);
    jmethodID method = env->GetMethodID(clazz, "onAnrDetected", "()V");
    
    if (method != nullptr) {
        env->CallVoidMethod(g_callback, method);
    }

    if (needDetach) {
        g_jvm->DetachCurrentThread();
    }
}

void sigquitHandler(int sig, siginfo_t* info, void* context) {
    LOGD("SIGQUIT received, ANR may have occurred");
    
    notifyAnrDetected();
    
    if (g_old_action.sa_flags & SA_SIGINFO) {
        if (g_old_action.sa_sigaction != nullptr) {
            g_old_action.sa_sigaction(sig, info, context);
        }
    } else {
        if (g_old_action.sa_handler != nullptr && g_old_action.sa_handler != SIG_DFL && g_old_action.sa_handler != SIG_IGN) {
            g_old_action.sa_handler(sig);
        }
    }
}

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    g_jvm = vm;
    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_douyipu_monitorsdk_anr_AnrMonitor_nativeInit(JNIEnv* env, jobject thiz, jobject callback) {
    if (g_is_initialized) {
        LOGD("Already initialized");
        return JNI_TRUE;
    }

    g_callback = env->NewGlobalRef(callback);

    struct sigaction action;
    memset(&action, 0, sizeof(action));
    sigemptyset(&action.sa_mask);
    action.sa_sigaction = sigquitHandler;
    action.sa_flags = SA_SIGINFO | SA_ONSTACK | SA_RESTART;

    if (sigaction(SIGQUIT, &action, &g_old_action) != 0) {
        LOGE("Failed to set SIGQUIT handler");
        env->DeleteGlobalRef(g_callback);
        g_callback = nullptr;
        return JNI_FALSE;
    }

    g_is_initialized = true;
    LOGD("SIGQUIT handler installed successfully");
    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_douyipu_monitorsdk_anr_AnrMonitor_nativeDestroy(JNIEnv* env, jobject thiz) {
    if (!g_is_initialized) {
        return;
    }

    sigaction(SIGQUIT, &g_old_action, nullptr);

    if (g_callback != nullptr) {
        env->DeleteGlobalRef(g_callback);
        g_callback = nullptr;
    }

    g_is_initialized = false;
    LOGD("SIGQUIT handler removed");
}

