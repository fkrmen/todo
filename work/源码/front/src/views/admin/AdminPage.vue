<template>
  <div class="admin-standalone">
    <div v-if="!isAdminLoggedIn" class="admin-login-page">
      <div class="ambient ambient-left"></div>
      <div class="ambient ambient-right"></div>

      <div class="login-shell">
        <section class="hero-panel">
          <span class="hero-badge">管理视角</span>
          <h1>统一管理用户、公告和后台操作。</h1>
          <p>这里是 Todo 待办的管理员入口。登录后可以查看用户状态、发布公告、重置密码和强制下线。</p>
          <div class="hero-points">
            <span>用户状态管理</span>
            <span>系统公告发布</span>
            <span>安全操作入口</span>
          </div>
          <el-button class="ghost-home-btn" round @click="router.push('/todos')">返回首页</el-button>
        </section>

        <section class="login-card">
          <div class="card-head">
            <div class="card-mark">
              <el-icon :size="20"><Setting /></el-icon>
            </div>
            <div>
              <h2>管理员登录</h2>
              <p>使用管理员账户进入后台。</p>
            </div>
          </div>

          <div class="admin-badge">
            <el-icon><Lock /></el-icon>
            <span>仅管理员可访问</span>
          </div>

          <div class="card-body">
            <el-form ref="adminFormRef" :model="adminForm" :rules="adminRules">
              <el-form-item prop="username">
                <el-input
                  v-model="adminForm.username"
                  placeholder="请输入管理员账号"
                  size="large"
                  :prefix-icon="User"
                  class="custom-input"
                />
              </el-form-item>
              <el-form-item prop="password">
                <el-input
                  v-model="adminForm.password"
                  type="password"
                  placeholder="请输入管理员密码"
                  size="large"
                  :prefix-icon="Lock"
                  show-password
                  class="custom-input"
                  @keyup.enter="handleAdminLogin"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleAdminLogin">
                  进入后台
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </section>
      </div>
    </div>

    <template v-else>
      <header class="admin-topbar">
        <div class="topbar-left">
          <div class="brand-mark">
            <el-icon :size="18"><Setting /></el-icon>
          </div>
          <div class="title-group">
            <span class="topbar-title">Todo 管理后台</span>
            <span class="topbar-subtitle"></span>
          </div>
        </div>
        <div class="topbar-right">
          <span class="admin-user">{{ username }}</span>
          <el-button text :icon="ArrowLeft" @click="router.push('/todos')">返回首页</el-button>
          <el-button text type="danger" @click="handleLogout">退出登录</el-button>
        </div>
      </header>

      <div class="admin-content">
        <AdminDashboard />
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Lock, User } from '@element-plus/icons-vue'
import request from '../../api/request'
import { useAuth } from '../../composables/useAuth'
import AdminDashboard from './AdminDashboard.vue'

const router = useRouter()
const { clearLogin, isAdmin, isLoggedIn, setLogin, username } = useAuth()

const isAdminLoggedIn = computed(() => isLoggedIn.value && isAdmin.value)
const loading = ref(false)
const adminFormRef = ref(null)

const adminForm = reactive({
  username: 'admin',
  password: ''
})

const adminRules = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入管理员密码', trigger: 'blur' }]
}

function handleAdminLogin() {
  adminFormRef.value?.validate(async valid => {
    if (!valid) {
      return
    }

    loading.value = true
    try {
      const res = await request.post('/admin/login', adminForm)
      if (res.code === 200) {
        setLogin(res.data)
        ElMessage.success('管理员登录成功')
      } else {
        ElMessage.error(res.msg || '登录失败')
      }
    } finally {
      loading.value = false
    }
  })
}

function handleLogout() {
  clearLogin()
  router.push('/todos')
}
</script>

<style scoped>
.admin-standalone {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(255, 209, 102, 0.18), transparent 22%),
    radial-gradient(circle at right center, rgba(64, 158, 255, 0.16), transparent 28%),
    linear-gradient(180deg, #f8fbff 0%, #f5f7fa 28%, #f5f7fa 100%);
}

.admin-login-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.ambient {
  position: absolute;
  width: 340px;
  height: 340px;
  border-radius: 50%;
  filter: blur(30px);
  opacity: 0.56;
  pointer-events: none;
}

.ambient-left {
  left: -120px;
  top: 70px;
  background: rgba(255, 214, 126, 0.42);
}

.ambient-right {
  right: -120px;
  bottom: 70px;
  background: rgba(64, 158, 255, 0.24);
}

.login-shell {
  position: relative;
  z-index: 1;
  width: min(1040px, 100%);
  display: grid;
  grid-template-columns: 1.08fr 0.92fr;
  gap: 24px;
}

.hero-panel,
.login-card {
  border-radius: 28px;
  box-shadow: 0 24px 60px rgba(28, 47, 74, 0.1);
}

.hero-panel {
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 540px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.76), rgba(255, 255, 255, 0.9)),
    linear-gradient(135deg, #f8fbff 0%, #fff8ea 100%);
  border: 1px solid rgba(255, 255, 255, 0.7);
}

.hero-badge {
  width: fit-content;
  padding: 8px 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: #7a5f00;
  background: rgba(255, 214, 126, 0.28);
}

.hero-panel h1 {
  margin: 24px 0 14px;
  font-size: clamp(34px, 4vw, 48px);
  line-height: 1.08;
  font-weight: 800;
  color: var(--text-primary);
}

.hero-panel p {
  max-width: 520px;
  font-size: 16px;
  line-height: 1.8;
  color: #546171;
}

.hero-points {
  margin-top: 28px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hero-points span {
  padding: 10px 14px;
  border-radius: 999px;
  font-size: 13px;
  color: #37536f;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(64, 158, 255, 0.12);
}

.ghost-home-btn {
  width: fit-content;
  margin-top: auto;
  color: #37536f;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(64, 158, 255, 0.12);
}

.login-card {
  padding: 26px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(18px);
  border: 1px solid rgba(255, 255, 255, 0.7);
}

.card-head {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 18px;
}

.card-mark {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  color: var(--primary);
  background: linear-gradient(135deg, #edf6ff 0%, #dcecff 100%);
}

.card-head h2 {
  margin: 0;
  font-size: 24px;
  color: var(--text-primary);
}

.card-head p {
  margin-top: 4px;
  font-size: 14px;
  color: var(--text-tertiary);
}

.admin-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 14px;
  border-radius: 16px;
  color: var(--primary);
  font-size: 15px;
  font-weight: 600;
  background: #f4f7fb;
}

.card-body {
  padding-top: 22px;
}

.custom-input :deep(.el-input__wrapper) {
  min-height: 48px;
  border-radius: 14px;
  background: #fbfcfe;
  box-shadow: 0 0 0 1px rgba(64, 158, 255, 0.08) inset;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.16) inset !important;
}

.submit-btn {
  width: 100%;
  height: 48px;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 700;
}

.admin-topbar {
  position: sticky;
  top: 0;
  z-index: 100;
  min-height: 68px;
  padding: 12px 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  backdrop-filter: blur(14px);
  background: rgba(255, 255, 255, 0.84);
  border-bottom: 1px solid rgba(64, 158, 255, 0.08);
}

.topbar-left,
.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
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
}

.title-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.topbar-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.topbar-subtitle {
  font-size: 12px;
  color: var(--text-tertiary);
}

.admin-user {
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 13px;
  color: var(--text-secondary);
  background: rgba(64, 158, 255, 0.08);
}

.admin-content {
  max-width: 1180px;
  margin: 0 auto;
  padding: 28px 20px 40px;
}

@media (max-width: 900px) {
  .login-shell {
    grid-template-columns: 1fr;
  }

  .hero-panel {
    min-height: auto;
  }
}

@media (max-width: 640px) {
  .admin-login-page {
    padding: 16px;
  }

  .hero-panel,
  .login-card {
    border-radius: 22px;
  }

  .hero-panel {
    padding: 28px 22px;
  }

  .login-card {
    padding: 22px 18px;
  }

  .admin-topbar {
    padding: 12px 16px;
    align-items: flex-start;
    flex-direction: column;
  }

  .topbar-subtitle,
  .admin-user {
    display: none;
  }

  .admin-content {
    padding: 22px 16px 32px;
  }
}
</style>
