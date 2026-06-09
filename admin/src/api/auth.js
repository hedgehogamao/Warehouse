import request from '@/api/request'

/**
 * 用户登录
 * @param {Object} data - { username, password }
 */
export function login(data) {
  return request.post('/auth/login', data)
}

/**
 * 获取当前登录用户信息
 */
export function getMe() {
  return request.get('/auth/me')
}

/**
 * 修改密码（当前用户）
 * @param {Object} data - { oldPassword, newPassword }
 */
export function changePassword(data) {
  return request.put('/auth/password', data)
}

/**
 * 退出登录
 */
export function logout() {
  return request.post('/auth/logout')
}
