<template>
  <div class="profile-page">
    <h1 class="page-title">{{ t('profile.title') }}</h1>

    <div class="profile-card">
      <el-form :model="form" label-position="top">
        <el-form-item :label="t('profile.name')">
          <el-input v-model="form.name" :placeholder="t('profile.name')" />
        </el-form-item>
        <el-form-item :label="t('profile.phone')">
          <el-input v-model="form.phone" :placeholder="t('profile.phone')" disabled />
        </el-form-item>
        <el-button type="primary" :loading="saving" @click="handleSave">{{ t('profile.save') }}</el-button>
      </el-form>
    </div>

    <!-- 语言切换 -->
    <div class="profile-card">
      <h3>{{ t('profile.language') }}</h3>
      <div class="lang-buttons">
        <el-button :type="locale === 'zh' ? 'primary' : 'default'" @click="switchLang('zh')">中文</el-button>
        <el-button :type="locale === 'es' ? 'primary' : 'default'" @click="switchLang('es')">Español</el-button>
      </div>
    </div>

    <!-- 退出登录 -->
    <div class="profile-card">
      <el-button type="danger" plain style="width:100%" @click="handleLogout">{{ t('profile.logout') }}</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile } from '@/api/customer'
import { useUserStore } from '@/store'

const router = useRouter()
const { t, locale } = useI18n()
const userStore = useUserStore()

const form = ref({ name: '', phone: '' })
const saving = ref(false)

function switchLang(lang) {
  locale.value = lang
  localStorage.setItem('customer_locale', lang)
}

async function handleSave() {
  saving.value = true
  try {
    await updateProfile({ name: form.value.name })
    userStore.updateInfo({ name: form.value.name, nickName: form.value.name })
    ElMessage.success(t('profile.saveSuccess'))
  } catch (e) {
    // 错误已处理
  } finally {
    saving.value = false
  }
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}

onMounted(async () => {
  try {
    const res = await getProfile()
    form.value.name = res.data?.name || ''
    form.value.phone = res.data?.phone || ''
  } catch (e) {
    // 错误已处理
  }
})
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
