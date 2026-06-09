<template>
  <div class="products-page">
    <!-- 搜索和筛选 -->
    <div class="toolbar">
      <el-input v-model="keyword" :placeholder="t('products.searchPlaceholder')" prefix-icon="Search" clearable @keyup.enter="loadProducts(true)" />
      <div class="categories">
        <el-tag :type="!currentCategory ? '' : 'info'" effect="plain" @click="filterCategory('')">{{ t('products.allCategories') }}</el-tag>
        <el-tag v-for="cat in categories" :key="cat" :type="currentCategory === cat ? '' : 'info'" effect="plain" @click="filterCategory(cat)">{{ cat }}</el-tag>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="product-grid" v-loading="loading">
      <div v-for="product in products" :key="product.id" class="product-card" @click="goDetail(product.id)">
        <div class="product-image">
          <img v-if="product.imageUrl" :src="getImageUrl(product.imageUrl)" :alt="product.name" />
          <div v-else class="no-image">📦</div>
          <el-tag :type="getStockType(product.stockStatus)" size="small" class="stock-tag">
            {{ getStockText(product.stockStatus) }}
          </el-tag>
        </div>
        <div class="product-info">
          <h3 class="product-name">{{ product.name }}</h3>
          <p class="product-meta">{{ product.brand }} · {{ product.category }}</p>
          <div class="product-bottom">
            <span class="price">C${{ product.salePrice }}</span>
            <el-button type="primary" size="small" circle @click.stop="addToCart(product)" :disabled="product.stockStatus === 'OUT'">
              <el-icon><Plus /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && products.length === 0" :description="t('products.noProducts')" />

    <!-- 加载更多 -->
    <div v-if="hasMore" class="load-more" v-loading="loadingMore">
      <el-button @click="loadMore">{{ t('common.loading') }}</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getProducts, getCategories } from '@/api/product'
import { useCartStore } from '@/store'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const cartStore = useCartStore()

const keyword = ref(route.query.keyword || '')
const currentCategory = ref(route.query.category || '')
const categories = ref([])
const products = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const page = ref(1)
const hasMore = ref(true)

function getImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return url
}

function getStockType(status) {
  if (status === 'OUT') return 'danger'
  if (status === 'LOW') return 'warning'
  return 'success'
}

function getStockText(status) {
  if (status === 'OUT') return t('stock.outOfStock')
  if (status === 'LOW') return t('stock.lowStock')
  return t('stock.inStock')
}

function goDetail(id) {
  router.push(`/products/${id}`)
}

function addToCart(product) {
  cartStore.addItem(product)
  ElMessage.success(t('message.addedToCart'))
}

function filterCategory(cat) {
  currentCategory.value = cat
  loadProducts(true)
}

async function loadProducts(reset = false) {
  if (reset) {
    page.value = 1
    hasMore.value = true
    products.value = []
  }
  loading.value = products.value.length === 0
  loadingMore.value = products.value.length > 0
  try {
    const params = { page: page.value, size: 20 }
    if (keyword.value) params.keyword = keyword.value
    if (currentCategory.value) params.category = currentCategory.value
    const res = await getProducts(params)
    const records = res.data?.records || []
    if (reset) {
      products.value = records
    } else {
      products.value.push(...records)
    }
    hasMore.value = records.length === 20
  } catch (e) {
    // 错误已处理
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function loadMore() {
  page.value++
  loadProducts()
}

onMounted(async () => {
  try {
    const catRes = await getCategories()
    categories.value = catRes.data || []
  } catch (e) {}
  loadProducts(true)
})

watch(() => route.query, (q) => {
  if (q.keyword !== undefined) keyword.value = q.keyword
  if (q.category !== undefined) currentCategory.value = q.category
  loadProducts(true)
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 20px;
}

.categories {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.categories .el-tag {
  cursor: pointer;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.product-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.product-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}

.product-image {
  position: relative;
  height: 160px;
  background: #f5f3ef;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image { font-size: 40px; }

.stock-tag {
  position: absolute;
  top: 8px;
  right: 8px;
}

.product-info { padding: 12px; }

.product-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-meta {
  font-size: 12px;
  color: #a09d96;
  margin-bottom: 8px;
}

.product-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price {
  font-size: 18px;
  font-weight: 600;
  color: #cc785c;
}

.load-more {
  text-align: center;
  padding: 20px;
}
</style>
