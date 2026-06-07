<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# admin

## Purpose
基于 Vue 3 + Element Plus 的 PC 端管理后台，供门店店员使用。包含用户管理、商品管理、入库管理、销售收银、库存管理、数据统计等功能。

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/views/` | 页面视图组件 |
| `src/components/` | 公共 UI 组件 |
| `src/api/` | Axios API 封装 |
| `src/router/` | Vue Router 路由配置 |
| `src/store/` | Pinia 状态管理 |
| `src/assets/` | 静态资源 |

## For AI Agents

### Working In This Directory
- 使用 Vue 3 Composition API (`<script setup>`)
- 组件使用 Element Plus 组件库
- API 请求统一通过 `src/api/` 封装的模块调用
- 使用 Pinia 管理全局状态
- 使用 Vue Router 管理路由

### Common Patterns
```vue
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import { listProducts } from '@/api/product'
</script>
```

### Key Routes
| Path | View | Description |
|------|------|-------------|
| `/login` | Login | 登录页 |
| `/` | Dashboard | 首页看板 |
| `/products` | ProductList | 商品列表 |
| `/stock-in` | StockIn | 入库管理 |
| `/sales` | POS | 销售收银 |
| `/inventory` | Inventory | 库存管理 |
| `/history` | History | 历史记录 |
| `/statistics` | Statistics | 数据统计 |

## Dependencies

### Internal
- `server/` - 后端 API 接口

### External
- Vue 3, Vue Router 4, Pinia
- Element Plus, Axios, ECharts

<!-- MANUAL: -->
