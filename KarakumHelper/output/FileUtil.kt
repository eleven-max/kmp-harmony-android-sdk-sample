// Generated by Karakum - do not modify it manually!

@file:JsModule("ohos/FileUtil")
@file:Suppress(
    "NON_EXTERNAL_DECLARATION_IN_INAPPROPRIATE_FILE",
)

package ohos

external class FileUtil {

companion object {
fun makeDir(path: String): Boolean
fun createFile(path: String): Boolean
fun writeFile(filePath: String, content: String): Boolean
fun getFileMd5(filePath: String): String
fun compressToZip(sourceFile: String, outputZipPath: String): Promise<Boolean>
fun unZipFile(zipFilePath: String, targetDir: String): Promise<Boolean>
}
}

/* export default FileUtil; */
