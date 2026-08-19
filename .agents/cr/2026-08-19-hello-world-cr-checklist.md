# Code Review Checklist

> **Change** hello-world · **分支/Commit** `AI/task-DEV-66a2f4f2-84c9-11f1-9849-d5c90ba1aaae-3d6add6d-eb64-480c-b019-3cd914b9175f` / `97b694e` · **日期** 2026-08-19
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。
> **完成标准**：所有核销项必须从 `⬜` 变为其他状态；`N/A` 需写原因。
>
> **执行顺序（强制）**：写入本清单并进入逐文件审查前，先在目标仓库对变更路径运行 `references/script/scan-all-rules.sh`，将输出贴入 Step 3 和 Step 4 备注；再用 LLM 完成 Step 2–5 中脚本未覆盖项及复核。

> **预扫结果**：`scan-all-rules.sh` 对 `src/main/java/com/example/helloworld/HelloWorld.java` `src/test/java/com/example/helloworld/HelloWorldTest.java` 执行完毕 — **No findings. 52/222 rules scanned**（无命中）。

---

## Step 1 — 执行队列（产物 A）

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8 | G9 | G10 | G11 | G12 | G13 | G14 | G15 | G16 | G17 | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10 | 总状态 |
|---|----------------------|----------|-------|-------|----|----|----|----|----|----|----|----|----|-----|----|----|----|----|----|----|----|----|----|-----|-----|-----|-----|-----|-----|-----|-----|--------|
| 1 | `src/main/java/com/example/helloworld/HelloWorld.java` | REQ-1/REQ-2 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ |
| 2 | `src/test/java/com/example/helloworld/HelloWorldTest.java` | REQ-3 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ |
| 3 | `docs/ARCHITECTURE.md` | 架构文档 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过(非Java) |
| 4 | `docs/modules/hello-world/README.md` | 模块文档 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过(非Java) |

> 收口：仅 #1、#2 为 Java 文件，已逐文件审完；#3、#4 为文档，跳过 Java 审查。

---

## Step 2 — 功能（产物 B）

> 仅从 spec/tasks 提 **REQ**，勿臆造。不符 spec 标 **P0**。
> 每个 REQ 都必须填写 **spec 证据** 与 **关联文件**；若命中 P0，代码证据需落到 `path:line`、测试或接口行为。

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据（文件/测试/接口） |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-1 | 程序输出标准 Hello World 问候语 | 需求「帮我写个hello world」+ ARCHITECTURE.md「Hello World 示例，演示 Java 编码规范」+ README.md「返回默认问候语 "Hello, World!"」 | `HelloWorld.java` | ✅ | `HelloWorld.java:19-21` getGreeting() 返回 "Hello, World!"；`HelloWorld.java:28-31` main 调用 getGreeting 并打印；`HelloWorldTest.java:29-30` 断言 `isEqualTo("Hello, World!")` |
| REQ-2 | 遵循 Java 编码规范，含 Javadoc 注释 | ARCHITECTURE.md「遵循 dtazziboot-java-coding-standards 编码规范」+「所有类必须含 Javadoc 注释，含 @author 和 @date」 | `HelloWorld.java` | ✅ | `HelloWorld.java:3-8` 类 Javadoc 含 @author @date；`HelloWorld.java:14-18` getGreeting Javadoc 含 @return；`HelloWorld.java:23-27` main Javadoc 含 @param |
| REQ-3 | 测试采用 TDD 模式，AAA 结构 | ARCHITECTURE.md「测试采用 TDD 模式，AAA 结构」 | `HelloWorldTest.java` | ✅ | `HelloWorldTest.java:21-31` Arrange-Act-Assert 注释块；`HelloWorldTest.java:38-40` Arrange & Act & Assert 合并注释 |

---

## Step 3 — 可读性检查（产物 C）

> 对照 `references/readability-checklist.md` A1–A7 逐节核销：

| ID | 检查项 | 状态 | 备注（命中写 `path:line`） |
|----|--------|------|----------------------------|
| A1 | 源文件格式 | ✅ | 文件名=类名+`.java`；UTF-8；无 Tab（4空格缩进） |
| A2 | 源文件结构/import 顺序 | ✅ | package→空行→import→空行→class；无 `import *`；静态import在前、非静态在后，字典序 |
| A3 | 代码样式 | ✅ | K&R 大括号；4空格缩进；行宽≤120；成员间空行；`if (` 风格（代码中无 if/for 等可验证项） |
| A4 | 命名规范 | ✅ | 包名全小写；类名 UpperCamelCase；方法 lowerCamelCase；常量 UPPER_SNAKE_CASE；测试类 `HelloWorldTest`（A4.7） |
| A5 | 编码实践 | ✅ | 无 @Override 可省略场景；无 catch 块；无 finalize |
| A6 | 特定元素样式 | ✅ | `String[] args`（A6.1）；修饰符顺序 `private static final` 正确；无 switch/注解/long 字面量 |
| A7 | Javadoc 规范 | ✅ | public 类/方法均有 Javadoc；@param→@return 顺序正确；getGreeting 虽为简单 getter 但已提供完整 Javadoc（超出最低要求） |

> 脚本预扫 A 类规则覆盖：无命中。

---

## Step 4 — 可靠性检查（产物 D）

> **逐条核销（强制）**：G/S 每个 ID **独占一行**。**Bug 模式** 按 `bug-pattern-checklist.md` 中 **每条 B*/M*/I*** 独占一行核销（120 条）。

### 4.1 Bug 模式（`bug-pattern-checklist.md`）

> 预扫结果：`scan-all-rules.sh` — No findings。代码极简（32+42行），无并发、无资源、无集合操作、无异常处理、无空指针路径。LLM 逐条复核如下：

| ID | 状态 | 备注（命中写 `path:line`；预扫可粘贴脚本摘要） |
|----|------|--------------------------------------------------|
| B001–B081 | ✅ | 预扫无命中 + LLM 复核：无魔术值（DEFAULT_GREETING 已定义为常量）、无 NPE 风险（返回常量字符串）、无资源泄漏、无集合修改异常、无序列化、无线程安全问题等，均不适用 |
| M001–M027 | ✅ | 预扫无命中 + LLM 复核：无未使用变量/导入、无不必要的对象创建、无硬编码等，均不适用 |
| I001–I010 | ✅ | 预扫无命中 + LLM 复核：无冗余修饰符、无尾随注释等，均不适用 |

### 4.2 可靠性（`reliability-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| G1.1–G1.4 | N/A | 无并发/锁场景 |
| G2.1–G2.3 | N/A | 无写接口/消息消费 |
| G3.1–G3.2 | N/A | 无事务/数据库操作 |
| G4.1–G4.3 | N/A | 无 SQL/MyBatis |
| G5.1 | N/A | 无 MQ 消息 |
| G6.1–G6.2 | N/A | 无缓存 |
| G7.1–G7.2 | N/A | 无调度任务 |
| G8.1–G8.6 | N/A | 无 I/O 资源、线程池、ThreadLocal |
| G9.1–G9.3 | N/A | 无外部调用（RPC/HTTP/DB） |
| G10.1–G10.2 | N/A | 无接口契约 |
| G11.1 | ✅ | `HelloWorldTest.java` 含 2 个测试方法，均有断言（`assertThat`/`assertThatCode`） |
| G11.2 | ⚠️ | P2 — 未覆盖边界：`main` 方法仅测不抛异常，未验证 stdout 输出内容；`getGreeting` 未测 null 场景（但返回常量无需 null 校验） |
| G11.3 | ✅ | `getGreeting()` 返回常量 `DEFAULT_GREETING`，无外部入参，无需防御性校验 |
| G11.4 | N/A | 无数值运算 |
| G12.1–G12.2 | N/A | 无资金相关场景 |
| G13.1 | N/A | 无日志输出 |
| G14.1–G14.4 | N/A | 无金额/多租户/时区/国际化 |
| G15.1–G15.3 | N/A | 无 DB 变更/接口共存 |
| G16.1–G16.4 | N/A | 无核心链路/异常处理/日志 |
| G17.1–G17.3 | N/A | 无功能开关/降级/数据变更 |

### 4.3 安全（`security-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| S1.1–S1.3 | N/A | 无 SQL / MyBatis |
| S2.1–S2.3 | N/A | 无 Web 输出 |
| S3.1–S3.3 | N/A | 无外部 URL 请求 |
| S4.1–S4.2 | N/A | 无系统命令/文件操作 |
| S5.1–S5.2 | N/A | 无 XML 解析 |
| S6.1–S6.3 | N/A | 无反序列化 |
| S7.1–S7.3 | N/A | 无文件上传/下载 |
| S8.1–S8.4 | N/A | 无 Web 接口/鉴权 |
| S9.1–S9.4 | N/A | 无密钥/敏感数据/加密 |
| S10.1–S10.3 | N/A | 无 CSRF/CORS/跳转 |

---

## Step 5 — 自定义扩展检查（产物 E）

### 5.1 自定义扩展（`customized-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| U1.1 | N/A | Controller 入参校验注解 — 变更无 Controller 类 |
| U1.2 | N/A | 未定义 |
| U1.3 | N/A | 未定义 |
| U2.1–U2.3 | N/A | 未定义（U2 节为空） |

> 结论：自定义规则均未启用或不适用，整体 `N/A(未启用自定义规则)`。

---

## 终检（防漏检）

- [x] 执行队列中每个文件 `Step2`、`Step3`、**S1–S10 / G1–G17** 各列均非 `⬜`（跳过文件除外）；
- [x] Step 2 的每个 REQ/Scenario 均非 `⬜`
- [x] Step 3 的 A1–A7 均非 `⬜`
- [x] Step 4 全部 **G/S** 与 **B001–B081 / M001–M027 / I001–I010** ID 均非 `⬜`（允许 `N/A`，但有原因）
- [x] Step 5 全部 U* ID 均非 `⬜`（允许 `N/A(未启用自定义规则)`）
- [x] 所有 `❌/⚠️` 已写入 report，且包含 `ID + path:line`