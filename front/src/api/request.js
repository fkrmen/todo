import axios from 'axios'
import { ElMessage } from 'element-plus'

function clearStoredAuth() {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('role')
  localStorage.removeItem('qq')
  localStorage.removeItem('nickname')
}

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response) {
      const { status, data } = error.response

      if (status === 401) {
        ElMessage.error(data?.msg || '登录已失效，请重新登录')
        clearStoredAuth()
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
      } else if (status === 403) {
        ElMessage.error(data?.msg || '没有权限执行该操作')
      } else {
        ElMessage.error(data?.msg || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查后端服务是否可用')
    }

    return Promise.reject(error)
  }
)

export default request
