package com.mybatis.sql.logger.parser;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;

/**
 * SQL 格式化工具类
 * 从 MybatisPlusLogRewrite.java 移植并优化
 */
public class SqlFormatter {

    /**
     * 获取参数值的字符串表示形式
     */
    public static String getParameterValue(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            return "'" + obj + "'";
        }
        if (obj instanceof java.util.Date) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
            return "'" + sdf.format((java.util.Date) obj) + "'";
        }
        if (obj instanceof Boolean) {
            return Boolean.TRUE.equals(obj) ? "1" : "0";
        }
        if (obj instanceof Number) {
            return obj.toString();
        }
        if (obj instanceof Collection) {
            return obj.toString();
        }
        if (obj.getClass().isArray()) {
            return Arrays.toString((Object[]) obj);
        }
        return "'" + obj.toString() + "'";
    }

    /**
     * 格式化 SQL 语句
     */
    public static String formatSql(String sql, String operation) {
        if (sql == null || sql.trim().isEmpty()) {
            return "";
        }

        // 1. 压缩多余空格
        sql = sql.replaceAll("\\s+", " ").trim();

        // 2. 处理SQL关键字换行
        String[] keywords = {
                "SELECT", "FROM", "WHERE", "AND", "OR",
                "JOIN", "LEFT JOIN", "RIGHT JOIN", "INNER JOIN", "FULL JOIN",
                "ON", "ORDER BY", "GROUP BY", "HAVING", "LIMIT",
                "INSERT INTO", "VALUES", "UPDATE", "SET", "DELETE FROM"
        };

        for (String keyword : keywords) {
            String regex = "(?i)\\b" + keyword + "\\b";
            String replacement = "\n" + getIndentForKeyword(keyword) + keyword;
            sql = sql.replaceAll(regex, replacement);
        }

        // 3. 根据操作类型进行特殊格式化
        if (operation != null) {
            if (operation.contains("INSERT")) {
                sql = formatInsertStatement(sql);
            } else if (operation.contains("UPDATE")) {
                sql = formatUpdateStatement(sql);
            } else if (operation.contains("DELETE")) {
                sql = formatDeleteStatement(sql);
            } else {
                sql = formatSelectStatement(sql);
            }
        }

        // 4. 移除多余空行
        return sql.replaceAll("\\n\\s*\\n", "\n").trim();
    }

    private static String formatInsertStatement(String sql) {
        int intoIndex = sql.toUpperCase().indexOf("INSERT INTO");
        if (intoIndex >= 0) {
            intoIndex += 11;
            int valuesIndex = sql.toUpperCase().indexOf("VALUES");
            if (valuesIndex > intoIndex) {
                String prefix = sql.substring(0, intoIndex) + " ";
                String columnsPart = sql.substring(intoIndex, valuesIndex).trim();
                columnsPart = columnsPart.replaceAll("\\(\\s*", "(\n      ");
                columnsPart = columnsPart.replaceAll(",\\s*", ",\n      ");
                sql = prefix + columnsPart + " " + sql.substring(valuesIndex);
            }
        }
        int valuesPos = sql.toUpperCase().indexOf("VALUES");
        if (valuesPos >= 0) {
            valuesPos += 6;
            if (valuesPos < sql.length()) {
                String prefix2 = sql.substring(0, valuesPos) + " ";
                String valuesPart = sql.substring(valuesPos).trim();
                valuesPart = valuesPart.replaceAll("\\(\\s*", "(\n      ");
                valuesPart = valuesPart.replaceAll(",\\s*", ",\n      ");
                valuesPart = valuesPart.replaceAll("\\)", "\n    )");
                sql = prefix2 + valuesPart;
            }
        }
        return sql;
    }

    private static String formatUpdateStatement(String sql) {
        int setIndex = sql.toUpperCase().indexOf("SET");
        if (setIndex >= 0) {
            int setEnd = setIndex + 3;
            int whereIndex = sql.toUpperCase().indexOf("WHERE");
            if (whereIndex < 0) {
                whereIndex = sql.length();
            }
            if (whereIndex > setEnd) {
                String prefix = sql.substring(0, setEnd);
                String setClause = sql.substring(setEnd, whereIndex).trim();
                setClause = setClause.replaceAll(",\\s*", ",\n      ");
                sql = prefix + "\n      " + setClause + "\n  " + sql.substring(whereIndex);
            }
        }
        return formatWhereConditions(sql);
    }

    private static String formatDeleteStatement(String sql) {
        return formatWhereConditions(sql);
    }

    private static String formatSelectStatement(String sql) {
        if (sql.toUpperCase().contains("SELECT") && sql.toUpperCase().contains("FROM")) {
            int selectIndex = sql.toUpperCase().indexOf("SELECT");
            int selectEnd = selectIndex + 6;
            int fromIndex = sql.toUpperCase().indexOf("FROM");
            if (fromIndex > selectEnd) {
                String prefix = sql.substring(0, selectEnd);
                String fields = sql.substring(selectEnd, fromIndex).trim();
                fields = fields.replaceAll(",\\s*", ",\n      ");
                sql = prefix + "\n      " + fields + "\n  " + sql.substring(fromIndex);
            }
        }
        return formatWhereConditions(sql);
    }

    private static String formatWhereConditions(String sql) {
        int whereIndex = sql.toUpperCase().indexOf("WHERE");
        if (whereIndex > 0) {
            String wherePart = sql.substring(whereIndex);
            wherePart = wherePart.replaceAll("(?i)\\bAND\\b", "\n    AND")
                    .replaceAll("(?i)\\bOR\\b", "\n    OR");
            sql = sql.substring(0, whereIndex) + wherePart;
        }
        return sql;
    }

    private static String getIndentForKeyword(String keyword) {
        String upper = keyword.toUpperCase();
        if (upper.equals("AND") || upper.equals("OR")) {
            return "    ";
        }
        if (upper.equals("ON")) {
            return "      ";
        }
        return "  ";
    }
}
