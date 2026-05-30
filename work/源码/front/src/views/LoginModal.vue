<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="emit('update:visible', $event)"
    title="登录 Todo 待办"
    width="420px"
    :close-on-click-modal="false"
    class="login-modal"
    center
  >
    <div class="modal-tabs">
      <span class="modal-tab" :class="{ active: activeTab === 'login' }" @click="activeTab = 'login'">登录</span>
      <span class="modal-tab" :class="{ active: activeTab === 'register' }" @click="activeTab = 'register'">注册</span>
    </div>

    <div v-show="activeTab === 'login'" class="modal-body">
      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" @submit.prevent>
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="submit-btn" @click="handleLogin" :loading="loading">
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div v-show="activeTab === 'register'" class="modal-body">
      <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" @submit.prevent>
        <el-form-item prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码，至少 6 位"
            size="large"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleRegister"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="submit-btn" @click="handleRegister" :loading="loading">
            注册
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </el-dialog>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import request from '../api/request'
import { useAuth } from '../composables/useAuth'

defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'login-success'])
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

function closeDialog() {
  emit('update:visible', false)
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
        emit('login-success', res.data)
        closeDialog()
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
        emit('login-success', res.data)
        closeDialog()
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
.modal-tabs {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
  background: var(--bg);
  border-radius: 10px;
  padding: 4px;
}

.modal-tab {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  color: var(--text-tertiary);
  cursor: pointer;
  transition: all 0.25s;
  user-select: none;
}

.modal-tab.active {
  background: var(--card-bg);
  color: var(--primary);
  font-weight: 600;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.modal-body {
  padding: 0 4px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
}
</style>
