<template>
  <div class="admin-system-config-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-header-left">
        <i class="iconfont icon-sliders page-header-icon"/>
        <h1 class="page-title">系统配置</h1>
      </div>
    </div>

    <!-- 配置表单 -->
    <div class="config-card" v-loading="loading">
      <el-form
        ref="formRef"
        :model="configForm"
        label-position="top"
        class="config-form"
      >
        <div class="form-section">
          <h3 class="section-title">
            <i class="iconfont icon-notification section-icon" />
            公告与内容
          </h3>

          <el-form-item label="公告内容">
            <EnhancedInput
              v-model="configForm.announcement"
              type="textarea"
              :rows="3"
              placeholder="请输入系统公告内容，将展示在用户首页"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="系统介绍">
            <EnhancedInput
              v-model="configForm.systemDescription"
              type="textarea"
              :rows="5"
              placeholder="请输入关于页面的系统介绍部分内容"
              maxlength="2000"
              show-word-limit
            />
          </el-form-item>
        </div>

        <el-divider class="config-divider" />

        <div class="form-section">
          <h3 class="section-title">
            <i class="iconfont icon-sliders section-icon" />
            功能开关
          </h3>

          <div class="switch-list">
            <div class="switch-item">
              <div class="switch-info">
                <span class="switch-label">开放注册</span>
                <span class="switch-desc">关闭后新用户将无法注册账号</span>
              </div>
              <el-switch
                v-model="configForm.registerOpen"
                :active-value="true"
                :inactive-value="false"
                active-text="开启"
                inactive-text="关闭"
              />
            </div>

            <div class="switch-item">
              <div class="switch-info">
                <span class="switch-label">AI 智能助手</span>
                <span class="switch-desc">关闭后用户将无法使用 AI 聊天功能</span>
              </div>
              <el-switch
                v-model="configForm.aiChatOpen"
                :active-value="true"
                :inactive-value="false"
                active-text="开启"
                inactive-text="关闭"
              />
            </div>

            <div class="switch-item">
              <div class="switch-info">
                <span class="switch-label">需要激活密钥</span>
                <span class="switch-desc">开启后，新注册的普通用户必须先输入有效的激活密钥才能使用系统功能</span>
              </div>
              <el-switch
                v-model="configForm.requireActivationKey"
                :active-value="true"
                :inactive-value="false"
                active-text="开启"
                inactive-text="关闭"
              />
            </div>
          </div>
        </div>

        <el-divider class="config-divider" />

        <div class="form-section">
          <h3 class="section-title">
            <i class="iconfont icon-contact section-icon" />
            联系信息
          </h3>

          <el-form-item label="管理员邮箱">
            <EnhancedInput
              v-model="configForm.adminEmail"
              placeholder="请输入管理员联系邮箱，用户可在激活页点击复制"
              maxlength="100"
              icon="icon-mail"
            />
          </el-form-item>
        </div>

        <div class="form-footer">
          <CustomButton
            type="primary"
            :loading="saving"
            @click="handleSave"
          >
            <i class="iconfont icon-save" />
            保存配置
          </CustomButton>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import CustomButton from '@/components/ui/CustomButton.vue'
import EnhancedInput from '@/components/ui/EnhancedInput.vue'
import { getSystemConfig, updateSystemConfig } from '@/api/admin.js'
import showMessage from '@/utils/message.js'

// ==================== 状态 ====================
const loading = ref(false)
const saving = ref(false)

const configForm = reactive({
  announcement: '',
  systemDescription: '',
  registerOpen: true,
  aiChatOpen: true,
  requireActivationKey: false,
  adminEmail: ''
})

// ==================== 数据获取 ====================
const fetchConfig = async () => {
  loading.value = true
  try {
    const res = await getSystemConfig()
    if (res.code === 200 && res.data) {
      const data = res.data
      configForm.announcement = data.announcement
      configForm.systemDescription = data.systemDescription
      configForm.registerOpen = data.registerOpen
      configForm.aiChatOpen = data.aiChatOpen
      configForm.requireActivationKey = data.requireActivationKey ?? false
      configForm.adminEmail = data.adminEmail ?? ''
    }
  } catch (error) {
    console.error('获取系统配置失败:', error)
    showMessage.error('获取系统配置失败')
  } finally {
    loading.value = false
  }
}

// ==================== 保存 ====================
const handleSave = async () => {
  saving.value = true
  try {
    // 按需更新：只传有实际值的字段（空字符串也是有效值，只排除 null/undefined）
    const payload = {}
    if (configForm.announcement !== null) payload.announcement = configForm.announcement
    if (configForm.systemDescription !== null) payload.systemDescription = configForm.systemDescription
    // 邮箱格式校验
    if (configForm.adminEmail) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!emailRegex.test(configForm.adminEmail)) {
        showMessage.warning('请输入有效的邮箱地址')
        saving.value = false
        return
      }
    }

    if (configForm.announcement !== null) payload.announcement = configForm.announcement
    if (configForm.systemDescription !== null) payload.systemDescription = configForm.systemDescription
    if (configForm.registerOpen !== null) payload.registerOpen = configForm.registerOpen
    if (configForm.aiChatOpen !== null) payload.aiChatOpen = configForm.aiChatOpen
    if (configForm.requireActivationKey !== null) payload.requireActivationKey = configForm.requireActivationKey
    if (configForm.adminEmail !== null && configForm.adminEmail !== undefined) {
      payload.adminEmail = configForm.adminEmail
    }

    const res = await updateSystemConfig(payload)
    if (res.code === 200) {
      showMessage.success('保存成功')
    } else {
      showMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存系统配置失败:', error)
    showMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchConfig()
})
</script>

<style scoped lang="scss">
.admin-system-config-container {
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
  animation: fade-in-up 0.6s ease-out;
}

.page-header {
  margin-bottom: var(--space-6);
}

.page-header-icon {
  font-size: 28px;
}

.page-header-left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
}

.config-card {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

.form-section {
  margin-bottom: var(--space-4);
}

.section-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-5);
}

.section-icon {
  font-size: 18px;
  color: var(--primary-color);
}

.config-divider {
  margin: var(--space-5) 0;
  border-color: var(--border-color);
}

.switch-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.switch-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4);
  background: var(--input-bg);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
}

.switch-info {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.switch-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.switch-desc {
  font-size: 12px;
  color: var(--text-secondary);
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-6);
  padding-top: var(--space-5);
  border-top: 1px solid var(--border-color);
}

/* 响应式 */
@media (max-width: 768px) {
  .page-title {
    font-size: 22px;
  }

  .config-card {
    padding: var(--space-4);
  }

  .switch-item {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-3);
  }
}

@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
