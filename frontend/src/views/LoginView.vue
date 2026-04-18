<template>
  <AuthLayout
    :title="systemName || '冰箱管理系统'"
    subtitle="欢迎回来，请登录您的账户"
  >
    <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="auth-form"
        @keyup.enter="handleLogin"
    >
      <el-form-item prop="account">
        <EnhancedInput
            v-model="loginForm.account"
            placeholder="请输入用户名或手机号"
            :icon="User"
        />
      </el-form-item>

      <el-form-item prop="password">
        <EnhancedInput
            v-model="loginForm.password"
            placeholder="请输入密码"
            :icon="Lock"
            type="password"
            :show-password="true"
        />
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

      <AuthButtonGroup
          primary-text="登 录"
          secondary-text="注 册"
          :loading="loading"
          loading-text="登录中..."
          @primary-action="handleLogin"
          @secondary-action="handleRegister"
      />
    </el-form>
  </AuthLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { User, Lock , QuestionFilled} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useSystemStore } from '@/stores/system'
import showMessage from '@/utils/message'
import AuthLayout from "@/components/auth/AuthLayout.vue";
import AuthButtonGroup from "@/components/auth/AuthButtonGroup.vue";
import EnhancedInput from "@/components/EnhancedInput.vue";

const router = useRouter()
const userStore = useUserStore()
const systemStore = useSystemStore()
const { systemName, getSystemInfo } = systemStore
const loginFormRef = ref()
const loading = ref(false)

// 初始化系统信息
onMounted(async () => {
  await getSystemInfo()
})

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

      await router.push('/user/index')
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