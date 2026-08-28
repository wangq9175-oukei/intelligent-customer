# Intelligent Customer

`intelligent-customer` 是一个基于 Spring AI 的智能航空客服示例工程。它把大模型对话、航班预订业务工具、检索增强生成（RAG）和 MCP（Model Context Protocol）服务组合在一起，演示客服如何在对话中查询订单、理解退改签条款，并按条件调用外部工具。

> 这是教学与原型工程：订单数据存放在内存中，重启服务后会重新生成；不适合作为生产系统直接使用。

## 功能概览

- 中文流式客服对话：通过 SSE 返回模型生成内容，并保留会话上下文。
- 航班订单展示：查询演示订单的预订号、旅客、日期、航线、舱位和状态。
- 对话式订单查询与退订：模型可按“预订号 + 姓名”查询详情，退订前要求查询订单并取得用户确认。
- 业务规则：起飞前 48 小时内不可取消；距离起飞不足 24 小时不可改签。
- RAG 条款问答：启动时将退改签服务条款切分并写入内存向量库，为客服回答提供相关上下文。
- MCP 天气工具：提供 HTTP/WebFlux 与 STDIO 两种 MCP Server，可按经纬度查询当前天气、未来 7 天天气预报和演示空气质量数据。
- Vue 管理界面：展示订单表格与聊天时间线；对话结束后会自动刷新订单列表。

## 模块说明

| 模块 | 作用 |
| --- | --- |
| `flight-booking` | 主业务服务：智能客服、订单演示数据、RAG、订单工具与 REST/SSE 接口。 |
| `mcp-server` | 基于 WebFlux 的 MCP 天气服务，默认监听 `8088`。 |
| `mcp-stdio-server` | 基于标准输入输出（STDIO）的 MCP 天气服务。 |
| `spring-ai-demos` | Spring AI 文本切分、Embedding 与向量检索实验代码。 |
| `app/chatgpt-demo` | Vue 3 + Vite + Element Plus 前端界面。 |

## 技术栈

- Java 17、Spring Boot 3.5、Maven
- Spring AI、DeepSeek Chat Model、DashScope Embedding
- Spring AI MCP（WebFlux / STDIO）
- Vue 3、Vite、Element Plus、Axios

## 核心接口

主服务实际运行端口由 `flight-booking/src/main/resources/application.properties` 中的 `server.port=8081` 决定；该值会覆盖 `application.yml` 中的 `8080` 设置。

| 接口 | 说明 |
| --- | --- |
| `GET /booking/list` | 获取当前内存中的全部演示订单。 |
| `GET /ai/generateStreamAsString?message=...` | 以 `text/event-stream` 形式流式返回客服回复，结束标记为 `[complete]`。 |

示例：

```text
http://localhost:8081/booking/list
http://localhost:8081/ai/generateStreamAsString?message=查询预订号101、姓名徐庶的航班
```

## 运行准备

1. 安装 Java 17、Maven 和 Node.js。
2. 为 DeepSeek 对话模型和 DashScope Embedding 配置有效的 API Key。
3. 不要将密钥写入或提交到版本库。将 `flight-booking/src/main/resources/application.properties.example` 复制为 `application.properties`，并填入本地密钥；该本地文件已被 Git 忽略。
4. 如需由主服务调用 MCP 工具，启用 `flight-booking/src/main/resources/application.yml` 中当前被注释的 MCP Client 配置，并按本机环境调整 MCP 服务地址或 STDIO JAR 路径。

## 启动方式

### 1. 启动主服务

在工程根目录执行：

```bash
mvn -pl flight-booking -am spring-boot:run
```

服务启动后访问 `http://localhost:8081/booking/list` 检查订单接口。

### 2. 启动前端

```bash
cd app/chatgpt-demo
npm install
npm run dev
```

前端代码默认请求 `http://localhost:8081` 的主服务，请确保后端先启动。

### 3. 启动 MCP 天气服务（可选）

HTTP 传输方式：

```bash
mvn -pl mcp-server -am spring-boot:run
```

服务默认监听 `8088`。STDIO 传输方式则需要先打包 `mcp-stdio-server`，再由 MCP 客户端以子进程方式启动其 JAR。

## 当前限制与注意事项

- 订单数据和向量库均在内存中，应用重启后不会保留订单状态或对话之外的业务数据。
- 订单在启动时随机生成，示例预订号与航线会随重启改变。
- `changeBooking` 已在服务层实现，但当前聊天工具只暴露订单查询和取消预订。
- `mcp-servers-config.json` 中包含特定操作系统和旧工作区的路径，使用 STDIO MCP 前必须改为本机实际 JAR 路径。
- 空气质量工具会生成演示数据，不应被视作真实空气质量报告。

## 项目结构

```text
intelligent-customer/
├── flight-booking/       # 智能航空客服主服务
├── mcp-server/           # HTTP MCP 天气服务
├── mcp-stdio-server/     # STDIO MCP 天气服务
├── spring-ai-demos/      # Spring AI 实验代码
└── app/chatgpt-demo/     # Vue 前端
```
