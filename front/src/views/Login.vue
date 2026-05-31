<template>
  <div class="login-page">
    <div class="ambient ambient-left"></div>
    <div class="ambient ambient-right"></div>

    <div class="login-shell">
      <section class="hero-panel">
        <div class="hero-badge">By Dormitory-111</div>
        <h1>TODO待办管理</h1>
        <!-- <p>
          首页可以直接浏览，登录后再创建任务、接收提醒和管理个人资料。整个流程现在只保留一种登录方式，入口更统一。
        </p> -->
        <!-- <div class="hero-points">
          <span>首页可直接浏览</span>
          <span>支持邮箱提醒</span>
          <span>个人资料独立维护</span>
        </div> -->
        <el-button class="ghost-home-btn" round @click="router.push('/todos')">先去首页看看</el-button>
      </section>

      <section class="login-card">
        <div class="card-head">
          <div class="card-mark">
            <el-icon :size="20"><List /></el-icon>
          </div>
          <div>
            <h2>登录 Todo 待办</h2>
            
          </div>
        </div>

        <div class="card-tabs">
          <button type="button" class="tab" :class="{ active: activeTab === 'login' }" @click="activeTab = 'login'">
            登录
          </button>
          <button
            type="button"
            class="tab"
            :class="{ active: activeTab === 'register' }"
            @click="activeTab = 'register'"
          >
            注册
          </button>
        </div>

        <div class="card-body">
          <el-form v-show="activeTab === 'login'" ref="loginFormRef" :model="loginForm" :rules="loginRules">
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                size="large"
                :prefix-icon="User"
                class="custom-input"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                :prefix-icon="Lock"
                show-password
                class="custom-input"
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" class="submit-btn" @click="handleLogin" :loading="loading">
                立即登录
              </el-button>
            </el-form-item>
          </el-form>

          <el-form
            v-show="activeTab === 'register'"
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
          >
            <el-form-item prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名"
                size="large"
                :prefix-icon="User"
                class="custom-input"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码，至少 6 位"
                size="large"
                :prefix-icon="Lock"
                show-password
                class="custom-input"
                @keyup.enter="handleRegister"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" class="submit-btn" @click="handleRegister" :loading="loading">
                创建账户
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import request from '../api/request'
import { useAuth } from '../composables/useAuth'

const router = useRouter()
const { setLogin } = useAuth()

const activeTab = ref('login')
const loading = ref(false)
const loginFormRef = ref(null)
const registerFormRef = ref(null)

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ]
}

function redirectAfterLogin(userRole) {
  router.push(userRole === 'ADMIN' ? '/admin' : '/todos')
}

function handleLogin() {
  loginFormRef.value?.validate(async valid => {
    if (!valid) {
      return
    }

    loading.value = true
    try {
      const res = await request.post('/user/login', loginForm)
      if (res.code === 200) {
        setLogin(res.data)
        ElMessage.success('登录成功')
        redirectAfterLogin(res.data.role)
      } else {
        ElMessage.error(res.msg || '登录失败')
      }
    } finally {
      loading.value = false
    }
  })
}

function handleRegister() {
  registerFormRef.value?.validate(async valid => {
    if (!valid) {
      return
    }

    loading.value = true
    try {
      const res = await request.post('/user/register', registerForm)
      if (res.code === 200) {
        setLogin(res.data)
        ElMessage.success('注册成功')
        redirectAfterLogin(res.data.role)
      } else {
        ElMessage.error(res.msg || '注册失败')
      }
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  padding: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(circle at top left, rgba(255, 209, 102, 0.22), transparent 24%),
    radial-gradient(circle at right center, rgba(64, 158, 255, 0.18), transparent 30%),
    linear-gradient(135deg, #fefbf3 0%, #f8fbff 46%, #eef5ff 100%);
}

.ambient {
  position: absolute;
  width: 360px;
  height: 360px;
  border-radius: 50%;
  filter: blur(28px);
  opacity: 0.55;
  pointer-events: none;
}

.ambient-left {
  left: -120px;
  top: 80px;
  background: rgba(255, 214, 126, 0.45);
}

.ambient-right {
  right: -120px;
  bottom: 60px;
  background: rgba(64, 158, 255, 0.28);
}

.login-shell {
  position: relative;
  z-index: 1;
  width: min(1040px, 100%);
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 24px;
  align-items: stretch;
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
  min-height: 560px;
  color: #1f2a37;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.74), rgba(255, 255, 255, 0.9)),
    linear-gradient(135deg, #f8fbff 0%, #fff8ea 100%);
  border: 1px solid rgba(255, 255, 255, 0.6);
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
  font-size: clamp(34px, 4vw, 52px);
  line-height: 1.08;
  font-weight: 800;
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
  margin-bottom: 24px;
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

.card-tabs {
  margin-bottom: 22px;
  padding: 5px;
  display: flex;
  gap: 6px;
  border-radius: 16px;
  background: #f4f7fb;
}

.tab {
  flex: 1;
  height: 44px;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-tertiary);
  background: transparent;
  cursor: pointer;
  transition: all 0.24s ease;
}

.tab.active {
  color: var(--primary);
  background: #fff;
  box-shadow: 0 8px 18px rgba(64, 158, 255, 0.12);
}

.card-body {
  padding: 8px 2px 2px;
}

.custom-input :deep(.el-input__wrapper) {
  min-height: 48px;
  border-radius: 14px;
  background: #fbfcfe;
  box-shadow: 0 0 0 1px rgba(64, 158, 255, 0.08) inset;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.18) inset !important;
}

.submit-btn {
  width: 100%;
  height: 48px;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 700;
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
  .login-page {
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

  .hero-panel h1 {
    margin-top: 18px;
    font-size: 32px;
  }
}
</style>
