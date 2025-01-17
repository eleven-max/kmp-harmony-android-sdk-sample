package com.evan.kmp_core

object FileUtil {

    fun makeDir(path: String) : Boolean {
        return platformMakeDir(path)
    }

    fun createFile(file: String) : Boolean {
        return platformCreateFile(file)
    }

    fun writeFile(file: String, content: String): Boolean {
        return platformWriteFile(file, content)
    }

    suspend fun compressToZip(sourceFile: String, outputZipPath: String): Boolean {
        return platformCompressToZip(sourceFile, outputZipPath)
    }

     suspend fun unZipFile(zipFilePath: String, targetDir: String): Boolean {
        return platformUnZipFile(zipFilePath, targetDir)
    }

    fun calculateFileMd5(filePath: String): String {
        return platformGetFileMd5(filePath)
    }
}