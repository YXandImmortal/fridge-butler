<template>
  <transition name="dialog-fade">
    <div v-if="visible" class="log-detail-dialog-overlay" @click.self="handleOverlayClick">
      <div class="log-detail-dialog">
        <!-- 头部 -->
        <div class="dialog-header">
          <div class="dialog-title-container">
            <i class="iconfont icon-script-text dialog-icon"/>
            <h3 class="dialog-title">请求详情</h3>
          </div>
          <i class="iconfont icon-close dialog-close" @click="handleClose"/>
        </div>

        <!-- 内容区 -->
        <div class="dialog-content">
          <div class="detail-grid">
            <div class="detail-row">
              <span class="detail-label">Trace ID</span>
              <span class="detail-value">{{ log?.traceId || '-' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">用户</span>
              <span class="detail-value">{{ log?.username }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">请求方法</span>
              <el-tag :type="methodTagType(log?.method)" size="small" class="detail-tag">
                {{ log?.method }}
              </el-tag>
            </div>
            <div class="detail-row">
              <span class="detail-label">请求路径</span>
              <span class="detail-value">{{ log?.uri }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">IP 地址</span>
              <span class="detail-value">{{ log?.ip }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">状态码</span>
              <el-tag :type="statusTagType(log?.statusCode)" size="small" class="detail-tag">
                {{ log?.statusCode }}
              </el-tag>
            </div>
            <div class="detail-row">
              <span class="detail-label">耗时</span>
              <span class="detail-value">{{ log?.durationMs != null ? log.durationMs + 'ms' : '-' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">时间</span>
              <span class="detail-value">{{ log?.createTime }}</span>
            </div>
            <div v-if="log?.params" class="detail-row detail-row--block">
              <span class="detail-label">请求参数</span>
              <pre class="detail-code">{{ formatJson(log.params) }}</pre>
            </div>
            <div v-if="log?.errorMsg" class="detail-row detail-row--block">
              <span class="detail-label">错误信息</span>
              <pre class="detail-code detail-code--error">{{ log.errorMsg }}</pre>
            </div>
          </div>
        </div>

        <!-- 底部 -->
        <div class="dialog-footer">
          <CustomButton type="primary" @click="handleClose">关闭</CustomButton>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import CustomButton from '@/components/ui/CustomButton.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  log: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:visible'])

const handleClose = () => {
  emit('update:visible', false)
}

const handleOverlayClick = () => {
  handleClose()
}

const methodTagType = (method) => {
  const map = {
    GET: 'primary',
    POST: 'success',
    PUT: 'warning',
    DELETE: 'danger',
    PATCH: 'info'
  }
  return map[method] || 'info'
}

const statusTagType = (code) => {
  if (code == null) return 'info'
  if (code >= 200 && code < 300) return 'success'
  if (code >= 400 && code < 500) return 'warning'
  if (code >= 500) return 'danger'
  return 'info'
}

const formatJson = (str) => {
  if (!str) return ''
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}
</script>

<style scoped lang="scss">
.log-detail-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--overlay-bg);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.log-detail-dialog {
  background: var(--card-bg);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  max-width: 560px;
  width: 90%;
  animation: dialog-slide-in 0.3s ease-out;
  transition: background-color 0.3s ease, color 0.3s ease,
    border-color 0.3s ease, box-shadow 0.3s ease;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-5) var(--space-6);
}

.dialog-title-container {
  display: flex;
  align-items: center;
  gap: var(--space-2) var(--space-3);
}

.dialog-icon {
  font-size: 24px;
  color: var(--primary-color);
}

.dialog-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.dialog-close {
  font-size: var(--space-5);
  color: var(--text-tertiary);
  cursor: pointer;
  transition: all 0.3s ease;
}

.dialog-close:hover {
  color: var(--primary-color);
  transform: scale(1.1);
}

.dialog-content {
  padding: var(--space-5) var(--space-6);
}

.detail-grid {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.detail-row {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--border-color);

  &:last-child {
    border-bottom: none;
  }
}

.detail-row--block {
  flex-direction: column;
  align-items: flex-start;
  gap: var(--space-2);
}

.detail-label {
  width: 80px;
  font-size: 14px;
  color: var(--text-secondary);
  flex-shrink: 0;
  font-weight: 500;
}

.detail-value {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
  flex: 1;
  word-break: break-all;
}

.detail-tag {
  font-weight: 500;
}

.detail-code {
  width: 100%;
  background: var(--input-bg);
  border-radius: var(--radius-sm);
  padding: var(--space-4);
  font-size: 12px;
  line-height: 1.6;
  color: var(--text-primary);
  overflow-x: auto;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}

.detail-code--error {
  color: var(--danger-color);
  background: var(--danger-light);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding: 0 var(--space-6) var(--space-6);
}

/* 动画 */
@keyframes dialog-slide-in {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: all 0.3s ease;
}

.dialog-fade-enter-from,
.dialog-fade-leave-to {
  opacity: 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .log-detail-dialog {
    min-width: 280px;
    width: 85%;
  }

  .dialog-header {
    padding: var(--space-4) var(--space-5);
  }

  .dialog-icon {
    font-size: 20px;
  }

  .dialog-title {
    font-size: 18px;
  }

  .dialog-content {
    padding: var(--space-4) var(--space-5);
  }

  .dialog-footer {
    padding: 0 var(--space-5) var(--space-5);
  }
}
</style>
