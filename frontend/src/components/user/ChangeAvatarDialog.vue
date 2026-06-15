<template>
  <teleport to="body">
    <transition name="dialog-fade">
      <div v-if="visible" class="change-avatar-dialog-overlay" @click.self="handleClose">
        <div class="change-avatar-dialog">
          <div class="dialog-header">
            <div class="dialog-title-container">
              <i class="iconfont icon-user dialog-icon"/>
              <h3 class="dialog-title">选择头像</h3>
            </div>
            <i class="iconfont icon-close dialog-close" @click="handleClose"/>
          </div>
          <div class="dialog-content">
            <div class="avatar-upload-section">
              <Avatar size="x-large" :avatar-id="selectedAvatar"/>
              <div class="avatar-grid">
                <div
                    v-for="avatarId in systemAvatars"
                    :key="avatarId"
                    class="avatar-item"
                    :class="{ 'selected': selectedAvatar === avatarId }"
                    @click="handleSelectAvatar(avatarId)"
                >
                  <Avatar size="large" :avatar-id="avatarId"/>
                </div>
              </div>
            </div>
          </div>
          <div class="dialog-footer">
            <CustomButton @click="handleClose">
              取消
            </CustomButton>
            <CustomButton
                type="primary"
                :loading="loading"
                @click="handleSubmit"
            >
              确认
            </CustomButton>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import {ref, watch} from 'vue'
import {useUserStore} from '@/stores/user'
import showMessage from '@/utils/message'
import {getSystemAvatarIds} from '@/utils/avatarManager'
import CustomButton from '@/components/ui/CustomButton.vue'
import Avatar from '@/components/ui/Avatar.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  currentAvatar: {
    type: String,
    default: 'egg'
  }
})

const emit = defineEmits(['update:visible', 'success'])

const userStore = useUserStore()
const {updateUserAvatar} = userStore

const systemAvatars = ref([])
const selectedAvatar = ref(props.currentAvatar)
const loading = ref(false)

const loadSystemAvatars = () => {
  systemAvatars.value = getSystemAvatarIds()
}

watch(() => props.visible, (val) => {
  if (val) {
    loadSystemAvatars()
    selectedAvatar.value = props.currentAvatar
  }
})

const handleClose = () => {
  emit('update:visible', false)
}

const handleSelectAvatar = (avatarId) => {
  selectedAvatar.value = avatarId
}

const handleSubmit = async () => {
  if (loading.value) return
  if (selectedAvatar.value === props.currentAvatar) {
    handleClose()
    return
  }

  try {
    loading.value = true
    const res = await updateUserAvatar(selectedAvatar.value)

    if (res.code === 200) {
      showMessage.success('头像修改成功')
      emit('success', selectedAvatar.value)
      handleClose()
    } else {
      showMessage.error('头像修改失败: ' + (res.message || '未知错误'))
    }
  } catch (error) {
    showMessage.error('头像修改失败')
    console.error('头像修改失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.change-avatar-dialog-overlay {
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
  z-index: 2000;
}

.change-avatar-dialog {
  background: var(--card-bg);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  max-width: 440px;
  width: 90%;
  animation: dialog-slide-in 0.3s ease-out;
  transition: background-color 0.3s ease, color 0.3s ease,
  border-color 0.3s ease, box-shadow 0.3s ease;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-5) var(--space-6) 0;
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

.avatar-upload-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.avatar-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  width: 100%;
  max-width: 300px;
}

.avatar-item {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 10px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.avatar-item:hover {
  background-color: var(--primary-light);
  transform: scale(1.05);
}

.avatar-item.selected {
  background-color: var(--primary-20);
  border: 2px solid var(--primary-color);
}

.avatar-item :deep(.el-avatar) {
  transition: all 0.3s ease;
}

.avatar-item:hover :deep(.el-avatar) {
  box-shadow: var(--shadow-avatar);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding: 0 var(--space-6) var(--space-6);
}

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

.dialog-fade-enter-from {
  opacity: 0;
}

.dialog-fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .change-avatar-dialog {
    min-width: 280px;
    width: 85%;
  }

  .dialog-header {
    padding: var(--space-4) var(--space-5) 0;
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

  .avatar-grid {
    gap: 16px;
  }
}
</style>
