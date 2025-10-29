package service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileService {

    /*
     * Получить список папок из папки
     * */
    public static List<File> getFolders(String path) {
        List<File> folders = new ArrayList<>();
        File directory = new File(path);

        if (directory.exists() && directory.isDirectory()) {
            File[] contents = directory.listFiles();
            if (contents != null) {
                for (File file : contents) {
                    if (file.isDirectory()) {
                        folders.add(file);
                    }
                }
            }
        }
        return folders;
    }


    /*
     * Получить список файлов из папки
     * */
    public static List<File> getFiles(String path) {
        List<File> files = new ArrayList<>();
        File directory = new File(path);

        if (directory.exists() && directory.isDirectory()) {
            File[] contents = directory.listFiles();
            if (contents != null) {
                for (File file : contents) {
                    if (file.isFile()) {
                        files.add(file);
                    }
                }
            }
        }
        return files;
    }

    /*
     * Проверка на изображение
     * */
    public static boolean isImage(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                name.endsWith(".png");
    }

    /*
    * Проверка существования файла/папки
    * */
    public static boolean pathExists(String path) {
        File file = new File(path);
        return file.exists();
    }
}
