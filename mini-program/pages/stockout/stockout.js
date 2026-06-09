const api = require('../../utils/api')
const i18n = require('../../utils/i18n')
const app = getApp()

Page({
  data: {
    product: null,
    quantity: 1,
    remark: '',
    submitting: false,
    t: {}
  },

  onShow() {
    this.setData({ t: this.getTranslations() })
  },

  getTranslations() {
    return {
      scanBtn: i18n.t('stockout.scanBtn'),
      scanTip: i18n.t('stockout.scanTip'),
      manualInput: i18n.t('stockout.manualInput'),
      skuPlaceholder: i18n.t('stockout.skuPlaceholder'),
      search: i18n.t('common.search'),
      currentStock: i18n.t('stockout.currentStock'),
      outQty: i18n.t('stockout.outQty'),
      remark: i18n.t('stockout.remark'),
      remarkPlaceholder: i18n.t('stockout.remarkPlaceholder'),
      confirmOut: i18n.t('stockout.confirmOut'),
      noProduct: i18n.t('stockout.noProduct'),
      outSuccess: i18n.t('stockout.outSuccess'),
      stockNotEnough: i18n.t('stockout.stockNotEnough'),
      brand: i18n.t('common.brand'),
      sku: i18n.t('common.sku'),
      category: i18n.t('common.category')
    }
  },

  /** 扫码 */
  onScan() {
    wx.scanCode({
      scanType: ['barCode', 'qrCode'],
      success: (res) => {
        const sku = res.result
        this.lookupSku(sku)
      },
      fail: () => {
        wx.showToast({ title: '扫码取消', icon: 'none' })
      }
    })
  },

  /** 手动输入 SKU 搜索 */
  skuInput: '',
  onSkuInput(e) { this.skuInput = e.detail.value },
  onSkuSearch() {
    if (this.skuInput) this.lookupSku(this.skuInput)
  },

  /** 按 SKU 查找商品 */
  async lookupSku(sku) {
    try {
      const res = await api.request(`/products/by-sku/${encodeURIComponent(sku)}`)
      if (res.code === 200) {
        const p = res.data
        p.imageUrl = app.getImageUrl(p.imageUrl)
        this.setData({ product: p, quantity: 1 })
      } else {
        wx.showToast({ title: this.data.t.noProduct, icon: 'none' })
      }
    } catch (e) {
      wx.showToast({ title: this.data.t.noProduct, icon: 'none' })
    }
  },

  /** 数量变更 */
  onQuantityChange(e) {
    this.setData({ quantity: parseInt(e.detail.value) || 1 })
  },
  onQtyMinus() {
    if (this.data.quantity > 1) this.setData({ quantity: this.data.quantity - 1 })
  },
  onQtyPlus() {
    const max = this.data.product ? this.data.product.stock : 999
    if (this.data.quantity < max) this.setData({ quantity: this.data.quantity + 1 })
  },

  /** 备注 */
  onRemarkInput(e) { this.setData({ remark: e.detail.value }) },

  /** 确认出库 */
  async onConfirm() {
    const { product, quantity, remark } = this.data
    if (!product) { wx.showToast({ title: this.data.t.noProduct, icon: 'none' }); return }
    if (quantity <= 0) { wx.showToast({ title: '数量必须大于0', icon: 'none' }); return }
    if (quantity > product.stock) { wx.showToast({ title: this.data.t.stockNotEnough, icon: 'none' }); return }

    this.setData({ submitting: true })
    try {
      const res = await api.request('/stock-out', 'POST', {
        productId: product.id,
        quantity: quantity,
        remark: remark || undefined
      })
      if (res.code === 200) {
        const d = res.data
        wx.showModal({
          title: this.data.t.outSuccess,
          content: `${d.productName}\n${this.data.t.outQty}: ${d.quantity}\n${this.data.t.currentStock}: ${d.beforeStock} → ${d.afterStock}`,
          showCancel: false
        })
        this.setData({ product: null, quantity: 1, remark: '' })
      }
    } catch (e) {
      // 拦截器处理
    } finally {
      this.setData({ submitting: false })
    }
  }
})
