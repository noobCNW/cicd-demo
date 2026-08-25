# cicd-demo (javademo)

Java 17 + Spring Boot 3 演示应用：**从 Nacos 配置中心读取配置**，通过 cicd-platform 拉取、构建、部署。

## 功能

- 启动后通过 nacos-client 连接 Nacos（地址取自平台注入的环境变量 `NACOS_SERVER_ADDR` / `NACOS_GROUP`），拉取 **demo.yaml** 配置
- 每 30s 主动刷新 + 注册监听器，Nacos 配置变更即时生效
- 接口：
  - `GET /` —— 服务名 + Nacos 连接信息 + 配置键值
  - `GET /config` —— demo.yaml 原始内容 + 解析结果
  - `GET /health` —— 健康检查

## 依赖的 Nacos 配置（demo.yaml）

```yaml
app.name: demo-java
message: hello-from-nacos
server.port: 8080
```

## 本地运行

```bash
# 需本地 Nacos 或设置 NACOS_SERVER_ADDR 指向远端
mvn -B -DskipTests package
java -jar target/javademo.jar
curl http://localhost:8080/
```

## 部署（cicd-platform）

1. 平台构建（pipeline：Maven 编译 → Docker 镜像 → 推送 registry）
2. 部署到环境（环境已启用 Nacos：`10.100.113.74:8848` / DEFAULT_GROUP / demo.yaml）
3. 平台自动注入 `NACOS_SERVER_ADDR`、`NACOS_GROUP` 环境变量并把 demo.yaml 挂载到 `/app/config`
