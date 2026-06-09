/**
 * 汽车配件门店小程序 - 全局逻辑
 * 客户端无需登录，购物车存储在本地
 */
App({
  globalData: {
    baseUrl: 'https://warehouse-production-d396.up.railway.app/api/v1',
    serverOrigin: 'https://warehouse-production-d396.up.railway.app',
    cart: wx.getStorageSync('cart') || []
  },

  /** 将相对路径图片 URL 转为完整 URL */
  getImageUrl(url) {
    if (!url) return ''
    if (url.startsWith('http')) return url
    return this.globalData.serverOrigin + url
  },

  /** 添加到购物车 */
  addToCart(product) {
    const cart = this.globalData.cart
    const existing = cart.find(item => item.id === product.id)
    if (existing) {
      existing.quantity++
    } else {
      cart.push({
        id: product.id,
        name: product.name,
        sku: product.sku,
        salePrice: product.salePrice,
        stock: product.stock,
        quantity: 1
      })
    }
    this.globalData.cart = cart
    wx.setStorageSync('cart', cart)
    wx.showToast({ title: '已加入购物车' })
  },

  /** 移除购物车商品 */
  removeFromCart(index) {
    this.globalData.cart.splice(index, 1)
    wx.setStorageSync('cart', this.globalData.cart)
  },

  /** 更新数量 */
  updateCartQuantity(index, quantity) {
    if (quantity <= 0) {
      this.removeFromCart(index)
    } else {
      this.globalData.cart[index].quantity = quantity
      wx.setStorageSync('cart', this.globalData.cart)
    }
  },

  /** 清空购物车 */
  clearCart() {
    this.globalData.cart = []
    wx.setStorageSync('cart', [])
  },

  /** 购物车总金额 */
  getCartTotal() {
    return this.globalData.cart.reduce((sum, item) => sum + item.salePrice * item.quantity, 0)
  },

  /** 购物车总数量 */
  getCartCount() {
    return this.globalData.cart.reduce((sum, item) => sum + item.quantity, 0)
  }
})
