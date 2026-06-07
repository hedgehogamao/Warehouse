<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# mini-program

## Purpose
微信小程序客户端，供门店客户自助查询商品信息、库存状态和个人购买记录。客户通过微信授权登录后使用。

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `pages/index/` | 首页 - 商品浏览/搜索入口 |
| `pages/products/` | 商品列表与详情页 |
| `pages/orders/` | 购买记录页 |
| `pages/mine/` | 个人中心页 |

## For AI Agents

### Working In This Directory
- 使用微信小程序原生开发框架
- 调用后端 API 需在微信公众平台配置合法域名
- 用户登录使用 `wx.login()` + 后端 JWT 鉴权
- 小程序包体积控制在 2MB 以内

### Key Pages
| Page | Description |
|------|-------------|
| 首页 | 商品分类导航、搜索入口、热门推荐 |
| 商品列表 | 按分类/品牌筛选、库存状态显示 |
| 商品详情 | 商品信息、适配车型、库存查询 |
| 购买记录 | 历史订单列表与详情 |
| 个人中心 | 微信头像/昵称、设置 |

## Dependencies

### Internal
- `server/` - 后端 API 接口

### External
- 微信小程序 SDK (wx.*)
- 微信开发者工具

<!-- MANUAL: -->
