import { computed, ref } from 'vue'

const token = ref(localStorage.getItem('token') || '')
const username = ref(localStorage.getItem('username') || '')
const role = ref(localStorage.getItem('role') || '')
const qq = ref(localStorage.getItem('qq') || '')
const nickname = ref(localStorage.getItem('nickname') || '')

const isLoggedIn = computed(() => Boolean(token.value))
const isAdmin = computed(() => role.value === 'ADMIN')
const roleLabel = computed(() => (role.value === 'ADMIN' ? '管理员' : '普通用户'))
const displayName = computed(() => nickname.value || username.value)
const avatarUrl = computed(() => {
  if (!qq.value) {
    return ''
  }
  return `https://q1.qlogo.cn/g?b=qq&nk=${qq.value}&s=100`
})

function setLogin(data) {
  token.value = data.token ?? token.value
  username.value = data.username ?? username.value
  role.value = data.role ?? role.value
  qq.value = data.qq ?? qq.value
  nickname.value = data.nickname ?? nickname.value

  localStorage.setItem('token', token.value)
  localStorage.setItem('username', username.value)
  localStorage.setItem('role', role.value)
  localStorage.setItem('qq', qq.value)
  localStorage.setItem('nickname', nickname.value)
}

function clearLogin() {
  token.value = ''
  username.value = ''
  role.value = ''
  qq.value = ''
  nickname.value = ''

  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('role')
  localStorage.removeItem('qq')
  localStorage.removeItem('nickname')
}

export function useAuth() {
  return {
    token,
    username,
    role,
    qq,
    nickname,
    isLoggedIn,
    isAdmin,
    roleLabel,
    displayName,
    avatarUrl,
    setLogin,
    clearLogin
  }
}
