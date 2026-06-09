<template>
  <div class="pos-page">
    <el-row :gutter="20" style="height: 100%">
      <el-col :span="14">
        <el-card shadow="never" class="pos-left">
          <template #header><div class="pos-header"><span class="pos-title">{{ t('pos.title') }}</span></div></template>
          <div class="search-bar">
            <el-input v-model="searchKeyword" :placeholder="t('pos.searchPlaceholder')" size="large" clearable @input="handleSearchDebounce" @keyup.enter="handleBarcodeEnter">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-button size="large" @click="openScanDialog" class="scan-btn">
              <el-icon size="18"><Camera /></el-icon>
              <span style="margin-left: 4px">{{ t('pos.scanBtn') }}</span>
            </el-button>
          </div>
          <div class="product-results" v-loading="searching">
            <div v-if="searchResults.length === 0 && searchKeyword" class="empty-tip">{{ t('pos.noMatch') }}</div>
            <div v-if="!searchKeyword" class="empty-tip">{{ t('pos.searchTip') }}</div>
            <div v-for="p in searchResults" :key="p.id" class="product-item" @click="addToCart(p)">
              <div class="product-info">
                <div class="product-name">{{ p.name }}</div>
                <div class="product-meta">{{ p.sku }} · {{ t('pos.stock') }}: {{ p.stock }}</div>
              </div>
              <div class="product-price">C${{ p.salePrice }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never" class="pos-right">
          <template #header>
            <div class="pos-header">
              <span>{{ t('pos.cart') }}</span>
              <el-button link type="danger" size="small" @click="clearCart" v-if="cart.length > 0">{{ t('pos.clear') }}</el-button>
            </div>
          </template>
          <div v-if="cart.length === 0" class="empty-cart">
            <el-icon :size="48" color="#e6dfd8"><ShoppingCart /></el-icon>
            <p>{{ t('pos.emptyCart') }}</p>
          </div>
          <div v-else class="cart-list">
            <div v-for="(item, index) in cart" :key="item.productId" class="cart-item">
              <div class="cart-item-info">
                <div class="cart-item-name">{{ item.name }}</div>
                <div class="cart-item-price">C${{ item.unitPrice }}</div>
              </div>
              <div class="cart-item-actions">
                <el-input-number v-model="item.quantity" :min="1" :max="item.stock" size="small" @change="updateSubtotal(item)" />
                <span class="cart-item-subtotal">C${{ item.subtotal.toFixed(2) }}</span>
                <el-button link type="danger" size="small" @click="removeFromCart(index)"><el-icon><Delete /></el-icon></el-button>
              </div>
            </div>
          </div>
          <div v-if="cart.length > 0" class="cart-total">
            <span>{{ t('common.total') }}</span>
            <span class="total-amount">C${{ totalAmount.toFixed(2) }}</span>
          </div>
          <div v-if="cart.length > 0" class="customer-info">
            <el-input v-model="customerName" :placeholder="t('pos.customerName')" size="small" />
            <el-input v-model="customerPhone" :placeholder="t('pos.customerPhone')" size="small" />
          </div>
          <div v-if="cart.length > 0" class="payment-method">
            <el-radio-group v-model="paymentMethod" size="small">
              <el-radio-button value="CASH">{{ t('pos.paymentCash') }}</el-radio-button>
              <el-radio-button value="WECHAT">{{ t('pos.paymentWechat') }}</el-radio-button>
              <el-radio-button value="ALIPAY">{{ t('pos.paymentAlipay') }}</el-radio-button>
              <el-radio-button value="CARD">{{ t('pos.paymentCard') }}</el-radio-button>
            </el-radio-group>
          </div>
          <div v-if="cart.length > 0" class="cart-actions">
            <el-button type="primary" size="large" style="width: 100%" :loading="submitting" @click="handleCheckout">
              {{ t('pos.confirmPay') }} C${{ totalAmount.toFixed(2) }}
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 扫码弹窗 -->
    <el-dialog v-model="scanDialogVisible" :title="t('pos.scanTitle')" width="480px" :close-on-click-modal="false" @close="closeScanDialog">
      <div class="scan-dialog-body">
        <p class="scan-tip">{{ t('pos.scanTip') }}</p>
        <div id="pos-scan-region" class="scan-region"></div>
        <div class="manual-sku-row">
          <el-input v-model="manualSku" :placeholder="t('pos.skuPlaceholder')" size="large" @keyup.enter="handleManualSku">
            <template #append>
              <el-button @click="handleManualSku">OK</el-button>
            </template>
          </el-input>
        </div>
        <el-checkbox v-model="continuousScan" style="margin-top: 8px">{{ t('pos.continuousScan') }}</el-checkbox>
      </div>
      <template #footer>
        <el-button @click="closeScanDialog">{{ t('pos.stopScan') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Search, Delete, ShoppingCart, Camera } from '@element-plus/icons-vue'
import { posSearchProducts, createOrder } from '@/api/order'
import { lookupBySku } from '@/api/product'
import { Html5Qrcode } from 'html5-qrcode'

const { t } = useI18n()
const searchKeyword = ref('')
const searchResults = ref([])
const searching = ref(false)
let searchTimer = null

function handleSearchDebounce() { clearTimeout(searchTimer); searchTimer = setTimeout(() => doSearch(), 300) }
async function doSearch() {
  if (!searchKeyword.value || searchKeyword.value.length < 1) { searchResults.value = []; return }
  searching.value = true
  try { const res = await posSearchProducts(searchKeyword.value); searchResults.value = res.data || [] } catch (e) {} finally { searching.value = false }
}

// 扫码枪回车 — 先按条码精确查找，找到直接加入购物车
async function handleBarcodeEnter() {
  const keyword = searchKeyword.value?.trim()
  if (!keyword) return
  try {
    const res = await lookupBySku(keyword)
    if (res.data) {
      addToCart(res.data)
      ElMessage.success(res.data.name + ' 已加入购物车')
      searchKeyword.value = ''
      searchResults.value = []
      return
    }
  } catch (e) {}
  // 条码未找到，走普通关键词搜索
  doSearch()
}

// ===== 条码扫描 =====
const scanDialogVisible = ref(false)
const continuousScan = ref(true)
const manualSku = ref('')
let html5QrCode = null
let isScanning = false

function openScanDialog() {
  scanDialogVisible.value = true
  manualSku.value = ''
  nextTick(() => startScanner())
}

function closeScanDialog() {
  stopScanner()
  scanDialogVisible.value = false
}

async function startScanner() {
  if (isScanning) return
  try {
    html5QrCode = new Html5Qrcode('pos-scan-region')
    await html5QrCode.start(
      { facingMode: 'environment' },
      { fps: 10, qrbox: { width: 300, height: 150 }, aspectRatio: 1.5 },
      onScanSuccess,
      () => {} // 忽略扫描失败（每帧都会触发）
    )
    isScanning = true
  } catch (e) {
    ElMessage.error('无法启动摄像头，请检查权限设置')
  }
}

async function stopScanner() {
  if (html5QrCode && isScanning) {
    try { await html5QrCode.stop() } catch (e) {}
    isScanning = false
  }
}

async function onScanSuccess(decodedText) {
  if (!decodedText) return
  // 暂停扫描防止重复触发
  if (!continuousScan.value) await stopScanner()
  await lookupAndAddToCart(decodedText)
}

async function handleManualSku() {
  const sku = manualSku.value.trim()
  if (!sku) return
  await lookupAndAddToCart(sku)
  manualSku.value = ''
}

async function lookupAndAddToCart(sku) {
  try {
    const res = await lookupBySku(sku)
    const product = res.data
    if (!product) {
      ElMessage.warning(t('pos.productNotFound') + ': ' + sku)
      return
    }
    addToCart(product)
    ElMessage.success(product.name + ' 已加入购物车')
  } catch (e) {
    ElMessage.warning(t('pos.productNotFound') + ': ' + sku)
  }
}

onBeforeUnmount(() => { stopScanner() })

const cart = ref([])
const customerName = ref('')
const customerPhone = ref('')
const paymentMethod = ref('WECHAT')
const submitting = ref(false)
const totalAmount = computed(() => cart.value.reduce((sum, item) => sum + item.subtotal, 0))

function addToCart(product) {
  const existing = cart.value.find(item => item.productId === product.id)
  if (existing) { if (existing.quantity < existing.stock) { existing.quantity++; existing.subtotal = existing.quantity * existing.unitPrice } else { ElMessage.warning(t('pos.maxStock')) } }
  else { cart.value.push({ productId: product.id, name: product.name, sku: product.sku, unitPrice: product.salePrice, quantity: 1, stock: product.stock, subtotal: product.salePrice }) }
}

function removeFromCart(index) { cart.value.splice(index, 1) }
function updateSubtotal(item) { item.subtotal = item.quantity * item.unitPrice }
function clearCart() { cart.value = []; customerName.value = ''; customerPhone.value = ''; paymentMethod.value = 'WECHAT' }

async function handleCheckout() {
  if (cart.value.length === 0) return
  submitting.value = true
  try {
    const data = { customerName: customerName.value || undefined, customerPhone: customerPhone.value || undefined, paymentMethod: paymentMethod.value, items: cart.value.map(item => ({ productId: item.productId, quantity: item.quantity, unitPrice: item.unitPrice })) }
    const res = await createOrder(data)
    ElMessage.success(t('pos.orderSuccess', { orderNo: res.data.orderNo, amount: res.data.totalAmount }))
    clearCart()
  } catch (e) {} finally { submitting.value = false }
}

document.addEventListener('keydown', (e) => { if (e.key === 'F8') { e.preventDefault(); handleCheckout() }; if (e.key === 'F9') { e.preventDefault(); clearCart() } })
</script>

<style scoped>
.pos-page { height: calc(100vh - 130px); }
.pos-left, .pos-right { height: 100%; display: flex; flex-direction: column; }
.pos-header { display: flex; justify-content: space-between; align-items: center; }
.pos-title { font-family: var(--claude-font-display); font-size: 20px; font-weight: 500; color: #141413; }
.product-results { flex: 1; overflow-y: auto; margin-top: 12px; }
.empty-tip { text-align: center; color: #8e8b82; padding: 48px 0; font-size: 14px; }
.product-item { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; border-radius: 8px; cursor: pointer; transition: background-color 0.15s; }
.product-item:hover { background-color: #f5f0e8; }
.product-name { font-size: 14px; font-weight: 500; color: #141413; }
.product-meta { font-size: 12px; color: #8e8b82; margin-top: 2px; }
.product-price { font-family: var(--claude-font-display); font-size: 18px; font-weight: 500; color: #cc785c; }
.empty-cart { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #8e8b82; }
.cart-list { flex: 1; overflow-y: auto; }
.cart-item { padding: 10px 0; border-bottom: 1px solid #ebe6df; }
.cart-item-info { display: flex; justify-content: space-between; margin-bottom: 4px; }
.cart-item-name { font-size: 14px; font-weight: 500; color: #141413; }
.cart-item-price { font-size: 13px; color: #6c6a64; }
.cart-item-actions { display: flex; align-items: center; gap: 12px; }
.cart-item-subtotal { font-weight: 600; color: #141413; min-width: 70px; text-align: right; }
.cart-total { display: flex; justify-content: space-between; align-items: center; padding: 16px 0; border-top: 2px solid #e6dfd8; margin-top: 12px; font-size: 16px; font-weight: 600; color: #141413; }
.total-amount { font-family: var(--claude-font-display); font-size: 24px; color: #cc785c; }
.customer-info { display: flex; flex-direction: column; gap: 8px; margin-top: 12px; }
.payment-method { margin-top: 12px; }
.cart-actions { margin-top: 16px; }
.search-bar { display: flex; gap: 8px; }
.search-bar .el-input { flex: 1; }
.scan-btn { border-radius: 8px; }
.scan-dialog-body { text-align: center; }
.scan-tip { color: #8e8b82; font-size: 13px; margin-bottom: 12px; }
.scan-region { width: 100%; min-height: 240px; border-radius: 8px; overflow: hidden; background: #000; margin-bottom: 16px; }
.manual-sku-row { margin-top: 12px; }
</style>
