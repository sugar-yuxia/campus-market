# 校园二手交易平台（Campus Market）

一个面向校园的二手闲置交易全栈项目：后端 Spring Boot + MyBatis + MySQL + Redis，前端 Vue 3 + Element Plus。

![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-6DB33F?logo=springboot&logoColor=white)
![MyBatis](https://img.shields.io/badge/MyBatis-3.0.4-1B6B8C)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?logo=redis&logoColor=white)
![Vue](https://img.shields.io/badge/Vue-3.5-4FC08D?logo=vuedotjs&logoColor=white)
![Element Plus](https://img.shields.io/badge/Element%20Plus-2.9-409EFF)

## 项目简介

校园里每年都会产生大量闲置物品（教材、数码、生活用品），传统的方式是在微信群/贴吧发消息，信息分散且难以管理。本项目提供一个轻量的校园二手交易平台，覆盖「发布 - 浏览 - 下单 - 支付 - 确认收货」的完整链路。

- 后端提供约 22 个 RESTful API，统一响应结构、JWT 鉴权、全局异常处理与参数校验
- 前端包含首页、商品详情、发布/编辑、我的闲置、我的订单、个人中心共 7 个页面
- 使用 Redis 缓存热门商品榜单，首页热门区读取缓存，商品/订单状态变化时自动失效
- 下单使用「带状态条件的 UPDATE」原子锁商品，避免同一件商品被重复购买

## 功能特性

### 用户模块
- 注册、登录（BCrypt 密码 + JWT Token，有效期 72 小时）
- 个人资料查看与修改（昵称、手机号、头像）
- 查看卖家主页信息

### 商品模块
- 商品发布 / 编辑 / 删除 / 上架 / 下架
- 首页商品列表：分页、关键词搜索（标题 + 描述）、分类筛选、排序（最新 / 价格升 / 价格降 / 浏览量）
- 商品详情：多图浏览、成色、原价与折扣、卖家信息、浏览量统计
- 我的闲置：按状态筛选（在售 / 已下架 / 交易中 / 已售出）
- 图片上传：本地存储 + 静态资源访问，限制格式与 5MB 大小

### 订单模块
- 买家下单（校验商品状态、禁止购买自己的商品、原子锁定商品）
- 在线支付（模拟）、确认收货、取消订单（恢复商品在售）
- 买家 / 卖家双视角订单列表与状态筛选
- 订单明细保存商品标题、封面、成交价快照

### 性能与稳定性
- Redis 缓存热门商品（默认 5 分钟 TTL），Redis 不可用时自动降级为直接查库，首页不受影响
- 数据库索引覆盖商品状态、分类、卖家，订单买家 / 卖家
- 统一异常处理，避免把堆栈信息暴露给前端

## 技术栈

| 层次 | 技术 |
| --- | --- |
| 后端框架 | Spring Boot 3.5.6、Spring MVC |
| 持久层 | MyBatis 3.0.4（XML Mapper）、HikariCP |
| 数据库 | MySQL 8.0（生产）、H2（本地 dev 演示） |
| 缓存 | Redis（Lettuce 客户端，StringRedisTemplate + Jackson） |
| 鉴权 | JWT（jjwt 0.12.6）+ 自定义拦截器与 `@CurrentUser` 参数解析 |
| 密码 | spring-security-crypto（BCrypt） |
| 前端 | Vue 3.5、Vue Router 4、Axios、Element Plus 2.9、Vite 6 |

## 项目结构

```
校园二手交易平台
├── backend/                          # Spring Boot 后端
│   ├── pom.xml
│   └── src/main
│       ├── java/com/campus/market
│       │   ├── auth/                 # JWT、拦截器、@CurrentUser
│       │   ├── common/               # Result、PageResult、异常处理
│       │   ├── config/               # Web 配置、密码编码器
│       │   ├── constant/             # 商品/订单状态常量
│       │   ├── controller/           # 6 个 REST Controller
│       │   ├── dto/                  # 请求参数与查询条件
│       │   ├── entity/               # 6 张表对应实体
│       │   ├── mapper/               # MyBatis Mapper 接口
│       │   ├── service/              # 业务逻辑（认证/商品/订单/缓存/文件）
│       │   └── vo/                   # 视图对象（ProductVO、OrderVO…）
│       └── resources
│           ├── application.yml       # 公共配置（默认 dev profile）
│           ├── application-dev.yml   # H2 + Redis 本地演示
│           ├── application-mysql.yml # MySQL + Redis 生产配置
│           ├── mapper/*.xml          # MyBatis SQL
│           └── db/                   # 建表脚本 + 演示数据
├── frontend/                         # Vue 3 前端
│   ├── package.json
│   ├── vite.config.js                # /api、/uploads 代理到 8080
│   └── src
│       ├── api/index.js              # Axios 实例与接口封装
│       ├── router/index.js           # 路由与登录守卫
│       ├── store/user.js             # 登录态持久化
│       ├── components/               # 顶栏、商品卡片
│       ├── styles/index.css          # 主题变量与全局样式
│       └── views/                    # 7 个页面
└── docs/API.md                       # 接口文档
```

## 数据库设计（6 张表）

| 表名 | 说明 | 关键字段 |
| --- | --- | --- |
| `users` | 用户表 | `username`（唯一）、`password`（BCrypt）、`nickname`、`avatar`、`phone` |
| `categories` | 商品分类表 | `name`、`sort_order` |
| `products` | 商品表 | `title`、`price`、`original_price`、`category_id`、`seller_id`、`cover`、`condition_level`、`status`、`view_count` |
| `product_images` | 商品图片表 | `product_id`、`url`、`sort_order` |
| `orders` | 订单表 | `order_no`（唯一）、`product_id`、`buyer_id`、`seller_id`、`amount`、`status`、`paid_at`、`finished_at` |
| `order_items` | 订单明细表 | `order_id`、`product_id`、商品标题/封面/价格快照、`quantity` |

状态枚举：

- 商品状态：`ON_SALE`（在售）、`OFF_SHELF`（已下架）、`LOCKED`（交易中）、`SOLD`（已售出）
- 订单状态：`PENDING_PAY`（待付款）、`PAID`（待收货）、`COMPLETED`（已完成）、`CANCELLED`（已取消）

订单流转：

```
下单 → 商品 ON_SALE → LOCKED，订单 PENDING_PAY
支付 → PAID
确认收货 → COMPLETED，商品 LOCKED → SOLD
取消（仅待付款）→ CANCELLED，商品 LOCKED → ON_SALE
```

## 快速开始

### 环境要求

- JDK 21+、Maven 3.8+
- Node.js 18+、npm
- MySQL 8.0、Redis 6+（仅生产 profile 需要）

### 方式一：本地演示（H2 内存库，开箱即用）

无需安装 MySQL，启动时自动建表并写入演示数据：

```bash
cd backend
mvn spring-boot:run          # 默认 dev profile，端口 8080

cd ../frontend
npm install
npm run dev                  # http://localhost:5173
```

> 若本机没有 Redis，热门商品接口会自动降级查库并在日志中打印一条 WARN，不影响功能。

### 方式二：MySQL + Redis（生产配置）

1. 初始化数据库（在 MySQL 客户端中按顺序执行）：

```bash
mysql -u root -p < backend/src/main/resources/db/mysql-schema.sql
mysql -u root -p campus_market < backend/src/main/resources/db/data.sql
```

2. 修改 `backend/src/main/resources/application-mysql.yml` 中的数据库账号、密码、Redis 地址。

3. 以 mysql profile 启动：

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

前端仍使用 `npm run dev`，Vite 会把 `/api`、`/uploads` 代理到 `http://localhost:8080`。

### 打包部署

```bash
cd backend && mvn clean package        # 产物 target/campus-market-1.0.0.jar
java -jar target/campus-market-1.0.0.jar --spring.profiles.active=mysql

cd frontend && npm run build           # 产物 dist/，交由 Nginx 托管并反向代理 /api、/uploads
```

### 演示账号

| 账号 | 密码 | 角色 |
| --- | --- | --- |
| `demo` | `123456` | 买家 |
| `seller` | `123456` | 卖家（演示数据中的商品发布者） |

## 配置说明

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `app.jwt.secret` | 内置示例值 | JWT 签名密钥，生产环境务必替换（≥32 字节） |
| `app.jwt.expire-hours` | `72` | Token 有效期（小时） |
| `app.upload-dir` | `./uploads` | 图片存储目录，通过 `/uploads/**` 访问 |
| `app.hot-products.limit` | `8` | 热门商品条数 |
| `app.hot-products.ttl-minutes` | `5` | 热门缓存过期时间（分钟） |

## 接口文档

完整的请求参数与响应示例见 [docs/API.md](docs/API.md)。

接口概览（共 22 个）：

| 模块 | 方法与路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| 认证 | `POST /api/auth/register` | 注册并返回 Token | 否 |
| 认证 | `POST /api/auth/login` | 登录 | 否 |
| 用户 | `GET /api/users/me` | 当前用户资料 | 是 |
| 用户 | `PUT /api/users/me` | 修改昵称/头像/手机号 | 是 |
| 用户 | `GET /api/users/{id}` | 用户公开资料 | 否 |
| 分类 | `GET /api/categories` | 分类列表 | 否 |
| 商品 | `GET /api/products` | 分页搜索商品 | 否 |
| 商品 | `GET /api/products/hot` | 热门商品（Redis） | 否 |
| 商品 | `GET /api/products/mine` | 我的闲置 | 是 |
| 商品 | `GET /api/products/{id}` | 商品详情（浏览量 +1） | 否 |
| 商品 | `POST /api/products` | 发布商品 | 是 |
| 商品 | `PUT /api/products/{id}` | 编辑商品 | 是（仅本人） |
| 商品 | `PUT /api/products/{id}/status` | 上架 / 下架 | 是（仅本人） |
| 商品 | `DELETE /api/products/{id}` | 删除商品 | 是（仅本人） |
| 上传 | `POST /api/uploads/image` | 上传商品图片 | 是 |
| 订单 | `POST /api/orders` | 下单 | 是 |
| 订单 | `GET /api/orders/bought` | 我买到的 | 是 |
| 订单 | `GET /api/orders/sold` | 我卖出的 | 是 |
| 订单 | `GET /api/orders/{id}` | 订单详情 | 是（买卖双方） |
| 订单 | `PUT /api/orders/{id}/pay` | 支付 | 是（买家） |
| 订单 | `PUT /api/orders/{id}/confirm` | 确认收货 | 是（买家） |
| 订单 | `PUT /api/orders/{id}/cancel` | 取消订单 | 是（买卖双方） |

## 关键实现说明

- **鉴权**：`AuthInterceptor` 解析 `Authorization: Bearer <token>`，白名单放行商品浏览、分类、登录注册等匿名接口；其余接口未登录返回 401，控制器通过 `@CurrentUser Long userId` 获取当前用户。
- **Redis 缓存**：`HotProductService` 采用 Cache-Aside，读取失败即降级查库；发布/编辑/上下架/删除/下单/支付/确认/取消都会清除缓存 Key。
- **下单防并发**：`UPDATE products SET status='LOCKED' WHERE id=? AND status='ON_SALE'`，以更新行数判断抢单结果，避免重复购买。
- **图片上传**：`FileStorageService` 校验 MIME、扩展名与大小，UUID 重命名落盘，`WebConfig` 将 `/uploads/**` 映射到本地目录。
- **前端登录态**：Token 与用户信息存 `localStorage`，Axios 请求拦截器自动携带；响应 401 时清理登录态并跳转登录页（带 redirect 参数）。

## License

仅用于学习与作品展示。
