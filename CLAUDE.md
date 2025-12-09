# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

HaloLight API Java 是基于 Spring Boot 3.4.1 + Java 23 构建的企业级后端 API，与 NestJS 版本共用同一数据库（PostgreSQL/Neon）和接口规范，为 HaloLight 多框架管理后台生态系统提供服务端支持。

## 技术栈速览

- **框架**: Spring Boot 3.4.1 + Java 23
- **安全**: Spring Security 6.x + JWT (JJWT 0.12.6)
- **ORM**: Spring Data JPA + PostgreSQL 16（开发环境可用 H2）
- **认证**: JWT 双令牌机制 (AccessToken + RefreshToken)
- **权限**: RBAC 角色权限控制
- **文档**: Springdoc OpenAPI 2.7.0
- **映射**: MapStruct 1.6.3 + Lombok 1.18.38
- **缓存**: Caffeine 本地缓存
- **限流**: Bucket4j 8.10.1
- **可观测**: Spring Actuator + Micrometer + Prometheus
- **构建工具**: Maven 3.9+

## 常用命令

```bash
# 开发
mvn spring-boot:run                 # 启动开发服务器（默认 http://localhost:8000）
mvn spring-boot:run -Dspring-boot.run.profiles=prod  # 生产配置启动

# 构建
mvn clean package                   # 构建 JAR 包
java -jar target/halolight-api-java-1.0.0.jar  # 运行构建产物

# 代码质量
mvn test                            # 运行测试
mvn test -Dtest=UserServiceTest     # 运行指定测试类
mvn verify                          # 测试 + 集成测试
mvn clean test jacoco:report        # 生成覆盖率报告（target/site/jacoco/）

# Docker
docker-compose up -d                # 启动 PostgreSQL + API
docker build -t halolight-api-java . # 构建 Docker 镜像
docker-compose down                 # 停止所有服务
```

## 架构

### 模块结构

项目采用经典分层架构：

```
src/main/java/com/halolight/
├── HalolightApplication.java       # 应用入口
├── controller/                     # REST 控制器层
│   ├── AuthController.java         # 认证端点（登录、注册、刷新令牌）
│   ├── UserController.java         # 用户管理
│   └── DashboardController.java    # 仪表盘统计
├── service/                        # 业务逻辑层
│   ├── AuthService.java
│   ├── UserService.java
│   ├── UserDetailsServiceImpl.java # Spring Security 用户服务
│   └── DashboardService.java
├── domain/                         # 领域层
│   ├── entity/                     # JPA 实体（User, Role, Document, File 等）
│   │   ├── enums/                  # 枚举类型（UserStatus, SharePermission 等）
│   │   └── id/                     # 复合主键类
│   └── repository/                 # JPA Repository 接口
├── web/dto/                        # 数据传输对象（按模块组织）
│   ├── auth/                       # 认证相关 DTO
│   ├── calendar/                   # 日历相关 DTO
│   ├── document/                   # 文档相关 DTO
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
│   ├── RateLimitFilter.java        # 限流过滤器
│   └── UserPrincipal.java          # Spring Security 用户主体
└── exception/                      # 异常处理
    ├── ApiException.java           # 自定义异常
    └── GlobalExceptionHandler.java # 全局异常处理器
```

### 核心设计模式

**认证流程:**
- JWT 认证通过 `JwtAuthenticationFilter` 拦截所有请求
- 公开端点在 `SecurityConfig` 中通过 `permitAll()` 放行
- 双令牌策略：AccessToken（24小时）+ RefreshToken（7天）
- RefreshToken 存储于数据库 `refresh_tokens` 表

**安全配置:**
- 无状态会话：`SessionCreationPolicy.STATELESS`
- CSRF 禁用（适用于 API 服务）
- 公开端点：`/api/auth/**`、`/api/health`、`/docs/**`、`/swagger-ui/**`、`/actuator/**`

**数据库访问:**
- 所有实体使用 JPA 注解映射
- 主键使用 `@Id` + cuid 字符串（与 NestJS 版本一致）
- 软删除通过 `deletedAt` 字段实现

**API 结构:**
- 全局前缀：无（直接使用 `/api/*`）
- Swagger UI：`/api/swagger-ui`
- OpenAPI JSON：`/docs`
- 健康检查：`/actuator/health`

### 数据流模式

1. **请求处理**: Controller → Service → Repository → Database
2. **认证拦截**: RateLimitFilter → JwtAuthenticationFilter → Controller
3. **异常处理**: GlobalExceptionHandler 统一捕获并格式化错误响应
4. **数据验证**: Jakarta Validation (`@Valid`, `@NotNull`, `@Email`)

## 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `DATABASE_URL` | PostgreSQL 连接 URL | `jdbc:postgresql://localhost:5432/halolight` |
| `DATABASE_USERNAME` | 数据库用户名 | `postgres` |
| `DATABASE_PASSWORD` | 数据库密码 | `postgres` |
| `JWT_SECRET` | JWT 签名密钥（≥256位） | - |
| `JWT_EXPIRATION` | AccessToken 过期时间（毫秒） | `86400000`（24小时） |
| `JWT_REFRESH_EXPIRATION` | RefreshToken 过期时间（毫秒） | `604800000`（7天） |
| `CORS_ALLOWED_ORIGINS` | CORS 允许源（逗号分隔） | `http://localhost:3000,http://localhost:5173` |
| `SPRING_PROFILES_ACTIVE` | 激活的配置文件 | `dev` |
| `PORT` | 服务端口 | `8000` |

支持 `.env` 文件配置（通过 spring-dotenv）。

## 代码规范

- **缩进**: 4 空格
- **命名**: 类使用 PascalCase，方法/变量使用 camelCase
- **注解**: 使用 Lombok 减少样板代码（`@Data`、`@Builder`、`@RequiredArgsConstructor`）
- **依赖注入**: 优先使用构造器注入（Lombok `@RequiredArgsConstructor`）
- **日志**: 使用 `@Slf4j` 注解
- **DTO**: 禁止直接暴露实体，使用 MapStruct 进行映射
- **提交规范**: 遵循 Conventional Commits（`feat:`、`fix:`、`docs:` 等）

## API 模块

项目覆盖 12 个核心业务模块，与 NestJS 版本保持接口一致：

| 模块 | 端点 | 描述 |
|------|------|------|
| **Auth** | `/api/auth/*` | 登录、注册、刷新令牌、登出、忘记/重置密码 |
| **Users** | `/api/users/*` | 用户 CRUD、状态更新、改密、批量操作 |
| **Roles** | `/api/roles/*` | 角色 CRUD、权限分配 |
| **Permissions** | `/api/permissions/*` | 权限管理 |
| **Documents** | `/api/documents/*` | 文档 CRUD、分享、标签、移动 |
| **Files** | `/api/files/*` | 文件上传、下载、收藏、批量操作 |
| **Folders** | `/api/folders/*` | 文件夹 CRUD、树形结构 |
| **Calendar** | `/api/calendar/*` | 日历事件、参会人、提醒 |
| **Teams** | `/api/teams/*` | 团队 CRUD、成员管理 |
| **Messages** | `/api/messages/*` | 会话、消息发送、已读标记 |
| **Notifications** | `/api/notifications/*` | 通知管理 |
| **Dashboard** | `/api/dashboard/*` | 仪表盘统计、图表数据 |

## 新增功能开发指南

### 添加新实体

1. 在 `domain/entity/` 创建 JPA 实体类
2. 在 `domain/repository/` 创建 Repository 接口
3. 运行应用，JPA 自动更新表结构（开发环境）

### 添加新 API 端点

1. 在 `web/dto/` 创建请求/响应 DTO
2. 在 `service/` 创建或修改 Service 类
3. 在 `controller/` 添加 Controller 方法
4. 添加 OpenAPI 注解（`@Operation`、`@ApiResponse`）
5. 如需权限控制，使用 `@PreAuthorize` 注解

### 添加缓存

```java
@Cacheable(value = "cacheName", key = "#id")
public SomeDTO findById(String id) { ... }

@CacheEvict(value = "cacheName", allEntries = true)
public void clearCache() { ... }
```

## 可观测性

- **健康检查**: `GET /actuator/health`
- **指标**: `GET /actuator/metrics`
- **Prometheus**: `GET /actuator/prometheus`
- **应用信息**: `GET /actuator/info`

## 与前端集成

配置前端 API 地址：
```env
# Next.js
NEXT_PUBLIC_API_URL=http://localhost:8000/api

# Vue/Vite
VITE_API_URL=http://localhost:8000/api
```
