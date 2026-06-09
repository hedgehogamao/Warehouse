const api = require('../../utils/api')
const i18n = require('../../utils/i18n')
const app = getApp()

Page({
  data: {
    categories: [],
    hotProducts: [],
    searchKeyword: '',
    cartCount: 0,
    t: {}
  },

  onLoad() {
    this.loadCategories()
    this.loadHotProducts()
  },

  onShow() {
    this.setData({ cartCount: app.getCartCount(), t: this.getTranslations() })
  },

  getTranslations() {
    return {
      searchPlaceholder: i18n.t('index.searchPlaceholder'),
      categories: i18n.t('index.categories'),
      hotProducts: i18n.t('index.hotProducts'),
      addCartBtn: i18n.t('common.addCartBtn'),
      stock: i18n.t('common.stock'),
      lowStock: i18n.t('common.lowStock'),
      outOfStock: i18n.t('common.outOfStock')
    }
  },

  async loadCategories() {
    try {
      const res = await api.request('/miniapp/products/categories')
      this.setData({ categories: res.data || [] })
    } catch (e) {}
  },

  async loadHotProducts() {
    try {
      const res = await api.request('/miniapp/products?page=1&size=6')
      const products = (res.data.records || []).map(p => ({ ...p, imageUrl: app.getImageUrl(p.imageUrl) }))
      this.setData({ hotProducts: products })
    } catch (e) {}
  },

  onSearchInput(e) { this.setData({ searchKeyword: e.detail.value }) },
  onSearch() {
    if (this.data.searchKeyword) {
      wx.navigateTo({ url: `/pages/products/products?keyword=${this.data.searchKeyword}` })
    }
  },
  onCategoryTap(e) {
    wx.navigateTo({ url: `/pages/products/products?category=${e.currentTarget.dataset.category}` })
  },
  onProductTap(e) {
    wx.navigateTo({ url: `/pages/products/detail?id=${e.currentTarget.dataset.id}` })
  },
  onAddToCart(e) {
    app.addToCart(e.currentTarget.dataset.product)
    this.setData({ cartCount: app.getCartCount() })
  }
})
