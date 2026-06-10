# 量股化金接口文档


**简介**:量股化金接口文档


**HOST**:localhost:8080


**联系人**:Feng


**Version**:v1.0.0


**接口路径**:/v2/api-docs


[TOC]






# 交易操作接口


## 撤销委托单


**接口地址**:`/api/user/trade/cancel`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|orderId|orderId|query|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 创建委托单


**接口地址**:`/api/user/trade/order`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|direction|direction|query|true|integer(int32)||
|price|price|query|true|number(double)||
|quantity|quantity|query|true|integer(int32)||
|symbol|symbol|query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 获取用户成交记录列表


**接口地址**:`/api/user/trade/query_deals`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 获取用户委托单列表


**接口地址**:`/api/user/trade/query_orders`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


# 关注接口


## 判断是否关注


**接口地址**:`/api/user/follow/or/not/{id}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|id|path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 关注和取关


**接口地址**:`/api/user/follow/{id}/{isFollow}`


**请求方式**:`PUT`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|id|path|true|integer(int64)||
|isFollow|isFollow|path|true|boolean||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


# 博客评论区接口


## 新增博客评论（一级-二级通用）


**接口地址**:`/api/user/blog/comments/add`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "blogId": 0,
  "content": "",
  "parentId": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|dto|dto|body|true|BlogCommentAddDTO|BlogCommentAddDTO|
|&emsp;&emsp;blogId|||false|integer(int64)||
|&emsp;&emsp;content|||false|string||
|&emsp;&emsp;parentId|||false|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 删除评论（逻辑删除）


**接口地址**:`/api/user/blog/comments/delete/{commentId}`


**请求方式**:`DELETE`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|commentId|commentId|path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|204|No Content||
|401|Unauthorized||
|403|Forbidden||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 评论点赞-取消点赞


**接口地址**:`/api/user/blog/comments/like/{commentId}`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|commentId|commentId|path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 查询博客评论列表（带二级评论，树形结构）


**接口地址**:`/api/user/blog/comments/list`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|blogId||query|false|integer(int64)||
|pageNum||query|false|integer(int32)||
|pageSize||query|false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


# 实时股票数据接口


## 获取股票分时数据


**接口地址**:`/api/user/realtime/minute`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|code|code|query|true|string||
|market|market|query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 获取股票实时资讯


**接口地址**:`/api/user/realtime/news`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|symbol|symbol|query|true|string||
|recentN|recentN|query|false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


# 用户登录接口


## 用户登录


**接口地址**:`/api/login`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "password": "",
  "username": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|loginDTO|loginDTO|body|true|LoginDTO|LoginDTO|
|&emsp;&emsp;password|密码||true|string||
|&emsp;&emsp;username|用户名||true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 员工退出


**接口地址**:`/api/logout`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


# 用户讨论区博客接口


## 发布博客


**接口地址**:`/api/user/blog`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "comments": 0,
  "context": "",
  "createTime": "",
  "createUser": "",
  "icon": "",
  "id": 0,
  "images": "",
  "isDeleted": 0,
  "isLike": true,
  "liked": 0,
  "name": "",
  "status": 0,
  "stockId": "",
  "title": "",
  "updateTime": "",
  "updateUser": "",
  "userId": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|blog|blog|body|true|Blog|Blog|
|&emsp;&emsp;comments|评论数||false|integer(int32)||
|&emsp;&emsp;context|正文||false|string||
|&emsp;&emsp;createTime|创建时间||false|string(date-time)||
|&emsp;&emsp;createUser|创建人||false|string||
|&emsp;&emsp;icon|||false|string||
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;images|照片，最多9张，多张以","隔开||false|string||
|&emsp;&emsp;isDeleted|是否已删除（0: 否，1: 是）||false|integer(int32)||
|&emsp;&emsp;isLike|||false|boolean||
|&emsp;&emsp;liked|点赞数||false|integer(int32)||
|&emsp;&emsp;name|||false|string||
|&emsp;&emsp;status|状态（0: 禁用，1: 正常）||false|integer(int32)||
|&emsp;&emsp;stockId|关联股票代码，多个用","隔开||false|string||
|&emsp;&emsp;title|标题||false|string||
|&emsp;&emsp;updateTime|修改时间||false|string(date-time)||
|&emsp;&emsp;updateUser|修改人||false|string||
|&emsp;&emsp;userId|用户id||false|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 用户删除自己的博客


**接口地址**:`/api/user/blog/delete/{id}`


**请求方式**:`DELETE`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|id|path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|204|No Content||
|401|Unauthorized||
|403|Forbidden||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 点赞功能接口


**接口地址**:`/api/user/blog/like/{id}`


**请求方式**:`PUT`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|id|path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 根据点赞数量（热度）展示博客


**接口地址**:`/api/user/blog/query/hot`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|current|current|query|false|integer(int32)||
|size|size|query|false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 粉丝查看关注所有用户博客接口


**接口地址**:`/api/user/blog/query/of/follow`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|current|current|query|false|integer(int32)||
|size|size|query|false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 分页查看登录用户自己的博客内容


**接口地址**:`/api/user/blog/query/of/me`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|current|current|query|false|integer(int32)||
|size|size|query|false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 查看指定用户发的博客


**接口地址**:`/api/user/blog/query/of/user`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|id|query|true|integer(int64)||
|current|current|query|false|integer(int32)||
|size|size|query|false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 根据id查询博客


**接口地址**:`/api/user/blog/query/{id}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|id|path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 用户编辑自己的博客


**接口地址**:`/api/user/blog/update`


**请求方式**:`PUT`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "content": "",
  "id": 0,
  "images": "",
  "title": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|blogUpdateDTO|blogUpdateDTO|body|true|BlogUpdateDTO|BlogUpdateDTO|
|&emsp;&emsp;content|||false|string||
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;images|||false|string||
|&emsp;&emsp;title|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


# 管理端-交易管理接口


## 分页查询成交记录


**接口地址**:`/api/admin/trade/deal/page`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|pageNum|pageNum|query|false|integer(int32)||
|pageSize|pageSize|query|false|integer(int32)||
|symbol|symbol|query|false|string||
|userId|userId|query|false|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 分页查询委托单


**接口地址**:`/api/admin/trade/order/page`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|pageNum|pageNum|query|false|integer(int32)||
|pageSize|pageSize|query|false|integer(int32)||
|symbol|symbol|query|false|string||
|userId|userId|query|false|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


# 管理端-博客管理接口


## 分页查询博客列表


**接口地址**:`/api/admin/blog/page`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|pageNum|pageNum|query|false|integer(int32)||
|pageSize|pageSize|query|false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 查看博客详情


**接口地址**:`/api/admin/blog/{id}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|id|path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 删除博客


**接口地址**:`/api/admin/blog/{id}`


**请求方式**:`DELETE`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|id|path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|204|No Content||
|401|Unauthorized||
|403|Forbidden||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


# 管理端-博客评论管理接口


## 分页查询评论列表


**接口地址**:`/api/admin/blog/comments/page`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|blogId|blogId|query|false|integer(int64)||
|pageNum|pageNum|query|false|integer(int32)||
|pageSize|pageSize|query|false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 删除评论


**接口地址**:`/api/admin/blog/comments/{id}`


**请求方式**:`DELETE`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|id|path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|204|No Content||
|401|Unauthorized||
|403|Forbidden||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


# 管理端-导入A股基础信息接口


## 根据股票代码更新股票信息-批量更新


**接口地址**:`/api/admin/stock/batch-update`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK||
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


暂无


**响应示例**:
```javascript

```


## 初始化导入A股基础信息


**接口地址**:`/api/admin/stock/import`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 分页查询股票列表


**接口地址**:`/api/admin/stock/page`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|keyword|keyword|query|false|string||
|pageNum|pageNum|query|false|integer(int32)||
|pageSize|pageSize|query|false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 同步数据到Elasticsearch


**接口地址**:`/api/admin/stock/sync-es`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 根据股票代码更新股票信息-单条更新


**接口地址**:`/api/admin/stock/update`


**请求方式**:`PUT`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "createTime": "",
  "createUser": "",
  "floatMarketCap": 0,
  "floatShares": 0,
  "id": 0,
  "industry": "",
  "isDeleted": 0,
  "listDate": "",
  "marketType": 0,
  "name": "",
  "shortName": "",
  "symbol": "",
  "totalMarketCap": 0,
  "totalShares": 0,
  "updateTime": "",
  "updateUser": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|stockBasic|stockBasic|body|true|StockBasic|StockBasic|
|&emsp;&emsp;createTime|创建时间||false|string(date-time)||
|&emsp;&emsp;createUser|创建人||false|string||
|&emsp;&emsp;floatMarketCap|流通市值||false|integer(int64)||
|&emsp;&emsp;floatShares|流通股||false|integer(int64)||
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;industry|所属行业||false|string||
|&emsp;&emsp;isDeleted|是否已删除（0: 否，1: 是）||false|integer(int32)||
|&emsp;&emsp;listDate|上市日期||false|string(date)||
|&emsp;&emsp;marketType|市场类型 (0-未知 1-沪A, 2-深A, 3-创业板, 4-科创板，5-北交所，6-新三版)||false|integer(int32)||
|&emsp;&emsp;name|股票名称||false|string||
|&emsp;&emsp;shortName|股票简称||false|string||
|&emsp;&emsp;symbol|股票代码||false|string||
|&emsp;&emsp;totalMarketCap|总市值||false|integer(int64)||
|&emsp;&emsp;totalShares|总股本||false|integer(int64)||
|&emsp;&emsp;updateTime|修改时间||false|string(date-time)||
|&emsp;&emsp;updateUser|修改人||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


# 管理端-用户管理接口


## 分页条件查询用户接口


**接口地址**:`/api/admin/user`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|begin|搜索起始时间|query|false|string(date-time)||
|email|邮箱|query|false|string||
|end|搜索终止时间|query|false|string(date-time)||
|icon|头像|query|false|string||
|isDeleted|是否已删除（0: 否，1: 是）|query|false|integer(int32)||
|nickName|昵称|query|false|string||
|phone|电话号|query|false|string||
|sex|性别（1: 男，2: 女）|query|false|integer(int32)||
|status|状态（0: 禁用、1: 正常）|query|false|integer(int32)||
|userType|简化身份标识（1: 普通用户，2: 机构用户，3: 管理员）|query|false|integer(int32)||
|username|登录用户名|query|false|string||
|current|current|query|false|integer(int64)||
|size|size|query|false|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 新增用户接口


**接口地址**:`/api/admin/user/add`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "email": "",
  "icon": "",
  "nickName": "",
  "password": "",
  "phone": "",
  "roleName": 0,
  "sex": 0,
  "status": 0,
  "userType": 0,
  "username": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|userDTO|userDTO|body|true|UserDTO|UserDTO|
|&emsp;&emsp;email|邮箱||false|string||
|&emsp;&emsp;icon|头像||false|string||
|&emsp;&emsp;nickName|昵称||false|string||
|&emsp;&emsp;password|密码||true|string||
|&emsp;&emsp;phone|电话号||false|string||
|&emsp;&emsp;roleName|管理员等级||false|integer(int32)||
|&emsp;&emsp;sex|性别（1: 男，2: 女）||false|integer(int32)||
|&emsp;&emsp;status|状态（0: 禁用，1: 正常）||false|integer(int32)||
|&emsp;&emsp;userType|简化身份标识（1: 普通用户，2: 机构用户，3: 管理员）||true|integer(int32)||
|&emsp;&emsp;username|登录用户名||true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 启用禁用用户账号


**接口地址**:`/api/admin/user/changeStatus/{id}`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|id|path|true|integer(int64)||
|status|status|query|true|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 更改用户信息接口


**接口地址**:`/api/admin/user/update`


**请求方式**:`PUT`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "email": "",
  "icon": "",
  "id": 0,
  "nickName": "",
  "password": "",
  "phone": "",
  "roleName": 0,
  "sex": 0,
  "status": 0,
  "userType": 0,
  "username": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|userUpdateDTO|userUpdateDTO|body|true|UserUpdateDTO|UserUpdateDTO|
|&emsp;&emsp;email|邮箱||false|string||
|&emsp;&emsp;icon|头像||false|string||
|&emsp;&emsp;id|用户id||true|integer(int64)||
|&emsp;&emsp;nickName|昵称||false|string||
|&emsp;&emsp;password|密码||false|string||
|&emsp;&emsp;phone|电话号||false|string||
|&emsp;&emsp;roleName|管理员等级||false|integer(int32)||
|&emsp;&emsp;sex|性别（1: 男，2: 女）||false|integer(int32)||
|&emsp;&emsp;status|状态（0: 禁用，1: 正常）||false|integer(int32)||
|&emsp;&emsp;userType|简化身份标识（1: 普通用户，2: 机构用户，3: 管理员）||false|integer(int32)||
|&emsp;&emsp;username|登录用户名||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 根据id查询用户接口


**接口地址**:`/api/admin/user/{id}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|用户id|path|false|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 删除用户接口


**接口地址**:`/api/admin/user/{id}`


**请求方式**:`DELETE`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|用户id|path|false|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|204|No Content||
|401|Unauthorized||
|403|Forbidden||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


# 股票搜索接口


## 搜索股票（支持代码前缀和名称模糊搜索）


**接口地址**:`/api/user/stock/search`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|keyword|keyword|query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


# 股票预测接口


## 预测股票未来一个月价格


**接口地址**:`/api/user/prediction/{symbol}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|symbol|symbol|path|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 导出excel表格


**接口地址**:`/api/user/prediction/{symbol}/get_excel`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|symbol|symbol|path|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


暂无


**响应示例**:
```javascript

```


# 自选股管理接口


## 添加自选股


**接口地址**:`/api/user/optional/add`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|symbol|symbol|query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 获取自选股列表


**接口地址**:`/api/user/optional/list`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 删除自选股


**接口地址**:`/api/user/optional/remove`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|symbol|symbol|query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


# 账户管理接口


## 创建模拟账户


**接口地址**:`/api/user/account/create`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 获取账户信息


**接口地址**:`/api/user/account/query_info`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 获取特定股票持仓


**接口地址**:`/api/user/account/query_position`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|symbol|symbol|query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 获取持仓列表


**接口地址**:`/api/user/account/query_positions`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```