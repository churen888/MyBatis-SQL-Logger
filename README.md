# IDEA 插件开发提示词：MyBatis / MyBatis-Plus SQL 格式化控制台

你是一名 JetBrains IntelliJ IDEA 插件高级开发工程师，
需要开发一个用于 **MyBatis / MyBatis-Plus SQL 日志增强展示** 的 IDEA 插件。

## 🎯 插件目标

- 自动监听 Run / Debug 控制台输出
- 识别 MyBatis / MyBatis-Plus SQL 日志
- 将 `?` 占位符替换为真实参数
- 对 SQL 进行结构化、美化格式化
- 将结果输出到：
   - 自定义 ToolWindow（SQL Console）
   - 或增强 Console 输出

## 📦 功能要求

### 1. 日志识别
- 支持如下日志格式：
   - Preparing:
   - Parameters:
- 支持 MyBatis / MyBatis-Plus
- 支持多行 SQL

### 2. SQL 解析
- 正确解析参数类型：
   - String → `'xxx'`
   - Number → `123`
   - Date → `'yyyy-MM-dd HH:mm:ss'`
   - Boolean → `1 / 0`
- 支持 IN (...) 参数

### 3. SQL 格式化
- SELECT / INSERT / UPDATE / DELETE 区分
- 关键字换行：
   - SELECT / FROM / WHERE / AND / OR / JOIN / ORDER BY / GROUP BY
- INSERT / UPDATE 语句列对齐
- WHERE 条件缩进

### 4. IDEA 插件技术栈
- IntelliJ Platform SDK
- ToolWindow
- ConsoleView
- ProcessListener
- Kotlin 或 Java

### 5. 非侵入性
- 不修改用户业务代码
- 不要求用户引入额外依赖
- 插件启停可控

## 🚀 高级功能（可选）
- SQL 高亮
- 一键复制 SQL
- 导出为 .sql 文件
- 执行计划 Explain
- SQL 执行耗时统计

请给出：
1. 插件整体架构设计
2. 核心类设计
3. 关键代码示例
4. 正则解析方案
5. ToolWindow UI 设计建议
