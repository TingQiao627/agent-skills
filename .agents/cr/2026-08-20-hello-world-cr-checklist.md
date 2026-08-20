# Code Review Checklist

> **Change** hello-world · **分支/Commit** `AI/task-DEV-66a2f4f2-84c9-11f1-9849-d5c90ba1aaae-8bd0226b-1410-4a33-b159-40e45f283515` / `d492640` · **日期** `2026-08-20`
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。
> **完成标准**：所有核销项必须从 `⬜` 变为其他状态；`N/A` 需写原因。
>
> **自动化预扫结果**：`scan-all-rules.sh` 扫描 6 个 Java 文件，命中 1 条：`[P1] M016 — JavaTimeDefaultTimeZone: HelloWorldServiceImpl.java:33`

---

## Step 1 — 执行队列（产物 A）

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8 | G9 | G10 | G11 | G12 | G13 | G14 | G15 | G16 | G17 | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10 | 总状态 |
|---|----------------------|----------|-------|-------|----|----|----|----|----|----|----|----|----|-----|----|----|----|----|----|----|----|----|----|----|-----|-----|-----|-----|-----|-----|-----|-----|--------|
| 1 | `src/hello-world/pom.xml` | 构建配置 | N/A(非Java) | N/A(非Java) | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | ✅ 跳过 |
| 2 | `src/hello-world/.../HelloWorldApplication.java` | REQ-启动入口 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ | N/A | N/A | ✅ 已审 |
| 3 | `src/hello-world/.../controller/HelloWorldController.java` | REQ-API端点 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ | N/A | N/A | ✅ 已审 |
| 4 | `src/hello-world/.../model/vo/HelloWorldResponse.java` | REQ-响应模型 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 5 | `src/hello-world/.../service/HelloWorldService.java` | REQ-服务接口 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 6 | `src/hello-world/.../service/impl/HelloWorldServiceImpl.java` | REQ-服务实现 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | ✅ | ⚠️ | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ 已审有问题 |
| 7 | `src/hello-world/.../test/.../HelloWorldServiceImplTest.java` | REQ-单元测试 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |

---

## Step 2 — 功能（产物 B）

> 仅从 `.agents/hello-world/impl.md` 提取 REQ，勿臆造。

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据（文件/测试/接口） |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-1 | GET `/api/hello` 返回问候消息 | impl.md: "GET \| `/api/hello` \| `name` (可选, 默认 \"World\") \| 返回问候消息" | HelloWorldController.java | ✅ | `@GetMapping` + `@RequestMapping("/api/hello")` → `HelloWorldController.java:15-16,31-33` |
| REQ-2 | `name` 参数可选，默认 "World" | impl.md: "`name` (可选, 默认 \"World\")" | HelloWorldController.java | ✅ | `@RequestParam(defaultValue = "World") String name` → `HelloWorldController.java:32` |
| REQ-3 | 响应包含 message 和 timestamp | impl.md: "返回问候消息" + 响应模型 `HelloWorldResponse` | HelloWorldResponse.java, HelloWorldServiceImpl.java | ✅ | `message` + `timestamp` 字段 → `HelloWorldResponse.java:13,16`；构造 → `HelloWorldServiceImpl.java:32-33` |
| REQ-4 | name 为 null 时抛 IllegalArgumentException | impl.md: impl.md 中未显式声明，但接口契约 `HelloWorldService.java:17` 声明了 `@throws IllegalArgumentException` | HelloWorldService.java, HelloWorldServiceImpl.java | ✅ | `if (name == null) throw new IllegalArgumentException(...)` → `HelloWorldServiceImpl.java:23-24`；测试覆盖 → `HelloWorldServiceImplTest.java:70-75` |
| REQ-5 | 空白/空字符串返回默认问候 "Hello, World!" | impl.md 未显式声明，属于实现细节（边界处理） | HelloWorldServiceImpl.java | ✅ | `trimmedName.isEmpty() ? DEFAULT_NAME : trimmedName` → `HelloWorldServiceImpl.java:28`；测试覆盖 → `HelloWorldServiceImplTest.java:50-66` |
| REQ-6 | 测试覆盖 5 个场景 | impl.md: "测试方法数：5" / "覆盖场景：正常路径 ✓、边界条件 ✓、异常处理 ✓" | HelloWorldServiceImplTest.java | ✅ | 5 个 `@Test` 方法 → `HelloWorldServiceImplTest.java:29-75` |

---

## Step 3 — 可读性检查（产物 C）

> 对照 `references/readability-checklist.md` A1–A7 逐节核销：

| ID | 检查项 | 状态 | 备注（命中写 `path:line`） |
|----|--------|------|----------------------------|
| A1 | 源文件格式 | ✅ | 所有 Java 文件：文件名=顶层类名+.java，UTF-8，无 Tab |
| A2 | 源文件结构/import 顺序 | ✅ | 所有文件：package→import→class，无 `import *`，静态/非静态分组，字母序 |
| A3 | 代码样式 | ✅ | 所有文件：K&R 大括号，4 空格缩进，行宽≤120，关键词空格正确 |
| A4 | 命名规范 | ✅ | 包名全小写；类名 UpperCamelCase；方法/字段 lowerCamelCase；常量 UPPER_SNAKE_CASE；测试类 `HelloWorldServiceImplTest` |
| A5 | 编码实践 | ✅ | `@Override` 正确使用（`HelloWorldServiceImpl.java:21`）；无空 catch；无 finalize 重写 |
| A6 | 特定元素样式 | ✅ | 无 switch；无 long 字面量；修饰符顺序正确；数组类型写法正确 |
| A7 | Javadoc 规范 | ✅ | 所有 public 类/方法均有 Javadoc；getter/setter 自解释可省略；`@param/@return/@throws` 顺序正确（`HelloWorldService.java:13-18`） |

---

## Step 4 — 可靠性检查（产物 D）

> **逐条核销（强制）**：G/S 每个 ID **独占一行**。**Bug 模式** 按 `bug-pattern-checklist.md` 中每条核销。报告等级：**Blocker→P0、Major→P1、Info→P2**。

### 4.1 Bug 模式（`bug-pattern-checklist.md`）

> 自动化预扫命中：`[P1] M016 — JavaTimeDefaultTimeZone: HelloWorldServiceImpl.java:33`

| ID | 状态 | 备注（命中写 `path:line`；预扫可粘贴脚本摘要） |
|----|------|--------------------------------------------------|
| B001 | N/A | 无 `LocalDateTime.parse`/`UUID.fromString` 字面量调用 |
| B002 | N/A | 无数组 `equals` 调用 |
| B003 | N/A | 无 `Arrays.fill` 调用 |
| B004 | N/A | 无数组 `toString()` 调用 |
| B005 | N/A | 无 `Arrays.asList` 原始类型数组 |
| B006 | N/A | 使用 AssertJ `assertThat`，非 JUnit `assertEquals` |
| B007 | N/A | 无 `catch(Throwable)` 捕获 |
| B008 | N/A | 无 `Executors` 创建线程池 |
| B009 | N/A | 无移位运算 |
| B010 | N/A | 无 `new BigDecimal(double)` |
| B011 | N/A | 无包装类型 `==` 比较 |
| B012 | N/A | 无 `Calendar.add` 固定天数 |
| B013 | N/A | 无 `Calendar.HOUR` 使用 |
| B014 | N/A | 无集合查询类型不兼容 |
| B015 | N/A | 无 `Collection.toArray` |
| B016 | N/A | 无自定义 `Comparable` |
| B017 | N/A | 无 `this == null` 判断 |
| B018 | N/A | 无三目运算符数值类型混用 |
| B019 | N/A | 无 Money 类使用 |
| B020 | N/A | 无编译期常量乘法 |
| B021 | N/A | 无 Jedis 使用 |
| B022 | N/A | 无 `SimpleDateFormat` |
| B023 | N/A | 无创建异常未抛出 |
| B024 | N/A | 无 `Thread` 创建 |
| B025 | N/A | 无双括号初始化 |
| B026 | N/A | 无 `equals(null)` |
| B027 | N/A | 无自定义 `equals` |
| B028 | N/A | 无 `DateUtil.formatDate` |
| B029 | N/A | 无 setter 赋值错误 |
| B030 | N/A | 无浮点 `==` 比较 |
| B031 | N/A | 无 `String.format` |
| B032 | N/A | 无注解 `getClass()` |
| B033 | N/A | 无 Unsafe 使用 |
| B034 | N/A | 无 `Hashtable` |
| B035 | N/A | 无同一对象二元运算 |
| B036 | N/A | 无 `IdentityHashMap` |
| B037 | N/A | 无可变参数条件表达式 |
| B038 | N/A | 无递归调用 |
| B039 | N/A | 无 `indexOf` 参数颠倒 |
| B040 | N/A | 无 `isInstance` |
| B041 | N/A | 无 JDBC 连接 |
| B042 | N/A | 非 JUnit3 测试 |
| B043 | N/A | 无内部类 `@Test` |
| B044 | N/A | 非 JUnit3+JUnit4 混用 |
| B045 | N/A | 无包装类型加锁 |
| B046 | N/A | 无循环条件未更新 |
| B047 | N/A | 无 `Float.compare` 精度损失 |
| B048 | N/A | 无 `Math.round` 整型入参 |
| B049 | N/A | 无 `DD` 日期格式 |
| B050 | N/A | 无 `hh`/`HH` 格式 |
| B051 | N/A | 无 `Boolean.getBoolean` |
| B052 | N/A | 无 `YYYY` 格式 |
| B053 | N/A | 使用 `assertThrows` 正确替代 try-fail |
| B054 | N/A | 无 `EqualsTester` |
| B055 | N/A | 无 Mockito |
| B056 | N/A | 无 `Arrays.asList` 修改 |
| B057 | N/A | 无增强 for 循环修改集合 |
| B058 | N/A | 无集合自身操作 |
| B059 | N/A | 无 `Collections.nCopies` |
| B060 | N/A | 无三目运算符 null 拆箱 |
| B061 | N/A | 无 `sun.misc.BASE64Encoder` |
| B062 | N/A | 无 `URLClassLoader` 强转 |
| B063 | N/A | 无 `javax.xml.bind` |
| B064 | N/A | 无 `Optional` |
| B065 | N/A | `HelloWorldResponse.java` 无自赋值 |
| B066 | N/A | 无 `(int) Math.random()` |
| B067 | N/A | 无 `Random.nextInt() %` |
| B068 | N/A | 无自赋值 |
| B069 | N/A | 无 `compareTo` |
| B070 | N/A | 无 `equals` 自比较 |
| B071 | N/A | 无 `size() >= 0` |
| B072 | N/A | 无 `Stream.toString()` |
| B073 | N/A | 无 `StringBuilder(char)` |
| B074 | N/A | 无 `substring(0)` |
| B075 | N/A | 无 for 循环条件矛盾 |
| B076 | N/A | 无 `@Transactional` |
| B077 | N/A | 无 `catch(Throwable)` 在测试中 |
| B078 | N/A | 无 `assertThat(x).isEqualTo(x)` |
| B079 | N/A | 无 `@Mock` |
| B080 | ✅ | 所有测试方法均有断言（`assertThat`/`assertThrows`） |
| B081 | N/A | 无集合原地修改后未使用 |
| M001 | N/A | 无连续相同条件判断 |
| M002 | N/A | 无 `instanceof` 恒真 |
| M003 | N/A | 无包装类构造器 |
| M004 | N/A | 无 `printStackTrace()` |
| M005 | N/A | 无内部类 |
| M006 | N/A | 无编译期常量布尔表达式 |
| M007 | N/A | 无空 catch |
| M008 | N/A | 无自定义 `equals`/`hashCode` |
| M009 | N/A | 无 `equals` 不兼容类型 |
| M010 | N/A | 无位运算 |
| M011 | N/A | 无 switch |
| M012 | N/A | 无 finally 中 return/throw |
| M013 | N/A | 无浮点类型转换 |
| M014 | N/A | 无枚举 `getClass()` |
| M015 | N/A | 无继承（字段隐藏） |
| M016 | ⚠️ | **预扫命中** `HelloWorldServiceImpl.java:33` — `LocalDateTime.now()` 未显式指定时区，依赖系统默认时区 |
| M017 | N/A | 所有测试方法均有 `@Test` |
| M018 | N/A | 无显式锁 |
| M019 | N/A | 无 switch 枚举 |
| M020 | ✅ | `HelloWorldServiceImpl.java:21` — `@Override` 已添加 |
| M021 | N/A | 无自定义 `equals(SpecificType)` |
| M022 | N/A | 无 `Optional.of(null)` |
| M023 | N/A | 无 `Object.toString()` 打印 |
| M024 | N/A | 无 `Optional` |
| M025 | N/A | 无 final 类 protected 成员 |
| M026 | N/A | 无 `@Mock` |
| M027 | N/A | 无 `ThreadLocal` |
| I001 | ⚠️ | `HelloWorldServiceImplTest.java:73-74` — null 异常测试仅断言类型，建议同时断言异常消息以增强可靠性（P2） |
| I002 | N/A | 无 `@DoNotMock` |
| I003 | N/A | 无 `@AutoValue` |
| I004 | N/A | 无 `java.util.Date` |
| I005 | N/A | 非 JUnit3 |
| I006 | N/A | 使用 `@BeforeEach`（JUnit5） |
| I007 | N/A | 无 `tearDown` |
| I008 | N/A | 无 `dataProvider` |
| I009 | N/A | 统计用规则 |
| I010 | N/A | 无 Spring 容器启动测试 |

### 4.2 可靠性（`reliability-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| G1.1 | N/A | 无事务/并发场景先读后写 |
| G1.2 | N/A | 无锁后二次校验场景 |
| G1.3 | N/A | 无乐观锁 |
| G1.4 | N/A | 无多资源加锁 |
| G2.1 | N/A | 无写接口/消息消费 |
| G2.2 | N/A | 无重试/定时任务/消息 |
| G2.3 | N/A | 无幂等键约定 |
| G3.1 | N/A | 无分布式事务 |
| G3.2 | N/A | 无 `@Transactional` |
| G4.1 | N/A | 无 SQL |
| G4.2 | N/A | 无 SQL |
| G4.3 | N/A | 无 SQL 分页查询 |
| G5.1 | N/A | 无消息消费 |
| G6.1 | N/A | 无缓存 |
| G6.2 | N/A | 无缓存双写 |
| G7.1 | N/A | 无调度任务 |
| G7.2 | N/A | 无调度任务 |
| G8.1 | N/A | 无 catch 吞异常（null 检查直接抛异常，正确） |
| G8.2 | N/A | 无外部依赖 |
| G8.3 | N/A | 无 I/O 流/连接/锁 |
| G8.4 | N/A | 无线程池/定时任务 |
| G8.5 | N/A | 无 ThreadLocal |
| G8.6 | N/A | 无 Executors 线程池 |
| G9.1 | N/A | 无外部调用 |
| G9.2 | N/A | 无外部调用 |
| G9.3 | N/A | 无重试 |
| G10.1 | N/A | 无字段 null 语义歧义 |
| G10.2 | N/A | 无契约变更 |
| G11.1 | ✅ | 测试含断言（`assertThat`/`assertThrows`） |
| G11.2 | ✅ | 测试覆盖：正常、中文、空、空白、null |
| G11.3 | ✅ | `HelloWorldServiceImpl.java:23` — null 参数有防御性校验 |
| G11.4 | N/A | 无数值运算/金额 |
| G12.1 | N/A | 无资金/库存场景 |
| G12.2 | N/A | 无资金场景 |
| G13.1 | ✅ | `HelloWorldServiceImpl.java:30` — 正常流程使用 `LOGGER.info`，级别正确 |
| G14.1 | N/A | 无金额 |
| G14.2 | N/A | 无多租户 |
| G14.3 | ⚠️ | `HelloWorldServiceImpl.java:33` — `LocalDateTime.now()` 未指定时区，跨区部署可能产生不一致时间戳（与 M016 同源，P1） |
| G14.4 | N/A | 无 `SimpleDateFormat`/`DateTimeFormatter` |
| G15.1 | N/A | 无数据库表结构变更 |
| G15.2 | N/A | 无新旧接口共存 |
| G15.3 | N/A | 无不兼容逻辑 |
| G16.1 | N/A | 无核心链路指标埋点（Hello World 入门模块，可接受） |
| G16.2 | ✅ | `HelloWorldServiceImpl.java:30` — 异常路径有日志（null 抛异常前无需额外日志，异常由框架处理） |
| G16.3 | ✅ | `HelloWorldServiceImpl.java:30` — `LOGGER.info` 级别正确 |
| G16.4 | N/A | 无空 catch |
| G17.1 | N/A | 无功能开关需求（Hello World 入门） |
| G17.2 | N/A | 无降级需求 |
| G17.3 | N/A | 无数据变更 |

### 4.3 安全（`security-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| S1.1 | N/A | 无 SQL |
| S1.2 | N/A | 无 SQL 动态排序 |
| S1.3 | N/A | 无 SQL like/in |
| S2.1 | N/A | 无 HTML/JS/URL 输出（Spring Boot 默认 JSON 序列化，安全） |
| S2.2 | N/A | 无富文本 |
| S2.3 | N/A | 无模板引擎 |
| S3.1 | N/A | 无外部 URL 请求 |
| S3.2 | N/A | 无 302 跳转 |
| S3.3 | N/A | 无外部请求 |
| S4.1 | N/A | 无系统命令执行 |
| S4.2 | N/A | 无文件/图片操作 |
| S5.1 | N/A | 无 XML 解析 |
| S5.2 | N/A | 无 XPath |
| S6.1 | N/A | 无反序列化 |
| S6.2 | N/A | 无 JSON 多态 |
| S6.3 | N/A | 无敏感字段 |
| S7.1 | N/A | 无文件上传 |
| S7.2 | N/A | 无文件路径 |
| S7.3 | N/A | 无文件操作 |
| S8.1 | ⚠️ | `HelloWorldController.java` — 接口无鉴权。Hello World 入门模块可接受，但若部署到生产需加鉴权（P2，参考建议） |
| S8.2 | ✅ | GET 请求为只读操作，符合 REST 规范 |
| S8.3 | N/A | 无数据 ID |
| S8.4 | N/A | 无 Cookie 设置 |
| S9.1 | N/A | 无密钥/凭证 |
| S9.2 | ✅ | `HelloWorldServiceImpl.java:30` — 日志仅记录 `displayName`，无敏感信息 |
| S9.3 | N/A | 无传输加密需求（入门模块） |
| S9.4 | N/A | 无随机数 |
| S10.1 | N/A | 无增删改操作（仅 GET） |
| S10.2 | N/A | 无 CORS 配置 |
| S10.3 | N/A | 无 URL 跳转 |

---

## Step 5 — 自定义扩展检查（产物 E）

> 按 `customized-checklist.md` 逐条核销；若未启用可整节写 `N/A(未启用自定义规则)`。

### 5.1 自定义扩展（`customized-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| U1.1 | N/A | 示例项 — Controller 入参 `@Valid` 校验：`HelloWorldController.java:32` 使用简单 String 参数，无需 `@Valid`；`@RequestParam` 已提供默认值 |
| U1.2 | N/A | 未定义 |
| U1.3 | N/A | 未定义 |
| U2.1 | N/A | 未定义 |
| U2.2 | N/A | 未定义 |
| U2.3 | N/A | 未定义 |

**结论**：N/A(未启用自定义规则，仅 U1.1 为示例项)

---

## 终检（防漏检）

- [x] 执行队列中每个文件 `Step2`、`Step3`、**S1–S10 / G1–G17** 各列均非 `⬜`（跳过文件除外）
- [x] Step 2 的每个 REQ/Scenario 均非 `⬜`
- [x] Step 3 的 A1–A7 均非 `⬜`
- [x] Step 4 全部 **G/S** 与 **B001–B081 / M001–M027 / I001–I010** ID 均非 `⬜`（允许 `N/A`，但有原因）
- [x] Step 5 全部 U* ID 均非 `⬜`
- [x] 所有 `❌/⚠️` 已写入 report，且包含 `ID + path:line`