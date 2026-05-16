import { createI18n } from 'vue-i18n'
import zh from './zh'
import en from './en'

const messages = {
  zh,
  en,
}

const savedLocale = localStorage.getItem('app_locale') || 'zh'

const i18n = createI18n({
  legacy: false,
  locale: savedLocale,
  fallbackLocale: 'zh',
  messages,
  missing: (locale, key) => {
    // 如果 key 是中文，在英文缺失时直接返回中文 key
    return key
  },
})

export function setLocale(locale) {
  i18n.global.locale.value = locale
  localStorage.setItem('app_locale', locale)
}

export function getLocale() {
  return i18n.global.locale.value
}

export default i18n
