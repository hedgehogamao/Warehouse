import request from './request'

/** 获取订单列表 */
export function getOrders(params) {
  return request.get('/miniapp/orders', { params })
}
