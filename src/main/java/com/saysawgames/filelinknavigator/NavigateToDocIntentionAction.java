
package com.saysawgames.filelinknavigator;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PriorityAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 点击链接时执行的导航动作
 */
public class NavigateToDocIntentionAction implements IntentionAction, PriorityAction {

    private static final String[] SELECT_NAME = new String[]{
        "Navigate to",
        "File not found",
        "Unable to open file:",
    };
    /*private static final String[] SELECT_NAME = new String[]{
        "导航到",
        "找不到文件",
        "无法打开文件:",
    };*/

    private final String filePath;
    private final int lineNumber;

    public NavigateToDocIntentionAction(String filePath, int lineNumber) {
        // 移除路径中的双引号（处理Javadoc注释中的引号）
        this.filePath = Utils.formatPath(filePath);
        this.lineNumber = lineNumber;
    }

    @NotNull
    @Override
    public String getText() {
        return SELECT_NAME[0] + filePath + ":" + lineNumber;
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "File Link Navigator";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file)
        throws IncorrectOperationException {
        VirtualFile targetFile = findFile(project, file, filePath);

        if (targetFile == null) {
            // 文件未找到，显示通知
            com.intellij.notification.Notifications.Bus.notify(
                new com.intellij.notification.Notification(
                    "DocLink",
                    SELECT_NAME[1],
                    SELECT_NAME[2] + filePath,
                    com.intellij.notification.NotificationType.WARNING
                ),
                project
            );
            return;
        }

        // 打开文件并跳转到指定行（行号从0开始，所以要减1）
        OpenFileDescriptor descriptor = new OpenFileDescriptor(
            project,
            targetFile,
            Math.max(0, lineNumber - 1),
            0
        );

        FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
    }

    /**
     * 查找目标文件
     * 支持：相对于当前文件的路径、相对于项目根目录的路径
     */
    private VirtualFile findFile(Project project, PsiFile currentFile, String path) {
        // 1. 尝试相对于当前文件的路径
        VirtualFile currentDir = currentFile.getVirtualFile().getParent();
        VirtualFile relativeFile = currentDir.findFileByRelativePath(path);
        if (relativeFile != null && relativeFile.exists()) {
            return relativeFile;
        }

        // 2. 尝试相对于项目根目录的路径
        VirtualFile projectBase = project.getBaseDir();
        if (projectBase != null) {
            VirtualFile projectFile = projectBase.findFileByRelativePath(path);
            if (projectFile != null && projectFile.exists()) {
                return projectFile;
            }
        }

        // 3. 尝试绝对路径
        Path absolutePath = Paths.get(path);
        if (absolutePath.isAbsolute()) {
            return LocalFileSystem.getInstance().findFileByPath(path);
        }

        return null;
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public @NotNull Priority getPriority() {
        return Priority.HIGH;
    }
}
