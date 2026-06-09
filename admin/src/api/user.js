import request from '@/api/request'

/**
 * 用户管理 API
 */
export function getUserList(params) {
  return request.get('/users', { params })
}

export function getUserById(id) {
  return request.get(`/users/${id}`)
}

export function createUser(data) {
  return request.post('/users', data)
}

export function updateUser(id, data) {
  return request.put(`/users/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/users/${id}`)
}

export function changePassword(id, data) {
  return request.put(`/users/${id}/password`, data)
}

/**
 * 启用/禁用用户
 */
export function updateUserStatus(id, status) {
  return request.put(`/users/${id}/status`, { status })
}
