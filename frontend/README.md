# 住宅小区互助寻物系统 - 前端

这是「住宅小区互助寻物系统」的前端部分，基于 Vue 3 + TypeScript + Vite + Element Plus 开发。

## 项目技术栈

| 技术栈         | 版本     | 说明                                   |
|---------------|---------|---------------------------------------|
| **Vue.js**    | 3.4+    | 渐进式 JavaScript 框架                  |
| **Vue Router**| 4.2+    | Vue.js 的官方路由                       |
| **Pinia**     | 3.0+    | Vue 的状态管理库，Vuex 的替代品           |
| **Element Plus** | 2.9.6+ | 基于 Vue 3 的桌面端组件库              |
| **Vite**      | 6.0+    | 现代前端构建工具                         |
| **TypeScript**| 5.0+    | JavaScript 的类型超集                   |
| **Axios**     | 1.6+    | 基于 Promise 的 HTTP 客户端             |

## 项目结构

```
frontend/
├── public/            # 静态资源
├── src/               # 源代码
│   ├── assets/        # 项目资源文件
│   ├── components/    # 公共组件
│   │   └── layout/    # 布局组件
│   ├── router/        # 路由配置
│   ├── stores/        # Pinia 状态管理
│   ├── views/         # 页面组件
│   │   ├── admin/     # 管理员页面
│   │   ├── announcements/ # 公告页面
│   │   ├── auth/      # 认证页面
│   │   ├── found-items/ # 失物招领页面
│   │   ├── forum/     # 邻里论坛页面
│   │   ├── lost-items/ # 寻物启事页面
│   │   └── profile/   # 个人中心页面
│   ├── App.vue        # 根组件
│   └── main.ts        # 入口文件
├── .gitignore         # Git 忽略文件
├── index.html         # HTML 入口
├── package.json       # 项目依赖
├── tsconfig.json      # TypeScript 配置
└── vite.config.ts     # Vite 配置
```

## 功能特点

### 用户功能
- 浏览公告
- 寻物启事发布、查看、修改、删除
- 失物招领发布、查看、修改、删除
- 物品认领与交流
- 邻里论坛发帖、回复
- 个人中心管理

### 管理员功能
- 公告管理
- 举报处理
- 认领管理

### 系统管理员功能
- 用户账户管理

## 开发环境搭建

1. 安装依赖

```bash
npm install
```

2. 启动本地开发服务

```bash
npm run dev
```

3. 构建生产版本

```bash
npm run build
```

4. 代码格式化

```bash
npm run format
```

## 项目预览

本地开发服务启动后，访问 http://localhost:5173/ 即可预览项目。

## 代码规范

- 使用 ESLint 和 Prettier 保持代码风格一致
- 组件名称使用 PascalCase
- 文件名称使用 kebab-case
- 状态管理使用 Pinia
- 使用 TypeScript 类型声明

## API 接口

项目中的 API 接口默认配置为连接后端服务，路径前缀为 `/api`。在开发环境中，目前使用模拟数据进行开发。

## 浏览器兼容性

- 推荐使用 Chrome、Firefox、Safari、Edge 等现代浏览器
- 不支持 IE11 及以下版本

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 打开 Pull Request

## 许可证

[MIT](https://opensource.org/licenses/MIT)
