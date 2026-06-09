import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * 客户用户状态管理
 */
export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('customer_token') || '')
  const customerInfo = ref(JSON.parse(localStorage.getItem('customer_info') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const customerId = computed(() => customerInfo.value?.id)
  const customerName = computed(() => customerInfo.value?.name || customerInfo.value?.nickName || '客户')

  function setAuth(data) {
    token.value = data.token
    customerInfo.value = data.customerInfo
    localStorage.setItem('customer_token', data.token)
    localStorage.setItem('customer_info', JSON.stringify(data.customerInfo))
  }

  function logout() {
    token.value = ''
    customerInfo.value = null
    localStorage.removeItem('customer_token')
    localStorage.removeItem('customer_info')
  }

  function updateInfo(info) {
    customerInfo.value = { ...customerInfo.value, ...info }
    localStorage.setItem('customer_info', JSON.stringify(customerInfo.value))
  }

  return { token, customerInfo, isLoggedIn, customerId, customerName, setAuth, logout, updateInfo }
})

/**
 * 购物车状态管理（localStorage 持久化）
 */
export const useCartStore = defineStore('cart', () => {
  const items = ref(JSON.parse(localStorage.getItem('cart') || '[]'))

  const totalCount = computed(() => items.value.reduce((sum, item) => sum + item.quantity, 0))
  const totalAmount = computed(() => items.value.reduce((sum, item) => sum + item.salePrice * item.quantity, 0))

  function addItem(product) {
    const existing = items.value.find(item => item.id === product.id)
    if (existing) {
      existing.quantity++
    } else {
      items.value.push({
        id: product.id,
        name: product.name,
        sku: product.sku,
        salePrice: product.salePrice,
        stock: product.stock,
        imageUrl: product.imageUrl,
        quantity: 1
      })
    }
    save()
  }

  function removeItem(index) {
    items.value.splice(index, 1)
    save()
  }

  function updateQuantity(index, quantity) {
    if (quantity <= 0) {
      removeItem(index)
    } else {
      items.value[index].quantity = quantity
      save()
    }
  }

  function clearCart() {
    items.value = []
    save()
  }

  function save() {
    localStorage.setItem('cart', JSON.stringify(items.value))
  }

  return { items, totalCount, totalAmount, addItem, removeItem, updateQuantity, clearCart }
})
