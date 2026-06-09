import request from '@/api/request'

/**
 * 库存管理 API
 */

/** 库存列表 */
export function getInventoryList(params) {
  return request.get('/inventory', { params })
}

/** 库存预警列表 */
export function getLowStockList() {
  return request.get('/inventory/low-stock')
}

/** 商品库存流水 */
export function getInventoryLogs(productId, params) {
  return request.get(`/inventory/${productId}/logs`, { params })
}

/** 库存调整 */
export function adjustStock(productId, data) {
  return request.put(`/inventory/${productId}/adjust`, data)
}
