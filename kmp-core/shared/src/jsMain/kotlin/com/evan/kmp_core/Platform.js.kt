package com.evan.kmp_core

class JSPlatform: Platform {
    override val name: String
        get() = "JS"
}

actual fun getPlatform(): Platform {
    return JSPlatform()
}

actual fun platformMakeDir(path: String): Boolean {
    TODO("Not yet implemented")
}

actual fun platformCreateFile(path: String): Boolean {
    TODO("Not yet implemented")
}

actual fun platformWriteFile(file: String, content: String): Boolean {
    TODO("Not yet implemented")
}

actual fun platformCompressToZip(
    sourceFile: String,
    outputZipPath: String
): Boolean {
    TODO("Not yet implemented")
}

actual fun platformLog(tag: String, message: String) {
}

actual fun platformUnZipFile(zipFilePath: String, targetDir: String): Boolean {
    TODO("Not yet implemented")
}

actual fun platformGetFileMd5(filePath: String): String {
    TODO("Not yet implemented")
}