<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="title">{{ t('login.title') }}</h1>
      <p class="subtitle">{{ t('app.name') }}</p>
      <el-form :model="form" @submit.prevent="handleLogin" label-position="top">
        <el-form-item :label="t('login.phone')">
          <el-input v-model="form.phone" prefix-icon="Phone" :placeholder="t('login.phone')" size="large" />
        </el-form-item>
        <el-form-item :label="t('login.password')">
          <el-input v-model="form.password" type="password" prefix-icon="Lock" :placeholder="t('login.password')" size="large" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="handleLogin" style="width:100%">
          {{ t('login.submit') }}
        </el-button>
      </el-form>
      <p class="switch-link">
        {{ t('login.noAccount') }}
        <router-link to="/register">{{ t('login.register') }}</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useUserStore } from '@/store'

const router = useRouter()
const { t } = useI18n()
const userStore = useUserStore()
const loading = ref(false)
const form = ref({ phone: '', password: '' })

async function handleLogin() {
  if (!form.value.phone || !form.value.password) return
  loading.value = true
  try {
    const res = await login(form.value)
    userStore.setAuth(res.data)
    ElMessage.success(t('login.success'))
    router.push('/home')
  } catch (e) {
    // 错误已由 request 拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #faf9f5;
}

.login-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.06);
}

.title {
  font-family: 'Cormorant Garamond', serif;
  font-size: 28px;
  font-weight: 700;
  text-align: center;
  margin-bottom: 4px;
}

.subtitle {
  text-align: center;
  color: #a09d96;
  font-size: 14px;
  margin-bottom: 32px;
}

.switch-link {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #666;
}

.switch-link a {
  color: #cc785c;
  font-weight: 500;
}
</style>
