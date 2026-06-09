const i18n = require('../../utils/i18n')
const app = getApp()

Page({
  data: { cart: [], total: 0, totalCount: 0, t: {} },

  onShow() {
    this.setData({ t: this.getTranslations() })
    this.refreshCart()
  },

  getTranslations() {
    return {
      itemCount: i18n.t('cart.itemCount'), clear: i18n.t('cart.clear'),
      subtotal: i18n.t('cart.subtotal'), total: i18n.t('common.total'),
      tip1: i18n.t('cart.tip1'), tip2: i18n.t('cart.tip2'),
      contactStore: i18n.t('cart.contactStore'),
      emptyIcon: i18n.t('cart.emptyIcon'), emptyText: i18n.t('cart.emptyText'),
      goShop: i18n.t('cart.goShop'),
      confirmRemove: i18n.t('cart.confirmRemove'),
      confirmClear: i18n.t('cart.confirmClear'),
      maxStock: i18n.t('cart.maxStock'),
      prompt: i18n.t('common.confirm')
    }
  },

  refreshCart() {
    this.setData({ cart: app.globalData.cart, total: app.getCartTotal(), totalCount: app.getCartCount() })
  },

  onQuantityChange(e) {
    const index = e.currentTarget.dataset.index
    const action = e.currentTarget.dataset.action
    const item = this.data.cart[index]
    if (!item) return
    if (action === 'minus') { app.updateCartQuantity(index, item.quantity - 1) }
    else if (action === 'plus') {
      if (item.quantity < item.stock) { app.updateCartQuantity(index, item.quantity + 1) }
      else { wx.showToast({ title: this.data.t.maxStock, icon: 'none' }) }
    }
    this.refreshCart()
  },

  onDelete(e) {
    wx.showModal({
      title: this.data.t.prompt, content: this.data.t.confirmRemove,
      success: (res) => { if (res.confirm) { app.removeFromCart(e.currentTarget.dataset.index); this.refreshCart() } }
    })
  },

  onClear() {
    wx.showModal({
      title: this.data.t.prompt, content: this.data.t.confirmClear,
      success: (res) => { if (res.confirm) { app.clearCart(); this.refreshCart() } }
    })
  },

  onContactStore() { wx.makePhoneCall({ phoneNumber: '13800000000', fail: () => {} }) }
})
