package com.saysawgames.filelinknavigator;

public class Utils {

    // 移除路径中的双引号（处理Javadoc注释中的引号）
    public static String formatPath(String filePath) {
        return filePath.replaceAll("^\"|\"$", "");
    }

}
