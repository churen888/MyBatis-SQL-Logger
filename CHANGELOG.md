# 版本变更日志 (CHANGELOG)

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
  - 右键菜单新增"删除选中内容"功能
  - 可使用快捷键进行编辑操作（Delete、Backspace、Ctrl+C/V/X等）
  - 支持在任意位置点击光标并输入新内容

### 🔧 优化改进
- 优化工具栏布局，添加分隔线提升视觉层次
- 改进用户交互体验，增强日志查看和编辑的便捷性
- 增强右键菜单功能，根据是否有选中内容智能显示操作选项
- 修复滚动到底部功能，改为操作JBScrollPane的滚动条而非Editor本身

### 📝 技术实现
- 新增 `ToggleListeningAction.java` - 实现监听开关切换
- 新增 `ScrollToBottomAction.java` - 实现快速跳转功能
- 扩展 `SqlConsoleService.java` - 添加监听状态管理
- 更新 `SqlConsolePanel.java` - 集成新功能到工具栏，添加编辑功能和滚动控制
- 添加 `deleteSelectedText()` 方法 - 实现选中内容删除功能
- 添加 `scrollToBottom()` 方法 - 正确控制滚动面板滚动到底部

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
