import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

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
