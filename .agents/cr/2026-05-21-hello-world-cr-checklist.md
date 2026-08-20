# Code Review Checklist
> **Change** hello-world · **分支/Commit** AI/task-DEV-66a2f4f2-84c9-11f1-9849-d5c90ba1aaae-58e56ca5-cb13-4663-b225-49411a66ddb9 · **日期** 2026-05-21
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。

---

## 执行队列（Step 1 — 产物 A）

| # | 文件 | 归属原因 | 状态 |
|---|------|----------|------|
| 1 | `src/main/java/com/example/HelloWorld.java` | 主入口类 | ✅ 已审 |
| 2 | `src/main/java/com/example/service/HelloWorldService.java` | 服务接口 | ✅ 已审 |
| 3 | `src/main/java/com/example/service/impl/HelloWorldServiceImpl.java` | 服务实现 | ✅ 已审 |
| 4 | `src/test/java/com/example/service/impl/HelloWorldServiceImplTest.java` | 单元测试 | ⚠️ 已审有问题 |

---

## Step 2：功能性检查（产物 B）

> **REQ 来源**：`docs/modules/hello-world/README.md`

| ID | 功能点 | Spec 证据 (README.md) | 关联文件 | 状态 |
|----|--------|----------------------|----------|------|
| REQ-1 | `getGreeting(String)` 返回 `"Hello, {name}!"` | L37: `name 为 null/空/空白时返回 "Hello, World!"，否则返回 "Hello, {name}!"` | HelloWorldServiceImpl.java | ✅ |
| REQ-2 | name 为 null 时返回默认问候语 `"Hello, World!"` | L37 | HelloWorldServiceImpl.java, Test | ✅ |
| REQ-3 | name 为空字符串时返回默认问候语 | L37 | HelloWorldServiceImpl.java, Test | ✅ |
| REQ-4 | name 为空白字符串时返回默认问候语 | L37 | HelloWorldServiceImpl.java, Test | ✅ |
| REQ-5 | 程序入口支持命令行参数 | L11: `含 main 方法，支持命令行参数` | HelloWorld.java | ✅ |
| REQ-6 | 无命令行参数时使用默认名称 | L37 (隐含) | HelloWorld.java | ✅ |
| REQ-7 | 接口-实现分离（Impl 后缀） | L44: `接口-实现分离 (Impl 后缀)` | HelloWorldService.java, HelloWorldServiceImpl.java | ✅ |

---

## Step 3：可读性检查（产物 C）

| ID | 规则 | 状态 |
|----|------|------|
| A1.1 | 文件名 = 顶层类名 + `.java` | ✅ |
| A1.2 | 编码 UTF-8 | ✅ |
| A1.3 | 空白仅允许 ASCII 空格和换行符，禁止 Tab | ✅ |
| A2.1 | 文件顺序：package → import → 一个顶层类，各部分间空行 | ✅ |
| A2.2 | 禁止 `import *` | ✅ |
| A2.3 | import 分两组：静态/非静态，组间空一行 | ✅ |
| A2.4 | import 按 ASCII 字典序排列 | ✅ |
| A2.5 | 重载方法连续放置 | N/A(无重载) |
| A3.1 | K&R 大括号 | ✅ |
| A3.2 | 多语句块空 catch 不可简写 | N/A(无 catch) |
| A3.3 | 缩进 4 空格，禁止 Tab | ✅ |
| A3.4 | 行宽 ≤ 120 字符 | ✅ |
| A3.5 | 换行规则 | N/A(无超长行) |
| A3.6 | 类成员之间必须空行 | ✅ |
| A3.7 | 关键字与 `(` 间加空格 | ✅ |
| A3.8 | 二元/三元运算符两侧加空格 | ✅ |
| A4.1 | 包名全小写 | ✅ |
| A4.2 | 类名 UpperCamelCase | ✅ |
| A4.3 | 方法名 lowerCamelCase | ⚠️ P2 — 测试方法含下划线: `HelloWorldServiceImplTest.java:32,44,57,70` |
| A4.4 | 常量 UPPER_SNAKE_CASE | ✅ |
| A4.5 | 字段/参数/局部变量 lowerCamelCase | ✅ |
| A4.6 | 泛型命名 | N/A(无泛型) |
| A4.7 | 测试类名 = 被测类名+Test | ✅ |
| A5.1 | 重写方法必须加 `@Override` | ✅ |
| A5.2 | catch 块不可为空 | N/A(无 catch) |
| A5.3 | 静态方法用类名调用 | N/A(无静态方法调用) |
| A5.4 | 禁止重写 `Object.finalize()` | ✅ |
| A6.1 | 数组方括号属于类型：`String[] args` | ✅ |
| A6.2 | switch fall-through 注释 + default | N/A(无 switch) |
| A6.3 | 修饰符顺序 | ✅ |
| A6.4 | 注解格式 | ✅ |
| A6.5 | long 字面量用大写 `L` | N/A(无 long 字面量) |
| A7.1 | public 类/成员必须有 Javadoc | ✅ |
| A7.2 | 块标记顺序：@param → @return → @throws → @deprecated | ✅ |
| A7.3 | 简单 getter / @Override 可省略 Javadoc | ✅ |
| A7.4 | 多段落用空行 + `<p>` 分隔 | ✅ |

---

## Step 4：可靠性检查（产物 D）

### 可靠性（G1–G17）

| ID | 规则 | 状态 |
|----|------|------|
| G1.1 | 先读后写无锁 | N/A(无并发写) |
| G1.2 | 加锁未二次校验 | N/A(无锁) |
| G1.3 | 乐观锁无限重试 | N/A(无乐观锁) |
| G1.4 | 多资源加锁顺序不一致 | N/A(无锁) |
| G2.1 | 写接口无幂等键 | N/A(无写接口) |
| G2.2 | 重试未防重复落库 | N/A(无持久化) |
| G2.3 | 幂等键与上游不一致 | N/A(无幂等) |
| G3.1 | 分布式事务当本地强一致 | N/A(无事务) |
| G3.2 | @Transactional 含外部 I/O | N/A(无事务) |
| G4.1–G4.3 | SQL 与索引 | N/A(无数据库) |
| G5.1 | MQ 消费未幂等 | N/A(无 MQ) |
| G6.1–G6.2 | 缓存 | N/A(无缓存) |
| G7.1–G7.2 | 调度任务 | N/A(无调度) |
| G8.1 | 仅处理 happy path，吞异常 | ✅ — `getGreeting` 对 null/空/空白均有防御 |
| G8.2 | 核心链路强依赖非核心 | N/A(无外部依赖) |
| G8.3 | I/O 流/连接/锁未释放 | ✅ — `System.out.println` 无资源泄漏风险 |
| G8.4 | 线程池未 shutdown | N/A(无线程池) |
| G8.5 | ThreadLocal 未 remove | N/A(无 ThreadLocal) |
| G8.6 | 使用无界队列线程池 | N/A(无线程池) |
| G9.1–G9.3 | 网络调用 | N/A(无网络调用) |
| G10.1–G10.2 | 接口契约 | ✅ — `getGreeting` 契约明确，null语义清晰 |
| G11.1 | 新逻辑无单测或无断言 | ✅ — 4 个测试用例，均有 `assertThat` |
| G11.2 | 未覆盖边界条件 | ✅ — 覆盖 null/空/空白/正常 |
| G11.3 | 入参空值无防御性校验 | ✅ — `name == null || name.isBlank()` |
| G11.4 | 金额用 float/double | N/A(无金额) |
| G12.1–G12.2 | 资损防控 | N/A(无资金操作) |
| G13.1 | 错误打 info/成功打 error | N/A(无日志) |
| G14.1–G14.4 | 国际化/多租户/时区 | N/A(无国际化场景) |
| G15.1–G15.3 | 可灰度 | N/A(无持久化/接口) |
| G16.1–G16.4 | 可监控 | N/A(无监控埋点) |
| G17.1–G17.3 | 可应急 | N/A(无应急场景) |

### 安全（S1–S10）

| ID | 规则 | 状态 |
|----|------|------|
| S1.1–S1.3 | SQL 注入 | N/A(无数据库) |
| S2.1–S2.3 | XSS | N/A(无 Web 输出) |
| S3.1–S3.3 | SSRF | N/A(无网络请求) |
| S4.1–S4.2 | 命令执行 | N/A(无命令执行) |
| S5.1–S5.2 | XXE | N/A(无 XML) |
| S6.1–S6.3 | 反序列化 | N/A(无序列化) |
| S7.1–S7.3 | 文件上传/下载 | N/A(无文件操作) |
| S8.1–S8.4 | 访问控制 | N/A(无 Web 接口) |
| S9.1–S9.4 | 数据安全 | ✅ — 无密钥、无敏感日志、无硬编码凭证 |
| S10.1–S10.3 | CSRF/CORS/跳转 | N/A(无 Web 接口) |

### Bug 模式（B/M/I）— 扫描结果

> `scan-all-rules.sh` 输出：**No findings. 52/222 rules scanned.**

---

## Step 5：自定义扩展检查（产物 E）

| ID | 规则 | 状态 |
|----|------|------|
| U1.1 | Controller 入参使用 `@Valid` | N/A(无 Controller) |
| U2.x | 业务红线 | N/A(未启用自定义规则) |