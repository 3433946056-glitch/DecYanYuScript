//
// Created by Administrator on 2026/2/1.
//
#include <iostream>
#include <vector>
#include <cstring>
#include "DecScript.h"

unsigned char delua_char(unsigned char data_byte, unsigned char key_byte) {
    return data_byte ^ key_byte;
}

unsigned char* xorlua_encrypt(
        const unsigned char* data,
        size_t data_len,
        const unsigned char* key,
        int key_len,
        unsigned int* out_len)
{
    if (data_len == 0) return nullptr;

    unsigned char* buffer = (unsigned char*)malloc(data_len);
    if (!buffer) return nullptr;
    memcpy(buffer, data, data_len);

    for (size_t i = 0; i < data_len; ++i) {
        buffer[i] = delua_char(buffer[i], key[i % key_len]);
    }

    if (out_len) {
        *out_len = (unsigned int)data_len;
    }

    return buffer;
}