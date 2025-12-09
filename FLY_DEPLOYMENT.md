# Fly.io 部署指南

本指南将帮助你将 HaloLight API Java 部署到 Fly.io。

## 前置要求

1. 安装 [Fly CLI](https://fly.io/docs/hands-on/install-flyctl/)
2. 注册 Fly.io 账号并登录：
   ```bash
   fly auth login
   ```

## 部署步骤

### 1. 验证 Docker 镜像可用性（可选）

在部署前，确认 Java 23 Alpine 镜像可用：

```bash
docker pull eclipse-temurin:23-jre-alpine
```

如果镜像不可用，请修改 `Dockerfile` 第 14 行：
```dockerfile
# 替换为非 Alpine 版本
FROM eclipse-temurin:23-jre
```

### 2. 创建 Fly 应用

```bash
# 在项目根目录执行
fly launch --no-deploy

# 或者如果 fly.toml 已存在
fly apps create halolight-api-java
```

**重要配置选项：**
- Region: 选择离你用户最近的区域（如 `hkg` 香港、`sin` 新加坡）
- Database: 选择 "No" （我们将手动创建 PostgreSQL）

### 3. 创建 PostgreSQL 数据库

```bash
# 创建 Fly Postgres 实例
fly postgres create --name halolight-db --region hkg

# 或者使用 Neon/Supabase 等外部数据库
```

如果使用 Fly Postgres，获取连接信息：
```bash
fly postgres connect -a halolight-db
```

### 4. 设置 Secrets（环境变量）

```bash
# 数据库配置
fly secrets set DATABASE_URL="jdbc:postgresql://halolight-db.internal:5432/halolight"
fly secrets set DATABASE_USERNAME="postgres"
fly secrets set DATABASE_PASSWORD="your-secure-password"

# JWT 配置（生成 256 位随机密钥）
fly secrets set JWT_SECRET="$(openssl rand -base64 32)"

# 可选配置
fly secrets set SPRING_PROFILES_ACTIVE="prod"
fly secrets set JWT_EXPIRATION="86400000"
fly secrets set JWT_REFRESH_EXPIRATION="604800000"
fly secrets set CORS_ALLOWED_ORIGINS="https://yourdomain.com,https://www.yourdomain.com"
```

### 5. 部署应用

```bash
fly deploy
```

首次部署可能需要 5-10 分钟（Maven 构建 + Docker 镜像构建）。

### 6. 验证部署

```bash
# 查看部署状态
fly status

# 查看健康检查
fly checks list

# 访问应用
fly open

# 查看日志
fly logs
```

访问以下端点验证：
- 健康检查：`https://your-app.fly.dev/actuator/health`
- API 文档：`https://your-app.fly.dev/swagger-ui.html`
- Prometheus 指标：`https://your-app.fly.dev/actuator/prometheus`

## 常用命令

```bash
# 查看应用信息
fly info

# 查看实时日志
fly logs -a halolight-api-java

# SSH 进入容器
fly ssh console

# 扩容（调整实例数量）
fly scale count 3

# 调整 VM 大小
fly scale vm shared-cpu-2x --memory 2048

# 查看密钥
fly secrets list

# 回滚到上一个版本
fly releases
fly deploy --image <previous-image>
```

## 配置优化

### 生产环境建议

**fly.toml 优化：**
```toml
[vm]
  size = "shared-cpu-2x"  # 升级到 2 核
  memory_mb = 2048        # 2GB 内存

[machines]
  min_machines_running = 2  # 保证高可用
  max_machines_running = 10 # 限制最大实例数
```

**JVM 参数优化：**

修改 `Dockerfile` 最后一行：
```dockerfile
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:InitialRAMPercentage=50.0", \
  "-Xlog:gc*:stdout:time", \
  "-jar", "app.jar"]
```

### 数据库连接池配置

在 `application-prod.properties` 或环境变量中：
```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
```

## 监控和告警

### 查看指标

```bash
# 查看 CPU/内存使用
fly dashboard

# Prometheus 指标
curl https://your-app.fly.dev/actuator/prometheus
```

### 配置告警

在 Fly.io Dashboard 配置：
- 健康检查失败告警
- CPU 使用率超过 80%
- 内存使用率超过 85%
- 请求延迟超过 5s

## 故障排查

### 健康检查失败

```bash
# 查看日志
fly logs

# 检查健康端点
fly ssh console
wget -O- http://localhost:8080/actuator/health
```

### 内存不足（OOMKilled）

```bash
# 增加内存
fly scale memory 2048

# 或调整 JVM 堆大小
# 在 Dockerfile ENTRYPOINT 添加：
# -Xmx1536m -Xms768m
```

### 数据库连接失败

```bash
# 验证数据库连接
fly ssh console
nc -zv halolight-db.internal 5432

# 检查环境变量
fly ssh console -C "env | grep DATABASE"
```

### 构建失败

```bash
# 本地测试构建
docker build -t halolight-api-java .

# 清理 fly 缓存
fly deploy --no-cache
```

## 成本估算

基于 Fly.io 定价（2025 年）：

| 配置 | 每月成本（USD） |
|------|----------------|
| shared-cpu-1x (1GB RAM) × 2 实例 | ~$15 |
| shared-cpu-2x (2GB RAM) × 2 实例 | ~$30 |
| Fly Postgres (单节点) | ~$5-15 |
| 流量（前 100GB 免费） | $0.02/GB |

**提示：** 使用 `auto_stop_machines = true` 可在空闲时自动停机，节省成本。

## 安全最佳实践

1. **永远不要提交敏感信息**：
   - 使用 `fly secrets` 管理密钥
   - 不要在 `fly.toml` 中硬编码密码

2. **使用 HTTPS**：
   - Fly.io 自动提供免费 SSL 证书
   - 配置 `force_https = true`

3. **限流保护**：
   - 项目已集成 Bucket4j 限流
   - 根据需要调整速率限制

4. **数据库安全**：
   - 使用强密码
   - 启用 SSL 连接
   - 定期备份

5. **监控和日志**：
   - 启用 Actuator 健康检查
   - 集成 Prometheus 监控
   - 配置日志聚合（Papertrail/Logflare）

## 外部数据库选项

如果不使用 Fly Postgres，可以使用：

### Neon（推荐）
```bash
# 在 Neon Console 创建数据库
# 获取连接字符串，格式：postgresql://user:pass@host/db
fly secrets set DATABASE_URL="jdbc:postgresql://ep-xxx.neon.tech/halolight?sslmode=require"
```

### Supabase
```bash
# 在 Supabase Dashboard 获取连接信息
fly secrets set DATABASE_URL="jdbc:postgresql://db.xxx.supabase.co:5432/postgres?sslmode=require"
```

### Railway
```bash
# 在 Railway 创建 PostgreSQL 服务
fly secrets set DATABASE_URL="jdbc:postgresql://containers-us-west-xxx.railway.app:6543/railway"
```

## 下一步

- [ ] 配置自定义域名：`fly certs add yourdomain.com`
- [ ] 设置 CI/CD：参考 `.github/workflows/`
- [ ] 配置数据库备份策略
- [ ] 集成日志聚合服务
- [ ] 配置 APM（Application Performance Monitoring）

## 参考资料

- [Fly.io Documentation](https://fly.io/docs/)
- [Spring Boot Deployment Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [Fly.io Postgres](https://fly.io/docs/postgres/)
- [Project README](./README.md)
