import request from '@/api/request'

/**
 * 数据统计 API
 */

/** 今日概况 */
export function getTodaySummary() {
  return request.get('/statistics/today')
}

/** 销售趋势 */
export function getSalesTrend(days = 7) {
  return request.get('/statistics/sales-trend', { params: { days } })
}

/** 商品销售排行 */
export function getTopProducts(days = 30, limit = 10) {
  return request.get('/statistics/top-products', { params: { days, limit } })
}

/** 分类占比 */
export function getCategoryDistribution(days = 30) {
  return request.get('/statistics/category-distribution', { params: { days } })
}
