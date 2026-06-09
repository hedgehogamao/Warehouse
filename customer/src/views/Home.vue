<template>
  <div class="home">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="keyword" :placeholder="t('home.searchPlaceholder')" prefix-icon="Search" size="large" clearable @keyup.enter="goSearch" />
    </div>

    <!-- 分类导航 -->
    <div class="categories">
      <el-tag v-for="cat in categories" :key="cat" :type="selectedCategory === cat ? '' : 'info'" effect="plain" class="cat-tag" @click="goCategory(cat)">
        {{ cat }}
      </el-tag>
    </div>

    <!-- 热门商品 -->
    <h2 class="section-title">{{ t('home.hotProducts') }}</h2>
    <div class="product-grid">
      <div v-for="product in hotProducts" :key="product.id" class="product-card" @click="goDetail(product.id)">
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
            <span class="price">¥{{ product.salePrice }}</span>
            <el-button type="primary" size="small" circle @click.stop="addToCart(product)" :disabled="product.stockStatus === 'OUT'">
              <el-icon><Plus /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="view-all">
      <el-button @click="router.push('/products')">{{ t('home.viewAll') }} →</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getProducts, getCategories } from '@/api/product'
import { useCartStore } from '@/store'

const router = useRouter()
const { t } = useI18n()
const cartStore = useCartStore()

const keyword = ref('')
const categories = ref([])
const selectedCategory = ref('')
const hotProducts = ref([])

function getImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return 'https://warehouse-production-d396.up.railway.app' + url
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

function goSearch() {
  router.push({ path: '/products', query: { keyword: keyword.value } })
}

function goCategory(cat) {
  router.push({ path: '/products', query: { category: cat } })
}

function goDetail(id) {
  router.push(`/products/${id}`)
}

function addToCart(product) {
  cartStore.addItem(product)
  ElMessage.success(t('message.addedToCart'))
}

onMounted(async () => {
  try {
    const [catRes, prodRes] = await Promise.all([
      getCategories(),
      getProducts({ page: 1, size: 6 })
    ])
    categories.value = catRes.data || []
    hotProducts.value = prodRes.data?.records || []
  } catch (e) {
    // 错误已处理
  }
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 20px;
}

.categories {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 24px;
}

.cat-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.cat-tag:hover {
  transform: translateY(-1px);
}

.section-title {
  font-family: 'Cormorant Garamond', serif;
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 16px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
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

.no-image {
  font-size: 40px;
}

.stock-tag {
  position: absolute;
  top: 8px;
  right: 8px;
}

.product-info {
  padding: 12px;
}

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

.view-all {
  text-align: center;
}
</style>
