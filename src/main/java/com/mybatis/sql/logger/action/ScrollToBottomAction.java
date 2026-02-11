package com.mybatis.sql.logger.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.mybatis.sql.logger.ui.SqlConsolePanel;
import org.jetbrains.annotations.NotNull;

/**
 * 滚动到底部 Action (Toggle 类型)
 * 状态与 SqlConsolePanel 的 isAutoScroll 绑定
 */
public class ScrollToBottomAction extends ToggleAction {

    private final SqlConsolePanel consolePanel;

    public ScrollToBottomAction(SqlConsolePanel consolePanel) {
        super("自动滚动到底部", "开启/关闭自动滚动到底部", AllIcons.RunConfigurations.Scroll_down);
        this.consolePanel = consolePanel;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return consolePanel.isAutoScroll();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        consolePanel.setAutoScroll(state);
    }
}
