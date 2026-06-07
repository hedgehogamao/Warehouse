/**
 * 小程序请求封装
 * 统一处理请求头、Token、错误提示
 */
const app = getApp()

/**
 * 封装 wx.request
 * @param {string} url - 接口路径（不含 baseUrl）
 * @param {string} method - 请求方法
 * @param {object} data - 请求数据
 * @returns {Promise}
 */
function request(url, method = 'GET', data = {}) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: app.globalData.baseUrl + url,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': app.globalData.token ? `Bearer ${app.globalData.token}` : ''
      },
      success(res) {
        if (res.statusCode === 200 && res.data.code === 200) {
          resolve(res.data)
        } else if (res.statusCode === 401) {
          // Token 过期，清除登录态
          wx.removeStorageSync('token')
          app.globalData.token = ''
          wx.showToast({ title: '请先登录', icon: 'none' })
          reject(new Error('未登录'))
        } else {
          wx.showToast({ title: res.data.message || '请求失败', icon: 'none' })
          reject(new Error(res.data.message))
        }
      },
      fail(err) {
        wx.showToast({ title: '网络连接失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

module.exports = {
  request
}
