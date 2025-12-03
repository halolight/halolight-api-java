# HaloLight API | Java (Spring Boot)

[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/halolight/halolight-api-java/blob/main/LICENSE)
[![Java](https://img.shields.io/badge/Java-23-%23ED8B00.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-%236DB33F.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-%23336791.svg)](https://www.postgresql.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-%23C71A36.svg)](https://maven.apache.org/)

基于 Spring Boot 3.4.1 的企业级后端 API 实现，与 NestJS 版本共用同一数据库（PostgreSQL/Neon）和接口规范，支持 JWT 认证、RBAC 权限、Swagger 文档，为 HaloLight 多框架管理后台提供强大、可扩展的服务端支持。

- 在线预览：<http://halolight-api-java.h7ml.cn>
- API 文档：<http://halolight-api-java.h7ml.cn/api/swagger-ui>
- GitHub：<https://github.com/halolight/halolight-api-java>

## 功能亮点

- **Spring Boot 3.4.1 + Java 23**：现代化 Java 框架、依赖注入、注解驱动，企业级稳定性
- **Spring Data JPA + PostgreSQL 16**：类型安全的数据库访问、自动建表、关系管理
- **JWT 认证 + RBAC 权限**：AccessToken/RefreshToken 双令牌机制，支持角色权限控制
- **Springdoc OpenAPI 文档**：自动生成交互式 API 文档，支持在线测试与调试
- **12 个业务模块**：60+ RESTful API 端点，覆盖用户、角色、权限、文档、文件、日历、通知等
- **企业级架构**：分层设计、依赖注入、全局异常处理、请求验证、日志记录
- **可观测性**：Spring Actuator + Micrometer + Prometheus 指标监控
- **限流与缓存**：Bucket4j 限流 + Caffeine 本地缓存
- **Docker 部署**：多阶段构建优化、Docker Compose 一键部署、健康检查机制

## 目录结构

```
src/main/java/com/halolight/
├── controller/                     # REST 控制器层
│   ├── AuthController.java         # 认证端点（登录、注册、刷新令牌）
│   ├── UserController.java         # 用户管理
│   ├── DashboardController.java    # 仪表盘统计
│   └── ...                         # 其他业务控制器
├── service/                        # 业务逻辑层
│   ├── AuthService.java
│   ├── UserService.java
│   └── ...
├── domain/                         # 领域层
│   ├── entity/                     # JPA 实体（User, Role, Document 等 17 个）
│   │   ├── enums/                  # 枚举类型
│   │   └── id/                     # 复合主键类
│   └── repository/                 # JPA Repository 接口
├── web/dto/                        # 数据传输对象（按模块组织）
│   ├── auth/                       # 认证相关 DTO
│   ├── calendar/                   # 日历相关 DTO
│   └── ...
├── config/                         # 配置类
│   ├── SecurityConfig.java         # Spring Security 配置
│   ├── CorsConfig.java             # CORS 配置
│   ├── OpenApiConfig.java          # Swagger 配置
│   ├── CacheConfig.java            # Caffeine 缓存配置
│   └── RateLimitConfig.java        # 限流配置
├── security/                       # 安全组件
│   ├── JwtTokenProvider.java       # JWT 生成/验证
│   ├── JwtAuthenticationFilter.java # JWT 认证过滤器
│   └── RateLimitFilter.java        # 限流过滤器
├── exception/                      # 异常处理
│   └── GlobalExceptionHandler.java # 全局异常处理器
└── HalolightApplication.java       # 应用入口
src/main/resources/
├── application.yml                 # 主配置文件
├── application-dev.yml             # 开发环境配置
└── application-prod.yml            # 生产环境配置
src/test/                           # 测试文件
```

## 快速开始

环境要求：JDK >= 23、Maven >= 3.9、PostgreSQL >= 13（或 Neon）。

```bash
git clone https://github.com/halolight/halolight-api-java.git
cd halolight-api-java

# 配置环境变量（创建 .env 文件）
cat > .env <<'EOF'
DATABASE_URL=jdbc:postgresql://localhost:5432/halolight
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres
JWT_SECRET=your-super-secret-jwt-key-change-in-production-min-32-chars
SPRING_PROFILES_ACTIVE=dev
EOF

mvn spring-boot:run   # 本地开发，默认 http://localhost:8080
```

生产构建与启动

```bash
mvn clean package
java -jar target/halolight-api-java-1.0.0.jar
```

## 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `SPRING_PROFILES_ACTIVE` | 运行环境 | `dev` |
| `PORT` | 服务端口 | `8080` |
| `DATABASE_URL` | PostgreSQL 数据库连接 | `jdbc:postgresql://localhost:5432/halolight` |
| `DATABASE_USERNAME` | 数据库用户名 | `postgres` |
| `DATABASE_PASSWORD` | 数据库密码 | `postgres` |
| `JWT_SECRET` | JWT 密钥（≥256位） | - |
| `JWT_EXPIRATION` | AccessToken 过期时间（毫秒） | `86400000`（24小时） |
| `JWT_REFRESH_EXPIRATION` | RefreshToken 过期时间（毫秒） | `604800000`（7天） |
| `CORS_ALLOWED_ORIGINS` | CORS 允许源 | `http://localhost:3000,http://localhost:5173` |

在项目根目录创建 `.env` 文件来配置环境变量（支持 spring-dotenv）：

```bash
# .env 示例
SPRING_PROFILES_ACTIVE=prod
PORT=8080
DATABASE_URL=jdbc:postgresql://your-host:5432/halolight_db?sslmode=require
DATABASE_USERNAME=your_username
DATABASE_PASSWORD=your_password
JWT_SECRET=your-super-secret-jwt-key-change-in-production-min-32-chars
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://halolight.h7ml.cn
```

## 常用脚本

```bash
mvn spring-boot:run                 # 启动开发服务器
mvn clean package                   # 生产构建，输出到 target/
mvn test                            # 运行单元测试
mvn test -Dtest=UserServiceTest     # 运行指定测试类
mvn verify                          # 运行测试 + 集成测试
mvn clean test jacoco:report        # 生成覆盖率报告
```

## API 模块

项目包含 **12 个核心业务模块**，提供 **60+ RESTful API 端点**：

| 模块 | 端点数 | 描述 |
|------|--------|------|
| **Auth** | 7 | 用户认证（登录、注册、刷新 Token、登出、忘记/重置密码） |
| **Users** | 6 | 用户管理（CRUD、分页、搜索、状态更新、改密） |
| **Roles** | 6 | 角色管理（CRUD + 权限分配） |
| **Permissions** | 4 | 权限管理 |
| **Teams** | 6 | 团队管理（成员增删、角色更新） |
| **Documents** | 10 | 文档管理（支持标签、分享、移动） |
| **Files** | 10 | 文件管理（上传、下载、收藏、批量操作） |
| **Folders** | 5 | 文件夹管理（树形结构） |
| **Calendar** | 8 | 日历事件管理（参会人、提醒） |
| **Notifications** | 5 | 通知管理 |
| **Messages** | 5 | 消息会话 |
| **Dashboard** | 5 | 仪表盘统计 |

### 📖 在线文档

- **Swagger API 文档**：<http://halolight-api-java.h7ml.cn/api/swagger-ui> - 交互式 API 测试与调试
- **完整使用指南（中文）**：<https://halolight.docs.h7ml.cn/guide/api-java> - 详细的 API 参考和使用示例
- **完整使用指南（英文）**：<https://halolight.docs.h7ml.cn/en/guide/api-java> - Full API reference in English

## 代码规范

- **分层架构**：Controller → Service → Repository，职责清晰
- **依赖注入**：使用构造器注入（Lombok `@RequiredArgsConstructor`）
- **DTO 映射**：使用 MapStruct 进行实体与 DTO 转换，禁止直接暴露实体
- **类型安全**：严格的 Java 类型系统，确保类型完整性
- **测试规范**：单元测试覆盖核心业务逻辑，集成测试覆盖关键路径
- **提交规范**：遵循 Conventional Commits 规范（`feat:`, `fix:`, `docs:` 等）

## 可观测性

项目集成 Spring Actuator + Micrometer + Prometheus：

| 端点 | 说明 |
|------|------|
| `/actuator/health` | 健康检查（Liveness/Readiness） |
| `/actuator/metrics` | 应用指标 |
| `/actuator/prometheus` | Prometheus 格式指标 |
| `/actuator/info` | 应用信息 |

## 部署

### Docker Compose（推荐）

```bash
# 克隆项目
git clone https://github.com/halolight/halolight-api-java.git
cd halolight-api-java

# 配置环境变量
cp .env.example .env
# 编辑 .env 文件，设置数据库密码、JWT密钥等

# 启动所有服务（API + PostgreSQL）
docker-compose up -d

# 查看日志
docker-compose logs -f app

# 停止服务
docker-compose down
```

### Docker 镜像构建

```bash
docker build -t halolight-api-java .
docker run -p 8080:8080 --env-file .env halolight-api-java
```

### 自托管部署

1. **环境准备**：确保 JDK >= 23 和 Maven >= 3.9 已安装
2. **配置环境变量**：创建 `.env` 文件并设置必要变量
3. **构建项目**：
   ```bash
   mvn clean package -DskipTests
   ```
4. **启动服务**：
   ```bash
   java -jar target/halolight-api-java-1.0.0.jar
   ```
5. **进程守护**（可选）：使用 systemd 或 Docker 运行

### 云平台部署

- **AWS**：Elastic Beanstalk、ECS 或 EKS
- **GCP**：Cloud Run、GKE
- **Azure**：App Service、AKS

## 贡献

欢迎提交 Issue 和 Pull Request 来帮助改进项目！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'feat: add amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

## 相关链接

- [在线预览](http://halolight-api-java.h7ml.cn)
- [API 文档](http://halolight-api-java.h7ml.cn/api/swagger-ui)
- [HaloLight 文档](https://github.com/halolight/docs)
- [HaloLight Next.js](https://github.com/halolight/halolight)
- [HaloLight Vue](https://github.com/halolight/halolight-vue)
- [HaloLight API NestJS](https://github.com/halolight/halolight-api-nestjs)
- [问题反馈](https://github.com/halolight/halolight-api-java/issues)

## 许可证

[MIT](LICENSE)
