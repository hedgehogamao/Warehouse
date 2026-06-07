/**
 * 汽车配件门店小程序 - 全局逻辑
 */
App({
  globalData: {
    baseUrl: 'http://localhost:8080/api/v1',
    token: '',
    userInfo: null
  },

  onLaunch() {
    // 检查登录态
    const token = wx.getStorageSync('token')
    if (token) {
      this.globalData.token = token
    }
  }
})
