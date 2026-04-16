<template>
  <div class="auth-page">
    <div class="auth-wrapper">
      <el-card class="auth-card glass-card">
        <div class="auth-header">
          <div class="icon-container">
            <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path d="M15 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm-9-2V7H4v3H1v2h3v3h2v-3h3v-2H6zm9 4c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
            </svg>
          </div>
          <h2 class="auth-title animated-title">用户注册</h2>
          <p class="auth-subtitle">创建您的账户，开启智能冰箱管理之旅</p>
        </div>

        <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            class="auth-form"
            @keyup.enter="handleRegister"
        >
          <el-form-item prop="username">
            <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名（最多50个字符）"
                class="enhanced-input"
                clearable
                size="large"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码"
                class="enhanced-input"
                show-password
                clearable
                size="large"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                class="enhanced-input"
                show-password
                clearable
                size="large"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="mobile">
            <el-input
                v-model="registerForm.mobile"
                placeholder="请输入手机号码"
                class="enhanced-input"
                clearable
                size="large"
            >
              <template #prefix>
                <el-icon><Phone /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item class="button-group">
            <el-button
                type="primary"
                @click="handleRegister"
                class="enhanced-button auth-primary-btn"
                :loading="loading"
            >
              {{ loading ? '注册中...' : '注 册' }}
            </el-button>
            <el-button
                @click="handleBackToLogin"
                class="enhanced-button auth-secondary-btn"
            >
              返 回
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
    <CopyrightFooter />
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { User, Lock, Phone } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import showMessage from '@/utils/message'
import CopyrightFooter from "@/components/CopyrightFooter.vue";

const router = useRouter()
const registerFormRef = ref()
const loading = ref(false)

// 注册表单数据
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  mobile: ''
})

// 自定义验证规则：确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 表单验证规则
const registerRules = {
  username: [
    { required: true, message: '用户名不能为空', trigger: 'blur' },
    { max: 50, message: '用户名长度不能超过50', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  mobile: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

// 注册方法
const handleRegister = async () => {
  if (loading.value) return

  try {
    await registerFormRef.value.validate()

    loading.value = true

    const res = await request({
      url: '/auth/register',
      method: 'post',
      data: {
        username: registerForm.username,
        password: registerForm.password,
        confirmPassword: registerForm.confirmPassword,
        mobile: registerForm.mobile
      }
    })

    if (res.code === 200) {
      showMessage.success(res.message || '注册成功！')
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    } else {
      showMessage.error(res.message || '注册失败')
    }
  } catch (error) {
    if (error?.message) {
      console.log('表单验证失败')
    } else {
      console.error('注册失败:', error)
    }
  } finally {
    loading.value = false
  }
}

// 返回登录页面
const handleBackToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
@import '@/assets/theme.css';
</style>
