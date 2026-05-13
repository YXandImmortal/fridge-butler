<template>
  <div class="captcha-input-wrapper">
    <EnhancedInput
      :model-value="modelValue"
      @update:model-value="$emit('update:modelValue', $event)"
      :placeholder="placeholder"
      icon="icon-captcha"
      :width="inputWidth"
    />
    <div class="captcha-image-box">
      <img
        :src="captchaUrl"
        alt="验证码"
        class="captcha-image"
        :style="{ height: `${height}px` }"
        @click="refreshCaptcha"
        title="点击刷新验证码"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import EnhancedInput from './EnhancedInput.vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  height: {
    type: Number,
    default: 44
  },
  inputWidth: {
    type: String,
    default: '60%'
  },
  placeholder: {
    type: String,
    default: '请输入验证码'
  }
})

const emit = defineEmits(['update:modelValue'])

// 验证码图片URL
const captchaUrl = ref('/captcha/generate')
// 验证码ID
const captchaId = ref('')

// 刷新验证码
const refreshCaptcha = async () => {
  try {
    const response = await axios({
      url: `/captcha/generate?timestamp=${Date.now()}`,
      method: 'get',
      responseType: 'blob',
      baseURL: import.meta.env.VITE_API_BASE_URL
    })

    captchaId.value = response.headers['x-captcha-id'] || ''
    captchaUrl.value = URL.createObjectURL(response.data)
  } catch (error) {
    console.error('刷新验证码失败:', error)
  }
}

onMounted(() => {
  refreshCaptcha()
})

// 暴露 captchaId 和 refreshCaptcha 供父组件使用
defineExpose({
  captchaId,
  refreshCaptcha
})
</script>

<style scoped lang="scss">
.captcha-input-wrapper {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  width: 100%;
}

.captcha-image-box {
  flex: 1;
  display: flex;
  justify-content: flex-end;
}

.captcha-image {
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: all 0.3s ease;
  box-shadow: var(--shadow-sm);
}

.captcha-image:hover {
  opacity: 0.9;
  transform: scale(1.02);
  box-shadow: 0 4px 12px var(--primary-20);
}
</style>
