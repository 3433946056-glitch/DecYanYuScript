#include <jni.h>
#include <string>
#include "DecScript/DecScript.h"

extern "C"
JNIEXPORT void JNICALL
Java_com_enzo_decyanyuscript_MainActivity_DecScript(JNIEnv *env, jclass clazz, jstring input_path,
                                                 jstring output_path) {
    // TODO: implement DecScript()
    const char *c_in = env->GetStringUTFChars(input_path, nullptr);
    const char *c_out = env->GetStringUTFChars(output_path, nullptr);
    DecScript::DecryptFile(c_in, c_out);

    env->ReleaseStringUTFChars(input_path, c_in);
    env->ReleaseStringUTFChars(output_path, c_out);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_enzo_decyanyuscript_MainActivity_EncScript(JNIEnv *env, jclass clazz, jstring input_path,
                                                 jstring output_path) {
    // TODO: implement EncScript()
    const char *c_in = env->GetStringUTFChars(input_path, nullptr);
    const char *c_out = env->GetStringUTFChars(output_path, nullptr);
    DecScript::EncryptFile(c_in, c_out);

    env->ReleaseStringUTFChars(input_path, c_in);
    env->ReleaseStringUTFChars(output_path, c_out);
}