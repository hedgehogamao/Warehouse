import request from '@/api/request'

/**
 * 商品管理 API
 */

/** 商品列表（分页+搜索+筛选） */
export function getProductList(params) {
  return request.get('/products', { params })
}

/** 商品详情 */
export function getProductById(id) {
  return request.get(`/products/${id}`)
}

/** 新增商品 */
export function createProduct(data) {
  return request.post('/products', data)
}

/** 修改商品 */
export function updateProduct(id, data) {
  return request.put(`/products/${id}`, data)
}

/** 删除商品 */
export function deleteProduct(id) {
  return request.delete(`/products/${id}`)
}

/** 上架/下架商品 */
export function updateProductStatus(id, status) {
  return request.put(`/products/${id}/status`, { status })
}

/** 获取商品分类列表 */
export function getCategories() {
  return request.get('/products/categories')
}

/** 根据条形码/SKU查找商品 */
export function lookupBySku(sku) {
  return request.get(`/products/by-sku/${encodeURIComponent(sku)}`)
}

/** Excel 批量导入商品 */
export function importProducts(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/products/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 300000 // 5分钟超时，支持大文件上传
  })
}

/** 下载导入模板 */
export function downloadImportTemplate() {
  return request.get('/products/import-template', { responseType: 'blob' })
}
