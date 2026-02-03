package com.mybatis.sql.logger.listener;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.mybatis.sql.logger.parser.SqlLogParser;
import com.mybatis.sql.logger.service.SqlConsoleService;
import org.jetbrains.annotations.NotNull;

/**
 * SQL 日志进程监听器
 * 监听控制台输出，解析 MyBatis SQL 日志
 */
public class SqlLogProcessListener implements ProcessListener {

    private static final Logger LOG = Logger.getInstance(SqlLogProcessListener.class);
    
    private final SqlLogParser parser = new SqlLogParser();
    private final Project project;

    public SqlLogProcessListener(Project project) {
        this.project = project;
    }

    private final StringBuilder lineBuffer = new StringBuilder();

    @Override
    public void startNotified(@NotNull ProcessEvent event) {
        // 进程启动时初始化
    }

    @Override
    public void processTerminated(@NotNull ProcessEvent event) {
        // 进程终止时清理
        synchronized (lineBuffer) {
            if (lineBuffer.length() > 0) {
                processLine(lineBuffer.toString());
                lineBuffer.setLength(0);
            }
        }
    }

    @Override
    public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
        String text = event.getText();
        
        if (text == null || text.isEmpty()) {
            return;
        }
        
        synchronized (lineBuffer) {
            lineBuffer.append(text);
            
            // 优化：只有当检测到完整的换行符时才处理
            // 避免因日志被截断导致只处理了一半的行
            int lineEndIndex;
            while ((lineEndIndex = lineBuffer.indexOf("\n")) != -1) {
                String line = lineBuffer.substring(0, lineEndIndex);
                // 处理回车符 \r (Windows: \r\n, Linux: \n)
                if (line.endsWith("\r")) {
                    line = line.substring(0, line.length() - 1);
                }
                
                processLine(line);
                
                // 移除已处理的行
                lineBuffer.delete(0, lineEndIndex + 1);
            }
        }
    }

    private void processLine(String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        // 调试：输出接收到的原始文本
        if (text.contains("Preparing") || text.contains("Parameters")) {
            LOG.debug("[ProcessListener] 处理行: " + text);
        }

        // 解析日志行
        try {
            SqlLogParser.ParsedSql parsedSql = parser.parseLine(text);
            
            if (parsedSql != null) {
                // 将解析后的 SQL 发送到当前项目的 SQL Console
                SqlConsoleService.getInstance(project).addSql(parsedSql);
            }
        } catch (Exception e) {
            // 解析失败，打印错误便于调试
            System.err.println("[MyBatis SQL Logger] Parse error: " + e.getMessage());
        }
    }
}
