<template>
  <div
    class="email-verify-input-wrapper"
    :class="{ 'is-focused': isFocused, 'is-error': error }"
    @focusin="handleFocusIn"
    @focusout="handleFocusOut"
  >
    <CustomInput
        :model-value="email"
        @update:model-value="handleEmailChange"
        :placeholder="placeholder"
        icon="icon-mail"
        :clearable="true"
        :disabled="sending"
        :error="error"
        size="large"
        class="email-input"
    />
    <CustomButton
        type="primary"
        size="large"
        class="verify-btn"
        :disabled="!isEmailValid || countdown > 0"
        :loading="sending"
        @click="handleSendCaptcha"
    >
      {{ countdown > 0 ? `${countdown}秒` : '验证' }}
    </CustomButton>
  </div>
</template>

<script setup>
import {ref, computed, watch, onUnmounted} from 'vue'
import request from '@/utils/request.js'
import showMessage from '@/utils/message.js'
import CustomInput from '../ui/CustomInput.vue'
import CustomButton from '../ui/CustomButton.vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '请输入邮箱（选填,用于找回密码）'
  },
  captchaType: {
    type: String,
    default: 'REGISTER',
    validator: (val) => ['REGISTER', 'RESET', 'BIND'].includes(val)
  },
  apiUrl: {
    type: String,
    default: '/auth/email/captcha'
  },
  error: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'send'])

const isFocused = ref(false)
const email = ref(props.modelValue || '')
const sending = ref(false)
const countdown = ref(0)
let countdownTimer = null

// 邮箱格式校验
const isEmailValid = computed(() => {
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  return emailRegex.test(email.value)
})

// 同步父组件的 v-model
watch(() => props.modelValue, (newVal) => {
  if (newVal !== email.value) {
    email.value = newVal || ''
  }
})

function handleFocusIn() {
  isFocused.value = true
}

function handleFocusOut(e) {
  // 检查焦点是否真正离开组件（而非在内部 input/button 之间切换）
  if (!e.currentTarget.contains(e.relatedTarget)) {
    isFocused.value = false
  }
}

const handleEmailChange = (val) => {
  email.value = val || ''
  emit('update:modelValue', val)
}

// 发送验证码
const handleSendCaptcha = async () => {
  if (!isEmailValid.value || countdown.value > 0 || sending.value) return

  try {
    sending.value = true
    const payload = props.apiUrl === '/auth/email/captcha'
      ? { email: email.value, type: props.captchaType }
      : { email: email.value }

    const res = await request({
      url: props.apiUrl,
      method: 'post',
      data: payload
    })

    if (res.code === 200) {
      showMessage.success(res.message || '验证码已发送，请查收邮件')
      startCountdown()
      emit('send', email.value)
    } else {
      showMessage.error(res.message || '验证码发送失败')
    }
  } catch (error) {
    // 请求拦截器已统一处理错误提示，此处无需额外处理
    console.error('发送邮箱验证码失败:', error)
  } finally {
    sending.value = false
  }
}

// 启动倒计时
const startCountdown = () => {
  countdown.value = 60
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

// 组件卸载时清除定时器
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})
</script>

<style scoped lang="scss">
/* 外层容器统一管理整个组合控件的边框、阴影和圆角 */
.email-verify-input-wrapper {
  display: flex;
  align-items: center;
  width: 100%;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-input);
  background-color: var(--card-bg);
  overflow: hidden;
  transition: all 0.3s ease;
  max-height: 42px;
}

.email-verify-input-wrapper:hover {
  border-color: var(--border-color);
  box-shadow: var(--shadow-input-hover);
}

/* Focus 时整个组合控件的边框和阴影同步高亮，无断层 */
.email-verify-input-wrapper.is-focused {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-input-focus);
}

/* CustomInput 去掉自身外观，完全融入外层容器 */
.email-verify-input-wrapper :deep(.custom-input) {
  border: none;
  box-shadow: none;
  border-radius: 0;
  background: transparent;
}

/* 按钮去掉 border 和圆角，融入外层容器 */
.verify-btn {
  width: 64px;
  font-size: 13px;
  border: none;
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
  height: 42px;
}

/* ---------- 错误状态 ---------- */
.email-verify-input-wrapper.is-error {
  border-color: var(--el-color-danger);
}

.email-verify-input-wrapper.is-error.is-focused {
  box-shadow: 0 4px 16px rgba(245, 108, 108, 0.3), 0 0 0 3px rgba(245, 108, 108, 0.15);
}

/* 取消 hover 上浮效果 */
.verify-btn:hover:not(:disabled) {
  transform: none;
  box-shadow: none;
}
</style>
