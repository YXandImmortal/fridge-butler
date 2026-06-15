<template>
  <transition name="dialog-fade">
    <div v-if="visible" class="user-detail-dialog-overlay" @click.self="handleOverlayClick">
      <div class="user-detail-dialog">
        <!-- 头部 -->
        <div class="dialog-header">
          <div class="dialog-title-container">
            <i class="iconfont icon-user dialog-icon"/>
            <h3 class="dialog-title">用户详情</h3>
          </div>
          <i class="iconfont icon-close dialog-close" @click="handleClose"/>
        </div>

        <!-- 内容区 -->
        <div class="dialog-content">
          <div class="detail-grid">
            <div class="detail-row">
              <span class="detail-label">用户ID</span>
              <span class="detail-value">{{ user?.id }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">用户名</span>
              <span class="detail-value">{{ user?.username }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">角色</span>
              <span class="detail-value">{{ user?.roleName }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">账号状态</span>
              <el-tag :type="user?.status === true ? 'success' : 'danger'" size="small" class="detail-tag">
                {{ user?.status === true ? '正常' : '禁用' }}
              </el-tag>
            </div>
            <div class="detail-row">
              <span class="detail-label">激活状态</span>
              <el-tag :type="user?.isActivated === true ? 'success' : 'warning'" size="small" class="detail-tag">
                {{ user?.isActivated === true ? '已激活' : '未激活' }}
              </el-tag>
            </div>
            <div class="detail-row">
              <span class="detail-label">邮箱</span>
              <span class="detail-value">{{ user?.email || '-' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">手机号</span>
              <span class="detail-value">{{ user?.mobile }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">新手引导</span>
              <el-tag :type="user?.guideCompleted === true ? 'success' : 'info'" size="small" class="detail-tag">
                {{ user?.guideCompleted === true ? '已完成' : '未完成' }}
              </el-tag>
            </div>
            <div class="detail-row">
              <span class="detail-label">注册时间</span>
              <span class="detail-value">{{ user?.createTime }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">最后登录</span>
              <span class="detail-value">{{ user?.lastLoginTime || '-' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">密码更新</span>
              <span class="detail-value">{{ user?.passwordUpdatedAt || '-' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">更新时间</span>
              <span class="detail-value">{{ user?.updateTime || '-' }}</span>
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
  user: {
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
</script>

<style scoped lang="scss">
.user-detail-dialog-overlay {
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

.user-detail-dialog {
  background: var(--card-bg);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  max-width: 480px;
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
  .user-detail-dialog {
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
