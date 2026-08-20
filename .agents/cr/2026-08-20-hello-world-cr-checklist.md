# Code Review Checklist

> **Change** hello-world · **分支/Commit** AI/task-DEV-66a2f4f2-84c9-11f1-9849-d5c90ba1aaae-df347fde-6d1f-4849-a345-7ca4f8885277 / 195ef79 · **日期** 2026-08-20
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。
> **完成标准**：所有核销项必须从 `⬜` 变为其他状态；`N/A` 需写原因。
>
> **预扫结果**：`scan-all-rules.sh` 对 5 个 Java 文件执行，52/222 规则扫描，**无发现**。

---

## Step 1 — 执行队列（产物 A）

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8 | G9 | G10 | G11 | G12 | G13 | G14 | G15 | G16 | G17 | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10 | 总状态 |
|---|----------------------|----------|-------|-------|----|----|----|----|----|----|----|----|----|-----|----|----|----|----|----|----|----|----|----|-----|-----|-----|-----|-----|-----|-----|-----|--------|
| 1 | `src/hello-world/src/main/java/com/dt/example/helloworld/HelloWorldApplication.java` | REQ-1 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 2 | `src/hello-world/src/main/java/com/dt/example/helloworld/controller/HelloWorldController.java` | REQ-2,3 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 3 | `src/hello-world/src/main/java/com/dt/example/helloworld/service/HelloWorldService.java` | REQ-2,3,4,5 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 4 | `src/hello-world/src/main/java/com/dt/example/helloworld/service/impl/HelloWorldServiceImpl.java` | REQ-2,3,4,5 | ⚠️ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ 已审有问题 |
| 5 | `src/hello-world/src/test/java/com/dt/example/helloworld/service/impl/HelloWorldServiceImplTest.java` | REQ-2,3,4,5 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |

> 非 Java 文件（跳过）：`src/hello-world/pom.xml`、`src/hello-world/docs/README.md`

---

## Step 2 — 功能（产物 B）

> 需求来源：`<requirement_section>` "帮我写个hello world"。从需求 + 代码接口契约推导 REQ。

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据（文件/测试/接口） |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-1 | **Given** 应用启动 **When** 服务就绪 **Then** 可接受 HTTP 请求 | 需求："帮我写个hello world"（隐含可运行服务） | HelloWorldApplication.java | ✅ | `HelloWorldApplication.java:19-20` — `SpringApplication.run()` |
| REQ-2 | **Given** 无 name 参数 **When** GET /api/hello **Then** 返回 "Hello, World!" | 需求："hello world" | HelloWorldController.java, HelloWorldServiceImpl.java | ✅ | `HelloWorldServiceImpl.java:33-34` — 默认消息；`HelloWorldServiceImplTest.java:29-38` — 测试覆盖 |
| REQ-3 | **Given** name=DTCoder **When** GET /api/hello?name=DTCoder **Then** 返回 "Hello, DTCoder!" | 需求："hello world"（带名称变体） | HelloWorldController.java, HelloWorldServiceImpl.java | ✅ | `HelloWorldServiceImpl.java:36` — 个性化消息；`HelloWorldServiceImplTest.java:43-53` — 测试覆盖 |
| REQ-4 | **Given** name="" 或 name="   " **When** GET /api/hello **Then** 返回 "Hello, World!" | 接口契约 `HelloWorldService.java:13` | HelloWorldServiceImpl.java | ✅ | `HelloWorldServiceImpl.java:33-34` — isBlank 判断；`HelloWorldServiceImplTest.java:58-82` — 2 个测试 |
| REQ-5 | **Given** name 长度 > 100 **When** greet(name) **Then** 抛出 IllegalArgumentException | 接口契约 `HelloWorldService.java:18` | HelloWorldServiceImpl.java | ✅ | `HelloWorldServiceImpl.java:29-31` — 长度校验；`HelloWorldServiceImplTest.java:86-92` — 测试覆盖 |

---

## Step 3 — 可读性检查（产物 C）

> 对照 `references/readability-checklist.md` A1–A7 逐节核销。预扫脚本无 A* 发现。

| ID | 检查项 | 状态 | 备注（命中写 `path:line`） |
|----|--------|------|----------------------------|
| A1 | 源文件格式 | ✅ | 所有文件 UTF-8，文件名=类名+.java，无 Tab |
| A2 | 源文件结构/import 顺序 | ✅ | package→import→类，无 `import *`，静态/非静态分组正确 |
| A3 | 代码样式 | ✅ | K&R 大括号，4 空格缩进，行宽 ≤120，成员间空行 |
| A4 | 命名规范 | ✅ | 包名全小写，类名 UpperCamelCase，方法 lowerCamelCase，常量 UPPER_SNAKE_CASE，测试类名 +Test |
| A5 | 编码实践 | ✅ | `@Override` 已加（`HelloWorldServiceImpl.java:27`），无空 catch，无 finalize |
| A6 | 特定元素样式 | ✅ | `String[] args` 正确，无 switch，修饰符顺序正确，long 无使用 |
| A7 | Javadoc 规范 | ✅ | 所有 public 类/方法有 Javadoc，`@param`/`@return`/`@throws` 顺序正确 |

---

## Step 4 — 可靠性检查（产物 D）

> **预扫**：`scan-all-rules.sh` 对 5 个 Java 文件执行，52/222 规则扫描，**无发现**。以下由 LLM 补全脚本未覆盖项。

### 4.1 Bug 模式（`bug-pattern-checklist.md`）

> 预扫脚本覆盖 52/222 条（B/M/I 子集），无命中。LLM 逐条核销剩余 170 条，本变更（Hello World 简单示例）无数据库/并发/IO/序列化等复杂场景，全部标 N/A。

| 范围 | 状态 | 备注 |
|------|------|------|
| B001–B081 (81 Blocker) | N/A | 预扫无命中；LLM 复核：无数据库操作、无并发、无序列化、无 IO 流——Hello World 示例不涉及这些场景 |
| M001–M027 (27 Major) | N/A | 同上 |
| I001–I010 (10 Info) | N/A | 同上 |

### 4.2 可靠性（`reliability-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| G1.1–G1.4 (并发) | N/A | 无并发场景 |
| G2.1–G2.3 (幂等) | N/A | 纯读接口，无写操作 |
| G3.1–G3.2 (事务) | N/A | 无数据库操作 |
| G4.1–G4.3 (SQL) | N/A | 无 SQL 操作 |
| G5.1 (MQ) | N/A | 无消息队列 |
| G6.1–G6.2 (缓存) | N/A | 无缓存 |
| G7.1–G7.2 (调度) | N/A | 无调度任务 |
| G8.1–G8.6 (防御编程) | ✅ | 已核：无 I/O 资源、无线程池、无 ThreadLocal。`HelloWorldServiceImpl.java:29-31` 对异常输入做了防御（null/blank/超长） |
| G9.1–G9.3 (网络调用) | N/A | 无外部 RPC/HTTP 调用 |
| G10.1–G10.2 (接口契约) | N/A | 无复杂字段语义 |
| G11.1–G11.4 (自测) | ✅ | 已核：5 个测试覆盖正常/边界/异常；`HelloWorldServiceImpl.java:29` 对 null 做了防御性校验 |
| G12.1–G12.2 (资损) | N/A | 无资金操作 |
| G13.1 (监控) | N/A | 无日志输出（Hello World 示例，可接受） |
| G14.1–G14.4 (国际化) | N/A | 无金额/多租户/时区 |
| G15.1–G15.3 (灰度) | N/A | 无数据库变更/接口演进 |
| G16.1–G16.4 (监控) | N/A | 无核心链路/异常处理 |
| G17.1–G17.3 (应急) | N/A | 无功能开关/降级需求 |

### 4.3 安全（`security-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| S1.1–S1.3 (SQL) | N/A | 无 SQL |
| S2.1–S2.3 (XSS) | N/A | 返回纯文本 "Hello, xxx!"，无 HTML/JS 渲染场景 |
| S3.1–S3.3 (SSRF) | N/A | 无外部 URL 请求 |
| S4.1–S4.2 (命令执行) | N/A | 无系统命令 |
| S5.1–S5.2 (XXE) | N/A | 无 XML 解析 |
| S6.1–S6.3 (反序列化) | N/A | 无自定义反序列化 |
| S7.1–S7.3 (文件) | N/A | 无文件上传/下载 |
| S8.1–S8.4 (访问控制) | N/A | Hello World 公开接口，无需鉴权；GET 方法只读，符合 REST |
| S9.1–S9.4 (数据安全) | N/A | 无密钥/敏感数据/加密 |
| S10.1–S10.3 (CSRF/CORS) | N/A | 无增删改操作；无跨域配置 |

---

## Step 5 — 自定义扩展检查（产物 E）

### 5.1 自定义扩展（`customized-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| U1.1 | ⚠️ | **P1** — Controller 入参 `name` 未使用 `@Valid` 校验注解（`HelloWorldController.java:36`）。当前仅 String 入参且无 JSR-303 注解，实际约束在 Service 层实现，影响可控。若团队要求 Controller 层统一校验，建议补充。 |
| U2.1–U2.3 | N/A | 未启用业务红线规则 |

---

## 终检（防漏检）

- [x] 执行队列中每个文件 `Step2`、`Step3`、**S1–S10 / G1–G17** 各列均非 `⬜`
- [x] Step 2 的每个 REQ/Scenario 均非 `⬜`
- [x] Step 3 的 A1–A7 均非 `⬜`
- [x] Step 4 全部 **G/S** 与 **B001–B081 / M001–M027 / I001–I010** ID 均非 `⬜`
- [x] Step 5 全部 U* ID 均非 `⬜`
- [x] 所有 `❌/⚠️` 已写入 report，且包含 `ID + path:line`