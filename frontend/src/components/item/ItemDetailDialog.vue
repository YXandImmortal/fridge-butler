<template>
  <transition name="dialog-fade">
    <div v-if="visible" class="item-create-dialog-overlay" @click.self="handleOverlayClick">
      <div class="item-create-dialog">
        <div class="dialog-header">
          <div class="dialog-title-container">
            <i class="iconfont dialog-icon" :class="mode === 'edit' ? 'icon-edit-box' : 'icon-item'"/>
            <h3 class="dialog-title">{{ mode === 'edit' ? '编辑物品' : '添加物品' }}</h3>
          </div>
          <i class="iconfont icon-close dialog-close" @click="handleClose"/>
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
              <CustomInput
                  v-model="form.itemName"
                  placeholder="请输入物品名称"
                  :maxlength="50"
                  showWordLimit
              />
            </el-form-item>

            <el-form-item label="存放位置" prop="storageLocation">
              <CustomInput
                  v-model="form.storageLocation"
                  placeholder="例如：冷藏室、冷冻室（选填）"
                  :maxlength="50"
                  showWordLimit
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
              <CustomInputNumber
                  v-model="form.itemNum"
                  :min="0"
                  :precision="2"
                  :step="1"
                  placeholder="请输入数量"
                  width="100%"
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
              <CustomDatePicker
                  v-model="form.productionDate"
                  type="date"
                  placeholder="请选择生产日期"
                  value-format="YYYY-MM-DD"
                  clearable
                  style="width: 100%;"
              />
            </el-form-item>

            <el-form-item label="保质期" prop="shelfLifeDays">
              <div class="shelf-life-container">
                <el-segmented
                    v-model="shelfLifeMode"
                    :options="shelfLifeOptions"
                    size="small"
                    class="mode-switch"
                    @change="handleModeChange"
                />

                <CustomInputNumber
                    v-model="inputValue"
                    :min="1"
                    :max="modeMax"
                    :step="1"
                    placeholder="请输入保质期"
                    width="100%"
                    @change="handleInputChange"
                />

                <div class="shelf-life-hint">
                  约 {{ form.shelfLifeDays || 0 }} 天
                </div>
              </div>
            </el-form-item>

            <el-form-item label="入库时间">
              <CustomDatePicker
                  v-model="form.storedDate"
                  type="date"
                  placeholder="请选择入库时间"
                  value-format="YYYY-MM-DD"
                  style="width: 100%;"
              />
            </el-form-item>

            <el-form-item label="备注" prop="remark">
              <CustomInput
                  v-model="form.remark"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入备注信息（选填）"
                  :maxlength="200"
                  showWordLimit
              />
            </el-form-item>
          </el-form>
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
            {{ mode === 'edit' ? '确认修改' : '确认添加' }}
          </CustomButton>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue'
import CustomButton from '@/components/ui/CustomButton.vue'
import CustomInput from '@/components/ui/CustomInput.vue'
import CustomInputNumber from '@/components/ui/CustomInputNumber.vue'
import CustomSelect from '@/components/ui/CustomSelect.vue'
import CustomDatePicker from '@/components/ui/CustomDatePicker.vue'
import {createItem, updateItem} from '@/api/item'
import showMessage from '@/utils/message'
import notifyGamificationResult from '@/utils/gamificationNotify'
import {applyRecommendToForm} from '@/utils/itemRecommend'

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
  },
  recommendData: {
    type: Object,
    default: () => null
  }
})

const emit = defineEmits(['update:visible', 'success'])

const formRef = ref(null)
const submitting = ref(false)
const shelfLifeMode = ref('day')
const inputValue = ref(null)

const shelfLifeOptions = [
  {label: '按天', value: 'day'},
  {label: '按月', value: 'month'},
  {label: '按年', value: 'year'}
]

const form = reactive({
  itemName: '',
  storageLocation: '',
  categoryId: null,
  itemNum: 1,
  unitTypeId: null,
  itemUnitId: null,
  productionDate: '',
  shelfLifeDays: 1,
  storedDate: '',
  remark: ''
})

const rules = {
  itemName: [
    {required: true, message: '请输入物品名称', trigger: 'blur'},
    {min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur'}
  ],
  categoryId: [
    {required: true, message: '请选择分类', trigger: 'change'}
  ],
  itemNum: [
    {required: true, message: '请输入数量', trigger: 'change'},
    {type: 'number', min: 0, message: '数量不能小于0', trigger: 'change'}
  ],
  unitTypeId: [
    {required: true, message: '请选择单位类型', trigger: 'change'}
  ],
  itemUnitId: [
    {required: true, message: '请选择单位', trigger: 'change'}
  ],
  productionDate: [
    {required: false, message: '请选择生产日期', trigger: 'change'}
  ],
  shelfLifeDays: [
    {required: false, type: 'number', min: 1, message: '保质期至少为1天', trigger: 'change'}
  ],
  storageLocation: [
    {min: 0, max: 50, message: '存放位置最多 50 个字符', trigger: 'blur'}
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

const modeMax = computed(() => {
  switch (shelfLifeMode.value) {
    case 'day':
      return 100
    case 'month':
      return 48
    case 'year':
      return 10
    default:
      return 100
  }
})

const handleUnitTypeChange = () => {
  form.itemUnitId = null
}

const handleInputChange = (val) => {
  if (val === null || val === undefined) {
    form.shelfLifeDays = null
    return
  }
  if (shelfLifeMode.value === 'day') {
    form.shelfLifeDays = val
  } else if (shelfLifeMode.value === 'month') {
    form.shelfLifeDays = val * 30
  } else if (shelfLifeMode.value === 'year') {
    form.shelfLifeDays = val * 365
  }
}

const handleModeChange = () => {
  const days = form.shelfLifeDays
  if (days && days > 0) {
    if (shelfLifeMode.value === 'day') {
      inputValue.value = Math.min(days, 100)
      form.shelfLifeDays = inputValue.value
    } else if (shelfLifeMode.value === 'month') {
      inputValue.value = Math.min(Math.max(Math.round(days / 30), 1), 48)
      form.shelfLifeDays = inputValue.value * 30
    } else if (shelfLifeMode.value === 'year') {
      inputValue.value = Math.min(Math.max(Math.round(days / 365), 1), 10)
      form.shelfLifeDays = inputValue.value * 365
    }
  } else {
    inputValue.value = null
    form.shelfLifeDays = null
  }
}

const initShelfLifeMode = (days) => {
  if (!days || days <= 0) {
    shelfLifeMode.value = 'day'
    inputValue.value = null
    return
  }
  if (days <= 100) {
    shelfLifeMode.value = 'day'
    inputValue.value = days
  } else if (days <= 1440) {
    shelfLifeMode.value = 'month'
    inputValue.value = Math.min(Math.round(days / 30), 48)
  } else {
    shelfLifeMode.value = 'year'
    inputValue.value = Math.min(Math.round(days / 365), 10)
  }
}

const resetForm = () => {
  form.itemName = ''
  form.storageLocation = ''
  form.categoryId = null
  form.itemNum = 1
  form.unitTypeId = null
  form.itemUnitId = null
  form.productionDate = ''
  form.shelfLifeDays = null
  form.storedDate = ''
  form.remark = ''
  shelfLifeMode.value = 'day'
  inputValue.value = null
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const initFormFromItemData = () => {
  if (props.itemData) {
    form.itemName = props.itemData.itemName || ''
    form.storageLocation = props.itemData.storageLocation || ''
    form.categoryId = props.itemData.categoryId || null
    form.itemNum = props.itemData.itemNum || 1
    form.unitTypeId = props.itemData.unitTypeId || null
    form.itemUnitId = props.itemData.itemUnitId || null
    form.productionDate = props.itemData.productionDate || ''
    form.shelfLifeDays = props.itemData.shelfLifeDays || null
    form.storedDate = props.itemData.storedDate || ''
    form.remark = props.itemData.remark || ''
    initShelfLifeMode(props.itemData.shelfLifeDays)
  }
}

const applyRecommendation = () => {
  if (props.mode === 'create' && props.recommendData) {
    applyRecommendToForm(props.recommendData, form, props.unitList)
  }
}

watch(() => props.visible, (val) => {
  if (val) {
    if (props.mode === 'edit' && props.itemData) {
      initFormFromItemData()
    } else {
      resetForm()
      applyRecommendation()
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
    const today = new Date().toISOString().split('T')[0]
    if (props.mode === 'edit') {
      res = await updateItem({
        id: props.itemData.id,
        itemName: form.itemName,
        categoryId: form.categoryId,
        itemNum: form.itemNum,
        itemUnitId: form.itemUnitId,
        storageLocation: form.storageLocation || null,
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
        storageLocation: form.storageLocation || null,
        storedDate: form.storedDate || today,
        productionDate: form.productionDate || null,
        shelfLifeDays: form.shelfLifeDays || null,
        remark: form.remark || null
      })
    }
    if (res.code === 200) {
      showMessage.success(props.mode === 'edit' ? '修改成功' : '添加成功')
      // 添加食材 / 整理冰箱 EXP 与徽章解锁提示
      const description = props.mode === 'edit' ? '整理冰箱' : '添加食材'
      notifyGamificationResult(res, description)
      emit('success', res.data?.itemId)
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding: var(--space-6);
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

.shelf-life-container {
  width: 100%;
}

.mode-switch {
  margin-bottom: 12px;
  display: flex;
  --el-border-radius-base: var(--radius-md);
}

.mode-switch :deep(.el-segmented__item) {
  padding: 2px 12px;
  font-size: 13px;
}

.shelf-life-hint {
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-tertiary);
  text-align: right;
}
</style>
