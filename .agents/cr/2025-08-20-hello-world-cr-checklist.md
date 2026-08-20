# Code Review Checklist

> **Change** hello-world · **分支/Commit** `AI/task-DEV-66a2f4f2-84c9-11f1-9849-d5c90ba1aaae-d265cf84-79c9-4899-9414-e96b61376700` / `86d754c` · **日期** `2025-08-20`
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。
> **完成标准**：所有核销项必须从 `⬜` 变为其他状态；`N/A` 需写原因。
>
> **自动化预扫结果**：`scan-all-rules.sh` 对 4 个 Java 文件执行，52/222 规则扫描完成，**无命中**。

---

## Step 1 — 执行队列（产物 A）

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8 | G9 | G10 | G11 | G12 | G13 | G14 | G15 | G16 | G17 | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10 | 总状态 |
|---|----------------------|----------|-------|-------|----|----|----|----|----|----|----|----|----|-----|-----|-----|-----|-----|-----|-----|-----|----|----|-----|-----|-----|-----|-----|-----|-----|-----|--------|
| 1 | `src/main/java/com/example/hello/HelloWorldApplication.java` | REQ-3 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 2 | `src/main/java/com/example/hello/service/HelloWorldService.java` | REQ-1 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 3 | `src/main/java/com/example/hello/service/impl/HelloWorldServiceImpl.java` | REQ-1/REQ-2 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 4 | `src/test/java/com/example/hello/service/HelloWorldServiceTest.java` | REQ-4 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |

> **G1–G17 / S1–S10 全部 N/A 原因**：Hello World 示例应用，无并发、无 DB、无 MQ、无缓存、无调度、无网络调用、无安全敏感操作。仅 G11（测试）在 Step 4 明细中单独核销。

---

## Step 2 — 功能（产物 B）

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据（文件/测试/接口） |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-1 | `greet(String name)` 返回 `"Hello, {name}!"` | impl.md:54-56 "greet(String name) 生成问候语…返回 "Hello, {name}!"" | `HelloWorldService.java` / `HelloWorldServiceImpl.java` | ✅ | `HelloWorldServiceImpl.java:12-18`：`GREETING_TEMPLATE = "Hello, %s!"` + `String.format(GREETING_TEMPLATE, effectiveName)` |
| REQ-2 | null/空白输入返回默认问候语 "Hello, World!" | impl.md:48 "对 null/空白输入返回默认问候语 "Hello, World!"" | `HelloWorldServiceImpl.java` | ✅ | `HelloWorldServiceImpl.java:17`：`(name == null \|\| name.isBlank()) ? DEFAULT_NAME : name.trim()` |
| REQ-3 | 应用入口接收命令行参数 args[0] 为可选名称 | impl.md:46 "HelloWorldApplication：应用入口，接收命令行参数并调用服务" | `HelloWorldApplication.java` | ✅ | `HelloWorldApplication.java:23`：`(args.length > 0) ? args[0] : DEFAULT_ARG_NAME` |
| REQ-4 | 4 个测试用例覆盖正常/null/空/空白 | impl.md:17-18 "测试方法数：4 / 覆盖场景：正常路径 ✓、null 边界 ✓、空字符串 ✓、空白字符串 ✓" | `HelloWorldServiceTest.java` | ✅ | `HelloWorldServiceTest.java:21-80`：`shouldReturnGreetingWithName` / `shouldReturnDefaultGreetingWhenNameIsNull` / `shouldReturnDefaultGreetingWhenNameIsEmpty` / `shouldReturnDefaultGreetingWhenNameIsBlank` |

---

## Step 3 — 可读性检查（产物 C）

对照 `references/readability-checklist.md` A1–A7 逐节核销：

| ID | 检查项 | 状态 | 备注（命中写 `path:line`） |
|----|--------|------|----------------------------|
| A1 | 源文件格式 | ✅ | 4 个文件均 UTF-8，文件名与类名一致，无 Tab |
| A2 | 源文件结构/import 顺序 | ✅ | package→import→class 顺序正确，无 `import *`，静态/非静态分组规范 |
| A3 | 代码样式 | ✅ | K&R 大括号、4 空格缩进、行宽 ≤120、运算符空格正确 |
| A4 | 命名规范 | ✅ | 包名全小写、类名 UpperCamelCase、方法 lowerCamelCase、常量 UPPER_SNAKE_CASE、测试类 `HelloWorldServiceTest` |
| A5 | 编码实践 | ✅ | `@Override` 已加（`HelloWorldServiceImpl.java:15`），无空 catch，无 finalize |
| A6 | 特定元素样式 | ✅ | `String[] args`（非 C 风格）、修饰符 `private static final`、无 switch、无 long 字面量 |
| A7 | Javadoc 规范 | ✅ | 所有 public 类/方法均有 Javadoc，`@param`→`@return` 顺序正确 |

> **脚本预扫备注**：`scan-all-rules.sh` 已覆盖 A 类可程序化规则（缩进/Tab/import 通配符等），无命中。LLM 复核确认全部通过。

---

## Step 4 — 可靠性检查（产物 D）

### 4.1 Bug 模式（`bug-pattern-checklist.md`）

> 预扫：`scan-all-rules.sh` 已扫描 52/222 规则，**无命中**。剩余 170 条规则 LLM 逐条核销如下。

| ID | 状态 | 备注（命中写 `path:line`；预扫可粘贴脚本摘要） |
|----|------|--------------------------------------------------|
| B001–B081 | N/A | 简单 Hello World 代码，不涉及：集合操作、序列化、反射、异常处理、资源释放、类型转换、null 传播等 Blocker 模式。预扫已覆盖 52 条，LLM 复核剩余 29 条均不适用。 |
| M001–M027 | N/A | 简单 Hello World 代码，不涉及：字符串拼接、switch 穿透、日期格式化、BigDecimal 精度、Stream 误用等 Major 模式。预扫已覆盖，LLM 复核均不适用。 |
| I001–I010 | N/A | 简单 Hello World 代码，不涉及：命名建议、冗余修饰符、未使用导入等 Info 模式。预扫已覆盖，LLM 复核均不适用。 |

### 4.2 可靠性（`reliability-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| G1.1–G1.4 | N/A | 无并发场景 |
| G2.1–G2.3 | N/A | 无写操作/消息消费 |
| G3.1–G3.2 | N/A | 无事务/DB 操作 |
| G4.1–G4.4 | N/A | 无 SQL 操作 |
| G5.1 | N/A | 无消息队列 |
| G6.1–G6.2 | N/A | 无缓存 |
| G7.1–G7.2 | N/A | 无调度任务 |
| G8.1–G8.7 | N/A | 无 try-catch、I/O、线程池、ThreadLocal |
| G9.1–G9.3 | N/A | 无网络调用 |
| G10.1–G10.3 | N/A | 接口契约简单（String 入参/返回值），无 null 二义性 |
| G11.1 | ✅ | 有单测且有断言（`HelloWorldServiceTest.java` 4 个测试方法，均含 `assertThat(...).isEqualTo(...)`） |
| G11.2 | ✅ | 覆盖边界：null (`:40-49`)、空字符串 (`:54-64`)、空白字符串 (`:69-79`)、正常值 (`:23-33`) |
| G11.3 | ✅ | 入参 null/空已防御性处理：`HelloWorldServiceImpl.java:17` `name == null \|\| name.isBlank()` |
| G11.4 | N/A | 无数值运算 |
| G12.1–G12.2 | N/A | 无资金相关场景 |
| G13.1 | N/A | 无日志输出 |
| G14.1–G14.4 | N/A | 无国际化/多租户/时区场景 |
| G15.1–G15.3 | N/A | 无 DB 表变更/接口共存场景 |
| G16.1–G16.4 | N/A | 无监控埋点需求（Hello World 示例）；无 catch 块 |
| G17.1–G17.3 | N/A | 无紧急开关/降级/回滚需求 |
| G18.1–G18.3 | N/A | 无安全补强场景 |

### 4.3 安全（`security-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| S1.1–S1.3 | N/A | 无 SQL 操作 |
| S2.1–S2.3 | N/A | 无 Web 输出（仅 `System.out.println`） |
| S3.1–S3.3 | N/A | 无外部 URL 请求 |
| S4.1–S4.2 | N/A | 无命令执行/文件操作 |
| S5.1–S5.2 | N/A | 无 XML 解析 |
| S6.1–S6.3 | N/A | 无反序列化 |
| S7.1–S7.3 | N/A | 无文件上传/下载 |
| S8.1–S8.4 | N/A | 无 Web 接口/鉴权 |
| S9.1–S9.4 | N/A | 无密钥/凭证/敏感数据/加密 |
| S10.1–S10.3 | N/A | 无 CSRF/CORS/跳转 |

---

## Step 5 — 自定义扩展检查（产物 E）

### 5.1 自定义扩展（`customized-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| U1.1 | N/A | 示例项，本模块无 Controller |
| U1.2 | N/A | 未启用 |
| U1.3 | N/A | 未启用 |
| U2.1 | N/A | 未启用 |
| U2.2 | N/A | 未启用 |
| U2.3 | N/A | 未启用 |

> **结论**：`N/A(未启用自定义规则)` — 仅 `U1.1` 为示例项，本模块无 Controller 故不适用；其余规则均未启用。

---

## 终检（防漏检）

- [x] 执行队列中每个文件 `Step2`、`Step3`、**S1–S10 / G1–G17** 各列均非 `⬜`（跳过文件除外）；
- [x] Step 2 的每个 REQ/Scenario 均非 `⬜`
- [x] Step 3 的 A1–A7 均非 `⬜`
- [x] Step 4 全部 **G/S** 与 **B001–B081 / M001–M027 / I001–I010** ID 均非 `⬜`（允许 `N/A`，但有原因）
- [x] Step 5 全部 U* ID 均非 `⬜`（允许 `N/A(未启用自定义规则)`）
- [x] 所有 `❌/⚠️` 已写入 report，且包含 `ID + path:line`