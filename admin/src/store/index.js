import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi } from '@/api/auth'

/**
 * 用户状态管理
 */
export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  /** 是否已登录 */
  const isLoggedIn = computed(() => !!token.value)
  /** 用户名 */
  const username = computed(() => userInfo.value?.username || '')
  /** 真实姓名 */
  const realName = computed(() => userInfo.value?.realName || '')
  /** 角色 */
  const role = computed(() => userInfo.value?.role || '')

  /**
   * 登录
   * @param {Object} loginForm - { username, password }
   */
  async function login(loginForm) {
    const res = await loginApi(loginForm)
    const data = res.data

    token.value = data.token
    userInfo.value = {
      userId: data.userId,
      username: data.username,
      realName: data.realName,
      role: data.role
    }

    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))

    return res
  }

  /**
   * 登出
   */
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    username,
    realName,
    role,
    login,
    logout
  }
})
