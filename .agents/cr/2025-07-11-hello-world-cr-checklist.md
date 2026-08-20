# Code Review Checklist

> **Change** `hello-world` · **分支/Commit** `AI/task-DEV-66a2f4f2` / `9b41f50` · **日期** `2025-07-11`

---

## Step 1：文件列表与执行队列

| # | 文件 | 状态 |
|---|------|:----:|
| 1 | `src/hello/HelloWorld.java` | ✅ 已审 |
| 2 | `src/hello/HelloWorldTest.java` | ⚠️ 已审有问题 |

---

## Step 2：功能性检查（REQ）

> 需求来源：`<requirement_section>` — "帮我写个hello world"

| ID | REQ 描述 | 关联文件 | 状态 |
|----|----------|----------|:----:|
| REQ-1 | 实现 HelloWorld 类，提供问候功能 | `HelloWorld.java:7` | ✅ |
| REQ-2 | `greet(String name)` 方法返回问候语 | `HelloWorld.java:21` | ✅ |
| REQ-3 | null/空白名称时返回默认问候 "Hello, World!" | `HelloWorld.java:22` | ✅ |
| REQ-4 | 有效名称返回个性化问候 "Hello, {name}!" | `HelloWorld.java:22-23` | ✅ |
| REQ-5 | 包含单元测试覆盖正常路径、null、空串、空白 | `HelloWorldTest.java` (4 tests) | ✅ |
| REQ-6 | 包含模块文档说明 | `docs/modules/hello/README.md` | ✅ |

---

## Step 3：可读性检查（A1–A7）

| ID | 规则 | 文件 | 状态 |
|----|------|------|:----:|
| A1.1 | 文件名 = 顶层类名 + `.java` | HelloWorld.java | ✅ |
| A1.1 | 文件名 = 顶层类名 + `.java` | HelloWorldTest.java | ✅ |
| A1.2 | 编码 UTF-8 | 全部 | ✅ |
| A1.3 | 空白仅允许 ASCII 空格和换行符，禁止 Tab | 全部 | ✅ |
| A2.1 | 文件顺序：package → import → 顶层类 | HelloWorld.java | ⚠️ P2 |
| A2.1 | 文件顺序：package → import → 顶层类 | HelloWorldTest.java | ⚠️ P2 |
| A2.2 | 禁止 `import *`（通配符引入） | HelloWorldTest.java:4 | ❌ P2 |
| A2.3 | import 分两组：静态 / 非静态，组间空行 | HelloWorldTest.java | ✅ |
| A2.4 | 每组内按 ASCII 字典序排列 | HelloWorldTest.java | ✅ |
| A3.1 | K&R 大括号 | 全部 | ✅ |
| A3.3 | 缩进 4 空格 | 全部 | ✅ |
| A3.4 | 行宽 ≤ 120 字符 | 全部 | ✅ |
| A3.6 | 类成员之间必须空行 | 全部 | ✅ |
| A3.7 | 关键字与 `(` 之间加空格 | 全部 | ✅ |
| A3.8 | 二元/三元运算符两侧加空格 | 全部 | ✅ |
| A4.2 | 类名 UpperCamelCase | 全部 | ✅ |
| A4.3 | 方法名 lowerCamelCase | 全部 | ✅ |
| A4.4 | 常量 UPPER_SNAKE_CASE | HelloWorld.java | ✅ |
| A4.7 | 测试类名 = 被测类名+Test | HelloWorldTest.java | ✅ |
| A5.1 | 重写方法必须加 `@Override` | 全部 | ✅ |
| A5.2 | catch 块不可为空 | 全部 | ✅ |
| A6.4 | 注解每行一个（类/方法） | HelloWorldTest.java | ✅ |
| A7.1 | public 类/成员必须有 Javadoc | HelloWorld.java | ✅ |
| A7.2 | 块标记顺序：@param → @return | HelloWorld.java | ✅ |

---

## Step 4：可靠性检查（G + S + B/M/I）

### G 可靠性（军规）

| ID | 描述 | 状态 |
|----|------|:----:|
| G1–G4 | 并发/幂等/事务/SQL | N/A（无相关场景） |
| G5 | 消息 MQ | N/A |
| G6 | 缓存 | N/A |
| G7 | 调度任务 | N/A |
| G8.3 | I/O 流/连接/锁释放 | N/A |
| G8.5 | ThreadLocal remove | N/A |
| G9 | 网络调用 | N/A |
| G10 | 接口契约 | N/A |
| G11.1 | 新逻辑有单测且断言 | ✅ |
| G11.2 | 覆盖边界：空、最大值 | ✅ |
| G11.3 | 入参空值有防御性校验 | ✅ |
| G11.4 | 数值运算溢出/除零 | N/A |
| G12 | 资损防控 | N/A |
| G13 | 监控核对 | N/A |
| G14 | 国际化/多租户/时区 | N/A |
| G15 | 可灰度 | N/A |
| G16 | 可监控 | N/A |
| G17 | 可应急 | N/A |

### S 安全

| ID | 描述 | 状态 |
|----|------|:----:|
| S1–S10 | 全部安全检查项 | N/A（HelloWorld 无外部输入/网络/DB/文件操作） |

### B/M/I Bug 模式（脚本扫描 + LLM 补充）

> 脚本已覆盖 52/222 条规则，仅发现 1 项。其余由 LLM 补充扫描。

| ID | 描述 | 状态 |
|----|------|:----:|
| A2.2 | WildcardImport | ❌ `HelloWorldTest.java:4` [脚本] |

---

## Step 5：自定义扩展检查（U）

| ID | 描述 | 状态 |
|----|------|:----:|
| U1.1 | Controller 入参 @Valid | N/A（非 Controller） |
| U2 | 业务红线 | N/A（未启用） |

> **结论**：Step 5 — N/A（未启用自定义规则）