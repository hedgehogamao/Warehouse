<template>
  <div class="detail-page" v-loading="loading">
    <div v-if="product" class="detail-content">
      <div class="detail-left">
        <div class="product-image">
          <img v-if="product.imageUrl" :src="getImageUrl(product.imageUrl)" :alt="product.name" />
          <div v-else class="no-image">📦</div>
        </div>
      </div>
      <div class="detail-right">
        <h1 class="product-name">{{ product.name }}</h1>
        <el-tag :type="getStockType(product.stockStatus)" class="stock-tag">
          {{ getStockText(product.stockStatus) }} ({{ product.stock }})
        </el-tag>
        <div class="price-row">
          <span class="price">¥{{ product.salePrice }}</span>
        </div>
        <div class="info-grid">
          <div class="info-item"><span class="label">{{ t('product.brand') }}</span><span>{{ product.brand || '-' }}</span></div>
          <div class="info-item"><span class="label">{{ t('product.sku') }}</span><span>{{ product.sku }}</span></div>
          <div class="info-item"><span class="label">{{ t('product.category') }}</span><span>{{ product.category || '-' }}</span></div>
          <div class="info-item" v-if="product.carModel"><span class="label">{{ t('product.carModel') }}</span>
            <div class="car-models">
              <el-tag v-for="m in product.carModel.split('/')" :key="m" size="small" type="info">{{ m }}</el-tag>
            </div>
          </div>
        </div>
        <div v-if="product.description" class="description">
          <h3>{{ t('product.description') }}</h3>
          <p>{{ product.description }}</p>
        </div>
        <div class="actions">
          <el-button size="large" @click="router.push('/cart')">{{ t('product.goToCart') }}</el-button>
          <el-button type="primary" size="large" @click="addToCart" :disabled="product.stockStatus === 'OUT'">
            {{ product.stockStatus === 'OUT' ? t('product.outOfStock') : t('product.addToCart') }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getProductById } from '@/api/product'
import { useCartStore } from '@/store'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const cartStore = useCartStore()

const product = ref(null)
const loading = ref(true)

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

function addToCart() {
  cartStore.addItem(product.value)
  ElMessage.success(t('message.addedToCart'))
}

onMounted(async () => {
  try {
    const res = await getProductById(route.params.id)
    product.value = res.data
  } catch (e) {
    // 错误已处理
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.detail-content {
  display: flex;
  gap: 32px;
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.detail-left {
  flex: 0 0 400px;
}

.product-image {
  width: 100%;
  height: 400px;
  background: #f5f3ef;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image { font-size: 80px; }

.detail-right {
  flex: 1;
}

.product-name {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 12px;
}

.stock-tag { margin-bottom: 16px; }

.price-row {
  margin-bottom: 24px;
}

.price {
  font-size: 32px;
  font-weight: 700;
  color: #cc785c;
}

.info-grid {
  display: grid;
  gap: 12px;
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  gap: 12px;
  font-size: 14px;
}

.info-item .label {
  color: #a09d96;
  min-width: 70px;
}

.car-models {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.description {
  margin-bottom: 24px;
}

.description h3 {
  font-size: 14px;
  color: #a09d96;
  margin-bottom: 8px;
}

.description p {
  font-size: 14px;
  line-height: 1.6;
  color: #666;
}

.actions {
  display: flex;
  gap: 12px;
}

@media (max-width: 768px) {
  .detail-content {
    flex-direction: column;
  }
  .detail-left {
    flex: none;
  }
  .product-image {
    height: 280px;
  }
}
</style>
