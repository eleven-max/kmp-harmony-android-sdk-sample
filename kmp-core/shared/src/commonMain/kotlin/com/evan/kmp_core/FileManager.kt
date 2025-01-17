package com.evan.kmp_core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
class FileManager {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var textFileMd5Str = ""
    private var zipFilePath = ""

    fun prepare(path: String) {
        //创建目录
        FileUtil.makeDir(path)
        //创建文件
        val file = "$path/content.txt"
        FileUtil.createFile(file)
        //写入 text 文本
        FileUtil.writeFile(file, "hello world!")

        textFileMd5Str = FileUtil.calculateFileMd5(file)
        ELog.v("FileManager", "textFile:$file, textFileMd5Str: $textFileMd5Str")
        //压缩文件
        coroutineScope.launch {
            zipFilePath = "$path/ZipTest.zip"
            FileUtil.compressToZip(file, zipFilePath)
            ELog.v("FileManager", "compressToZip finish, zipFilePath:$zipFilePath")

        }
    }

    fun downloadFile(path: String) {
        coroutineScope.launch {
            delay(3000)
            val downloadPath = "$path/download"
            FileUtil.makeDir(downloadPath)

            FileUtil.unZipFile(zipFilePath, downloadPath)

            val unZipFileMd5 = FileUtil.calculateFileMd5("$downloadPath/content.txt")
            if (unZipFileMd5 == textFileMd5Str) {
                ELog.v("FileManager", "文件校验成功")
            } else {
                ELog.v("FileManager", "文件校验失败")
            }
        }
    }
}