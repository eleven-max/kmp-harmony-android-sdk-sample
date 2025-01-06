package com.evan.kmp_core

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun platformLog(tag: String, message: String)

expect fun platformMakeDir(path: String) : Boolean

expect fun platformCreateFile(path: String): Boolean

expect fun platformWriteFile(file: String, content: String): Boolean

expect fun platformCompressToZip(sourceFile: String, outputZipPath: String): Boolean

expect fun platformUnZipFile(zipFilePath: String, targetDir: String): Boolean

expect fun platformGetFileMd5(filePath: String): String