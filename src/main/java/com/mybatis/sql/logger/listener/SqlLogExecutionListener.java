package com.mybatis.sql.logger.listener;

import com.intellij.execution.ExecutionListener;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
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
    }
}
