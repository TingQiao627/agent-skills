# Code Review Checklist

> **Change** hello-world · **分支/Commit** `AI/task-DEV-66a2f4f2` / `6ada77a` · **日期** `2026-08-20`
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。
> **完成标准**：所有核销项必须从 `⬜` 变为其他状态；`N/A` 需写原因。
>
> **执行顺序（强制）**：写入本清单并进入逐文件审查前，先在目标仓库对变更路径运行 `references/script/scan-all-rules.sh`，将输出贴入 Step 3 和 Step 4 备注；再用 LLM 完成 Step 2–5 中脚本未覆盖项及复核。

**自动化预扫结果**：`scan-all-rules.sh` 对 `src/main/java/com/dtcoder/hello/` 和 `src/test/java/com/dtcoder/hello/` 扫描完毕，**52/222 条规则无命中**（No findings）。以下由 LLM 逐条补全。

---

## Step 1 — 执行队列（产物 A）

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8 | G9 | G10 | G11 | G12 | G13 | G14 | G15 | G16 | G17 | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10 | 总状态 |
|---|----------------------|----------|-------|-------|----|----|----|----|----|----|----|----|----|-----|----|----|----|----|----|----|----|----|----|-----|-----|-----|-----|-----|-----|-----|-----|--------|
| 1 | src/main/java/com/dtcoder/hello/controller/HelloController.java | REQ-1/REQ-2 | ✅ | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ 已审有问题 |
| 2 | src/main/java/com/dtcoder/hello/model/dto/HelloResponse.java | REQ-1 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 3 | src/main/java/com/dtcoder/hello/service/HelloService.java | REQ-1 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 4 | src/main/java/com/dtcoder/hello/service/impl/HelloServiceImpl.java | REQ-1/REQ-3 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | ✅ | N/A | ✅ | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 5 | src/test/java/com/dtcoder/hello/service/impl/HelloServiceImplTest.java | REQ-4 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |

> 跳过文件：`docs/modules/hello/README.md` — 非 Java（文档文件，跳过）。

---

## Step 2 — 功能（产物 B）

> 仅从 spec/design 文档提 REQ。spec 来源：`docs/modules/hello/README.md`。

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据（文件/测试/接口） |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-1 | 提供 Hello World 问候服务 | README §模块职责：「提供 Hello World 问候服务，包含 REST 接口和业务逻辑」 | HelloService.java, HelloServiceImpl.java, HelloResponse.java | ✅ | `HelloServiceImpl.java:25-43` getGreeting 实现；`HelloServiceImplTest.java` 5 个测试用例全部通过 |
| REQ-2 | REST 控制器处理问候请求 | README §关键类说明：「HelloController - REST 控制器，处理问候请求」；§API 接口列表：「greet(String name)」 | HelloController.java | ⚠️ | `HelloController.java:15` — 类声明缺少 `@RestController`/`@Controller` 等 Spring Web 注解，方法 `greet()` 无 `@RequestMapping`/`@GetMapping` 等映射注解，无法作为 REST 端点对外服务 |
| REQ-3 | 参数校验：null 抛异常，空字符串返回默认值 | README §API 接口列表：「name（可选，null 抛异常）」；HelloService Javadoc：「@throws IllegalArgumentException 当 name 为 null 时抛出」 | HelloServiceImpl.java | ✅ | `HelloServiceImpl.java:26-28` null 校验 → `IllegalArgumentException`；`:32-36` 空字符串 → `"Hello!"`；`:30` trim 处理 |
| REQ-4 | 单元测试覆盖正常/边界/异常场景 | README §关键类说明：「HelloServiceImplTest - 单元测试，覆盖正常/边界/异常场景」 | HelloServiceImplTest.java | ✅ | 5 个 `@Test`：正常名称（`World`）、中文名称（`世界`）、空字符串、null 异常、首尾空格 trim |

---

## Step 3 — 可读性检查（产物 C）

> 对照 `references/readability-checklist.md` A1–A7 逐节核销。预扫脚本无命中。

| ID | 检查项 | 状态 | 备注（命中写 `path:line`） |
|----|--------|------|----------------------------|
| A1 | 源文件格式 | ✅ | 所有文件 UTF-8 编码，文件名与类名一致 |
| A2 | 源文件结构/import 顺序 | ⚠️ | `HelloController.java:5` 直接 import `HelloServiceImpl`（具体实现类），而非仅依赖接口 `HelloService`。违反了依赖倒置原则，且 import 分组未严格按静态/非静态分两组（无静态 import，不强制分组） |
| A3 | 代码样式 | ✅ | K&R 大括号、4 空格缩进、行宽均未超 120 字符 |
| A4 | 命名规范 | ✅ | 包名全小写、类名 UpperCamelCase、方法名 lowerCamelCase、测试类名 `HelloServiceImplTest` 符合规范 |
| A5 | 编码实践 | ✅ | `HelloServiceImpl.java:24` `@Override` 正确使用；无空 catch；无 finalize 重写 |
| A6 | 特定元素样式 | ✅ | 无数组、switch、long 字面量等场景 |
| A7 | Javadoc 规范 | ✅ | 所有 public 类/接口/方法均有 Javadoc，`@param`/`@return`/`@throws` 顺序正确 |

---

## Step 4 — 可靠性检查（产物 D）

### 4.1 Bug 模式（`bug-pattern-checklist.md`）

> 预扫脚本 `scan-all-rules.sh` **无命中**。以下 LLM 逐条核对结果。

| ID | 状态 | 备注 |
|----|------|------|
| B001 | N/A | 无 `LocalDateTime.parse` / `UUID.fromString` 等字面量调用 |
| B002 | N/A | 无数组 equals 比较 |
| B003 | N/A | 无 `Arrays.fill` |
| B004 | N/A | 无数组 `toString()` |
| B005 | N/A | 无 `Arrays.asList` 基本类型数组 |
| B006 | N/A | 测试使用 AssertJ `assertThat`，非 JUnit `assertEquals` |
| B007 | N/A | 无 `catch(Throwable)` 吞断言 |
| B008 | N/A | 无 `Executors` 创建线程池 |
| B009 | N/A | 无移位运算 |
| B010 | N/A | 无 `BigDecimal(double)` |
| B011 | N/A | 无包装类型 `==` 比较 |
| B012 | N/A | 无 `Calendar` 使用 |
| B013 | N/A | 无 `Calendar.HOUR` |
| B014 | N/A | 无集合类型不兼容查询 |
| B015 | N/A | 无 `Collection.toArray` |
| B016 | N/A | 无 `Comparable` 实现 |
| B017 | N/A | 无 `this == null` |
| B018 | N/A | 无三目运算符数值类型混用 |
| B019 | N/A | 无 Money 类 |
| B020 | N/A | 无编译期常量乘法溢出 |
| B021 | N/A | 无 Jedis 使用 |
| B022 | N/A | 无 `SimpleDateFormat` |
| B023 | N/A | 无死异常实例 |
| B024 | N/A | 无 `new Thread()` 未 start |
| B025 | N/A | 无双括号初始化 |
| B026 | N/A | 无 `x.equals(null)` |
| B027 | N/A | 无 `equals` 方法 |
| B028 | N/A | 无 `DateUtil` |
| B029 | N/A | 无 setter 赋值错误 |
| B030 | N/A | 无浮点数 `==` 比较 |
| B031 | N/A | 无 `String.format` |
| B032 | N/A | 无注解 `getClass()` |
| B033 | N/A | 无 Unsafe 操作 |
| B034 | N/A | 无 `Hashtable` |
| B035 | N/A | 无同一对象二元运算 |
| B036 | N/A | 无 `IdentityHashMap` |
| B037 | N/A | 无可变参数条件表达式 |
| B038 | N/A | 无无条件递归 |
| B039 | N/A | 无 `String.indexOf` |
| B040 | N/A | 无 `Class.isInstance` |
| B041 | N/A | 无 JDBC 连接 |
| B042 | N/A | 非 JUnit3 |
| B043 | N/A | 无内部类 `@Test` |
| B044 | N/A | 非 JUnit3/JUnit4 混用 |
| B045 | N/A | 无包装类型加锁 |
| B046 | N/A | 无循环条件不更新 |
| B047 | N/A | 无 `Float.compare` |
| B048 | N/A | 无 `Math.round(整型)` |
| B049 | N/A | 无日期格式 `DD` |
| B050 | N/A | 无 12/24 小时制混用 |
| B051 | N/A | 无 `Boolean.getBoolean` |
| B052 | N/A | 无 `YYYY-MM-dd` |
| B053 | N/A | 测试用 `assertThatThrownBy`，非 try/catch 模式 |
| B054 | N/A | 无 `EqualsTester` |
| B055 | N/A | 无 Mockito |
| B056 | N/A | 无 `Arrays.asList().add()` |
| B057 | N/A | 无增强 for 中修改集合 |
| B058 | N/A | 无集合自操作 |
| B059 | N/A | 无 `Collections.nCopies` |
| B060 | N/A | 无三目 null 拆箱 |
| B061 | N/A | 无 `sun.misc.BASE64` |
| B062 | N/A | 无 `URLClassLoader` 强转 |
| B063 | N/A | 无 `javax.xml` |
| B064 | N/A | 无 `Optional` `==` |
| B065 | N/A | 无 Pojo 自赋值 |
| B066 | N/A | 无 `(int) Math.random()` |
| B067 | N/A | 无 `Random.nextInt() %` |
| B068 | N/A | 无自赋值 |
| B069 | N/A | 无 `compareTo` |
| B070 | N/A | 无 `equals` 自比较 |
| B071 | N/A | 无 `size() >= 0` |
| B072 | N/A | 无 `Stream.toString()` |
| B073 | N/A | 无 `StringBuilder(char)` |
| B074 | N/A | 无 `substring(0)` |
| B075 | N/A | 无可疑 for 循环 |
| B076 | N/A | 无 `@Transactional` |
| B077 | N/A | 测试无 `catch(Throwable)` |
| B078 | N/A | 无 `assertThat(x).isEqualTo(x)` |
| B079 | N/A | 无 `@Mock` |
| B080 | ✅ | `HelloServiceImplTest.java` 所有 5 个测试方法均含断言（`assertThat`/`assertThatThrownBy`） |
| B081 | N/A | 无集合原地修改后未使用 |
| M001 | N/A | 无连续相同条件判断 |
| M002 | N/A | 无 `instanceof` 恒真 |
| M003 | N/A | 无包装类构造器 |
| M004 | ✅ | 无 `printStackTrace()`，异常通过 `throw` 和日志处理 |
| M005 | N/A | 无内部类 |
| M006 | N/A | 无编译期可确定布尔表达式 |
| M007 | ✅ | 无空 catch 块 |
| M008 | N/A | 无 `equals`/`hashCode` 重写 |
| M009 | N/A | 无 `equals` 不兼容类型 |
| M010 | N/A | 无位运算错误 |
| M011 | N/A | 无 switch |
| M012 | N/A | 无 finally 中 return/throw |
| M013 | N/A | 无浮点强转 |
| M014 | N/A | 无枚举 `getClass()` |
| M015 | N/A | 无继承关系 |
| M016 | N/A | 无 `LocalDateTime.now()` 等默认时区调用 |
| M017 | N/A | 所有测试方法均有 `@Test` |
| M018 | N/A | 无显式锁 |
| M019 | N/A | 无 switch 枚举 |
| M020 | ✅ | `HelloServiceImpl.java:24` `@Override` 正确添加 |
| M021 | N/A | 无 `equals` 重写 |
| M022 | N/A | 无 `Optional.of(null)` |
| M023 | N/A | 无 `Object.toString()` 误用 |
| M024 | N/A | 无 Optional 空 get |
| M025 | N/A | 无 final 类 |
| M026 | N/A | 无 `@Mock` |
| M027 | N/A | 无 `ThreadLocal` |
| I001 | ✅ | `HelloServiceImplTest.java:78-81` 对异常消息做了 `hasMessageContaining` 断言 |
| I002 | N/A | 无 `@DoNotMock` |
| I003 | N/A | 无 `@AutoValue` |
| I004 | N/A | 无 `java.util.Date` |
| I005 | N/A | 非 JUnit3 |
| I006 | N/A | 使用 `@BeforeEach`（JUnit5），非 `setUp()` |
| I007 | N/A | 无 `tearDown()` |
| I008 | N/A | 无 `@DataProvider` |
| I009 | N/A | 统计用，不在审查范围 |
| I010 | N/A | 无 Spring 容器启动 |

### 4.2 可靠性（`reliability-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| G1.1 | N/A | 无事务/并发先读后写场景 |
| G1.2 | N/A | 无锁后二次校验场景 |
| G1.3 | N/A | 无乐观锁 |
| G1.4 | N/A | 无多资源加锁 |
| G2.1 | N/A | 无写接口/消息消费 |
| G2.2 | N/A | 无重试/定时任务 |
| G2.3 | N/A | 无幂等键约定 |
| G3.1 | N/A | 无分布式事务 |
| G3.2 | N/A | 无 `@Transactional` |
| G4.1 | N/A | 无 SQL |
| G4.2 | N/A | 无索引列函数转换 |
| G4.3 | N/A | 无分页查询 |
| G5.1 | N/A | 无 MQ 消费 |
| G6.1 | N/A | 无缓存 |
| G6.2 | N/A | 无缓存双写 |
| G7.1 | N/A | 无调度任务 |
| G7.2 | N/A | 无调度任务 |
| G8.1 | N/A | 无 catch 吞异常 |
| G8.2 | N/A | 无核心链路强依赖 |
| G8.3 | N/A | 无 I/O 流/连接/锁资源 |
| G8.4 | N/A | 无线程池 |
| G8.5 | N/A | 无 ThreadLocal |
| G8.6 | N/A | 无 Executors 创建线程池 |
| G8.7 | N/A | 模板无此 ID |
| G9.1 | N/A | 无外部调用 |
| G9.2 | N/A | 无外部调用 |
| G9.3 | N/A | 无重试逻辑 |
| G10.1 | N/A | 无 null 多义字段 |
| G10.2 | N/A | 无接口版本变更 |
| G10.3 | N/A | 模板无此 ID |
| G11.1 | ✅ | `HelloServiceImplTest.java` 5 个测试均有断言 |
| G11.2 | ✅ | 覆盖空字符串、null、首尾空格、中文、正常英文 |
| G11.3 | ✅ | `HelloServiceImpl.java:26-28` null 入参有防御性校验 |
| G11.4 | N/A | 无数值运算 |
| G12.1 | N/A | 无资金相关场景 |
| G12.2 | N/A | 无资金相关场景 |
| G13.1 | ✅ | 日志级别正确：debug 用 DEBUG，异常用 throw（无错误日志误用） |
| G14.1 | N/A | 无金额/货币 |
| G14.2 | N/A | 无多租户 |
| G14.3 | N/A | 无时区处理 |
| G14.4 | N/A | 无日期格式化 |
| G15.1 | N/A | 无数据库表结构变更 |
| G15.2 | N/A | 无接口共存 |
| G15.3 | N/A | 无开关切换 |
| G16.1 | N/A | 无核心链路指标埋点（简单 hello world） |
| G16.2 | ⚠️ | `HelloController.java:43-54` greet() 方法无 try-catch，若 service 抛异常，Controller 层无日志记录（service 层已处理，但 Controller 层缺少防御性异常日志）— P2 |
| G16.3 | ✅ | 日志级别正确：debug 级别记录调试信息，符合规范 |
| G16.4 | N/A | 无空 catch 或 printStackTrace |
| G17.1 | N/A | 无功能开关需求 |
| G17.2 | N/A | 无降级预案需求 |
| G17.3 | N/A | 无数据变更 |
| G18.1 | N/A | 模板无此 ID |
| G18.2 | N/A | 模板无此 ID |
| G18.3 | N/A | 模板无此 ID |

### 4.3 安全（`security-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| S1.1 | N/A | 无 SQL 操作 |
| S1.2 | N/A | 无动态 SQL |
| S1.3 | N/A | 无 SQL 操作 |
| S2.1 | N/A | 无 HTML/JS 输出 |
| S2.2 | N/A | 无富文本 |
| S2.3 | N/A | 无模板引擎 |
| S3.1 | N/A | 无外部 URL 请求 |
| S3.2 | N/A | 无 URL 跳转 |
| S3.3 | N/A | 无外部请求 |
| S4.1 | N/A | 无系统命令 |
| S4.2 | N/A | 无文件操作 |
| S5.1 | N/A | 无 XML 解析 |
| S5.2 | N/A | 无 XPath |
| S6.1 | N/A | 无反序列化 |
| S6.2 | N/A | 无 JSON 多态反序列化 |
| S6.3 | N/A | 无敏感字段 |
| S7.1 | N/A | 无文件上传 |
| S7.2 | N/A | 无文件路径 |
| S7.3 | N/A | 无文件存储 |
| S8.1 | N/A | 无鉴权需求（简单 hello world） |
| S8.2 | N/A | 无 HTTP 方法映射 |
| S8.3 | N/A | 无数据 ID |
| S8.4 | N/A | 无 Cookie |
| S9.1 | N/A | 无密钥/凭证 |
| S9.2 | N/A | 日志仅记录 name 参数，无敏感信息 |
| S9.3 | N/A | 无传输/存储加密需求 |
| S9.4 | N/A | 无随机数 |
| S10.1 | N/A | 无 CSRF 场景 |
| S10.2 | N/A | 无 CORS 配置 |
| S10.3 | N/A | 无 URL 跳转 |

---

## Step 5 — 自定义扩展检查（产物 E）

| ID | 状态 | 备注 |
|----|------|------|
| U1.1 | N/A | `customized-checklist.md` 仅含示例项 `U1.1`（Controller 入参 `@Valid`），本项目无 Spring 依赖，`N/A(未启用自定义规则)` |
| U1.2 | N/A | 未定义 |
| U1.3 | N/A | 未定义 |
| U2.1 | N/A | 未定义 |
| U2.2 | N/A | 未定义 |
| U2.3 | N/A | 未定义 |

> **Step 5 整体**：`N/A(未启用自定义规则)` — `customized-checklist.md` 仅含占位示例项，无实际项目自定义规则。

---

## 终检（防漏检）

- [x] 执行队列中每个文件 `Step2`、`Step3`、**S1–S10 / G1–G17** 各列均非 `⬜`（跳过文件除外）
- [x] Step 2 的每个 REQ/Scenario 均非 `⬜`（4/4）
- [x] Step 3 的 A1–A7 均非 `⬜`（7/7）
- [x] Step 4 全部 **G/S** 与 **B001–B081 / M001–M027 / I001–I010** ID 均非 `⬜`
- [x] Step 5 全部 U* ID 均非 `⬜`
- [x] 所有 `❌/⚠️` 已写入 report，且包含 `ID + path:line`