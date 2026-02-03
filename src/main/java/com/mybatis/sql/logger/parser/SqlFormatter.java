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

        // 2. 根据操作类型进行格式化
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

        // 3. 移除多余空行
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
        // 处理 WHERE 子句
        return formatWhereAndOtherClauses(sql);
    }

    private static String formatDeleteStatement(String sql) {
        // 处理 WHERE 子句
        return formatWhereAndOtherClauses(sql);
    }

    private static String formatSelectStatement(String sql) {
        if (!sql.toUpperCase().contains("SELECT") || !sql.toUpperCase().contains("FROM")) {
            return sql;
        }

        StringBuilder result = new StringBuilder();
        String upperSql = sql.toUpperCase();
        
        // 1. 处理 SELECT 子句
        int selectIndex = upperSql.indexOf("SELECT");
        int selectEnd = selectIndex + 6;
        int fromIndex = upperSql.indexOf("FROM");
        
        if (fromIndex > selectEnd) {
            // SELECT 关键字
            String selectPart = sql.substring(selectIndex, selectEnd);
            result.append(selectPart);
            
            // 获取字段列表（可能包含 DISTINCT 等修饰符）
            String fieldsSection = sql.substring(selectEnd, fromIndex).trim();
            
            // 检查是否有 DISTINCT、ALL 等修饰符
            String modifier = "";
            String indent = "       "; // 默认为 "SELECT " 的长度 (7个空格)
            
            if (fieldsSection.toUpperCase().startsWith("DISTINCT ")) {
                modifier = "DISTINCT ";
                fieldsSection = fieldsSection.substring(9).trim();
                indent = "                "; // "SELECT DISTINCT " 的长度 (16个空格)
            } else if (fieldsSection.toUpperCase().startsWith("ALL ")) {
                modifier = "ALL ";
                fieldsSection = fieldsSection.substring(4).trim();
                indent = "           "; // "SELECT ALL " 的长度 (11个空格)
            }
            
            // 格式化字段列表：每个字段一行，统一缩进
            String[] fields = fieldsSection.split(",");
            if (fields.length > 0) {
                result.append(" ").append(modifier).append(fields[0].trim());
                for (int i = 1; i < fields.length; i++) {
                    result.append(",\n").append(indent).append(fields[i].trim());
                }
            }
            result.append("\n");
            
            // 2. 处理 FROM 子句
            int joinIndex = findNextKeywordIndex(upperSql, fromIndex, 
                "LEFT JOIN", "RIGHT JOIN", "INNER JOIN", "FULL JOIN", "CROSS JOIN", "JOIN",
                "WHERE", "GROUP BY", "ORDER BY", "LIMIT", "UNION", "HAVING");
            
            if (joinIndex < 0) {
                joinIndex = sql.length();
            }
            
            String fromClause = sql.substring(fromIndex, joinIndex).trim();
            result.append("FROM ").append(fromClause.substring(4).trim()).append("\n");
            
            // 3. 处理 JOIN 子句
            if (joinIndex < sql.length()) {
                String remaining = sql.substring(joinIndex);
                remaining = formatJoinClauses(remaining);
                result.append(remaining);
            }
        }
        
        return result.toString();
    }
    
    /**
     * 格式化 JOIN 子句
     */
    private static String formatJoinClauses(String sql) {
        StringBuilder result = new StringBuilder();
        String upperSql = sql.toUpperCase();
        int currentPos = 0;
        
        while (currentPos < sql.length()) {
            // 查找下一个 JOIN
            int nextJoinIndex = findNextKeywordIndex(upperSql, currentPos,
                "LEFT JOIN", "RIGHT JOIN", "INNER JOIN", "FULL JOIN", "CROSS JOIN", "JOIN");
            
            if (nextJoinIndex < 0) {
                // 没有更多 JOIN，处理剩余部分（WHERE/GROUP BY/ORDER BY 等）
                String remaining = sql.substring(currentPos);
                result.append(formatWhereAndOtherClauses(remaining));
                break;
            }
            
            // 确定 JOIN 类型和长度
            String joinType = "";
            int joinTypeLength = 0;
            for (String type : new String[]{"LEFT JOIN", "RIGHT JOIN", "INNER JOIN", "FULL JOIN", "CROSS JOIN", "JOIN"}) {
                if (upperSql.substring(nextJoinIndex).startsWith(type)) {
                    joinType = sql.substring(nextJoinIndex, nextJoinIndex + type.length());
                    joinTypeLength = type.length();
                    break;
                }
            }
            
            // 查找 ON 子句或下一个 JOIN
            int onIndex = upperSql.indexOf(" ON ", nextJoinIndex + joinTypeLength);
            int nextJoinAfterCurrent = findNextKeywordIndex(upperSql, nextJoinIndex + joinTypeLength,
                "LEFT JOIN", "RIGHT JOIN", "INNER JOIN", "FULL JOIN", "CROSS JOIN", "JOIN");
            int whereIndex = findNextKeywordIndex(upperSql, nextJoinIndex + joinTypeLength,
                "WHERE", "GROUP BY", "ORDER BY", "LIMIT", "HAVING");
            
            // 确定此 JOIN 子句的结束位置
            int joinEndIndex = sql.length();
            if (nextJoinAfterCurrent > 0) {
                joinEndIndex = Math.min(joinEndIndex, nextJoinAfterCurrent);
            }
            if (whereIndex > 0) {
                joinEndIndex = Math.min(joinEndIndex, whereIndex);
            }
            
            // 提取表名和 ON 条件
            String joinContent;
            if (onIndex > 0 && onIndex < joinEndIndex) {
                String tablePart = sql.substring(nextJoinIndex + joinTypeLength, onIndex).trim();
                String onPart = sql.substring(onIndex + 4, joinEndIndex).trim();
                // JOIN 和 ON 在同一行，整体缩进
                result.append("         ").append(joinType).append(" ").append(tablePart)
                      .append(" ON ").append(onPart).append("\n");
            } else {
                joinContent = sql.substring(nextJoinIndex, joinEndIndex).trim();
                result.append("         ").append(joinContent).append("\n");
            }
            
            currentPos = joinEndIndex;
        }
        
        return result.toString();
    }
    
    /**
     * 格式化 WHERE 和其他子句
     */
    private static String formatWhereAndOtherClauses(String sql) {
        StringBuilder result = new StringBuilder();
        String upperSql = sql.toUpperCase();
        
        // 查找 WHERE
        int whereIndex = upperSql.indexOf("WHERE");
        if (whereIndex >= 0) {
            result.append("WHERE ");
            
            // 查找 WHERE 子句的结束位置
            int whereEndIndex = findNextKeywordIndex(upperSql, whereIndex + 5,
                "GROUP BY", "ORDER BY", "LIMIT", "HAVING", "UNION");
            if (whereEndIndex < 0) {
                whereEndIndex = sql.length();
            }
            
            String whereClause = sql.substring(whereIndex + 5, whereEndIndex).trim();
            // 格式化 WHERE 条件：AND/OR 左对齐
            whereClause = whereClause.replaceAll("(?i)\\s+AND\\s+", "\n  AND ")
                                   .replaceAll("(?i)\\s+OR\\s+", "\n  OR ");
            result.append(whereClause).append("\n");
            
            // 处理剩余部分
            if (whereEndIndex < sql.length()) {
                result.append(formatOrderGroupBy(sql.substring(whereEndIndex)));
            }
        } else {
            // 没有 WHERE，直接处理其他子句
            result.append(formatOrderGroupBy(sql));
        }
        
        return result.toString();
    }
    
    /**
     * 格式化 ORDER BY、GROUP BY、HAVING、LIMIT 等子句
     */
    private static String formatOrderGroupBy(String sql) {
        StringBuilder result = new StringBuilder();
        String upperSql = sql.toUpperCase().trim();
        
        if (upperSql.isEmpty()) {
            return "";
        }
        
        // GROUP BY
        if (upperSql.startsWith("GROUP BY")) {
            int groupByEnd = findNextKeywordIndex(upperSql, 8, "HAVING", "ORDER BY", "LIMIT", "UNION");
            if (groupByEnd < 0) groupByEnd = sql.length();
            result.append("GROUP BY ").append(sql.substring(8, groupByEnd).trim()).append("\n");
            if (groupByEnd < sql.length()) {
                result.append(formatOrderGroupBy(sql.substring(groupByEnd)));
            }
        }
        // HAVING
        else if (upperSql.startsWith("HAVING")) {
            int havingEnd = findNextKeywordIndex(upperSql, 6, "ORDER BY", "LIMIT", "UNION");
            if (havingEnd < 0) havingEnd = sql.length();
            result.append("HAVING ").append(sql.substring(6, havingEnd).trim()).append("\n");
            if (havingEnd < sql.length()) {
                result.append(formatOrderGroupBy(sql.substring(havingEnd)));
            }
        }
        // ORDER BY
        else if (upperSql.startsWith("ORDER BY")) {
            int orderByEnd = findNextKeywordIndex(upperSql, 8, "LIMIT", "UNION");
            if (orderByEnd < 0) orderByEnd = sql.length();
            result.append("ORDER BY ").append(sql.substring(8, orderByEnd).trim()).append("\n");
            if (orderByEnd < sql.length()) {
                result.append(formatOrderGroupBy(sql.substring(orderByEnd)));
            }
        }
        // LIMIT
        else if (upperSql.startsWith("LIMIT")) {
            result.append("LIMIT ").append(sql.substring(5).trim());
        }
        // 其他情况
        else {
            result.append(sql.trim());
        }
        
        return result.toString();
    }
    
    /**
     * 查找下一个关键字的位置
     */
    private static int findNextKeywordIndex(String upperSql, int startPos, String... keywords) {
        int minIndex = -1;
        for (String keyword : keywords) {
            int index = upperSql.indexOf(keyword, startPos);
            if (index >= 0) {
                if (minIndex < 0 || index < minIndex) {
                    minIndex = index;
                }
            }
        }
        return minIndex;
    }




}
