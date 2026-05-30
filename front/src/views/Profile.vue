<template>
  <div class="profile-page">
    <div class="profile-topbar">
      <el-button :icon="ArrowLeft" text class="back-btn" @click="router.push('/todos')">返回首页</el-button>
      <el-button text type="primary" @click="openAnnouncement">查看系统公告</el-button>
    </div>

    <section class="profile-hero">
      <div class="hero-main">
        <el-avatar v-if="avatarUrl" :size="78" :src="avatarUrl" class="hero-avatar" />
        <el-avatar v-else :size="78" :icon="UserFilled" class="hero-avatar fallback-avatar" />
        <div class="hero-info">
          <span class="hero-badge">{{ userInfo.role === 'ADMIN' ? '管理员账户' : '个人中心' }}</span>
          <h1>{{ userInfo.nickname || userInfo.username }}</h1>
          <div class="hero-meta">
            <el-tag :type="userInfo.role === 'ADMIN' ? 'danger' : 'primary'" size="small" effect="light" round>
              {{ userInfo.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
            <span class="hero-email" v-if="userInfo.email">
              <el-icon><Message /></el-icon>{{ userInfo.email }}
            </span>
          </div>
        </div>
      </div>
      <div class="hero-side">
        <div class="metric-card">
          <span class="metric-label">邮箱剩余修改次数</span>
          <strong>{{ remainingEmailChanges }}</strong>
          <span class="metric-desc">每日最多修改 3 次</span>
        </div>
      </div>
    </section>

    <div class="profile-grid">
      <section class="profile-card">
        <div class="card-head">
          <div class="card-icon qq"><el-icon><Picture /></el-icon></div>
          <div>
            <h2>QQ 头像设置</h2>
            <p>设置 QQ 号后，导航栏会自动显示对应头像</p>
          </div>
        </div>
        <div class="setting-row">
          <el-input v-model="userInfo.qq" placeholder="请输入 QQ 号" size="large" clearable class="setting-input" />
          <el-avatar
            v-if="userInfo.qq"
            :size="50"
            :src="`https://q1.qlogo.cn/g?b=qq&nk=${userInfo.qq}&s=100`"
            class="qq-preview"
          />
          <el-button type="primary" size="large" class="save-btn" :loading="savingQq" @click="saveQq">
            保存 QQ
          </el-button>
        </div>
      </section>

      <section class="profile-card">
        <div class="card-head">
          <div class="card-icon nickname"><el-icon><Edit /></el-icon></div>
          <div>
            <h2>昵称设置</h2>
            <p>昵称会优先显示在页面顶部和导航栏中</p>
          </div>
        </div>
        <div class="setting-row">
          <el-input
            v-model="userInfo.nickname"
            placeholder="请输入显示昵称"
            size="large"
            clearable
            class="setting-input"
          />
          <el-button type="primary" size="large" class="save-btn" :loading="savingNickname" @click="saveNickname">
            保存昵称
          </el-button>
        </div>
      </section>

      <section class="profile-card">
        <div class="card-head">
          <div class="card-icon email"><el-icon><Message /></el-icon></div>
          <div>
            <h2>邮箱设置</h2>
            <p>
              任务临近截止时会收到邮件提醒
              <span class="email-limit">今日剩余次数：{{ remainingEmailChanges }}</span>
            </p>
          </div>
        </div>
        <div class="setting-row">
          <el-input v-model="userInfo.email" placeholder="请输入邮箱地址" size="large" clearable class="setting-input">
            <template #prefix>
              <el-icon><Message /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" size="large" class="save-btn" :loading="savingEmail" @click="saveEmail">
            保存邮箱
          </el-button>
        </div>
      </section>

      <section class="profile-card">
        <div class="card-head">
          <div class="card-icon password"><el-icon><Lock /></el-icon></div>
          <div>
            <h2>修改密码</h2>
            <p></p>
          </div>
        </div>
        <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-position="top">
          <div class="pwd-row">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                v-model="pwdForm.oldPassword"
                type="password"
                show-password
                size="large"
                placeholder="请输入原密码"
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="pwdForm.newPassword"
                type="password"
                show-password
                size="large"
                placeholder="请输入新密码，至少 6 位"
              />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input
                v-model="pwdForm.confirmPassword"
                type="password"
                show-password
                size="large"
                placeholder="请再次输入新密码"
              />
            </el-form-item>
          </div>
          <el-form-item>
            <el-button type="primary" size="large" class="pwd-btn" :loading="savingPassword" @click="changePwd">
              更新密码
            </el-button>
          </el-form-item>
        </el-form>
      </section>
    </div>

    <el-dialog
      v-model="announcementVisible"
      title="系统公告"
      width="520px"
      class="announcement-dialog"
      :close-on-click-modal="false"
    >
      <div v-if="announcements.length === 0" class="empty-announcement">
        <el-icon :size="48" color="#dcdfe6"><ChatLineSquare /></el-icon>
        <p>暂无公告</p>
      </div>
      <div v-for="notice in announcements" :key="notice.id" class="notice-card">
        <h4>{{ notice.title }}</h4>
        <span class="notice-time">{{ formatDate(notice.createdAt) }}</span>
        <p class="notice-text">{{ notice.content }}</p>
      </div>
      <template #footer>
        <el-button type="primary" size="large" @click="announcementVisible = false">知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, UserFilled } from '@element-plus/icons-vue'
import request from '../api/request'
import { useAuth } from '../composables/useAuth'

const router = useRouter()
const { avatarUrl, clearLogin, role, setLogin, token, username } = useAuth()

const savingQq = ref(false)
const savingNickname = ref(false)
const savingEmail = ref(false)
const savingPassword = ref(false)
const pwdFormRef = ref(null)
const announcementVisible = ref(false)
const announcements = ref([])
const originalQq = ref('')
const originalNickname = ref('')
const originalEmail = ref('')

const userInfo = reactive({
  username: '',
  email: '',
  role: '',
  qq: '',
  nickname: '',
  emailChangeCount: 0,
  emailChangeDate: null,
  lastNoticeSeenAt: null
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const remainingEmailChanges = computed(() => Math.max(0, 3 - (userInfo.emailChangeCount || 0)))

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入的新密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

function normalizeValue(value) {
  return value == null ? '' : String(value).trim()
}

function syncOriginalValues() {
  originalQq.value = normalizeValue(userInfo.qq)
  originalNickname.value = normalizeValue(userInfo.nickname)
  originalEmail.value = normalizeValue(userInfo.email)
}

function updateLoginProfile(qqValue, nicknameValue) {
  setLogin({
    token: token.value,
    username: username.value,
    role: role.value,
    qq: qqValue,
    nickname: nicknameValue
  })
}

function showUnchangedMessage(label) {
  ElMessage.info(`${label}未变化`)
}

function showUpdatedMessage(label) {
  ElMessage.success(`${label}更新成功`)
}

function formatDate(value) {
  if (!value) {
    return ''
  }

  const date = new Date(value)
  const pad = num => String(num).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

async function fetchUserInfo() {
  const res = await request.get('/user/info')
  if (res.code !== 200) {
    return
  }

  Object.assign(userInfo, res.data)
  syncOriginalValues()

  const lastSeen = res.data.lastNoticeSeenAt
  const today = new Date().toDateString()
  const lastSeenDay = lastSeen ? new Date(lastSeen).toDateString() : null

  if (lastSeenDay !== today) {
    fetchAnnouncements(true)
    request.put('/user/notice-seen').catch(() => {})
  }
}

async function fetchAnnouncements(autoShow = false) {
  const res = await request.get('/notice/latest')
  if (res.code === 200) {
    announcements.value = res.data || []
    if (autoShow) {
      announcementVisible.value = true
    }
  }
}

function openAnnouncement() {
  fetchAnnouncements(true)
}

async function saveQq() {
  const normalizedQq = normalizeValue(userInfo.qq)
  if (normalizedQq === originalQq.value) {
    showUnchangedMessage('QQ')
    return
  }

  savingQq.value = true
  try {
    const res = await request.put('/user/profile', {
      qq: normalizedQq || null
    })

    if (res.code === 200) {
      userInfo.qq = res.data.qq || ''
      originalQq.value = normalizeValue(userInfo.qq)
      updateLoginProfile(res.data.qq || '', userInfo.nickname)
      showUpdatedMessage('QQ')
    }
  } finally {
    savingQq.value = false
  }
}

async function saveNickname() {
  const normalizedNickname = normalizeValue(userInfo.nickname)
  if (!normalizedNickname) {
    ElMessage.warning('昵称不能为空')
    return
  }
  if (normalizedNickname === originalNickname.value) {
    showUnchangedMessage('昵称')
    return
  }

  savingNickname.value = true
  try {
    const res = await request.put('/user/profile', {
      nickname: normalizedNickname
    })

    if (res.code === 200) {
      userInfo.nickname = res.data.nickname || normalizedNickname
      originalNickname.value = normalizeValue(userInfo.nickname)
      updateLoginProfile(userInfo.qq, userInfo.nickname)
      showUpdatedMessage('昵称')
    }
  } finally {
    savingNickname.value = false
  }
}

async function saveEmail() {
  const normalizedEmail = normalizeValue(userInfo.email)
  if (!normalizedEmail) {
    ElMessage.warning('请输入邮箱地址')
    return
  }

  const emailRegex = /^[\w.+-]+@[\w-]+\.[\w.]+$/
  if (!emailRegex.test(normalizedEmail)) {
    ElMessage.warning('邮箱格式不正确')
    return
  }

  if (normalizedEmail === originalEmail.value) {
    ElMessage.info('邮箱未变化，不会扣除修改次数')
    return
  }

  savingEmail.value = true
  try {
    const res = await request.put('/user/email', { email: normalizedEmail })
    if (res.code === 200) {
      userInfo.email = normalizedEmail
      originalEmail.value = normalizedEmail
      showUpdatedMessage('邮箱')
      fetchUserInfo()
    }
  } finally {
    savingEmail.value = false
  }
}

function resetPasswordForm() {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
}

function changePwd() {
  pwdFormRef.value?.validate(async valid => {
    if (!valid) {
      return
    }

    savingPassword.value = true
    try {
      const res = await request.put('/user/password', {
        oldPassword: pwdForm.oldPassword,
        newPassword: pwdForm.newPassword
      })

      if (res.code === 200) {
        ElMessage.success('密码更新成功，请重新登录')
        resetPasswordForm()
        clearLogin()
        router.push('/login')
      }
    } finally {
      savingPassword.value = false
    }
  })
}

onMounted(fetchUserInfo)
</script>

<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.profile-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.back-btn {
  font-weight: 600;
}

.profile-hero,
.profile-card {
  border: 1px solid rgba(64, 158, 255, 0.08);
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 18px 36px rgba(27, 39, 53, 0.06);
}

.profile-hero {
  display: flex;
  align-items: stretch;
  justify-content: space-between;
  gap: 20px;
  padding: 28px 30px;
  border-radius: 28px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(255, 255, 255, 0.76)),
    linear-gradient(120deg, rgba(255, 214, 126, 0.12), rgba(64, 158, 255, 0.12));
}

.hero-main {
  display: flex;
  align-items: center;
  gap: 18px;
}

.hero-avatar {
  flex-shrink: 0;
  box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.1);
}

.fallback-avatar {
  background: #f3f7fb;
  color: var(--primary);
}

.hero-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.hero-badge {
  display: inline-flex;
  width: fit-content;
  padding: 7px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  color: #705400;
  background: rgba(255, 214, 126, 0.24);
}

.hero-info h1 {
  margin: 0;
  font-size: clamp(28px, 4vw, 34px);
  line-height: 1.12;
  color: var(--text-primary);
}

.hero-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.hero-email {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: var(--text-tertiary);
}

.hero-side {
  display: flex;
  align-items: center;
}

.metric-card {
  min-width: 180px;
  padding: 18px 20px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.74);
  border: 1px solid rgba(64, 158, 255, 0.08);
}

.metric-label,
.metric-desc {
  display: block;
}

.metric-label {
  font-size: 13px;
  color: var(--text-tertiary);
}

.metric-card strong {
  display: block;
  margin: 10px 0 8px;
  font-size: 34px;
  color: var(--text-primary);
}

.metric-desc {
  font-size: 12px;
  color: var(--text-disabled);
}

.profile-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.profile-card {
  padding: 26px;
  border-radius: 24px;
}

.card-head {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  margin-bottom: 18px;
}

.card-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  font-size: 20px;
  flex-shrink: 0;
}

.card-icon.qq {
  color: var(--primary);
  background: rgba(64, 158, 255, 0.12);
}

.card-icon.nickname {
  color: #d28a11;
  background: rgba(230, 162, 60, 0.14);
}

.card-icon.email {
  color: #5ba831;
  background: rgba(103, 194, 58, 0.14);
}

.card-icon.password {
  color: #ef5e5e;
  background: rgba(245, 108, 108, 0.14);
}

.card-head h2 {
  margin: 0 0 5px;
  font-size: 18px;
  color: var(--text-primary);
}

.card-head p {
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-tertiary);
}

.email-limit {
  color: var(--primary);
  font-weight: 600;
}

.setting-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.setting-input {
  flex: 1;
}

.setting-input :deep(.el-input__wrapper),
.pwd-row :deep(.el-input__wrapper) {
  min-height: 46px;
  border-radius: 14px;
  background: #fbfcfe;
  box-shadow: 0 0 0 1px rgba(64, 158, 255, 0.08) inset;
}

.setting-input :deep(.el-input__wrapper.is-focus),
.pwd-row :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.16) inset !important;
}

.qq-preview {
  flex-shrink: 0;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.08);
}

.save-btn,
.pwd-btn {
  height: 46px;
  padding: 0 24px;
  border-radius: 14px;
  font-weight: 700;
}

.pwd-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.pwd-row :deep(.el-form-item__label) {
  color: var(--text-secondary);
  font-weight: 600;
}

.empty-announcement {
  text-align: center;
  color: var(--text-tertiary);
  padding: 40px 0;
}

.notice-card {
  margin-bottom: 12px;
  padding: 16px;
  border-radius: 12px;
  background: var(--bg-light);
}

.notice-card h4 {
  margin: 0 0 4px;
  color: var(--text-primary);
  font-size: 15px;
}

.notice-time {
  font-size: 12px;
  color: var(--text-tertiary);
}

.notice-text {
  margin: 8px 0 0;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
}

@media (max-width: 768px) {
  .profile-hero {
    flex-direction: column;
    padding: 24px;
  }

  .hero-main {
    flex-direction: column;
    align-items: flex-start;
  }

  .metric-card {
    width: 100%;
  }

  .setting-row {
    flex-direction: column;
  }

  .save-btn,
  .setting-input {
    width: 100%;
  }

  .pwd-row {
    grid-template-columns: 1fr;
  }
}
</style>
