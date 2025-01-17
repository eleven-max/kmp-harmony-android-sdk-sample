declare class  FileUtil {
  public static makeDir(path: string): boolean;
  public static createFile(path: string): boolean;
  public static writeFile(filePath: string, content: string): boolean;
  public static getFileMd5(filePath: string): string;
  public static compressToZip(sourceFile: string, outputZipPath: string): Promise<boolean>;
  public static unZipFile(zipFilePath: string, targetDir: string): Promise<boolean>;
}

export default FileUtil;