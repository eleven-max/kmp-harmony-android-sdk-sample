package com.evan.kmp_core

class FileManager {

    fun prepare(path: String) {
        //创建目录
        FileUtil.makeDir(path)
        //创建文件
        val file = "$path/content.txt"
        FileUtil.createFile(file)
        //写入 text 文本
        FileUtil.writeFile(file, "hello world!")
        //压缩文件
        val zipFile = "$path/ZipTest.zip"
        FileUtil.compressToZip(file, zipFile)
    }

    fun downloadFile() {
        //校验 md5
        //解压缩文件到目标位置
    }
}