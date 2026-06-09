<template>
  <div class="profile-page">
    <h1 class="page-title">{{ t('profile.title') }}</h1>

    <!-- 语言切换 -->
    <div class="profile-card">
      <h3>{{ t('profile.language') }}</h3>
      <div class="lang-buttons">
        <el-button :type="locale === 'zh' ? 'primary' : 'default'" @click="switchLang('zh')">中文</el-button>
        <el-button :type="locale === 'es' ? 'primary' : 'default'" @click="switchLang('es')">Español</el-button>
      </div>
    </div>

    <!-- 联系门店 -->
    <div class="profile-card">
      <el-button type="primary" plain style="width:100%" @click="callStore">
        📞 {{ t('cart.contactStore') }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'

const { t, locale } = useI18n()

function switchLang(lang) {
  locale.value = lang
  localStorage.setItem('customer_locale', lang)
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

.profile-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.profile-card h3 {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 12px;
}

.lang-buttons {
  display: flex;
  gap: 8px;
}
</style>
