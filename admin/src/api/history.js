import request from '@/api/request'

/**
 * 历史记录 API
 */

/** 库存流水查询 */
export function getInventoryLogs(params) {
  return request.get('/inventory-logs', { params })
}

/** 操作日志查询 */
export function getOperationLogs(params) {
  return request.get('/operation-logs', { params })
}
