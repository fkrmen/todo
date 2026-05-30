# Todo Master

基于 `Vue 3 + Element Plus + Spring Boot + MySQL` 的全栈待办事项管理系统。

当前版本已经完成以下更新：
- 前端首页、登录页、个人中心、管理后台 UI 统一重构
- 首页允许未登录浏览，只有点击“添加任务”时才跳转登录页
- 个人中心支持 QQ、昵称、邮箱、密码独立维护
- 邮箱未变化时不扣修改次数，昵称为空时阻止保存
- 管理后台支持用户状态管理、密码重置、强制下线、公告发布
- 首页底部已增加版权信息 `By Dormitory-111`
- 前端构建已调整为稳定分包，避免 Element Plus 循环依赖导致的后台白屏

## 技术栈

| 层级 | 技术 | 说明 |
|------|------|------|
| 前端 | Vue 3.4、Vue Router 4、Element Plus、Axios、Vite 5 | 单页应用，已做 UI 重构与稳定分包 |
| 后端 | Spring Boot 4.0.6、MyBatis-Plus 3.5.9、JWT、JavaMail | REST API、定时提醒、管理员功能 |
| 数据库 | MySQL 8.0 | `fab_user`、`fab_todo`、`fab_notice` 三张核心表 |

## 目录结构

```text
todo-project-claude/
├─ front/                   前端项目
│  ├─ src/
│  │  ├─ api/               Axios 请求封装
│  │  ├─ composables/       登录态与用户信息管理
│  │  ├─ router/            路由与鉴权
│  │  ├─ styles/            全局主题样式
│  │  └─ views/             页面与后台页面
│  ├─ package.json
│  └─ vite.config.js
├─ back/                    后端项目
│  ├─ src/main/java/com/fab/todo/
│  │  ├─ config/            拦截器、MyBatis、自动填充
│  │  ├─ controller/        用户、待办、公告、后台接口
│  │  ├─ dto/               请求 DTO
│  │  ├─ entity/            实体类
│  │  ├─ mapper/            MyBatis-Plus Mapper
│  │  ├─ service/           业务服务
│  │  ├─ task/              定时提醒任务
│  │  └─ utils/             JWT 工具
│  ├─ src/main/resources/application.yml
│  └─ pom.xml
├─ db/
│  └─ schema.sql            建库建表 SQL
└─ work/                    课程/交付材料与镜像源码
```

## 功能概览

### 用户侧
- 首页可直接浏览，未登录不会被强制跳转到登录页
- 待办事项新增、编辑、删除、完成状态切换
- 优先级与截止时间设置
- 个人中心维护 QQ、昵称、邮箱、密码
- 邮箱每天最多修改 3 次，未变化时不扣次数
- 公告查看与已读跟踪
- 到期前邮件提醒

### 管理员侧
- `/admin` 独立后台入口
- 用户列表、启用/禁用、密码重置、强制下线
- 公告发布、编辑、删除
- 后台页面白屏问题已修复

## 环境要求

- Node.js 18+
- Java 17
- MySQL 8.0

## 快速开始

### 1. 初始化数据库

```bash
mysql -u root -p < db/schema.sql
```

### 2. 配置后端

编辑 `back/src/main/resources/application.yml`：
- 数据库连接
- JWT 密钥
- SMTP 邮箱配置

### 3. 启动后端

```bash
cd back
mvnw.cmd spring-boot:run
```

如果你在 Linux / macOS 环境，没有 `mvnw` 包装脚本时可直接使用：

```bash
cd back
mvn spring-boot:run
```

默认地址：
- 后端接口：`http://localhost:8080`

### 4. 启动前端

```bash
cd front
npm install
npm run dev
```

默认地址：
- 前端开发服务器：`http://localhost:3000`
- `/api` 会代理到 `http://localhost:8080`

如果前端出现 `ECONNREFUSED`，通常表示后端 `8080` 没启动。

## 生产构建

### 前端

```bash
cd front
npm run build
```

### 后端打包

```bash
cd back
mvnw.cmd -DskipTests package
```

Linux / macOS 可使用：

```bash
cd back
mvn -DskipTests package
```

后端 `pom.xml` 已配置将 `../front/dist` 打包进 JAR 的 `static/` 目录。

生产环境更新前端后，必须重新执行这两步：

```bash
cd front
npm run build

cd ../back
mvn -DskipTests package
```

否则 `8080` 仍可能继续提供旧版静态资源。

## 部署验证

服务更新后可用以下命令确认服务器是否已经加载新版前端：

```bash
curl http://127.0.0.1:8080/ | head -20
curl -I http://127.0.0.1:8080/assets/index-DS38Yaji.js
curl -I http://127.0.0.1:8080/assets/element-plus-BAxHmODt.js
```

如果后台仍白屏，先打开浏览器开发者工具查看 `Console`，再重点检查是否存在旧版 chunk 缓存或脚本运行错误。

## 默认管理员

系统首次启动时会自动创建管理员：

| 用户名 | 密码 |
|--------|------|
| admin | admin123 |

## 主要接口

| 接口 | 方法 | 认证 | 说明 |
|------|------|------|------|
| `/api/user/register` | POST | 否 | 用户注册 |
| `/api/user/login` | POST | 否 | 用户登录 |
| `/api/user/info` | GET | 是 | 当前用户信息 |
| `/api/user/profile` | PUT | 是 | 更新 QQ / 昵称 |
| `/api/user/email` | PUT | 是 | 更新邮箱 |
| `/api/user/password` | PUT | 是 | 修改密码 |
| `/api/user/notice-seen` | PUT | 是 | 标记公告已读 |
| `/api/user/has-unread` | GET | 是 | 是否有未读公告 |
| `/api/todo/list` | GET | 是 | 待办列表 |
| `/api/todo/create` | POST | 是 | 创建待办 |
| `/api/todo/update/{id}` | PUT | 是 | 更新待办 |
| `/api/todo/toggle/{id}` | PUT | 是 | 切换完成状态 |
| `/api/todo/delete/{id}` | DELETE | 是 | 删除待办 |
| `/api/notice/latest` | GET | 是 | 最新公告 |
| `/api/admin/login` | POST | 否 | 管理员登录 |
| `/api/admin/users` | GET | ADMIN | 用户列表 |
| `/api/notice/all` | GET | ADMIN | 公告列表 |

## work 目录说明

`work/` 目录已同步为当前版本的提交材料，包含：
- 更新后的课程说明文档
- 当前可导入的数据库脚本
- 当前前后端源码镜像
- 当前后端配置文件与 Linux `systemd` 部署 service 文件

如果你后续继续修改源码，记得同时同步 `work/源码` 和 `work/配置文件`。
