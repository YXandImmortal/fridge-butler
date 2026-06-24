<template>
  <div v-loading="loading" class="purchase-plan-preview">
    <div class="cork-board">
      <!-- 软木板标题牌 -->
      <div class="board-title-plate">
        <span class="plate-text">采购计划制定</span>
      </div>

      <!-- 头部纸条区 -->
      <div class="cork-board-header">
        <div class="sticky-note note-plan" :style="{ '--rotate': getNoteRotate('plan') }">
          <span class="note-label">计划名称</span>
          <input
              :value="planName"
              class="note-input"
              type="text"
              placeholder="输入计划名称"
              @input="handlePlanNameInput"
          />
        </div>
        <div class="sticky-note note-fridge" :style="{ '--rotate': getNoteRotate('fridge') }">
          <span class="note-label">所属冰箱</span>
          <span class="note-value">{{ fridgeName || '未选择冰箱' }}</span>
        </div>
        <div class="sticky-note note-date" :style="{ '--rotate': getNoteRotate('date') }">
          <span class="note-label">当前日期</span>
          <span class="note-value">{{ dateText }}</span>
        </div>
      </div>

      <!-- AI 提示便签 -->
      <div
          v-if="tips && tips.length > 0"
          class="ai-tips-note"
          :style="{ '--rotate': getNoteRotate('tips') }"
      >
        <p v-for="(tip, index) in tips" :key="index" class="ai-tip-line">tip：{{ tip }}</p>
      </div>

      <!-- 物品便签区 -->
      <div class="sticky-notes-grid">
        <div
            v-for="(item, index) in items"
            :key="item.id"
            class="item-note"
            :class="{ 'item-note--not-stored': item.storeInFridge === false }"
            :style="{ '--rotate': getNoteRotate(item.id) }"
        >
          <i class="iconfont icon-close item-note-delete" @click="handleDeleteItem(index)"/>
          <div class="item-note-field">
            <label>物品名称</label>
            <input
                :value="item.itemName"
                type="text"
                placeholder="名称"
                @input="updateItemField(index, 'itemName', $event.target.value)"
            />
          </div>
          <div class="item-note-row">
            <div class="item-note-field">
              <label>数量</label>
              <input
                  :value="item.plannedNum"
                  type="text"
                  placeholder="数量"
                  @input="updateItemField(index, 'plannedNum', $event.target.value)"
              />
            </div>
            <div class="item-note-field">
              <label>单位类型</label>
              <StickyNoteSelect
                  :model-value="item.unitTypeId"
                  :options="unitTypeOptions"
                  placeholder="类型"
                  size="small"
                  :seed="`${item.id}-type`"
                  @change="handleUnitTypeChange(index, $event)"
              />
            </div>
          </div>
          <div class="item-note-row">
            <div class="item-note-field">
              <label>单位</label>
              <StickyNoteSelect
                  :model-value="item.itemUnitId"
                  :options="unitOptionsFor(item)"
                  placeholder="单位"
                  size="small"
                  :disabled="!item.unitTypeId"
                  :seed="`${item.id}-unit`"
                  @change="handleUnitChange(index, $event)"
              />
            </div>
            <div class="item-note-field">
              <label>分类</label>
              <StickyNoteSelect
                  :model-value="item.categoryId"
                  :options="categoryOptions"
                  placeholder="分类"
                  size="small"
                  :seed="`${item.id}-category`"
                  @change="handleCategoryChange(index, $event)"
              />
            </div>
          </div>
          <p v-if="item.reason" class="item-note-reason">{{ item.reason }}</p>
          <div class="item-note-footer">
            <span
                class="storage-toggle"
                :class="{ 'storage-toggle--not-stored': !item.storeInFridge }"
                @click="toggleStoreInFridge(index)"
            >
              {{ item.storeInFridge ? '存入冰箱' : '不入冰箱' }}
            </span>
          </div>
        </div>

        <!-- 添加物品便签 -->
        <div class="item-note item-note-add" @click="handleAddItem">
          <i class="iconfont icon-add-box"/>
          <span>添加物品</span>
        </div>
      </div>

      <!-- 操作便签区 -->
      <div class="cork-board-actions">
        <button
            type="button"
            class="action-note action-note--default"
            :style="{ '--rotate': getNoteRotate('return') }"
            @click="handleReturn"
        >
          返回
        </button>
        <button
            type="button"
            class="action-note action-note--success"
            :style="{ '--rotate': getNoteRotate('template') }"
            @click="handleSaveTemplate"
        >
          保存为模板
        </button>
        <button
            type="button"
            class="action-note action-note--primary"
            :style="{ '--rotate': getNoteRotate('confirm') }"
            @click="handleConfirm"
        >
          确定计划
        </button>
      </div>
    </div>

    <!-- 我的模板软木板 -->
    <div v-if="templateList.length > 0" v-loading="templatesLoading" class="cork-board template-board">
      <div class="board-title-plate plate-secondary">
        <span class="plate-text">我的模板</span>
      </div>

      <div class="template-notes-grid">
        <div
            v-for="template in templateList"
            :key="template.id"
            class="template-note"
            :style="{ '--rotate': getNoteRotate(template.id) }"
            @click="handleUseTemplate(template)"
        >
          <i class="iconfont icon-close template-note-delete" @click.stop="handleDeleteTemplate(template, $event)"/>
          <h4 class="template-note-name">{{ template.templateName }}</h4>
          <p class="template-note-desc">{{ template.sceneDesc || '暂无描述' }}</p>
          <p class="template-note-hint">点击追加到当前计划</p>
          <span class="template-note-meta">{{ template.itemCount ?? template.items?.length ?? 0 }} 件物品</span>
        </div>
      </div>
    </div>

    <!-- 删除模板确认 -->
    <ConfirmDialog
        v-model:visible="showDeleteTemplateDialog"
        title="删除模板"
        :message="`确定要删除模板「${deletingTemplate?.templateName || ''}」吗？删除后不可恢复。`"
        confirm-text="删除"
        cancel-text="取消"
        @confirm="confirmDeleteTemplate"
        width="400px"
    />

    <!-- 返回确认 -->
    <ConfirmDialog
        v-model:visible="showReturnConfirmDialog"
        title="确认返回"
        message="返回后会失去当前所有修改，是否继续？"
        confirm-text="确定返回"
        cancel-text="取消"
        @confirm="confirmReturn"
        width="400px"
    />

    <InputDialog
        v-model:visible="showSaveTemplateDialog"
        title="保存为模板"
        label="模板名称"
        placeholder="请输入模板名称"
        icon="icon-edit"
        :maxlength="30"
        show-second-field
        second-label="场景描述"
        second-placeholder="请输入场景描述，如周末烧烤聚会"
        second-icon="icon-edit"
        :second-maxlength="100"
        :second-rows="3"
        value-prop="name"
        second-value-prop="desc"
        :data="saveTemplateFormData"
        confirm-text="保存"
        :loading="saveTemplateLoading"
        @submit="confirmSaveTemplate"
    />
  </div>
</template>

<script setup>
import {computed, onMounted, ref, watch} from 'vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import InputDialog from '@/components/ui/InputDialog.vue'
import StickyNoteSelect from '@/components/ui/StickyNoteSelect.vue'
import {createPurchaseTemplate, listMyTemplates, getTemplateDetail, deletePurchaseTemplate} from '@/api/purchase.js'
import {listItemCategories, listItemUnits, listUnitTypes} from '@/api/item.js'
import showMessage from '@/utils/message.js'

const props = defineProps({
  loading: {
    type: Boolean,
    default: false
  },
  planName: {
    type: String,
    default: ''
  },
  items: {
    type: Array,
    default: () => []
  },
  fridgeName: {
    type: String,
    default: ''
  },
  dateText: {
    type: String,
    default: ''
  },
  source: {
    type: String,
    default: ''
  },
  sceneDesc: {
    type: String,
    default: ''
  },
  tips: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:planName', 'update:items', 'return', 'confirm', 'template-deleted'])

const showReturnConfirmDialog = ref(false)
const showSaveTemplateDialog = ref(false)
const saveTemplateLoading = ref(false)
const showDeleteTemplateDialog = ref(false)
const deletingTemplate = ref(null)

const categoryList = ref([])
const unitTypeList = ref([])
const unitList = ref([])
const baseDataLoading = ref(false)

const templateList = ref([])
const templatesLoading = ref(false)

const categoryOptions = computed(() => categoryList.value.map(c => ({
  label: c.categoryName,
  value: c.id
})))

const unitTypeOptions = computed(() => unitTypeList.value.map(t => ({
  label: t.unitTypeName,
  value: t.id
})))

const unitOptionsFor = (item) => {
  if (!item.unitTypeId) return []
  return unitList.value
      .filter(u => u.unitTypeId === item.unitTypeId)
      .map(u => ({label: u.unitName, value: u.id}))
}

const saveTemplateFormData = computed(() => ({
  name: props.planName,
  desc: props.sceneDesc
}))

const handlePlanNameInput = (event) => {
  emit('update:planName', event.target.value)
}

const stripTrailingZeros = (value) => {
  if (value === null || value === undefined || value === '') return '1'
  const num = Number(value)
  if (Number.isNaN(num)) return '1'
  return String(num)
}

const createEmptyItem = () => ({
  id: `manual-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`,
  itemName: '',
  plannedNum: '1',
  unitName: '',
  categoryName: '',
  reason: '',
  categoryId: null,
  itemUnitId: null,
  unitTypeId: null,
  storeInFridge: true
})

const handleAddItem = () => {
  emit('update:items', [...props.items, createEmptyItem()])
}

const handleDeleteItem = (index) => {
  const newItems = [...props.items]
  newItems.splice(index, 1)
  emit('update:items', newItems)
}

const updateItemField = (index, field, value) => {
  const newItems = [...props.items]
  newItems[index] = {...newItems[index], [field]: value}
  emit('update:items', newItems)
}

const handleCategoryChange = (index, value) => {
  const option = categoryOptions.value.find(o => o.value === value)
  const newItems = [...props.items]
  newItems[index] = {
    ...newItems[index],
    categoryId: value,
    categoryName: option?.label || ''
  }
  emit('update:items', newItems)
}

const handleUnitTypeChange = (index, value) => {
  const newItems = [...props.items]
  newItems[index] = {
    ...newItems[index],
    unitTypeId: value,
    itemUnitId: null,
    unitName: ''
  }
  emit('update:items', newItems)
}

const handleUnitChange = (index, value) => {
  const option = unitOptionsFor(props.items[index]).find(o => o.value === value)
  const newItems = [...props.items]
  newItems[index] = {
    ...newItems[index],
    itemUnitId: value,
    unitName: option?.label || ''
  }
  emit('update:items', newItems)
}

const toggleStoreInFridge = (index) => {
  const newItems = [...props.items]
  newItems[index] = {
    ...newItems[index],
    storeInFridge: !newItems[index].storeInFridge
  }
  emit('update:items', newItems)
}

const patchItemBaseInfo = () => {
  if (!props.items.length) return
  let changed = false
  const newItems = props.items.map(item => {
    const updates = {}
    if (item.itemUnitId && !item.unitTypeId) {
      const unit = unitList.value.find(u => u.id === item.itemUnitId)
      if (unit) {
        updates.unitTypeId = unit.unitTypeId
      }
    }
    if (item.itemUnitId && !item.unitName) {
      const unit = unitList.value.find(u => u.id === item.itemUnitId)
      if (unit) updates.unitName = unit.unitName
    }
    if (item.categoryId && !item.categoryName) {
      const cat = categoryList.value.find(c => c.id === item.categoryId)
      if (cat) updates.categoryName = cat.categoryName
    }
    if (Object.keys(updates).length) {
      changed = true
      return {...item, ...updates}
    }
    return item
  })
  if (changed) {
    emit('update:items', newItems)
  }
}

const loadBaseData = async () => {
  baseDataLoading.value = true
  try {
    const [catRes, unitRes, typeRes] = await Promise.all([
      listItemCategories().catch(() => ({code: -1, data: []})),
      listItemUnits().catch(() => ({code: -1, data: []})),
      listUnitTypes().catch(() => ({code: -1, data: []}))
    ])
    if (catRes.code === 200 && Array.isArray(catRes.data)) {
      categoryList.value = catRes.data
    }
    if (unitRes.code === 200 && Array.isArray(unitRes.data)) {
      unitList.value = unitRes.data
    }
    if (typeRes.code === 200 && Array.isArray(typeRes.data)) {
      unitTypeList.value = typeRes.data
    }
    patchItemBaseInfo()
  } catch (error) {
    console.error('加载基础数据失败:', error)
    showMessage.error('加载分类或单位数据失败')
  } finally {
    baseDataLoading.value = false
  }
}

onMounted(() => {
  loadBaseData()
  fetchTemplates()
})

watch(() => props.items, () => {
  if (categoryList.value.length || unitList.value.length || unitTypeList.value.length) {
    patchItemBaseInfo()
  }
}, {deep: true})

const handleReturn = () => {
  showReturnConfirmDialog.value = true
}

const confirmReturn = () => {
  emit('return')
}

const handleConfirm = () => {
  emit('confirm')
}

const getValidItems = () => props.items.filter(
    item => item.itemName?.trim() && Number(item.plannedNum) > 0
)

const mapTemplateItems = (items) => items.map(item => ({
  itemName: item.itemName.trim(),
  categoryId: item.categoryId,
  plannedNum: String(item.plannedNum),
  itemUnitId: item.itemUnitId,
  storeInFridge: item.storeInFridge ?? true
}))

const handleDeleteTemplate = (template, event) => {
  event.stopPropagation()
  deletingTemplate.value = template
  showDeleteTemplateDialog.value = true
}

const confirmDeleteTemplate = async () => {
  if (!deletingTemplate.value) return
  try {
    const res = await deletePurchaseTemplate(deletingTemplate.value.id)
    if (res.code === 200) {
      showMessage.success('删除模板成功')
      templateList.value = templateList.value.filter(t => t.id !== deletingTemplate.value.id)
      emit('template-deleted', deletingTemplate.value.id)
      deletingTemplate.value = null
      showDeleteTemplateDialog.value = false
    } else {
      showMessage.error(res.message || '删除模板失败')
    }
  } catch (error) {
    console.error('删除模板失败:', error)
    showMessage.error('删除模板失败')
  }
}

const handleSaveTemplate = () => {
  if (props.items.length === 0) {
    showMessage.warning('空计划不能保存为模板')
    return
  }
  showSaveTemplateDialog.value = true
}

const fetchTemplates = async () => {
  templatesLoading.value = true
  try {
    const res = await listMyTemplates()
    if (res.code === 200 && Array.isArray(res.data)) {
      templateList.value = res.data
    }
  } catch (error) {
    console.error('加载模板列表失败:', error)
  } finally {
    templatesLoading.value = false
  }
}

const handleUseTemplate = async (template) => {
  try {
    const res = await getTemplateDetail(template.id)
    if (res.code !== 200 || !res.data?.items?.length) {
      showMessage.warning('该模板暂无物品')
      return
    }

    const templateItems = res.data.items.map(it => {
      const unit = it.itemUnitId ? unitList.value.find(u => u.id === it.itemUnitId) : null
      return {
        itemName: it.itemName,
        plannedNum: stripTrailingZeros(it.plannedNum),
        unitName: unit?.unitName || it.itemUnitName || '',
        categoryName: it.categoryName || '',
        reason: '',
        categoryId: it.categoryId ?? null,
        itemUnitId: it.itemUnitId ?? null,
        unitTypeId: unit?.unitTypeId ?? null,
        storeInFridge: it.storeInFridge ?? true
      }
    })

    const existingItems = [...props.items]
    let addedCount = 0
    let mergedCount = 0

    templateItems.forEach((tplItem, idx) => {
      const matchIndex = existingItems.findIndex(item =>
          item.itemName?.trim() === tplItem.itemName?.trim() &&
          item.categoryId === tplItem.categoryId &&
          item.unitTypeId === tplItem.unitTypeId &&
          item.itemUnitId === tplItem.itemUnitId
      )

      if (matchIndex >= 0) {
        const existing = existingItems[matchIndex]
        const sum = Number(existing.plannedNum) + Number(tplItem.plannedNum)
        existingItems[matchIndex] = {
          ...existing,
          plannedNum: stripTrailingZeros(sum)
        }
        mergedCount++
      } else {
        existingItems.push({
          ...tplItem,
          id: `tpl-${template.id}-${idx}-${Date.now()}`
        })
        addedCount++
      }
    })

    emit('update:items', existingItems)

    if (mergedCount > 0 && addedCount > 0) {
      showMessage.success(`已合并 ${mergedCount} 件相同物品，新增 ${addedCount} 件物品`)
    } else if (mergedCount > 0) {
      showMessage.success(`已合并 ${mergedCount} 件相同物品`)
    } else {
      showMessage.success(`已追加 ${addedCount} 件模板物品`)
    }
  } catch (error) {
    console.error('获取模板详情失败:', error)
    showMessage.error('获取模板详情失败')
  }
}

const confirmSaveTemplate = async ({value, secondValue}) => {
  const validItems = getValidItems()
  if (validItems.length === 0) {
    showMessage.warning('请至少添加一件有效物品')
    return
  }

  saveTemplateLoading.value = true
  try {
    const res = await createPurchaseTemplate({
      templateName: value.trim(),
      sceneDesc: secondValue.trim(),
      items: mapTemplateItems(validItems)
    })
    if (res.code === 200) {
      showMessage.success('保存模板成功')
      showSaveTemplateDialog.value = false
      await fetchTemplates()
    } else {
      showMessage.error(res.message || '保存模板失败')
    }
  } catch (error) {
    showMessage.error('保存模板失败')
  } finally {
    saveTemplateLoading.value = false
  }
}

const getNoteRotate = (seed) => {
  const str = String(seed)
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    hash = ((hash << 5) - hash) + str.charCodeAt(i)
    hash |= 0
  }
  const deg = (Math.abs(hash) % 5) - 2
  return `${deg}deg`
}
</script>

<style scoped lang="scss">
/* =========================================================
 * 采购计划预览区（原木风软木板）
 * ========================================================= */
.purchase-plan-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  max-width: 1100px;
  margin: var(--space-6) auto 0;
  padding: var(--space-6) 0;
}

.cork-board {
  position: relative;
  width: 100%;
  background: var(--cork-board-bg);
  border-radius: var(--radius-xl);
  padding: var(--space-8);
  box-shadow:
      inset 0 0 40px rgba(0, 0, 0, 0.12),
      0 12px 0 var(--cork-board-shadow),
      0 24px 50px rgba(0, 0, 0, 0.22);
  overflow: visible;
}

.cork-board::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image:
      radial-gradient(circle at 20% 25%, var(--cork-board-texture-dark) 0, transparent 7px),
      radial-gradient(circle at 60% 55%, var(--cork-board-texture-dark) 0, transparent 6px),
      radial-gradient(circle at 35% 80%, var(--cork-board-texture-light) 0, transparent 6px);
  background-size: 70px 70px, 55px 55px, 65px 65px;
  pointer-events: none;
}

/* 软木板标题牌 */
.board-title-plate {
  position: absolute;
  top: -18px;
  left: 28px;
  z-index: 2;
  padding: 12px 32px;
  background:
      repeating-linear-gradient(
          0deg,
          transparent 0px,
          transparent 2px,
          var(--cork-plate-texture) 2px,
          var(--cork-plate-texture) 4px,
          transparent 4px,
          transparent 7px,
          var(--cork-plate-texture-light) 7px,
          var(--cork-plate-texture-light) 8px
      ),
      var(--cork-plate-bg);
  border-radius: 3px;
  box-shadow:
      inset 0 1px 0 rgba(255, 255, 255, 0.2),
      inset 0 -2px 0 rgba(0, 0, 0, 0.12),
      2px 4px 10px rgba(0, 0, 0, 0.25);
  transform: rotate(-1.5deg);
}

.board-title-plate .plate-text {
  font-size: 16px;
  font-weight: 700;
  color: var(--cork-plate-text);
  letter-spacing: 1px;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.45);
}

.board-title-plate::before,
.board-title-plate::after {
  content: '';
  position: absolute;
  top: 5px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--cork-plate-pin);
  box-shadow: 0 2px 3px rgba(0, 0, 0, 0.35);
}

.board-title-plate::before {
  left: 10px;
}

.board-title-plate::after {
  right: 10px;
}

/* 头部纸条区 */
.cork-board-header {
  position: relative;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: var(--space-6);
  margin-bottom: var(--space-8);
  z-index: 1;
}

.sticky-note {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  min-width: 160px;
  max-width: 260px;
  padding: var(--space-4) var(--space-5);
  background: var(--cork-note-yellow);
  border-radius: 2px;
  box-shadow: 2px 3px 8px rgba(0, 0, 0, 0.18);
  transform: rotate(var(--rotate, 0deg));
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.sticky-note:hover {
  transform: rotate(0deg) translateY(-2px);
  box-shadow: 3px 5px 12px rgba(0, 0, 0, 0.22);
}

.note-label {
  font-size: 12px;
  color: var(--cork-note-label);
  font-weight: 500;
}

.note-value {
  font-size: 15px;
  color: var(--cork-note-text);
  font-weight: 600;
  word-break: break-all;
}

.note-input {
  font-size: 15px;
  font-weight: 600;
  color: var(--cork-note-text);
  background: transparent;
  border: none;
  border-bottom: 1px dashed var(--cork-note-input-border);
  padding: 2px 0;
  outline: none;
  width: 100%;
}

.note-input:focus {
  border-bottom-color: var(--cork-note-label);
}

/* AI 提示便签 */
.ai-tips-note {
  position: relative;
  width: 100%;
  margin-bottom: var(--space-6);
  padding: var(--space-4) var(--space-5);
  background: var(--cork-note-green);
  border-radius: 2px;
  box-shadow: 2px 3px 8px rgba(0, 0, 0, 0.18);
  transform: rotate(var(--rotate, 0deg));
  color: var(--cork-note-text);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.ai-tips-note:hover {
  transform: rotate(0deg) translateY(-2px);
  box-shadow: 3px 5px 12px rgba(0, 0, 0, 0.22);
}

.ai-tip-line {
  margin: 0;
  font-size: 14px;
  line-height: 1.8;
  word-break: break-all;
}

.ai-tip-line + .ai-tip-line {
  margin-top: var(--space-1);
}

/* 物品便签区 */
.sticky-notes-grid {
  position: relative;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: var(--space-5);
  z-index: 2;
}

.item-note {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  padding: var(--space-4);
  background: var(--cork-note-yellow);
  border-radius: 2px;
  box-shadow: 2px 3px 8px rgba(0, 0, 0, 0.18);
  transform: rotate(var(--rotate, 0deg));
  transition: transform 0.3s ease, box-shadow 0.3s ease, background-color 0.3s ease;
  z-index: 1;
}

.item-note:has(.sticky-note-select.is-open) {
  z-index: 10;
}

.item-note:hover {
  transform: rotate(0deg) translateY(-3px);
  box-shadow: 3px 6px 14px rgba(0, 0, 0, 0.22);
}

.item-note-delete {
  position: absolute;
  top: 8px;
  right: 8px;
  font-size: 12px;
  color: var(--cork-note-label);
  cursor: pointer;
  opacity: 0.6;
  transition: opacity 0.3s ease, color 0.3s ease;
}

.item-note-delete:hover {
  opacity: 1;
  color: #c62828;
}

.item-note-field {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.item-note-field label {
  font-size: 11px;
  color: var(--cork-note-label);
  font-weight: 500;
}

.item-note-field input {
  font-size: 14px;
  color: var(--cork-note-text);
  background: var(--cork-note-yellow-field-bg);
  border: none;
  border-bottom: 1px dashed var(--cork-note-border);
  border-radius: 2px;
  padding: 4px 6px;
  outline: none;
  width: 100%;
  transition: border-bottom-color 0.3s ease, background-color 0.3s ease;
}

.item-note-field input:focus {
  background: var(--cork-note-yellow-field-bg-focus);
  border-bottom-color: var(--cork-note-label);
}

.item-note-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-2);
}

.item-note-reason {
  margin: 0;
  font-size: 12px;
  color: var(--cork-note-label);
  font-style: italic;
  line-height: 1.4;
  padding-top: var(--space-1);
  border-top: 1px dashed var(--cork-note-border);
}

/* 不入冰箱的物品便签 */
.item-note--not-stored {
  background: var(--cork-note-blue);
}

.item-note--not-stored .item-note-field input {
  background: var(--cork-note-blue-hover);
}

.item-note--not-stored .item-note-field input:focus {
  background: var(--cork-note-blue);
}

/* 存放位置切换 */
.item-note-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: auto;
  padding-top: var(--space-2);
  border-top: 1px dashed var(--cork-note-border);
}

.storage-toggle {
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  font-weight: 600;
  color: var(--cork-note-label);
  cursor: pointer;
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  transition: all 0.3s ease;
}

.storage-toggle:hover {
  background: rgba(0, 0, 0, 0.06);
  color: var(--cork-note-text);
}

.storage-toggle--not-stored {
  color: var(--cork-note-text);
}

.item-note-add {
  align-items: center;
  justify-content: center;
  min-height: 180px;
  cursor: pointer;
  background: var(--cork-note-yellow);
  color: var(--cork-note-label);
  gap: var(--space-2);
}

.item-note-add .iconfont {
  font-size: 32px;
}

.item-note-add span {
  font-size: 14px;
  font-weight: 500;
}

/* 操作便签区 */
.cork-board-actions {
  position: relative;
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: var(--space-6);
  margin-top: var(--space-8);
  z-index: 1;
}

.action-note {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 120px;
  padding: var(--space-3) var(--space-6);
  font-size: 15px;
  font-weight: 600;
  line-height: 1.4;
  border: none;
  border-radius: 2px;
  cursor: pointer;
  box-shadow: 2px 3px 8px rgba(0, 0, 0, 0.18);
  transform: rotate(var(--rotate, 0deg));
  transition: transform 0.3s ease, box-shadow 0.3s ease, background-color 0.3s ease;
}

.action-note:hover {
  transform: rotate(0deg) translateY(-3px);
  box-shadow: 3px 6px 14px rgba(0, 0, 0, 0.22);
}

.action-note:active {
  transform: rotate(0deg) translateY(-1px);
}

.action-note--default {
  background: var(--cork-action-default);
  color: var(--cork-action-default-text);
}

.action-note--default:hover {
  background: var(--cork-action-default-hover);
}

.action-note--primary {
  background: var(--cork-action-primary);
  color: var(--cork-action-primary-text);
}

.action-note--primary:hover {
  background: var(--cork-action-primary-hover);
}

.action-note--success {
  background: var(--cork-action-success);
  color: var(--cork-action-success-text);
}

.action-note--success:hover {
  background: var(--cork-action-success-hover);
}

/* 我的模板软木板 */
.template-board {
  margin-top: var(--space-10);
}

.template-board .board-title-plate {
  background:
      repeating-linear-gradient(
          0deg,
          transparent 0px,
          transparent 2px,
          var(--cork-plate-texture) 2px,
          var(--cork-plate-texture) 4px,
          transparent 4px,
          transparent 7px,
          var(--cork-plate-texture-light) 7px,
          var(--cork-plate-texture-light) 8px
      ),
      var(--cork-plate-bg);
}

/* 模板便签网格 */
.template-notes-grid {
  position: relative;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: var(--space-5);
  margin-top: var(--space-4);
  z-index: 1;
}

/* 单个模板便签 */
.template-note {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 2px;
  padding: var(--space-3) var(--space-4);
  background: var(--cork-note-blue);
  border-radius: 2px;
  box-shadow: 2px 3px 8px rgba(0, 0, 0, 0.18);
  transform: rotate(var(--rotate, 0deg));
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  cursor: pointer;
  min-height: 100px;
}

.template-note:hover {
  transform: rotate(0deg) translateY(-3px);
  box-shadow: 3px 6px 14px rgba(0, 0, 0, 0.22);
}

.template-note-delete {
  position: absolute;
  top: 6px;
  right: 6px;
  font-size: 12px;
  color: var(--cork-note-label);
  cursor: pointer;
  opacity: 0.5;
  transition: opacity 0.3s ease, color 0.3s ease;
  z-index: 1;
}

.template-note-delete:hover {
  opacity: 1;
  color: #c62828;
}

.template-note-name {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--cork-note-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 100%;
}

.template-note-desc {
  margin: 0;
  font-size: 12px;
  color: var(--cork-note-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 100%;
}

.template-note-hint {
  margin: 0;
  font-size: 11px;
  color: var(--cork-note-label);
  font-style: italic;
  font-weight: 500;
}

.template-note-meta {
  padding-top: var(--space-1);
  font-size: 12px;
  color: var(--cork-note-label);
  margin-top: auto;
}

/* 空状态 */
.template-empty-note {
  position: relative;
  text-align: center;
  padding: var(--space-10);
  color: var(--cork-empty-text);
  font-size: 14px;
  z-index: 1;
}

/* 响应式：平板 */
@media (max-width: 768px) {
  .board-title-plate {
    padding: 10px 24px;
    top: -14px;
    left: 20px;
  }

  .board-title-plate .plate-text {
    font-size: 14px;
  }

  .cork-board {
    padding: var(--space-6);
  }

  .cork-board-header {
    gap: var(--space-4);
  }

  .sticky-note {
    min-width: 140px;
    padding: var(--space-3) var(--space-4);
  }

  .template-notes-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  }
}

/* 响应式：手机 */
@media (max-width: 480px) {
  .board-title-plate {
    top: -14px;
    left: 50%;
    padding: 10px 24px;
    transform: translateX(-50%) rotate(-1deg);
  }

  .board-title-plate .plate-text {
    font-size: 14px;
  }

  .purchase-plan-preview {
    padding: var(--space-3) 0;
  }

  .cork-board {
    padding: var(--space-5);
    border-radius: var(--radius-lg);
  }

  .cork-board-header {
    flex-direction: column;
    align-items: stretch;
    gap: var(--space-3);
  }

  .sticky-note {
    max-width: none;
  }

  .sticky-notes-grid {
    grid-template-columns: 1fr;
  }

  .cork-board-actions {
    width: 100%;
    flex-direction: column;
    gap: var(--space-3);
  }

  .action-note {
    width: 100%;
  }

  .template-notes-grid {
    grid-template-columns: 1fr;
  }

  .template-board {
    margin-top: var(--space-8);
  }
}
</style>
