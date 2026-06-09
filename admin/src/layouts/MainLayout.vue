<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <h3>{{ t('app.title') }}</h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#181715"
        text-color="#a09d96"
        active-text-color="#cc785c"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>{{ t('nav.home') }}</span>
        </el-menu-item>
        <el-menu-item index="/products">
          <el-icon><Goods /></el-icon>
          <span>{{ t('nav.products') }}</span>
        </el-menu-item>
        <el-menu-item index="/stock-in">
          <el-icon><Download /></el-icon>
          <span>{{ t('nav.stockIn') }}</span>
        </el-menu-item>
        <el-menu-item index="/inventory">
          <el-icon><Box /></el-icon>
          <span>{{ t('nav.inventory') }}</span>
        </el-menu-item>
        <el-menu-item index="/sales">
          <el-icon><ShoppingCart /></el-icon>
          <span>{{ t('nav.sales') }}</span>
        </el-menu-item>
        <el-menu-item index="/orders">
          <el-icon><Document /></el-icon>
          <span>{{ t('nav.orders') }}</span>
        </el-menu-item>
        <el-menu-item index="/history">
          <el-icon><Clock /></el-icon>
          <span>{{ t('nav.history') }}</span>
        </el-menu-item>
        <el-menu-item index="/statistics">
          <el-icon><DataAnalysis /></el-icon>
          <span>{{ t('nav.statistics') }}</span>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <span>{{ t('nav.users') }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">{{ t('nav.home') }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <LanguageSwitcher />
          <el-dropdown @command="handleCommand" style="margin-left: 16px;">
            <span class="user-info">
              <el-icon><UserFilled /></el-icon>
              {{ userStore.userInfo.username || t('nav.admin') }}
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">{{ t('nav.logout') }}</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 页面内容 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { HomeFilled, UserFilled, User, ArrowDown, Goods, Download, Box, ShoppingCart, Document, Clock, DataAnalysis } from '@element-plus/icons-vue'
import { useUserStore } from '@/store'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

function handleCommand(command) {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.main-layout {
  height: 100vh;
}
.sidebar {
  background-color: #181715;
  overflow-y: auto;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #faf9f5;
  font-family: 'Cormorant Garamond', 'EB Garamond', Garamond, serif;
  font-size: 17px;
  font-weight: 500;
  letter-spacing: -0.02em;
  border-bottom: 1px solid #252320;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #faf9f5;
  border-bottom: 1px solid #e6dfd8;
}
.header-right {
  display: flex;
  align-items: center;
}
.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #3d3d3a;
  font-size: 14px;
  gap: 4px;
}
.main-content {
  background-color: #faf9f5;
  padding: 24px;
}
</style>
