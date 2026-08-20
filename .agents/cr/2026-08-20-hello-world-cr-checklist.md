# Code Review Checklist

> **Change** hello-world · **分支/Commit** `AI/task-DEV-66a2f4f2` / `3d77172` · **日期** `2026-08-20`
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。
> **完成标准**：所有核销项必须从 `⬜` 变为其他状态；`N/A` 需写原因。
>
> **执行顺序（强制）**：写入本清单并进入逐文件审查前，先在目标仓库对变更路径运行 `references/script/scan-all-rules.sh`，将输出贴入 Step 3 和 Step 4 备注；再用 LLM 完成 Step 2–5 中脚本未覆盖项及复核。

---

## Step 1 — 执行队列（产物 A）

> **Step4 列语义**：每个 **Sn / Gn** 表示「**本文件**在 Step4 审查中，对 `reliability-checklist.md` 第 **G*n*** 节、`security-checklist.md` 第 **S*n*** 节的扫描结论」。**Bug 模式（B/M/I）** 不在本表分列，在下方 **§4.1** 按清单 ID 核销（可与 `scan-all-rules.sh` 预扫结果对照）。与变更无关填 `N/A`；已扫无命中填 `✅`；命中风险填 `⚠️` 或 `❌`（并在 Step 4 明细表与 report 中写清 `Gx.x` / `Sx.x` + `path:line`）。

**列说明（与 references 章节对齐）**

| 列组 | 列名 | 对应清单章节 |
|------|------|----------------|
| 可靠性 | **G1** … **G17**（+ **G18** 仅明细表） | `reliability-checklist.md` — G1 并发 … G17 可应急；**G18** 安全补强在 Step 4.2 逐条核销，Step 1 可不单列 |
| 安全 | **S1** … **S10** | `security-checklist.md` — S1 SQL 注入 … S10 CSRF/CORS/跳转 |

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8 | G9 | G10 | G11 | G12 | G13 | G14 | G15 | G16 | G17 | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10 | 总状态 |
|---|----------------------|----------|-------|-------|----|----|----|----|----|----|----|----|----|-----|-----|-----|-----|-----|-----|-----|-----|----|----|----|----|----|----|----|----|----|-----|--------|
| 1 | `src/main/java/com/example/HelloWorld.java` | REQ-1~6 / design | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ |
| 2 | `src/test/java/com/example/HelloWorldTest.java` | REQ-1~6 / 测试覆盖 | ✅ | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ |

- 由 `git diff --name-only …` 等展开；**禁止 glob**；非 Java 标 `跳过`（跳过文件的 Step4 各列可统一 `跳过` 或 `N/A(非 Java)`）。
- **守卫**：无 `.java` → 按技能终止。
- **收口**：每文件各 **Sn/Gn** 列均非 `⬜` 后，再与下方 Step 4 **逐条 ID 表** 核对一致；若某大类整节与当前文件无关，该列可一次性标 `N/A(无 SQL/无 MQ/…)`，但须在 Step 4 明细对应 ID 行同样标 `N/A` 并写原因。

---

## Step 2 — 功能（产物 B）

> 仅从 spec/tasks 提 **REQ**，勿臆造。不符 spec 标 **P0**。
> 每个 REQ 都必须填写 **spec 证据** 与 **关联文件**；若命中 P0，代码证据需落到 `path:line`、测试或接口行为。

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据（文件/测试/接口） |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-1 | 提供标准 Hello World 问候消息输出能力 | `docs/modules/hello-world/README.md` §模块职责 | `HelloWorld.java` | ✅ | `HelloWorld.java:41-43` — `getMessage()` 返回默认消息 |
| REQ-2 | 默认消息为 "Hello, World!" | `docs/modules/hello-world/README.md` §API接口 — `getMessage()` | `HelloWorld.java` | ✅ | `HelloWorld.java:18` — `DEFAULT_NAME = "World"`; `HelloWorldTest.java:25` — `assertEquals("Hello, World!", message)` |
| REQ-3 | 支持自定义名称消息 "Hello, {name}!" | `docs/modules/hello-world/README.md` §API接口 — `getMessage(String name)` | `HelloWorld.java` | ✅ | `HelloWorld.java:52-57` — `getMessage(String name)`; `HelloWorldTest.java:39` — `assertEquals("Hello, DTCoder!", message)` |
| REQ-4 | name 为 null 或空白时抛出 IllegalArgumentException | `docs/modules/hello-world/README.md` §API接口 — 异常 | `HelloWorld.java` | ✅ | `HelloWorld.java:53-54` — `if (name == null || name.isBlank())`; `HelloWorldTest.java:49` — `assertThrows(IllegalArgumentException.class, ...)` |
| REQ-5 | 所有公共方法必须有 Javadoc 注释 | `docs/ARCHITECTURE.md` §约束 | `HelloWorld.java` | ✅ | `HelloWorld.java:22-26` — `main` 有 Javadoc; `HelloWorld.java:36-40` — `getMessage()` 有 Javadoc; `HelloWorld.java:45-51` — `getMessage(String)` 有 Javadoc |
| REQ-6 | 程序入口 main() 输出问候消息到标准输出 | `docs/modules/hello-world/README.md` §API接口 — `main(String[] args)` | `HelloWorld.java` | ✅ | `HelloWorld.java:27-34` — `main` 方法含 `System.out.println` |

---

## Step 3 — 可读性检查（产物 C）

> 无 Java：**整节 N/A**。

对照 `references/readability-checklist.md` A1–A7 逐节核销：

| ID | 检查项 | 状态 | 备注（命中写 `path:line`） |
|----|--------|------|----------------------------|
| A1 | 源文件格式 | ✅ | 文件名=类名+`.java`，UTF-8，无Tab |
| A2 | 源文件结构/import 顺序 | ⚠️ | **预扫命中**: `HelloWorldTest.java:6` — `import static org.junit.jupiter.api.Assertions.*;` 违反 A2.2 禁止通配符 import（P2）。`HelloWorld.java` 无 import 语句，✅ |
| A3 | 代码样式 | ✅ | K&R 大括号 ✅；4空格缩进 ✅；行宽≤120 ✅；运算符空格 ✅ |
| A4 | 命名规范 | ✅ | 包名全小写 ✅；类名 UpperCamelCase ✅；方法名 lowerCamelCase ✅；常量 UPPER_SNAKE_CASE ✅；测试类名 `HelloWorldTest` ✅ |
| A5 | 编码实践 | ✅ | 无 `@Override` 场景；无空 catch 块；静态方法调用正常 |
| A6 | 特定元素样式 | ✅ | `String[] args` ✅；无 switch 语句；修饰符顺序正确；无 long 字面量 |
| A7 | Javadoc 规范 | ✅ | 公共类有 Javadoc ✅；公共方法有 Javadoc ✅；`@param`→`@return`→`@throws` 顺序正确 ✅ |

---

## Step 4 — 可靠性检查（产物 D）

> **逐条核销（强制）**：G/S 每个 ID **独占一行**，禁止合并为区间（例如 ~~`G1.1 ~ G14.3`~~）。**Bug 模式** 按 `bug-pattern-checklist.md` 中 **每条 B*/M*/I*** 独占一行核销（120 条）**；无关变更可对该 ID 标 `N/A` 并写原因。报告等级：**Blocker→P0、Major→P1、Info→P2**。

### 4.1 Bug 模式（`bug-pattern-checklist.md`）

> 可先运行 `references/script/scan-all-rules.sh`（对变更目录）将命中写入备注，再人工/LLM 补全脚本未覆盖规则。

| ID | 状态 | 备注（命中写 `path:line`；预扫可粘贴脚本摘要） |
|----|------|--------------------------------------------------|
| B001 | N/A | 无 `parse`/`of` 字面量调用 |
| B002 | N/A | 无数组 `equals` 比较 |
| B003 | N/A | 无 `Arrays.fill` |
| B004 | N/A | 无数组 `toString()` |
| B005 | N/A | 无 `Arrays.asList` 基本类型 |
| B006 | ✅ | `HelloWorldTest.java:25,39` — `assertEquals(expected, actual)` 参数顺序正确 |
| B007 | N/A | 无 `catch (Throwable)` |
| B008 | N/A | 无 `Executors` 线程池创建 |
| B009 | N/A | 无移位操作 |
| B010 | N/A | 无 `BigDecimal` 构造 |
| B011 | N/A | 无包装类型 `==` 比较 |
| B012 | N/A | 无 `Calendar` 操作 |
| B013 | N/A | 无 `Calendar` 操作 |
| B014 | N/A | 无集合泛型查询 |
| B015 | N/A | 无 `Collection.toArray` |
| B016 | N/A | 无 `Comparable` 实现 |
| B017 | N/A | 无 `this == null` 判断 |
| B018 | N/A | 无三目运算符类型混用 |
| B019 | N/A | 无 Money 类操作 |
| B020 | N/A | 无编译期常量乘法 |
| B021 | N/A | 无 Jedis 操作 |
| B022 | N/A | 无 `SimpleDateFormat` |
| B023 | N/A | 无异常创建未抛出 |
| B024 | N/A | 无 `Thread` 创建 |
| B025 | N/A | 无双括号初始化 |
| B026 | N/A | 无 `equals(null)` |
| B027 | N/A | 无 `equals` 实现 |
| B028 | N/A | 无 `DateUtil` |
| B029 | N/A | 无 setter 方法 |
| B030 | N/A | 无浮点数 `==` 比较 |
| B031 | N/A | 无 `String.format` |
| B032 | N/A | 无注解 `getClass()` |
| B033 | N/A | 无 Unsafe 操作 |
| B034 | N/A | 无 `Hashtable` |
| B035 | N/A | 无二元运算自比较 |
| B036 | N/A | 无 `IdentityHashMap` |
| B037 | N/A | 无可变参数条件表达式 |
| B038 | N/A | 无递归调用 |
| B039 | N/A | 无 `String.indexOf` |
| B040 | N/A | 无 `Class.isInstance` |
| B041 | N/A | 无 JDBC 连接 |
| B042 | N/A | 非 JUnit3 |
| B043 | N/A | 无内部类 `@Test` |
| B044 | N/A | 非 JUnit3+JUnit4 混用 |
| B045 | N/A | 无包装类型加锁 |
| B046 | N/A | 无循环条件 |
| B047 | N/A | 无数值 compare |
| B048 | N/A | 无 `Math.round` |
| B049 | N/A | 无日期格式 |
| B050 | N/A | 无日期格式 |
| B051 | N/A | 无 `Boolean.getBoolean` |
| B052 | N/A | 无日期格式 |
| B053 | ✅ | `HelloWorldTest.java:49,59` — 使用 `assertThrows` 而非 try-catch，无需 `fail()` |
| B054 | N/A | 无 `EqualsTester` |
| B055 | N/A | 无 Mockito |
| B056 | N/A | 无 `Arrays.asList` |
| B057 | N/A | 无增强 for 循环修改集合 |
| B058 | N/A | 无集合自身参数 |
| B059 | N/A | 无 `Collections.nCopies` |
| B060 | N/A | 无三目运算符拆箱 |
| B061 | N/A | 无 `BASE64Encoder` |
| B062 | N/A | 无 `ClassLoader` 强转 |
| B063 | N/A | 无 `javax.xml` 包 |
| B064 | N/A | 无 `Optional` |
| B065 | N/A | 无 Pojo 自赋值 |
| B066 | N/A | 无 `Math.random()` |
| B067 | N/A | 无 `Random.nextInt()` |
| B068 | N/A | 无变量自赋值 |
| B069 | N/A | 无 `compareTo` |
| B070 | N/A | 无 `equals` 自比较 |
| B071 | N/A | 无 `size() >= 0` |
| B072 | N/A | 无 `Stream.toString()` |
| B073 | N/A | 无 `StringBuilder` char 构造 |
| B074 | N/A | 无 `substring(0)` |
| B075 | N/A | 无 for 循环 |
| B076 | N/A | 无 `@Transactional` |
| B077 | N/A | `HelloWorldTest.java` 无 `catch (Throwable)` |
| B078 | N/A | 无 Truth 断言 |
| B079 | N/A | 无 `@Mock` |
| B080 | ✅ | `HelloWorldTest.java:25,39,49,59` — 所有测试方法均含断言 (`assertEquals`/`assertThrows`) |
| B081 | N/A | 无集合原地修改 |
| M001 | N/A | 无连续相同条件判断 |
| M002 | N/A | 无 `instanceof` |
| M003 | N/A | 无包装类构造器 |
| M004 | N/A | 无 `printStackTrace()` |
| M005 | N/A | `HelloWorldTest` 是顶层类，非内部类 |
| M006 | N/A | 无编译期常量布尔表达式 |
| M007 | N/A | 无 catch 块 |
| M008 | N/A | 无 `equals`/`hashCode` 重写 |
| M009 | N/A | 无类型不兼容 `equals` |
| M010 | N/A | 无位运算 |
| M011 | N/A | 无 switch |
| M012 | N/A | 无 finally 块 |
| M013 | N/A | 无类型转换 |
| M014 | N/A | 无枚举 `getClass()` |
| M015 | N/A | 无继承 |
| M016 | N/A | 无时间 API |
| M017 | ✅ | `HelloWorldTest.java` 所有测试方法均含 `@Test` 注解 |
| M018 | N/A | 无显式锁 |
| M019 | N/A | 无 switch 枚举 |
| M020 | N/A | 无重写方法 |
| M021 | N/A | 无 `equals` 重写 |
| M022 | N/A | 无 `Optional` |
| M023 | N/A | 无 `toString()` 调用 |
| M024 | N/A | 无 `Optional` |
| M025 | N/A | 无 final 类 |
| M026 | N/A | 无 `@Mock` |
| M027 | N/A | 无 `ThreadLocal` |
| I001 | N/A | 测试使用 `assertThrows` 仅断言异常类型，未断言异常消息。属 JUnit 5 最佳实践常见做法，不强制要求（P2 参考级） |
| I002 | N/A | 无 `@DoNotMock` |
| I003 | N/A | 无 `@AutoValue` |
| I004 | N/A | 无 `java.util.Date` |
| I005 | N/A | 非 JUnit3 |
| I006 | N/A | 无 `setUp()` |
| I007 | N/A | 无 `tearDown()` |
| I008 | N/A | 无 `dataProvider` |
| I009 | N/A | 统计用途，非检查项 |
| I010 | N/A | 无 Spring/Pandora 容器 |

### 4.2 可靠性（`reliability-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| G1.1 | N/A | 无事务/并发场景，无数据库操作 |
| G1.2 | N/A | 无加锁场景 |
| G1.3 | N/A | 无乐观锁场景 |
| G1.4 | N/A | 无多资源加锁 |
| G2.1 | N/A | 无写接口/消息消费 |
| G2.2 | N/A | 无重试/定时任务/MQ |
| G2.3 | N/A | 无幂等键 |
| G3.1 | N/A | 无分布式事务 |
| G3.2 | N/A | 无 `@Transactional` |
| G4.1 | N/A | 无 SQL 操作 |
| G4.2 | N/A | 无 SQL 操作 |
| G4.3 | N/A | 无 SQL 操作 |
| G5.1 | N/A | 无 MQ 消费 |
| G6.1 | N/A | 无缓存 |
| G6.2 | N/A | 无缓存 |
| G7.1 | N/A | 无调度任务 |
| G7.2 | N/A | 无调度任务 |
| G8.1 | N/A | 无 catch 块，无异常吞没风险 |
| G8.2 | N/A | 无外部依赖 |
| G8.3 | N/A | 无 I/O 流/连接/锁 |
| G8.4 | N/A | 无线程池 |
| G8.5 | N/A | 无 ThreadLocal |
| G8.6 | N/A | 无线程池 |
| G9.1 | N/A | 无外部调用（HTTP/RPC/DB/Redis） |
| G9.2 | N/A | 无外部调用 |
| G9.3 | N/A | 无重试逻辑 |
| G10.1 | N/A | 简单字符串返回值，无字段歧义 |
| G10.2 | N/A | 无接口契约变更 |
| G11.1 | ✅ | 新逻辑有单测覆盖：`HelloWorldTest.java` 4 个测试方法均有断言 |
| G11.2 | ✅ | 边界覆盖：空值 `null`（行49）、空白字符串 `"   "`（行59）、有效值 `"DTCoder"`（行33）、默认值 ✅ |
| G11.3 | ✅ | `HelloWorld.java:53` — `name == null \|\| name.isBlank()` 防御性校验 |
| G11.4 | N/A | 无数值运算，无金额操作 |
| G12.1 | N/A | 无资金相关场景 |
| G12.2 | N/A | 无资金相关场景 |
| G13.1 | N/A | 无日志输出 |
| G14.1 | N/A | 无金额操作 |
| G14.2 | N/A | 无多租户 |
| G14.3 | N/A | 无时区操作 |
| G14.4 | N/A | 无时间格式化 |
| G15.1 | N/A | 无数据库变更 |
| G15.2 | N/A | 无接口共存 |
| G15.3 | N/A | 无开关逻辑 |
| G16.1 | N/A | 无核心链路（简单示例） |
| G16.2 | N/A | 无异常捕获路径 |
| G16.3 | N/A | 无日志输出 |
| G16.4 | N/A | 无 catch 块 |
| G17.1 | N/A | 无功能开关 |
| G17.2 | N/A | 无降级预案 |
| G17.3 | N/A | 无数据变更 |
| G18.1 | N/A | 安全补强 — 无相关场景 |
| G18.2 | N/A | 安全补强 — 无相关场景 |
| G18.3 | N/A | 安全补强 — 无相关场景 |

### 4.3 安全（`security-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| S1.1 | N/A | 无 SQL 操作 |
| S1.2 | N/A | 无 SQL 操作 |
| S1.3 | N/A | 无 SQL 操作 |
| S2.1 | N/A | 无 Web 输出 |
| S2.2 | N/A | 无富文本 |
| S2.3 | N/A | 无模板引擎 |
| S3.1 | N/A | 无外部 URL 请求 |
| S3.2 | N/A | 无外部 URL 请求 |
| S3.3 | N/A | 无外部 URL 请求 |
| S4.1 | N/A | 无命令执行 |
| S4.2 | N/A | 无命令执行 |
| S5.1 | N/A | 无 XML 解析 |
| S5.2 | N/A | 无 XPath |
| S6.1 | N/A | 无反序列化 |
| S6.2 | N/A | 无 JSON 反序列化 |
| S6.3 | N/A | 无敏感字段 |
| S7.1 | N/A | 无文件上传/下载 |
| S7.2 | N/A | 无文件上传/下载 |
| S7.3 | N/A | 无文件上传/下载 |
| S8.1 | N/A | 无 Web 接口 |
| S8.2 | N/A | 无 Web 接口 |
| S8.3 | N/A | 无数据 ID |
| S8.4 | N/A | 无 Cookie |
| S9.1 | N/A | 无密钥/凭证 |
| S9.2 | N/A | 无日志输出 |
| S9.3 | N/A | 无传输/存储 |
| S9.4 | N/A | 无随机数 |
| S10.1 | N/A | 无 Web 增删改 |
| S10.2 | N/A | 无 CORS |
| S10.3 | N/A | 无 URL 跳转 |

---

## Step 5 — 自定义扩展检查（产物 E）

> 按 `customized-checklist.md` 逐条核销；若未启用可整节写 `N/A(未启用自定义规则)`。

### 5.1 自定义扩展（`customized-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| U1.1 | N/A | 示例项 — Controller 入参校验注解，本项目无 Controller |
| U1.2 | N/A | 未启用自定义规则 |
| U1.3 | N/A | 未启用自定义规则 |
| U2.1 | N/A | 未启用自定义规则 |
| U2.2 | N/A | 未启用自定义规则 |
| U2.3 | N/A | 未启用自定义规则 |

---

## 终检（防漏检）

- [x] 执行队列中每个文件 `Step2`、`Step3`、**S1–S10 / G1–G17** 各列均非 `⬜`（跳过文件除外）；
- [x] Step 2 的每个 REQ/Scenario 均非 `⬜`
- [x] Step 3 的 A1–A7 均非 `⬜`
- [x] Step 4 全部 **G/S** 与 **B001–B081 / M001–M027 / I001–I010** ID 均非 `⬜`（允许 `N/A`，但有原因）
- [x] Step 5 全部 U* ID 均非 `⬜`（允许 `N/A(未启用自定义规则)`）
- [x] 所有 `❌/⚠️` 已写入 report，且包含 `ID + path:line`