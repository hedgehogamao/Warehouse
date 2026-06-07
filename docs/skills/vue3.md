# Skill: Vue3 管理后台开发规范

> 适用于前端开发（`frontend-dev`）

---

## 1 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.x | 核心框架（Composition API） |
| Vite | 5.x | 构建工具 |
| Element Plus | 2.x | UI 组件库 |
| Vue Router | 4.x | 路由管理 |
| Pinia | 2.x | 状态管理 |
| Axios | 1.x | HTTP 请求 |
| ECharts | 5.x | 图表（统计页面） |
| dayjs | — | 日期处理 |

---

## 2 目录结构

```
admin/src/
├── api/                    # API 请求模块
│   ├── request.js          # Axios 实例封装
│   ├── auth.js             # 认证 API
│   ├── product.js          # 商品 API
│   ├── stock-in.js         # 入库 API
│   ├── inventory.js        # 库存 API
│   ├── order.js            # 订单 API
│   ├── history.js          # 历史记录 API
│   └── statistics.js       # 统计 API
├── views/                  # 页面组件
│   ├── Login.vue           # 登录页
│   ├── Dashboard.vue       # 首页看板
│   ├── ProductList.vue     # 商品列表
│   ├── StockIn.vue         # 入库管理
│   ├── Inventory.vue       # 库存管理
│   ├── POS.vue             # POS 收银
│   ├── OrderList.vue       # 订单列表
│   ├── History.vue         # 历史记录
│   ├── Statistics.vue      # 数据统计
│   └── UserManagement.vue  # 用户管理
├── components/             # 公共组件
│   ├── InventoryLogDialog.vue    # 库存流水弹窗
│   ├── InventoryAdjustDialog.vue # 库存调整弹窗
│   └── OrderDetailDialog.vue     # 订单详情弹窗
├── layouts/                # 布局组件
│   └── MainLayout.vue      # 主布局（侧边栏+顶栏+内容区）
├── router/                 # 路由配置
│   └── index.js
├── store/                  # Pinia 状态
│   └── user.js             # 用户状态（Token、用户信息）
├── utils/                  # 工具函数
│   └── index.js
└── App.vue
```

---

## 3 代码风格

### 3.1 组件风格

- 使用 `<script setup>` 语法
- 使用 Composition API（`ref`、`reactive`、`computed`、`onMounted`）
- 组件文件名使用 PascalCase：`ProductList.vue`

```vue
<script setup>
import { ref, onMounted } from 'vue'
import { getProducts } from '@/api/product'

const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const queryParams = ref({ page: 1, size: 20, keyword: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getProducts(queryParams.value)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>
```

### 3.2 命名规范

| 类型 | 规则 | 示例 |
|------|------|------|
| 组件文件 | PascalCase | `ProductList.vue` |
| 变量 | camelCase | `tableData`、`queryParams` |
| 方法 | camelCase | `loadData`、`handleSearch` |
| 常量 | UPPER_SNAKE_CASE | `PAYMENT_METHODS` |
| CSS 类 | kebab-case | `.product-list`、`.search-bar` |

---

## 4 Axios 封装

```javascript
// api/request.js
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 10000
})

// 请求拦截器：自动携带 Token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一错误处理
request.interceptors.response.use(
  response => {
    const { code, message, data } = response.data
    if (code === 200) {
      return response.data
    }
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message))
  },
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      router.push('/login')
    }
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
```

---

## 5 路由设计

```javascript
const routes = [
  { path: '/login', component: () => import('@/views/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '首页' } },
      { path: 'products', component: () => import('@/views/ProductList.vue'), meta: { title: '商品管理' } },
      { path: 'stock-in', component: () => import('@/views/StockIn.vue'), meta: { title: '入库管理' } },
      { path: 'inventory', component: () => import('@/views/Inventory.vue'), meta: { title: '库存管理' } },
      { path: 'sales', component: () => import('@/views/POS.vue'), meta: { title: '销售收银' } },
      { path: 'orders', component: () => import('@/views/OrderList.vue'), meta: { title: '订单管理' } },
      { path: 'history', component: () => import('@/views/History.vue'), meta: { title: '历史记录' } },
      { path: 'users', component: () => import('@/views/UserManagement.vue'), meta: { title: '用户管理', roles: ['ADMIN'] } },
    ]
  }
]
```

**路由守卫**：未登录跳转 `/login`，无权限角色跳转首页。

---

## 6 状态管理（Pinia）

```javascript
// store/user.js
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  const setToken = (t) => {
    token.value = t
    localStorage.setItem('token', t)
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return { token, userInfo, setToken, logout }
})
```

---

## 7 Element Plus 使用规范

- 表格：`<el-table>` + `<el-table-column>`
- 表单：`<el-form>` + `<el-form-item>` + 校验规则
- 分页：`<el-pagination>`
- 弹窗：`<el-dialog>`
- 搜索栏：`<el-input>` + `<el-select>` + `<el-button>`
- 状态标签：`<el-tag>`（不同状态不同颜色）
- 消息提示：`ElMessage.success()` / `ElMessage.error()`
- 确认框：`ElMessageBox.confirm()`

---

## 8 页面通用模式

### 列表页

```
搜索栏 → 表格 → 分页
新增按钮 → 弹窗表单
编辑按钮 → 弹窗表单（预填数据）
删除按钮 → 确认框 → 调用删除接口
```

### 表单弹窗

- 使用 `v-model` 控制显示/隐藏
- 打开时重置表单 + 加载编辑数据
- 提交前校验（`formRef.validate()`）
- 提交成功后刷新列表 + 关闭弹窗

---

## 9 Vite 配置

```javascript
// vite.config.js
export default defineConfig({
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```
