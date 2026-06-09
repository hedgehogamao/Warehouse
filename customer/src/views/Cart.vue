<template>
  <div class="cart-page">
    <h1 class="page-title">{{ t('cart.title') }}</h1>

    <el-empty v-if="cartStore.items.length === 0" :description="t('cart.empty')">
      <el-button type="primary" @click="router.push('/products')">{{ t('cart.goShopping') }}</el-button>
    </el-empty>

    <div v-else class="cart-content">
      <div class="cart-items">
        <div v-for="(item, index) in cartStore.items" :key="item.id" class="cart-item">
          <div class="item-image" @click="router.push(`/products/${item.id}`)">
            <img v-if="item.imageUrl" :src="getImageUrl(item.imageUrl)" :alt="item.name" />
            <div v-else class="no-image">📦</div>
          </div>
          <div class="item-info">
            <h3 class="item-name" @click="router.push(`/products/${item.id}`)">{{ item.name }}</h3>
            <p class="item-sku">{{ item.sku }}</p>
            <p class="item-price">¥{{ item.salePrice }}</p>
          </div>
          <div class="item-actions">
            <div class="quantity-control">
              <el-button size="small" circle @click="cartStore.updateQuantity(index, item.quantity - 1)">
                <el-icon><Minus /></el-icon>
              </el-button>
              <span class="quantity">{{ item.quantity }}</span>
              <el-button size="small" circle @click="cartStore.updateQuantity(index, item.quantity + 1)" :disabled="item.quantity >= item.stock">
                <el-icon><Plus /></el-icon>
              </el-button>
            </div>
            <p class="item-subtotal">¥{{ (item.salePrice * item.quantity).toFixed(2) }}</p>
            <el-button type="danger" text size="small" @click="cartStore.removeItem(index)">
              {{ t('cart.delete') }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 底部结算区 -->
      <div class="cart-footer">
        <div class="footer-left">
          <el-button text @click="cartStore.clearCart()">{{ t('cart.clear') }}</el-button>
        </div>
        <div class="footer-right">
          <div class="total">
            <span class="total-label">{{ t('cart.total') }}</span>
            <span class="total-amount">¥{{ cartStore.totalAmount.toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <!-- 到店提示 -->
      <div class="store-tip">
        <el-alert :title="t('cart.tip1')" :description="t('cart.tip2')" type="info" show-icon :closable="false" />
        <el-button type="primary" size="large" style="width:100%;margin-top:12px" @click="callStore">
          📞 {{ t('cart.contactStore') }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useCartStore } from '@/store'

const router = useRouter()
const { t } = useI18n()
const cartStore = useCartStore()

function getImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return 'https://warehouse-production-d396.up.railway.app' + url
}

function callStore() {
  window.location.href = 'tel:13800000000'
}
</script>

<style scoped>
.page-title {
  font-family: 'Cormorant Garamond', serif;
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 20px;
}

.cart-content {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  border-bottom: 1px solid #f0eeea;
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  background: #f5f3ef;
  overflow: hidden;
  cursor: pointer;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image { font-size: 30px; }

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  margin-bottom: 4px;
}

.item-sku {
  font-size: 12px;
  color: #a09d96;
  margin-bottom: 4px;
}

.item-price {
  font-size: 14px;
  color: #cc785c;
  font-weight: 500;
}

.item-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 8px;
}

.quantity {
  font-weight: 500;
  min-width: 24px;
  text-align: center;
}

.item-subtotal {
  font-size: 16px;
  font-weight: 600;
  color: #181715;
}

.cart-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-top: 1px solid #f0eeea;
}

.total {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.total-label {
  font-size: 14px;
  color: #666;
}

.total-amount {
  font-size: 24px;
  font-weight: 700;
  color: #cc785c;
}

.store-tip {
  padding: 20px;
  background: #faf9f5;
}
</style>
