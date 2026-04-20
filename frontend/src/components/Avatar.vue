<template>
  <div class="avatar-container" :class="[sizeClass, shapeClass]">
    <img
        v-if="avatarSrc"
        :src="avatarSrc"
        :alt="alt"
        class="avatar-image"
        :class="{ 'avatar-image--rounded': shape === 'circle' }"
    />
    <div v-else class="avatar-placeholder">
      {{ placeholderText }}
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

// 定义系统默认头像映射
const SYSTEM_AVATARS = {
  'panda': () => import('@/assets/avatars/panda.svg'),
  'bot': () => import('@/assets/avatars/bot.svg'),
  'core': () => import('@/assets/avatars/core.svg'),
  'fruit': () => import('@/assets/avatars/fruit.svg')
}

// 定义组件属性
const props = defineProps({
  // 头像ID，可以是系统头像ID或自定义URL
  avatarId: {
    type: String,
    default: 'bot'
  },
  // 头像尺寸
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large', 'x-large'].includes(value)
  },
  // 头像形状
  shape: {
    type: String,
    default: 'circle',
    validator: (value) => ['circle', 'square'].includes(value)
  },
  // 替代文本
  alt: {
    type: String,
    default: '用户头像'
  },
  // 占位符文本（当没有头像时显示）
  placeholder: {
    type: String,
    default: null
  }
})

// 计算头像源
const avatarSrc = computed(() => {
  if (!props.avatarId) return null

  // 如果是系统头像ID
  if (SYSTEM_AVATARS[props.avatarId]) {
    // 在实际使用中，这里需要处理动态导入
    // 由于SVG导入的限制，我们使用相对路径
    return `/src/assets/avatars/${props.avatarId}.svg`
  }

  // 如果是URL，直接返回
  return props.avatarId
})

// 计算占位符文本
const placeholderText = computed(() => {
  if (props.placeholder) return props.placeholder
  return props.alt ? props.alt.charAt(0).toUpperCase() : 'U'
})

// 计算尺寸类名
const sizeClass = computed(() => `avatar--${props.size}`)

// 计算形状类名
const shapeClass = computed(() => `avatar--${props.shape}`)
</script>

<style scoped>
.avatar-container {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  overflow: hidden;
  font-family: inherit;
}

/* 尺寸样式 */
.avatar--small {
  width: 32px;
  height: 32px;
  font-size: 14px;
}

.avatar--medium {
  width: 48px;
  height: 48px;
  font-size: 16px;
}

.avatar--large {
  width: 64px;
  height: 64px;
  font-size: 18px;
}

.avatar--x-large {
  width: 96px;
  height: 96px;
  font-size: 24px;
}

/* 形状样式 */
.avatar--circle {
  border-radius: 50%;
}

.avatar--square {
  border-radius: 8px;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-image--rounded {
  border-radius: inherit;
}

.avatar-placeholder {
  color: #666;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}
</style>