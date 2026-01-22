package com.mybatis.sql.logger.listener;

import com.intellij.execution.ExecutionListener;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunContentDescriptor;
import org.jetbrains.annotations.NotNull;

/**
 * SQL 日志执行监听器
 * 监听进程启动事件，自动附加 ProcessListener
 */
public class SqlLogExecutionListener implements ExecutionListener {

    @Override
    public void processStarted(@NotNull String executorId,
                                @NotNull ExecutionEnvironment env,
                                @NotNull ProcessHandler handler) {
        // 当进程启动时，附加 SQL 日志监听器（传入项目引用）
        handler.addProcessListener(new SqlLogProcessListener(env.getProject()));
        
        // 在用户控制台输出欢迎信息
        printWelcomeMessage(env);
    }
    
    /**
     * 在控制台输出欢迎信息
     */
    private void printWelcomeMessage(@NotNull ExecutionEnvironment env) {
        try {
            // 获取 RunContentDescriptor
            RunContentDescriptor descriptor = env.getContentToReuse();
            if (descriptor == null) {
                // 延迟一下等待 descriptor 创建
                com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
                    RunContentDescriptor desc = env.getContentToReuse();
                    if (desc != null) {
                        printToConsole(desc);
                    }
                });
                return;
            }
            
            printToConsole(descriptor);
        } catch (Exception e) {
            // 忽略错误
        }
    }
    
    /**
     * 输出到控制台
     */
    private void printToConsole(RunContentDescriptor descriptor) {
        try {
            // 获取 ConsoleView
            ConsoleView console = (ConsoleView) descriptor.getExecutionConsole();
            if (console != null) {
                // 构建欢迎信息
                StringBuilder welcome = new StringBuilder();
                welcome.append("\n");
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
                welcome.append("🔔 提示：请打开 View → Tool Windows → SQL Console 查看格式化后的 SQL\n");
                welcome.append("═".repeat(80)).append("\n\n");
                
                // 输出到控制台
                console.print(welcome.toString(), ConsoleViewContentType.SYSTEM_OUTPUT);
            }
        } catch (Exception e) {
            // 忽略错误
        }
    }
}
