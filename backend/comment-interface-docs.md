# 留言接口文档

## 1. 创建留言

### 接口描述
创建一条新的留言，用于在寻物启事或失物招领物品下发表评论。

### 请求方法
`POST`

### 请求路径
`/api/comments`

### 请求头
需要包含用户认证token：
```
Authorization: Bearer {token}
```

### 请求参数
| 参数名 | 类型 | 是否必须 | 描述 |
|-------|-----|---------|------|
| content | String | 是 | 留言内容，不能为空，最大500个字符 |
| itemId | Long | 是 | 物品ID，关联的寻物启事或失物招领ID |
| itemType | String | 是 | 物品类型，必须为'lost'(寻物启事)或'found'(失物招领) |

### 请求示例
```json
{
  "content": "我在学校食堂看到过类似的物品，可以私信我详细交流。",
  "itemId": 123,
  "itemType": "lost"
}
```

### 响应参数
| 参数名 | 类型 | 描述 |
|-------|-----|------|
| code | Integer | 状态码，200表示成功 |
| message | String | 消息描述 |
| data | Object | 返回的数据对象 |
| data.id | Long | 留言ID |
| data.content | String | 留言内容 |
| data.itemId | Long | 物品ID |
| data.itemType | String | 物品类型：lost或found |
| data.userId | Long | 发布留言的用户ID |
| data.username | String | 用户名 |
| data.userAvatar | String | 用户头像URL |
| data.createdAt | DateTime | 创建时间 |

### 响应示例
```json
{
  "code": 200,
  "message": "留言创建成功",
  "data": {
    "id": 456,
    "content": "我在学校食堂看到过类似的物品，可以私信我详细交流。",
    "itemId": 123,
    "itemType": "lost",
    "userId": 789,
    "username": "张三",
    "userAvatar": "https://example.com/avatar/user789.jpg",
    "createdAt": "2023-10-01T14:30:00"
  }
}
```

## 2. 获取物品留言列表（分页）

### 接口描述
获取指定物品（寻物启事或失物招领）的留言列表，支持分页。

### 请求方法
`GET`

### 请求路径
`/api/comments`

### 请求参数
| 参数名 | 类型 | 是否必须 | 描述 |
|-------|-----|---------|------|
| itemId | Long | 是 | 物品ID |
| itemType | String | 是 | 物品类型，'lost'(寻物启事)或'found'(失物招领) |
| page | Integer | 否 | 页码，默认为1 |
| size | Integer | 否 | 每页条数，默认为10 |

### 响应参数
| 参数名 | 类型 | 描述 |
|-------|-----|------|
| code | Integer | 状态码，200表示成功 |
| message | String | 消息描述 |
| data | Object | 返回的数据对象 |
| data.comments | Array | 留言列表 |
| data.comments[].id | Long | 留言ID |
| data.comments[].content | String | 留言内容 |
| data.comments[].itemId | Long | 物品ID |
| data.comments[].itemType | String | 物品类型 |
| data.comments[].userId | Long | 发布留言的用户ID |
| data.comments[].username | String | 用户名 |
| data.comments[].userAvatar | String | 用户头像URL |
| data.comments[].createdAt | DateTime | 创建时间 |
| data.currentPage | Integer | 当前页码 |
| data.pageSize | Integer | 每页条数 |
| data.totalItems | Long | 总条数 |
| data.totalPages | Integer | 总页数 |

### 响应示例
```json
{
  "code": 200,
  "message": "查询留言列表成功",
  "data": {
    "comments": [
      {
        "id": 456,
        "content": "我在学校食堂看到过类似的物品，可以私信我详细交流。",
        "itemId": 123,
        "itemType": "lost",
        "userId": 789,
        "username": "张三",
        "userAvatar": "https://example.com/avatar/user789.jpg",
        "createdAt": "2023-10-01T14:30:00"
      },
      {
        "id": 457,
        "content": "请问物品是什么颜色的？",
        "itemId": 123,
        "itemType": "lost",
        "userId": 790,
        "username": "李四",
        "userAvatar": "https://example.com/avatar/user790.jpg",
        "createdAt": "2023-10-01T14:35:00"
      }
    ],
    "currentPage": 1,
    "pageSize": 10,
    "totalItems": 2,
    "totalPages": 1
  }
}
```

## 3. 获取物品的所有留言（不分页）

### 接口描述
获取指定物品的所有留言，不进行分页处理。

### 请求方法
`GET`

### 请求路径
`/api/comments/all`

### 请求参数
| 参数名 | 类型 | 是否必须 | 描述 |
|-------|-----|---------|------|
| itemId | Long | 是 | 物品ID |
| itemType | String | 是 | 物品类型，'lost'(寻物启事)或'found'(失物招领) |

### 响应参数
| 参数名 | 类型 | 描述 |
|-------|-----|------|
| code | Integer | 状态码，200表示成功 |
| message | String | 消息描述 |
| data | Array | 留言列表 |
| data[].id | Long | 留言ID |
| data[].content | String | 留言内容 |
| data[].itemId | Long | 物品ID |
| data[].itemType | String | 物品类型 |
| data[].userId | Long | 发布留言的用户ID |
| data[].username | String | 用户名 |
| data[].userAvatar | String | 用户头像URL |
| data[].createdAt | DateTime | 创建时间 |

## 4. 根据ID获取留言

### 接口描述
根据留言ID获取特定留言详情。

### 请求方法
`GET`

### 请求路径
`/api/comments/{id}`

### 路径参数
| 参数名 | 类型 | 是否必须 | 描述 |
|-------|-----|---------|------|
| id | Long | 是 | 留言ID |

### 响应参数
| 参数名 | 类型 | 描述 |
|-------|-----|------|
| code | Integer | 状态码，200表示成功 |
| message | String | 消息描述 |
| data | Object | 留言对象 |
| data.id | Long | 留言ID |
| data.content | String | 留言内容 |
| data.itemId | Long | 物品ID |
| data.itemType | String | 物品类型 |
| data.userId | Long | 发布留言的用户ID |
| data.username | String | 用户名 |
| data.userAvatar | String | 用户头像URL |
| data.createdAt | DateTime | 创建时间 |

## 5. 删除留言

### 接口描述
删除指定的留言，只有留言的发布者才能删除自己的留言。

### 请求方法
`DELETE`

### 请求路径
`/api/comments/{id}`

### 请求头
需要包含用户认证token：
```
Authorization: Bearer {token}
```

### 路径参数
| 参数名 | 类型 | 是否必须 | 描述 |
|-------|-----|---------|------|
| id | Long | 是 | 留言ID |

### 响应参数
| 参数名 | 类型 | 描述 |
|-------|-----|------|
| code | Integer | 状态码，200表示成功 |
| message | String | 消息描述 |
| data | null | 无数据返回 |

### 响应示例
```json
{
  "code": 200,
  "message": "留言删除成功",
  "data": null
}
```

## 6. 获取用户自己的留言列表

### 接口描述
获取当前登录用户发布的所有留言列表。

### 请求方法
`GET`

### 请求路径
`/api/comments/my-comments`

### 请求头
需要包含用户认证token：
```
Authorization: Bearer {token}
```

### 响应参数
| 参数名 | 类型 | 描述 |
|-------|-----|------|
| code | Integer | 状态码，200表示成功 |
| message | String | 消息描述 |
| data | Array | 留言列表 |
| data[].id | Long | 留言ID |
| data[].content | String | 留言内容 |
| data[].itemId | Long | 物品ID |
| data[].itemType | String | 物品类型 |
| data[].userId | Long | 发布留言的用户ID |
| data[].username | String | 用户名 |
| data[].userAvatar | String | 用户头像URL |
| data[].createdAt | DateTime | 创建时间 |

### 响应示例
```json
{
  "code": 200,
  "message": "查询留言列表成功",
  "data": [
    {
      "id": 456,
      "content": "我在学校食堂看到过类似的物品，可以私信我详细交流。",
      "itemId": 123,
      "itemType": "lost",
      "userId": 789,
      "username": "张三",
      "userAvatar": "https://example.com/avatar/user789.jpg",
      "createdAt": "2023-10-01T14:30:00"
    },
    {
      "id": 458,
      "content": "这个物品我之前好像看到过。",
      "itemId": 124,
      "itemType": "found",
      "userId": 789,
      "username": "张三",
      "userAvatar": "https://example.com/avatar/user789.jpg",
      "createdAt": "2023-10-02T10:15:00"
    }
  ]
}
```

## 错误响应

### 401 未授权
当用户未登录或token失效时：
```json
{
  "code": 401,
  "message": "未授权操作，请先登录",
  "data": null
}
```

### 403 禁止访问
当用户尝试删除不属于自己的留言时：
```json
{
  "code": 403,
  "message": "留言删除失败，可能不存在或无权删除",
  "data": null
}
```

### 404 资源不存在
当请求的留言不存在时：
```json
{
  "code": 404,
  "message": "留言不存在",
  "data": null
}
```

### 400 参数错误
当请求参数不合法时：
```json
{
  "code": 400,
  "message": "请求参数错误",
  "data": {
    "content": "留言内容不能为空",
    "itemType": "物品类型必须为'lost'或'found'"
  }
}
``` 