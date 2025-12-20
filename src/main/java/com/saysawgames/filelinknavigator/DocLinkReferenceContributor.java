package com.saysawgames.filelinknavigator;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 注册文档链接引用的贡献者
 * 使 IDEA 能识别注释中的文档链接，支持 Ctrl+点击导航
 */
public class DocLinkReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        PsiReferenceProvider provider = new PsiReferenceProvider() {
            @Override
            public PsiReference @NotNull [] getReferencesByElement(
                @NotNull PsiElement element,
                @NotNull ProcessingContext context) {

                List<PsiReference> references = new ArrayList<>();

                // 获取当前元素的文本（对于 PsiDocToken，它只包含纯文本，不含前缀的 *）
                String text = element.getText();

                List<DocLinkParser.DocLinkMatch> matches = DocLinkParser.parseDocLinks(text);
                for (DocLinkParser.DocLinkMatch match : matches) {
                    String filePath = match.filePath;
                    // 移除路径中的双引号（处理Javadoc注释中的引号）
                    String lineNumberString = match.lineNumber;
                    int startOffset = match.start;
                    int endOffset = match.end;

                    // 创建相对于当前元素（Token）的 Range
                    TextRange range = new TextRange(startOffset, endOffset);

                    filePath = Utils.formatPath(filePath);
                    int lineNumber = Integer.parseInt(lineNumberString == null ? "1" : lineNumberString);

                    // 直接使用当前 element (它是 PsiDocToken 或 PsiComment)
                    references.add(new DocLinkReference(element, range, filePath, lineNumber));
                }

                return references.toArray(new PsiReference[0]);
            }
        };

        // 注册范围扩展：
        registrar.registerReferenceProvider(
            PlatformPatterns.or(
                // 1. 普通注释 (// ...)
                PlatformPatterns.psiElement(PsiComment.class),
                // 2. Javadoc 中的普通文本 (处理 "see @doc:...")
                PlatformPatterns.psiElement(PsiDocToken.class),
                // 3. 【新增】Javadoc 标签 (处理行首的 "@doc:...")
                PlatformPatterns.psiElement(PsiDocTag.class)
            ),
            provider
        );
    }
}
