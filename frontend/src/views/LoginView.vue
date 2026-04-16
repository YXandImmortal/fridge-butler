<template>
  <div class="login-page">
    <div class="login-wrapper">
      <el-card class="login-card glass-card">
        <div class="login-header">
          <div class="icon-container">
            <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path d="M18 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 18H6V4h12v16z"/>
              <path d="M8 6h8v2H8zm0 4h8v2H8zm0 4h5v2H8z"/>
            </svg>
          </div>
          <h2 class="login-title animated-title">智能冰箱管理系统</h2>
          <p class="login-subtitle">欢迎回来，请登录您的账户</p>
        </div>

        <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
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
                class="enhanced-button login-btn"
                :loading="loading"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
            <el-button
                @click="handleRegister"
                class="enhanced-button register-btn"
            >
              注 册
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { User, Lock , QuestionFilled} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import showMessage from '@/utils/message'

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
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-wrapper {
  width: 100%;
  max-width: 480px;
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-card {
  padding: 40px;
  border: none;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  color: #2d3748;
  margin: 20px 0 8px 0;
}

.login-subtitle {
  font-size: 14px;
  color: #718096;
  margin: 0;
}

.login-form {
  margin-top: 10px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.remember-me-item {
  margin-bottom: 24px !important;
}

.remember-me-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.custom-checkbox :deep(.el-checkbox__label) {
  margin-top: -1px;
}

.tips-icon {
  color: var(--primary-color);
  font-size: 16px;
  cursor: help;
  transition: all 0.3s ease;
}

.tips-icon:hover {
  color: var(--primary-dark);
  transform: scale(1.1);
}

.button-group {
  margin-bottom: 0 !important;
  display: flex;
  gap: 12px;
}

.login-btn {
  flex: 1;
  background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
  border: none;
}

.register-btn {
  flex: 1;
  background: #ffffff;
  border: 2px solid var(--primary-color);
  color: var(--primary-color);
}

.register-btn:hover {
  background: var(--primary-light);
  border-color: var(--primary-dark);
  color: var(--primary-dark);
}

@media (max-width: 480px) {
  .login-card {
    padding: 30px 20px;
  }

  .login-title {
    font-size: 24px;
  }

  .button-group {
    flex-direction: column;
  }
}
</style>