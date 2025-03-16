# 基于 Kotlin KMP 实现 鸿蒙HarmonyOS 与 Android 双平台 SDK 开发实践

# 背景

随着鸿蒙平台的进一步发展，大家的态度也逐渐从观望转向实际投入，越来越多的公司开始考虑将自家应用迁移到鸿蒙平台。但是这一过程并非想象中的那么简单，尤其对于已经存在很多年的大型项目来说，直接投入大量人力物力重新开发一个鸿蒙版本的应用成本过高，短时间内难以实现。对于小公司而言，这种成本压力更是难以承受。

因此，许多公司倾向于使用跨平台技术，例如 React Native（RN）和 Flutter。但抛开性能这些先不说，使用这两种技术将 App 迁移到鸿蒙平台存在两个严重问题：

1. **兼容性问题**：鸿蒙 OS 的底层架构与 Android 存在一定差异，RN 和 Flutter 在鸿蒙上的生态尚未成熟，遇到 API 兼容性问题，非常影响应用的稳定性。
2. **技术栈转换成本**：如果现有业务已经使用 RN 或 Flutter，迁移到鸿蒙仍然需要大量适配工作。如果未使用这些技术，直接将整个项目改造成 RN 或 Flutter 的技术栈再进行迁移，成本更是难以接受。

面对这样的情况，Kotlin 的 Kotlin Multiplatform（KMP）技术进入了大家的视野。1）Kotlin 本身就是 Android 端的 native 语言，可以将 Android 端的代码以较低的成本改造为 KMP 跨平台项目。2）Android 平台和鸿蒙平台相似度也较高，适配时会相对容易不少。例如两个平台的 webview 容器接口、对外的 callback 接口，几乎可以视为一模一样只是名字不同。3）使用 KMP 的构建产物是各个平台的 native 代码，接入时各个端只是接入一个各自平台的 library，不需要接入额外的库。接入成本也低。

因此，使用 KMP 技术来迁移 Android 端至鸿蒙平台成为了一种极具吸引力的方案。



在跨平台开发中，尤其是针对 Android 和 HarmonyOS 双端 SDK 的开发，我们需要解决一些关键问题，以确保逻辑的统一性和平台兼容性。以下是在开发过程中应重点考虑的几个问题：

## 问题

1. **两端的 context 如何抹平？**
   Android 和 HarmonyOS 的 `Context` 机制存在差异，如何在 KMP 项目中抹平这种差异，使得上层逻辑无需关心平台的具体实现？
2. **在这种跨端项目中如何实现异步操作？两端的异步操作如何平衡？**
   异步操作是 SDK 开发中的常见需求，但 Android 和 HarmonyOS 的异步机制（如 Android 的 `Coroutine` 和 HarmonyOS 的 `TaskDispatcher`）不同，如何在 KMP 中实现统一的异步调用？
3. **KMP 项目如何调用底层的，特别是系统的 API？**
   KMP 的 common 模块无法直接调用平台特定的 API，如何通过 `expect` 和 `actual` 机制实现对系统 API 的调用？
4. **如何处理平台特定的功能实现？**
   例如，计算文件的 MD5 值，Android 和 HarmonyOS 的实现方式不同。在这种情况下，如何在各自平台实现特定逻辑，并通过 KMP 项目的 commonMain 部分统一调用？

## 编写示例 Demo

为了更好地说明上述问题的解决方案，这里设计了一个简单的示例 Demo。 Demo 实现了一个名为 `FileManager` 的 SDK（它不包含 UI），仅提供纯逻辑功能。`FileManager` 的主要功能包括：

- **判断文件是否存在**：根据传入的文件路径，判断文件是否存在。
- **写入内容**：向指定文件写入内容。
- **文件压缩**：对指定文件进行压缩。
- **文件解压缩**：对指定压缩文件进行解压。

尝试通过这个 Demo讲清楚在具体的项目实施中解决以上几个问题。接下来，逐步实现这个 Demo，并详细说明每个步骤的关键点。

## 准备工作

### 创建KMP 项目

这个项目包括的是 Android 端的 demo 工程和 Kotlin Multiplatform 的核心代码。使用 Android Studio 来创建

首先检查插件，如果没有安装。Android studio 先安装插件 Kotlin Multiplatform。

接着 File->New->New Project 创建 KMP 项目工程。此时新创建的项目缺失了鸿蒙（即jsMain）部分。1）模仿 androidMain 、iosMain 部分的结构创建 jsMain target 部分目录和初始platform 文件。如下图所示：

<img src="https://cdn.jsdelivr.net/gh/eleven-max/picgo@main/image-20250117134045572.png" style="zoom:50%;" />

2）在 shared 的 build.gradle.kts文件中增加 js 对应的配置：

```kotlin
js(IR) {
        moduleName = "kmp-core-kit"
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions.freeCompilerArgs.add("-Xerror-tolerance-policy=SYNTAX")
            }
            if(this.compilationName == "main") {
                packageJson {
                    name = "kmp-core-kit"
                    version = "0.0.1"
                }
            }
        }

        generateTypeScriptDefinitions()
        useEsModules()
        nodejs()
        binaries.executable()
    }
```

### 创建鸿蒙Demo工程

这里的工程包括鸿蒙的 demo 工程，还有从 KMP 项目中将 kotlin 编译成的 js 代码。为了使用方便这一部分代码会作为主工程的 library 接入。

DevEco-Studio New 出工程，命名为 HarmonyExample。接着 New Project Module，类型选择 Shared Library。子模块名称随意，本次直接采用默认名称 **library**。

然后在 entry 的 oh-package.json5 文件中配置 dependencies 节点，内容如下图所示。

![](https://cdn.jsdelivr.net/gh/eleven-max/picgo@main/image-20250117181456472.png)

### 创建karakum工程

该工程用于在编码过程中将鸿蒙的*.d.ts 文件转换为 kt 文件，在 KMP 工程中使用。Github 地址：https://github.com/karakum-team/karakum

本地演示采用命令行模式来使用该工具。创建一个目录，然后执行指令

```
npm install karakum typescript -D
```

接着创建 input、output 目录。创建一个名为karakum.config.json 的文件，内容配置为：

```
{
    "input": "input/*", //输入目录放置*.d.ts 文件
    "output": "output", //输出目录，用于存放转换生成的*.kt 文件
    "libraryName": "ohos" //生成的kt文件的包名
}
```

配置完成后，工程结构如下图所示：

![](https://cdn.jsdelivr.net/gh/eleven-max/picgo@main/image-20250117183118889.png)

使用时只需要将需要转换的.d.ts 文件放置在 input 目录下，执行 如下命令即可。

```
npx karakum --config karakum.config.json
```

## 开始编码

整个项目比较简单，模拟 app 下载 zip 到本地后，解压缩、校验解压缩后文件的 md5 值，这个场景。只是文件不是从远端下载而是本地生成一个 zip 文件供模拟使用。

1. 文件根路径的处理，或者说平台 context 如何处理？

   对于 Android 平台获取一个文件操作路径可以这样操作：

   ```
   val rootPath = applicationContext.getExternalFilesDir("kmp_core")?.absolutePath ?: ""
   ```

   鸿蒙平台也有类似的写法：

   ```
   const rootDir = getContext(this).filesDir +"/kmp-core"
   ```

   问题在于两个平台的 context 不同，想抹平差异还是有点困难。针对这种场景我们想了两个方案，1）如果 context 是在程序中持续要使用的，那么就编写一个 set 方法将各自平台的 context 注入到对应的 target。例如 Android 平台的就注入到 androidMain，鸿蒙的就注入到 jsMain；2）规避 context 的注入。例如我们目前Demo 的场景，我们只是需要一个文件根路径，那么完全可以在函数中增加入参，将 root 路径以一个 String 的形式注入到 commonMain 部分。

   因此我们在 commonMain  target 部分，创建一个类 FileManager。设计一个函数 prepare，入参为 string 类型。

   ```
   class FileManager {
       fun prepare(path: String) {
       }
   ```

   那么在 Android 端的业务部分调用 FileManager 的时候就可以写为：

   ```
   val rootPath = applicationContext.getExternalFilesDir("kmp_core")?.absolutePath ?: ""
   val fileManager = FileManager()
   fileManager.prepare(rootPath)
   ```

   鸿蒙平台调用时代码写为：

   ```
   const rootDir = getContext(this).filesDir +"/kmp-core"
   const fileManager = new FileManager()
   fileManager.prepare(rootDir)
   ```

2. 如何调用系统能力，系统 API

   很多时候我们都需要调用系统能力，在这个 demo 里用一个日志打印功能来举例。

   首先在 commonMain 里定义：

   ```
   expect fun platformLog(tag: String, message: String)
   ```

   为了方便使用接着可以封装一个 log 模块：

   ```
   object ELog {
   
       fun v(message: String) {
           v("KMPCore", message)
       }
   
       fun v(tag: String, message: String) {
           platformLog(tag, message)
       }
   }
   ```

   第二步就是如何调用底层系统的 API 来实现各个端上的真实的打印日志能力。

   Android 端非常容易，在 androidMain 的 Platform.android.kt 文件中，按照 KMP 的 expect/actual 协议来实现 platformLog 函数,并且在函数内直接调用android.util.Log 系统 API 即可。代码如下：

   ```
   actual fun platformLog(tag: String, message: String) {
       Log.v(tag, message)
   }
   ```

   鸿蒙端就稍微有点复杂。同样按照 expect/actual 协议来操作，在 jsMain target 部分的 Platform.js.kt 文件中我们可以做如下实现：

   ```
   actual fun platformLog(tag: String, message: String) {
       
   }
   ```

   此时有个问题就是我们没有鸿蒙端对应的 kt 文件，无法实现这里的逻辑。我们借助于karakum 工具来实现鸿蒙平台调用底层日志打印的 kt 文件。

   * 鸿蒙平台打印日志的模块为 @ohos.hilog.   在HarmonyExample 工程中随便找个*.ets 文件顶部写import hilog from '@ohos.hilog'; 点进去就能找到@ohos.hilog.d.ts 文件。

   * 将@ohos.hilog.d.ts 文件 拷贝到karakum工程的 input 目录下。执行命令：npx karakum --config karakum.config.json。 在karakum工程的 output 目录下就会生成我们需要的@ohos.hilog.kt 文件

   * 接着将@ohos.hilog.kt 文件拷贝到 KMP 工程的 jsMain target。（为了方便管理，特意在karakum 的工程配置中增加了包路径ohos，所以这里拷贝来的文件需要放置在 ohos 目录下）

     <img src="https://cdn.jsdelivr.net/gh/eleven-max/picgo@main/image-20250124121538210.png" style="zoom:50%;" />

   * 这时的 kt文件还需要做进一步的修改：1）注释调顶部的@file:JsModule("ohos/@ohos.hilog")  2）增加JsModule 配置。

     ```
     @JsModule("@ohos.hilog")
     @JsNonModule
     external object hilog {
     ```

   * 最终回到 Platform.js.kt 文件，就可以调用 hilog来实现 KMP 调用鸿蒙平台的日志打印 API 功能

     ```
     import ohos.hilog
     actual fun platformLog(tag: String, message: String) {
         hilog.debug(0.0, tag, message)
     }
     ```

3. 如何将基于系统 API 封装的功能模块，提供给 KMP 的 commonMain 调用

   在实际的开发过程中，调用各个端的能力不可能像 log 打印一样简单。各个端的差异比较大，还是很难抹平的。例如给文件计算 md5 值，每个端的实现差异还是比较大的。

   面对这种情况，我们采用各个端来实现具体的逻辑，common 中只是一个空函数。

   在 commonMain target 的 Platform.kt 文件中定义：

   ```
   expect fun platformGetFileMd5(filePath: String): String
   ```

   在 androidMain target 的 Platform.android.kt 文件中实现 android 端的逻辑

   ```
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
   ```

   这种场景下鸿蒙端可以重复类似 log 模块的流程，将计算 md5 需要的各个kt 类都凑齐，然后在 jsMain target 的 Platform.js.kt 文件中实现和 Android 端同样的逻辑。只是这种比较麻烦，而且后期不容易调试。

   这里采用另外一种方式。

   如图所示：

   <img src="https://cdn.jsdelivr.net/gh/eleven-max/picgo@main/image-20250124162630113.png" style="zoom:50%;" />

   * 首先我们在鸿蒙端FileUtil 类，对应两个文件，FileUtil.ts 和 FileUtil.d.ts

   * 在FileUtil.ts 中实现具体的鸿蒙端计算 md5 的内容逻辑：

     ```
     import fs from '@ohos.file.fs'
     import util from '@ohos.util'
     import zlib from '@ohos.zlib'
     import hilog from '@ohos.hilog';
     
     import { cryptoFramework } from '@kit.CryptoArchitectureKit'
     import { buffer } from '@kit.ArkTS'
     
     class FileUtil {
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
     }
     export default FileUtil;
     ```

     在 FiltUtil.d.ts 文件中做声明：

     ```
     declare class  FileUtil {
       public static getFileMd5(filePath: string): string;
     }
     export default FileUtil;
     ```

   * 重复之前的步骤，将FiltUtil.d.ts 文件拷贝到karakum 生成对应的 kt 文件

   * 将 FiltUtil.kt 文件拷贝到 KMP 工程中，如图：

     <img src="https://cdn.jsdelivr.net/gh/eleven-max/picgo@main/image-20250124163911721.png" style="zoom:50%;" />

     再做修改：

     ```
     // Generated by Karakum - do not modify it manually!
     
     //@file:JsModule("ohos/FileUtil")
     @file:Suppress(
         "NON_EXTERNAL_DECLARATION_IN_INAPPROPRIATE_FILE",
     )
     
     package ohos
     
     @JsModule("./ohos/FileUtil")
     @JsNonModule
     external class FileUtil {
     
     companion object {
     fun getFileMd5(filePath: String): String
     }
     }
     /* export default FileUtil; */
     ```

     （这里有个小发现，JsModule 部分如果写为：@JsModule("./ohos/FileUtil") 不直接用@JsModule("ohos/FileUtil"）。要不然构建导出的js 文件会报错，找不到我们在鸿蒙端定义的 FiltUtil类。加上./后，构建生成的 js 文件中就会长这样```import FileUtil from './ohos/FileUtil';```,免于后期修正)

   * FileUtil.kt 生成了剩下的就很简单，在 Platform.js.kt 中直接调用即可：

     ```
     import ohos.FileUtil
     actual fun platformGetFileMd5(filePath: String): String {
         return FileUtil.getFileMd5(filePath)
     }
     ```

4. 耗时操作的异步

   1. 在 commonMain 中异步，或者说开线程

      在项目开发中，耗时操作异步处理几乎是必不可少的。在 KMP 项目中相比于文章前面的几个模块功能，因为有官方封装的 kotlin 协程库异步反倒更简单一点。

      * 先引入协程库：

        ```
        commonMain.dependencies {
           //put your multiplatform dependencies here
           implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
        }
        ```

      * 然后使用即可：

        ```
        private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        
        fun prepare(path: String) {
          coroutineScope.launch {
          	//todo
          }
        }
        ```

   2. 平台本身就要求异步调用，如何抹平差异？

      各个平台的具体逻辑实现为同步式，在 commonMain 部分做异步操作，是比较合理且通用的一种开发方式。例如在鸿蒙平台我们实现文件的 md5 计算，均调用**fs.openSync**这样的同步 API来实现。

      但凡事都怕例外，在开发过程中发现，一些操作在系统层面就是个异步操作还没有同步式的接口暴露。例如在鸿蒙平台做文件压缩操作。

      ```
      function compressFile(inFile: string, outFile: string, options: Options): Promise<void>;
      ```

      开始面对这种情况感觉有点棘手。习惯性的采用 Callback 回调方式来处理，Android 和鸿蒙端采用回调方式将结果送到 commonMain，回到 commonMain 后，使用协程的suspendCoroutine 方式将异步转同步来实现逻辑。

      后来发现了一个更优雅的方式。官方的协程库已经和 promise 做了很好的处理。

      * 第一步定义expect/actual 接口。只不过这次定义时增加suspend 关键字。

        ```
        expect suspend fun platformCompressToZip(sourceFile: String, outputZipPath: String): Boolean
        
        expect suspend fun platformUnZipFile(zipFilePath: String, targetDir: String): Boolean
        ```

        android 端非常简单，实现对应的函数，逻辑和之前一样直接填充进去就行。

        ```
        actual suspend fun platformCompressToZip(
            sourceFile: String,
            outputZipPath: String
        ): Boolean
        ```

      * 第二步，鸿蒙端在 FileUtil.d.ts 文件中做如下定义：

        ```
        public static compressToZip(sourceFile: string, outputZipPath: string): Promise<boolean>;
        ```

        在 FileUtil.ts 文件中实现具体逻辑。

        ```
        public static compressToZip(sourceFile: string, outputZipPath: string): Promise<boolean> {
          return new Promise<boolean>((resolve, reject) => {
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
        ```

        注：这里为了对齐 boolean 做返回值这个逻辑，又用自己的 Promise 将系统的封装了一层。

      * 第三步，再次生成FileUtil.kt 文件，其中对应的函数为：

        ```
        fun compressToZip(sourceFile: String, outputZipPath: String): Promise<Boolean>
        ```

        接着在 Platform.js.kt 中使用

        ```
        actual suspend fun platformCompressToZip(
            sourceFile: String,
            outputZipPath: String
        ): Boolean {
            return FileUtil.compressToZip(sourceFile, outputZipPath).await()
        }
        ```

        借助于协程库对 Promise 的封装，这里调用 Promise 的 await 方法，简化逻辑，比较优雅的实现了两端同步、异步不同实现的抹平。


## 总结

最后来尝试回答文章开头的几个问题：

1. **两端的 context 如何抹平？**

   context 的处理，方式 1；就是回避这个问题，例如上面的 demo 中，获取 context 是为了获取文件处理的路径。那么选择将 root 根路径直接从函数接口注入即可。方式 2；将各个平台的 context 注入到对应的平台 target 部分。例如 android 的注入到 androidMain 部分，commonMain 再通过定义`expect` /`actual`来调用 android 端的能力。

2. **在这种跨端项目中如何实现异步操作？两端的异步操作如何平衡？**
   异步操作直接调用官方封装的协程库，各个端的实现逻辑尽量采用同步式写法。如果不能，例如鸿蒙端的文件压缩逻辑，那么就封装为 Promise，然后在 Platform.js.kt 中调用协程库的 await 等函数来做抹平操作。

3. **KMP 项目如何调用底层的，特别是系统的 API？**
   和 demo 中的 log 模块一样的处理逻辑。android 端直接调用，鸿蒙端则根据其接口定义生成对应的*.kt 文件，在 jsMain中调用。

4. **如何处理平台特定的功能实现？**
   稍显复杂的逻辑，例如 demo 中计算文件 md5，压缩文件等功能，采用在commonMain 中定义`expect` /`actual`接口，各个端实现具体逻辑的方式来开发。



# 其他

1. 跨端框架有很多，例如 flutter、RN 也可以做到跨端，也能覆盖到鸿蒙平台。Kotlin KMP 的不同点在哪？

   目前接触下来最大的感觉是 KMP 更靠近底层 native 层，Flutter、RN 偏向于 UI 和业务。逻辑层的代码可以用 KMP 做统一。

   并且它的另外一个优势就是接入成本低，性能仅次于各个端 native 代码。KMP 的目标产物是各个平台的 native 代码，各端接入只是相当于多了个 library。Android 端，代码可以直接编译为 aar；鸿蒙端编译为*.js 代码；直接引入即可不需要额外集成其他的虚拟机、或者 skia 这样的库；侵入性也低。

2. 整个 KMP 项目的开发调试还是有很多不足，android 端可以做断点调试，其他的例如 js（鸿蒙）、ios 比较难处理，只能采用打印 log 的方式进行。因此除了 android 端，其他端的 Platform.xxx.kt 文件中尽量减少逻辑代码，真正的逻辑实现都在各自端实现方便调试，只是将生成的协议 kt 文件放置在项目中，供 KMP 项目调用。

3. 对数据类型的处理：在跨平台通信时，String（字符串）和 Boolean（布尔值）是最稳定的类型，不容易因为平台差异而出现兼容性问题。因此，在定义跨端接口（如 common 层的 API）时，接口使用 String 和 Boolean 作为入参和返回值。如果需要传递列表数据，可以使用 类似List<String>的定义，这样在 Android、iOS、鸿蒙各个端都能保持一致，避免解析错误。

   性能需权衡：这样的设计肯定会有性能问题，JSON的序列化/反序列化可能成为性能瓶颈，复杂场景可考虑二进制协议；

   过度使用String可能导致类型擦除问题，建议配合kotlinx.serialization使用。

   重要数据建议做Base64编码避免解析问题。

4. 目前平台功能还不够完善，很多复杂功能需要自己构建 native 的实现供 common 调用。这种情况下通过“协议”方式穿透各端。在调用各平台的复杂功能（如网络请求）时，可以采用协议封装的方式，让 common 层使用 JSON 结构传递信息，各端再解析后调用各自的原生实现。例如，可以定义如下 JSON 格式的请求体：

   {
       "url": "https://api.example.com/data",
       "method": "POST",
       "header": "{Authorization: Bearer token}",
       "body": "{'key': 'value'}"
   }

   在 common 层，这个 JSON 字符串会作为统一的协议传递到 Android、iOS 或鸿蒙其他端，各端解析后利用原生的网络库来执行实际的请求。

5. 示例 demo 完整代码：https://github.com/eleven-max/kmp-harmony-android-sdk-sample

6. 参考资料：

* https://kotlinlang.org/docs/js-ir-compiler.html
* https://juejin.cn/post/7404858270513152000
* https://juejin.cn/post/7379059228105621556

