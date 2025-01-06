package com.evan.kmp_core

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

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