<template>
  <div class="auth-page">
    <div class="auth-wrapper">
      <el-card class="auth-card glass-card">
        <div class="auth-header">
          <div class="icon-container">
            <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path d="M18 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 18H6V4h12v16z"/>
              <path d="M8 6h8v2H8zm0 4h8v2H8zm0 4h5v2H8z"/>
            </svg>
          </div>
          <h2 class="auth-title animated-title">智能冰箱管理系统</h2>
          <p class="auth-subtitle">欢迎回来，请登录您的账户</p>
        </div>

        <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="auth-form"
            @keyup.enter="handleLogin"
        >
          <el-form-item prop="account">
            <el-input
                v-model="loginForm.account"
                placeholder="请输入用户名或手机号"
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
                v-model="loginForm.password"
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

          <el-form-item class="remember-me-item">
            <div class="remember-me-wrapper">
              <el-checkbox v-model="loginForm.rememberMe" class="custom-checkbox">
                下次自动登录
              </el-checkbox>
              <el-tooltip
                  content="勾选后30天内无需输入密码自动登录"
                  placement="top"
                  effect="light"
                  :show-after="200"
              >
                <el-icon class="tips-icon"><QuestionFilled /></el-icon>
              </el-tooltip>
            </div>
          </el-form-item>

          <el-form-item class="button-group">
            <el-button
                type="primary"
                @click="handleLogin"
                class="enhanced-button auth-primary-btn"
                :loading="loading"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
            <el-button
                @click="handleRegister"
                class="enhanced-button auth-secondary-btn"
            >
              注 册
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
    <CopyrightFooter />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { User, Lock , QuestionFilled} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import showMessage from '@/utils/message'
import CopyrightFooter from "@/components/CopyrightFooter.vue";

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref()
const loading = ref(false)

// 登录表单数据
const loginForm = ref({
  account: '',
  password: '',
  rememberMe: false
})

// 表单验证规则
const loginRules = {
  account: [
    { required: true, message: '用户名或手机号不能为空', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' }
  ]
}

// 登录方法
const handleLogin = async () => {
  if (loading.value) return

  try {
    await loginFormRef.value.validate()

    loading.value = true

    const res = await userStore.login(loginForm.value)

    // 校验后端返回的code是否为200
    if (res.code === 200) {
      const message = res.data?.rememberMe
          ? '登录成功！30天内自动登录'
          : (res.message || '登录成功！')
      showMessage.success(message)
      // 现阶段暂时不跳转至其他页面
      // router.push('/')
    } else {
      showMessage.error(res.message || '登录失败')
    }
  } catch (error) {
    if (error?.message) {
      console.log('表单验证失败')
    } else {
      // 网络错误等异常情况
      console.error('登录失败:', error)
    }
  } finally {
    loading.value = false
  }
}

// 注册按钮（功能留空）
const handleRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
@import '@/assets/theme.css';
</style>
