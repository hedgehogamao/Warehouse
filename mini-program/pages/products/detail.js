const api = require('../../utils/api')
const i18n = require('../../utils/i18n')
const app = getApp()

Page({
  data: { product: null, loading: true, t: {} },

  onLoad(options) {
    this.setData({ t: this.getTranslations() })
    if (options.id) this.loadProduct(options.id)
  },

  getTranslations() {
    return {
      brand: i18n.t('common.brand'), sku: i18n.t('common.sku'),
      category: i18n.t('common.category'), carModel: i18n.t('common.carModel'),
      description: i18n.t('common.description'), goCart: i18n.t('detail.goCart'),
      addCart: i18n.t('detail.addCart'), outOfStock: i18n.t('detail.outOfStock'),
      stock: i18n.t('common.stock'), lowStock: i18n.t('common.lowStock'),
      outOfStockStatus: i18n.t('common.outOfStock'), stockInfo: i18n.t('detail.stockInfo'),
      loading: i18n.t('common.loading')
    }
  },

  async loadProduct(id) {
    try {
      const res = await api.request(`/miniapp/products/${id}`)
      const p = res.data
      p.imageUrl = app.getImageUrl(p.imageUrl)
      if (p.carModel) p.carModelList = p.carModel.split('/')
      this.setData({ product: p, loading: false })
      wx.setNavigationBarTitle({ title: p.name })
    } catch (e) { this.setData({ loading: false }); wx.showToast({ title: 'Not found', icon: 'none' }) }
  },

  onAddToCart() { if (this.data.product) app.addToCart(this.data.product) },
  onGoCart() { wx.switchTab({ url: '/pages/cart/cart' }) }
})
