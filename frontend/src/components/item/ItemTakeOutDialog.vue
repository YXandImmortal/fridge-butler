<template>
  <transition name="dialog-fade">
    <div v-if="visible" class="item-takeout-dialog-overlay" @click.self="handleOverlayClick">
      <div class="item-takeout-dialog">
        <div class="dialog-header">
          <div class="dialog-title-container">
            <i class="iconfont icon-arrow-down dialog-icon"/>
            <h3 class="dialog-title">取出物品</h3>
          </div>
          <i class="iconfont icon-close dialog-close" @click="handleClose"/>
        </div>
        <div class="dialog-content">
          <div class="current-stock">
            <span class="stock-label">当前库存</span>
            <span class="stock-value">{{ item?.itemNum }}</span>
            <span class="stock-unit">{{ item?.unitName }}</span>
          </div>

          <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              label-position="top"
              class="takeout-form"
          >
            <el-form-item label="取出数量" prop="takeOutNum">
              <CustomInputNumber
                  v-model="form.takeOutNum"
                  :min="0.01"
                  :max="maxTakeOut"
                  :precision="2"
                  :step="1"
                  placeholder="请输入取出数量"
                  width="100%"
              />
            </el-form-item>
          </el-form>

          <div class="quick-actions">
            <span class="quick-label">快捷选择：</span>
            <CustomButton type="default" size="small" @click="setHalf">
              取出一半
            </CustomButton>
            <CustomButton type="default" size="small" @click="setAll">
              取出全部
            </CustomButton>
          </div>

          <div class="result-preview">
            <span class="result-label">取出后剩余：</span>
            <span class="result-value" :class="{ 'is-zero': remaining <= 0 }">
              {{ remaining > 0 ? remaining.toFixed(2) : 0 }}
            </span>
            <span class="result-unit">{{ item?.unitName }}</span>
          </div>
        </div>
        <div class="dialog-footer">
          <CustomButton @click="handleClose">
            取消
          </CustomButton>
          <CustomButton
              type="primary"
              :loading="submitting"
              @click="handleSubmit"
          >
            确认取出
          </CustomButton>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue'
import CustomButton from '@/components/ui/CustomButton.vue'
import CustomInputNumber from '@/components/ui/CustomInputNumber.vue'
import {takeOutItem} from '@/api/item'
import showMessage from '@/utils/message'
import notifyGamificationResult from '@/utils/gamificationNotify'
import {getFreshnessStatus} from '@/utils/data-analysis'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  item: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:visible', 'success'])

const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  takeOutNum: 1
})

const maxTakeOut = computed(() => {
  return props.item?.itemNum || 0
})

const remaining = computed(() => {
  const current = props.item?.itemNum || 0
  return current - (form.takeOutNum || 0)
})

const rules = {
  takeOutNum: [
    {required: true, message: '请输入取出数量', trigger: 'change'},
    {type: 'number', min: 0.01, message: '取出数量必须大于0', trigger: 'change'}
  ]
}

const resetForm = () => {
  form.takeOutNum = 1
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

watch(() => props.visible, (val) => {
  if (val) {
    resetForm()
    // 默认取出数量为1，但如果库存不足1则取库存值
    const num = props.item?.itemNum || 0
    if (num > 0 && num < 1) {
      form.takeOutNum = num
    }
  }
})

const handleClose = () => {
  emit('update:visible', false)
}

const handleOverlayClick = () => {
  handleClose()
}

const setHalf = () => {
  const num = props.item?.itemNum || 0
  if (num > 0) {
    form.takeOutNum = Math.max(0.01, +(num / 2).toFixed(2))
  }
}

const setAll = () => {
  const num = props.item?.itemNum || 0
  if (num > 0) {
    form.takeOutNum = num
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const currentNum = props.item?.itemNum || 0
  if (form.takeOutNum > currentNum) {
    showMessage.warning('取出数量不能大于当前库存')
    return
  }

  submitting.value = true
  try {
    const res = await takeOutItem({
      id: props.item.id,
      takeOutNum: form.takeOutNum
    })
    if (res.code === 200) {
      showMessage.success('取出成功')
      const status = getFreshnessStatus(props.item)
      const description = status.label === '临期' ? '消耗临期食材' : '整理冰箱'
      notifyGamificationResult(res, description)
      emit('success')
      handleClose()
    } else {
      showMessage.error(res.message || '取出失败')
    }
  } catch (error) {
    console.error('取出物品失败:', error)
    showMessage.error('取出失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.item-takeout-dialog-overlay {
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

.item-takeout-dialog {
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

/* 当前库存 */
.current-stock {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-bottom: var(--space-5);
  padding: var(--space-3) var(--space-4);
  background: var(--primary-10);
  border-radius: var(--radius-md);
}

.stock-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.stock-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--primary-color);
}

.stock-unit {
  font-size: 14px;
  color: var(--text-secondary);
}

/* 表单 */
.takeout-form :deep(.el-form-item__label) {
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 13px;
  padding-bottom: 4px;
}

.takeout-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.form-input-number {
  width: 100%;
}

/* 快捷操作 */
.quick-actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

.quick-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.quick-actions .custom-button {
  padding: 4px 10px;
  font-size: 12px;
}

/* 结果预览 */
.result-preview {
  display: flex;
  align-items: baseline;
  gap: 6px;
  padding: var(--space-3) var(--space-4);
  background: var(--gray-20);
  border-radius: var(--radius-md);
}

.result-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.result-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.result-value.is-zero {
  color: var(--danger-color);
}

.result-unit {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 底部 */
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
  .item-takeout-dialog {
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
}
</style>
