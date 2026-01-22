# MyBatis SQL Logger - IDEA Plugin

<div align="center">
  <h3>MyBatis / MyBatis-Plus SQL 日志格式化插件</h3>
  <p>自动监听控制台 SQL 日志，提供美观的格式化展示</p>
</div>

---

## ✨ 功能特性

- ✅ **自动监听** - 自动识别 MyBatis / MyBatis-Plus SQL 日志
- ✅ **参数替换** - 将 `?` 占位符替换为真实参数值
- ✅ **SQL 格式化** - 支持 SELECT/INSERT/UPDATE/DELETE 结构化格式化
- ✅ **语法高亮** - SQL 语法高亮显示
- ✅ **专属面板** - 独立的 SQL Console ToolWindow
- ✅ **一键复制** - 快速复制格式化后的 SQL
- ✅ **非侵入式** - 无需修改业务代码

## 📦 安装方式

### 方式一：本地构建安装

1. 克隆项目
```bash
git clone https://github.com/your-repo/mybatis-sql-logger.git
cd mybatis-sql-logger
```

2. 构建插件
```bash
./gradlew buildPlugin
```

3. 在 IDEA 中安装
- 打开 `Settings/Preferences` → `Plugins`
- 点击 ⚙️ → `Install Plugin from Disk...`
- 选择 `build/distributions/mybatis-sql-logger-1.0.0.zip`

### 方式二：从源码运行

```bash
./gradlew runIde
```

## 🚀 使用说明

### 1. 打开 SQL Console

运行/调试项目后，打开 SQL Console 面板：
- 菜单：`View` → `Tool Windows` → `SQL Console`
- 或点击底部工具栏的 `SQL Console` 标签

### 2. 查看格式化 SQL

插件会自动监听控制台输出，识别 MyBatis 日志并格式化：

**原始日志：**
```
==>  Preparing: SELECT id, name, age FROM user WHERE id = ? AND status = ?
==>  Parameters: 1001(Integer), 1(Integer)
```

**格式化后：**
```sql
SELECT
      id,
      name,
      age
  FROM user
  WHERE id = 1001
    AND status = 1
```

### 3. 工具栏功能

- **复制 SQL** - 复制选中或全部 SQL 到剪贴板
- **清空控制台** - 清空当前所有 SQL 记录

## 🎯 支持的日志格式

### MyBatis 标准日志
```
==>  Preparing: SELECT * FROM user WHERE id = ?
==>  Parameters: 100(Integer)
```

### MyBatis-Plus 日志
```
==>  Preparing: SELECT id,name,email FROM user WHERE deleted=? AND id=?
==>  Parameters: 0(Integer), 1001(Long)
```

## 📐 项目结构

```
mybatis-sql-logger/
├── src/main/java/com/mybatis/sql/logger/
│   ├── action/              # Action 处理
│   │   ├── CopySqlAction.java
│   │   └── ClearConsoleAction.java
│   ├── listener/            # 日志监听器
│   │   └── SqlLogProcessListener.java
│   ├── parser/              # SQL 解析器
│   │   ├── SqlLogParser.java
│   │   └── SqlFormatter.java
│   ├── service/             # 服务层
│   │   └── SqlConsoleService.java
│   └── ui/                  # UI 组件
│       ├── SqlConsoleToolWindowFactory.java
│       └── SqlConsolePanel.java
├── src/main/resources/
│   ├── META-INF/
│   │   └── plugin.xml       # 插件配置
│   └── icons/
│       └── sql-console.svg  # 图标资源
├── build.gradle.kts         # Gradle 构建脚本
└── README.md
```

## 🔧 技术栈

- **语言**: Java 17
- **构建工具**: Gradle 8.x
- **插件框架**: IntelliJ Platform SDK
- **IDEA 版本**: 2023.2+

## 🛠️ 开发说明

### 环境要求

- JDK 17+
- IntelliJ IDEA 2023.2+
- Gradle 8.0+

### 构建命令

```bash
# 构建插件
./gradlew buildPlugin

# 运行插件
./gradlew runIde

# 验证插件
./gradlew verifyPlugin

# 清理构建
./gradlew clean
```

### 调试插件

在 `build.gradle.kts` 中配置 IDEA 版本：
```kotlin
intellij {
    version.set("2023.2")
    type.set("IC") // 或 "IU" 为 Ultimate
}
```

## 📝 核心实现

### 1. SQL 日志解析
基于正则表达式识别 MyBatis 日志格式：
```java
Pattern.compile("==>\\s+Preparing:\\s+(.+)")
Pattern.compile("==>\\s+Parameters:\\s+(.+)")
```

### 2. 参数替换
根据参数类型自动格式化：
- String → `'value'`
- Number → `123`
- Date → `'yyyy-MM-dd HH:mm:ss'`
- Boolean → `1/0`

### 3. SQL 格式化
参考原始代码实现：
- 关键字换行对齐
- 列名缩进
- WHERE 条件缩进
- JOIN 条件处理

## 🤝 参与贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License

## 🔗 相关链接

- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [MyBatis 官方文档](https://mybatis.org/)
- [MyBatis-Plus 官方文档](https://baomidou.com/)

---

<div align="center">
  Made with ❤️ by MyBatis Community
</div>
