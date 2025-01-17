import { FileManager} from 'library/src/main/ets/kmp-core-kit'
@Entry
@Component
struct Index {
  @State message: string = 'Hello World';

  aboutToAppear(): void {
    const rootDir = getContext(this).filesDir +"/kmp-core"
    const fileManager = new FileManager()
    fileManager.prepare(rootDir)
    fileManager.downloadFile(rootDir)
  }

  build() {
    RelativeContainer() {
      Text(this.message)
        .id('HelloWorld')
        .fontSize(50)
        .fontWeight(FontWeight.Bold)
        .alignRules({
          center: { anchor: '__container__', align: VerticalAlign.Center },
          middle: { anchor: '__container__', align: HorizontalAlign.Center }
        })
    }
    .height('100%')
    .width('100%')
  }
}