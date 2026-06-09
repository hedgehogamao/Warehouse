<template>
  <div class="orders-page">
    <h1 class="page-title">{{ t('orders.title') }}</h1>

    <el-empty v-if="!loading && orders.length === 0" :description="t('orders.empty')" />

    <div v-loading="loading" class="order-list">
      <div v-for="order in orders" :key="order.id" class="order-card">
        <div class="order-header">
          <span class="order-no">{{ t('orders.orderNo') }} {{ order.orderNo }}</span>
          <el-tag :type="order.status === 'PAID' ? 'success' : 'info'" size="small">
            {{ t(`orders.status.${order.status}`) }}
          </el-tag>
        </div>
        <div class="order-items">
          <div v-for="item in order.items" :key="item.productName" class="order-item">
            <span class="item-name">{{ item.productName }}</span>
            <span class="item-qty">×{{ item.quantity }}</span>
            <span class="item-subtotal">C${{ item.subtotal }}</span>
          </div>
        </div>
        <div class="order-footer">
          <span class="order-date">{{ order.createdAt }}</span>
          <span class="order-total">{{ t('orders.total') }} C${{ order.totalAmount }}</span>
        </div>
      </div>
    </div>

    <div v-if="hasMore" class="load-more">
      <el-button @click="loadMore" :loading="loadingMore">{{ t('common.loading') }}</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getOrders } from '@/api/order'

const { t } = useI18n()

const orders = ref([])
const loading = ref(true)
const loadingMore = ref(false)
const page = ref(1)
const hasMore = ref(true)

async function loadOrders(reset = false) {
  if (reset) {
    page.value = 1
    hasMore.value = true
    orders.value = []
  }
  loading.value = orders.value.length === 0
  loadingMore.value = orders.value.length > 0
  try {
    const res = await getOrders({ page: page.value, size: 20 })
    const records = res.data?.records || []
    if (reset) {
      orders.value = records
    } else {
      orders.value.push(...records)
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
  loadOrders()
}

onMounted(() => loadOrders(true))
</script>

<style scoped>
.page-title {
  font-family: 'Cormorant Garamond', serif;
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 20px;
}

.order-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px 20px;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.order-no {
  font-size: 13px;
  color: #666;
}

.order-items {
  border-top: 1px solid #f0eeea;
  padding-top: 8px;
  margin-bottom: 8px;
}

.order-item {
  display: flex;
  align-items: center;
  padding: 6px 0;
  font-size: 14px;
}

.item-name { flex: 1; }
.item-qty { color: #a09d96; margin: 0 12px; }
.item-subtotal { font-weight: 500; min-width: 80px; text-align: right; }

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #f0eeea;
  padding-top: 8px;
}

.order-date { font-size: 12px; color: #a09d96; }
.order-total { font-size: 16px; font-weight: 600; color: #cc785c; }
.load-more { text-align: center; padding: 20px; }
</style>
