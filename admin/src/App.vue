<template>
  <el-config-provider :locale="epLocale">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import { computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import es from 'element-plus/dist/locale/es.mjs'

const { locale } = useI18n()

const epLocale = computed(() => locale.value === 'es' ? es : zhCn)

// 同步 HTML lang 属性
watch(locale, (val) => {
  document.documentElement.lang = val === 'es' ? 'es' : 'zh-CN'
}, { immediate: true })
</script>

<style>
/* 全局样式重置 + Claude 设计系统基础 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
html, body, #app {
  height: 100%;
  font-family: 'Inter', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', -apple-system, sans-serif;
  background-color: #faf9f5;
  color: #141413;
}

/* 衬线标题全局样式 */
h1, h2, h3, h4 {
  font-family: 'Cormorant Garamond', 'EB Garamond', Garamond, 'Times New Roman', serif;
  font-weight: 500;
  letter-spacing: -0.02em;
  color: #141413;
}

/* 滚动条美化（WebKit） */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: #e6dfd8;
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: #d4cdc5;
}
</style>
