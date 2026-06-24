<template>
  <div v-loading="loading || baseDataLoading" class="purchase-check-in-page">
    <!-- 表单模式：指定了待核对方案 -->
    <template v-if="selectedPlan && selectedPlan.planStatus === 1">
      <div class="back-bar">
        <CustomButton type="link" @click="backToList">
          <i class="iconfont icon-arrow-left"/>
          返回列表
        </CustomButton>
      </div>

      <div class="check-in-card card">
        <div class="card-header">
          <div class="card-title-wrapper">
            <div class="card-title-icon">
              <i class="iconfont icon-check"/>
            </div>
            <div>
              <h2 class="card-title">核对入库 · {{ selectedPlan.planName }}</h2>
              <p class="card-subtitle">
                目标冰箱：{{ selectedPlan.fridgeName || '未选择' }} · 原方案 {{ selectedPlan.items?.length || 0 }} 件物品
              </p>
            </div>
          </div>
          <div class="step-indicator">
            <span class="step" :class="{ active: currentStep === 1 }">1 核对采购物品</span>
            <span class="step-arrow"><i class="iconfont icon-arrow-right"/></span>
            <span class="step" :class="{ active: currentStep === 2 }">2 填写入库信息</span>
          </div>
        </div>

        <!-- 第一步：核对采购物品 -->
        <template v-if="currentStep === 1">
          <div class="tips-note">
            <div class="tips-text">
              <i class="iconfont icon-info"/>
              请确认每件物品是否采购，并可修改名称、分类、单位、数量。
            </div>
          </div>

          <div class="step1-toolbar">
            <CustomButton type="primary" size="small" @click="handleBuyAll">全部采购</CustomButton>
            <CustomButton type="default" size="small" @click="handleSkipAll">全部不采购</CustomButton>
          </div>

          <div class="list-header step1-header">
            <span class="col col-info">物品</span>
            <span class="col col-category">分类</span>
            <span class="col col-unit-type">单位类型</span>
            <span class="col col-unit">单位</span>
            <span class="col col-planned">计划数量</span>
            <span class="col col-actual">实际数量</span>
            <span class="col col-bought">是否采购</span>
            <span class="col col-action">操作</span>
          </div>

          <div class="item-list">
            <PurchaseCheckInDecisionRow
                v-for="item in settleItems"
                :key="item.tempId"
                :item="item"
                :category-options="categoryOptions"
                :unit-type-options="unitTypeOptions"
                :unit-options="allUnitOptions"
                @update="handleItemFieldUpdate(item, $event)"
                @remove="handleRemoveItem(item)"
            />
          </div>

          <div class="add-item-bar">
            <CustomButton type="default" @click="handleAddItem">
              <i class="iconfont icon-add"/>
              新增物品
            </CustomButton>
          </div>
        </template>

        <!-- 第二步：填写入库信息 -->
        <template v-else>
          <div class="tips-note">
            <div class="tips-text">
              <i class="iconfont icon-info"/>
              请确认已采购物品的入库方式与存放信息；存放位置为空时将由 AI 自动补全。
            </div>
          </div>

          <div class="list-header step2-header">
            <span class="col col-info">物品</span>
            <span class="col col-actual">实际数量</span>
            <span class="col col-store">存入冰箱</span>
            <span class="col col-date">生产日期</span>
            <span class="col col-life">保质期(天)</span>
            <span class="col col-location">存放位置</span>
            <span class="col col-remark">备注</span>
          </div>

          <div class="item-list">
            <template v-if="purchasedItems.length > 0">
              <PurchaseCheckInStorageRow
                  v-for="item in purchasedItems"
                  :key="item.tempId"
                  :item="item"
                  @update="handleItemFieldUpdate(item, $event)"
              />
            </template>
            <div v-else class="empty-list-tip">
              当前没有已采购物品，所有原有物品将被跳过。
            </div>
          </div>
        </template>

        <div class="progress-section">
          <div class="progress-bar">
            <div
                v-if="storedPercent > 0"
                class="progress-segment segment-stored"
                :style="{ width: storedPercent + '%' }"
            />
            <div
                v-if="notStoredPercent > 0"
                class="progress-segment segment-not-stored"
                :style="{ width: notStoredPercent + '%' }"
            />
            <div
                v-if="skippedPercent > 0"
                class="progress-segment segment-skipped"
                :style="{ width: skippedPercent + '%' }"
            />
          </div>
          <div class="progress-legend">
            <span class="legend-item"><i class="dot dot-stored"/> 确定入库 {{ storedCount }}</span>
            <span class="legend-item"><i class="dot dot-not-stored"/> 已采购不入库 {{ notStoredCount }}</span>
            <span class="legend-item"><i class="dot dot-skipped"/> 跳过 {{ skippedCount }}</span>
            <span class="legend-item total">共 {{ totalCount }} 件</span>
          </div>
        </div>

        <div class="card-actions">
          <template v-if="currentStep === 1">
            <CustomButton type="default" @click="backToList">返回列表</CustomButton>
            <CustomButton type="primary" @click="goStep2">下一步</CustomButton>
          </template>
          <template v-else>
            <CustomButton type="default" @click="goStep1">上一步</CustomButton>
            <CustomButton type="primary" :loading="settleLoading" @click="handleSettle">确认结算</CustomButton>
          </template>
        </div>
      </div>
    </template>

    <!-- 列表模式：Tab 内嵌历史 -->
    <template v-else>
      <div class="page-header">
        <div class="page-title-wrapper">
          <div class="page-title-icon">
            <i class="iconfont icon-check"/>
          </div>
          <div>
            <h1 class="page-title text-gradient-primary">核对入库</h1>
            <p class="page-subtitle">查看待核对方案与历史结算记录</p>
          </div>
        </div>
      </div>

      <CustomSegmented
          v-model="activeTab"
          :options="tabOptions"
          :key="tabKey"
          block
          size="large"
          class="tab-segmented"
          @change="handleTabChange"
      />

      <div class="tab-content">
        <div v-if="currentPlans.length === 0" class="empty-card">
          <i class="iconfont icon-empty"/>
          <p>暂无{{ currentTabLabel }}方案</p>
        </div>

        <div v-else class="plan-grid">
          <div
              v-for="plan in currentPlans"
              :key="plan.id"
              class="plan-card"
              @click="activeTab === 'pending' ? enterPlan(plan) : openHistoryDetail(plan)"
          >
            <div class="plan-card-header">
              <h3 class="plan-name">{{ plan.planName }}</h3>
              <span class="plan-status" :class="planStatusClass(plan.planStatus)">
                {{ planStatusText(plan.planStatus) }}
              </span>
            </div>
            <div class="plan-card-meta">
              <span><i class="iconfont icon-fridge-line"/> {{ plan.fridgeName || '未选择冰箱' }}</span>
              <span><i class="iconfont icon-box"/> {{ plan.totalItems }} 件物品</span>
              <span><i class="iconfont icon-time"/> {{ formatDateTime(plan.createTime) }}</span>
            </div>
            <div v-if="plan.planStatus === 2" class="plan-card-stats">
              <span class="stat-stored">入库 {{ plan.items?.filter(it => it.status === 2).length || 0 }}</span>
              <span class="stat-not-stored">不入库 {{ plan.items?.filter(it => it.status === 4).length || 0 }}</span>
              <span class="stat-skipped">跳过 {{ plan.items?.filter(it => it.status === 3).length || 0 }}</span>
            </div>
            <div v-if="plan.planStatus === 3" class="plan-card-actions" @click.stop>
              <CustomButton type="danger" size="small" @click="handleDeletePlan(plan)">删除</CustomButton>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 历史详情弹窗 -->
    <PurchasePlanSettleDetailDialog
        v-model:visible="showHistoryDetailDialog"
        :plan="selectedHistoryPlan"
    />

    <!-- 删除确认 -->
    <ConfirmDialog
        v-model:visible="showDeleteDialog"
        title="删除方案"
        :message="`确定要删除方案「${deletingPlan?.planName || ''}」吗？删除后不可恢复。`"
        confirm-text="删除"
        cancel-text="取消"
        @confirm="confirmDeletePlan"
        width="400px"
    />
    <PurchaseCheckInTour ref="tourRef" :mode="tourMode"/>
  </div>
</template>

<script setup>
import {computed, nextTick, onMounted, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import PurchaseCheckInDecisionRow from '@/components/purchase/PurchaseCheckInDecisionRow.vue'
import PurchaseCheckInStorageRow from '@/components/purchase/PurchaseCheckInStorageRow.vue'
import CustomButton from '@/components/ui/CustomButton.vue'
import CustomSegmented from '@/components/ui/CustomSegmented.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import PurchasePlanSettleDetailDialog from '@/components/purchase/PurchasePlanSettleDetailDialog.vue'
import PurchaseCheckInTour from '@/components/tour/PurchaseCheckInTour.vue'
import {useTourStore, TOUR_SCENES} from '@/stores/tour'
import showMessage from '@/utils/message'
import notifyGamificationResult from '@/utils/gamificationNotify'
import {listPurchasePlans, getPurchasePlan, settlePurchasePlan, deletePurchasePlan} from '@/api/purchase'
import {listItemCategories, listItemUnits, listUnitTypes} from '@/api/item'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const baseDataLoading = ref(false)
const plans = ref([])
const activeTab = ref('pending')
const settleItems = ref([])
const settleLoading = ref(false)
const currentStep = ref(1)
const nextTempId = ref(1)

const categoryList = ref([])
const unitTypeList = ref([])
const unitList = ref([])

const showHistoryDetailDialog = ref(false)
const selectedHistoryPlan = ref(null)

const showDeleteDialog = ref(false)
const deletingPlan = ref(null)

const selectedPlanId = computed(() => Number(route.query.planId) || null)
const selectedPlan = computed(() => plans.value.find(p => p.id === selectedPlanId.value) || null)

const categoryOptions = computed(() => categoryList.value.map(c => ({
  label: c.categoryName,
  value: c.id
})))

const unitTypeOptions = computed(() => unitTypeList.value.map(t => ({
  label: t.unitTypeName,
  value: t.id
})))

const allUnitOptions = computed(() => unitList.value.map(u => ({
  label: u.unitName,
  value: u.id,
  unitTypeId: u.unitTypeId
})))

const pendingPlans = computed(() => plans.value.filter(p => p.planStatus === 1))
const completedPlans = computed(() => plans.value.filter(p => p.planStatus === 2))
const cancelledPlans = computed(() => plans.value.filter(p => p.planStatus === 3))

const tabOptions = computed(() => [
  {label: `待核对（${pendingPlans.value.length}）`, value: 'pending'},
  {label: `已完成（${completedPlans.value.length}）`, value: 'completed'},
  {label: `已取消（${cancelledPlans.value.length}）`, value: 'cancelled'}
])

const tabKey = computed(() => tabOptions.value.map(o => o.label).join('|'))

const currentPlans = computed(() => {
  switch (activeTab.value) {
    case 'pending':
      return pendingPlans.value
    case 'completed':
      return completedPlans.value
    case 'cancelled':
      return cancelledPlans.value
    default:
      return []
  }
})

const currentTabLabel = computed(() => {
  const map = {pending: '待核对', completed: '已完成', cancelled: '已取消'}
  return map[activeTab.value] || ''
})

const purchasedItems = computed(() => settleItems.value.filter(i => i.bought))

const storedCount = computed(() => settleItems.value.filter(i => i.bought && i.storeInFridge).length)
const notStoredCount = computed(() => settleItems.value.filter(i => i.bought && !i.storeInFridge).length)
const skippedCount = computed(() => settleItems.value.filter(i => !i.bought).length)
const totalCount = computed(() => settleItems.value.length)

const storedPercent = computed(() => totalCount.value ? (storedCount.value / totalCount.value) * 100 : 0)
const notStoredPercent = computed(() => totalCount.value ? (notStoredCount.value / totalCount.value) * 100 : 0)
const skippedPercent = computed(() => totalCount.value ? (skippedCount.value / totalCount.value) * 100 : 0)

const formatDecimal = (value) => {
  if (value === null || value === undefined || value === '') return ''
  const num = parseFloat(value)
  if (Number.isNaN(num)) return value
  return Number(num.toFixed(2))
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const formatDateTime = (date) => {
  if (!date) return ''
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const planStatusText = (status) => {
  const map = {1: '待核对', 2: '已完成', 3: '已取消'}
  return map[status] || '未知'
}

const planStatusClass = (status) => {
  const map = {1: 'status-pending', 2: 'status-completed', 3: 'status-cancelled'}
  return map[status] || ''
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
  } catch (error) {
    console.error('加载基础数据失败:', error)
    showMessage.error('加载基础数据失败')
  } finally {
    baseDataLoading.value = false
  }
}

const fetchPlans = async () => {
  loading.value = true
  try {
    const res = await listPurchasePlans()
    if (res.code === 200 && Array.isArray(res.data)) {
      plans.value = res.data
    } else {
      plans.value = []
    }
  } catch (error) {
    console.error('获取方案列表失败:', error)
    showMessage.error('获取方案列表失败')
    plans.value = []
  } finally {
    loading.value = false
  }
}

const createNewItem = () => {
  const id = nextTempId.value++
  return {
    planItemId: null,
    itemName: '',
    categoryId: null,
    categoryName: '',
    itemUnitId: null,
    itemUnitName: '',
    unitTypeId: null,
    unitTypeName: '',
    plannedNum: 0,
    actualNum: 1,
    bought: true,
    storeInFridge: true,
    productionDate: null,
    shelfLifeDays: null,
    storageLocation: '',
    remark: '',
    isNew: true,
    tempId: `new-${id}`
  }
}

const handleAddItem = () => {
  settleItems.value.push(createNewItem())
}

const handleRemoveItem = (item) => {
  const index = settleItems.value.findIndex(i => i.tempId === item.tempId)
  if (index > -1) {
    settleItems.value.splice(index, 1)
  }
}

const handleBuyAll = () => {
  settleItems.value.forEach(item => {
    if (!item.isNew) {
      item.bought = true
    }
  })
}

const handleSkipAll = () => {
  settleItems.value.forEach(item => {
    if (!item.isNew) {
      item.bought = false
    }
  })
}

const handleItemFieldUpdate = (item, payload) => {
  const target = settleItems.value.find(i => i.tempId === item.tempId)
  if (!target) return
  const {field, value} = payload

  if (field === 'categoryId') {
    target.categoryId = value
    const cat = categoryList.value.find(c => c.id === value)
    target.categoryName = cat?.categoryName || ''
  } else if (field === 'unitTypeId') {
    target.unitTypeId = value
    target.unitTypeName = unitTypeList.value.find(t => t.id === value)?.unitTypeName || ''
    target.itemUnitId = null
    target.itemUnitName = ''
  } else if (field === 'itemUnitId') {
    target.itemUnitId = value
    const unit = unitList.value.find(u => u.id === value)
    target.itemUnitName = unit?.unitName || ''
    if (unit && unit.unitTypeId && !target.unitTypeId) {
      target.unitTypeId = unit.unitTypeId
      target.unitTypeName = unitTypeList.value.find(t => t.id === unit.unitTypeId)?.unitTypeName || ''
    }
  } else {
    target[field] = value
  }
}

const initSettleItems = (plan) => {
  settleItems.value = (plan?.items || []).map(it => {
    let unitTypeId = it.unitTypeId ?? null
    if (!unitTypeId && it.itemUnitId && unitList.value.length) {
      const unit = unitList.value.find(u => u.id === it.itemUnitId)
      if (unit) unitTypeId = unit.unitTypeId
    }
    return {
      planItemId: it.id,
      itemName: it.itemName || '',
      categoryId: it.categoryId ?? null,
      categoryName: it.categoryName || '',
      itemUnitId: it.itemUnitId ?? null,
      itemUnitName: it.itemUnitName || '',
      unitTypeId,
      unitTypeName: it.unitTypeName || '',
      plannedNum: Number(it.plannedNum) || 0,
      actualNum: Number(it.plannedNum) || 1,
      bought: false,
      storeInFridge: it.storeInFridge ?? true,
      productionDate: null,
      shelfLifeDays: it.shelfLifeDays ?? null,
      storageLocation: it.storageLocation || '',
      remark: '',
      isNew: false,
      tempId: `plan-${it.id}`
    }
  })
  nextTempId.value = 1
}

const initForm = async () => {
  if (!selectedPlanId.value) {
    settleItems.value = []
    currentStep.value = 1
    return
  }
  const plan = plans.value.find(p => p.id === selectedPlanId.value)
  if (plan && plan.planStatus !== 1) {
    showMessage.warning('该方案不是待核对状态')
    backToList()
    return
  }
  loading.value = true
  try {
    const res = await getPurchasePlan(selectedPlanId.value)
    if (res.code === 200 && res.data) {
      if (res.data.planStatus !== 1) {
        showMessage.warning('该方案不是待核对状态')
        backToList()
        return
      }
      initSettleItems(res.data)
      currentStep.value = 1
    } else {
      showMessage.error(res.message || '获取方案详情失败')
      backToList()
    }
  } catch (error) {
    console.error('获取方案详情失败:', error)
    showMessage.error('获取方案详情失败')
    backToList()
  } finally {
    loading.value = false
  }
}

const validateStep1 = () => {
  for (const item of settleItems.value) {
    if (!item.bought && !item.isNew) continue
    if (!item.itemName?.trim()) {
      showMessage.warning('请填写新增/已采购物品的名称')
      return false
    }
    if (!item.categoryId) {
      showMessage.warning(`「${item.itemName}」请选择分类`)
      return false
    }
    if (!item.itemUnitId) {
      showMessage.warning(`「${item.itemName}」请选择单位`)
      return false
    }
    if (!item.actualNum || Number(item.actualNum) <= 0) {
      showMessage.warning(`「${item.itemName}」实际数量必须大于 0`)
      return false
    }
  }
  return true
}

const goStep1 = () => {
  currentStep.value = 1
}

const goStep2 = () => {
  if (!validateStep1()) return
  currentStep.value = 2
}

const buildSettlePayload = () => {
  return {
    items: settleItems.value.map(it => {
      const base = {remark: it.remark || undefined}
      if (!it.bought && !it.isNew) {
        return {...base, planItemId: it.planItemId, skip: true}
      }
      const entry = {
        ...base,
        planItemId: it.planItemId || undefined,
        skip: false,
        itemName: it.itemName,
        categoryId: it.categoryId,
        itemUnitId: it.itemUnitId,
        actualNum: String(it.actualNum),
        forceStoreInFridge: it.storeInFridge
      }
      if (it.storeInFridge) {
        entry.productionDate = it.productionDate || undefined
        entry.shelfLifeDays = it.shelfLifeDays || undefined
        entry.storageLocation = it.storageLocation
      }
      return entry
    })
  }
}

const handleSettle = async () => {
  if (!selectedPlan.value) return
  if (!validateStep1()) return

  settleLoading.value = true
  try {
    const payload = buildSettlePayload()
    const res = await settlePurchasePlan(selectedPlan.value.id, payload)
    if (res.code === 200) {
      notifyGamificationResult(res, '核对入库结算')
      const data = res.data || {}
      const settled = data.settledCount ?? 0
      const notStored = data.notStoredCount ?? 0
      const skipped = data.skippedCount ?? 0
      showMessage.success(`结算完成：入库 ${settled} 件，未入库 ${notStored} 件，跳过 ${skipped} 件`)
      await router.push('/purchase/tasks')
    } else {
      showMessage.error(res.message || '结算失败')
    }
  } catch (error) {
    console.error('结算失败:', error)
    showMessage.error('结算失败')
  } finally {
    settleLoading.value = false
  }
}

const enterPlan = (plan) => {
  router.push(`/purchase/check-in?planId=${plan.id}`)
}

const backToList = () => {
  router.replace('/purchase/check-in')
}

const handleTabChange = (val) => {
  activeTab.value = val
}

const openHistoryDetail = (plan) => {
  selectedHistoryPlan.value = plan
  showHistoryDetailDialog.value = true
}

const handleDeletePlan = (plan) => {
  deletingPlan.value = plan
  showDeleteDialog.value = true
}

const confirmDeletePlan = async () => {
  if (!deletingPlan.value) return
  try {
    const res = await deletePurchasePlan(deletingPlan.value.id)
    if (res.code === 200) {
      showMessage.success('方案已删除')
      await fetchPlans()
    } else {
      showMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    console.error('删除方案失败:', error)
    showMessage.error('删除方案失败')
  } finally {
    deletingPlan.value = null
    showDeleteDialog.value = false
  }
}

watch(() => route.query.planId, initForm, {immediate: false})

watch(activeTab, () => {
  if (selectedPlanId.value) {
    backToList()
  }
})

onMounted(() => {
  loadBaseData().then(() => {
    fetchPlans().then(() => {
      if (selectedPlanId.value) {
        initForm()
      }
    })
  })
})

// 页面引导
const tourRef = ref(null)
const tourStore = useTourStore()
const tourMode = ref('list')
const isInFormMode = computed(() => !!selectedPlan.value && selectedPlan.value.planStatus === 1)

watch(() => tourStore.pendingStartScene, (scene) => {
  if (scene === TOUR_SCENES.PURCHASE_CHECK_IN) {
    // 根据当前状态决定展示列表态还是核对流程态的指引
    tourMode.value = isInFormMode.value ? 'form' : 'list'
    nextTick(() => {
      tourRef.value?.start()
    })
  }
})
</script>

<style scoped lang="scss">
.purchase-check-in-page {
  width: 100%;
  max-width: 1140px;
  margin: 0 auto;
  padding: var(--space-6) var(--space-4);
  animation: fade-in-up 0.5s ease-out;
}

.back-bar {
  margin-bottom: var(--space-4);
}

/* 通用卡片（与冰箱详情、物品管理统一） */
.card {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

/* 核对表单卡片 */
.check-in-card {
  animation: fade-in-up 0.5s ease-out;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-5);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--border-color);
  gap: var(--space-4);
  flex-wrap: wrap;
}

.card-title-wrapper {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.card-title-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  background: var(--primary-10);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.card-title-icon .iconfont {
  font-size: 26px;
  color: var(--primary-color);
}

.card-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  line-height: 1.3;
}

.card-subtitle {
  font-size: 13px;
  color: var(--text-secondary);
  margin: var(--space-1) 0 0;
}

/* 步骤指示器 */
.step-indicator {
  display: inline-flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2) var(--space-4);
  background: var(--primary-10);
  border-radius: var(--radius-full);
  font-size: 14px;
  color: var(--text-secondary);
  flex-shrink: 0;
}

.step {
  font-weight: 500;
  transition: color 0.3s ease;
}

.step.active {
  color: var(--primary-color);
  font-weight: 600;
}

.step-arrow {
  font-size: 12px;
  color: var(--text-tertiary);
}

.tips-note {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background: var(--warning-light);
  color: var(--warning-color);
  border-radius: var(--radius-md);
  font-size: 14px;
  margin-bottom: var(--space-4);

  .iconfont {
    font-size: 16px;
  }
}

.tips-text {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.empty-list-tip {

  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-10) var(--space-4);
  color: var(--text-secondary);
  font-size: 14px;
  background: var(--primary-10);
  border-radius: var(--radius-md);
}

/* 列表表头 */
.list-header {
  display: grid;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2) var(--space-4);
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border-color);
  background: var(--primary-10);
  border-radius: var(--radius-md) var(--radius-md) 0 0;
}

.list-header .col {
  text-align: center;
}

.list-header .col-info {
  text-align: left;
}

.list-header.step1-header {
  grid-template-columns:
    minmax(160px, 1.8fr)
    minmax(110px, 1.2fr)
    minmax(110px, 1.2fr)
    minmax(110px, 1.2fr)
    80px
    100px
    80px
    75px;
}

.list-header.step2-header {
  grid-template-columns:
    minmax(160px, 1.4fr)
    110px
    70px
    160px
    100px
    130px
    minmax(120px, 1fr);
}

.step1-toolbar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: var(--space-2);
  padding: 0 var(--space-4) var(--space-3);
}

.add-item-bar {
  display: flex;
  justify-content: flex-start;
  padding: var(--space-3) var(--space-4);
  margin-bottom: var(--space-4);
  border: 1px dashed var(--border-color);
  border-radius: 0 0 var(--radius-md) var(--radius-md);
  background: var(--primary-10);
}



/* 进度条 */
.progress-section {
  padding: var(--space-2) 0 var(--space-4);
}

.progress-bar {
  display: flex;
  height: 12px;
  border-radius: var(--radius-full);
  overflow: hidden;
  background: var(--border-color);
  margin-bottom: var(--space-3);
}

.progress-segment {
  height: 100%;
  transition: width 0.35s ease;
}

.segment-stored {
  background: var(--success-color);
}

.segment-not-stored {
  background: var(--info-color);
}

.segment-skipped {
  background: var(--warning-color);
}

.progress-legend {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: var(--space-4);
  font-size: 13px;
  color: var(--text-secondary);
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.dot-stored {
  background: var(--success-color);
}

.dot-not-stored {
  background: var(--info-color);
}

.dot-skipped {
  background: var(--warning-color);
}

.total {
  color: var(--text-primary);
  font-weight: 600;
}

/* 卡片内操作按钮 */
.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px solid var(--border-color);
}

/* 列表模式页面标题 */
.page-header {
  display: flex;
  justify-content: center;
  margin-bottom: var(--space-5);
}

.page-title-wrapper {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.page-title-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  background: var(--primary-10);
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-title-icon .iconfont {
  font-size: 32px;
  color: var(--primary-color);
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0;
  line-height: 1.2;
}

.page-subtitle {
  font-size: 14px;
  color: var(--text-tertiary);
  margin: var(--space-1) 0 0;
}

.tab-segmented {
  margin-bottom: var(--space-5);
}

.tab-content {
  min-height: 320px;
}

.empty-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  padding: var(--space-10) 0;
  color: var(--text-secondary);
  font-size: 15px;
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
}

.empty-card .iconfont {
  font-size: 48px;
  color: var(--text-tertiary);
}

/* 方案卡片网格 */
.plan-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-4);
}

.plan-card {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: all 0.3s ease;
}

.plan-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-card-hover);
  border-color: var(--primary-30);
}

.plan-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
}

.plan-name {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  line-height: 1.3;
}

.plan-status {
  flex-shrink: 0;
  padding: 2px 8px;
  border-radius: var(--radius-full);
  font-size: 11px;
  font-weight: 600;
}

.status-pending {
  background: var(--warning-light);
  color: var(--warning-color);
}

.status-completed {
  background: var(--success-light);
  color: var(--success-color);
}

.status-cancelled {
  background: var(--danger-light);
  color: var(--danger-color);
}

.plan-card-meta {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: var(--space-3);

  .iconfont {
    margin-right: var(--space-1);
  }
}

.plan-card-stats {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
  font-size: 12px;
  margin-bottom: var(--space-3);
}

.stat-stored {
  color: var(--success-color);
}

.stat-not-stored {
  color: var(--info-color);
}

.stat-skipped {
  color: var(--warning-color);
}

.plan-card-actions {
  display: flex;
  justify-content: flex-end;
}

/* 动画 */
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

/* 响应式 */
@media (max-width: 992px) {
  .list-header {
    display: none;
  }

  .item-list {
    padding-right: 0;
  }
}

@media (max-width: 768px) {
  .purchase-check-in-page {
    padding: var(--space-4) var(--space-3);
  }

  .card {
    padding: var(--space-4);
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-3);
  }

  .step-indicator {
    width: 100%;
    justify-content: center;
  }

  .card-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .add-item-bar {
    justify-content: center;
  }

  .page-title {
    font-size: 26px;
  }

  .plan-grid {
    grid-template-columns: 1fr;
  }
}
</style>
