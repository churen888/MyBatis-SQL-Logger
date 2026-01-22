package com.mybatis.sql.logger.listener;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
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

    private final SqlLogParser parser = new SqlLogParser();
    private final Project project;

    public SqlLogProcessListener(Project project) {
        this.project = project;
    }

    @Override
    public void startNotified(@NotNull ProcessEvent event) {
        // 进程启动时初始化
    }

    @Override
    public void processTerminated(@NotNull ProcessEvent event) {
        // 进程终止时清理
    }

    @Override
    public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
        String text = event.getText();
        
        if (text == null || text.trim().isEmpty()) {
            return;
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
