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

    fun compressToZip(sourceFile: String, outputZipPath: String): Boolean {
        return platformCompressToZip(sourceFile, outputZipPath)
    }

    // commonMain
    expect fun unzipFile(zipFilePath: String, targetDir: String): Boolean

    // commonMain
    expect fun calculateFileMd5(filePath: String): String
}