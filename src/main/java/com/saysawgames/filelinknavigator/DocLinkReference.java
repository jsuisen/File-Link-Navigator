package com.saysawgames.filelinknavigator;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.intellij.psi.impl.FakePsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文档链接的引用实现
 * 这个类使得链接可以被 Ctrl+点击识别和导航，并正确跳转到指定行号
 */
public class DocLinkReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {

    private final String filePath;
    private final int lineNumber;

    public DocLinkReference(@NotNull PsiElement element, TextRange rangeInElement,
                            String filePath, int lineNumber) {
        // 关键修复1: super 构造函数第三个参数设为 false，使其成为非软引用
        // 这样每次点击都会重新解析，避免缓存导致的行号不刷新问题
        super(element, rangeInElement, false);
        this.filePath = filePath;
        this.lineNumber = lineNumber;
    }

    @Override
    public @Nullable PsiElement resolve() {
        ResolveResult[] results = multiResolve(false);
        return results.length > 0 ? results[0].getElement() : null;
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        VirtualFile targetFile = findFile(myElement.getProject(), myElement.getContainingFile());

        if (targetFile != null) {
            PsiManager psiManager = PsiManager.getInstance(myElement.getProject());
            PsiFile psiFile = psiManager.findFile(targetFile);

            if (psiFile != null) {
                // 关键修复2: 创建包装对象，将行号信息嵌入到 PsiElement 中
                // 这样 IDEA 在导航时会使用我们自定义的 navigate() 方法
                return new ResolveResult[]{
                    new PsiElementResolveResult(new NavigatablePsiFileElement(psiFile, targetFile, lineNumber))
                };
            }
        }

        return ResolveResult.EMPTY_ARRAY;
    }

    @Override
    public boolean isSoft() {
        // 关键修复3: 返回 false 使其成为强引用，每次都重新计算
        // 配合构造函数中的设置，确保引用不会被缓存
        return false;
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) {
        // 不支持重命名
        return myElement;
    }

    @Override
    public PsiElement bindToElement(@NotNull PsiElement element) {
        return myElement;
    }

    /**
     * 查找目标文件
     */
    private VirtualFile findFile(com.intellij.openapi.project.Project project, PsiFile currentFile) {
        // 1. 尝试相对于当前文件的路径
        VirtualFile currentDir = currentFile.getVirtualFile().getParent();
        VirtualFile relativeFile = currentDir.findFileByRelativePath(filePath);
        if (relativeFile != null && relativeFile.exists()) {
            return relativeFile;
        }

        // 2. 尝试相对于项目根目录的路径
        VirtualFile projectBase = project.getBaseDir();
        if (projectBase != null) {
            VirtualFile projectFile = projectBase.findFileByRelativePath(filePath);
            if (projectFile != null && projectFile.exists()) {
                return projectFile;
            }
        }

        // 3. 尝试绝对路径
        Path absolutePath = Paths.get(filePath);
        if (absolutePath.isAbsolute()) {
            return LocalFileSystem.getInstance().findFileByPath(filePath);
        }

        return null;
    }

    /**
     * 关键修复4: 创建一个假的 PsiElement，它实现了 Navigatable 接口
     * 当用户 Ctrl+点击时，IDEA 会调用这个类的 navigate() 方法
     * 在 navigate() 方法中，我们创建包含行号的 OpenFileDescriptor 进行精确跳转
     * <p>
     * 使用 FakePsiElement 基类来简化实现
     */
    private static class NavigatablePsiFileElement extends FakePsiElement implements Navigatable {
        private final PsiFile psiFile;
        private final VirtualFile virtualFile;
        private final int lineNumber;

        public NavigatablePsiFileElement(PsiFile psiFile, VirtualFile virtualFile, int lineNumber) {
            this.psiFile = psiFile;
            this.virtualFile = virtualFile;
            this.lineNumber = lineNumber;
        }

        @Override
        public PsiElement getParent() {
            return psiFile;
        }

        @Override
        public void navigate(boolean requestFocus) {
            if (canNavigate()) {
                // 关键：创建包含精确行号和列号的文件描述符
                // lineNumber - 1 是因为 IDEA 的行号从 0 开始，而用户输入的行号从 1 开始
                OpenFileDescriptor descriptor = new OpenFileDescriptor(
                    psiFile.getProject(),
                    virtualFile,
                    Math.max(0, lineNumber - 1),  // 行号
                    0                               // 列号
                );
                descriptor.navigate(requestFocus);
            }
        }

        @Override
        public boolean canNavigate() {
            return virtualFile != null && virtualFile.isValid();
        }

        @Override
        public boolean canNavigateToSource() {
            return canNavigate();
        }
    }
}
