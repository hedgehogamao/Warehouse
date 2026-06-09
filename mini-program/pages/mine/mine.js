const i18n = require('../../utils/i18n')

Page({
  data: {
    phone: '', name: '', locale: 'zh', t: {}
  },

  onLoad() {
    const userInfo = wx.getStorageSync('userInfo')
    if (userInfo) this.setData({ phone: userInfo.phone || '', name: userInfo.name || '' })
  },

  onShow() {
    this.setData({ locale: i18n.getLocale(), t: this.getTranslations() })
  },

  getTranslations() {
    return {
      storeName: i18n.t('mine.storeName'), storeSub: i18n.t('mine.storeSub'),
      myInfo: i18n.t('mine.myInfo'), name: i18n.t('mine.name'),
      namePlaceholder: i18n.t('mine.namePlaceholder'), phone: i18n.t('mine.phone'),
      phonePlaceholder: i18n.t('mine.phonePlaceholder'), saved: i18n.t('mine.saved'),
      callStore: i18n.t('mine.callStore'), language: i18n.t('mine.language'),
      langZh: i18n.t('mine.langZh'), langEs: i18n.t('mine.langEs')
    }
  },

  onPhoneInput(e) { this.setData({ phone: e.detail.value }) },
  onNameInput(e) { this.setData({ name: e.detail.value }) },

  onSave() {
    wx.setStorageSync('userInfo', { phone: this.data.phone, name: this.data.name })
    wx.showToast({ title: this.data.t.saved })
  },

  onCallStore() { wx.makePhoneCall({ phoneNumber: '13800000000', fail: () => {} }) },

  onSwitchLang(e) {
    const locale = e.currentTarget.dataset.locale
    i18n.setLocale(locale)
    this.setData({ locale, t: this.getTranslations() })
    // 更新 Tab 栏文字
    wx.setTabBarItem({ index: 0, text: i18n.t('tab.home') })
    wx.setTabBarItem({ index: 1, text: i18n.t('tab.products') })
    wx.setTabBarItem({ index: 2, text: i18n.t('tab.stockout') })
    wx.setTabBarItem({ index: 3, text: i18n.t('tab.cart') })
    wx.setTabBarItem({ index: 4, text: i18n.t('tab.mine') })
  }
})
