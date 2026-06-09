import request from '@/api/request'

/**
 * 入库管理 API
 */

/** 新建入库 */
export function createStockIn(data) {
  return request.post('/stock-in', data)
}

/** 入库记录列表 */
export function getStockInList(params) {
  return request.get('/stock-in', { params })
}

/** 入库单详情 */
export function getStockInById(id) {
  return request.get(`/stock-in/${id}`)
}
