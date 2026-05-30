# Todo List 待办事项管理系统 — CentOS 7 部署文档

## 项目概述

- **技术栈**: Vue3 + Spring Boot 2.7.x + MyBatis-Plus + MySQL 8.0
- **后端包名**: com.fab.todo
- **数据库**: fab_todo_db（表：fab_user、fab_todo、fab_notice）
- **默认管理员**: admin / admin123
- **端口**: 8080（统一端口，前端打包放入后端 static 目录）

---

## 一、环境安装

### 1.1 安装 JDK 11

```bash
# 查看是否已安装
java -version

# 安装 OpenJDK 11
sudo yum install -y java-11-openjdk-devel

# 验证安装
java -version

# 设置 JAVA_HOME（如未自动设置）
echo 'export JAVA_HOME=/usr/lib/jvm/java-11-openjdk' | sudo tee -a /etc/profile
echo 'export PATH=$JAVA_HOME/bin:$PATH' | sudo tee -a /etc/profile
source /etc/profile
```

### 1.2 安装 MySQL 8.0

```bash
# 添加 MySQL 官方 YUM 仓库
sudo yum install -y https://dev.mysql.com/get/mysql80-community-release-el7-3.noarch.rpm

# 安装 MySQL 8.0
sudo yum install -y mysql-community-server

# 启动 MySQL
sudo systemctl start mysqld
sudo systemctl enable mysqld

# 查看临时密码
sudo grep 'temporary password' /var/log/mysqld.log

# 运行安全配置（按提示修改 root 密码）
sudo mysql_secure_installation

# 登录 MySQL 测试（替换为你的 root 密码）
mysql -u root -p
```

登录 MySQL 后执行 `db/schema.sql`：

```sql
source /path/to/todo-project-claude/db/schema.sql;
-- 或者粘贴 SQL 内容执行
```

### 1.3 安装 Maven

```bash
# 下载 Maven 3.9.x
wget https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz

# 解压到 /opt
sudo tar -xzf apache-maven-3.9.6-bin.tar.gz -C /usr/local/
sudo ln -s /usr/local/apache-maven-3.9.6 /usr/local/maven

# 配置环境变量
echo 'export MAVEN_HOME=/usr/local/maven' | sudo tee -a /etc/profile
echo 'export PATH=$MAVEN_HOME/bin:$PATH' | sudo tee -a /etc/profile
source /etc/profile

# 验证
mvn -version
```

### 1.4 安装 Node.js 16

```bash
# 使用 NodeSource 安装 Node.js 16
curl -fsSL https://rpm.nodesource.com/setup_16.x | sudo bash -
sudo yum install -y nodejs

# 验证
node -v
npm -v
```

---

## 二、项目配置

### 2.1 修改配置

编辑 `back/src/main/resources/application.yml`，修改以下配置：

```yaml
spring:
  datasource:
    password: your_mysql_password  # ← 改为你的 MySQL root 密码
  mail:
    username: your_qq@qq.com          # ← 改为你的 QQ 邮箱
    password: your_authorization_code # ← 改为 QQ 邮箱 SMTP 授权码
```

**QQ 邮箱 SMTP 授权码获取方法**：
1. 登录 QQ 邮箱 → 设置 → 账户
2. 找到「POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务」
3. 开启「SMTP服务」，生成授权码

### 2.2 导入数据库

```bash
mysql -u root -p < db/schema.sql
```

---

## 三、构建与运行

### 3.1 构建前端

```bash
cd front
npm install
npm run build

# 验证构建产物
ls -la dist/
```

### 3.2 构建后端（含前端静态资源）

```bash
cd back

# 清理旧构建
mvn clean

# 复制前端 dist 到后端 static
cp -r ../front/dist src/main/resources/static

# 打包（生成 fat JAR）
mvn package -DskipTests

# 验证
ls -la target/fab-todo.jar
```

### 3.3 运行

```bash
# 直接运行
java -jar target/fab-todo.jar

# 后台运行（nohup）
nohup java -jar target/fab-todo.jar > app.log 2>&1 &
```

### 3.4 配置 systemd 开机自启

```bash
sudo tee /etc/systemd/system/fab-todo.service << 'EOF'
[Unit]
Description=Fab Todo List Application
After=network.target mysqld.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/fab-todo
ExecStart=/usr/bin/java -jar /opt/fab-todo/fab-todo.jar
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

# 部署 jar 到 /opt/fab-todo
sudo mkdir -p /opt/fab-todo
sudo cp back/target/fab-todo.jar /opt/fab-todo/

# 启用并启动服务
sudo systemctl daemon-reload
sudo systemctl enable fab-todo
sudo systemctl start fab-todo

# 查看状态
sudo systemctl status fab-todo

# 查看日志
journalctl -u fab-todo -f
```

---

## 四、防火墙配置

```bash
# 开放 8080 端口
sudo firewall-cmd --zone=public --add-port=8080/tcp --permanent
sudo firewall-cmd --reload

# 验证端口
sudo firewall-cmd --list-ports
```

如果使用云服务器，还需在云控制台的「安全组」中开放 8080 端口。

---

## 五、访问系统

- **用户端**: http://服务器IP:8080（前端页面）
  - 登录/注册用户 → 待办列表 → 个人中心
- **管理端**: http://服务器IP:8080/#/admin
  - 管理员登录 → 用户管理 + 公告管理

### API 接口汇总

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/user/register | POST | 用户注册 |
| /api/user/login | POST | 用户登录 |
| /api/user/info | GET | 获取用户信息 |
| /api/user/email | PUT | 更新邮箱 |
| /api/user/password | PUT | 修改密码 |
| /api/todo/list | GET | 获取待办列表 |
| /api/todo/create | POST | 新增待办 |
| /api/todo/update/{id} | PUT | 更新待办 |
| /api/todo/toggle/{id} | PUT | 切换完成状态 |
| /api/todo/delete/{id} | DELETE | 删除待办 |
| /api/notice/latest | GET | 获取最新公告 |
| /api/admin/login | POST | 管理员登录 |
| /api/admin/users | GET | 用户列表（管理员） |
| /api/admin/users/{id}/toggle-status | PUT | 启用/禁用用户 |
| /api/admin/users/{id}/reset-password | PUT | 重置密码 |
| /api/admin/users/{id}/force-logout | PUT | 强制下线 |
| /api/notice/all | GET | 全部公告（管理员） |
| /api/notice/create | POST | 发布公告（管理员） |
| /api/notice/update/{id} | PUT | 编辑公告（管理员） |
| /api/notice/delete/{id} | DELETE | 删除公告（管理员） |

---

## 六、测试邮件提醒

### 6.1 准备工作

1. 用户登录后在「个人中心」填写有效邮箱地址
2. 管理员在后台添加一条待办并设置截止时间（当前～未来1小时内）

### 6.2 手动触发

系统默认每 5 分钟自动扫描。也可重启后观察日志：

```bash
journalctl -u fab-todo -f
# 或
tail -f /opt/fab-todo/app.log
```

### 6.3 手动快速测试

创建一条截止时间为 2 分钟后的待办，等待定时任务扫描。

---

## 七、常见问题

### 7.1 MySQL 连接失败

```bash
# 检查 MySQL 是否运行
systemctl status mysqld

# 检查 root 密码是否正确
mysql -u root -p

# 检查 MySQL 远程访问设置
mysql -u root -p -e "SELECT user, host FROM mysql.user WHERE user='root';"
```

### 7.2 端口被占用

```bash
# 查看 8080 端口占用
netstat -tlnp | grep 8080

# 修改 application.yml 中的 server.port
```

### 7.3 邮件发送失败

- 确认 QQ 邮箱 SMTP 授权码正确
- 确认用户已填写邮箱地址
- 查看日志：`journalctl -u fab-todo -f | grep 提醒任务`
