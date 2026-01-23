package com.mybatis.sql.logger.service;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.mybatis.sql.logger.parser.SqlLogParser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SQL Console 服务
 * 管理解析后的 SQL 数据（项目级别）
 */
@Service(Service.Level.PROJECT)
public final class SqlConsoleService {

    private final List<SqlConsoleListener> listeners = new CopyOnWriteArrayList<>();
    // 默认开启监听
    private volatile boolean isListening = true;

    public static SqlConsoleService getInstance(Project project) {
        return project.getService(SqlConsoleService.class);
    }

    /**
     * 添加 SQL
     */
    public void addSql(SqlLogParser.ParsedSql parsedSql) {
        // 只有在监听状态下才添加 SQL
        if (!isListening) {
            return;
        }
        // 直接通知监听器，不再缓存
        notifyListeners(parsedSql);
    }

    /**
     * 获取监听状态
     */
    public boolean isListening() {
        return isListening;
    }

    /**
     * 设置监听状态
     */
    public void setListening(boolean listening) {
        this.isListening = listening;
    }

    /**
     * 清空 SQL（仅通知监听器）
     */
    public void clearSql() {
        notifyListenersClear();
    }

    /**
     * 添加监听器
     */
    public void addListener(SqlConsoleListener listener) {
        listeners.add(listener);
    }

    /**
     * 移除监听器
     */
    public void removeListener(SqlConsoleListener listener) {
        listeners.remove(listener);
    }

    /**
     * 通知监听器新增 SQL
     */
    private void notifyListeners(SqlLogParser.ParsedSql parsedSql) {
        for (SqlConsoleListener listener : listeners) {
            listener.onSqlAdded(parsedSql);
        }
    }

    /**
     * 通知监听器清空
     */
    private void notifyListenersClear() {
        for (SqlConsoleListener listener : listeners) {
            listener.onSqlCleared();
        }
    }

    /**
     * SQL Console 监听器接口
     */
    public interface SqlConsoleListener {
        void onSqlAdded(SqlLogParser.ParsedSql parsedSql);
        void onSqlCleared();
    }
}
