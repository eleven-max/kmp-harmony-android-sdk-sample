type Nullable<T> = T | null | undefined
export declare class FileManager {
    constructor();
    prepare(path: string): void;
    downloadFile(path: string): void;
}