# Code Review Checklist

> **Change** hello-world · **分支/Commit** `AI/task-DEV-66a2f4f2-...` / `c1d7b7e` · **日期** `2026-08-20`
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。
> **完成标准**：所有核销项必须从 `⬜` 变为其他状态；`N/A` 需写原因。
>
> **执行顺序（强制）**：写入本清单并进入逐文件审查前，先在目标仓库对变更路径运行 `references/script/scan-all-rules.sh`，将输出贴入 Step 3 和 Step 4 备注；再用 LLM 完成 Step 2–5 中脚本未覆盖项及复核。
>
> **预扫结果**：`scan-all-rules.sh` 对 `src/hello-world/src/main/java/com/dt/example/hello/` 和 `src/hello-world/src/test/java/com/dt/example/hello/service/` 运行，结果：**No findings. 52/222 rules scanned**。

---

## Step 1 — 执行队列（产物 A）

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8 | G9 | G10 | G11 | G12 | G13 | G14 | G15 | G16 | G17 | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10 | 总状态 |
|---|----------------------|----------|-------|-------|----|----|----|----|----|----|----|----|----|-----|-----|-----|-----|-----|-----|-----|-----|----|----|----|----|----|----|----|----|----|-----|--------|
| 1 | src/hello-world/src/main/java/com/dt/example/hello/HelloWorldApplication.java | REQ-1 入口 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ |
| 2 | src/hello-world/src/main/java/com/dt/example/hello/common/constant/HelloWorldConstants.java | REQ-4 常量 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ |
| 3 | src/hello-world/src/main/java/com/dt/example/hello/service/HelloWorldService.java | REQ-2 接口 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ |
| 4 | src/hello-world/src/main/java/com/dt/example/hello/service/impl/HelloWorldServiceImpl.java | REQ-3 实现 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ |
| 5 | src/hello-world/src/test/java/com/dt/example/hello/service/HelloWorldServiceTest.java | 测试 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ |

> G1–G17 / S1–S10 全部 N/A：本模块为纯 CLI 独立应用，无并发/事务/DB/MQ/缓存/调度/网络/资损/多租户/灰度/应急等场景。安全维度无 SQL/XSS/SSRF/命令执行/XXE/反序列化/文件上传/访问控制/数据安全/CSRF。

---

## Step 2 — 功能（产物 B）

> 仅从 spec/tasks 提 **REQ**，勿臆造。不符 spec 标 **P0**。
> 每个 REQ 都必须填写 **spec 证据** 与 **关联文件**；若命中 P0，代码证据需落到 `path:line`、测试或接口行为。

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据（文件/测试/接口） |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-1 | 应用主入口，解析命令行参数 | docs/modules/hello-world/README.md L11: "应用主入口，解析命令行参数" | HelloWorldApplication.java | ✅ | `main(String[] args)` L13-17，`args[0]` 解析命令行参数 |
| REQ-2 | 问候服务接口，定义 greet(String) | docs/modules/hello-world/README.md L12: "问候服务接口，定义 `greet(String)`" | HelloWorldService.java | ✅ | `String greet(String name)` L17 |
| REQ-3 | 服务实现，处理 null/空白名称回退 | docs/modules/hello-world/README.md L13: "服务实现，处理 null/空白名称回退" | HelloWorldServiceImpl.java | ✅ | `normalizeName()` L24-29 处理 null/isBlank() |
| REQ-4 | 常量：默认问候前缀、默认名称、后缀 | docs/modules/hello-world/README.md L14: "默认问候前缀、默认名称、后缀" | HelloWorldConstants.java | ✅ | 三个常量 L15/L18/L21 |
| REQ-5 | greet 返回格式化问候语；null/空白→"World" | docs/modules/hello-world/README.md L24: "返回格式化的问候语；name 为 null/空白时使用默认名称 \"World\"" | HelloWorldServiceImpl.java, HelloWorldServiceTest.java | ✅ | `greet()` L14-18 返回 `"Hello, " + name + "!"`；测试覆盖 null/empty/blank/valid/中文 |

---

## Step 3 — 可读性检查（产物 C）

> 对照 `references/readability-checklist.md` A1–A7 逐节核销：

| ID | 检查项 | 状态 | 备注（命中写 `path:line`） |
|----|--------|------|----------------------------|
| A1 | 源文件格式 | ✅ | A1.1 文件名=类名+`.java`，A1.2 UTF-8，A1.3 无 Tab — 全部通过 |
| A2 | 源文件结构/import 顺序 | ✅ | A2.1 顺序正确，A2.2 无通配符 import，A2.3 静态/非静态分组（仅测试文件有静态 import），A2.4 字典序 — 全部通过 |
| A3 | 代码样式 | ✅ | A3.1 K&R 大括号，A3.3 4空格缩进，A3.4 行宽≤120，A3.7 关键字空格 — 全部通过 |
| A4 | 命名规范 | ✅ | A4.1 包名全小写，A4.2 类名 UpperCamelCase，A4.3 方法名 lowerCamelCase，A4.4 常量 UPPER_SNAKE_CASE，A4.5 无前缀后缀，A4.7 测试类名=被测类名+Test — 全部通过 |
| A5 | 编码实践 | ✅ | A5.1 `@Override` 已加（HelloWorldServiceImpl.java:13），A5.2 无 catch 块，A5.3 无静态方法实例调用，A5.4 无 finalize 重写 — 全部通过 |
| A6 | 特定元素样式 | ✅ | A6.1 `String[] args`（非 C 风格），A6.2 无 switch，A6.3 无多修饰符，A6.5 无 long 字面量 — 全部通过 |
| A7 | Javadoc 规范 | ✅ | A7.1 public 类/接口/方法均有 Javadoc，A7.2 @param→@return 顺序正确，A7.3 简单 getter/@Override 可省略（已满足） — 全部通过 |

---

## Step 4 — 可靠性检查（产物 D）

### 4.1 Bug 模式（`bug-pattern-checklist.md`）

> 预扫结果：`scan-all-rules.sh` — **No findings. 52/222 rules scanned**。以下 LLM 补全脚本未覆盖规则。

| ID | 状态 | 备注 |
|----|------|------|
| B001–B081 | N/A | 预扫无命中；LLM 复核：本模块无集合操作、异常处理、资源管理、并发、序列化等复杂逻辑，无适用 Bug 模式 |
| M001–M027 | N/A | 预扫无命中；LLM 复核：无适用 Major 级别缺陷模式 |
| I001–I010 | N/A | 预扫无命中；LLM 复核：无适用 Info 级别建议模式 |

### 4.2 可靠性（`reliability-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| G1.1–G1.4 | N/A | 无并发场景 |
| G2.1–G2.3 | N/A | 无写接口/消息消费 |
| G3.1–G3.2 | N/A | 无事务/数据库操作 |
| G4.1–G4.3 | N/A | 无 SQL/数据库 |
| G5.1 | N/A | 无 MQ |
| G6.1–G6.2 | N/A | 无缓存 |
| G7.1–G7.2 | N/A | 无调度任务 |
| G8.1–G8.6 | N/A | 无外部 I/O/线程池/ThreadLocal/连接 |
| G9.1–G9.3 | N/A | 无网络调用（纯 CLI 应用） |
| G10.1–G10.2 | N/A | 无外部接口契约 |
| G11.1 | ✅ | 有单测且有断言：HelloWorldServiceTest.java 5 个测试方法均有 assertThat 断言 |
| G11.2 | ⚠️ | 边界覆盖：null/空/空白/中文已覆盖；但缺少"带前后空白字符的有效名称"（如 `"  World  "`）测试，`normalizeName` 中的 `trim()` 行为未显式验证 — P2 |
| G11.3 | ✅ | `normalizeName` 对 null 有防御性校验：HelloWorldServiceImpl.java:25 |
| G11.4 | N/A | 无数值运算 |
| G12.1–G12.2 | N/A | 无资金相关场景 |
| G13.1 | N/A | 无日志输出 |
| G14.1–G14.4 | N/A | 无金额/多租户/时区/日期格式化 |
| G15.1–G15.3 | N/A | 无数据库/接口变更 |
| G16.1–G16.4 | N/A | 无日志/异常处理（纯 CLI 无 catch 块） |
| G17.1–G17.3 | N/A | 无功能开关/降级/数据变更 |
| G18.1–G18.3 | N/A | 无安全补强场景 |

### 4.3 安全（`security-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| S1.1–S1.3 | N/A | 无 SQL 操作 |
| S2.1–S2.3 | N/A | 无 Web 输出 |
| S3.1–S3.3 | N/A | 无外部 URL 请求 |
| S4.1–S4.2 | N/A | 无系统命令执行 |
| S5.1–S5.2 | N/A | 无 XML 解析 |
| S6.1–S6.3 | N/A | 无反序列化 |
| S7.1–S7.3 | N/A | 无文件上传/下载 |
| S8.1–S8.4 | N/A | 无 Web 接口/鉴权 |
| S9.1–S9.4 | N/A | 无密钥/敏感数据/加密 |
| S10.1–S10.3 | N/A | 无 CSRF/CORS/URL 跳转 |

---

## Step 5 — 自定义扩展检查（产物 E）

### 5.1 自定义扩展（`customized-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| U1.1 | N/A | 示例项（Controller 入参校验），本模块无 Controller |
| U1.2 | N/A | 未定义 |
| U1.3 | N/A | 未定义 |
| U2.1 | N/A | 未定义 |
| U2.2 | N/A | 未定义 |
| U2.3 | N/A | 未定义 |

> 自定义扩展清单中仅含示例项（U1.1），其余 U1.2–U2.3 均为空。整体标记为 **N/A(未启用自定义规则)**。

---

## 终检（防漏检）

- [x] 执行队列中每个文件 `Step2`、`Step3`、**S1–S10 / G1–G17** 各列均非 `⬜`（跳过文件除外）；
- [x] Step 2 的每个 REQ/Scenario 均非 `⬜`
- [x] Step 3 的 A1–A7 均非 `⬜`
- [x] Step 4 全部 **G/S** 与 **B001–B081 / M001–M027 / I001–I010** ID 均非 `⬜`（允许 `N/A`，但有原因）
- [x] Step 5 全部 U* ID 均非 `⬜`（允许 `N/A(未启用自定义规则)`）
- [x] 所有 `❌/⚠️` 已写入 report，且包含 `ID + path:line`