import { createI18n } from 'vue-i18n'
import zh from './zh.json'
import es from './es.json'

const savedLocale = localStorage.getItem('customer_locale') || 'zh'

const i18n = createI18n({
  legacy: false,
  locale: savedLocale,
  fallbackLocale: 'zh',
  messages: { zh, es }
})

export default i18n
