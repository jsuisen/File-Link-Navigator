
package com.saysawgames.filelinknavigator;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * 右键菜单动作：插入文档链接
 * 允许用户选择文件和行号，自动插入格式化的链接
 */
public class InsertDocLinkAction extends AnAction {
    private static final String[] SELECT_NAME = new String[]{
        "Select documentation file",
        "Choose the documentation file to link to",
        "Enter line number",
        "Line number",
        "Line number must be ≥ 0",
        "Invalid input",
        "Invalid line number",
        "Invalid input",
    };
    /*private static final String[] SELECT_NAME = new String[]{
        "选择文档文件",
        "选择待链接的文档文件",
        "输入行号",
        "行号",
        "行号必须大于等于0",
        "非法输入",
        "非法行号",
        "非法输入",
    };*/

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);

        if (project == null || editor == null) {
            return;
        }

        // 创建文件选择器，只显示 Markdown 文件
        FileChooserDescriptor descriptor = new FileChooserDescriptor(
            true,
            false,
            false,
            false,
            false,
            false
        ).withFileFilter(file -> {
            file.getName();
            return true;
        }/*file.getName().endsWith(".md")*/);

        descriptor.setTitle(SELECT_NAME[0]);
        descriptor.setDescription(SELECT_NAME[1]);

        VirtualFile selectedFile = FileChooser.chooseFile(descriptor, project, null);
        if (selectedFile == null) {
            return;
        }

        // 询问行号
        String lineNumberStr = Messages.showInputDialog(
            project,
            SELECT_NAME[2],
            SELECT_NAME[3],
            Messages.getQuestionIcon(),
            "1",
            null
        );
        if (lineNumberStr == null) {
            return;
        }

        int lineNumber;
        try {
            lineNumber = Integer.parseInt(lineNumberStr.trim());
            if (lineNumber < 1) {
                Messages.showErrorDialog(project, SELECT_NAME[4], SELECT_NAME[5]);
                return;
            }
        } catch (NumberFormatException ex) {
            Messages.showErrorDialog(project, SELECT_NAME[6], SELECT_NAME[7]);
            return;
        }

        // 获取相对路径
        VirtualFile projectBase = project.getBaseDir();
        String relativePath = com.intellij.openapi.vfs.VfsUtilCore.getRelativePath(
            selectedFile, projectBase, '/'
        );

        if (relativePath == null) {
            relativePath = selectedFile.getPath();
        }

        String prefix = "@doc";
        if (Constant.getValidTags() != null && Constant.getValidTags().length > 0) {
            prefix = Constant.getValidTags()[0];
        }

        // 构建链接文本
        String linkText = String.format("//" + prefix + " %s:%d", relativePath, lineNumber);

        // 插入到编辑器
        Document document = editor.getDocument();
        int offset = editor.getCaretModel().getOffset();

        WriteCommandAction.runWriteCommandAction(project, () -> {
            // 如果当前行不为空，先插入换行
            int lineStartOffset = document.getLineStartOffset(
                document.getLineNumber(offset)
            );
            String lineText = document.getText().substring(
                lineStartOffset,
                Math.min(lineStartOffset + 100, document.getTextLength())
            ).trim();

            if (!lineText.isEmpty()) {
                document.insertString(offset, "\n" + linkText + "\n");
            } else {
                document.insertString(offset, linkText + "\n");
            }
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // 只在编辑器中启用此动作
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setEnabledAndVisible(project != null && editor != null);
    }
}
