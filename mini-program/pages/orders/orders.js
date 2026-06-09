const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    orders: [],
    page: 1,
    loading: false,
    noMore: false
  },

  onShow() {
    app.checkLogin(() => {
      this.setData({ orders: [], page: 1, noMore: false })
      this.loadOrders()
    })
  },

  async loadOrders() {
    if (this.data.loading || this.data.noMore) return
    this.setData({ loading: true })
    try {
      const res = await api.request(`/miniapp/orders?page=${this.data.page}&size=20`)
      const newOrders = this.data.page === 1 ? res.data.records : [...this.data.orders, ...res.data.records]
      this.setData({
        orders: newOrders,
        noMore: newOrders.length >= res.data.total,
        loading: false
      })
    } catch (e) { this.setData({ loading: false }) }
  },

  onReachBottom() {
    if (!this.data.noMore) {
      this.setData({ page: this.data.page + 1 })
      this.loadOrders()
    }
  },

  onPullDownRefresh() {
    this.setData({ orders: [], page: 1, noMore: false })
    this.loadOrders().then(() => wx.stopPullDownRefresh())
  }
})
