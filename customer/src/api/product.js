import request from './request'

/** 获取商品列表 */
export function getProducts(params) {
  return request.get('/miniapp/products', { params })
}

/** 获取商品详情 */
export function getProductById(id) {
  return request.get(`/miniapp/products/${id}`)
}

/** 获取商品分类 */
export function getCategories() {
  return request.get('/miniapp/products/categories')
}
