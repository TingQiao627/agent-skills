# hello-world 模块

## 模块职责

提供标准 Hello World 问候语输出的示例模块，演示 Java 编码规范下的基础程序结构。

## 关键类

| 类名 | 类型 | 说明 |
|------|------|------|
| `HelloWorld` | 入口类 | 含 `main` 方法和 `getGreeting()` 业务方法 |
| `HelloWorldTest` | 测试类 | 覆盖 `getGreeting` 正常路径和 `main` 方法执行 |

## 依赖关系

无外部依赖，纯 Java SE 实现。

## API 接口列表

| 方法 | 签名 | 说明 |
|------|------|------|
| `getGreeting` | `String getGreeting()` | 返回默认问候语 `"Hello, World!"` |
| `main` | `static void main(String[] args)` | 程序入口，向标准输出打印问候语 |

## 文件清单

| 文件 | 路径 |
|------|------|
| HelloWorld.java | `src/main/java/com/example/helloworld/HelloWorld.java` |
| HelloWorldTest.java | `src/test/java/com/example/helloworld/HelloWorldTest.java` |