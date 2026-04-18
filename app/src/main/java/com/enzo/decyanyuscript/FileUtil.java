package com.enzo.decyanyuscript;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    /**
     * 判断路径是否存在
     */
    public static boolean exists(String path) {
        if (path == null || path.isEmpty()) return false;
        File file = new File(path);
        return file.exists();
    }
    /**
     * 递归遍历指定目录下包括子目录中指定后缀的文件路径。
     * * @param directoryPath 要开始遍历的目录路径（例如: "/sdcard/Download"）
     * @param extension 要查找的文件后缀名（例如: ".jpg" 或 ".mp4"）
     * @return 包含所有匹配文件绝对路径的 List<String>
     */
    public static List<String> findFilesWithExtension(String directoryPath, String extension) {
        List<String> filePaths = new ArrayList<>();
        File directory = new File(directoryPath);

        // 检查路径是否存在且是否是一个目录
        if (!directory.exists() || !directory.isDirectory()) {
            // 目录不存在或不是目录，返回空列表
            return filePaths;
        }

        // 确保后缀名以点号开头，方便匹配
        final String searchExtension = extension.startsWith(".") ? extension : "." + extension;

        // 调用私有递归方法开始遍历
        traverseDirectory(directory, searchExtension, filePaths);

        return filePaths;
    }

    /**
     * 私有递归方法：执行实际的目录遍历和文件匹配。
     * * @param currentDir 当前正在遍历的目录
     * @param extension 要查找的文件后缀名（已确保包含点号）
     * @param filePaths 存储找到的文件路径的列表
     */
    private static void traverseDirectory(File currentDir, String extension, List<String> filePaths) {
        // 获取当前目录下的所有文件和子目录
        File[] files = currentDir.listFiles();

        // 检查文件列表是否为空（权限问题或空目录）
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // 如果是目录，则递归调用自身，进入子目录
                traverseDirectory(file, extension, filePaths);
            } else if (file.isFile()) {
                // 如果是文件，则检查其后缀名是否匹配
                if (file.getName().toLowerCase().endsWith(extension.toLowerCase())) {
                    // 匹配成功，将文件的绝对路径添加到列表中
                    filePaths.add(file.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 检查给定的路径是否是一个存在的、可访问的目录。
     *
     * @param path 要检查的路径字符串。
     * @return 如果路径是一个存在的目录，则返回 true；否则返回 false。
     */
    public static boolean isDirectory(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        File file = new File(path);
        // 1. 检查文件/目录是否存在
        // 2. 检查它是否是一个目录
        return file.exists() && file.isDirectory();
    }

    /**
     * 检查给定的路径是否是一个存在的、可访问的普通文件。
     *
     * @param path 要检查的路径字符串。
     * @return 如果路径是一个存在的文件，则返回 true；否则返回 false。
     */
    public static boolean isFile(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        File file = new File(path);
        // 1. 检查文件/目录是否存在
        // 2. 检查它是否是一个普通文件 (排除目录、符号链接等特殊类型)
        return file.exists() && file.isFile();
    }

    /**
     * 综合判断给定的路径类型。
     * * @param path 要检查的路径字符串。
     * @return 返回路径的类型描述，例如："Directory", "File", 或 "Not Found"。
     */
    public static String getPathType(String path) {
        if (path == null || path.isEmpty()) {
            return "Invalid Path";
        }
        File file = new File(path);

        if (!file.exists()) {
            return "Not Found";
        }

        if (file.isDirectory()) {
            return "Directory";
        }

        if (file.isFile()) {
            return "File";
        }

        // 可能是符号链接、设备文件等其他特殊类型
        return "Other Type";
    }
}
