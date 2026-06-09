import axios from 'axios'
import { ElMessage } from 'element-plus'
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

// 响应拦截器：统一处理错误
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || t('message.requestFailed'))
      return Promise.reject(new Error(res.message || t('message.requestFailed')))
    }
    return res
  },
  (error) => {
    if (error.response) {
      ElMessage.error(error.response.data?.message || t('message.serverError'))
    } else {
      ElMessage.error(t('message.networkError'))
    }
    return Promise.reject(error)
  }
)

export default request
