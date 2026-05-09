<template>
  <div class="fridge-create-page">
    <div class="fridge-create-container">
      <!-- 返回按钮 -->
      <div class="back-bar">
        <CustomButton type="link" @click="handleBack">
          <i class="iconfont icon-arrow-left" />
          返回列表
        </CustomButton>
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
            <EnhancedInput
              v-model="form.name"
              placeholder="请输入冰箱名称，如：家用冰箱、办公室冰箱"
              maxlength="50"
              show-word-limit
              icon="icon-refrigerator"
            />
          </el-form-item>

          <el-form-item label="冰箱描述" prop="description">
            <EnhancedInput
              v-model="form.description"
              type="textarea"
              :rows="4"
              placeholder="请输入冰箱描述（选填）"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="地址" prop="address">
            <EnhancedInput
              v-model="form.address"
              placeholder="请输入冰箱地址（选填）"
              maxlength="100"
              show-word-limit
              icon="icon-location"
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
            {{ submitting ? '创建中...' : '创建冰箱' }}
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
import { createFridge } from '@/api/fridge'
import EnhancedInput from "@/components/EnhancedInput.vue";
import CustomButton from "@/components/CustomButton.vue";

const router = useRouter()

// 表单引用
const formRef = ref(null)

// 提交状态
const submitting = ref(false)

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
</script>

<style scoped>
.fridge-create-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100%;
}

.fridge-create-container {
  max-width: 600px;
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
