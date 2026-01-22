package com.mybatis.sql.logger.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.mybatis.sql.logger.action.ClearConsoleAction;
import com.mybatis.sql.logger.action.CopySqlAction;
import com.mybatis.sql.logger.action.ScrollToBottomAction;
import com.mybatis.sql.logger.action.ToggleListeningAction;
import com.mybatis.sql.logger.parser.SqlLogParser;
import com.mybatis.sql.logger.service.SqlConsoleService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * SQL Console 面板
 */
public class SqlConsolePanel extends JPanel implements Disposable, SqlConsoleService.SqlConsoleListener {

    private final Project project;
    private final Editor editor;
    private final JBScrollPane scrollPane;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final List<RangeHighlighter> highlighters = new ArrayList<>();

    public SqlConsolePanel(Project project) {
        super(new BorderLayout());
        this.project = project;

        // 创建编辑器
        EditorFactory editorFactory = EditorFactory.getInstance();
        FileType sqlFileType = FileTypeManager.getInstance().getFileTypeByExtension("sql");

        editor = editorFactory.createEditor(
                editorFactory.createDocument(""),
                project,
                sqlFileType,
                // 设置为可编辑模式
                false
        );

        // 配置编辑器
        if (editor instanceof EditorEx) {
            EditorEx editorEx = (EditorEx) editor;
            EditorSettings settings = editorEx.getSettings();
            settings.setLineNumbersShown(true);
            settings.setFoldingOutlineShown(true);
            settings.setLineMarkerAreaShown(true);
            settings.setIndentGuidesShown(true);
            settings.setUseSoftWraps(false);
        }

        // 创建工具栏
        JPanel toolbarPanel = createToolbar();

        // 创建滚动面板
        scrollPane = new JBScrollPane(editor.getComponent());
        
        // 布局
        add(toolbarPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        // 添加右键菜单
        addContextMenu();

        // 注册监听器
        SqlConsoleService.getInstance(project).addListener(this);
        
        // 显示欢迎信息
        showWelcomeMessage();
    }
    
    /**
     * 显示欢迎信息
     */
    private void showWelcomeMessage() {
        // 构建欢迎信息文本
        StringBuilder welcome = new StringBuilder();
        welcome.append("═".repeat(80)).append("\n");
        welcome.append("🎉 欢迎使用 MyBatis SQL Beautifier 插件 🎉\n");
        welcome.append("─".repeat(80)).append("\n");
        welcome.append("💡 功能特性:\n");
        welcome.append("   • 自动捕获并格式化 MyBatis/MyBatis-Plus SQL 日志\n");
        welcome.append("   • 实时替换 SQL 参数，展示完整的可执行 SQL\n");
        welcome.append("   • 支持 SQL 语法高亮和颜色区分（查询/插入/更新/删除）\n");
        welcome.append("   • 可编辑模式，支持手动修改和复制 SQL\n");
        welcome.append("\n");
        welcome.append("👨\u200d💻 作者：程序员 curen\n");
        welcome.append("📧 反馈邮箱：1139632166@qq.com\n");
        welcome.append("\n");
        welcome.append("🔔 提示：请确保项目开启了 MyBatis 日志输出（DEBUG 级别）\n");
        welcome.append("═".repeat(80)).append("\n\n");
        
        String welcomeText = welcome.toString();
        
        // 在 SQL Console 中显示
        ApplicationManager.getApplication().invokeLater(() -> {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                editor.getDocument().setText(welcomeText);
                
                // 为欢迎信息添加颜色（使用青色）
                Color welcomeColor = new Color(0, 150, 136); // 青绿色
                TextAttributes attributes = new TextAttributes();
                attributes.setForegroundColor(welcomeColor);
                
                RangeHighlighter highlighter = editor.getMarkupModel().addRangeHighlighter(
                        0, editor.getDocument().getTextLength(),
                        HighlighterLayer.SYNTAX,
                        attributes,
                        HighlighterTargetArea.EXACT_RANGE
                );
                highlighters.add(highlighter);
            });
        });
    }

    /**
     * 添加右键菜单
     */
    private void addContextMenu() {
        editor.getContentComponent().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }
        });
    }
    
    /**
     * 显示右键菜单
     */
    private void showContextMenu(MouseEvent e) {
        JPopupMenu popupMenu = new JPopupMenu();
        
        // 获取选中的文本
        String selectedText = editor.getSelectionModel().getSelectedText();
        boolean hasSelection = selectedText != null && !selectedText.trim().isEmpty();
        
        // 复制相关菜单
        if (hasSelection) {
            // 有选中内容 - 显示"复制选中内容"
            JMenuItem copySelectedItem = new JMenuItem("复制选中内容");
            copySelectedItem.setIcon(com.intellij.icons.AllIcons.Actions.Copy);
            copySelectedItem.addActionListener(event -> {
                java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(selectedText);
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            });
            popupMenu.add(copySelectedItem);
            
            // 添加删除选中内容菜单项
            JMenuItem deleteSelectedItem = new JMenuItem("删除选中内容");
            deleteSelectedItem.setIcon(com.intellij.icons.AllIcons.Actions.Cancel);
            deleteSelectedItem.addActionListener(event -> deleteSelectedText());
            popupMenu.add(deleteSelectedItem);
        } else {
            // 没有选中内容 - 显示"复制所有 SQL"
            JMenuItem copyAllItem = new JMenuItem("复制所有 SQL");
            copyAllItem.setIcon(com.intellij.icons.AllIcons.Actions.Copy);
            copyAllItem.addActionListener(event -> {
                String text = editor.getDocument().getText();
                if (!text.isEmpty()) {
                    java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(text);
                    java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                }
            });
            popupMenu.add(copyAllItem);
        }
        
        // 添加分隔线
        popupMenu.addSeparator();
        
        // 清除所有 SQL 菜单项
        JMenuItem clearItem = new JMenuItem("清除所有 SQL");
        clearItem.setIcon(com.intellij.icons.AllIcons.Actions.GC);
        clearItem.addActionListener(event -> clear());
        popupMenu.add(clearItem);
        
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }
    
    /**
     * 删除选中的文本
     */
    private void deleteSelectedText() {
        ApplicationManager.getApplication().invokeLater(() -> {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                int selectionStart = editor.getSelectionModel().getSelectionStart();
                int selectionEnd = editor.getSelectionModel().getSelectionEnd();
                if (selectionStart < selectionEnd) {
                    editor.getDocument().deleteString(selectionStart, selectionEnd);
                    // 移除选中状态
                    editor.getSelectionModel().removeSelection();
                }
            });
        });
    }

    /**
     * 创建工具栏
     */
    private JPanel createToolbar() {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        
        // 添加开启/停止监听 Action
        ToggleListeningAction toggleAction = new ToggleListeningAction(SqlConsoleService.getInstance(project));
        actionGroup.add(toggleAction);
        
        actionGroup.addSeparator();
        
        // 添加复制 SQL Action
        CopySqlAction copyAction = new CopySqlAction(editor);
        actionGroup.add(copyAction);
        
        // 添加清空控制台 Action
        ClearConsoleAction clearAction = new ClearConsoleAction(this);
        actionGroup.add(clearAction);
        
        actionGroup.addSeparator();
        
        // 添加滚动到底部 Action
        ScrollToBottomAction scrollAction = new ScrollToBottomAction(this);
        actionGroup.add(scrollAction);

        // 创建工具栏（水平布局）
        ActionToolbar toolbar = ActionManager.getInstance()
                .createActionToolbar("SqlConsoleToolbar", actionGroup, true);
        toolbar.setTargetComponent(this);

        JPanel toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.add(toolbar.getComponent(), BorderLayout.WEST);
        
        // 添加标题标签
        JLabel titleLabel = new JLabel(" MyBatis SQL 日志");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        toolbarPanel.add(titleLabel, BorderLayout.CENTER);

        return toolbarPanel;
    }

    /**
     * 追加 SQL 到编辑器末尾
     */
    private void appendSqlToEditor(SqlLogParser.ParsedSql parsedSql) {
        ApplicationManager.getApplication().invokeLater(() -> {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                String timestamp = timeFormat.format(new Date());
                String operation = parsedSql.getOperation();
                String border = "═".repeat(59);  // 统一使用双线框
                
                int startOffset = editor.getDocument().getTextLength();
                
                // 构建 SQL 块内容
                StringBuilder sqlBlock = new StringBuilder();
                // 顶部边框
                sqlBlock.append(border).append("\n");
                // 标题行
                sqlBlock.append(operation).append(" [").append(timestamp).append("]\n");
                // 分隔线
                sqlBlock.append("─".repeat(59)).append("\n");
                // SQL 内容
                sqlBlock.append(parsedSql.getFormattedSql()).append("\n");
                // 底部边框
                sqlBlock.append(border).append("\n\n");
                
                // 追加到文档末尾
                editor.getDocument().insertString(startOffset, sqlBlock.toString());
                
                int endOffset = editor.getDocument().getTextLength();
                
                // 添加颜色高亮
                addColorHighlightForRange(startOffset, endOffset, parsedSql.getSqlType());
                
                // 滚动到底部
                scrollToBottom();
            });
        });
    }

    /**
     * 根据 SQL 类型获取颜色
     */
    private Color getColorByType(SqlLogParser.SqlType sqlType) {
        switch (sqlType) {
            case QUERY:
                return new Color(34, 139, 34);   // 查询 - 森林绿
            case INSERT:
                return new Color(218, 165, 32);  // 插入 - 金黄色
            case UPDATE:
                return new Color(30, 144, 255);  // 更新 - 道奇蓝
            case DELETE:
                return new Color(220, 20, 60);   // 删除 - 深红色
            default:
                return new Color(0, 206, 209);   // 其他 - 青色
        }
    }

    /**
     * 为指定范围添加颜色高亮
     */
    private void addColorHighlightForRange(int startOffset, int endOffset, SqlLogParser.SqlType sqlType) {
        if (startOffset >= editor.getDocument().getTextLength()) {
            return;
        }
        
        Color color = getColorByType(sqlType);
        TextAttributes attributes = new TextAttributes();
        attributes.setForegroundColor(color);
        
        // 只给边框和标题添加颜色
        String text = editor.getDocument().getText();
        int currentPos = startOffset;
        
        // 找到每一行的边框和标题
        String[] lines = text.substring(startOffset, 
                Math.min(endOffset, editor.getDocument().getTextLength())).split("\n");
        
        for (int i = 0; i < lines.length && currentPos < editor.getDocument().getTextLength(); i++) {
            String line = lines[i];
            int lineStart = currentPos;
            int lineEnd = Math.min(currentPos + line.length(), editor.getDocument().getTextLength());
            
            // 第一行（顶部边框）、第二行（标题）、第三行（分隔线）和最后一行（底部边框）需要颜色
            if (i == 0 || i == 1 || i == 2 || line.startsWith("═")) {
                if (lineStart < lineEnd) {
                    RangeHighlighter highlighter = editor.getMarkupModel().addRangeHighlighter(
                            lineStart, lineEnd,
                            HighlighterLayer.SYNTAX,
                            attributes,
                            HighlighterTargetArea.EXACT_RANGE
                    );
                    highlighters.add(highlighter);
                }
            }
            
            currentPos = lineEnd + 1; // +1 为换行符
        }
    }

    /**
     * 清空控制台
     */
    public void clear() {
        ApplicationManager.getApplication().invokeLater(() -> {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                // 清空编辑器内容
                editor.getDocument().setText("");
                
                // 清除所有高亮
                for (RangeHighlighter highlighter : highlighters) {
                    editor.getMarkupModel().removeHighlighter(highlighter);
                }
                highlighters.clear();
            });
        });
        
        // 清空 Service 中的记录
        SqlConsoleService.getInstance(project).clearSql();
    }
    
    /**
     * 滚动到底部
     */
    public void scrollToBottom() {
        ApplicationManager.getApplication().invokeLater(() -> {
            // 获取垂直滚动条
            javax.swing.JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
            // 滚动到最大值
            verticalScrollBar.setValue(verticalScrollBar.getMaximum());
            
            // 同时移动光标到末尾
            WriteCommandAction.runWriteCommandAction(project, () -> {
                int textLength = editor.getDocument().getTextLength();
                if (textLength > 0) {
                    editor.getCaretModel().moveToOffset(textLength);
                }
            });
        });
    }

    @Override
    public void onSqlAdded(SqlLogParser.ParsedSql parsedSql) {
        // 直接追加到编辑器，不覆盖用户的编辑
        appendSqlToEditor(parsedSql);
    }

    @Override
    public void onSqlCleared() {
        // Service 被清空时，不做任何操作（保留用户编辑的内容）
    }

    @Override
    public void dispose() {
        SqlConsoleService.getInstance(project).removeListener(this);
        EditorFactory.getInstance().releaseEditor(editor);
    }
}
