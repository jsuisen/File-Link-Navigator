package com.saysawgames.filelinknavigator;

import java.util.ArrayList;
import java.util.List;

import com.saysawgames.filelinknavigator.Constant;

public class DocLinkParser {

    /**
     * 文档链接匹配结果
     */
    public static class DocLinkMatch {
        public final String tagName;        // 标签名（不含@和:）
        public final String filePath;       // 文件路径
        public final String lineNumber;     // 行号（可能为null）
        public final int start;             // 起始位置
        public final int end;               // 结束位置
        public final String fullMatch;      // 完整匹配的文本

        public DocLinkMatch(String tagName, String filePath, String lineNumber,
                            int start, int end, String fullMatch) {
            this.tagName = tagName;
            this.filePath = filePath;
            this.lineNumber = lineNumber;
            this.start = start;
            this.end = end;
            this.fullMatch = fullMatch;
        }

        @Override
        public String toString() {
            return String.format("DocLinkMatch{tag=%s, path=%s, line=%s, pos=[%d,%d)}",
                tagName, filePath, lineNumber, start, end);
        }
    }

    /**
     * 解析注释文本中的所有文档链接
     */
    public static List<DocLinkMatch> parseDocLinks(String commentText) {
        List<DocLinkMatch> matches = new ArrayList<>();

        if (commentText == null || commentText.isEmpty()) {
            return matches;
        }

        int pos = 0;
        while (pos < commentText.length()) {
            DocLinkMatch match = findNextDocLink(commentText, pos);
            if (match != null) {
                matches.add(match);
                pos = match.end;
            } else {
                pos++;
            }
        }

        return matches;
    }

    /**
     * 从指定位置查找下一个文档链接
     */
    private static DocLinkMatch findNextDocLink(String text, int startPos) {
        // 1. 查找标签
        TagMatch tagMatch = findTag(text, startPos);
        if (tagMatch == null) {
            return null;
        }

        int pos = tagMatch.endPos;

        // 2. 跳过标签后的空白字符
        while (pos < text.length() && isWhitespace(text.charAt(pos))) {
            pos++;
        }

        if (pos >= text.length()) {
            return null;
        }

        // 3. 解析文件路径
        int pathStart = pos;
        while (pos < text.length() && isValidPathChar(text.charAt(pos))) {
            pos++;
        }

        if (pos == pathStart) {
            return null; // 没有文件路径
        }

        String filePath = text.substring(pathStart, pos);
        String lineNumber = null;
        int finalPos = pos;

        // 4. 尝试解析行号部分（可选）
        if (pos < text.length()) {
            LineNumberMatch lineMatch = parseLineNumber(text, pos);
            if (lineMatch != null) {
                lineNumber = lineMatch.lineNumber;
                finalPos = lineMatch.endPos;
            }
        }

        String fullMatch = text.substring(tagMatch.startPos, finalPos);

        return new DocLinkMatch(
            tagMatch.tagName,
            filePath,
            lineNumber,
            tagMatch.startPos,
            finalPos,
            fullMatch
        );
    }

    /**
     * 标签匹配结果
     */
    private static class TagMatch {
        String tagName;
        int startPos;
        int endPos;

        TagMatch(String tagName, int startPos, int endPos) {
            this.tagName = tagName;
            this.startPos = startPos;
            this.endPos = endPos;
        }
    }

    /**
     * 查找标签
     */
    private static TagMatch findTag(String text, int startPos) {
        for (int i = startPos; i < text.length(); i++) {
            for (String tag : Constant.getValidTags()) {
                if (text.startsWith(tag, i)) {
                    // 提取标签名，去掉@和:
                    String tagName = tag.substring(1, tag.length() - 1);
                    return new TagMatch(tagName, i, i + tag.length());
                }
            }
        }
        return null;
    }

    /**
     * 行号匹配结果
     */
    private static class LineNumberMatch {
        String lineNumber;
        int endPos;

        LineNumberMatch(String lineNumber, int endPos) {
            this.lineNumber = lineNumber;
            this.endPos = endPos;
        }
    }

    /**
     * 解析行号部分：[:#] [空白] [L] 数字
     */
    private static LineNumberMatch parseLineNumber(String text, int pos) {
        // 跳过前导空白
        while (pos < text.length() && isWhitespace(text.charAt(pos))) {
            pos++;
        }

        if (pos >= text.length()) {
            return null;
        }

        // 检查分隔符 : 或 #
        char ch = text.charAt(pos);
        boolean isValidChar = false;
        for (char c : Constant.getValidNumberSplit()) {
            if (c == ch) {
                isValidChar = true;
                break;
            }
        }
        if (!isValidChar) {
            return null;
        }

        pos++;

        // 跳过分隔符后的空白
        while (pos < text.length() && isWhitespace(text.charAt(pos))) {
            pos++;
        }

        if (pos >= text.length()) {
            return null;
        }

        // 可选的 L 前缀
        for (char c : Constant.getValidNumberPrefix()) {
            if (text.charAt(pos) == c) {
                pos++;
                break;
            }
        }

        if (pos >= text.length()) {
            return null;
        }

        // 解析数字
        int digitStart = pos;
        while (pos < text.length() && isDigit(text.charAt(pos))) {
            pos++;
        }

        if (pos == digitStart) {
            return null; // 没有找到数字
        }

        String lineNumber = text.substring(digitStart, pos);
        return new LineNumberMatch(lineNumber, pos);
    }

    /**
     * 判断字符是否为有效的路径字符
     * 包括：字母、数字、中文、路径分隔符、点、下划线、连字符
     */
    private static boolean isValidPathChar(char c) {
        // 英文字母和数字
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
            return true;
        }

        // 中文字符范围
        if (c >= '\u4e00' && c <= '\u9fa5') {
            return true;
        }

        // 路径分隔符和特殊字符
        if (c == '/' || c == '\\' || c == '.' || c == '_' || c == '-') {
            return true;
        }

        return false;
    }

    /**
     * 判断是否为空白字符（空格、制表符）
     */
    private static boolean isWhitespace(char c) {
        return c == ' ' || c == '\t';
    }

    /**
     * 判断是否为数字
     */
    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

}
