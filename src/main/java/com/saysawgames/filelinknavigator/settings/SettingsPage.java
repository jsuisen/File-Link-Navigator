
package com.saysawgames.filelinknavigator.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.saysawgames.filelinknavigator.Constant;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * 设置页面类，用于提供用户配置界面
 */
public class SettingsPage implements Configurable {

    private static final String[] LABEL_NAMES = new String[]{
        "Tag names, separated by commas (e.g., @doc,@markdown,@see)",
        "Delimiter between filename and line number (separated by commas, e.g., :,#):",
        "Line-number prefix (separated by commas, e.g., L):",
        "Example: @doc README.md:10 or @markdown docs/guide.txt#5L",
    };
    /*private static final String[] LABEL_NAMES = new String[]{
        "标签名,用[,]分隔，如：@doc,@markdown,@see",
        "文件名与行号之间的分隔符（用[,]分隔，如：:,#）：",
        "文件行号前缀（用[,]分隔，如：L）：",
        "示例：@doc README.md:10 或 @markdown docs/guide.txt#5L",
    };*/

    private JBTextField tagNamesField;
    private JBTextField separatorsField;
    private JBTextField linePrefixesField;

    private SettingsState settingsState;

    public SettingsPage() {
        // 获取SettingsState实例，IntelliJ服务管理器会确保实例正确创建
        settingsState = SettingsState.getInstance();
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "File Link Navigator";
    }


    @Nullable
    @Override
    public JComponent createComponent() {
        // 使用默认值或当前设置值初始化输入字段
        String defaultTagNames = Constant.DEFAULT_TAGNAMES;
        String defaultSeparators = Constant.DEFAULT_SEPARATORS;
        String defaultLinePrefixes = Constant.DEFAULT_LINEPREFIXES;

        if (settingsState != null) {
            defaultTagNames = settingsState.tagNames;
            defaultSeparators = settingsState.separators;
            defaultLinePrefixes = settingsState.linePrefixes;
        }

        tagNamesField = new JBTextField(defaultTagNames);
        separatorsField = new JBTextField(defaultSeparators);
        linePrefixesField = new JBTextField(defaultLinePrefixes);

        return FormBuilder.createFormBuilder()
            .addLabeledComponent(new JBLabel(LABEL_NAMES[0]), tagNamesField, 1, false)
            .addLabeledComponent(new JBLabel(LABEL_NAMES[1]), separatorsField, 1, false)
            .addLabeledComponent(new JBLabel(LABEL_NAMES[2]), linePrefixesField, 1, false)
            .addSeparator()
            .addLabeledComponent(new JBLabel(LABEL_NAMES[3]), new JLabel(""), 1, false)
            .getPanel();
    }

    @Override
    public boolean isModified() {
        if (tagNamesField == null || separatorsField == null || linePrefixesField == null || settingsState == null) {
            return false;
        }
        return !tagNamesField.getText().equals(settingsState.tagNames) ||
            !separatorsField.getText().equals(settingsState.separators) ||
            !linePrefixesField.getText().equals(settingsState.linePrefixes);
    }

    @Override
    public void apply() throws ConfigurationException {
        if (settingsState != null) {
            settingsState.tagNames = tagNamesField.getText();
            settingsState.separators = separatorsField.getText();
            settingsState.linePrefixes = linePrefixesField.getText();

            // 配置变更后，需要重新编译正则表达式
            Constant.clearCache();  // 清除旧缓存
            Constant.updateDocLinkPattern();  // 重新加载设置
        }
    }

    @Override
    public void reset() {
        if (tagNamesField != null && separatorsField != null && linePrefixesField != null && settingsState != null) {
            tagNamesField.setText(settingsState.tagNames);
            separatorsField.setText(settingsState.separators);
            linePrefixesField.setText(settingsState.linePrefixes);
        }
    }

    @Override
    public void disposeUIResources() {
        tagNamesField = null;
        separatorsField = null;
        linePrefixesField = null;
    }
}


