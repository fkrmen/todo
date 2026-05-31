# Todo Master

基于 `Vue 3 + Element Plus + Spring Boot + MySQL` 的待办事项管理系统，包含普通用户端、管理员端、邮件提醒，以及新增待办时的“一句话智能解析”辅助能力。

## 当前版本重点

- 首页支持未登录浏览，只有真正执行添加任务等受保护操作时才跳转登录。
- 待办新增区已重构为“手动添加主入口 + 智能解析辅助入口”。
- 新增 `POST /api/todo/parse`，可将一句自然语言解析为 `标题 / 优先级 / 截止时间`，仅回填草稿，不落库。
- 智能解析由后端直连 DeepSeek，前端不暴露密钥。
- 截止时间改为“快捷时间胶囊 + 自定义时间”两段式交互。
- 首页 Hero Badge 已改为按时间显示问候语，例如 `上午好，张三`；访客仍显示 `访客浏览模式`。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3、Vue Router 4、Element Plus、Axios、Vite 5 |
| 后端 | Spring Boot 4、MyBatis-Plus、JWT、JavaMail |
| 数据库 | MySQL 8 |
| 运行环境 | Node.js 18+、Java 17 |

## 主要功能

### 用户侧

- 注册、登录、退出登录
- 待办新增、编辑、删除、完成状态切换
- 手动添加任务，支持标题、优先级、截止时间直接填写
- 一句话智能解析待办，自动回填标题、优先级、截止时间
- 截止时间快捷选择：`今天结束前 / 明天 / 3天内 / 一周内 / 清空`
- 个人中心维护昵称、QQ、邮箱、密码
- 公告查看、已读状态记录
- 到期前邮件提醒

### 管理员侧

- `/admin` 独立后台入口
- 用户启用/禁用
- 用户密码重置
- 强制用户下线
- 公告发布、编辑、删除

## 智能解析说明

### 接口

- `POST /api/todo/parse`
- 请求体：`{ "text": "明天下午3点提交周报，高优先级" }`
- 返回体：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "title": "提交周报",
    "level": "HIGH",
    "deadline": "2026-06-01T15:00:00"
  }
}
```

### 解析规则

- `title` 为空时回退为原始输入句子
- `level` 无法识别时回退为 `MEDIUM`
- `deadline` 无法判断、格式非法或解析失败时回退为 `null`
- 服务端时间基准为 `Asia/Shanghai`
- 缺少 `deepseek.api-key` 或 `deepseek.model` 时，接口返回 `503`
- 模型超时、非 2xx、非 JSON 等异常不会把原始错误暴露给前端，会统一走兜底草稿结果

### DeepSeek 配置

在 `back/src/main/resources/application.yml` 中配置：

```yml
deepseek:
  base-url: ${DEEPSEEK_BASE_URL:https://api.deepseek.com}
  api-key: ${DEEPSEEK_API_KEY:}
  model: ${DEEPSEEK_MODEL:}
  timeout-ms: ${DEEPSEEK_TIMEOUT_MS:15000}
```

建议使用环境变量或部署平台密钥管理，不要把真实密钥提交到版本库。

## 项目结构

```text
todo-project-claude/
├─ front/                  前端项目
│  ├─ src/api/             Axios 请求封装
│  ├─ src/composables/     登录态与用户信息
│  ├─ src/router/          路由与鉴权
│  ├─ src/styles/          全局主题
│  └─ src/views/           页面视图
├─ back/                   后端项目
│  ├─ src/main/java/com/fab/todo/config/
│  ├─ src/main/java/com/fab/todo/controller/
│  ├─ src/main/java/com/fab/todo/dto/
│  ├─ src/main/java/com/fab/todo/entity/
│  ├─ src/main/java/com/fab/todo/mapper/
│  ├─ src/main/java/com/fab/todo/service/
│  ├─ src/main/java/com/fab/todo/task/
│  └─ src/main/resources/
├─ db/                     数据库脚本
└─ work/                   交付材料、源码镜像、配置镜像
```

## 快速开始

### 1. 初始化数据库

```bash
mysql -u root -p < db/schema.sql
```

### 2. 配置后端

编辑 `back/src/main/resources/application.yml`，至少确认以下配置：

- MySQL 连接信息
- JWT 密钥
- SMTP 邮箱配置
- DeepSeek 配置

### 3. 启动后端

Windows：

```bash
cd back
mvnw.cmd spring-boot:run
```

Linux / macOS：

```bash
cd back
mvn spring-boot:run
```

默认地址：`http://localhost:8080`

### 4. 启动前端

```bash
cd front
npm install
npm run dev
```

默认地址：`http://localhost:3000`

- `/api` 会代理到 `http://localhost:8080`
- 如果前端出现 `ECONNREFUSED`，通常表示后端 `8080` 未启动

## 生产构建

### 前端构建

```bash
cd front
npm run build
```

### 后端打包

Windows：

```bash
cd back
mvnw.cmd -DskipTests package
```

Linux / macOS：

```bash
cd back
mvn -DskipTests package
```

后端 `pom.xml` 已配置将 `../front/dist` 打包进 JAR 的 `static/` 目录，所以更新前端后必须重新执行前端构建和后端打包。

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
| `/api/todo/parse` | POST | 是 | 一句话解析待办草稿 |
| `/api/todo/update/{id}` | PUT | 是 | 更新待办 |
| `/api/todo/toggle/{id}` | PUT | 是 | 切换完成状态 |
| `/api/todo/delete/{id}` | DELETE | 是 | 删除待办 |
| `/api/notice/latest` | GET | 是 | 最新公告 |
| `/api/admin/login` | POST | 否 | 管理员登录 |
| `/api/admin/users` | GET | ADMIN | 用户列表 |
| `/api/notice/all` | GET | ADMIN | 公告列表 |

## work 目录说明

`work/` 已同步为当前版本的交付目录，包含：

- `work/work.txt`：项目说明
- `work/newuse.txt`：更新部署说明
- `work/数据库脚本/`：数据库脚本镜像
- `work/源码/`：前后端源码镜像
- `work/配置文件/`：配置文件镜像

如果继续迭代源码，建议同时同步更新 `work/`，避免交付材料和真实工程版本不一致。
