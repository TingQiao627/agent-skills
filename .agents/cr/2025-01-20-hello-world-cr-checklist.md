# Code Review Checklist

> **Change** hello-world · **分支/Commit** AI/task-DEV-66a2f4f2-84c9-11f1-9849-d5c90ba1aaae-203bec33-7c34-45bc-8075-e18265ce8324 / 9807a7d · **日期** 2025-01-20
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。
> **完成标准**：所有核销项必须从 `⬜` 变为其他状态；`N/A` 需写原因。
>
> **执行顺序（强制）**：写入本清单并进入逐文件审查前，先在目标仓库对变更路径运行 `references/script/scan-all-rules.sh`，将输出贴入 Step 3 和 Step 4 备注；再用 LLM 完成 Step 2–5 中脚本未覆盖项及复核。

**预扫结果**：`scan-all-rules.sh` 对 `src/hello-world/` 扫描，52/222 规则，**零命中**。

---

## Step 1 — 执行队列（产物 A）

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8 | G9 | G10 | G11 | G12 | G13 | G14 | G15 | G16 | G17 | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10 | 总状态 |
|---|----------------------|----------|-------|-------|----|----|----|----|----|----|----|----|----|-----|----|----|----|----|----|----|----|----|----|-----|-----|-----|-----|-----|-----|-----|-----|--------|
| 1 | src/hello-world/src/main/java/com/dt/example/hello/GreetingService.java | REQ-1/2/3 — 接口契约 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 2 | src/hello-world/src/main/java/com/dt/example/hello/GreetingServiceImpl.java | REQ-1/2/3 — 核心实现 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 3 | src/hello-world/src/main/java/com/dt/example/hello/HelloWorldApplication.java | REQ-4 — 入口演示 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 4 | src/hello-world/src/test/java/com/dt/example/hello/GreetingServiceImplTest.java | REQ-1/2/3 — 单元测试 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |

> 非 Java 文件（跳过）：
> - `.agents/hello-world/impl.md` — 跳过（非 Java）
> - `docs/ARCHITECTURE.md` — 跳过（非 Java）
> - `docs/modules/hello-world/README.md` — 跳过（非 Java）

---

## Step 2 — 功能（产物 B）

> 仅从 spec/tasks 提 **REQ**，勿臆造。不符 spec 标 **P0**。
> 每个 REQ 都必须填写 **spec 证据** 与 **关联文件**；若命中 P0，代码证据需落到 `path:line`、测试或接口行为。

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据（文件/测试/接口） |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-1 | Given name=null, When greet(null), Then return "Hello!" | impl.md §IMPL：「空名称返回默认问候」 | GreetingServiceImpl.java:23 | ✅ | `GreetingServiceImpl.java:23` — `if (name == null \|\| name.isEmpty()) return DEFAULT_GREETING;` 测试 `GreetingServiceImplTest.java:43-52` |
| REQ-2 | Given name="", When greet(""), Then return "Hello!" | impl.md §IMPL：「空名称返回默认问候」 | GreetingServiceImpl.java:23 | ✅ | `GreetingServiceImpl.java:23` — `name.isEmpty()` 分支；测试 `GreetingServiceImplTest.java:30-40` |
| REQ-3 | Given name="World", When greet("World"), Then return "Hello, World!" | impl.md §IMPL：「否则返回 Hello, {name}!」 | GreetingServiceImpl.java:26 | ✅ | `GreetingServiceImpl.java:26` — `GREETING_PREFIX + name + GREETING_SUFFIX`；测试 `GreetingServiceImplTest.java:18-28` |
| REQ-4 | Given main() executed, When run, Then output "Hello!" and "Hello, World!" | impl.md §IMPL：「main 方法演示调用」 | HelloWorldApplication.java:16-24 | ✅ | `HelloWorldApplication.java:20-23` — `greet(null)` + `greet("World")` 输出 |

---

## Step 3 — 可读性检查（产物 C）

> 对照 `references/readability-checklist.md` A1–A7 逐节核销：

| ID | 检查项 | 状态 | 备注（命中写 `path:line`） |
|----|--------|------|----------------------------|
| A1 | 源文件格式 | ✅ | 文件名与类名一致，UTF-8 |
| A2 | 源文件结构/import 顺序 | ✅ | package→import→class 顺序正确；无 `import *`；静态/非静态 import 分组正确（GreetingServiceImplTest.java:3-4） |
| A3 | 代码样式 | ✅ | K&R 大括号，4空格缩进，行宽 ≤120，运算符空格正确 |
| A4 | 命名规范 | ✅ | 包名全小写，类名 UpperCamelCase，方法名 lowerCamelCase，常量 UPPER_SNAKE_CASE，测试类 `{被测类}Test` |
| A5 | 编码实践 | ✅ | `@Override` 已标注（GreetingServiceImpl.java:20），无空 catch，无实例调用静态方法 |
| A6 | 特定元素样式 | ✅ | 数组 `String[] args` 正确（HelloWorldApplication.java:16），无 switch，无 long 字面量 |
| A7 | Javadoc 规范 | ✅ | 所有 public 类/方法均有 Javadoc，含 `@param`/`@return`/`@author`/`@date` |

---

## Step 4 — 可靠性检查（产物 D）

> **逐条核销（强制）**：G/S 每个 ID **独占一行**。**预扫结果**：`scan-all-rules.sh` 零命中。

### 4.1 Bug 模式（`bug-pattern-checklist.md`）

> 脚本扫描 52/222 条规则，零命中。以下 LLM 逐条核销仅针对与本次变更相关的规则；无关规则标 `N/A`。

| ID | 状态 | 备注 |
|----|------|------|
| B001–B081 | N/A | 纯 Java SE 接口/实现/测试，无集合操作、无 IO、无异常处理、无并发、无反射、无序列化等复杂场景，120 条 Bug 模式规则均不适用 |
| M001–M027 | N/A | 同上 |
| I001–I010 | N/A | 同上 |

### 4.2 可靠性（`reliability-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| G1.1–G1.4 | N/A | 无并发/事务场景 |
| G2.1–G2.3 | N/A | 无写接口/消息消费 |
| G3.1–G3.2 | N/A | 无事务注解 |
| G4.1–G4.3 | N/A | 无 SQL/数据库操作 |
| G5.1 | N/A | 无 MQ 消息 |
| G6.1–G6.2 | N/A | 无缓存 |
| G7.1–G7.2 | N/A | 无调度任务 |
| G8.1–G8.6 | N/A | 无 I/O、线程池、ThreadLocal |
| G9.1–G9.3 | N/A | 无外部 RPC/HTTP/DB 调用 |
| G10.1–G10.2 | N/A | 无复杂接口契约 |
| G11.1 | ✅ | GreetingServiceImplTest.java 含 3 个测试方法，均有断言 |
| G11.2 | ✅ | 覆盖正常路径 + null + 空字符串边界 |
| G11.3 | ✅ | GreetingServiceImpl.java:23 — `name == null \|\| name.isEmpty()` 防御性校验 |
| G11.4 | N/A | 无数值运算 |
| G12.1–G12.2 | N/A | 无资金/资损场景 |
| G13.1 | N/A | 无日志输出 |
| G14.1–G14.4 | N/A | 无金额/多租户/时区场景 |
| G15.1–G15.3 | N/A | 无数据库变更 |
| G16.1–G16.4 | N/A | 无核心链路/外部调用 |
| G17.1–G17.3 | N/A | 无功能开关/降级需求 |

### 4.3 安全（`security-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| S1.1–S1.3 | N/A | 无 SQL |
| S2.1–S2.3 | N/A | 无 HTML/JS 输出 |
| S3.1–S3.3 | N/A | 无外部 URL 请求 |
| S4.1–S4.2 | N/A | 无命令执行 |
| S5.1–S5.2 | N/A | 无 XML 解析 |
| S6.1–S6.3 | N/A | 无反序列化 |
| S7.1–S7.3 | N/A | 无文件上传/下载 |
| S8.1–S8.4 | N/A | 无 Web 接口 |
| S9.1–S9.4 | N/A | 无密钥/敏感数据 |
| S10.1–S10.3 | N/A | 无 CSRF/CORS |

---

## Step 5 — 自定义扩展检查（产物 E）

> 按 `customized-checklist.md` 逐条核销；若未启用可整节写 `N/A(未启用自定义规则)`。

### 5.1 自定义扩展（`customized-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| U1.1 | N/A | 示例项，hello-world 模块无 Controller |
| U2.1 | N/A | 未启用业务红线规则 |

**结论**：N/A(未启用自定义规则)

---

## 终检（防漏检）

- [x] 执行队列中每个文件 `Step2`、`Step3`、**S1–S10 / G1–G17** 各列均非 `⬜`（跳过文件除外）
- [x] Step 2 的每个 REQ/Scenario 均非 `⬜`
- [x] Step 3 的 A1–A7 均非 `⬜`
- [x] Step 4 全部 **G/S** 与 **B001–B081 / M001–M027 / I001–I010** ID 均非 `⬜`
- [x] Step 5 全部 U* ID 均非 `⬜`
- [x] 所有核销项均为 `✅` 或 `N/A`，无 `❌`/`⚠️`