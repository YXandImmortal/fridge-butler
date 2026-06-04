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
            icon="icon-contact"
        />
      </el-form-item>

      <el-form-item prop="password">
        <EnhancedInput
            v-model="registerForm.password"
            placeholder="请输入密码"
            icon="icon-lock"
            type="password"
            :show-password="true"
        />
      </el-form-item>

      <el-form-item prop="confirmPassword">
        <EnhancedInput
            v-model="registerForm.confirmPassword"
            placeholder="请再次输入密码"
            icon="icon-lock"
            type="password"
            :show-password="true"
        />
      </el-form-item>

      <el-form-item prop="mobile">
        <EnhancedInput
            v-model="registerForm.mobile"
            placeholder="请输入手机号码（选填，可用于登录）"
            icon="icon-device-phone"
        />
      </el-form-item>

      <el-form-item prop="captcha">
        <CaptchaInput
            v-model="registerForm.captcha"
            ref="captchaInputRef"
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
import {ref, reactive, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import request from '@/utils/request.js'
import showMessage from '@/utils/message.js'
import {useSystemStore} from '@/stores/system.js'
import {useUserStore} from '@/stores/user.js'
import AuthLayout from "@/layouts/AuthLayout.vue"
import AuthButtonGroup from "@/components/auth/AuthButtonGroup.vue"
import EnhancedInput from "@/components/ui/EnhancedInput.vue"
import CaptchaInput from "@/components/form/CaptchaInput.vue"

const router = useRouter()
const systemStore = useSystemStore()
const userStore = useUserStore()
const {systemName, getSystemInfo} = systemStore;
const registerFormRef = ref()
const captchaInputRef = ref()
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
  mobile: '',
  captcha: ''
})

// 自定义验证规则：密码
const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 8 || value.length > 20) {
    callback(new Error('密码长度应为8-20位'))
  } else {
    let typeCount = 0
    if (/[A-Z]/.test(value)) typeCount++
    if (/[a-z]/.test(value)) typeCount++
    if (/[0-9]/.test(value)) typeCount++
    if (/[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(value)) typeCount++

    if (typeCount < 2) {
      callback(new Error('密码需包含大写字母、小写字母、数字、特殊符号中的至少两种'))
    } else if (registerForm.username && value.includes(registerForm.username)) {
      callback(new Error('密码不能包含用户名'))
    } else if (registerForm.mobile && value.includes(registerForm.mobile)) {
      callback(new Error('密码不能包含手机号'))
    } else {
      callback()
    }
  }
}

// 自定义验证规则：手机号（选填，填写时校验格式）
const validateMobile = (rule, value, callback) => {
  if (value === '' || value == null) {
    callback()
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

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
    {required: true, message: '用户名不能为空', trigger: 'blur'},
    {max: 50, message: '用户名长度不能超过50', trigger: 'blur'}
  ],
  password: [
    {required: true, validator: validatePassword, trigger: 'blur'}
  ],
  confirmPassword: [
    {required: true, validator: validateConfirmPassword, trigger: 'blur'}
  ],
  mobile: [
    {validator: validateMobile, trigger: 'blur'}
  ],
  captcha: [
    {required: true, message: '验证码不能为空', trigger: 'blur'},
    {min: 4, max: 4, message: '验证码长度为4位', trigger: 'blur'}
  ]
}

// 注册方法
const handleRegister = async () => {
  if (loading.value) return

  try {
    await registerFormRef.value.validate()

    loading.value = true

    // 传递captchaId到注册请求
    const registerData = {
      ...registerForm,
      captchaId: captchaInputRef.value?.captchaId || ''
    }

    const res = await request({
      url: '/auth/register/user',
      method: 'post',
      data: registerData
    })

    if (res.code === 200) {
      showMessage.success(res.message || '注册成功！')

      // 自动登录：后端已返回和登录相同的响应数据
      if (res.data) {
        userStore.saveLoginData(res.data)
      }

      setTimeout(() => {
        // 根据角色跳转到对应首页（与登录页逻辑保持一致）
        if (userStore.roleId === 1) {
          router.push({name: 'super-admin-dashboard'})
        } else {
          router.push({name: 'user-index'})
        }
      }, 1500)
    } else {
      showMessage.error(res.message || '注册失败')
      // 注册失败时刷新验证码
      await captchaInputRef.value?.refreshCaptcha()
    }
  } catch (error) {
    if (error?.message) {
      console.log('表单验证失败')
    } else {
      console.error('注册失败:', error)
      // 异常时刷新验证码
      captchaInputRef.value?.refreshCaptcha()
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

<style scoped lang="scss">
/* 验证错误时的 focus 样式 */
.el-form-item.is-error :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--el-color-danger) inset;
  border-color: var(--el-color-danger);
}
</style>