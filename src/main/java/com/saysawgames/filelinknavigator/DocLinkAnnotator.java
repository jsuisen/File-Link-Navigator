package com.saysawgames.filelinknavigator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

/**
 * 扫描 Java 注释，识别文档链接并添加可点击的注解
 */
public class DocLinkAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        // 处理所有类型的注释元素
        // PsiComment: // 单行注释和 /* */ 块注释
        // PsiDocComment: /** */ JavaDoc 注释 (继承自 PsiComment)
        if (!(element instanceof PsiComment)) {
            return;
        }

        PsiComment comment = (PsiComment) element;
        String commentText = comment.getText();

        List<DocLinkParser.DocLinkMatch> matches = DocLinkParser.parseDocLinks(commentText);
        for (DocLinkParser.DocLinkMatch match : matches) {
            String filePath = match.filePath;
            // 移除路径中的双引号（处理Javadoc注释中的引号）
            String lineNumberString = match.lineNumber;
            int startOffset = comment.getTextRange().getStartOffset() + match.start;
            int endOffset = comment.getTextRange().getStartOffset() + match.end;

            // 移除路径中的双引号（处理Javadoc注释中的引号）
            filePath = Utils.formatPath(filePath);
            int lineNumber = Integer.parseInt(lineNumberString == null ? "1" : lineNumberString);

            TextRange linkRange = new TextRange(startOffset, endOffset);

            // 创建带超链接效果的注解
            // 使用 newAnnotation API 并设置为超链接样式
            holder.newAnnotation(HighlightSeverity.INFORMATION, "Navigate to " + filePath + ":" + lineNumber)
                .range(linkRange)
                .textAttributes(DefaultLanguageHighlighterColors.IDENTIFIER)  // 使用标准的标识符样式
                .withFix(new NavigateToDocIntentionAction(filePath, lineNumber))
                // 关键：添加高亮信息使其可以 Ctrl+点击
                .needsUpdateOnTyping(false)
                .create();

            // 额外创建一个自定义的高亮注解，使其看起来像链接
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(linkRange)
                .textAttributes(createLinkAttributes())
                .create();
        }
    }

    /**
     * 创建链接样式的文本属性（蓝色下划线）
     */
    private static TextAttributesKey createLinkAttributes() {
        TextAttributes attributes = new TextAttributes();
        attributes.setForegroundColor(new JBColor(new Color(0, 102, 204), new Color(100, 150, 255)));
        attributes.setEffectType(com.intellij.openapi.editor.markup.EffectType.LINE_UNDERSCORE);
        attributes.setEffectColor(new JBColor(new Color(0, 102, 204), new Color(100, 150, 255)));
        return TextAttributesKey.createTextAttributesKey("DOC_LINK", attributes);
    }
}
