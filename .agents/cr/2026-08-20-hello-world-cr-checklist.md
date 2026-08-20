# Code Review Checklist

> **Change** `hello-world` · **分支/Commit** `AI/task-DEV-66a2f4f2-84c9-11f1-9849-d5c90ba1aaae-befe7905-c88f-4bd0-8432-d2a2ee50f9d6` / `04b45c2` · **日期** `2026-08-20`
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。
> **完成标准**：所有核销项必须从 `⬜` 变为其他状态；`N/A` 需写原因。
>
> **自动化预扫结果**：`scan-all-rules.sh` 命中 1 条 → `[P1] M016 — JavaTimeDefaultTimeZone: src/hello-world/src/main/java/com/dt/example/hello/service/impl/HelloServiceImpl.java:38`

---

## Step 1 — 执行队列（产物 A）

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8 | G9 | G10 | G11 | G12 | G13 | G14 | G15 | G16 | G17 | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10 | 总状态 |
|---|----------------------|----------|-------|-------|----|----|----|----|----|----|----|----|----|-----|-----|-----|-----|-----|-----|-----|-----|----|----|----|----|----|----|----|----|----|-----|--------|
| 1 | `src/hello-world/src/main/java/com/dt/example/hello/HelloApplication.java` | REQ-1 / 启动类 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 2 | `src/hello-world/src/main/java/com/dt/example/hello/api/controller/HelloController.java` | REQ-1 / REST 端点 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 3 | `src/hello-world/src/main/java/com/dt/example/hello/model/vo/HelloVO.java` | REQ-1,REQ-4 / 视图对象 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 4 | `src/hello-world/src/main/java/com/dt/example/hello/service/HelloService.java` | REQ-1,REQ-2 / 服务接口 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 5 | `src/hello-world/src/main/java/com/dt/example/hello/service/impl/HelloServiceImpl.java` | REQ-1,REQ-2,REQ-3,REQ-4 / 核心实现 | ✅ | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ 已审有问题 |
| 6 | `src/hello-world/src/test/java/com/dt/example/hello/service/HelloServiceTest.java` | REQ-2,REQ-3 / 单元测试 | ✅ | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ 已审有问题 |

---

## Step 2 — 功能（产物 B）

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据（文件/测试/接口） |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-1 | **Given** 服务启动 **When** GET `/api/hello?name=World` **Then** 返回 `HelloVO` 含问候语 | `docs/modules/hello/README.md` L5 "提供简单的 Hello World REST API 接口", L23-25 API 表格 | HelloController.java, HelloService.java, HelloServiceImpl.java, HelloVO.java | ✅ | `HelloController.java:34-36` — `@GetMapping` + `sayHello` 方法；`HelloServiceImpl.java:35-38` — 生成消息并返回 VO |
| REQ-2 | **Given** name 为空/null/空白 **When** 调用 `sayHello()` **Then** 抛出 `IllegalArgumentException` | `HelloService.java:19` — `@throws IllegalArgumentException 当 name 为空或空白时抛出` | HelloService.java, HelloServiceImpl.java, HelloServiceTest.java | ✅ | `HelloServiceImpl.java:30-33` — null/blank 校验 + 抛异常；`HelloServiceTest.java:67-91` — 3 个异常测试覆盖空/null/空白 |
| REQ-3 | **Given** name="World" **When** 调用 `sayHello()` **Then** 返回 message="Hello, World!" | `docs/modules/hello/README.md` L37 — `"message": "Hello, World!"` | HelloServiceImpl.java, HelloServiceTest.java | ✅ | `HelloServiceImpl.java:35` — `"Hello, " + name + "!"`；`HelloServiceTest.java:43-44` — `assertThat(result.getMessage()).contains("Hello")` + `contains("World")` |
| REQ-4 | **Given** 调用 `sayHello()` **When** 返回响应 **Then** 包含 `timestamp` 字段 | `docs/modules/hello/README.md` L38 — `"timestamp": "2026-01-01T12:00:00"` | HelloVO.java, HelloServiceImpl.java | ✅ | `HelloServiceImpl.java:38` — `new HelloVO(message, LocalDateTime.now())`；`HelloVO.java:17` — `private LocalDateTime timestamp` |

---

## Step 3 — 可读性检查（产物 C）

| ID | 检查项 | 状态 | 备注（命中写 `path:line`） |
|----|--------|------|----------------------------|
| A1 | 源文件格式 | ✅ | 所有文件文件名=类名+.java，UTF-8，无Tab |
| A2 | 源文件结构/import 顺序 | ⚠️ | **P2** `HelloServiceImpl.java:9` — `java.time.LocalDateTime` 应排在 `org.slf4j` 之前（ASCII 序）；**P2** `HelloServiceTest.java:1-7` — 静态 import 应放在非静态 import 之前（A2.3） |
| A3 | 代码样式 | ✅ | K&R 大括号、4空格缩进、行宽≤120、成员间空行、运算符空格均正确 |
| A4 | 命名规范 | ✅ | 包名全小写、类名 UpperCamelCase、方法/字段 lowerCamelCase、常量 UPPER_SNAKE_CASE（LOGGER）、测试类 HelloServiceTest |
| A5 | 编码实践 | ✅ | `@Override` 已添加（`HelloServiceImpl.java:28`），无空 catch，无 finalize |
| A6 | 特定元素样式 | ✅ | `String[] args`（非 C 风格），无 switch，修饰符顺序正确，注解每行一个 |
| A7 | Javadoc 规范 | ✅ | 所有 public 类/方法均有 Javadoc，`@param`/`@return`/`@throws` 顺序正确 |

---

## Step 4 — 可靠性检查（产物 D）

### 4.1 Bug 模式（`bug-pattern-checklist.md`）

> 自动化预扫命中：`[P1] M016 — JavaTimeDefaultTimeZone: HelloServiceImpl.java:38`

| ID | 状态 | 备注（命中写 `path:line`；预扫可粘贴脚本摘要） |
|----|------|--------------------------------------------------|
| B001 | N/A | 无字面量调用 parse/of |
| B002 | N/A | 无数组 equals |
| B003 | N/A | 无 Arrays.fill |
| B004 | N/A | 无数组 toString |
| B005 | N/A | 无 Arrays.asList |
| B006 | N/A | 使用 AssertJ assertThat，非 JUnit assertEquals |
| B007 | N/A | 无 catch Throwable/Error |
| B008 | N/A | 无 Executors 线程池 |
| B009 | N/A | 无移位运算 |
| B010 | N/A | 无 BigDecimal |
| B011 | N/A | 无包装类型 == 比较 |
| B012 | N/A | 无 Calendar |
| B013 | N/A | 无 Calendar |
| B014 | N/A | 无集合不兼容类型查询 |
| B015 | N/A | 无 Collection.toArray |
| B016 | N/A | 无自定义 Comparable |
| B017 | N/A | 无 this==null |
| B018 | N/A | 无三目运算符数值类型混合 |
| B019 | N/A | 无 Money 类 |
| B020 | N/A | 无编译期常量乘法 |
| B021 | N/A | 无 Jedis |
| B022 | N/A | 无 SimpleDateFormat |
| B023 | N/A | 无 DeadException |
| B024 | N/A | 无 DeadThread |
| B025 | N/A | 无双括号初始化 |
| B026 | N/A | 无 equals(null) |
| B027 | N/A | 无 equals 实现 |
| B028 | N/A | 无 commons.httpclient |
| B029 | N/A | 无 Pojo setter |
| B030 | N/A | 无浮点 == |
| B031 | N/A | 无 String.format |
| B032 | N/A | 无注解 getClass |
| B033 | N/A | 无 Unsafe |
| B034 | N/A | 无 Hashtable |
| B035 | N/A | 无 IdentityBinaryExpression |
| B036 | N/A | 无 IdentityHashMap |
| B037 | N/A | 无可变参数条件表达式 |
| B038 | N/A | 无递归方法 |
| B039 | N/A | 无 indexOf |
| B040 | N/A | 无 isInstance |
| B041 | N/A | 无 JDBC |
| B042 | N/A | 非 JUnit3 |
| B043 | N/A | 无内部类 @Test |
| B044 | N/A | 非 JUnit3+JUnit4 混用 |
| B045 | N/A | 无包装类型加锁 |
| B046 | N/A | 无循环条件未更新 |
| B047 | N/A | 无 Float.compare |
| B048 | N/A | 无 Math.round |
| B049 | N/A | 无日期格式 DD |
| B050 | N/A | 无 hh 格式 |
| B051 | N/A | 无 Boolean.getBoolean |
| B052 | N/A | 无 YYYY 格式 |
| B053 | N/A | 使用 assertThatThrownBy，无需 fail |
| B054 | N/A | 无 EqualsTester |
| B055 | N/A | 无 Mockito |
| B056 | N/A | 无 Arrays.asList 修改 |
| B057 | N/A | 无增强 for 修改集合 |
| B058 | N/A | 无集合自身参数 |
| B059 | N/A | 无 nCopies |
| B060 | N/A | 无 NullTernary |
| B061 | N/A | 无 BASE64Encoder |
| B062 | N/A | 无 URLClassLoader 强转 |
| B063 | N/A | 无 javax.xml.bind |
| B064 | N/A | 无 Optional |
| B065 | N/A | 无 Pojo 自赋值 |
| B066 | N/A | 无 Math.random 强转 |
| B067 | N/A | 无 Random % |
| B068 | N/A | 无自赋值 |
| B069 | N/A | 无 compareTo 自身 |
| B070 | N/A | 无 equals 自身 |
| B071 | N/A | 无 size()>=0 |
| B072 | N/A | 无 Stream.toString |
| B073 | N/A | 无 StringBuilder(char) |
| B074 | N/A | 无 substring(0) |
| B075 | N/A | 无 SuspiciousForLoop |
| B076 | N/A | 无 @Transactional |
| B077 | N/A | 无 catch Throwable |
| B078 | N/A | 无 Truth assertThat(x).isEqualTo(x) |
| B079 | N/A | 无 @Mock |
| B080 | ✅ | 所有测试方法均有断言（assertThat / assertThatThrownBy） |
| B081 | N/A | 无 UnusedCollectionModifiedInPlace |
| M001 | N/A | 无连续相同条件判断 |
| M002 | N/A | 无 instanceof 恒真 |
| M003 | N/A | 无包装类构造器 |
| M004 | N/A | 无 printStackTrace |
| M005 | N/A | 无内部类 |
| M006 | N/A | 无编译期常量布尔表达式 |
| M007 | N/A | 无 catch 块 |
| M008 | N/A | 无 equals/hashCode 重写 |
| M009 | N/A | 无不兼容类型 equals |
| M010 | N/A | 无位运算 |
| M011 | N/A | 无 switch |
| M012 | N/A | 无 finally |
| M013 | N/A | 无 FloatCast |
| M014 | N/A | 无枚举 getClass |
| M015 | N/A | 无父子类字段隐藏 |
| **M016** | **⚠️** | **P1** `HelloServiceImpl.java:38` — `LocalDateTime.now()` 使用系统默认时区，应显式指定 `ZoneId`（预扫命中） |
| M017 | N/A | 所有测试有 @Test |
| M018 | N/A | 无 Lock |
| M019 | N/A | 无枚举 switch |
| M020 | ✅ | `HelloServiceImpl.java:28` — `@Override` 已添加 |
| M021 | N/A | 无 equals(SpecificType) |
| M022 | N/A | 无 Optional.of(null) |
| M023 | N/A | 无 Object.toString 误用 |
| M024 | N/A | 无 Optional get 误用 |
| M025 | N/A | 无 final 类 protected |
| M026 | N/A | 无 @Mock static |
| M027 | N/A | 无 ThreadLocal |
| I001 | ✅ | 测试使用 `hasMessageContaining("name")` 对异常消息断言 |
| I002 | N/A | 无 @DoNotMock |
| I003 | N/A | 无 @AutoValue |
| I004 | N/A | 无 java.util.Date |
| I005 | N/A | 非 JUnit3 |
| I006 | N/A | 使用 @BeforeEach |
| I007 | N/A | 无 tearDown |
| I008 | N/A | 无 dataProvider |
| I009 | N/A | 统计用 |
| I010 | N/A | 无 Spring 容器测试 |

### 4.2 可靠性（`reliability-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| G1.1 | N/A | 无并发先读后写场景 |
| G1.2 | N/A | 无锁+二次校验场景 |
| G1.3 | N/A | 无乐观锁场景 |
| G1.4 | N/A | 无多资源加锁场景 |
| G2.1 | N/A | 无写接口 |
| G2.2 | N/A | 无重试/定时任务 |
| G2.3 | N/A | 无幂等键 |
| G3.1 | N/A | 无分布式事务 |
| G3.2 | N/A | 无 @Transactional |
| G4.1 | N/A | 无 SQL |
| G4.2 | N/A | 无索引列 |
| G4.3 | N/A | 无分页查询 |
| G5.1 | N/A | 无 MQ |
| G6.1 | N/A | 无缓存 |
| G6.2 | N/A | 无缓存双写 |
| G7.1 | N/A | 无调度任务 |
| G7.2 | N/A | 无调度任务 |
| G8.1 | ✅ | 异常路径有日志+抛异常，非仅吞异常 |
| G8.2 | N/A | 无外部依赖调用 |
| G8.3 | N/A | 无 I/O 流/连接/锁 |
| G8.4 | N/A | 无线程池 |
| G8.5 | N/A | 无 ThreadLocal |
| G8.6 | N/A | 无 Executors 线程池 |
| G9.1 | N/A | 无外部调用 |
| G9.2 | N/A | 无外部调用 |
| G9.3 | N/A | 无重试 |
| G10.1 | N/A | 无 null 语义混淆 |
| G10.2 | N/A | 无契约变更 |
| G11.1 | ✅ | 有 5 个测试方法，均有断言 |
| G11.2 | ✅ | 覆盖空/空白/null/中文/英文边界 |
| G11.3 | ✅ | `HelloServiceImpl.java:30` — 入参 null/blank 有校验 |
| G11.4 | N/A | 无数值运算 |
| G12.1 | N/A | 无资金场景 |
| G12.2 | N/A | 无资金场景 |
| G13.1 | ✅ | 校验失败 WARN，成功 INFO，级别正确 |
| G14.1 | N/A | 无金额 |
| G14.2 | N/A | 无多租户 |
| G14.3 | ⚠️ | **P1** `HelloServiceImpl.java:38` — `LocalDateTime.now()` 未指定时区（同 M016） |
| G14.4 | N/A | 无 SimpleDateFormat/DateTimeFormatter |
| G15.1 | N/A | 无数据库变更 |
| G15.2 | N/A | 新模块无旧接口 |
| G15.3 | N/A | 无不兼容逻辑 |
| G16.1 | N/A | Hello World 简单模块，无需核心链路指标 |
| G16.2 | ✅ | 异常有日志含上下文（`"sayHello 参数校验失败：name 为空或空白"`） |
| G16.3 | ✅ | WARN 校验失败，INFO 成功 — 正确 |
| G16.4 | ✅ | 无空 catch 块 |
| G17.1 | N/A | Hello World 简单模块 |
| G17.2 | N/A | 无降级需求 |
| G17.3 | N/A | 无数据变更 |
| G18.1 | N/A | 无安全补强场景 |
| G18.2 | N/A | — |
| G18.3 | N/A | — |

### 4.3 安全（`security-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| S1.1 | N/A | 无 SQL |
| S1.2 | N/A | 无 SQL |
| S1.3 | N/A | 无 SQL |
| S2.1 | N/A | REST API 返回 JSON，Spring Boot 默认转义 |
| S2.2 | N/A | 无富文本 |
| S2.3 | N/A | 无模板引擎 |
| S3.1 | N/A | 无外部 URL 请求 |
| S3.2 | N/A | 无外部 URL 请求 |
| S3.3 | N/A | 无外部 URL 请求 |
| S4.1 | N/A | 无系统命令 |
| S4.2 | N/A | 无文件操作 |
| S5.1 | N/A | 无 XML 解析 |
| S5.2 | N/A | 无 XPath |
| S6.1 | N/A | 无自定义反序列化 |
| S6.2 | N/A | 无 JSON 多态反序列化 |
| S6.3 | N/A | 无敏感 transient 字段 |
| S7.1 | N/A | 无文件上传 |
| S7.2 | N/A | 无文件路径 |
| S7.3 | N/A | 无文件存储 |
| S8.1 | N/A | Hello World 公开接口，无需鉴权 |
| S8.2 | ✅ | GET 方法仅读取，符合 RESTful 语义 |
| S8.3 | N/A | 无数据 ID |
| S8.4 | N/A | 无 Cookie |
| S9.1 | N/A | 无密钥/凭证 |
| S9.2 | N/A | 无敏感信息日志 |
| S9.3 | N/A | 无加密需求 |
| S9.4 | N/A | 无随机数 |
| S10.1 | N/A | GET 只读，无需 CSRF |
| S10.2 | N/A | 无 CORS 配置 |
| S10.3 | N/A | 无 URL 跳转 |

---

## Step 5 — 自定义扩展检查（产物 E）

### 5.1 自定义扩展（`customized-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| U1.1 | N/A | 仅示例项 `@Valid`，当前变更无复杂入参校验需求 |
| U1.2 | N/A | 未定义 |
| U1.3 | N/A | 未定义 |
| U2.1 | N/A | 未定义 |
| U2.2 | N/A | 未定义 |
| U2.3 | N/A | 未定义 |

> **结论**：自定义扩展清单仅含示例项，当前变更未启用项目自定义规则。整节标记为 `N/A(未启用自定义规则)`。

---

## 终检（防漏检）

- [x] 执行队列中每个文件 `Step2`、`Step3`、**S1–S10 / G1–G17** 各列均非 `⬜`（跳过文件除外）；
- [x] Step 2 的每个 REQ/Scenario 均非 `⬜`
- [x] Step 3 的 A1–A7 均非 `⬜`
- [x] Step 4 全部 **G/S** 与 **B001–B081 / M001–M027 / I001–I010** ID 均非 `⬜`（允许 `N/A`，但有原因）
- [x] Step 5 全部 U* ID 均非 `⬜`（允许 `N/A(未启用自定义规则)`）
- [x] 所有 `❌/⚠️` 已写入 report，且包含 `ID + path:line`