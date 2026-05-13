<template>
  <el-form-item class="button-group">
    <CustomButton
        type="primary"
        @click="primaryAction"
        class="enhanced-button auth-primary-btn"
        :loading="loading"
        :loading-text="loadingText"
    >
      {{ primaryText }}
    </CustomButton>
    <CustomButton
        @click="secondaryAction"
        class="enhanced-button auth-secondary-btn"
    >
      {{ secondaryText }}
    </CustomButton>
  </el-form-item>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'
import CustomButton from "@/components/CustomButton.vue";

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

<style scoped lang="scss">
/* 按钮组 */
.button-group {
  margin-bottom: 0;
}

.button-group :deep(.el-form-item__content) {
  display: flex;
  gap: var(--space-6);
}

/* 按钮增强样式 */
:deep(button.custom-button.enhanced-button) {
  font-size: 16px;
  border-radius: 12px;
  padding: 14px 24px;
  font-weight: 400;
  letter-spacing: 1px;
  transition: all 0.3s ease;
}

:deep(button.custom-button.enhanced-button:hover:not(:disabled)) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px var(--primary-40);
}

/* 主要按钮（登录/注册） */
:deep(button.custom-button.auth-primary-btn) {
    flex: 1;
    background: radial-gradient(ellipse, var(--primary-color), var(--primary-dark));
    border: none;
}

:deep(button.custom-button.auth-primary-btn:hover:not(:disabled)) {
    background: radial-gradient(ellipse, var(--primary-color), var(--primary-dark));
    filter: brightness(1.05);
}

/* 次要按钮（返回/跳转） */
:deep(button.custom-button.auth-secondary-btn) {
    flex: 1;
    background: var(--card-bg);
    border: 2px solid var(--primary-color);
    color: var(--primary-color);
}

:deep(button.custom-button.auth-secondary-btn:hover:not(:disabled)) {
    background: var(--primary-light);
    border-color: var(--primary-dark);
    color: var(--primary-dark);
}

/* 响应式设计 */
@media (max-width: 480px) {
    .button-group :deep(.el-form-item__content) {
        flex-direction: column;
    }
}
</style>
