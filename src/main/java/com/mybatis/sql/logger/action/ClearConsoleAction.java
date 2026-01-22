package com.mybatis.sql.logger.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.mybatis.sql.logger.ui.SqlConsolePanel;
import org.jetbrains.annotations.NotNull;

/**
 * 清空控制台 Action
 */
public class ClearConsoleAction extends AnAction {

    private final SqlConsolePanel consolePanel;

    public ClearConsoleAction(SqlConsolePanel consolePanel) {
        super("清空控制台", "清空 SQL Console 中的所有内容", AllIcons.Actions.GC);
        this.consolePanel = consolePanel;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        consolePanel.clear();
    }
}
