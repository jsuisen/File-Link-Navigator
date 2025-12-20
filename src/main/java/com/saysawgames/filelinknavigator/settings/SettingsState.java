package com.saysawgames.filelinknavigator.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.saysawgames.filelinknavigator.Constant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 设置状态类，用于保存用户配置的标签名、分隔符和行号前缀
 */
@State(
    name = "com.saysawgames.filelinknavigator.settings.SettingsState",
    storages = @Storage("DocLinkNavigatorSettings.xml")
)
public class SettingsState implements PersistentStateComponent<SettingsState> {
    // 默认的标签名列表，用|分隔
    public String tagNames = Constant.DEFAULT_TAGNAMES;
    // 默认的文件名与行号之间的分隔符列表，用|分隔
    public String separators = Constant.DEFAULT_SEPARATORS;
    // 默认的行号前缀，用|分隔
    public String linePrefixes = Constant.DEFAULT_LINEPREFIXES;

    // 获取单例实例
    public static SettingsState getInstance() {
        return ApplicationManager.getApplication().getService(SettingsState.class);
    }

    @Nullable
    @Override
    public SettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull SettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public String getTagNamesRegex() {
        return tagNames;
    }

    public String getSeparatorsRegex() {
        return separators;
    }

    public String getLinePrefixesRegex() {
        return linePrefixes;
    }
}
