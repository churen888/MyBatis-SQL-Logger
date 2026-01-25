# 版本变更日志 (CHANGELOG)
# [1.0.2] - 2026-01-25

### ✨ 新增功能
- **SQL控制台搜索功能**: 添加编辑器内置搜索功能
  - 工具栏新增搜索按钮（🔍），点击打开搜索面板
  - 支持标准快捷键：`Ctrl+F` / `Cmd+F` 打开搜索
  - 搜索功能包括：
    - 实时搜索高亮显示所有匹配项
    - 上一个/下一个匹配项快速跳转
    - 匹配计数显示（如：1/5）
    - 大小写敏感/不敏感切换
    - 支持正则表达式搜索
  - 使用 IntelliJ Platform 标准 `EditorSearchSession` 组件，与 IDEA 编辑器搜索体验一致

### 🐛 问题修复
- **修复 Spring Boot 配置下 SQL 日志格式化失败问题**
  - 修复插件在 Spring Boot 运行配置下，SQL 末尾拼接日志后缀的问题
  - 问题原因：ProcessListener 按小片段接收日志，导致日志前缀（DEBUG、线程信息、类名等）被逐个单词发送并误收集为 SQL 内容
  - 解决方案：实现智能多行 SQL 收集策略，只收集以 SQL 关键字开头的行，拒绝日志碎片
  - 新增 `startsWithSqlKeyword()` 方法，定义 SQL 关键字白名单过滤规则
  - 优化 `PREPARING_PATTERN` 和 `PARAMETERS_PATTERN` 正则表达式，使用非贪婪匹配和边界检测
  - 确保 SQL 在 Application 和 Spring Boot 两种运行配置下都能正确格式化

### 🔧 优化改进
- 优化日志输出级别，将调试日志从 INFO 降级为 DEBUG
  - 正常使用时不会产生过多日志输出
  - 需要排查问题时可通过 Help > Diagnostic Tools > Debug Log Settings 启用
- 精简日志输出内容，合并匹配位置信息

### 📝 技术实现
- 改进 `SqlLogParser.parseLine()` 方法的多行收集逻辑
- 新增 `startsWithSqlKeyword()` 方法 - 判断行是否以 SQL 关键字开头
- 优化正则表达式边界检测：`(?:\\s+(?:DEBUG|INFO|WARN|ERROR|TRACE)|$)`
- 移除了复杂的 `isSqlContent()` 误判逻辑，采用更精确的关键字匹配
- 扩展 `SqlConsolePanel.java` - 集成 `EditorSearchSession` 搜索功能
- 新增 `showSearchPanel()` 方法 - 显示搜索面板
- 新增 `closeSearchPanel()` 方法 - 关闭搜索面板并清理资源

---

# [1.0.1] - 2026-01-22

### ✨ 新增功能
- **SQL日志监听开关**: 添加开始/停止监听日志按钮，支持动态控制SQL日志的捕获
  - 开启监听时显示暂停图标，停止监听时显示播放图标
  - 停止监听期间，新的SQL日志将不会被记录
  - 监听状态在项目级别持久化
  
- **快速跳转到底部**: 添加滚动到最下面按钮
  - 一键快速跳转到SQL日志的最底部
  - 方便查看最新的SQL执行记录
  - 使用向下箭头图标，直观易用

- **SQL控制台内容编辑功能**: 支持在SQL Console中直接编辑、删除和添加内容
  - 将编辑器从只读模式改为可编辑模式
  - 支持所有标准文本编辑操作（输入、删除、复制、粘贴等）
  - 右键菜单新增“删除选中内容”功能
  - 可使用快捷键进行编辑操作（Delete、Backspace、Ctrl+C/V/X等）
  - 支持在任意位置点击光标并输入新内容

- **启动欢迎信息**: 插件启动时在Run控制台和SQL Console显示欢迎信息
  - 展示插件功能特性
  - 显示作者信息：程序员 curen
  - 提供反馈邮箱：1139632166@qq.com
  - 使用 emoji 增强视觉效果

### 🐛 问题修复
- 修复文档修改必须使用 WriteCommandAction 而非 runWriteAction 的错误
- 修复用户编辑内容被新SQL覆盖的问题（改用 insertString 而非 setText）
- 修复删除的SQL被恢复的问题（移除历史记录加载逻辑）
- 修复滚动到底部功能不生效（改为操作 JBScrollPane 滚动条）

### 🔧 优化改进
- 优化工具栏布局，添加分隔线提升视觉层次
- 改进用户交互体验，增强日志查看和编辑的便捷性
- 增强右键菜单功能，根据是否有选中内容智能显示操作选项
- 优化内存管理，移除 SqlConsoleService 中的 SQL 历史缓存避免内存泄漏
- 简化 SQL 添加逻辑，直接通知监听器而不缓存

### 📝 技术实现
- 新增 `ToggleListeningAction.java` - 实现监听开关切换
- 新增 `ScrollToBottomAction.java` - 实现快速跳转功能
- 扩展 `SqlConsoleService.java` - 添加监听状态管理
- 重构 `SqlConsolePanel.java` - 集成新功能，实现可编辑模式和欢迎信息
- 优化 `SqlLogExecutionListener.java` - 添加 ConsoleView 输出欢迎信息功能
- 添加 `deleteSelectedText()` 方法 - 实现选中内容删除功能
- 添加 `scrollToBottom()` 方法 - 正确控制滚动面板滚动到底部
- 添加 `showWelcomeMessage()` 方法 - 在 UI 中显示欢迎信息
- 添加 `printWelcomeMessage()` 方法 - 在控制台输出欢迎信息

---

## [1.0.0] - 2026-01-21

### ✨ 核心功能

#### 1. SQL日志自动捕获与解析
- 自动监听 IDEA Run/Debug 控制台输出
- 智能识别 MyBatis/MyBatis-Plus SQL日志格式
  - 支持 `Preparing:` 语句识别
  - 支持 `Parameters:` 参数解析
- 参数类型智能转换
  - String → `'xxx'`
  - Number → `123`
  - Date → `'yyyy-MM-dd HH:mm:ss'`
  - Boolean → `1/0`
  - 支持 IN (...) 参数列表

#### 2. SQL格式化与美化
- SQL语句结构化格式化
  - SELECT / INSERT / UPDATE / DELETE 智能识别
  - 关键字自动换行 (SELECT, FROM, WHERE, JOIN, ORDER BY, GROUP BY 等)
  - WHERE 条件自动缩进
  - INSERT / UPDATE 语句列对齐
- 双线框样式包裹，提升可读性
- 时间戳标记，记录SQL执行时间

#### 3. 按SQL类型着色展示
- **查询(SELECT)**: 森林绿色 (#228B22)
- **插入(INSERT)**: 金黄色 (#DAA520)
- **更新(UPDATE)**: 道奇蓝色 (#1E90FF)
- **删除(DELETE)**: 深红色 (#DC143C)
- **其他类型**: 青色 (#00CED1)
- 边框、标题、分隔线统一着色，快速识别SQL类型

#### 4. 自定义 SQL Console 控制台
- 独立的 ToolWindow 界面
- 基于 IntelliJ Editor 组件，支持代码高亮
- 显示行号、代码折叠、缩进参考线
- 实时追加新的SQL日志
- 自动滚动到最新记录

#### 5. 便捷操作功能
- **复制SQL**: 一键复制编辑器中的SQL内容
- **清空控制台**: 快速清除所有SQL日志记录
- **右键菜单**: 
  - 有选中内容时: "复制选中内容"、"删除选中内容" (v1.0.1新增)
  - 无选中内容时: "复制所有 SQL"
  - 清除所有 SQL 日志
- 上下文感知的操作功能，智能判断用户意图

### 🏗️ 技术架构

#### 核心模块
- **listener**: 日志监听模块
  - `SqlLogExecutionListener.java` - 执行监听器
  - `SqlLogProcessListener.java` - 进程监听器
  
- **parser**: SQL解析模块
  - `SqlLogParser.java` - SQL日志解析器，参数替换
  - `SqlFormatter.java` - SQL格式化工具
  
- **service**: 服务层
  - `SqlConsoleService.java` - 单例服务，管理SQL数据和事件分发
  
- **ui**: 用户界面
  - `SqlConsolePanel.java` - 主面板，Editor集成和渲染
  - `SqlConsoleToolWindowFactory.java` - ToolWindow工厂类
  
- **action**: 用户操作
  - `CopySqlAction.java` - 复制SQL操作
  - `ClearConsoleAction.java` - 清空控制台操作

#### 设计特性
- **非侵入式**: 无需修改业务代码或添加依赖
- **项目级隔离**: 每个IDEA项目独立维护SQL日志
- **事件驱动**: 基于监听器模式，解耦UI和数据层
- **线程安全**: 使用 CopyOnWriteArrayList 保证并发安全

### 🛠️ 开发环境
- IntelliJ Platform SDK
- JDK 17+
- Gradle 构建系统
- 兼容 IntelliJ IDEA 2023.2+

### 📦 构建与发布
- Gradle 构建配置外置化管理
- 支持 `./gradlew build` 构建插件
- 支持 `./gradlew runIde` 调试运行
- 生成 distributions 发布包

---

## 说明

### 版本规范
本项目遵循 [语义化版本](https://semver.org/lang/zh-CN/) 规范：
- **主版本号(MAJOR)**: 不兼容的API修改
- **次版本号(MINOR)**: 向下兼容的功能性新增
- **修订号(PATCH)**: 向下兼容的问题修正

### 变更类型
- ✨ **新增功能 (Added)**: 新增的功能特性
- 🔧 **优化改进 (Changed)**: 对现有功能的改进
- 🐛 **问题修复 (Fixed)**: 修复的bug
- ⚠️ **废弃功能 (Deprecated)**: 即将移除的功能
- 🗑️ **移除功能 (Removed)**: 已移除的功能
- 🔒 **安全修复 (Security)**: 安全相关的修复
