# 量股化金(LGHJ) API 接口文档

## 1. 认证模块

### 1.1 用户登录
- **接口地址**: `/api/login`
- **请求方式**: `POST`
- **接口描述**: 用户或管理员登录，返回JWT令牌
- **请求数据类型**: `application/json`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| username | 用户名 | body | true | string |
| password | 密码 | body | true | string |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 (1:成功, 0:失败) | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 返回数据 | object | LoginVO |
| - token | JWT令牌 | string | string |
| - id | 用户ID | integer(int64) | integer(int64) |
| - username | 用户名 | string | string |
| - userType | 用户类型(0:用户, 1:管理) | integer(int32) | integer(int32) |
| - identityDesc | 身份描述 | string | string |
| - state | 用户状态 | integer(int32) | integer(int32) |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": {
        "id": 1,
        "username": "admin",
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "userType": 1,
        "identityDesc": "管理员",
        "state": 1
    }
}
```

### 1.2 用户退出
- **接口地址**: `/api/logout`
- **请求方式**: `POST`
- **接口描述**: 退出登录
- **请求数据类型**: `application/json`
- **响应数据类型**: `*/*`

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 (1:成功, 0:失败) | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 返回数据 | object | null |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": null
}
```

## 2. 用户端接口 (User API)

### 2.1 股票行情与搜索

#### 2.1.1 搜索股票
- **接口地址**: `/api/user/stock/search`
- **请求方式**: `GET`
- **接口描述**: 基于Elasticsearch的股票搜索（支持代码前缀和名称模糊搜索）
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| keyword | 搜索关键词(代码/名称) | query | true | string |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 股票列表 | array | List<StockDoc> |
| - symbol | 股票代码 | string | string |
| - name | 股票名称 | string | string |
| - industry | 行业 | string | string |
| - marketType | 市场类型 | string | string |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": [
        {
            "symbol": "600519",
            "name": "贵州茅台",
            "industry": "白酒",
            "marketType": "A股"
        }
    ]
}
```

#### 2.1.2 获取股票实时资讯
- **接口地址**: `/api/user/realtime/news`
- **请求方式**: `GET`
- **接口描述**: 获取股票实时资讯
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| symbol | 股票代码 | query | true | string |
| recentN | 资讯数量，默认10 | query | false | integer |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 资讯列表 | array | List<StockNewsVO> |
| - title | 资讯标题 | string | string |
| - time | 发布时间 | string | string |
| - source | 资讯来源 | string | string |
| - url | 资讯链接 | string | string |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": [
        {
            "title": "贵州茅台股价创新高",
            "time": "2026-02-20 10:30:00",
            "source": "财经网",
            "url": "https://example.com/news/1"
        }
    ]
}
```

#### 2.1.3 获取股票分时数据
- **接口地址**: `/api/user/realtime/minute`
- **请求方式**: `GET`
- **接口描述**: 获取当日分时数据
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| market | 市场代码(sh/sz) | query | true | string |
| code | 股票代码 | query | true | string |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 分时数据列表 | array | List<Map> |
| - time | 时间(HHmm) | string | string |
| - price | 价格 | number | double |
| - volume | 成交量 | integer | integer |
| - avg_price | 均价 | number | double |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": [
        {
            "time": "0930",
            "price": 1800.00,
            "volume": 10000,
            "avg_price": 1800.00
        }
    ]
}
```

### 2.2 智能预测

#### 2.2.1 预测未来30天股价
- **接口地址**: `/api/user/prediction/{symbol}`
- **请求方式**: `GET`
- **接口描述**: 调用LSTM模型预测未来30天股价走势
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| symbol | 股票代码 | path | true | string |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 预测结果 | object | StockPredictionVO |
| - symbol | 股票代码 | string | string |
| - predictions | 预测列表 | array | List<PredictionItem> |
| -- date | 日期 | string | string |
| -- price | 预测价格 | number | decimal |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": {
        "symbol": "600519",
        "predictions": [
            {
                "date": "2026-02-21",
                "price": 1810.00
            }
        ]
    }
}
```

#### 2.2.2 导出预测数据Excel
- **接口地址**: `/api/user/prediction/{symbol}/get_excel`
- **请求方式**: `GET`
- **接口描述**: 导出预测结果为Excel文件
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| symbol | 股票代码 | path | true | string |

### 2.3 模拟交易与账户

#### 2.3.1 创建模拟账户
- **接口地址**: `/api/user/account/create`
- **请求方式**: `POST`
- **接口描述**: 创建模拟账户
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 账户信息 | object | SimAccount |
| - id | 账户ID | integer(int64) | integer(int64) |
| - userId | 用户ID | integer(int64) | integer(int64) |
| - totalCash | 总资产 | number | decimal |
| - availableCash | 可用资金 | number | decimal |
| - frozenCash | 冻结资金 | number | decimal |
| - createTime | 创建时间 | string | string |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": {
        "id": 1,
        "userId": 1,
        "totalCash": 1000000.00,
        "availableCash": 1000000.00,
        "frozenCash": 0.00,
        "createTime": "2026-02-20 00:00:00"
    }
}
```

#### 2.3.2 获取账户信息
- **接口地址**: `/api/user/account/query_info`
- **请求方式**: `GET`
- **接口描述**: 查询用户模拟账户资金情况
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 账户信息 | object | SimAccount |
| - id | 账户ID | integer(int64) | integer(int64) |
| - userId | 用户ID | integer(int64) | integer(int64) |
| - totalCash | 总资产 | number | decimal |
| - availableCash | 可用资金 | number | decimal |
| - frozenCash | 冻结资金 | number | decimal |
| - createTime | 创建时间 | string | string |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": {
        "id": 1,
        "userId": 1,
        "totalCash": 1000000.00,
        "availableCash": 1000000.00,
        "frozenCash": 0.00,
        "createTime": "2026-02-20 00:00:00"
    }
}
```

#### 2.3.3 获取持仓列表
- **接口地址**: `/api/user/account/query_positions`
- **请求方式**: `GET`
- **接口描述**: 查询用户当前持仓股票
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 持仓列表 | array | List<UserPosition> |
| - id | 持仓ID | integer(int64) | integer(int64) |
| - userId | 用户ID | integer(int64) | integer(int64) |
| - symbol | 股票代码 | string | string |
| - quantity | 持有数量 | integer | integer |
| - avgPrice | 平均成本 | number | decimal |
| - marketValue | 市值 | number | decimal |
| - profit | 盈亏 | number | decimal |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": [
        {
            "id": 1,
            "userId": 1,
            "symbol": "600519",
            "quantity": 100,
            "avgPrice": 1800.00,
            "marketValue": 180000.00,
            "profit": 0.00
        }
    ]
}
```

#### 2.3.4 获取特定股票持仓
- **接口地址**: `/api/user/account/query_position`
- **请求方式**: `GET`
- **接口描述**: 获取特定股票的持仓信息
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| symbol | 股票代码 | query | true | string |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 持仓信息 | object | UserPosition |
| - id | 持仓ID | integer(int64) | integer(int64) |
| - userId | 用户ID | integer(int64) | integer(int64) |
| - symbol | 股票代码 | string | string |
| - quantity | 持有数量 | integer | integer |
| - avgPrice | 平均成本 | number | decimal |
| - marketValue | 市值 | number | decimal |
| - profit | 盈亏 | number | decimal |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": {
        "id": 1,
        "userId": 1,
        "symbol": "600519",
        "quantity": 100,
        "avgPrice": 1800.00,
        "marketValue": 180000.00,
        "profit": 0.00
    }
}
```

#### 2.3.5 创建委托单(下单)
- **接口地址**: `/api/user/trade/order`
- **请求方式**: `POST`
- **接口描述**: 用户发起买入或卖出委托
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| symbol | 股票代码 | query | true | string |
| direction | 方向(1买 2卖) | query | true | integer |
| price | 委托价格 | query | true | double |
| quantity | 数量 | query | true | integer |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 委托单信息 | object | TradeOrder |
| - id | 委托单ID | integer(int64) | integer(int64) |
| - userId | 用户ID | integer(int64) | integer(int64) |
| - symbol | 股票代码 | string | string |
| - direction | 方向 | short | short |
| - price | 委托价格 | number | double |
| - quantity | 委托数量 | integer | integer |
| - filledQuantity | 已成交数量 | integer | integer |
| - status | 状态 | short | short |
| - createTime | 创建时间 | string | string |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": {
        "id": 1,
        "userId": 1,
        "symbol": "600519",
        "direction": 1,
        "price": 1800.00,
        "quantity": 100,
        "filledQuantity": 0,
        "status": 1,
        "createTime": "2026-02-20 10:00:00"
    }
}
```

#### 2.3.6 撤销委托单
- **接口地址**: `/api/user/trade/cancel`
- **请求方式**: `POST`
- **接口描述**: 撤销未成交的委托单
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| orderId | 委托单ID | query | true | integer(int64) |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 返回数据 | object | null |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": null
}
```

#### 2.3.7 获取用户委托单列表
- **接口地址**: `/api/user/trade/query_orders`
- **请求方式**: `GET`
- **接口描述**: 获取用户的委托单列表
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 委托单列表 | array | List<TradeOrder> |
| - id | 委托单ID | integer(int64) | integer(int64) |
| - userId | 用户ID | integer(int64) | integer(int64) |
| - symbol | 股票代码 | string | string |
| - direction | 方向 | short | short |
| - price | 委托价格 | number | double |
| - quantity | 委托数量 | integer | integer |
| - filledQuantity | 已成交数量 | integer | integer |
| - status | 状态 | short | short |
| - createTime | 创建时间 | string | string |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": [
        {
            "id": 1,
            "userId": 1,
            "symbol": "600519",
            "direction": 1,
            "price": 1800.00,
            "quantity": 100,
            "filledQuantity": 0,
            "status": 1,
            "createTime": "2026-02-20 10:00:00"
        }
    ]
}
```

#### 2.3.8 获取用户成交记录列表
- **接口地址**: `/api/user/trade/query_deals`
- **请求方式**: `GET`
- **接口描述**: 获取用户的成交记录列表
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 成交记录列表 | array | List<TradeDeal> |
| - id | 成交记录ID | integer(int64) | integer(int64) |
| - orderId | 委托单ID | integer(int64) | integer(int64) |
| - userId | 用户ID | integer(int64) | integer(int64) |
| - symbol | 股票代码 | string | string |
| - direction | 方向 | short | short |
| - price | 成交价格 | number | double |
| - quantity | 成交数量 | integer | integer |
| - dealTime | 成交时间 | string | string |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": [
        {
            "id": 1,
            "orderId": 1,
            "userId": 1,
            "symbol": "600519",
            "direction": 1,
            "price": 1800.00,
            "quantity": 100,
            "dealTime": "2026-02-20 10:01:00"
        }
    ]
}
```

### 2.4 社区与互动

#### 2.4.1 发布博客
- **接口地址**: `/api/user/blog`
- **请求方式**: `POST`
- **接口描述**: 发布新博客
- **请求数据类型**: `application/json`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| title | 标题 | body | true | string |
| content | 内容 | body | true | string |
| userId | 用户ID | body | true | integer(int64) |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 返回数据 | object | null |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": null
}
```

#### 2.4.2 点赞博客
- **接口地址**: `/api/user/blog/like/{id}`
- **请求方式**: `PUT`
- **接口描述**: 点赞或取消点赞博客
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| id | 博客ID | path | true | integer(int64) |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 返回数据 | object | null |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": null
}
```

#### 2.4.3 分页查看登录用户自己的博客内容
- **接口地址**: `/api/user/blog/query/of/me`
- **请求方式**: `GET`
- **接口描述**: 分页查看当前登录用户的博客列表
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| current | 当前页，默认1 | query | false | integer |
| size | 每页条数，默认10 | query | false | integer |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 博客列表 | array | List<Blog> |
| - id | 博客ID | integer(int64) | integer(int64) |
| - userId | 用户ID | integer(int64) | integer(int64) |
| - title | 标题 | string | string |
| - content | 内容 | string | string |
| - likeCount | 点赞数 | integer | integer |
| - commentCount | 评论数 | integer | integer |
| - createTime | 创建时间 | string | string |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": [
        {
            "id": 1,
            "userId": 1,
            "title": "贵州茅台投资分析",
            "content": "贵州茅台是中国白酒行业的龙头企业...",
            "likeCount": 10,
            "commentCount": 5,
            "createTime": "2026-02-20 09:00:00"
        }
    ]
}
```

#### 2.4.4 查询热门博客
- **接口地址**: `/api/user/blog/query/hot`
- **请求方式**: `GET`
- **接口描述**: 分页查询热门博客列表
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| current | 当前页，默认1 | query | false | integer |
| size | 每页条数，默认10 | query | false | integer |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 博客列表 | array | List<Blog> |
| - id | 博客ID | integer(int64) | integer(int64) |
| - userId | 用户ID | integer(int64) | integer(int64) |
| - title | 标题 | string | string |
| - content | 内容 | string | string |
| - likeCount | 点赞数 | integer | integer |
| - commentCount | 评论数 | integer | integer |
| - createTime | 创建时间 | string | string |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": [
        {
            "id": 1,
            "userId": 1,
            "title": "贵州茅台投资分析",
            "content": "贵州茅台是中国白酒行业的龙头企业...",
            "likeCount": 10,
            "commentCount": 5,
            "createTime": "2026-02-20 09:00:00"
        }
    ]
}
```

#### 2.4.5 根据id查询博客
- **接口地址**: `/api/user/blog/query/{id}`
- **请求方式**: `GET`
- **接口描述**: 根据ID查询博客详情
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| id | 博客ID | path | true | integer(int64) |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 博客信息 | object | Blog |
| - id | 博客ID | integer(int64) | integer(int64) |
| - userId | 用户ID | integer(int64) | integer(int64) |
| - title | 标题 | string | string |
| - content | 内容 | string | string |
| - likeCount | 点赞数 | integer | integer |
| - commentCount | 评论数 | integer | integer |
| - createTime | 创建时间 | string | string |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": {
        "id": 1,
        "userId": 1,
        "title": "贵州茅台投资分析",
        "content": "贵州茅台是中国白酒行业的龙头企业...",
        "likeCount": 10,
        "commentCount": 5,
        "createTime": "2026-02-20 09:00:00"
    }
}
```

#### 2.4.6 查看指定用户发的博客
- **接口地址**: `/api/user/blog/query/of/user`
- **请求方式**: `GET`
- **接口描述**: 查看指定用户的博客列表
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| current | 当前页，默认1 | query | false | integer |
| size | 每页条数，默认10 | query | false | integer |
| id | 用户ID | query | true | integer(int64) |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 博客列表 | array | List<Blog> |
| - id | 博客ID | integer(int64) | integer(int64) |
| - userId | 用户ID | integer(int64) | integer(int64) |
| - title | 标题 | string | string |
| - content | 内容 | string | string |
| - likeCount | 点赞数 | integer | integer |
| - commentCount | 评论数 | integer | integer |
| - createTime | 创建时间 | string | string |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": [
        {
            "id": 1,
            "userId": 1,
            "title": "贵州茅台投资分析",
            "content": "贵州茅台是中国白酒行业的龙头企业...",
            "likeCount": 10,
            "commentCount": 5,
            "createTime": "2026-02-20 09:00:00"
        }
    ]
}
```

#### 2.4.7 粉丝查看关注所有用户博客接口
- **接口地址**: `/api/user/blog/query/of/follow`
- **请求方式**: `GET`
- **接口描述**: 查看当前用户关注的所有用户的博客
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| current | 当前页，默认1 | query | false | integer |
| size | 每页条数，默认10 | query | false | integer |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 博客列表 | object | PageResult<Blog> |
| - records | 博客列表 | array | List<Blog> |
| - total | 总记录数 | integer | integer |
| - size | 每页大小 | integer | integer |
| - current | 当前页 | integer | integer |
| - pages | 总页数 | integer | integer |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": {
        "records": [
            {
                "id": 1,
                "userId": 2,
                "title": "五粮液投资分析",
                "content": "五粮液是中国白酒行业的知名企业...",
                "likeCount": 8,
                "commentCount": 3,
                "createTime": "2026-02-20 08:00:00"
            }
        ],
        "total": 1,
        "size": 10,
        "current": 1,
        "pages": 1
    }
}
```

#### 2.4.8 用户删除自己的博客
- **接口地址**: `/api/user/blog/delete/{id}`
- **请求方式**: `DELETE`
- **接口描述**: 用户删除自己的博客
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| id | 博客ID | path | true | integer(int64) |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 返回数据 | object | null |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": null
}
```

#### 2.4.9 用户编辑自己的博客
- **接口地址**: `/api/user/blog/update`
- **请求方式**: `PUT`
- **接口描述**: 用户编辑自己的博客
- **请求数据类型**: `application/json`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| id | 博客ID | body | true | integer(int64) |
| title | 标题 | body | true | string |
| content | 内容 | body | true | string |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 返回数据 | object | null |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": null
}
```

#### 2.4.10 新增博客评论（一级/二级通用）
- **接口地址**: `/api/user/blog/comments/add`
- **请求方式**: `POST`
- **接口描述**: 新增博客评论，支持一级和二级评论
- **请求数据类型**: `application/json`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| blogId | 博客ID | body | true | integer(int64) |
| userId | 用户ID | body | true | integer(int64) |
| content | 评论内容 | body | true | string |
| parentId | 父评论ID，一级评论为0 | body | false | integer(int64) |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 返回数据 | object | null |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": null
}
```

#### 2.4.11 查询博客评论列表（带二级评论，树形结构）
- **接口地址**: `/api/user/blog/comments/list`
- **请求方式**: `GET`
- **接口描述**: 查询博客评论列表，包含二级评论的树形结构
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| blogId | 博客ID | query | true | integer(int64) |
| pageNum | 页码，默认1 | query | false | integer |
| pageSize | 每页大小，默认10 | query | false | integer |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 评论列表 | object | PageResult<BlogCommentVO> |
| - records | 评论列表 | array | List<BlogCommentVO> |
| - total | 总记录数 | integer | integer |
| - size | 每页大小 | integer | integer |
| - current | 当前页 | integer | integer |
| - pages | 总页数 | integer | integer |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": {
        "records": [
            {
                "id": 1,
                "blogId": 1,
                "userId": 2,
                "content": "写得很好！",
                "likeCount": 5,
                "createTime": "2026-02-20 09:30:00",
                "children": [
                    {
                        "id": 2,
                        "blogId": 1,
                        "userId": 3,
                        "content": "同意，分析很到位",
                        "likeCount": 2,
                        "createTime": "2026-02-20 09:45:00",
                        "children": []
                    }
                ]
            }
        ],
        "total": 1,
        "size": 10,
        "current": 1,
        "pages": 1
    }
}
```

#### 2.4.12 评论点赞/取消点赞
- **接口地址**: `/api/user/blog/comments/like/{commentId}`
- **请求方式**: `POST`
- **接口描述**: 点赞或取消点赞评论
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| commentId | 评论ID | path | true | integer(int64) |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 返回数据 | object | null |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": null
}
```

#### 2.4.13 删除评论（逻辑删除）
- **接口地址**: `/api/user/blog/comments/delete/{commentId}`
- **请求方式**: `DELETE`
- **接口描述**: 删除评论（逻辑删除）
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| commentId | 评论ID | path | true | integer(int64) |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 返回数据 | object | null |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": null
}
```

### 2.5 关注管理

#### 2.5.1 关注和取关
- **接口地址**: `/api/user/follow/{id}/{isFollow}`
- **请求方式**: `PUT`
- **接口描述**: 关注或取消关注用户
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| id | 被关注用户ID | path | true | integer(int64) |
| isFollow | 是否关注，true-关注，false-取消关注 | path | true | boolean |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 返回数据 | object | null |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": null
}
```

#### 2.5.2 判断是否关注
- **接口地址**: `/api/user/follow/or/not/{id}`
- **请求方式**: `GET`
- **接口描述**: 判断当前用户是否关注了指定用户
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| id | 被关注用户ID | path | true | integer(int64) |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 是否关注 | boolean | boolean |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": true
}
```

### 2.6 自选股管理

#### 2.6.1 添加自选股
- **接口地址**: `/api/user/optional/add`
- **请求方式**: `POST`
- **接口描述**: 添加自选股
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| symbol | 股票代码 | query | true | string |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 返回数据 | object | null |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": null
}
```

#### 2.6.2 删除自选股
- **接口地址**: `/api/user/optional/remove`
- **请求方式**: `POST`
- **接口描述**: 删除自选股
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| symbol | 股票代码 | query | true | string |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 返回数据 | object | null |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": null
}
```

#### 2.6.3 获取自选股列表
- **接口地址**: `/api/user/optional/list`
- **请求方式**: `GET`
- **接口描述**: 获取用户的自选股列表
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 自选股列表 | array | List<StockFollowVO> |
| - symbol | 股票代码 | string | string |
| - name | 股票名称 | string | string |
| - currentPrice | 当前价格 | number | decimal |
| - changePercent | 涨跌幅 | number | decimal |
| - followTime | 关注时间 | string | string |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": [
        {
            "symbol": "600519",
            "name": "贵州茅台",
            "currentPrice": 1800.00,
            "changePercent": 0.5,
            "followTime": "2026-02-20 09:00:00"
        }
    ]
}
```

## 3. 管理端接口 (Admin API)

### 3.1 股票数据管理

#### 3.1.1 初始化导入A股基础信息
- **接口地址**: `/api/admin/stock/import`
- **请求方式**: `POST`
- **接口描述**: 初始化导入A股基础信息
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 导入结果信息 | string | string |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": "导入成功，共导入1000条数据"
}
```

#### 3.1.2 同步数据到Elasticsearch
- **接口地址**: `/api/admin/stock/sync-es`
- **请求方式**: `POST`
- **接口描述**: 手动触发MySQL到Elasticsearch的全量同步
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 返回数据 | object | null |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": "同步完成"
}
```

#### 3.1.3 分页查询股票列表
- **接口地址**: `/api/admin/stock/page`
- **请求方式**: `GET`
- **接口描述**: 管理员查询股票基础信息列表
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| pageNum | 页码，默认1 | query | false | integer |
| pageSize | 每页大小，默认10 | query | false | integer |
| keyword | 关键词 | query | false | string |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 股票列表 | object | Page<StockBasic> |
| - records | 股票列表 | array | List<StockBasic> |
| - total | 总记录数 | integer | integer |
| - size | 每页大小 | integer | integer |
| - current | 当前页 | integer | integer |
| - pages | 总页数 | integer | integer |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": {
        "records": [
            {
                "symbol": "600519",
                "name": "贵州茅台",
                "industry": "白酒",
                "marketType": "A股"
            }
        ],
        "total": 1,
        "size": 10,
        "current": 1,
        "pages": 1
    }
}
```

#### 3.1.4 根据股票代码更新股票信息-单条更新
- **接口地址**: `/api/admin/stock/update`
- **请求方式**: `PUT`
- **接口描述**: 更新指定股票的基础信息
- **请求数据类型**: `application/json`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| symbol | 股票代码 | body | true | string |
| name | 股票名称 | body | false | string |
| industry | 行业 | body | false | string |
| marketType | 市场类型 | body | false | string |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 返回数据 | object | null |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": "更新成功"
}
```

#### 3.1.5 根据股票代码更新股票信息-批量更新
- **接口地址**: `/api/admin/stock/batch-update`
- **请求方式**: `POST`
- **接口描述**: 批量更新股票信息
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `text/plain`

**响应示例**:
```
批量更新任务已提交
```

### 3.2 交易管理

#### 3.2.1 分页查询委托单
- **接口地址**: `/api/admin/trade/order/page`
- **请求方式**: `GET`
- **接口描述**: 管理员查询所有用户的委托单
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| pageNum | 页码，默认1 | query | false | integer |
| pageSize | 每页大小，默认10 | query | false | integer |
| userId | 用户ID | query | false | integer(int64) |
| symbol | 股票代码 | query | false | string |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 委托单列表 | object | Page<TradeOrder> |
| - records | 委托单列表 | array | List<TradeOrder> |
| - total | 总记录数 | integer | integer |
| - size | 每页大小 | integer | integer |
| - current | 当前页 | integer | integer |
| - pages | 总页数 | integer | integer |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": {
        "records": [
            {
                "id": 1,
                "userId": 1,
                "symbol": "600519",
                "direction": 1,
                "price": 1800.00,
                "quantity": 100,
                "filledQuantity": 0,
                "status": 1,
                "createTime": "2026-02-20 10:00:00"
            }
        ],
        "total": 1,
        "size": 10,
        "current": 1,
        "pages": 1
    }
}
```

#### 3.2.2 分页查询成交记录
- **接口地址**: `/api/admin/trade/deal/page`
- **请求方式**: `GET`
- **接口描述**: 管理员查询所有用户的成交记录
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| pageNum | 页码，默认1 | query | false | integer |
| pageSize | 每页大小，默认10 | query | false | integer |
| userId | 用户ID | query | false | integer(int64) |
| symbol | 股票代码 | query | false | string |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 成交记录列表 | object | Page<TradeDeal> |
| - records | 成交记录列表 | array | List<TradeDeal> |
| - total | 总记录数 | integer | integer |
| - size | 每页大小 | integer | integer |
| - current | 当前页 | integer | integer |
| - pages | 总页数 | integer | integer |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": {
        "records": [
            {
                "id": 1,
                "orderId": 1,
                "userId": 1,
                "symbol": "600519",
                "direction": 1,
                "price": 1800.00,
                "quantity": 100,
                "dealTime": "2026-02-20 10:01:00"
            }
        ],
        "total": 1,
        "size": 10,
        "current": 1,
        "pages": 1
    }
}
```

### 3.3 内容管理

#### 3.3.1 分页查询博客列表
- **接口地址**: `/api/admin/blog/page`
- **请求方式**: `GET`
- **接口描述**: 管理员查询所有博客
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| pageNum | 页码，默认1 | query | false | integer |
| pageSize | 每页大小，默认10 | query | false | integer |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 博客列表 | object | Page<Blog> |
| - records | 博客列表 | array | List<Blog> |
| - total | 总记录数 | integer | integer |
| - size | 每页大小 | integer | integer |
| - current | 当前页 | integer | integer |
| - pages | 总页数 | integer | integer |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": {
        "records": [
            {
                "id": 1,
                "userId": 1,
                "title": "贵州茅台投资分析",
                "content": "贵州茅台是中国白酒行业的龙头企业...",
                "likeCount": 10,
                "commentCount": 5,
                "createTime": "2026-02-20 09:00:00"
            }
        ],
        "total": 1,
        "size": 10,
        "current": 1,
        "pages": 1
    }
}
```

#### 3.3.2 删除博客
- **接口地址**: `/api/admin/blog/{id}`
- **请求方式**: `DELETE`
- **接口描述**: 管理员删除违规博客
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| id | 博客ID | path | true | integer(int64) |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 返回数据 | object | null |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": null
}
```

#### 3.3.3 查看博客详情
- **接口地址**: `/api/admin/blog/{id}`
- **请求方式**: `GET`
- **接口描述**: 查看博客详情
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| id | 博客ID | path | true | integer(int64) |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 博客信息 | object | Blog |
| - id | 博客ID | integer(int64) | integer(int64) |
| - userId | 用户ID | integer(int64) | integer(int64) |
| - title | 标题 | string | string |
| - content | 内容 | string | string |
| - likeCount | 点赞数 | integer | integer |
| - commentCount | 评论数 | integer | integer |
| - createTime | 创建时间 | string | string |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": {
        "id": 1,
        "userId": 1,
        "title": "贵州茅台投资分析",
        "content": "贵州茅台是中国白酒行业的龙头企业...",
        "likeCount": 10,
        "commentCount": 5,
        "createTime": "2026-02-20 09:00:00"
    }
}
```

#### 3.3.4 分页查询评论列表
- **接口地址**: `/api/admin/blog/comments/page`
- **请求方式**: `GET`
- **接口描述**: 管理员查询博客评论
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| pageNum | 页码，默认1 | query | false | integer |
| pageSize | 每页大小，默认10 | query | false | integer |
| blogId | 博客ID | query | false | integer(int64) |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 评论列表 | object | Page<BlogComments> |
| - records | 评论列表 | array | List<BlogComments> |
| - total | 总记录数 | integer | integer |
| - size | 每页大小 | integer | integer |
| - current | 当前页 | integer | integer |
| - pages | 总页数 | integer | integer |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": {
        "records": [
            {
                "id": 1,
                "blogId": 1,
                "userId": 2,
                "content": "写得很好！",
                "likeCount": 5,
                "createTime": "2026-02-20 09:30:00"
            }
        ],
        "total": 1,
        "size": 10,
        "current": 1,
        "pages": 1
    }
}
```

#### 3.3.5 删除评论
- **接口地址**: `/api/admin/blog/comments/{id}`
- **请求方式**: `DELETE`
- **接口描述**: 管理员删除违规评论
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| id | 评论ID | path | true | integer(int64) |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 返回数据 | object | null |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": null
}
```

### 3.4 用户管理

#### 3.4.1 分页条件查询用户接口
- **接口地址**: `/api/admin/user`
- **请求方式**: `GET`
- **接口描述**: 分页条件查询用户列表
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| current | 当前页，默认1 | query | false | long |
| size | 每页大小，默认10 | query | false | long |
| username | 用户名 | query | false | string |
| userType | 用户类型 | query | false | integer |
| status | 用户状态 | query | false | integer |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 用户列表 | object | PageResult<UserVO> |
| - records | 用户列表 | array | List<UserVO> |
| - total | 总记录数 | integer | integer |
| - size | 每页大小 | integer | integer |
| - current | 当前页 | integer | integer |
| - pages | 总页数 | integer | integer |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": {
        "records": [
            {
                "id": 1,
                "username": "admin",
                "userType": 1,
                "status": 1,
                "createTime": "2026-02-20 00:00:00"
            }
        ],
        "total": 1,
        "size": 10,
        "current": 1,
        "pages": 1
    }
}
```

#### 3.4.2 新增用户接口
- **接口地址**: `/api/admin/user/add`
- **请求方式**: `POST`
- **接口描述**: 新增用户
- **请求数据类型**: `application/json`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| username | 用户名 | body | true | string |
| password | 密码 | body | true | string |
| userType | 用户类型 | body | true | integer |
| status | 用户状态 | body | true | integer |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 返回数据 | object | null |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": null
}
```

#### 3.4.3 删除用户接口
- **接口地址**: `/api/admin/user/{id}`
- **请求方式**: `DELETE`
- **接口描述**: 删除用户
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| id | 用户ID | path | true | integer(int64) |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 返回数据 | object | null |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": null
}
```

#### 3.4.4 根据id查询用户接口
- **接口地址**: `/api/admin/user/{id}`
- **请求方式**: `GET`
- **接口描述**: 根据ID查询用户详情
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| id | 用户ID | path | true | integer(int64) |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 用户信息 | object | UserVO |
| - id | 用户ID | integer(int64) | integer(int64) |
| - username | 用户名 | string | string |
| - userType | 用户类型 | integer | integer |
| - status | 用户状态 | integer | integer |
| - createTime | 创建时间 | string | string |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": {
        "id": 1,
        "username": "admin",
        "userType": 1,
        "status": 1,
        "createTime": "2026-02-20 00:00:00"
    }
}
```

#### 3.4.5 更改用户信息接口
- **接口地址**: `/api/admin/user/update`
- **请求方式**: `PUT`
- **接口描述**: 更新用户信息
- **请求数据类型**: `application/json`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| id | 用户ID | body | true | integer(int64) |
| username | 用户名 | body | false | string |
| password | 密码 | body | false | string |
| userType | 用户类型 | body | false | integer |
| status | 用户状态 | body | false | integer |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 返回数据 | object | null |

**响应示例**:
```json
{
    "code": 1,
    "msg": "success",
    "data": null
}
```

#### 3.4.6 启用禁用用户账号
- **接口地址**: `/api/admin/user/changeStatus/{id}`
- **请求方式**: `POST`
- **接口描述**: 启用或禁用用户账号
- **请求数据类型**: `application/x-www-form-urlencoded`
- **响应数据类型**: `*/*`

**请求参数**:

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
| :--- | :--- | :--- | :--- | :--- |
| id | 用户ID | path | true | integer(int64) |
| status | 状态，1-启用，0-禁用 | query | true | short |

**响应参数**:

| 参数名称 | 参数说明 | 类型 | schema |
| :--- | :--- | :--- | :--- |
| code | 状态码 | integer(int32) | integer(int32) |
| msg | 提示信息 | string | string |
| data | 返回数据 | object | null |

**响应示例**:
```json
{
    "code": 1,
    "msg": "启用账号成功",
    "data": null
}
```

## 4. 接口通用规范

### 4.1 响应格式统一
所有API接口返回数据格式统一为：

```json
{
    "code": 1, // 状态码，1表示成功，0表示失败
    "msg": "success", // 提示信息
    "data": null // 返回数据，根据接口不同返回不同格式
}
```

### 4.2 错误处理
当接口调用失败时，返回格式为：

```json
{
    "code": 0,
    "msg": "错误信息",
    "data": null
}
```

### 4.3 认证方式
- 使用JWT进行身份认证
- 登录成功后获取token，后续请求在请求头中携带token
- 请求头格式：`Authorization: Bearer {token}`

### 4.4 分页响应格式
分页接口返回格式统一为：

```json
{
    "code": 1,
    "msg": "success",
    "data": {
        "records": [], // 数据列表
        "total": 100, // 总记录数
        "size": 10, // 每页大小
        "current": 1, // 当前页
        "pages": 10 // 总页数
    }
}
```

## 5. 接口版本控制

- 当前API版本：v1
- 版本控制方式：URL路径前缀
- 示例：`/api/v1/user/login`

## 6. 接口安全

1. **身份认证**：使用JWT进行身份认证
2. **权限控制**：基于用户角色的权限控制
3. **请求频率限制**：防止API滥用
4. **数据验证**：对所有输入参数进行验证
5. **SQL注入防护**：使用MyBatis-Plus的参数化查询
6. **XSS防护**：对用户输入进行过滤

## 7. 接口调用示例

### 7.1 登录示例

**请求**:
```http
POST /api/login
Content-Type: application/json

{
    "username": "admin",
    "password": "123456"
}
```

**响应**:
```json
{
    "code": 1,
    "msg": "success",
    "data": {
        "id": 1,
        "username": "admin",
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "userType": 1,
        "identityDesc": "管理员",
        "state": 1
    }
}
```

### 7.2 带认证的请求示例

**请求**:
```http
GET /api/user/stock/search?keyword=贵州茅台
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应**:
```json
{
    "code": 1,
    "msg": "success",
    "data": [
        {
            "symbol": "600519",
            "name": "贵州茅台",
            "industry": "白酒",
            "marketType": "A股"
        }
    ]
}
```