package com.mybatis.sql.logger.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MyBatis SQL 日志解析器
 */
public class SqlLogParser {

    // MyBatis Preparing 日志正则
    private static final Pattern PREPARING_PATTERN = Pattern.compile(
            "==>\\s+Preparing:\\s+(.+)",
            Pattern.CASE_INSENSITIVE
    );

    // MyBatis Parameters 日志正则
    private static final Pattern PARAMETERS_PATTERN = Pattern.compile(
            "==>\\s+Parameters:\\s+(.+)",
            Pattern.CASE_INSENSITIVE
    );

    private String currentSql = null;
    private final List<String> sqlBuffer = new ArrayList<>();

    /**
     * 解析单行日志
     */
    public ParsedSql parseLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        // 匹配 Preparing
        Matcher preparingMatcher = PREPARING_PATTERN.matcher(line);
        if (preparingMatcher.find()) {
            currentSql = preparingMatcher.group(1).trim();
            sqlBuffer.clear();
            sqlBuffer.add(currentSql);
            return null;
        }

        // 如果当前有 SQL 缓存，继续收集多行 SQL
        if (currentSql != null && !line.contains("Parameters:")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("==>")) {
                sqlBuffer.add(trimmed);
                currentSql = String.join(" ", sqlBuffer);
            }
        }

        // 匹配 Parameters
        Matcher parametersMatcher = PARAMETERS_PATTERN.matcher(line);
        if (parametersMatcher.find() && currentSql != null) {
            String parametersStr = parametersMatcher.group(1).trim();
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
     */
    private List<Object> parseParameters(String parametersStr) {
        List<Object> parameters = new ArrayList<>();
        
        if (parametersStr == null || parametersStr.trim().isEmpty()) {
            return parameters;
        }

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
