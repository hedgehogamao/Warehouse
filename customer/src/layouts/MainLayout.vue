<template>
  <div class="layout">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <router-link to="/home" class="logo">
          <span class="logo-text">{{ t('app.name') }}</span>
        </router-link>
        <nav class="nav">
          <router-link to="/home" class="nav-link" :class="{ active: route.path === '/home' }">
            {{ t('nav.home') }}
          </router-link>
          <router-link to="/products" class="nav-link" :class="{ active: route.path.startsWith('/products') }">
            {{ t('nav.products') }}
          </router-link>
          <router-link to="/cart" class="nav-link cart-link" :class="{ active: route.path === '/cart' }">
            {{ t('nav.cart') }}
            <span v-if="cartStore.totalCount > 0" class="cart-badge">{{ cartStore.totalCount }}</span>
          </router-link>
          <router-link to="/orders" class="nav-link" :class="{ active: route.path === '/orders' }">
            {{ t('nav.orders') }}
          </router-link>
          <router-link to="/profile" class="nav-link" :class="{ active: route.path === '/profile' }">
            {{ t('nav.profile') }}
          </router-link>
        </nav>
        <div class="header-right">
          <el-dropdown @command="switchLang" trigger="click">
            <span class="lang-btn">{{ locale === 'zh' ? '中文' : 'EN' }} ▾</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="zh">中文</el-dropdown-item>
                <el-dropdown-item command="es">Español</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- 页面内容 -->
    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useCartStore } from '@/store'

const route = useRoute()
const { t, locale } = useI18n()
const cartStore = useCartStore()

function switchLang(lang) {
  locale.value = lang
  localStorage.setItem('customer_locale', lang)
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  background: #faf9f5;
}

.header {
  background: #181715;
  color: #fff;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 60px;
  display: flex;
  align-items: center;
  gap: 32px;
}

.logo-text {
  font-family: 'Cormorant Garamond', serif;
  font-size: 20px;
  font-weight: 700;
  color: #cc785c;
  white-space: nowrap;
}

.nav {
  display: flex;
  gap: 4px;
  flex: 1;
}

.nav-link {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  color: #a09d96;
  transition: all 0.2s;
  position: relative;
  white-space: nowrap;
}

.nav-link:hover, .nav-link.active {
  color: #fff;
  background: rgba(255,255,255,0.1);
}

.cart-badge {
  position: absolute;
  top: 2px;
  right: 4px;
  background: #cc785c;
  color: #fff;
  font-size: 11px;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}

.header-right {
  display: flex;
  align-items: center;
}

.lang-btn {
  color: #a09d96;
  cursor: pointer;
  font-size: 14px;
  padding: 6px 12px;
  border-radius: 4px;
  transition: all 0.2s;
}

.lang-btn:hover {
  color: #fff;
  background: rgba(255,255,255,0.1);
}

.main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  min-height: calc(100vh - 60px);
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 12px;
    gap: 8px;
  }
  .logo-text {
    font-size: 16px;
  }
  .nav-link {
    padding: 6px 8px;
    font-size: 12px;
  }
  .main {
    padding: 12px;
  }
}
</style>
