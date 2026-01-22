package com.mybatis.sql.logger.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;

/**
 * 复制 SQL Action
 */
public class CopySqlAction extends AnAction {

    private final Editor editor;

    public CopySqlAction(Editor editor) {
        super("复制 SQL", "复制选中的 SQL 语句到剪贴板", AllIcons.Actions.Copy);
        this.editor = editor;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String selectedText = editor.getSelectionModel().getSelectedText();
        
        if (selectedText == null || selectedText.trim().isEmpty()) {
            // 如果没有选中文本，复制全部
            selectedText = editor.getDocument().getText();
        }

        if (selectedText != null && !selectedText.trim().isEmpty()) {
            CopyPasteManager.getInstance().setContents(new StringSelection(selectedText));
            Messages.showInfoMessage("SQL 已复制到剪贴板", "复制成功");
        } else {
            Messages.showWarningDialog("没有可复制的内容", "复制失败");
        }
    }
}
