<template>
  <el-dropdown @command="switchLocale" trigger="click">
    <span class="lang-switcher">
      <el-icon><Operation /></el-icon>
      {{ currentLabel }}
      <el-icon class="el-icon--right"><ArrowDown /></el-icon>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="zh" :disabled="locale === 'zh'">中文</el-dropdown-item>
        <el-dropdown-item command="es" :disabled="locale === 'es'">Español</el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { Operation, ArrowDown } from '@element-plus/icons-vue'

const { locale } = useI18n()

const currentLabel = computed(() => locale.value === 'zh' ? '中文' : 'Español')

function switchLocale(lang) {
  locale.value = lang
  localStorage.setItem('locale', lang)
}
</script>

<style scoped>
.lang-switcher {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #3d3d3a;
  font-size: 14px;
  gap: 4px;
}
</style>
