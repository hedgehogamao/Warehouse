import request from './request'

/** 客户注册 */
export function register(data) {
  return request.post('/customer/auth/register', data)
}

/** 客户登录 */
export function login(data) {
  return request.post('/customer/auth/login', data)
}

/** 获取当前客户信息 */
export function getMe() {
  return request.get('/customer/auth/me')
}
