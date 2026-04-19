<template>
  <el-form-item class="button-group">
    <el-button
        type="primary"
        @click="primaryAction"
        class="enhanced-button auth-primary-btn"
        :loading="false"
    >
      <template v-if="loading">
        <i class="iconfont icon-loader auth-loading-icon"></i>
        {{ loadingText }}
      </template>
      <template v-else>
        {{ primaryText }}
      </template>
    </el-button>
    <el-button
        @click="secondaryAction"
        class="enhanced-button auth-secondary-btn"
    >
      {{ secondaryText }}
    </el-button>
  </el-form-item>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({
  primaryText: {
    type: String,
    required: true
  },
  secondaryText: {
    type: String,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  loadingText: {
    type: String,
    default: '处理中...'
  }
})

const emit = defineEmits(['primaryAction', 'secondaryAction'])

const primaryAction = () => {
  emit('primaryAction')
}

const secondaryAction = () => {
  emit('secondaryAction')
}
</script>

<style scoped>
@import '@/assets/theme.css';

/* 按钮组 */
.button-group {
    margin-bottom: 0 !important;
    display: flex;
    gap: 12px;
}

/* 主要按钮（登录/注册） */
.auth-primary-btn {
    flex: 1;
    background: radial-gradient(ellipse, var(--primary-color), var(--primary-dark));
    border: none;
}

/* 次要按钮（返回/跳转） */
.auth-secondary-btn {
    flex: 1;
    background: var(--card-bg);
    border: 2px solid var(--primary-color);
    color: var(--primary-color);
}

.auth-secondary-btn:hover {
    background: var(--primary-light);
    border-color: var(--primary-dark);
    color: var(--primary-dark);
}

.auth-loading-icon {
    margin-right: 8px;
    animation: spin 2s linear infinite;
}

@keyframes spin {
    from {
        transform: rotate(0deg);
    }
    to {
        transform: rotate(360deg);
    }
}

/* 响应式设计 */
@media (max-width: 480px) {
    .button-group {
        flex-direction: column;
    }
}
</style>