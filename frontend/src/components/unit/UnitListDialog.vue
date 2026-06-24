<template>
  <transition name="dialog-fade">
    <div v-if="visible" class="unit-expand-dialog-overlay" @click.self="handleOverlayClick">
      <div class="unit-expand-dialog">
        <div class="dialog-header">
          <div class="dialog-title-container">
            <i class="iconfont icon-inbox-all dialog-icon"/>
            <h3 class="dialog-title">{{ unitTypeName }}</h3>
          </div>
          <i class="iconfont icon-close dialog-close" @click="handleClose"/>
        </div>

        <div class="dialog-content">
          <!-- 单位数量统计 -->
          <div class="unit-stats">
            <span class="stats-label">共 {{ unitList.length }} 个单位</span>
            <span v-if="unitType?.isSystemDefault" class="system-hint">
              <i class="iconfont icon-bookmark"/> 系统默认，不可编辑
            </span>
          </div>

          <!-- 添加新单位（仅非系统默认） -->
          <div v-if="!unitType?.isSystemDefault" class="add-unit-section">
            <el-form
                ref="addFormRef"
                :model="addForm"
                :rules="addRules"
                class="add-unit-form"
                @submit.prevent
            >
              <el-form-item prop="unitName" class="add-unit-input">
                <CustomInput
                    v-model="addForm.unitName"
                    placeholder="请输入新单位名称，如：千克、升、个"
                    :maxlength="20"
                    showWordLimit
                    icon="icon-inbox"
                    @keyup.enter="handleAddUnit"
                />
              </el-form-item>
              <CustomButton
                  type="primary"
                  :loading="adding"
                  @click="handleAddUnit"
                  class="add-unit-btn"
              >添加
              </CustomButton>
            </el-form>
          </div>

          <!-- 单位列表 -->
          <div class="unit-list">
            <div v-if="unitList.length === 0" class="empty-units">
              <i class="iconfont icon-empty-box empty-icon"/>
              <p>该分类下暂无单位</p>
              <p v-if="!unitType?.isSystemDefault" class="empty-hint">请在上方添加新单位</p>
            </div>

            <div
                v-for="unit in unitList"
                :key="unit.id"
                class="unit-item"
                :class="{ 'unit-item--editable': !unitType?.isSystemDefault && !unit.isSystemDefault }"
                @click="handleEditUnit(unit)"
            >
              <div class="unit-info">
                <i class="iconfont icon-inbox unit-item-icon"/>
                <span class="unit-name">{{ unit.unitName }}</span>
              </div>
              <div class="unit-actions">
                <i
                    v-if="!unitType?.isSystemDefault && !unit.isSystemDefault"
                    class="iconfont icon-close unit-delete-icon"
                    @click.stop="handleDeleteUnit(unit)"
                />
                <span v-else-if="unit.isSystemDefault" class="system-unit-badge">
                  <i class="iconfont icon-bookmark"/> 系统
                </span>
              </div>
            </div>
          </div>
        </div>

        <div class="dialog-footer">
          <CustomButton @click="handleClose">
            关闭
          </CustomButton>
        </div>
      </div>
    </div>
  </transition>

  <!-- 编辑单位对话框 -->
  <InputDialog
      v-model:visible="showEditDialog"
      title="编辑单位名称"
      label="单位名称"
      placeholder="请输入单位名称"
      icon="icon-inbox"
      value-prop="unitName"
      confirm-text="确认修改"
      :data="selectedUnit"
      :loading="editLoading"
      @submit="handleEditSubmit"
  />

  <!-- 删除单位确认对话框 -->
  <ConfirmDialog
      v-model:visible="showDeleteConfirm"
      title="删除单位"
      :message="`确定要删除单位「${selectedUnit?.unitName || ''}」吗？删除后无法恢复，且会影响使用该单位的物品。`"
      confirm-text="确定删除"
      cancel-text="取消"
      @confirm="confirmDeleteUnit"
      width="420px"
  />
</template>

<script setup>
import {reactive, ref, watch, computed} from 'vue'
import CustomButton from '@/components/ui/CustomButton.vue'
import CustomInput from '@/components/ui/CustomInput.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import {createItemUnit, deleteItemUnit, updateItemUnit} from '@/api/item'
import showMessage from '@/utils/message'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  unitType: {
    type: Object,
    default: () => null
  },
  unitList: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:visible', 'success'])

const addFormRef = ref(null)
const adding = ref(false)
const showDeleteConfirm = ref(false)
const selectedUnit = ref(null)
const showEditDialog = ref(false)
const editLoading = ref(false)

const unitTypeName = computed(() => {
  return props.unitType?.unitTypeName || '单位分类'
})

const addForm = reactive({
  unitName: ''
})

const addRules = {
  unitName: [
    {required: true, message: '请输入单位名称', trigger: 'blur'},
    {min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur'}
  ]
}

const resetAddForm = () => {
  addForm.unitName = ''
  if (addFormRef.value) {
    addFormRef.value.resetFields()
  }
}

watch(() => props.visible, (val) => {
  if (val) {
    resetAddForm()
  }
})

const handleClose = () => {
  emit('update:visible', false)
}

const handleOverlayClick = () => {
  handleClose()
}

// 添加单位
const handleAddUnit = async () => {
  if (adding.value) return
  if (!props.unitType) return

  const valid = await addFormRef.value.validate().catch(() => false)
  if (!valid) return

  adding.value = true
  try {
    const res = await createItemUnit({
      unitName: addForm.unitName.trim(),
      unitTypeId: props.unitType.id
    })
    if (res.code === 200) {
      showMessage.success('添加成功')
      resetAddForm()
      emit('success')
    } else {
      showMessage.error(res.message || '添加失败')
    }
  } catch (error) {
    console.error('添加单位失败:', error)
    showMessage.error('添加失败')
  } finally {
    adding.value = false
  }
}

// 编辑单位
const handleEditUnit = (unit) => {
  if (props.unitType?.isSystemDefault || unit.isSystemDefault) return
  selectedUnit.value = unit
  showEditDialog.value = true
}

// 编辑提交
const handleEditSubmit = async ({id, value}) => {
  editLoading.value = true
  try {
    const res = await updateItemUnit({
      id,
      unitName: value,
      unitTypeId: selectedUnit.value?.unitTypeId || props.unitType?.id
    })
    if (res.code === 200) {
      showMessage.success('修改成功')
      showEditDialog.value = false
      emit('success')
    } else {
      showMessage.error(res.message || '修改失败')
    }
  } catch (error) {
    console.error('修改单位失败:', error)
    showMessage.error('修改失败')
  } finally {
    editLoading.value = false
  }
}

// 删除单位确认
const handleDeleteUnit = (unit) => {
  selectedUnit.value = unit
  showDeleteConfirm.value = true
}

// 确认删除单位
const confirmDeleteUnit = async () => {
  if (!selectedUnit.value) return
  try {
    const res = await deleteItemUnit(selectedUnit.value.id)
    if (res.code === 200) {
      showMessage.success('删除成功')
      emit('success')
    } else {
      showMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    console.error('删除单位失败:', error)
    showMessage.error('删除失败')
  } finally {
    showDeleteConfirm.value = false
    selectedUnit.value = null
  }
}
</script>

<style scoped lang="scss">
.unit-expand-dialog-overlay {
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
  padding: var(--space-4);
}

.unit-expand-dialog {
  background: var(--card-bg);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  max-width: 560px;
  width: 100%;
  max-height: 80vh;
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
  flex-shrink: 0;
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
}

.unit-stats {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-4);
}

.stats-label {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

.system-hint {
  font-size: 12px;
  color: var(--badge-silver-text);
  background: var(--badge-silver-bg);
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  gap: 4px;
}

.system-hint .iconfont {
  font-size: 12px;
  color: var(--badge-silver);
}

/* 添加单位区域 */
.add-unit-section {
  margin-bottom: var(--space-5);
  padding-bottom: var(--space-4);
  border-bottom: 1px dashed var(--gray-40);
}

.add-unit-form {
  display: flex;
  gap: var(--space-3);
  align-items: flex-start;
}

.add-unit-input {
  flex: 1;
  margin-bottom: 0 !important;
}

/* 单位列表 */
.unit-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: var(--space-3);
}

.empty-units {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-8) 0;
  color: var(--text-tertiary);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: var(--space-4);
  color: var(--gray-40);
}

.empty-units p {
  margin: 0;
  font-size: 14px;
}

.empty-hint {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: var(--space-1);
}

.unit-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-4) var(--space-3);
  background: var(--glass-bg);
  border-radius: var(--radius-md);
  border: 1px solid var(--gray-30);
  transition: all 0.3s ease;
  position: relative;
  gap: var(--space-2);
}

.unit-item:hover {
  background: var(--primary-light);
  border-color: var(--primary-20);
  transform: translateY(-2px);
}

.unit-item--editable {
  cursor: pointer;
}

.unit-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  overflow: hidden;
  width: 100%;
}

.unit-item-icon {
  font-size: 24px;
  color: var(--primary-color);
  flex-shrink: 0;
}

.unit-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: center;
  width: 100%;
}

.unit-actions {
  position: absolute;
  top: 4px;
  right: 4px;
}

.unit-delete-icon {
  font-size: 14px;
  color: var(--text-tertiary);
  cursor: pointer;
  padding: 4px;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.unit-delete-icon:hover {
  color: var(--danger-color);
  background: var(--danger-light);
}

.system-unit-badge {
  font-size: 11px;
  color: var(--badge-silver-text);
  background: var(--badge-silver-bg);
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  gap: 4px;
}

.system-unit-badge .iconfont {
  font-size: 10px;
  color: var(--badge-silver);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding: 0 var(--space-6) var(--space-6);
  flex-shrink: 0;
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
  .unit-expand-dialog {
    min-width: 280px;
    width: 95%;
    max-height: 85vh;
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

  .add-unit-form {
    flex-direction: column;
  }

  .unit-item {
    padding: var(--space-3);
  }

  .dialog-footer {
    padding: 0 var(--space-5) var(--space-5);
  }
}
</style>
