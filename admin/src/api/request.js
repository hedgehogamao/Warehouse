import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import i18n from '@/i18n'

const t = i18n.global.t

// 创建 Axios 实例
const request = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器：自动携带 Token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理错误
request.interceptors.response.use(
  (response) => {
    const res = response.data

    // 业务错误
    if (res.code !== 200) {
      ElMessage.error(res.message || t('message.requestFailed'))
      return Promise.reject(new Error(res.message || t('message.requestFailed')))
    }

    return res
  },
  (error) => {
    if (error.response) {
      const { status } = error.response

      if (status === 401) {
        // Token 过期 → 清除登录态，跳转登录页
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
        ElMessage.error(t('login.expired'))
      } else if (status === 403) {
        ElMessage.error(t('message.noPermission'))
      } else {
        ElMessage.error(error.response.data?.message || t('message.serverError'))
      }
    } else {
      ElMessage.error(t('message.networkError'))
    }

    return Promise.reject(error)
  }
)

export default request
