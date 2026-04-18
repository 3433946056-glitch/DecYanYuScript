#include <iostream>
#include <fstream>
#include <vector>
#include <cstring>
#include <string>
#include "../Crypto/xxtea.h"

class DecScript {
private:
    static inline const char* LUA_MAGIC_HEADER         = "KEILY";
    static inline const size_t LUA_MAGIC_HEADER_LEN   = 5;
    static inline const unsigned char LUA_XOR_KEY[]   = "QuJianYu";
    static inline const unsigned char LUA_XXTEA_KEY[] = "LanJingNo1";

    static unsigned char delua_char(unsigned char data_byte, unsigned char key_byte) {
        return data_byte ^ key_byte;
    }

public:
    static bool EncryptFile(const std::string& inputPath, const std::string& outputPath) {
        // 1. 读取原始明文文件
        std::ifstream inFile(inputPath, std::ios::binary | std::ios::ate);
        if (!inFile.is_open()) return false;

        std::streamsize size = inFile.tellg();
        if (size <= 0) {
            inFile.close();
            return false;
        }

        inFile.seekg(0, std::ios::beg);
        std::vector<unsigned char> plainBuffer(size);
        inFile.read((char*)plainBuffer.data(), size);
        inFile.close();

        unsigned int teaLen = 0;
        unsigned char* teaData = xxtea_encrypt(
                plainBuffer.data(),
                (unsigned int)plainBuffer.size(),
                (unsigned char*)LUA_XXTEA_KEY,
                (unsigned int)strlen((const char*)LUA_XXTEA_KEY),
                &teaLen
        );

        if (!teaData) return false;

        size_t xorKeyLen = strlen((const char*)LUA_XOR_KEY);
        for (size_t i = 0; i < (size_t)teaLen; ++i) {
            teaData[i] = delua_char(teaData[i], LUA_XOR_KEY[i % xorKeyLen]);
        }

        std::ofstream outFile(outputPath, std::ios::binary);
        if (!outFile.is_open()) {
            free(teaData);
            return false;
        }

        outFile.write(LUA_MAGIC_HEADER, LUA_MAGIC_HEADER_LEN);
        outFile.write((char*)teaData, teaLen);

        outFile.close();
        free(teaData); // 释放 xxtea 分配的内存
        return true;
    }

    /**
     * 解密文件函数 (你提供的逻辑)
     */
    static bool DecryptFile(const std::string& inputPath, const std::string& outputPath) {
        std::ifstream inFile(inputPath, std::ios::binary | std::ios::ate);
        if (!inFile.is_open()) return false;

        std::streamsize size = inFile.tellg();
        if (size <= (std::streamsize)LUA_MAGIC_HEADER_LEN) {
            inFile.close();
            return false;
        }

        inFile.seekg(0, std::ios::beg);
        std::vector<unsigned char> buffer(size);
        inFile.read((char*)buffer.data(), size);
        inFile.close();

        if (memcmp(buffer.data(), LUA_MAGIC_HEADER, LUA_MAGIC_HEADER_LEN) != 0) return false;

        unsigned char* cipherPtr = buffer.data() + LUA_MAGIC_HEADER_LEN;
        size_t cipherLen = buffer.size() - LUA_MAGIC_HEADER_LEN;

        size_t xorKeyLen = strlen((const char*)LUA_XOR_KEY);
        for (size_t i = 0; i < cipherLen; ++i) {
            cipherPtr[i] = delua_char(cipherPtr[i], LUA_XOR_KEY[i % xorKeyLen]);
        }

        unsigned int finalLen = 0;
        unsigned char* finalData = xxtea_decrypt(
                cipherPtr,
                (unsigned int)cipherLen,
                (unsigned char*)LUA_XXTEA_KEY,
                (unsigned int)strlen((const char*)LUA_XXTEA_KEY),
                &finalLen
        );

        if (!finalData) return false;

        std::ofstream outFile(outputPath, std::ios::binary);
        if (!outFile.is_open()) {
            free(finalData);
            return false;
        }
        outFile.write((char*)finalData, finalLen);
        outFile.close();

        free(finalData);
        return true;
    }
};