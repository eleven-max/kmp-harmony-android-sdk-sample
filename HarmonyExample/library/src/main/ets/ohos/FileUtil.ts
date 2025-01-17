import fs from '@ohos.file.fs'
import util from '@ohos.util'
import zlib from '@ohos.zlib'

import { cryptoFramework } from '@kit.CryptoArchitectureKit'
import { buffer } from '@kit.ArkTS'


class FileUtil {

  public static makeDir(path: string): boolean {
    let temp = ''
    let array = path.split('/')
    array.forEach((value, Index, array) => {
      if(value != "") {
        temp = temp + "/" + value
        if(!fs.accessSync(temp)){
          fs.mkdirSync(temp)
        }
      }
    })
    return true
  }
  public static createFile(path: string): boolean {
    let file = fs.openSync(path, fs.OpenMode.READ_WRITE | fs.OpenMode.CREATE)
    // fs.writeSync(file.fd, enResult.buffer)
    fs.closeSync(file)
    return true
  }

  public static writeFile(filePath: string, content: string): boolean {
    let textEncoder = util.TextEncoder.create('utf-8')
    let enResult = textEncoder.encodeInto(content)

    let file = fs.openSync(filePath, fs.OpenMode.READ_WRITE | fs.OpenMode.CREATE)
    fs.writeSync(file.fd, enResult.buffer)
    fs.closeSync(file)
    return true
  }

  public static getFileMd5(filePath: string): string {
      let md5 = cryptoFramework.createMd("MD5")
    let file = fs.openSync(filePath, fs.OpenMode.READ_ONLY)
    let arrayBuffer = new ArrayBuffer(2048)
    let len : number = 0
    let position: number = 0
    do {
      len = fs.readSync(file.fd, arrayBuffer, {offset: position})
      if( len > 0) {
        let uint8Array = new Uint8Array(arrayBuffer.slice(0, len))
        md5.updateSync({data: uint8Array})
        position += len
      }
    } while (len > 0)
    let mdResult = md5.digestSync()

    return Array.from(mdResult.data).map(byte => byte.toString(16).padStart(2, '0')).join('')
  }
  public static compressToZip(sourceFile: string, outputZipPath: string): Promise<boolean> {
    return new Promise<boolean>((resolve, reject) => {
      // if(!fs.accessSync(outputZipPath)){
      //   this.makeDir(outputZipPath)
      // }
      var options = {
        level: zlib.CompressLevel.COMPRESS_LEVEL_DEFAULT_COMPRESSION,
        memLevel: zlib.MemLevel.MEM_LEVEL_DEFAULT,
        strategy: zlib.CompressStrategy.COMPRESS_STRATEGY_DEFAULT_STRATEGY
      }
      zlib.compressFile(sourceFile, outputZipPath, options).then(success => {
        resolve(true)
      }).catch(error => {
        console.log(error)
        reject(false)
      })
    })
  }
  public static unZipFile(zipFilePath: string, targetDir: string): Promise<boolean> {
    return new Promise<boolean>((resolve, reject) => {
      if(!fs.accessSync(targetDir)){
        this.makeDir(targetDir)
      }
      zlib.decompressFile(zipFilePath, targetDir).then(success => {
        resolve(true)
      }).catch(error => {
        reject(false)
      })
    })
  }
}

export default FileUtil;