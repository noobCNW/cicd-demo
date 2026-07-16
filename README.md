# cicd-demo

用于验证 cicd-platform 构建与部署的演示应用。

## 本地运行

```bash
npm start
curl http://localhost:8080/health
```

## Docker

```bash
docker build -t cicd-demo:latest .
docker run --rm -p 8080:8080 cicd-demo:latest
```
