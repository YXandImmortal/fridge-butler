<template>
  <transition name="dialog-fade">
    <div v-if="visible" class="item-create-dialog-overlay" @click.self="handleOverlayClick">
      <div class="item-create-dialog">
        <div class="dialog-header">
          <div class="dialog-title-container">
            <i class="iconfont dialog-icon" :class="mode === 'edit' ? 'icon-edit' : 'icon-item'" />
            <h3 class="dialog-title">{{ mode === 'edit' ? '编辑物品' : '添加物品' }}</h3>
          </div>
          <i class="iconfont icon-close dialog-close" @click="handleClose" />
        </div>
        <div class="dialog-content">
          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-position="top"
            class="add-form"
          >
            <el-form-item label="物品名称" prop="itemName">
              <EnhancedInput
                v-model="form.itemName"
                placeholder="请输入物品名称"
                maxlength="50"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="分类" prop="categoryId">
              <CustomSelect
                v-model="form.categoryId"
                :options="categoryOptions"
                placeholder="请选择分类"
                clearable
                grid
                full-width
                class="form-select"
              />
            </el-form-item>

            <el-form-item label="数量" prop="itemNum">
              <el-input-number
                v-model="form.itemNum"
                :min="0"
                :precision="2"
                :step="1"
                placeholder="请输入数量"
                class="form-input-number"
              />
            </el-form-item>

            <el-form-item label="单位类型" prop="unitTypeId">
              <CustomSelect
                v-model="form.unitTypeId"
                :options="unitTypeOptions"
                placeholder="请选择单位类型"
                clearable
                grid
                full-width
                class="form-select"
                @change="handleUnitTypeChange"
              />
            </el-form-item>

            <el-form-item label="单位" prop="itemUnitId">
              <CustomSelect
                v-model="form.itemUnitId"
                :options="unitOptions"
                placeholder="请选择单位"
                clearable
                grid
                full-width
                :disabled="!form.unitTypeId"
                class="form-select"
              />
            </el-form-item>

            <el-form-item label="生产日期" prop="productionDate">
              <el-date-picker
                v-model="form.productionDate"
                type="date"
                placeholder="请选择生产日期"
                value-format="YYYY-MM-DD"
                format="YYYY-MM-DD"
                class="form-date-picker"
                clearable
                style="width: 100%"
                :default-value="new Date()"
              />
            </el-form-item>

            <el-form-item label="保质期（天）" prop="shelfLifeDays">
              <el-input-number
                v-model="form.shelfLifeDays"
                :min="1"
                :precision="0"
                :step="1"
                placeholder="请输入保质期天数"
                class="form-input-number"
              />
            </el-form-item>

            <el-form-item v-if="mode === 'edit'" label="入库时间">
              <el-date-picker
                v-model="form.storedDate"
                type="date"
                placeholder="暂无入库时间"
                value-format="YYYY-MM-DD"
                format="YYYY-MM-DD"
                class="form-date-picker"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="备注" prop="remark">
              <el-input
                v-model="form.remark"
                type="textarea"
                :rows="3"
                placeholder="请输入备注信息（选填）"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
          </el-form>
        </div>
        <div class="dialog-footer">
          <CustomButton class="dialog-btn dialog-btn-cancel" @click="handleClose">
            取消
          </CustomButton>
          <CustomButton
            type="primary"
            class="dialog-btn dialog-btn-confirm"
            :loading="submitting"
            @click="handleSubmit"
          >
            {{ mode === 'edit' ? '确认修改' : '确认添加' }}
          </CustomButton>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import CustomButton from '@/components/CustomButton.vue'
import EnhancedInput from '@/components/EnhancedInput.vue'
import CustomSelect from '@/components/CustomSelect.vue'
import { createItem, updateItem } from '@/api/item'
import showMessage from '@/utils/message'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  mode: {
    type: String,
    default: 'create' // 'create' | 'edit'
  },
  itemData: {
    type: Object,
    default: () => null
  },
  categoryList: {
    type: Array,
    default: () => []
  },
  unitList: {
    type: Array,
    default: () => []
  },
  unitTypeList: {
    type: Array,
    default: () => []
  },
  fridgeId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['update:visible', 'success'])

const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  itemName: '',
  categoryId: null,
  itemNum: 1,
  unitTypeId: null,
  itemUnitId: null,
  productionDate: '',
  shelfLifeDays: null,
  storedDate: '',
  remark: ''
})

const rules = {
  itemName: [
    { required: true, message: '请输入物品名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  itemNum: [
    { required: true, message: '请输入数量', trigger: 'change' },
    { type: 'number', min: 0, message: '数量不能小于0', trigger: 'change' }
  ],
  unitTypeId: [
    { required: true, message: '请选择单位类型', trigger: 'change' }
  ],
  itemUnitId: [
    { required: true, message: '请选择单位', trigger: 'change' }
  ],
  productionDate: [
    { required: false, message: '请选择生产日期', trigger: 'change' }
  ],
  shelfLifeDays: [
    { required: false, type: 'number', min: 1, message: '保质期至少为1天', trigger: 'change' }
  ]
}

const categoryOptions = computed(() => {
  return props.categoryList.map(cat => ({
    label: cat.categoryName,
    value: cat.id
  }))
})

const unitTypeOptions = computed(() => {
  return props.unitTypeList.map(ut => ({
    label: ut.unitTypeName,
    value: ut.id
  }))
})

const unitOptions = computed(() => {
  if (!form.unitTypeId) return []
  return props.unitList
    .filter(u => u.unitTypeId === form.unitTypeId)
    .map(unit => ({
      label: unit.unitName,
      value: unit.id
    }))
})

const handleUnitTypeChange = () => {
  form.itemUnitId = null
}

const resetForm = () => {
  form.itemName = ''
  form.categoryId = null
  form.itemNum = 1
  form.unitTypeId = null
  form.itemUnitId = null
  form.productionDate = ''
  form.shelfLifeDays = null
  form.storedDate = ''
  form.remark = ''
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const initFormFromItemData = () => {
  if (props.itemData) {
    form.itemName = props.itemData.itemName || ''
    form.categoryId = props.itemData.categoryId || null
    form.itemNum = props.itemData.itemNum || 1
    form.unitTypeId = props.itemData.unitTypeId || null
    form.itemUnitId = props.itemData.itemUnitId || null
    form.productionDate = props.itemData.productionDate || ''
    form.shelfLifeDays = props.itemData.shelfLifeDays || null
    form.storedDate = props.itemData.storedDate || ''
    form.remark = props.itemData.remark || ''
  }
}

watch(() => props.visible, (val) => {
  if (val) {
    if (props.mode === 'edit' && props.itemData) {
      initFormFromItemData()
    } else {
      resetForm()
    }
  }
})

const handleClose = () => {
  emit('update:visible', false)
}

const handleOverlayClick = () => {
  handleClose()
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    let res
    if (props.mode === 'edit') {
      res = await updateItem({
        id: props.itemData.id,
        itemName: form.itemName,
        categoryId: form.categoryId,
        itemNum: form.itemNum,
        itemUnitId: form.itemUnitId,
        storedDate: form.storedDate || null,
        productionDate: form.productionDate || null,
        shelfLifeDays: form.shelfLifeDays || null,
        remark: form.remark || null
      })
    } else {
      res = await createItem({
        itemName: form.itemName,
        categoryId: form.categoryId,
        itemNum: form.itemNum,
        itemUnitId: form.itemUnitId,
        fridgeId: props.fridgeId,
        storedDate: new Date().toISOString().split('T')[0],
        productionDate: form.productionDate || null,
        shelfLifeDays: form.shelfLifeDays || null,
        remark: form.remark || null
      })
    }
    if (res.code === 200) {
      showMessage.success(props.mode === 'edit' ? '修改成功' : '添加成功')
      emit('success')
      handleClose()
    } else {
      showMessage.error(res.message || (props.mode === 'edit' ? '修改失败' : '添加失败'))
    }
  } catch (error) {
    console.error(props.mode === 'edit' ? '修改物品失败:' : '添加物品失败:', error)
    showMessage.error(props.mode === 'edit' ? '修改失败' : '添加失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.item-create-dialog-overlay {
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

.item-create-dialog {
  background: var(--card-bg);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  max-width: 480px;
  width: 90%;
  max-height: 85vh;
  min-height: 520px;
  display: flex;
  flex-direction: column;
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
  overflow-y: auto;
  flex: 1;
  min-height: 0;
}

.add-form :deep(.el-form-item__label) {
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 13px;
  padding-bottom: 4px;
}

.add-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.add-form :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.form-select {
  width: 100%;
}

.form-input-number {
  width: 100%;
}

.form-input-number :deep(.el-input__wrapper) {
  padding-left: 8px;
  padding-right: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding: var(--space-6);
}

.dialog-btn {
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.dialog-btn-cancel {
  background: var(--gray-20);
  color: var(--text-secondary);
}

.dialog-btn-cancel:hover {
  background: var(--gray-30);
  transform: translateY(-2px);
}

.dialog-btn-confirm {
  background: var(--primary-color);
  color: var(--text-inverse);
}

.dialog-btn-confirm:hover {
  box-shadow: 0 6px 20px var(--primary-40);
  transform: translateY(-2px);
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
  .item-create-dialog {
    min-width: 280px;
    width: 85%;
    min-height: auto;
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
