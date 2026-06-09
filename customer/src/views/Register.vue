<template>
  <div class="register-page">
    <div class="register-card">
      <h1 class="title">{{ t('register.title') }}</h1>
      <p class="subtitle">{{ t('app.name') }}</p>
      <el-form :model="form" @submit.prevent="handleRegister" label-position="top">
        <el-form-item :label="t('register.phone')">
          <el-input v-model="form.phone" prefix-icon="Phone" :placeholder="t('register.phone')" size="large" />
        </el-form-item>
        <el-form-item :label="t('register.name')">
          <el-input v-model="form.name" prefix-icon="User" :placeholder="t('register.name')" size="large" />
        </el-form-item>
        <el-form-item :label="t('register.password')">
          <el-input v-model="form.password" type="password" prefix-icon="Lock" :placeholder="t('register.password')" size="large" show-password />
        </el-form-item>
        <el-form-item :label="t('register.confirmPassword')">
          <el-input v-model="form.confirmPassword" type="password" prefix-icon="Lock" :placeholder="t('register.confirmPassword')" size="large" show-password @keyup.enter="handleRegister" />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="handleRegister" style="width:100%">
          {{ t('register.submit') }}
        </el-button>
      </el-form>
      <p class="switch-link">
        {{ t('register.hasAccount') }}
        <router-link to="/login">{{ t('register.login') }}</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'
import { useUserStore } from '@/store'

const router = useRouter()
const { t } = useI18n()
const userStore = useUserStore()
const loading = ref(false)
const form = ref({ phone: '', password: '', confirmPassword: '', name: '' })

async function handleRegister() {
  if (!form.value.phone || !form.value.password) return
  if (form.value.password !== form.value.confirmPassword) {
    ElMessage.warning(t('register.passwordMismatch'))
    return
  }
  loading.value = true
  try {
    const res = await register({
      phone: form.value.phone,
      password: form.value.password,
      name: form.value.name
    })
    userStore.setAuth(res.data)
    ElMessage.success(t('register.success'))
    router.push('/home')
  } catch (e) {
    // 错误已由 request 拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #faf9f5;
}

.register-card {
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
