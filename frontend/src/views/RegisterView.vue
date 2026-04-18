<template>
  <AuthLayout
    title="用户注册"
    :subtitle="`创建您的账户，开启${systemName || '冰箱管理系统'}之旅`"
  >
    <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="auth-form"
        @keyup.enter="handleRegister"
    >
      <el-form-item prop="username">
        <EnhancedInput
            v-model="registerForm.username"
            placeholder="请输入用户名（最多50个字符）"
            :icon="User"
        />
      </el-form-item>

      <el-form-item prop="password">
        <EnhancedInput
            v-model="registerForm.password"
            placeholder="请输入密码"
            :icon="Lock"
            type="password"
            :show-password="true"
        />
      </el-form-item>

      <el-form-item prop="confirmPassword">
        <EnhancedInput
            v-model="registerForm.confirmPassword"
            placeholder="请再次输入密码"
            :icon="Lock"
            type="password"
            :show-password="true"
        />
      </el-form-item>

      <el-form-item prop="mobile">
        <EnhancedInput
            v-model="registerForm.mobile"
            placeholder="请输入手机号码"
            :icon="Phone"
        />
      </el-form-item>

      <AuthButtonGroup
          primary-text="注 册"
          secondary-text="返 回"
          :loading="loading"
          loading-text="注册中..."
          @primary-action="handleRegister"
          @secondary-action="handleBackToLogin"
      />
    </el-form>
  </AuthLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { User, Lock, Phone } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import showMessage from '@/utils/message'
import { useSystemStore } from '@/stores/system'
import AuthLayout from "@/components/auth/AuthLayout.vue";
import AuthButtonGroup from "@/components/auth/AuthButtonGroup.vue";
import EnhancedInput from "@/components/EnhancedInput.vue";

const router = useRouter()
const systemStore = useSystemStore()
const { systemName, getSystemInfo } = systemStore;
const registerFormRef = ref()
const loading = ref(false)

// 初始化系统信息
onMounted(async () => {
  await getSystemInfo()
})

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
      url: '/auth/register/user',
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