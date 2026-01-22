package com.mybatis.sql.logger.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.mybatis.sql.logger.service.SqlConsoleService;
import org.jetbrains.annotations.NotNull;

/**
 * 开始/停止监听 SQL 日志 Action
 */
public class ToggleListeningAction extends ToggleAction {

    private final SqlConsoleService sqlConsoleService;

    public ToggleListeningAction(SqlConsoleService sqlConsoleService) {
        super("开始/停止监听日志为SQL", "开启或停止监听 MyBatis SQL 日志", AllIcons.Actions.Execute);
        this.sqlConsoleService = sqlConsoleService;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return sqlConsoleService.isListening();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        sqlConsoleService.setListening(state);
        
        // 更新图标和提示
        if (state) {
            getTemplatePresentation().setIcon(AllIcons.Actions.Suspend);
            getTemplatePresentation().setText("停止监听日志为SQL");
        } else {
            getTemplatePresentation().setIcon(AllIcons.Actions.Execute);
            getTemplatePresentation().setText("开始监听日志为SQL");
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        // 根据当前状态更新图标
        boolean isListening = sqlConsoleService.isListening();
        if (isListening) {
            e.getPresentation().setIcon(AllIcons.Actions.Suspend);
            e.getPresentation().setText("停止监听日志为SQL");
        } else {
            e.getPresentation().setIcon(AllIcons.Actions.Execute);
            e.getPresentation().setText("开始监听日志为SQL");
        }
    }
}
