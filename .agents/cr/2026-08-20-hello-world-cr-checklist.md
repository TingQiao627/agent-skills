# Code Review Checklist

> **Change** hello-world · **分支/Commit** AI/task-DEV-66a2f4f2-84c9-11f1-9849-d5c90ba1aaae-e1184781-1e0c-4c41-9fe5-504f2a48eb89 · **日期** 2026-08-20
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。
> **完成标准**：所有核销项必须从 `⬜` 变为其他状态；`N/A` 需写原因。
>
> **执行顺序（强制）**：已运行 `scan-all-rules.sh`，无发现（52/222 rules scanned）。

---

## Step 1 — 执行队列（产物 A）

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8 | G9 | G10 | G11 | G12 | G13 | G14 | G15 | G16 | G17 | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10 | 总状态 |
|---|----------------------|----------|-------|-------|----|----|----|----|----|----|----|----|----|-----|----|----|----|----|----|----|----|----|----|-----|-----|-----|-----|-----|-----|-----|-----|--------|
| 1 | `src/main/java/com/example/HelloWorld.java` | REQ-1, REQ-2 / 核心实现 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 2 | `src/test/java/com/example/HelloWorldTest.java` | REQ-1, REQ-2 / 测试验证 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |

---

## Step 2 — 功能（产物 B）

> 仅从 spec 提 **REQ**，勿臆造。不符 spec 标 **P0**。
> Spec 源：`docs/modules/hello-world/README.md`

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据（文件/测试/接口） |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-1 | `greet()` 返回默认问候语 "Hello, World!" | README.md §API 接口列表: `String greet()` 返回默认问候语 "Hello, World!" | HelloWorld.java, HelloWorldTest.java | ✅ | HelloWorld.java:24-26 `String.format(GREETING_TEMPLATE, DEFAULT_NAME)`; HelloWorldTest.java:27-36 `assertEquals(DEFAULT_GREETING, result)` |
| REQ-2 | `greet(String name)` 返回个性化问候语；name 为 null/空白时抛 IllegalArgumentException | README.md §API 接口列表: `String greet(String name)` 返回个性化问候语，name 为 null 或空白时抛 IllegalArgumentException | HelloWorld.java, HelloWorldTest.java | ✅ | HelloWorld.java:35-40 参数校验+格式化; HelloWorldTest.java:42-84 覆盖正常/null/空串/纯空格 4 场景 |

---

## Step 3 — 可读性检查（产物 C）

对照 `references/readability-checklist.md` A1–A7 逐节核销：

| ID | 检查项 | 状态 | 备注（命中写 `path:line`） |
|----|--------|------|----------------------------|
| A1 | 源文件格式 | ✅ | 文件名=类名，UTF-8，空格无 Tab |
| A2 | 源文件结构/import 顺序 | ✅ | 无 import（纯标准库）；HelloWorldTest 静态/非静态分组正确 |
| A3 | 代码样式 | ✅ | K&R 大括号，4空格缩进，行宽≤120，运算符空格正确 |
| A4 | 命名规范 | ✅ | 包名全小写，类名 UpperCamelCase，方法 lowerCamelCase，常量 UPPER_SNAKE_CASE，测试类 Test 后缀 |
| A5 | 编码实践 | ✅ | 无 @Override 需求（无重写），无 catch 块，无 static 实例调用 |
| A6 | 特定元素样式 | ✅ | 无数组/switch/long 字面量 |
| A7 | Javadoc 规范 | ✅ | public 类+方法均有 Javadoc，@param→@return→@throws 顺序正确 |

---

## Step 4 — 可靠性检查（产物 D）

### 4.1 Bug 模式（`bug-pattern-checklist.md`）

> 预扫 `scan-all-rules.sh` 已运行，无发现。

| ID | 状态 | 备注 |
|----|------|------|
| B001 | N/A | 无外部依赖注入/Spring 上下文 |
| B002 | N/A | 无序列化场景 |
| B003 | N/A | 无 equals/hashCode 重写 |
| B004 | N/A | 无 clone 方法 |
| B005 | N/A | 无 finalize 方法 |
| B006 | N/A | 无 Comparable 实现 |
| B007 | N/A | 无枚举比较 |
| B008 | N/A | 无 BigDecimal 使用 |
| B009 | N/A | 无浮点运算 |
| B010 | N/A | 无除零运算 |
| B011 | N/A | 无数组访问 |
| B012 | N/A | 无集合迭代修改 |
| B013 | N/A | 无锁操作 |
| B014 | N/A | 无 wait/notify |
| B015 | N/A | 无 SimpleDateFormat |
| B016 | N/A | 无随机数使用 |
| B017 | N/A | 无正则编译 |
| B018 | N/A | 无反射调用 |
| B019 | N/A | 无类加载器 |
| B020 | N/A | 无系统退出 |
| B021 | N/A | 无 Runtime.exec |
| B022 | N/A | 无线程创建 |
| B023 | N/A | 无 ThreadLocal |
| B024 | N/A | 无 IO 流操作 |
| B025 | N/A | 无 JDBC 连接 |
| B026 | N/A | 无 Socket 连接 |
| B027 | N/A | 无 finally return |
| B028 | N/A | 无异常吞没（仅抛 IllegalArgumentException） |
| B029 | N/A | 无泛型裸类型 |
| B030 | N/A | 无集合原始类型 |
| B031 | N/A | 无 Map keySet 遍历 |
| B032 | N/A | 无 subList 修改 |
| B033 | N/A | 无 toArray 无参 |
| B034 | N/A | 无 asList 后修改 |
| B035 | N/A | 无 foreach 删除 |
| B036 | N/A | 无 split 点号 |
| B037 | N/A | 无字符串拼接 |
| B038 | N/A | 无包装类比较 |
| B039 | N/A | 无自动拆箱 NPE |
| B040 | N/A | 无 switch 穿透 |
| B041 | N/A | 无 switch 枚举 default |
| B042 | N/A | 无循环内变量声明 |
| B043 | N/A | 无静态变量集合 |
| B044 | N/A | 无 HashMap 初始化大小 |
| B045 | N/A | 无接口常量 |
| B046 | N/A | 无抽象类命名 |
| B047 | N/A | 无包名规范 |
| B048 | N/A | 无类名冲突 |
| B049 | N/A | 无方法过长 |
| B050 | N/A | 无参数过多 |
| B051 | N/A | 无过深嵌套 |
| B052 | N/A | 无魔法值 |
| B053 | N/A | 无注释代码 |
| B054 | N/A | 无空 catch |
| B055 | N/A | 无 System.out |
| B056 | N/A | 无 printStackTrace |
| B057 | N/A | 无异常不处理 |
| B058 | N/A | 无事务注解 |
| B059 | N/A | 无 ORM 映射 |
| B060 | N/A | 无 SQL 拼接 |
| B061 | N/A | 无 MyBatis ${} |
| B062 | N/A | 无分页查询 |
| B063 | N/A | 无索引提示 |
| B064 | N/A | 无表字段类型 |
| B065 | N/A | 无多表关联 |
| B066 | N/A | 无 count(*) |
| B067 | N/A | 无 IS NULL |
| B068 | N/A | 无 OR 条件 |
| B069 | N/A | 无 LIKE 前缀 |
| B070 | N/A | 无 NOT IN |
| B071 | N/A | 无函数索引 |
| B072 | N/A | 无隐式转换 |
| B073 | N/A | 无 ORDER BY |
| B074 | N/A | 无 GROUP BY |
| B075 | N/A | 无 DISTINCT |
| B076 | N/A | 无 UNION |
| B077 | N/A | 无子查询 |
| B078 | N/A | 无存储过程 |
| B079 | N/A | 无触发器 |
| B080 | N/A | 无视图 |
| B081 | N/A | 无外键 |
| M001 | N/A | 无 POJO 序列化 |
| M002 | N/A | 无日期格式化 |
| M003 | N/A | 无 Map 初始化 |
| M004 | N/A | 无 ArrayList 扩容 |
| M005 | N/A | 无线程池 |
| M006 | N/A | 无 ThreadLocal remove |
| M007 | N/A | 无计数器 |
| M008 | N/A | 无随机种子 |
| M009 | N/A | 无正则性能 |
| M010 | N/A | 无字符串 intern |
| M011 | N/A | 无并发集合 |
| M012 | N/A | 无锁粒度 |
| M013 | N/A | 无双重检查锁 |
| M014 | N/A | 无 volatile |
| M015 | N/A | 无 CountDownLatch |
| M016 | N/A | 无 CyclicBarrier |
| M017 | N/A | 无 Semaphore |
| M018 | N/A | 无 BlockingQueue |
| M019 | N/A | 无 CompletableFuture |
| M020 | N/A | 无 parallelStream |
| M021 | N/A | 无 Optional |
| M022 | N/A | 无 Stream |
| M023 | N/A | 无 Lambda |
| M024 | N/A | 无方法引用 |
| M025 | N/A | 无接口默认方法 |
| M026 | N/A | 无模块化 |
| M027 | N/A | 无记录类 |
| I001 | N/A | 无 Javadoc 缺失 |
| I002 | N/A | 无命名不规范 |
| I003 | N/A | 无过长方法 |
| I004 | N/A | 无过多参数 |
| I005 | N/A | 无嵌套过深 |
| I006 | N/A | 无魔法值 |
| I007 | N/A | 无注释代码 |
| I008 | N/A | 无导入未使用 |
| I009 | N/A | 无变量未使用 |
| I010 | N/A | 无死代码 |

### 4.2 可靠性（`reliability-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| G1.1 | N/A | 无事务/并发场景 |
| G1.2 | N/A | 无锁操作 |
| G1.3 | N/A | 无乐观锁 |
| G1.4 | N/A | 无多资源锁 |
| G2.1 | N/A | 无写接口 |
| G2.2 | N/A | 无重试/定时任务 |
| G2.3 | N/A | 无幂等键 |
| G3.1 | N/A | 无分布式事务 |
| G3.2 | N/A | 无 @Transactional |
| G4.1 | N/A | 无 SQL |
| G4.2 | N/A | 无 SQL |
| G4.3 | N/A | 无 SQL |
| G4.4 | N/A | 无 SQL |
| G5.1 | N/A | 无 MQ |
| G6.1 | N/A | 无缓存 |
| G6.2 | N/A | 无缓存 |
| G7.1 | N/A | 无调度任务 |
| G7.2 | N/A | 无调度任务 |
| G8.1 | N/A | 无 try-catch |
| G8.2 | N/A | 无外部依赖 |
| G8.3 | N/A | 无 IO 资源 |
| G8.4 | N/A | 无线程池 |
| G8.5 | N/A | 无 ThreadLocal |
| G8.6 | N/A | 无线程池 |
| G8.7 | N/A | 无 |
| G9.1 | N/A | 无网络调用 |
| G9.2 | N/A | 无网络调用 |
| G9.3 | N/A | 无重试 |
| G10.1 | N/A | 无 null 语义歧义 |
| G10.2 | N/A | 无接口契约变更 |
| G10.3 | N/A | 无 |
| G11.1 | ✅ | 新逻辑有全覆盖单测（5 个测试方法） |
| G11.2 | ✅ | 边界覆盖：null/空串/纯空格/正常值 |
| G11.3 | ✅ | greet(String) L36 对 null 和 blank 做防御性校验 |
| G11.4 | N/A | 无数值运算 |
| G12.1 | N/A | 无资金场景 |
| G12.2 | N/A | 无资金场景 |
| G13.1 | N/A | 无日志输出 |
| G14.1 | N/A | 无金额 |
| G14.2 | N/A | 无多租户 |
| G14.3 | N/A | 无时区 |
| G14.4 | N/A | 无日期格式化 |
| G15.1 | N/A | 无数据库 |
| G15.2 | N/A | 无接口共存 |
| G15.3 | N/A | 无开关 |
| G16.1 | N/A | 无核心链路 |
| G16.2 | N/A | 无异常路径日志 |
| G16.3 | N/A | 无日志 |
| G16.4 | N/A | 无 catch 块 |
| G17.1 | N/A | 无功能开关 |
| G17.2 | N/A | 无降级 |
| G17.3 | N/A | 无数据变更 |
| G18.1 | N/A | 无 |
| G18.2 | N/A | 无 |
| G18.3 | N/A | 无 |

### 4.3 安全（`security-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| S1.1 | N/A | 无 SQL |
| S1.2 | N/A | 无 SQL |
| S1.3 | N/A | 无 SQL |
| S2.1 | N/A | 无 Web 输出 |
| S2.2 | N/A | 无 Web 输出 |
| S2.3 | N/A | 无模板引擎 |
| S3.1 | N/A | 无外部 URL 请求 |
| S3.2 | N/A | 无外部 URL 请求 |
| S3.3 | N/A | 无外部 URL 请求 |
| S4.1 | N/A | 无命令执行 |
| S4.2 | N/A | 无命令执行 |
| S5.1 | N/A | 无 XML |
| S5.2 | N/A | 无 XPath |
| S6.1 | N/A | 无反序列化 |
| S6.2 | N/A | 无 JSON 反序列化 |
| S6.3 | N/A | 无敏感字段 |
| S7.1 | N/A | 无文件上传 |
| S7.2 | N/A | 无文件上传 |
| S7.3 | N/A | 无文件上传 |
| S8.1 | N/A | 无 Web 接口 |
| S8.2 | N/A | 无 Web 接口 |
| S8.3 | N/A | 无 Web 接口 |
| S8.4 | N/A | 无 Cookie |
| S9.1 | N/A | 无密钥/凭证 |
| S9.2 | N/A | 无日志 |
| S9.3 | N/A | 无传输 |
| S9.4 | N/A | 无随机数 |
| S10.1 | N/A | 无 CSRF |
| S10.2 | N/A | 无 CORS |
| S10.3 | N/A | 无 URL 跳转 |

---

## Step 5 — 自定义扩展检查（产物 E）

### 5.1 自定义扩展（`customized-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| U1.1 | N/A(未启用自定义规则) | 示例项，非项目实际启用 |
| U1.2 | N/A(未启用自定义规则) | 无 |
| U1.3 | N/A(未启用自定义规则) | 无 |
| U2.1 | N/A(未启用自定义规则) | 无 |
| U2.2 | N/A(未启用自定义规则) | 无 |
| U2.3 | N/A(未启用自定义规则) | 无 |

---

## 终检（防漏检）

- [x] 执行队列中每个文件 `Step2`、`Step3`、**S1–S10 / G1–G17** 各列均非 `⬜`（跳过文件除外）；
- [x] Step 2 的每个 REQ/Scenario 均非 `⬜`
- [x] Step 3 的 A1–A7 均非 `⬜`
- [x] Step 4 全部 **G/S** 与 **B001–B081 / M001–M027 / I001–I010** ID 均非 `⬜`（允许 `N/A`，但有原因）
- [x] Step 5 全部 U* ID 均非 `⬜`（允许 `N/A(未启用自定义规则)`）
- [x] 所有 `❌/⚠️` 已写入 report，且包含 `ID + path:line` — 无 ❌/⚠️，无需写入