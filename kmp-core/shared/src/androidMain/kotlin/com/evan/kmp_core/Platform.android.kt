package com.evan.kmp_core

import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class AndroidPlatform : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()


actual fun platformMakeDir(path: String): Boolean {
    val file = File(path)
    if(file.exists()) return false
    return file.mkdirs()
}

actual fun platformCreateFile(path: String): Boolean {
    val file = File(path)
    if (file.exists()) return false // 如果文件已存在，返回 false
    return file.createNewFile() // 尝试创建文件
}

actual fun platformWriteFile(file: String, content: String): Boolean {
    return try {
        File(file).writeText(content)
        true
    } catch (e: IOException) {
        false
    }
}

actual fun platformCompressToZip(
    sourceFile: String,
    outputZipPath: String
): Boolean {
    return try {
        val source = File(sourceFile)

        // 检查文件是否存在并且是普通文件
        if (!source.exists() || !source.isFile) return false

        FileOutputStream(outputZipPath).use { fos ->
            ZipOutputStream(fos).use { zipOut ->
                FileInputStream(source).use { fis ->
                    val zipEntry = ZipEntry(source.name)
                    zipOut.putNextEntry(zipEntry)
                    fis.copyTo(zipOut)
                }
            }
        }
        true // 成功压缩
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        false
    } catch (e: IOException) {
        e.printStackTrace()
        false
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
actual fun platformUnZipFile(zipFilePath: String, targetDir: String): Boolean {
    return try {
        val zipFile = File(zipFilePath)
        val targetDirectory = File(targetDir)
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs()
        }
        val zipInputStream = ZipInputStream(FileInputStream(zipFile))
        var entry: ZipEntry?
        while (zipInputStream.nextEntry.also { entry = it } != null) {
            val entryName = entry!!.name
            val outputFile = File(targetDirectory, entryName)
            if (entry!!.isDirectory) {
                outputFile.mkdirs()
            } else {
                outputFile.parentFile?.mkdirs()
                val outputStream = FileOutputStream(outputFile)
                val buffer = ByteArray(8192)
                var len: Int
                while (zipInputStream.read(buffer).also { len = it } != -1) {
                    outputStream.write(buffer, 0, len)
                }
                outputStream.close()
            }
            zipInputStream.closeEntry()
        }
        zipInputStream.close()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

actual fun platformLog(tag: String, message: String) {
    Log.v(tag, message)
}

actual fun platformGetFileMd5(filePath: String): String {
    val digest = MessageDigest.getInstance("MD5")
    val file = File(filePath)
    val inputStream = FileInputStream(file)
    val buffer = ByteArray(8192)
    var bytesRead: Int
    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
        digest.update(buffer, 0, bytesRead)
    }
    inputStream.close()
    val md5Bytes = digest.digest()
    return md5Bytes.joinToString("") { "%02x".format(it) }
}