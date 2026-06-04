<template>
  <button
      :type="nativeType"
      :class="buttonClasses"
      :disabled="disabled || loading"
      @click="handleClick"
  >
    <!-- 六边形 Logo 区域 -->
    <span class="logo-button__logo-wrap">
      <Logo class="logo-button__logo"/>
    </span>

    <!-- 按钮内容区域 -->
    <span class="logo-button__content">
      <i v-if="loading" class="iconfont icon-loader logo-button__loading-icon"></i>
      <span class="logo-button__text">
        <slot>{{ loading ? loadingText : '' }}</slot>
      </span>
    </span>
  </button>
</template>

<script setup>
import {computed} from 'vue'
import Logo from './Logo.vue'

const props = defineProps({
  type: {
    type: String,
    default: 'primary',
    validator: (val) => ['default', 'primary', 'danger', 'link'].includes(val)
  },
  size: {
    type: String,
    default: 'default',
    validator: (val) => ['default', 'large', 'small'].includes(val)
  },
  loading: {
    type: Boolean,
    default: false
  },
  loadingText: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  },
  nativeType: {
    type: String,
    default: 'button',
    validator: (val) => ['button', 'submit', 'reset'].includes(val)
  }
})

const emit = defineEmits(['click'])

const buttonClasses = computed(() => {
  const classes = ['logo-button']
  classes.push(`logo-button--${props.type}`)
  if (props.size !== 'default') {
    classes.push(`logo-button--${props.size}`)
  }
  if (props.loading) {
    classes.push('is-loading')
  }
  if (props.disabled) {
    classes.push('is-disabled')
  }
  return classes
})

const handleClick = (event) => {
  if (props.loading || props.disabled) return
  emit('click', event)
}
</script>

<style scoped lang="scss">
/* =========================================================
 * LogoButton — 带品牌六边形 Logo 的按钮
 * 布局：六边形 Logo 位于左侧，与按钮主体形成视觉整体
 * ========================================================= */

.logo-button {
  position: relative;
  display: inline-flex;
  align-items: center;
  border-radius: var(--radius-md);
  padding: 10px 20px 10px 34px; /* 左侧留出 Logo 空间 */
  font-weight: 200;
  font-size: 14px;
  line-height: 1;
  letter-spacing: 0.5px;
  cursor: pointer;
  transition: background-color 0.3s ease, transform 0.3s ease,
  box-shadow 0.3s ease, border-color 0.3s ease,
  filter 0.3s ease, opacity 0.3s ease;
  border: 1px solid transparent;
  outline: none;
  font-family: var(--el-font-family);
  white-space: nowrap;
  user-select: none;

  /* ---- Logo 容器 ---- */
  &__logo-wrap {
    position: absolute;
    left: -18px; /* 向左突出，与按钮重叠 */
    top: 50%;
    transform: translateY(-50%);
    width: 50px;
    height: 50px;
    display: flex;
    align-items: center;
    justify-content: center;
    filter: drop-shadow(0 2px 6px rgba(0, 0, 0, 0.15));
    transition: transform 0.3s ease, filter 0.3s ease;
    z-index: 2;
  }

  &__logo {
    width: 100%;
    height: 100%;
  }

  /* ---- 内容区 ---- */
  &__content {
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }

  &__text {
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }

  &__loading-icon {
    font-size: 1em;
    animation: spin 1.5s linear infinite;
  }
}

/* =========================================================
 * 类型变体
 * ========================================================= */

/* —— 默认样式 —— */
.logo-button--default {
  background: transparent;
  color: var(--text-primary);
  border-color: var(--gray-40);
}

.logo-button--default:hover:not(:disabled) {
  background: var(--primary-light);
  border-color: var(--primary-color);
  color: var(--primary-dark);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px var(--gray-40);
}

.logo-button--default:hover:not(:disabled) .logo-button__logo-wrap {
  transform: translateY(-50%) scale(1.08);
  filter: drop-shadow(0 4px 10px rgba(0, 0, 0, 0.2));
}

/* —— 主要样式（推荐默认） —— */
.logo-button--primary {
  background: var(--primary-color);
  color: var(--text-inverse);
  border: none;
}

.logo-button--primary:hover:not(:disabled) {
  background: var(--primary-dark);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px var(--primary-40);
}

.logo-button--primary:hover:not(:disabled) .logo-button__logo-wrap {
  transform: translateY(-50%) scale(1.08);
  filter: drop-shadow(0 4px 10px rgba(0, 0, 0, 0.25));
}

/* —— 危险样式 —— */
.logo-button--danger {
  background: var(--danger-color);
  color: var(--text-inverse);
  border: none;
}

.logo-button--danger:hover:not(:disabled) {
  filter: brightness(0.95);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px var(--danger-40);
}

.logo-button--danger:hover:not(:disabled) .logo-button__logo-wrap {
  transform: translateY(-50%) scale(1.08);
}

/* —— 链接样式 —— */
.logo-button--link {
  background: transparent;
  color: var(--text-secondary);
  border: none;
  padding: 4px 8px 4px 32px;
}

.logo-button--link:hover:not(:disabled) {
  color: var(--primary-color);
  transform: translateY(-1px);
}

.logo-button--link .logo-button__logo-wrap {
  width: 28px;
  height: 28px;
  left: 0;
}

/* =========================================================
 * 尺寸变体
 * ========================================================= */

/* —— 大号 —— */
.logo-button--large {
  padding: 12px 32px 12px 52px;
  font-size: 16px;

  .logo-button__logo-wrap {
    width: 50px;
    height: 50px;
    left: -6px;
  }
}

/* —— 小号 —— */
.logo-button--small {
  padding: 6px 12px 6px 36px;
  font-size: 12px;
  border-radius: var(--radius-sm);

  .logo-button__logo-wrap {
    width: 34px;
    height: 34px;
    left: -2px;
  }
}

/* =========================================================
 * 状态
 * ========================================================= */

.is-loading {
  cursor: default;
  opacity: 0.85;
}

.is-disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
