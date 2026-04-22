<template>
  <div class="index-container">
    <!-- 头部组件 -->
    <Header @show-logout-dialog="showLogoutDialog = true" />

    <!-- 主体内容区域 -->
    <div class="main-content-wrapper">
      <!-- 左侧导航栏 -->
      <Sidebar />

      <!-- 主内容区域 -->
      <main class="main-content">
        <div class="fridge-create-container">
          <!-- 返回按钮 -->
          <div class="back-bar">
            <el-button link @click="handleBack">
              <i class="iconfont icon-arrow-left" />
              返回列表
            </el-button>
          </div>

          <div class="create-card">
            <h2 class="create-title">新建冰箱</h2>

            <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              label-position="top"
              class="create-form"
            >
              <el-form-item label="冰箱名称" prop="name">
                <el-input
                  v-model="form.name"
                  placeholder="请输入冰箱名称，如：家用冰箱、办公室冰箱"
                  maxlength="50"
                  show-word-limit
                >
                  <template #prefix>
                    <i class="iconfont icon-refrigerator" />
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item label="冰箱描述" prop="description">
                <el-input
                  v-model="form.description"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入冰箱描述（选填）"
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="地址" prop="address">
                <el-input
                  v-model="form.address"
                  placeholder="请输入冰箱地址（选填）"
                  maxlength="100"
                  show-word-limit
                >
                  <template #prefix>
                    <i class="iconfont icon-location" />
                  </template>
                </el-input>
              </el-form-item>
            </el-form>

            <div class="form-actions">
              <el-button
                type="primary"
                size="large"
                :loading="submitting"
                @click="handleSubmit"
              >
                {{ submitting ? '创建中...' : '创建冰箱' }}
              </el-button>
              <el-button size="large" @click="handleBack">取消</el-button>
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- 底部版权信息 -->
    <CopyrightFooter />

    <!-- 登出确认对话框 -->
    <ConfirmDialog
      v-model:visible="showLogoutDialog"
      title="退出登录"
      message="您确定要退出登录吗？"
      confirm-text="确定"
      cancel-text="取消"
      @confirm="handleLogout"
    />
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import Header from '@/components/Header.vue'
import Sidebar from '@/components/Sidebar.vue'
import CopyrightFooter from '@/components/CopyrightFooter.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import showMessage from '@/utils/message'
import { useUserStore } from '@/stores/user'
import { createFridge } from '@/api/fridge'

const router = useRouter()
const userStore = useUserStore()
const { logout } = userStore

// 表单引用
const formRef = ref(null)

// 提交状态
const submitting = ref(false)

// 对话框
const showLogoutDialog = ref(false)

// 表单数据
const form = reactive({
  name: '',
  description: '',
  address: ''
})

// 表单校验规则
const rules = {
  name: [
    { required: true, message: '请输入冰箱名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述最多 200 个字符', trigger: 'blur' }
  ],
  address: [
    { max: 100, message: '地址最多 100 个字符', trigger: 'blur' }
  ]
}

// 提交表单
const handleSubmit = async () => {
  if (submitting.value) return

  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }

  submitting.value = true
  try {
    const res = await createFridge({
      fridgeName: form.name.trim(),
      remark: form.description.trim() || undefined,
      fridgeAddress: form.address.trim() || undefined
    })

    if (res.code === 200) {
      showMessage.success('冰箱创建成功')
      await router.push({
        name: 'fridge-detail',
        params: { id: res.data ? res.data : '' }
      })
    } else {
      showMessage.error(res.message || '创建失败')
    }
  } catch (error) {
    console.error('创建冰箱失败:', error)
    showMessage.error('创建失败')
  } finally {
    submitting.value = false
  }
}

// 返回列表
const handleBack = () => {
  router.push('/fridge/list')
}

// 处理退出登录
const handleLogout = () => {
  logout()
  showLogoutDialog.value = false
  router.push('/login')
  showMessage.info('已退出登录')
}
</script>

<style scoped>
.index-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-content-wrapper {
  margin-top: var(--header-height);
}

.main-content {
  margin-left: var(--sidebar-width);
  transition: all 0.3s ease;
  min-height: calc(100vh - var(--header-height) - var(--footer-height));
  background: var(--main-content-bg);
  padding: var(--space-5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.fridge-create-container {
  max-width: 600px;
  width: 100%;
  animation: fade-in-up 0.6s ease-out;
}

.back-bar {
  margin-bottom: 20px;
}

.back-bar :deep(.el-button) {
  font-size: 14px;
  color: var(--text-secondary);
}

.back-bar :deep(.iconfont) {
  margin-right: 4px;
  font-size: 12px;
}

.create-card {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.create-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 32px;
  text-align: center;
  color: var(--text-primary);
}

.create-form {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.create-form :deep(.el-form-item) {
  width: 100%;
  margin-bottom: 24px;
}

.create-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 15px;
}

.create-form :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  padding: var(--space-1) var(--space-4);
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08), inset 0 1px 0 rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.create-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.12), inset 0 1px 0 rgba(255, 255, 255, 0.6);
  border-color: rgba(0, 0, 0, 0.1);
}

.create-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 16px var(--primary-30), 0 0 0 3px rgba(100, 181, 246, 0.15);
  border-color: var(--primary-color);
}

.create-form :deep(.el-textarea__inner) {
  border-radius: var(--radius-md);
  padding: 12px var(--space-4);
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08), inset 0 1px 0 rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  resize: none;
}

.create-form :deep(.el-textarea__inner:hover) {
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.12), inset 0 1px 0 rgba(255, 255, 255, 0.6);
  border-color: rgba(0, 0, 0, 0.1);
}

.create-form :deep(.el-textarea__inner:focus) {
  box-shadow: 0 4px 16px var(--primary-30), 0 0 0 3px rgba(100, 181, 246, 0.15);
  border-color: var(--primary-color);
}

.form-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  padding-top: 8px;
}

.form-actions :deep(.el-button) {
  border-radius: 12px;
  padding: 12px 32px;
  font-weight: 600;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
  min-width: 140px;
}

.form-actions :deep(.el-button--primary) {
  background: var(--primary-color);
  border: none;
}

.form-actions :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px var(--primary-40);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    margin-left: var(--sidebar-width-md);
    padding: var(--space-4);
  }

  .create-card {
    padding: 32px 24px;
  }

  .create-title {
    font-size: 24px;
    margin-bottom: 24px;
  }
}

/* 小屏幕适配 */
@media (max-width: 480px) {
  .main-content {
    margin-left: 0;
    padding: var(--space-3);
  }

  .create-card {
    padding: 24px 16px;
  }

  .create-title {
    font-size: 22px;
  }

  .form-actions {
    flex-direction: column;
  }

  .form-actions :deep(.el-button) {
    width: 100%;
  }
}
</style>
