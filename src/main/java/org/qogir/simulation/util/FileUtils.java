package org.qogir.simulation.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件读取工具类
 */
public class FileUtils {

    /**
     * 从 txt 文件中读取正则表达式，每行一个
     * 自动跳过空行和以 // 开头的注释行
     *
     * @param filePath 文件路径
     * @return 正则表达式数组
     * @throws IOException 文件读取异常
     */
    public static String[] readRegexFromTxt(String filePath) throws IOException {
        File file = new File(filePath);
        List<String> regexList = new ArrayList<>();
        
        // 按行读取文件
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // 跳过空行和注释行
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("//")) {
                    regexList.add(line);
                }
            }
        }
        
        // 转换为数组返回
        return regexList.toArray(new String[0]);
    }
}
