import { createI18n } from 'vue-i18n'
import zh from './zh.json'
import es from './es.json'

/** 从 localStorage 读取语言偏好，默认中文 */
const savedLocale = localStorage.getItem('locale') || 'zh'

const i18n = createI18n({
  legacy: false, // 使用 Composition API 模式
  locale: savedLocale,
  fallbackLocale: 'zh',
  messages: { zh, es }
})

export default i18n
