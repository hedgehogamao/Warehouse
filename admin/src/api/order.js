import request from '@/api/request'

/**
 * 订单管理 API
 */

/** 创建订单（销售下单） */
export function createOrder(data) {
  return request.post('/orders', data)
}

/** 订单列表 */
export function getOrderList(params) {
  return request.get('/orders', { params })
}

/** 订单详情 */
export function getOrderById(id) {
  return request.get(`/orders/${id}`)
}

/** 订单退款 */
export function refundOrder(id, remark) {
  return request.put(`/orders/${id}/refund`, { remark })
}

/** POS 快速搜索商品 */
export function posSearchProducts(keyword) {
  return request.get('/products/pos-list', { params: { keyword } })
}
