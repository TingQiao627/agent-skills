# Code Review Checklist

> **Change** `hello-world` · **分支/Commit** `AI/task-DEV-...` / `d4a71fa` · **日期** `2025-07-10`
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。
> **完成标准**：所有核销项必须从 `⬜` 变为其他状态；`N/A` 需写原因。

---

## Step 1 — 执行队列（产物 A）

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8 | G9 | G10 | G11 | G12 | G13 | G14 | G15 | G16 | G17 | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10 | 总状态 |
|---|----------------------|----------|-------|-------|----|----|----|----|----|----|----|----|----|-----|----|----|----|----|----|----|----|----|----|-----|-----|-----|-----|-----|-----|-----|-----|--------|
| 1 | `src/main/java/com/example/helloworld/HelloWorldApplication.java` | REQ-1 入口 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 2 | `src/main/java/com/example/helloworld/service/HelloWorldService.java` | REQ-1 接口 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 3 | `src/main/java/com/example/helloworld/service/impl/HelloWorldServiceImpl.java` | REQ-1 实现 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 4 | `src/test/java/com/example/helloworld/service/HelloWorldServiceTest.java` | REQ-2 测试 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 5 | `pom.xml` | 项目配置 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过(非Java) |

---

## Step 2 — 功能（产物 B）

> 需求来源：用户需求 "帮我写个hello world"，无独立 spec 文档，以需求描述本身为 spec 证据。

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据（文件/测试/接口） |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-1 | Given 程序启动, When 执行main方法, Then 控制台输出 "Hello, World!" | 需求："帮我写个hello world" | HelloWorldApplication.java:20-21, HelloWorldService.java:16, HelloWorldServiceImpl.java:22-23 | ✅ | `HelloWorldServiceImpl.java:14` DEFAULT_GREETING="Hello, World!"; `HelloWorldApplication.java:21` System.out.println输出 |
| REQ-2 | Given 调用getMessage(), When 服务实现返回消息, Then 消息非空且内容为"Hello, World!" | 需求隐含：可运行的完整代码 | HelloWorldServiceTest.java:25-33, 41-46 | ✅ | 单测覆盖正常路径(should_returnHelloWorldMessage) + 边界条件(should_returnNonEmptyMessage) |

---

## Step 3 — 可读性检查（产物 C）

> 对照 `references/readability-checklist.md` A1–A7 逐节核销。自动化预扫无命中。

| ID | 检查项 | 状态 | 备注（命中写 `path:line`） |
|----|--------|------|----------------------------|
| A1 | 源文件格式 | ✅ | 所有 Java 文件名与顶层类名一致，UTF-8编码 |
| A2 | 源文件结构/import 顺序 | ✅ | 无 `import *`；import 分组正确；无静态 import 组（仅 HelloWorldServiceTest 有静态 import Assertions） |
| A3 | 代码样式 | ✅ | K&R 大括号、4空格缩进、行宽 ≤120、运算符空格均符合 |
| A4 | 命名规范 | ✅ | 包名全小写、类名 UpperCamelCase、方法名 lowerCamelCase、常量 UPPER_SNAKE_CASE(`DEFAULT_GREETING`)、测试类 `HelloWorldServiceTest` |
| A5 | 编码实践 | ✅ | `@Override` 正确使用（HelloWorldServiceImpl:22） |
| A6 | 特定元素样式 | ✅ | `String[] args` 数组括号在类型侧；无 switch/注解/long 字面量 |
| A7 | Javadoc 规范 | ✅ | public 类和方法均有 Javadoc；getMessage() 为简单 getter 但有文档 |

---

## Step 4 — 可靠性检查（产物 D）

> 自动化预扫：`scan-all-rules.sh` 对 `src/main/java/com/example/helloworld/` 和 `src/test/java/com/example/helloworld/` 扫描 **52/222** 条规则，**无命中**。

### 4.1 Bug 模式（`bug-pattern-checklist.md`）

> 120 条规则按 ID 逐条核销。本次变更代码极简（无 DB/线程池/锁/集合操作/日期/浮点/MQ/反射），绝大部分规则 N/A。

| ID | 状态 | 备注 |
|----|------|------|
| B001 | N/A | 无 parse/of 字面量调用 |
| B002 | N/A | 无数组比较 |
| B003 | N/A | 无 Arrays.fill |
| B004 | N/A | 无数组 toString |
| B005 | N/A | 无 Arrays.asList |
| B006 | N/A | 无 assertEquals 参数顺序问题（测试中 expected 在前） |
| B007 | N/A | 无 catch Throwable |
| B008 | N/A | 无 Executors 线程池 |
| B009 | N/A | 无移位操作 |
| B010 | N/A | 无 BigDecimal |
| B011 | N/A | 无包装类型 == 比较 |
| B012 | N/A | 无 Calendar |
| B013 | N/A | 无 Calendar |
| B014 | N/A | 无集合查询 |
| B015 | N/A | 无 Collection.toArray |
| B016 | N/A | 无 Comparable |
| B017 | N/A | 无 this==null |
| B018 | N/A | 无三目运算符数值分支 |
| B019 | N/A | 无 Money 类 |
| B020 | N/A | 无常量乘法 |
| B021 | N/A | 无 Jedis |
| B022 | N/A | 无 SimpleDateFormat |
| B023 | N/A | 无 DeadException |
| B024 | N/A | 无 DeadThread |
| B025 | N/A | 无双括号初始化 |
| B026 | N/A | 无 equals(null) |
| B027 | N/A | 无 equals 方法 |
| B028 | N/A | 无 DateUtil |
| B029 | N/A | 无 setter |
| B030 | N/A | 无浮点比较 |
| B031 | N/A | 无 String.format |
| B032 | N/A | 无注解 getClass |
| B033 | N/A | 无 Unsafe |
| B034 | N/A | 无 Hashtable |
| B035 | N/A | 无 IdentityBinaryExpression |
| B036 | N/A | 无 IdentityHashMap |
| B037 | N/A | 无可变参数条件表达式 |
| B038 | N/A | 无递归调用 |
| B039 | N/A | 无 IndexOfChar |
| B040 | N/A | 无 isInstance |
| B041 | N/A | 无 JDBC |
| B042 | N/A | JUnit 5，非 JUnit 3 |
| B043 | N/A | 无内部类 @Test |
| B044 | N/A | JUnit 5，非 JUnit 3/4 混用 |
| B045 | N/A | 无包装类型加锁 |
| B046 | N/A | 无循环 |
| B047 | N/A | 无数值 compare |
| B048 | N/A | 无 Math.round |
| B049 | N/A | 无日期格式 |
| B050 | N/A | 无日期格式 |
| B051 | N/A | 无 Boolean.getBoolean |
| B052 | N/A | 无日期格式 |
| B053 | N/A | 无 try-fail 模式 |
| B054 | N/A | 无 EqualsTester |
| B055 | N/A | 无 Mockito |
| B056 | N/A | 无 Arrays.asList 修改 |
| B057 | N/A | 无增强 for 修改集合 |
| B058 | N/A | 无集合自引用 |
| B059 | N/A | 无 nCopies |
| B060 | N/A | 无 NullTernary |
| B061 | N/A | 无 BASE64 |
| B062 | N/A | 无 ClassLoader 强转 |
| B063 | N/A | 无 javax.xml |
| B064 | N/A | 无 Optional == |
| B065 | N/A | 无 setter |
| B066 | N/A | 无 Math.random |
| B067 | N/A | 无 Random |
| B068 | N/A | 无自赋值 |
| B069 | N/A | 无 compareTo |
| B070 | N/A | 无 equals |
| B071 | N/A | 无 size()>=0 |
| B072 | N/A | 无 Stream.toString |
| B073 | N/A | 无 StringBuilder(char) |
| B074 | N/A | 无 substring(0) |
| B075 | N/A | 无 for 循环 |
| B076 | N/A | 无 @Transactional |
| B077 | N/A | 无 catch Throwable |
| B078 | N/A | 无 assertThat(x).isEqualTo(x) — 测试用 `isEqualTo(expected)` 比较不同引用 |
| B079 | N/A | 无 @Mock |
| B080 | ✅ | 测试方法含断言：`assertThat(actual).isEqualTo(expected)` + `assertThat(message).isNotNull().isNotBlank()` |
| B081 | N/A | 无集合原地修改 |
| M001 | N/A | 无 if-else 重复条件 |
| M002 | N/A | 无 instanceof |
| M003 | N/A | 无包装类构造器 |
| M004 | N/A | 无 printStackTrace |
| M005 | N/A | 无内部类 |
| M006 | N/A | 无编译期常量布尔表达式 |
| M007 | N/A | 无 catch 块 |
| M008 | N/A | 无 equals/hashCode |
| M009 | N/A | 无 equals 跨类型 |
| M010 | N/A | 无位运算 |
| M011 | N/A | 无 switch |
| M012 | N/A | 无 finally |
| M013 | N/A | 无浮点强转 |
| M014 | N/A | 无枚举 |
| M015 | N/A | 无继承 |
| M016 | N/A | 无 java.time 默认时区 |
| M017 | N/A | 测试方法均有 @Test |
| M018 | N/A | 无 Lock |
| M019 | N/A | 无枚举 switch |
| M020 | ✅ | `HelloWorldServiceImpl.java:22` 有 `@Override` |
| M021 | N/A | 无 equals |
| M022 | N/A | 无 Optional |
| M023 | N/A | 无 Object.toString |
| M024 | N/A | 无 Optional |
| M025 | N/A | 无 final 类 protected |
| M026 | N/A | 无 @Mock |
| M027 | N/A | 无 ThreadLocal |
| I001 | N/A | 无异常断言 |
| I002 | N/A | 无 @DoNotMock |
| I003 | N/A | 无 @AutoValue |
| I004 | N/A | 无 java.util.Date |
| I005 | N/A | JUnit 5 |
| I006 | N/A | JUnit 5，无 setUp |
| I007 | N/A | JUnit 5，无 tearDown |
| I008 | N/A | 无 dataProvider |
| I009 | N/A | 统计用 |
| I010 | N/A | 无 Spring 容器 |

### 4.2 可靠性（`reliability-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| G1.1 | N/A | 无事务/并发场景 |
| G1.2 | N/A | 无锁操作 |
| G1.3 | N/A | 无乐观锁 |
| G1.4 | N/A | 无多资源加锁 |
| G2.1 | N/A | 无写接口/消息消费 |
| G2.2 | N/A | 无重试/定时任务/MQ |
| G2.3 | N/A | 无幂等键 |
| G3.1 | N/A | 无分布式事务 |
| G3.2 | N/A | 无 @Transactional |
| G4.1 | N/A | 无 SQL |
| G4.2 | N/A | 无 SQL |
| G4.3 | N/A | 无 SQL |
| G5.1 | N/A | 无 MQ |
| G6.1 | N/A | 无缓存 |
| G6.2 | N/A | 无缓存 |
| G7.1 | N/A | 无调度任务 |
| G7.2 | N/A | 无调度任务 |
| G8.1 | N/A | 无异常处理路径 |
| G8.2 | N/A | 无核心链路依赖 |
| G8.3 | N/A | 无 I/O 流/连接/锁 |
| G8.4 | N/A | 无线程池 |
| G8.5 | N/A | 无 ThreadLocal |
| G8.6 | N/A | 无线程池 |
| G9.1 | N/A | 无外部调用 |
| G9.2 | N/A | 无外部调用 |
| G9.3 | N/A | 无重试 |
| G10.1 | N/A | 无接口字段 |
| G10.2 | N/A | 无接口契约变更 |
| G11.1 | ✅ | 测试类含 2 个测试方法，均有断言 |
| G11.2 | ✅ | 覆盖正常路径 + 边界条件（null/blank） |
| G11.3 | N/A | getMessage() 无入参，无需校验 |
| G11.4 | N/A | 无数值运算 |
| G12.1 | N/A | 无资金场景 |
| G12.2 | N/A | 无资金场景 |
| G13.1 | N/A | 无日志输出 |
| G14.1 | N/A | 无金额 |
| G14.2 | N/A | 无多租户 |
| G14.3 | N/A | 无时区 |
| G14.4 | N/A | 无日期格式化 |
| G15.1 | N/A | 无 DB 表变更 |
| G15.2 | N/A | 无接口共存 |
| G15.3 | N/A | 无开关逻辑 |
| G16.1 | N/A | 非核心链路（Hello World 示例） |
| G16.2 | ⚠️ | `HelloWorldApplication.java:21` — 仅用 `System.out.println` 输出，无日志框架，生产环境排查困难。**P2(参考)** |
| G16.3 | N/A | 无日志输出 |
| G16.4 | N/A | 无 catch 块 |
| G17.1 | N/A | 无功能开关 |
| G17.2 | N/A | 无降级场景 |
| G17.3 | N/A | 无数据变更 |

### 4.3 安全（`security-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| S1.1 | N/A | 无 SQL |
| S1.2 | N/A | 无动态 SQL |
| S1.3 | N/A | 无 SQL |
| S2.1 | N/A | 无 Web 输出 |
| S2.2 | N/A | 无富文本 |
| S2.3 | N/A | 无模板引擎 |
| S3.1 | N/A | 无外部 URL 请求 |
| S3.2 | N/A | 无 HTTP 重定向 |
| S3.3 | N/A | 无外部请求 |
| S4.1 | N/A | 无系统命令 |
| S4.2 | N/A | 无文件操作 |
| S5.1 | N/A | 无 XML 解析 |
| S5.2 | N/A | 无 XPath |
| S6.1 | N/A | 无反序列化 |
| S6.2 | N/A | 无 JSON 多态反序列化 |
| S6.3 | N/A | 无 transient 字段 |
| S7.1 | N/A | 无文件上传 |
| S7.2 | N/A | 无文件路径 |
| S7.3 | N/A | 无文件存储 |
| S8.1 | N/A | 无 Web 接口 |
| S8.2 | N/A | 无 HTTP 接口 |
| S8.3 | N/A | 无数据 ID |
| S8.4 | N/A | 无 Cookie |
| S9.1 | N/A | 无密钥/凭证 |
| S9.2 | N/A | 无日志 |
| S9.3 | N/A | 无传输/存储加密 |
| S9.4 | N/A | 无随机数 |
| S10.1 | N/A | 无 CSRF 场景 |
| S10.2 | N/A | 无 CORS |
| S10.3 | N/A | 无 URL 跳转 |

---

## Step 5 — 自定义扩展检查（产物 E）

### 5.1 自定义扩展（`customized-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| U1.1 | N/A | 无 Controller，无 @Valid 场景 |

> 自定义清单仅含 1 条示例规则（U1.1），U2 节为空。**整节 N/A(未启用自定义规则)**。

---

## 终检（防漏检）

- [x] 执行队列中每个文件 `Step2`、`Step3`、**S1–S10 / G1–G17** 各列均非 `⬜`（跳过文件除外）
- [x] Step 2 的每个 REQ/Scenario 均非 `⬜`
- [x] Step 3 的 A1–A7 均非 `⬜`
- [x] Step 4 全部 **G/S** 与 **B001–B081 / M001–M027 / I001–I010** ID 均非 `⬜`
- [x] Step 5 全部 U* ID 均非 `⬜`
- [x] 所有 `❌/⚠️` 已写入 report，且包含 `ID + path:line`