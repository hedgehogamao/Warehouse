const api = require('../../utils/api')
const i18n = require('../../utils/i18n')
const app = getApp()

Page({
  data: {
    products: [], page: 1, total: 0, keyword: '', category: '',
    loading: false, noMore: false, t: {}
  },

  onLoad(options) {
    if (options.keyword) this.setData({ keyword: options.keyword })
    if (options.category) this.setData({ category: options.category })
    this.loadProducts()
  },

  onShow() { this.setData({ t: this.getTranslations() }) },

  getTranslations() {
    return {
      searchPlaceholder: i18n.t('products.searchPlaceholder'),
      loading: i18n.t('common.loading'),
      noMore: i18n.t('common.noMore'),
      noData: i18n.t('common.noData'),
      addCartBtn: i18n.t('common.addCartBtn'),
      stock: i18n.t('common.stock'),
      lowStock: i18n.t('common.lowStock'),
      outOfStock: i18n.t('common.outOfStock'),
      inStock: i18n.t('common.inStock')
    }
  },

  async loadProducts() {
    if (this.data.loading || this.data.noMore) return
    this.setData({ loading: true })
    try {
      const params = { page: this.data.page, size: 20 }
      if (this.data.keyword) params.keyword = this.data.keyword
      if (this.data.category) params.category = this.data.category
      const query = Object.keys(params).map(k => `${k}=${encodeURIComponent(params[k])}`).join('&')
      const res = await api.request(`/miniapp/products?${query}`)
      const fixed = res.data.records.map(p => ({ ...p, imageUrl: app.getImageUrl(p.imageUrl) }))
      const newProducts = this.data.page === 1 ? fixed : [...this.data.products, ...fixed]
      this.setData({ products: newProducts, total: res.data.total, noMore: newProducts.length >= res.data.total, loading: false })
    } catch (e) { this.setData({ loading: false }) }
  },

  onSearchInput(e) { this.setData({ keyword: e.detail.value }) },
  onSearch() { this.setData({ page: 1, noMore: false, products: [] }); this.loadProducts() },
  onReachBottom() { if (!this.data.noMore) { this.setData({ page: this.data.page + 1 }); this.loadProducts() } },
  onPullDownRefresh() { this.setData({ page: 1, noMore: false, products: [] }); this.loadProducts().then(() => wx.stopPullDownRefresh()) },
  onProductTap(e) { wx.navigateTo({ url: `/pages/products/detail?id=${e.currentTarget.dataset.id}` }) },
  onAddToCart(e) { app.addToCart(e.currentTarget.dataset.product) }
})
