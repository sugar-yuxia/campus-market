# 接口文档（Campus Market API）

- Base URL：`http://localhost:8080/api`
- 数据格式：`application/json;charset=UTF-8`
- 鉴权方式：请求头 `Authorization: Bearer <token>`（除标注「公开」外均需携带）
- 时间格式：`yyyy-MM-dd HH:mm:ss`

## 统一响应结构

```json
{
  "code": 200,
  "message": "ok",
  "data": { }
}
```

| code | 含义 |
| --- | --- |
| 200 | 成功 |
| 400 | 参数校验失败 / 业务约束不满足 |
| 401 | 未登录或 Token 失效、用户名密码错误 |
| 403 | 无权操作（如操作他人商品、查看他人订单） |
| 404 | 资源不存在 |
| 409 | 并发冲突（商品已被他人下单） |
| 500 | 服务端异常 |

> 业务错误（BizException、参数校验）以 HTTP 200 返回，`code` 为具体业务码；仅拦截器鉴权失败返回 HTTP 401。

## 分页结构

```json
{
  "total": 9,
  "page": 1,
  "size": 12,
  "records": []
}
```

`page` 从 1 开始，`size` 最大 50。

---

# 一、认证与用户

## 1. 注册

`POST /api/auth/register`（公开）

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| username | string | 是 | 3-20 位字母、数字或下划线 |
| password | string | 是 | 6-20 位 |
| nickname | string | 否 | 最多 20 字符，缺省等于用户名 |

请求示例：

```json
{ "username": "student01", "password": "123456", "nickname": "小明" }
```

响应示例：

```json
{
  "code": 200,
  "message": "ok",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1003,
      "username": "student01",
      "nickname": "小明",
      "avatar": null,
      "phone": null,
      "createdAt": "2026-10-29 18:31:00"
    }
  }
}
```

## 2. 登录

`POST /api/auth/login`（公开）

```json
{ "username": "demo", "password": "123456" }
```

响应结构同上（`token` + `user`）。用户名或密码错误返回 `code: 401`。

## 3. 获取当前用户资料

`GET /api/users/me`

响应 `data`：

```json
{
  "id": 1001,
  "username": "demo",
  "nickname": "校园小买家",
  "avatar": null,
  "phone": "13800000001",
  "createdAt": "2026-09-01 09:00:00"
}
```

## 4. 修改个人资料

`PUT /api/users/me`

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| nickname | string | 否 | 最多 20 字符 |
| avatar | string | 否 | 图片地址（一般由上传接口返回） |
| phone | string | 否 | 0-20 位数字 |

响应为更新后的用户资料。未传字段保持不变。

## 5. 查看用户公开资料

`GET /api/users/{id}`（公开）

响应同上（不含密码）。

---

# 二、分类

## 6. 分类列表

`GET /api/categories`（公开）

```json
{
  "code": 200,
  "message": "ok",
  "data": [
    { "id": 1, "name": "教材书籍", "sortOrder": 1 },
    { "id": 2, "name": "数码电子", "sortOrder": 2 }
  ]
}
```

---

# 三、商品

## 7. 商品列表（分页 / 搜索 / 筛选 / 排序）

`GET /api/products`（公开）

| 参数 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| page | int | 1 | 页码 |
| size | int | 10 | 每页数量（1-50） |
| keyword | string | - | 标题或描述模糊匹配 |
| categoryId | long | - | 分类 id |
| sort | string | newest | `newest` / `priceAsc` / `priceDesc` / `hot` |

- 仅返回 `ON_SALE` 商品
- 请求示例：`GET /api/products?page=1&size=12&keyword=键盘&categoryId=2&sort=priceAsc`

响应 `data` 为分页结构，`records` 元素字段：

```json
{
  "id": 1002,
  "title": "罗技 K845 机械键盘 九成新",
  "description": "青轴手感清脆……",
  "price": 129.00,
  "originalPrice": 349.00,
  "categoryId": 2,
  "categoryName": "数码电子",
  "sellerId": 1002,
  "sellerNickname": "清仓学长",
  "sellerAvatar": null,
  "cover": "/uploads/xxx.jpg 或外链",
  "conditionLevel": 9,
  "status": "ON_SALE",
  "viewCount": 203,
  "createdAt": "2026-10-21 14:20:00",
  "updatedAt": "2026-10-21 14:20:00"
}
```

## 8. 热门商品

`GET /api/products/hot`（公开）

按浏览量倒序取前 8 条（`ON_SALE`），优先读取 Redis 缓存（Key：`campus:market:hot:products`，TTL 5 分钟）。`data` 为商品数组。

## 9. 我的闲置

`GET /api/products/mine`（鉴权）

| 参数 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| page | int | 1 | 页码 |
| size | int | 10 | 每页数量 |
| status | string | - | 不传则返回全部状态 |

返回当前用户发布的商品（含 `OFF_SHELF` / `LOCKED` / `SOLD`）。

## 10. 商品详情

`GET /api/products/{id}`（公开）

返回单个商品，额外包含 `images`（按 `sort_order` 排序的图片地址数组）；每次调用浏览量 `viewCount` +1。商品不存在返回 `code: 404`。

## 11. 发布商品

`POST /api/products`（鉴权）

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| title | string | 是 | 最多 60 字符 |
| description | string | 否 | 最多 2000 字符 |
| price | number | 是 | ≥ 0.01 |
| originalPrice | number | 否 | ≥ 0.01 |
| categoryId | long | 是 | 必须存在 |
| conditionLevel | int | 是 | 1-10，10 表示全新 |
| images | string[] | 否 | 最多 9 个地址，第一张作为封面 |

```json
{
  "title": "宿舍 LED 护眼台灯",
  "description": "三档亮度，USB 供电",
  "price": 25.00,
  "originalPrice": 79.00,
  "categoryId": 3,
  "conditionLevel": 9,
  "images": ["/uploads/a.png", "/uploads/b.png"]
}
```

成功返回商品详情；发布后自动清除热门商品缓存。

## 12. 编辑商品

`PUT /api/products/{id}`（鉴权，仅发布者）

请求体同「发布商品」。`LOCKED` / `SOLD` 状态不可编辑；`images` 传 `null` 表示不修改图片，传空数组表示清空图片。

## 13. 上架 / 下架

`PUT /api/products/{id}/status`（鉴权，仅发布者）

```json
{ "status": "OFF_SHELF" }
```

`status` 仅支持 `ON_SALE` / `OFF_SHELF`；商品处于 `LOCKED` / `SOLD` 时返回 403。

## 14. 删除商品

`DELETE /api/products/{id}`（鉴权，仅发布者）

仅 `ON_SALE` / `OFF_SHELF` 状态可删除，同时删除关联图片并清除热门缓存。

---

# 四、图片上传

## 15. 上传图片

`POST /api/uploads/image`（鉴权，`multipart/form-data`）

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| file | file | 是 | jpg / jpeg / png / gif / webp，≤ 5MB |

```json
{ "code": 200, "message": "ok", "data": { "url": "/uploads/0ac57a6f96e74ec4978d2d8338b6a6ed.png" } }
```

返回地址可直接用于商品 `images`、用户 `avatar`；通过 `GET /uploads/**` 访问。

---

# 五、订单

## 16. 下单

`POST /api/orders`（鉴权，买家）

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| productId | long | 是 | 商品 id |
| remark | string | 否 | 买家留言，最多 200 字符 |

```json
{ "productId": 1001, "remark": "周三下午图书馆门口" }
```

约束：不能购买自己的商品；商品必须为 `ON_SALE`；下单成功后商品变为 `LOCKED`，重复下单返回 409。

响应示例：

```json
{
  "code": 200,
  "message": "ok",
  "data": {
    "id": 1,
    "orderNo": "CM202610291830571805",
    "productId": 1001,
    "productTitle": "《高等数学》同济第八版 上下册",
    "productCover": "/uploads/xxx.jpg",
    "amount": 18.00,
    "status": "PENDING_PAY",
    "remark": "周三下午图书馆门口",
    "buyerId": 1001,
    "buyerNickname": "校园小买家",
    "sellerId": 1002,
    "sellerNickname": "清仓学长",
    "createdAt": "2026-10-29 18:30:57",
    "paidAt": null,
    "finishedAt": null
  }
}
```

## 17. 我买到的

`GET /api/orders/bought`（鉴权）

| 参数 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| page | int | 1 | 页码 |
| size | int | 10 | 每页数量 |
| status | string | - | `PENDING_PAY` / `PAID` / `COMPLETED` / `CANCELLED` |

返回分页结构，元素字段同下单单条响应。

## 18. 我卖出的

`GET /api/orders/sold`（鉴权），参数同上。

## 19. 订单详情

`GET /api/orders/{id}`（鉴权，仅买卖双方）

非交易参与方返回 403，订单不存在返回 404。

## 20. 支付

`PUT /api/orders/{id}/pay`（鉴权，仅买家）

仅 `PENDING_PAY` 可支付，成功后状态变为 `PAID` 并记录 `paidAt`。响应为更新后的订单。

## 21. 确认收货

`PUT /api/orders/{id}/confirm`（鉴权，仅买家）

仅 `PAID` 可确认，成功后订单 `COMPLETED`，商品由 `LOCKED` 变为 `SOLD`。

## 22. 取消订单

`PUT /api/orders/{id}/cancel`（鉴权，买卖双方）

仅 `PENDING_PAY` 可取消，订单变为 `CANCELLED`，商品由 `LOCKED` 恢复为 `ON_SALE`。

---

## 错误响应示例

```json
{ "code": 400, "message": "不能购买自己发布的商品", "data": null }
```

```json
{ "code": 409, "message": "手慢了，商品已被其他同学下单", "data": null }
```

Token 缺失或失效（HTTP 401）：

```json
{ "code": 401, "message": "登录已过期，请重新登录", "data": null }
```
