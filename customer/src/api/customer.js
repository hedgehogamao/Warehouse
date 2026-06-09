import request from './request'

/** 获取客户个人信息 */
export function getProfile() {
  return request.get('/miniapp/customer/profile')
}

/** 更新客户个人信息 */
export function updateProfile(data) {
  return request.put('/miniapp/customer/profile', data)
}
