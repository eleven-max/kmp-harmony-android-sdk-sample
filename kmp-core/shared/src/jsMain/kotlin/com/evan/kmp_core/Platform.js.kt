package com.evan.kmp_core

import kotlinx.coroutines.await
import ohos.FileUtil
import ohos.hilog

class JSPlatform: Platform {
    override val name: String
        get() = "JS"
}

actual fun getPlatform(): Platform {
    return JSPlatform()
}

actual fun platformLog(tag: String, message: String) {
    hilog.debug(0.0, tag, message)
}

actual fun platformMakeDir(path: String): Boolean {
    return FileUtil.makeDir(path)
}

actual fun platformCreateFile(path: String): Boolean {
    return FileUtil.createFile(path)
}

actual fun platformWriteFile(file: String, content: String): Boolean {
    return FileUtil.writeFile(file, content)
}

actual fun platformGetFileMd5(filePath: String): String {
    return FileUtil.getFileMd5(filePath)
}

actual suspend fun platformCompressToZip(
    sourceFile: String,
    outputZipPath: String
): Boolean {
    return FileUtil.compressToZip(sourceFile, outputZipPath).await()
}

actual suspend fun platformUnZipFile(zipFilePath: String, targetDir: String): Boolean {
    return FileUtil.unZipFile(zipFilePath, targetDir).await()
}

