package com.mybatis.sql.logger.parser;

import com.intellij.openapi.diagnostic.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MyBatis SQL 日志解析器
 */
public class SqlLogParser {

    private static final Logger LOG = Logger.getInstance(SqlLogParser.class);

    // MyBatis Preparing 标记正则
    private static final Pattern PREPARING_PATTERN = Pattern.compile(
            "==>\\s*Preparing:\\s*(.+?)(?:\\s+\\b(?:DEBUG|INFO|WARN|ERROR|TRACE)\\b|$)",
            Pattern.CASE_INSENSITIVE
    );

    // MyBatis Parameters 标记正则
    private static final Pattern PARAMETERS_PATTERN = Pattern.compile(
            "==>\\s*Parameters:\\s*(.+?)(?:\\s+\\b(?:DEBUG|INFO|WARN|ERROR|TRACE)\\b|$)",
            Pattern.CASE_INSENSITIVE
    );

    // MyBatis Parameters 宽松匹配正则 (处理前缀被截断的情况)
    private static final Pattern PARAMETERS_PATTERN_LOOSE = Pattern.compile(
            "Parameters:\\s*(.+?)(?:\\s+\\b(?:DEBUG|INFO|WARN|ERROR|TRACE)\\b|$)",
            Pattern.CASE_INSENSITIVE
    );

    private String currentSql = null;
    private final List<String> sqlBuffer = new ArrayList<>();

    /**
     * 判断是否正在处理多行 SQL
     */
    public boolean hasPendingSql() {
        return currentSql != null;
    }

    /**
     * 解析单行日志
     */
    public ParsedSql parseLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        // 匹配 Preparing 标记
        Matcher preparingMatcher = PREPARING_PATTERN.matcher(line);
        if (preparingMatcher.find()) {
            String sqlPart = preparingMatcher.group(1).trim();
            
            // 如果提取的 SQL 结尾包含明显的日志后缀（例如时间戳、线程号等），尝试去除
            // 这种情况通常发生在正则过于贪婪，或者日志格式比较特殊时
            if (containsLogPattern(sqlPart)) {
                // 简单的尝试：如果包含 DEBUG/INFO 等，截断
                String[] levels = {"DEBUG", "INFO", "WARN", "ERROR", "TRACE"};
                for (String level : levels) {
                    int idx = sqlPart.indexOf(level);
                    if (idx > 0) {
                        sqlPart = sqlPart.substring(0, idx).trim();
                        break;
                    }
                }
            }
            
            currentSql = sqlPart;
            sqlBuffer.clear();
            sqlBuffer.add(currentSql);
            
            // 详细调试信息
            LOG.debug("========== Preparing ==========");
            LOG.debug("原始行: " + line);
            LOG.info("提取SQL: " + currentSql);
            
            return null;
        }

        // ========================================
        // 智能多行SQL收集策略
        // ========================================
        if (currentSql != null && !line.contains("Parameters:")) {
            String trimmed = line.trim();
                    
            // 跳过包含"==>"的行（MyBatis的其他输出）
            if (trimmed.startsWith("==>")) {
                return null;
            }
                    
            // 只收集明确以SQL关键字开头的行（真正的SQL延续行）
            // 使用更宽松的 isSqlContent 判断，支持字段换行的情况
            boolean isContent = isSqlContent(trimmed);
            
            if (!trimmed.isEmpty() && isContent) {
                LOG.info("[多行收集] 收集SQL延续行: " + trimmed);
                sqlBuffer.add(trimmed);
                currentSql = String.join(" ", sqlBuffer);
            } else {
                // 记录被拒绝的行（便于调试）
                LOG.debug("[多行收集] 跳过非 SQL 行: " + trimmed);
            }
        }

        // 匹配 Parameters 标记
        Matcher parametersMatcher = PARAMETERS_PATTERN.matcher(line);
        boolean parametersFound = parametersMatcher.find();
        
        // 如果标准正则未匹配到，尝试使用宽松正则
        if (!parametersFound && line.trim().startsWith("Parameters:")) {
            parametersMatcher = PARAMETERS_PATTERN_LOOSE.matcher(line);
            parametersFound = parametersMatcher.find();
        }

        if (parametersFound && currentSql != null) {
            String parametersStr = parametersMatcher.group(1).trim();
            LOG.debug("Parameters - 原始行: " + line);
            LOG.debug("Parameters - 提取参数: " + parametersStr);
            
            List<Object> parameters = parseParameters(parametersStr);
            
            String completeSql = replacePlaceholders(currentSql, parameters);
            String operation = detectOperation(completeSql);
            SqlType sqlType = detectSqlType(completeSql);
            String formattedSql = SqlFormatter.formatSql(completeSql, operation);
            
            ParsedSql result = new ParsedSql(currentSql, formattedSql, operation, sqlType, parameters);
            
            // 重置状态
            currentSql = null;
            sqlBuffer.clear();
            
            return result;
        }

        return null;
    }

    /**
     * 解析参数字符串
     * 兼容处理参数值中包含换行符的情况
     */
    private List<Object> parseParameters(String parametersStr) {
        List<Object> parameters = new ArrayList<>();
        
        if (parametersStr == null || parametersStr.trim().isEmpty()) {
            return parameters;
        }
        
        // 预处理：将所有换行符、回车符、制表符替换为空格，并合并多余空格
        parametersStr = parametersStr.replaceAll("[\\r\\n\\t]+", " ")
                                     .replaceAll("\\s+", " ")
                                     .trim();

        // 分割参数（处理逗号分隔）
        String[] parts = parametersStr.split(",(?![^()]*\\))");
        
        for (String part : parts) {
            part = part.trim();
            
            // 解析参数类型和值
            if (part.contains("(")) {
                int typeStart = part.indexOf('(');
                int typeEnd = part.indexOf(')');
                if (typeStart > 0 && typeEnd > typeStart) {
                    String value = part.substring(0, typeStart).trim();
                    String type = part.substring(typeStart + 1, typeEnd).trim();
                    parameters.add(parseValue(value, type));
                }
            } else {
                parameters.add(part);
            }
        }
        
        return parameters;
    }

    /**
     * 根据类型解析值
     */
    private Object parseValue(String value, String type) {
        if ("null".equalsIgnoreCase(value)) {
            return null;
        }
        
        type = type.toLowerCase();
        
        try {
            if (type.contains("string") || type.contains("varchar")) {
                return value;
            } else if (type.contains("integer") || type.contains("int")) {
                return Integer.parseInt(value);
            } else if (type.contains("long") || type.contains("bigint")) {
                return Long.parseLong(value);
            } else if (type.contains("double") || type.contains("float")) {
                return Double.parseDouble(value);
            } else if (type.contains("boolean") || type.contains("bit")) {
                return Boolean.parseBoolean(value);
            }
        } catch (Exception e) {
            // 解析失败，返回原始值
        }
        
        return value;
    }

    /**
     * 替换 SQL 中的占位符
     */
    private String replacePlaceholders(String sql, List<Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return sql;
        }

        String result = sql;
        for (Object param : parameters) {
            String value = SqlFormatter.getParameterValue(param);
            result = result.replaceFirst("\\?", Matcher.quoteReplacement(value));
        }
        
        return result;
    }

    /**
     * 判断一行文本是否以SQL关键字开头（用于多行收集）
     * 只收集明确以SQL关键字开头的行，拒绝日志碎片
     */
    private boolean startsWithSqlKeyword(String line) {
        if (line == null || line.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = line.trim().toUpperCase();
        
        // SQL关键字列表（只包括可能在多行中出现的）
        String[] sqlKeywords = {
            "SELECT", "FROM", "WHERE", "AND", "OR", "ORDER", "GROUP", 
            "HAVING", "LIMIT", "OFFSET", "JOIN", "LEFT", "RIGHT", "INNER",
            "INSERT", "UPDATE", "DELETE", "SET", "VALUES", "INTO",
            "UNION", "DISTINCT", "AS", "ON", "IN", "NOT", "IS", "LIKE"
        };
        
        for (String keyword : sqlKeywords) {
            if (trimmed.startsWith(keyword + " ") || trimmed.equals(keyword)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 判断一行文本是否是SQL内容（而不是日志行）
     * 采用启发式规则：SQL通常以关键字、标点或标识符开头
     */
    private boolean isSqlContent(String line) {
        if (line == null || line.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = line.trim().toUpperCase();
        
        // 1. 以SQL关键字开头
        String[] sqlKeywords = {
            "SELECT", "FROM", "WHERE", "AND", "OR", "ORDER", "GROUP", 
            "HAVING", "LIMIT", "OFFSET", "JOIN", "LEFT", "RIGHT", "INNER",
            "INSERT", "UPDATE", "DELETE", "SET", "VALUES", "INTO",
            "CREATE", "ALTER", "DROP", "UNION", "DISTINCT", "AS",
            "ON", "IN", "NOT", "IS", "NULL", "LIKE", "BETWEEN"
        };
        for (String keyword : sqlKeywords) {
            if (trimmed.startsWith(keyword + " ") || trimmed.startsWith(keyword + "(")) {
                return true;
            }
        }
        
        // 2. 以标点符号开头（SQL的延续部分）
        if (trimmed.startsWith(",") || trimmed.startsWith("(") || trimmed.startsWith(")")) {
            return true;
        }
        
        // 3. 看起来像字段名或表达式（字母/数字/下划线开头，后面跟逗号或空格）
        if (trimmed.matches("^[a-zA-Z_][a-zA-Z0-9_]*[,\\s].*") || 
            trimmed.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            return true;
        }
        
        // 4. 排除明显的日志行特征
        // 如果包含常见的日志级别关键字且不像SQL，认为是日志行
        if (containsLogPattern(line)) {
            return false;
        }
        
        // 默认认为是SQL内容（宽松策略，避免丢失SQL）
        return true;
    }
    
    /**
     * 检查是否包含日志行的典型特征
     */
    private boolean containsLogPattern(String line) {
        // 常见的日志行模式
        // 1. 包含时间戳
        if (line.matches(".*\\d{4}-\\d{2}-\\d{2}[\\sT]\\d{2}:\\d{2}:\\d{2}.*")) {
            return true;
        }
        
        // 2. 包含日志级别 + 进程ID/线程信息的组合
        if (line.matches(".*(DEBUG|INFO|WARN|ERROR|TRACE)\\s+\\d+\\s+.*")) {
            return true;
        }
        
        // 3. 包含类路径和方法名的模式（如：c.u.t.m.D.selectPage）
        if (line.matches(".*(DEBUG|INFO|WARN|ERROR|TRACE).*[a-z]\\.[a-z]\\.[a-z]\\.[a-zA-Z]+.*:.*")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 检测 SQL 操作类型
     */
    private String detectOperation(String sql) {
        String upperSql = sql.toUpperCase().trim();
        
        if (upperSql.startsWith("SELECT")) {
            return "🔎 QUERY";
        } else if (upperSql.startsWith("INSERT")) {
            return "✨ INSERT";
        } else if (upperSql.startsWith("UPDATE")) {
            return "✏️ UPDATE";
        } else if (upperSql.startsWith("DELETE")) {
            return "🗑️ DELETE";
        }
        
        return "📝 SQL";
    }
    
    /**
     * 检测 SQL 类型（用于颜色区分）
     */
    private SqlType detectSqlType(String sql) {
        String upperSql = sql.toUpperCase().trim();
        
        if (upperSql.startsWith("SELECT")) {
            return SqlType.QUERY;
        } else if (upperSql.startsWith("INSERT")) {
            return SqlType.INSERT;
        } else if (upperSql.startsWith("UPDATE")) {
            return SqlType.UPDATE;
        } else if (upperSql.startsWith("DELETE")) {
            return SqlType.DELETE;
        }
        
        return SqlType.OTHER;
    }

    /**
     * SQL 类型枚举
     */
    public enum SqlType {
        QUERY,      // 查询 - 绿色
        INSERT,     // 插入 - 黄色
        UPDATE,     // 更新 - 蓝色
        DELETE,     // 删除 - 红色
        OTHER       // 其他 - 灰色
    }

    /**
     * 解析结果
     */
    public static class ParsedSql {
        private final String originalSql;
        private final String formattedSql;
        private final String operation;
        private final SqlType sqlType;
        private final List<Object> parameters;

        public ParsedSql(String originalSql, String formattedSql, String operation, SqlType sqlType, List<Object> parameters) {
            this.originalSql = originalSql;
            this.formattedSql = formattedSql;
            this.operation = operation;
            this.sqlType = sqlType;
            this.parameters = parameters;
        }

        public String getOriginalSql() {
            return originalSql;
        }

        public String getFormattedSql() {
            return formattedSql;
        }

        public String getOperation() {
            return operation;
        }
        
        public SqlType getSqlType() {
            return sqlType;
        }

        public List<Object> getParameters() {
            return parameters;
        }
    }
}
