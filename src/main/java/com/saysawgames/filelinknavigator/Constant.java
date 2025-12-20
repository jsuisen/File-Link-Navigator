package com.saysawgames.filelinknavigator;

import com.saysawgames.filelinknavigator.settings.SettingsState;

import java.util.ArrayList;
import java.util.List;

public class Constant {
    // 默认的标签名列表，用,分隔
    public static final String DEFAULT_TAGNAMES = "@doc,@markdown,@see";
    // 默认的文件名与行号之间的分隔符列表，用,分隔
    public static final String DEFAULT_SEPARATORS = ":,#";
    // 默认的行号前缀，用,分隔
    public static final String DEFAULT_LINEPREFIXES = "L";

    private static String[] validTagsCache = null;
    private static char[] validNumberSplitCache = null;
    private static char[] validNumberPrefixCache = null;

    public static String[] getValidTags() {
        if (validTagsCache == null) {
            updateDocLinkPattern();
        }
        return validTagsCache;
    }

    public static char[] getValidNumberSplit() {
        if (validNumberSplitCache == null) {
            updateDocLinkPattern();
        }
        return validNumberSplitCache;
    }

    public static char[] getValidNumberPrefix() {
        if (validNumberPrefixCache == null) {
            updateDocLinkPattern();
        }
        return validNumberPrefixCache;
    }

    /**
     * 更新文档链接的正则表达式模式
     */
    public static synchronized void updateDocLinkPattern() {
        SettingsState settings = SettingsState.getInstance();
        if (settings != null) {
            String tagNameArray = settings.getTagNamesRegex();
            String splitArray = settings.getSeparatorsRegex();
            String linePrefixArray = settings.getLinePrefixesRegex();

            validTagsCache = (String[]) filterArray(tagNameArray, DEFAULT_TAGNAMES, "string");
            validNumberSplitCache = (char[]) filterArray(splitArray, DEFAULT_SEPARATORS, "char");
            validNumberPrefixCache = (char[]) filterArray(linePrefixArray, DEFAULT_LINEPREFIXES, "char");
        } else {
            // 如果无法获取设置，则使用默认值
            validTagsCache = DEFAULT_TAGNAMES.split(",", -1);
            validNumberSplitCache = new char[]{':', '#'};
            validNumberPrefixCache = new char[]{'L'};
        }
    }
    
    /**
     * 清除缓存，强制下次访问时重新加载设置
     */
    public static synchronized void clearCache() {
        validTagsCache = null;
        validNumberSplitCache = null;
        validNumberPrefixCache = null;
    }

    private static Object filterArray(String tagNameArray, String defaultArray, String returnType) {
        // 1. 先处理过滤逻辑，得到String类型的有效标签列表
        List<String> validTagList = new ArrayList<>();
        if (tagNameArray != null && !tagNameArray.trim().isEmpty()) {
            String[] tmp = tagNameArray.split(",", -1);
            for (String tag : tmp) {
                String trimmedTag = tag.trim();
                if (!trimmedTag.isEmpty()) {
                    validTagList.add(trimmedTag);
                }
            }
        }

        // 2. 处理默认值（如果过滤后无有效数据）
        String[] validStringArray;
        if (validTagList.isEmpty()) {
            validStringArray = defaultArray.split(",", -1);
            // 对默认值也做空元素过滤（保持逻辑一致）
            List<String> defaultList = new ArrayList<>();
            for (String tag : validStringArray) {
                String trimmedTag = tag.trim();
                if (!trimmedTag.isEmpty()) {
                    defaultList.add(trimmedTag);
                }
            }
            validStringArray = defaultList.toArray(new String[0]);
        } else {
            validStringArray = validTagList.toArray(new String[0]);
        }

        // 3. 根据指定类型返回对应数组
        if ("string".equalsIgnoreCase(returnType)) {
            return validStringArray;
        } else if ("char".equalsIgnoreCase(returnType)) {
            // 将String[] 转换为 char[]（每个String取第一个字符，若为空则跳过）
            List<Character> charList = new ArrayList<>();
            for (String s : validStringArray) {
                if (!s.isEmpty()) {
                    charList.add(s.charAt(0)); // 取每个标签的第一个字符
                }
            }
            // 转换为char数组
            char[] charArray = new char[charList.size()];
            for (int i = 0; i < charList.size(); i++) {
                charArray[i] = charList.get(i);
            }
            return charArray;
        } else {
            throw new IllegalArgumentException("Error returnType:" + returnType + "，Only 'string' or 'char'");
        }
    }
}
