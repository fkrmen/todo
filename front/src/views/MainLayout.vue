<template>
  <div class="main-layout">
    <header class="top-bar">
      <div class="top-left" @click="router.push('/todos')">
        <div class="brand-mark">
          <el-icon :size="20"><List /></el-icon>
        </div>
        <div class="brand-copy">
          <span class="logo-text">Todo 待办</span>
          <span class="logo-subtitle">By Dormitory-111</span>
        </div>
      </div>

      <div class="top-right">
        <template v-if="isLoggedIn">
          <button class="notice-trigger" type="button" title="系统公告" @click="toggleNoticePanel">
            <el-icon :size="18"><Bell /></el-icon>
            <span v-show="notificationCount > 0" class="notice-dot"></span>
          </button>

          <el-dropdown trigger="click" @command="handleCommand" class="user-dropdown">
            <button class="user-dropdown-trigger" type="button">
              <el-avatar v-if="avatarUrl" :size="34" :src="avatarUrl" class="user-avatar" />
              <el-avatar v-else :size="34" :icon="UserFilled" class="user-avatar fallback-avatar" />
              <div class="user-copy">
                <span class="username-text">{{ displayName }}</span>
                <span class="role-text">{{ isAdmin ? '管理员' : '个人账户' }}</span>
              </div>
              <el-icon class="arrow-icon"><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item v-if="isAdmin" command="admin">
                  <el-icon><Setting /></el-icon>管理后台
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>

        <template v-else>
          <el-button class="browse-btn" text @click="router.push('/todos')">先逛首页</el-button>
          <el-button type="primary" round class="login-top-btn" @click="router.push('/login')">
            <el-icon><User /></el-icon>
            登录 / 注册
          </el-button>
        </template>
      </div>
    </header>

    <main class="content-area">
      <router-view />
    </main>

    <footer v-if="route.path === '/todos'" class="page-footer">
      <span class="footer-copy">By Dormitory-111</span>
    </footer>

    <el-drawer v-model="noticePanelVisible" title="系统公告" direction="rtl" size="400px" class="notice-drawer">
      <div v-if="notices.length === 0" class="empty-notices">
        <el-icon :size="48" color="#dcdfe6"><ChatLineSquare /></el-icon>
        <p>暂无公告</p>
        <span>有新的系统消息时会显示在这里</span>
      </div>
      <div v-for="notice in notices" :key="notice.id" class="notice-card">
        <h4>{{ notice.title }}</h4>
        <span class="notice-time">{{ formatDate(notice.createdAt) }}</span>
        <p class="notice-text">{{ notice.content }}</p>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import request from '../api/request'
import { useAuth } from '../composables/useAuth'

const router = useRouter()
const route = useRoute()
const { avatarUrl, clearLogin, displayName, isAdmin, isLoggedIn } = useAuth()

const notificationCount = ref(0)
const notices = ref([])
const noticePanelVisible = ref(false)

function formatDate(value) {
  if (!value) {
    return ''
  }

  const date = new Date(value)
  const pad = num => String(num).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

async function fetchUnreadStatus() {
  if (!isLoggedIn.value) {
    notificationCount.value = 0
    return
  }

  const res = await request.get('/user/has-unread')
  if (res.code === 200) {
    notificationCount.value = res.data?.hasUnread ? 1 : 0
  }
}

async function fetchNotices() {
  const res = await request.get('/notice/latest')
  if (res.code === 200) {
    notices.value = res.data || []
  }
}

function toggleNoticePanel() {
  noticePanelVisible.value = !noticePanelVisible.value
  if (!noticePanelVisible.value) {
    return
  }

  fetchNotices()
  notificationCount.value = 0
  request.put('/user/notice-seen').catch(() => {})
}

function handleCommand(command) {
  if (command === 'profile') {
    router.push('/profile')
    return
  }

  if (command === 'admin') {
    router.push('/admin')
    return
  }

  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => {
        clearLogin()
        router.push('/todos')
      })
      .catch(() => {})
  }
}

watch(isLoggedIn, value => {
  if (value) {
    fetchUnreadStatus()
  } else {
    notificationCount.value = 0
    notices.value = []
  }
})

onMounted(() => {
  if (isLoggedIn.value) {
    fetchUnreadStatus()
  }
})
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background:
    radial-gradient(circle at top left, rgba(64, 158, 255, 0.1), transparent 28%),
    linear-gradient(180deg, #f8fbff 0%, #f5f7fa 22%, #f5f7fa 100%);
}

.top-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  min-height: 64px;
  padding: 12px 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  backdrop-filter: blur(14px);
  background: rgba(255, 255, 255, 0.82);
  border-bottom: 1px solid rgba(64, 158, 255, 0.08);
}

.top-left {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  user-select: none;
}

.brand-mark {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  color: var(--primary);
  background: linear-gradient(135deg, #ecf5ff 0%, #d8ebff 100%);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.brand-copy {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 0.02em;
}

.logo-subtitle {
  font-size: 12px;
  color: var(--text-tertiary);
}

.top-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notice-trigger,
.user-dropdown-trigger {
  border: none;
  outline: none;
  background: transparent;
}

.notice-trigger {
  position: relative;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  cursor: pointer;
  color: var(--text-tertiary);
  transition: all 0.2s;
}

.notice-trigger:hover {
  color: var(--primary);
  background: rgba(64, 158, 255, 0.1);
}

.notice-dot {
  position: absolute;
  top: 7px;
  right: 7px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--danger);
  border: 2px solid #fff;
}

.user-dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 6px 4px 4px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.2s;
}

.user-dropdown-trigger:hover {
  background: rgba(64, 158, 255, 0.08);
}

.user-avatar {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.14);
}

.fallback-avatar {
  background: #f3f7fb;
  color: var(--primary);
}

.user-copy {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
}

.username-text {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.role-text {
  font-size: 12px;
  color: var(--text-tertiary);
}

.arrow-icon {
  font-size: 12px;
  color: var(--text-tertiary);
}

.browse-btn {
  color: var(--text-secondary);
}

.login-top-btn {
  height: 38px;
  padding: 0 18px;
  font-weight: 600;
}

.content-area {
  flex: 1;
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 20px 40px;
}

.page-footer {
  display: flex;
  justify-content: center;
  padding: 0 20px 28px;
}

.footer-copy {
  font-size: 13px;
  letter-spacing: 0.08em;
  color: var(--text-tertiary);
}

.empty-notices {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  text-align: center;
  color: var(--text-tertiary);
  padding: 56px 0;
}

.empty-notices span {
  font-size: 13px;
  color: var(--text-disabled);
}

.notice-card {
  margin-bottom: 12px;
  padding: 16px;
  border-radius: 12px;
  background: var(--bg);
}

.notice-card h4 {
  margin: 0 0 6px;
  font-size: 15px;
  color: var(--text-primary);
}

.notice-time {
  font-size: 12px;
  color: var(--text-disabled);
}

.notice-text {
  margin: 8px 0 0;
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  white-space: pre-wrap;
}

@media (max-width: 640px) {
  .top-bar {
    padding: 12px 16px;
  }

  .logo-subtitle,
  .role-text,
  .browse-btn {
    display: none;
  }

  .username-text {
    max-width: 72px;
  }

  .content-area {
    padding: 24px 16px 32px;
  }

  .page-footer {
    padding: 0 16px 24px;
  }
}
</style>
