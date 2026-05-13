<template>
  <div class="unit-type-create-page">
    <div class="unit-type-create-container">
      <!-- 返回按钮 -->
      <div class="back-bar">
        <CustomButton type="link" @click="handleBack">
          <i class="iconfont icon-arrow-left" />
          返回列表
        </CustomButton>
      </div>

      <div class="create-card">
        <h2 class="create-title">创建物品单位分类</h2>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          class="create-form"
        >
          <el-form-item label="分类名称" prop="typeName">
            <EnhancedInput
              v-model="form.typeName"
              placeholder="请输入单位分类名称，如：重量、容量、数量"
              maxlength="20"
              show-word-limit
              icon="icon-label-alt"
            />
          </el-form-item>
        </el-form>

        <div class="form-actions">
          <CustomButton
            type="primary"
            size="large"
            :loading="submitting"
            @click="handleSubmit"
          >
            {{ submitting ? '创建中...' : '创建分类' }}
          </CustomButton>
          <CustomButton size="large" @click="handleBack" type="danger">取消</CustomButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import showMessage from '@/utils/message'
import { createUnitType } from '@/api/item'
import EnhancedInput from '@/components/EnhancedInput.vue'
import CustomButton from '@/components/CustomButton.vue'

const router = useRouter()

// 表单引用
const formRef = ref(null)

// 提交状态
const submitting = ref(false)

// 表单数据
const form = reactive({
  typeName: ''
})

// 表单校验规则
const rules = {
  typeName: [
    { required: true, message: '请输入单位分类名称', trigger: 'blur' },
    { min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur' }
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
    const res = await createUnitType({
      typeName: form.typeName.trim()
    })

    if (res.code === 200) {
      showMessage.success('创建成功')
      router.push({ name: 'item-unit-type-list' })
    } else {
      showMessage.error(res.message || '创建失败')
    }
  } catch (error) {
    console.error('创建单位分类失败:', error)
    showMessage.error('创建失败')
  } finally {
    submitting.value = false
  }
}

// 返回列表
const handleBack = () => {
  router.push({ name: 'item-unit-type-list' })
}
</script>

<style scoped lang="scss">
.unit-type-create-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100%;
}

.unit-type-create-container {
  max-width: 560px;
  width: 100%;
  animation: fade-in-up 0.6s ease-out;
}

.back-bar {
  margin-bottom: var(--space-5);
}

.back-bar .custom-button {
  font-size: 14px;
  color: var(--text-secondary);
}

.back-bar :deep(.iconfont) {
  margin-right: 4px;
  font-size: 12px;
}

/* 验证错误时的 focus 样式 */
.el-form-item.is-error :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--el-color-danger) inset;
  border-color: var(--el-color-danger);
}

.create-card {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  padding: var(--space-10);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-color);
}

.create-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: var(--space-8);
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
  margin-bottom: var(--space-6);
}

.create-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 15px;
}

.form-actions {
  display: flex;
  gap: var(--space-4);
  justify-content: center;
  padding-top: var(--space-2);
}

.form-actions .custom-button {
  border-radius: 12px;
  padding: 12px var(--space-8);
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
  min-width: 140px;
}

.form-actions .custom-button--primary {
  background: var(--primary-color);
  border: none;
}

.form-actions .custom-button--primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px var(--primary-40);
}

.form-actions .custom-button--danger {
  background: var(--danger-color);
  border: none;
}

.form-actions .custom-button--danger:hover:not(:disabled) {
  box-shadow: 0 6px 20px var(--danger-40);
}

/* 响应式设计 */
@media (max-width: 768px) {
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
  .create-card {
    padding: 24px 16px;
  }

  .create-title {
    font-size: 22px;
  }

  .form-actions {
    flex-direction: column;
  }

  .form-actions .custom-button {
    width: 100%;
  }
}
</style>
