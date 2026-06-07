# Skill: 微信小程序开发规范

> 适用于小程序开发（`mini-dev`）

---

## 1 技术选型

- **原生微信小程序**（非 uni-app / Taro）
- 开发工具：微信开发者工具
- 后端 API：复用 Spring Boot REST API（`/api/v1/miniapp/` 前缀）

---

## 2 目录结构

```
miniprogram/
├── app.js                  # 全局逻辑（登录、Token 管理）
├── app.json                # 全局配置（页面路径、TabBar、窗口样式）
├── app.wxss                # 全局样式（重置、通用类）
├── pages/
│   ├── index/              # 首页（分类入口 + 热门商品）
│   │   ├── index.js
│   │   ├── index.json
│   │   ├── index.wxml
│   │   └── index.wxss
│   ├── products/           # 商品相关
│   │   ├── products.js     # 商品列表页
│   │   ├── products.wxml
│   │   ├── products.wxss
│   │   └── products.json
│   │   ├── detail.js       # 商品详情页
│   │   ├── detail.wxml
│   │   ├── detail.wxss
│   │   └── detail.json
│   ├── orders/             # 购买记录页
│   │   ├── orders.js
│   │   ├── orders.wxml
│   │   ├── orders.wxss
│   │   └── orders.json
│   └── mine/               # 个人中心页
│       ├── mine.js
│       ├── mine.wxml
│       ├── mine.wxss
│       └── mine.json
└── utils/
    ├── api.js              # 请求封装
    ├── auth.js             # 登录与 Token 管理
    └── config.js           # 配置（API 地址等）
```

---

## 3 页面路径配置 (app.json)

```json
{
  "pages": [
    "pages/index/index",
    "pages/products/products",
    "pages/products/detail",
    "pages/orders/orders",
    "pages/mine/mine"
  ],
  "window": {
    "navigationBarTitleText": "XX汽配商店",
    "navigationBarBackgroundColor": "#1677FF",
    "navigationBarTextStyle": "white"
  },
  "tabBar": {
    "color": "#999999",
    "selectedColor": "#1677FF",
    "list": [
      { "pagePath": "pages/index/index", "text": "首页", "iconPath": "images/home.png", "selectedIconPath": "images/home-active.png" },
      { "pagePath": "pages/orders/orders", "text": "记录", "iconPath": "images/order.png", "selectedIconPath": "images/order-active.png" },
      { "pagePath": "pages/mine/mine", "text": "我的", "iconPath": "images/mine.png", "selectedIconPath": "images/mine-active.png" }
    ]
  }
}
```

---

## 4 请求封装 (utils/api.js)

```javascript
const BASE_URL = 'https://your-domain.com/api/v1/miniapp'

const request = (url, method = 'GET', data = {}) => {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token')
    wx.request({
      url: BASE_URL + url,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 200) {
          resolve(res.data)
        } else if (res.statusCode === 401) {
          // Token 过期，重新登录
          wx.removeStorageSync('token')
          wx.reLaunch({ url: '/pages/index/index' })
          reject(new Error('登录已过期'))
        } else {
          wx.showToast({ title: res.data.message || '请求失败', icon: 'none' })
          reject(new Error(res.data.message))
        }
      },
      fail: (err) => {
        wx.showToast({ title: '网络错误', icon: 'none' })
        reject(err)
      }
    })
  })
}

module.exports = { request }
```

---

## 5 登录流程

```javascript
// utils/auth.js
const login = () => {
  return new Promise((resolve, reject) => {
    wx.login({
      success: (loginRes) => {
        if (loginRes.code) {
          wx.getUserProfile({
            desc: '用于展示用户信息',
            success: (userRes) => {
              request('/auth/login', 'POST', {
                code: loginRes.code,
                nickName: userRes.userInfo.nickName,
                avatarUrl: userRes.userInfo.avatarUrl
              }).then(res => {
                wx.setStorageSync('token', res.data.token)
                wx.setStorageSync('userInfo', res.data.customerInfo)
                resolve(res.data)
              }).catch(reject)
            },
            fail: reject
          })
        }
      },
      fail: reject
    })
  })
}
```

**简化方案**（如不需要 getUserProfile）：仅通过 `wx.login` 获取 code 后端换 openid，首次登录后自动创建客户。

---

## 6 页面交互规范

### 6.1 首页

- 搜索框：点击跳转商品列表页并聚焦搜索
- 分类入口：九宫格/横向滚动，点击按分类筛选
- 热门商品：展示 5~10 个商品卡片（图片+名称+价格+库存状态）

### 6.2 商品列表

- 搜索：顶部搜索框，输入后 300ms 防抖请求
- 筛选：分类/品牌下拉筛选
- 列表：每项含图片、名称、价格、库存状态标签
- 分页：触底加载更多（`onReachBottom`）
- 点击跳转商品详情

### 6.3 商品详情

- 轮播图（如有多图）
- 名称、价格、库存状态
- 品牌、编码、适配车型
- 商品描述
- 底部"联系店家"按钮（拨打电话或复制微信号）
- **客户不可直接下单**，需联系店员

### 6.4 购买记录

- 当前客户的历史订单列表
- 每项显示：订单号、商品摘要、总金额、状态、时间
- 点击展开详情

### 6.5 个人中心

- 用户头像和昵称
- 菜单：我的购买记录、联系方式、设置

---

## 7 样式规范

- 使用 `rpx` 作为尺寸单位（750rpx = 屏幕宽度）
- 主题色：`#1677FF`
- 背景色：`#F5F5F5`
- 卡片背景：`#FFFFFF`，圆角 `12rpx`
- 字体大小：标题 `32rpx`，正文 `28rpx`，辅助 `24rpx`

---

## 8 注意事项

- 小程序请求必须使用 **HTTPS** 且域名已在微信后台配置
- 图片域名也需在微信后台配置
- `wx.getUserProfile` 已在基础库 2.27.1 调整，需关注最新登录策略
- 避免在 `onLoad` 中发起过多并发请求
- 使用 `wx.showLoading()` / `wx.hideLoading()` 提升体验
