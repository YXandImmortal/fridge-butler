<template>
  <transition name="dialog-fade">
    <div v-if="visible" class="batch-takeout-dialog-overlay" @click.self="handleOverlayClick">
      <div class="batch-takeout-dialog">
        <div class="dialog-header">
          <div class="dialog-title-container">
            <i class="iconfont icon-forwardburger dialog-icon"/>
            <h3 class="dialog-title">批量取出</h3>
          </div>
          <i class="iconfont icon-close dialog-close" @click="handleClose"/>
        </div>

        <div class="dialog-content">
          <!-- 步骤指示器（参考 PurchaseCheckInView 风格） -->
          <div class="step-indicator">
            <span class="step" :class="{ active: currentStep === 1 }">1 选择物品</span>
            <span class="step-arrow"><i class="iconfont icon-arrow-right"/></span>
            <span class="step" :class="{ active: currentStep === 2 }">2 确定数量</span>
            <span class="step-arrow"><i class="iconfont icon-arrow-right"/></span>
            <span class="step" :class="{ active: currentStep === 3 }">3 核对确认</span>
          </div>

          <!-- 第 1 步：选择物品 -->
          <template v-if="currentStep === 1">
            <div class="step-toolbar">
              <CustomInput
                  v-model="searchKeyword"
                  placeholder="搜索物品名称"
                  clearable
                  class="search-input"
              >
                <template #prefix>
                  <i class="iconfont icon-search"/>
                </template>
              </CustomInput>
              <span class="selected-count">已选 {{ selectedIds.size }} 件</span>
            </div>

            <div v-if="filteredItems.length === 0" class="empty-state">
              <i class="iconfont icon-empty"/>
              <p>暂无符合条件的物品</p>
            </div>

            <div v-else class="item-card-grid">
              <div
                  v-for="item in filteredItems"
                  :key="item.id"
                  class="item-card"
                  :class="{ selected: selectedIds.has(item.id) }"
                  @click="toggleSelect(item)"
              >
                <div class="card-select-icon">
                  <i class="iconfont icon-check"/>
                </div>
                <div class="card-icon">
                  <i class="iconfont icon-item"/>
                </div>
                <div class="card-name">{{ item.itemName }}</div>
                <div class="card-tags">
                  <el-tag
                      v-if="item.storageLocation"
                      size="small"
                      type="success"
                      :effect="themeStore.theme === 'dark' ? 'dark' : 'light'"
                  >
                    {{ item.storageLocation }}
                  </el-tag>
                  <el-tag
                      v-if="item.categoryName"
                      size="small"
                      type="info"
                      :effect="themeStore.theme === 'dark' ? 'dark' : 'light'"
                  >
                    {{ item.categoryName }}
                  </el-tag>
                </div>
                <div class="card-stock">
                  <span class="stock-label">当前</span>
                  <span class="stock-value">{{ item.itemNum }}</span>
                  <span class="stock-unit">{{ item.unitName }}</span>
                </div>
              </div>
            </div>
          </template>

          <!-- 第 2 步：确定数量 -->
          <template v-if="currentStep === 2">
            <div class="step-toolbar">
              <div class="quick-actions">
                <span class="quick-label">快捷设置：</span>
                <CustomButton type="default" size="small" @click="setAllQuantities('default')">
                  全部取 1
                </CustomButton>
                <CustomButton type="default" size="small" @click="setAllQuantities('half')">
                  全部取一半
                </CustomButton>
                <CustomButton type="default" size="small" @click="setAllQuantities('all')">
                  全部取出
                </CustomButton>
              </div>
              <span class="selected-count">共 {{ selectedItems.length }} 件</span>
            </div>

            <div class="quantity-list">
              <div class="quantity-list-header">
                <span class="col-name">物品名称</span>
                <span class="col-current">当前数量</span>
                <span class="col-takeout">取出数量</span>
                <span class="col-remaining">取出后剩余</span>
              </div>
              <div class="quantity-list-body">
                <div
                    v-for="item in selectedItems"
                    :key="item.id"
                    class="quantity-row"
                    :class="{ 'is-zero': remainingOf(item) <= 0 }"
                >
                  <span class="col-name">{{ item.itemName }}</span>
                  <span class="col-current">
                    <span class="num">{{ item.itemNum }}</span>
                    <span class="unit">{{ item.unitName }}</span>
                  </span>
                  <span class="col-takeout">
                    <CustomInputNumber
                        v-model="quantityMap[item.id]"
                        :min="0.01"
                        :max="item.itemNum"
                        :precision="2"
                        :step="1"
                        placeholder="取出数量"
                        width="140px"
                    />
                    <span class="row-actions">
                      <CustomButton type="link" size="small" @click="setItemQuantity(item, 'half')">
                        一半
                      </CustomButton>
                      <CustomButton type="link" size="small" @click="setItemQuantity(item, 'all')">
                        全部
                      </CustomButton>
                    </span>
                  </span>
                  <span class="col-remaining">
                    <span class="num" :class="{ 'zero-text': remainingOf(item) <= 0 }">
                      {{ remainingOf(item) > 0 ? remainingOf(item).toFixed(2) : '0' }}
                    </span>
                    <span class="unit">{{ item.unitName }}</span>
                  </span>
                </div>
              </div>
            </div>
          </template>

          <!-- 第 3 步：核对确认 -->
          <template v-if="currentStep === 3">
            <div class="review-section">
              <div class="review-title">
                <i class="iconfont icon-info"/>
                请核对以下取出信息
              </div>

              <div class="review-block">
                <div class="review-block-title">取出物品</div>
                <div class="review-card-list">
                  <div
                      v-for="item in selectedItems"
                      :key="item.id"
                      class="review-card"
                  >
                    <div class="review-card-name">{{ item.itemName }}</div>
                    <div class="review-card-meta">
                      <el-tag
                          v-if="item.storageLocation"
                          size="small"
                          type="success"
                          :effect="themeStore.theme === 'dark' ? 'dark' : 'light'"
                      >
                        {{ item.storageLocation }}
                      </el-tag>
                      <el-tag
                          v-if="item.categoryName"
                          size="small"
                          type="info"
                          :effect="themeStore.theme === 'dark' ? 'dark' : 'light'"
                      >
                        {{ item.categoryName }}
                      </el-tag>
                    </div>
                  </div>
                </div>
              </div>

              <div class="review-block">
                <div class="review-block-title">数量核对</div>
                <div class="review-table">
                  <div class="review-table-header">
                    <span class="col-name">物品名称</span>
                    <span class="col-current">当前数量</span>
                    <span class="col-takeout">取出数量</span>
                    <span class="col-remaining">取出后剩余</span>
                  </div>
                  <div
                      v-for="item in selectedItems"
                      :key="item.id"
                      class="review-table-row"
                      :class="{ 'is-zero': remainingOf(item) <= 0 }"
                  >
                    <span class="col-name">{{ item.itemName }}</span>
                    <span class="col-current">
                      <span class="num">{{ item.itemNum }}</span>
                      <span class="unit">{{ item.unitName }}</span>
                    </span>
                    <span class="col-takeout">
                      <span class="num takeout-num">{{ quantityOf(item).toFixed(2) }}</span>
                      <span class="unit">{{ item.unitName }}</span>
                    </span>
                    <span class="col-remaining">
                      <span class="num" :class="{ 'zero-text': remainingOf(item) <= 0 }">
                        {{ remainingOf(item) > 0 ? remainingOf(item).toFixed(2) : '0' }}
                      </span>
                      <span class="unit">{{ item.unitName }}</span>
                    </span>
                  </div>
                </div>
              </div>

              <div v-if="expiringCount > 0" class="review-tip tip-expiring">
                <i class="iconfont icon-warning"/>
                本次包含 {{ expiringCount }} 个临期食材，取出后可获得额外 EXP。
              </div>
              <div v-if="willClearCount > 0" class="review-tip tip-clear">
                <i class="iconfont icon-info-box"/>
                本次将清空 {{ willClearCount }} 个物品的库存。
              </div>
            </div>
          </template>
        </div>

        <div class="dialog-footer">
          <CustomButton @click="handleClose">
            取消
          </CustomButton>
          <template v-if="currentStep === 1">
            <CustomButton
                type="primary"
                :disabled="selectedIds.size === 0"
                @click="goStep2"
            >
              下一步
            </CustomButton>
          </template>
          <template v-if="currentStep === 2">
            <CustomButton @click="goStep1">
              上一步
            </CustomButton>
            <CustomButton
                type="primary"
                :disabled="!isStep2Valid"
                @click="goStep3"
            >
              下一步
            </CustomButton>
          </template>
          <template v-if="currentStep === 3">
            <CustomButton @click="goStep2">
              上一步
            </CustomButton>
            <CustomButton
                type="primary"
                :loading="submitting"
                @click="handleSubmit"
            >
              确认取出
            </CustomButton>
          </template>
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
import {useThemeStore} from '@/stores/theme'
import {batchTakeOutItem} from '@/api/item'
import showMessage from '@/utils/message'
import notifyGamificationResult from '@/utils/gamificationNotify'
import {getFreshnessStatus} from '@/utils/data-analysis'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  itemList: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:visible', 'success'])

const themeStore = useThemeStore()

const currentStep = ref(1)
const searchKeyword = ref('')
const selectedIds = ref(new Set())
const quantityMap = reactive({})
const submitting = ref(false)

const availableItems = computed(() => {
  return props.itemList.filter(item => item.itemNum > 0)
})

const filteredItems = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  if (!keyword) return availableItems.value
  return availableItems.value.filter(item =>
      item.itemName && item.itemName.toLowerCase().includes(keyword)
  )
})

const selectedItems = computed(() => {
  return availableItems.value.filter(item => selectedIds.value.has(item.id))
})

const isStep2Valid = computed(() => {
  return selectedItems.value.every(item => {
    const q = quantityMap[item.id]
    return q != null && q > 0 && q <= item.itemNum
  })
})

const expiringCount = computed(() => {
  return selectedItems.value.filter(item => getFreshnessStatus(item).label === '临期').length
})

const willClearCount = computed(() => {
  return selectedItems.value.filter(item => remainingOf(item) <= 0).length
})

const remainingOf = (item) => {
  const current = Number(item.itemNum) || 0
  const takeOut = Number(quantityMap[item.id]) || 0
  return current - takeOut
}

const quantityOf = (item) => {
  return Number(quantityMap[item.id]) || 0
}

const defaultQuantity = (item) => {
  const num = Number(item.itemNum) || 0
  if (num > 0 && num < 1) return num
  return 1
}

const resetState = () => {
  currentStep.value = 1
  searchKeyword.value = ''
  selectedIds.value = new Set()
  Object.keys(quantityMap).forEach(key => delete quantityMap[key])
}

const initQuantities = () => {
  selectedItems.value.forEach(item => {
    if (!(item.id in quantityMap) || quantityMap[item.id] == null) {
      quantityMap[item.id] = defaultQuantity(item)
    }
  })
}

watch(() => props.visible, (val) => {
  if (val) {
    resetState()
  }
})

const toggleSelect = (item) => {
  const newSet = new Set(selectedIds.value)
  if (newSet.has(item.id)) {
    newSet.delete(item.id)
  } else {
    newSet.add(item.id)
  }
  selectedIds.value = newSet
}

const goStep1 = () => {
  currentStep.value = 1
}

const goStep2 = () => {
  if (selectedIds.value.size === 0) {
    showMessage.warning('请至少选择一个物品')
    return
  }
  initQuantities()
  currentStep.value = 2
}

const goStep3 = () => {
  if (!isStep2Valid.value) {
    showMessage.warning('请检查每个物品的取出数量是否合法')
    return
  }
  currentStep.value = 3
}

const setItemQuantity = (item, type) => {
  const num = Number(item.itemNum) || 0
  if (num <= 0) return
  if (type === 'all') {
    quantityMap[item.id] = num
  } else if (type === 'half') {
    quantityMap[item.id] = Math.max(0.01, +(num / 2).toFixed(2))
  }
}

const setAllQuantities = (type) => {
  selectedItems.value.forEach(item => {
    if (type === 'default') {
      quantityMap[item.id] = defaultQuantity(item)
    } else {
      setItemQuantity(item, type)
    }
  })
}

const buildPayload = () => {
  return selectedItems.value.map(item => ({
    id: item.id,
    takeOutNum: quantityMap[item.id]
  }))
}

const hasExpiringItem = () => {
  return selectedItems.value.some(item => getFreshnessStatus(item).label === '临期')
}

const handleSubmit = async () => {
  if (!isStep2Valid.value) {
    showMessage.warning('请检查取出数量')
    return
  }

  submitting.value = true
  try {
    const payload = buildPayload()
    const res = await batchTakeOutItem({items: payload})
    if (res.code === 200) {
      showMessage.success(`批量取出成功，共 ${payload.length} 件物品`)
      notifyGamificationResult(res, hasExpiringItem() ? '消耗临期食材' : '整理冰箱')
      emit('success')
      handleClose()
    } else {
      showMessage.error(res.message || '批量取出失败')
    }
  } catch (error) {
    console.error('批量取出失败:', error)
    showMessage.error('批量取出失败')
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  emit('update:visible', false)
}

const handleOverlayClick = () => {
  handleClose()
}
</script>

<style scoped lang="scss">
.batch-takeout-dialog-overlay {
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

.batch-takeout-dialog {
  background: var(--card-bg);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  max-width: 720px;
  width: 90%;
  max-height: 85vh;
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
  gap: var(--space-3);
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
  padding: 0 var(--space-6);
  overflow-y: auto;
  flex: 1;
  min-height: 0;
}

/* 步骤指示器 */
.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  padding: var(--space-3) 0 var(--space-5);
  border-bottom: 1px solid var(--gray-40);
  margin-bottom: var(--space-5);
  flex-wrap: wrap;
}

.step {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-tertiary);
  transition: all 0.3s ease;
}

.step.active {
  color: var(--primary-color);
  font-weight: 600;
}

.step-arrow {
  color: var(--text-tertiary);
  font-size: 12px;
}

.step-arrow .iconfont {
  font-size: 12px;
}

/* 工具栏 */
.step-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  min-width: 200px;
}

.search-input :deep(.custom-input) {
  padding-left: 32px;
}

.selected-count {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
  flex-shrink: 0;
}

.quick-actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.quick-label {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-10) 0;
  color: var(--text-tertiary);
}

.empty-state .iconfont {
  font-size: 48px;
  margin-bottom: var(--space-3);
}

.empty-state p {
  font-size: 14px;
}

/* 物品卡片网格 */
.item-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: var(--space-3);
  padding-bottom: var(--space-4);
}

.item-card {
  position: relative;
  background: var(--glass-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: var(--space-3);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 4px;
}

.item-card:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-sm);
}

.item-card.selected {
  border-color: var(--primary-color);
  background: var(--primary-10);
}

.card-select-icon {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  color: transparent;
  transition: all 0.3s ease;
}

.item-card.selected .card-select-icon {
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: var(--text-inverse);
}

.card-select-icon .iconfont {
  font-size: 10px;
}

.card-icon {
  width: 36px;
  height: 36px;
  background: var(--primary-light);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-icon .iconfont {
  font-size: 18px;
  color: var(--primary-color);
}

.card-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 100%;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 4px;
  min-height: 22px;
}

.card-stock {
  display: flex;
  align-items: baseline;
  gap: 3px;
  margin-top: 2px;
}

.stock-label {
  font-size: 11px;
  color: var(--text-secondary);
}

.stock-value {
  font-size: 15px;
  font-weight: 700;
  color: var(--primary-color);
}

.stock-unit {
  font-size: 11px;
  color: var(--text-secondary);
}

/* 数量列表 */
.quantity-list {
  border: 1px solid var(--gray-40);
  border-radius: var(--radius-md);
  overflow: hidden;
  margin-bottom: var(--space-4);
}

.quantity-list-header,
.quantity-row {
  display: grid;
  grid-template-columns: 1.5fr 1fr 1.8fr 1fr;
  align-items: center;
  padding: var(--space-3) var(--space-4);
  gap: var(--space-3);
}

.quantity-list-header {
  background: var(--gray-20);
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.quantity-row {
  border-top: 1px solid var(--gray-40);
  font-size: 14px;
  color: var(--text-primary);
  transition: background 0.3s ease;
}

.quantity-row.is-zero {
  background: var(--danger-light);
}

.quantity-row .col-name {
  font-weight: 500;
}

.quantity-row .col-current,
.quantity-row .col-remaining {
  color: var(--text-secondary);
}

.quantity-row .col-current .num,
.quantity-row .col-remaining .num {
  font-weight: 600;
  color: var(--text-primary);
}

.quantity-row .col-current .unit,
.quantity-row .col-remaining .unit {
  font-size: 12px;
  margin-left: 2px;
}

.quantity-row .col-takeout {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.row-actions {
  display: flex;
  gap: 2px;
}

.row-actions .custom-button {
  padding: 2px 6px;
  font-size: 12px;
}

.zero-text {
  color: var(--danger-color) !important;
}

/* 核对区域 */
.review-section {
  padding-bottom: var(--space-4);
}

.review-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: var(--space-4);
  padding: var(--space-3) var(--space-4);
  background: var(--primary-10);
  border-radius: var(--radius-md);
}

.review-title .iconfont {
  color: var(--primary-color);
}

.review-block {
  margin-bottom: var(--space-5);
}

.review-block-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-3);
}

.review-card-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: var(--space-3);
}

.review-card {
  background: var(--glass-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: var(--space-3);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.review-card-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.review-card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.review-table {
  border: 1px solid var(--gray-40);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.review-table-header,
.review-table-row {
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr 1fr;
  align-items: center;
  padding: var(--space-3) var(--space-4);
  gap: var(--space-3);
}

.review-table-header {
  background: var(--gray-20);
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.review-table-row {
  border-top: 1px solid var(--gray-40);
  font-size: 14px;
  color: var(--text-primary);
}

.review-table-row.is-zero {
  background: var(--danger-light);
}

.review-table-row .num {
  font-weight: 600;
}

.review-table-row .takeout-num {
  color: var(--primary-color);
}

.review-table-row .unit {
  font-size: 12px;
  color: var(--text-secondary);
  margin-left: 2px;
}

.review-tip {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  font-size: 13px;
  margin-bottom: var(--space-3);
}

.review-tip .iconfont {
  font-size: 14px;
}

.tip-expiring {
  background: var(--warning-light);
  color: var(--warning-dark);
}

.tip-clear {
  background: var(--info-light);
  color: var(--info-dark);
}

/* 底部 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-6) var(--space-6);
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

/* 响应式 */
@media (max-width: 768px) {
  .batch-takeout-dialog {
    width: 95%;
    max-height: 90vh;
  }

  .dialog-header {
    padding: var(--space-4) var(--space-5);
  }

  .dialog-content {
    padding: 0 var(--space-5);
  }

  .dialog-footer {
    padding: var(--space-4) var(--space-5) var(--space-5);
  }

  .item-card-grid {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  }

  .quantity-list-header,
  .quantity-row,
  .review-table-header,
  .review-table-row {
    grid-template-columns: 1fr 1fr;
    gap: var(--space-2);
  }

  .quantity-row .col-name,
  .review-table-row .col-name {
    grid-column: 1 / -1;
  }

  .quantity-row .col-takeout {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 480px) {
  .step-indicator {
    gap: var(--space-2);
  }

  .step {
    font-size: 12px;
  }

  .item-card-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: var(--space-2);
  }

  .item-card {
    padding: var(--space-2);
  }

  .card-icon {
    width: 32px;
    height: 32px;
  }

  .card-icon .iconfont {
    font-size: 16px;
  }

  .card-name {
    font-size: 12px;
  }

  .card-tags :deep(.el-tag) {
    padding: 0 4px;
    font-size: 10px;
  }

  .card-stock {
    gap: 2px;
  }

  .stock-value {
    font-size: 13px;
  }

  .stock-label,
  .stock-unit {
    font-size: 10px;
  }
}
</style>
