package com.mybatis.sql.logger.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.mybatis.sql.logger.ui.SqlConsolePanel;
import org.jetbrains.annotations.NotNull;

/**
 * 滚动到底部 Action
 */
public class ScrollToBottomAction extends AnAction {

    private final SqlConsolePanel consolePanel;

    public ScrollToBottomAction(SqlConsolePanel consolePanel) {
        super("跳转到最下面", "滚动到 SQL 日志的底部", AllIcons.General.ArrowDown);
        this.consolePanel = consolePanel;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        consolePanel.scrollToBottom();
    }
}
