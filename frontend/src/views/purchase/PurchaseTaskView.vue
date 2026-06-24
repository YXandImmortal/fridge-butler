<template>
  <div v-loading="loading" class="purchase-task-page">
    <!-- 编辑态：复用 PurchasePlanPreview -->
    <Transition name="preview-slide" mode="out-in">
      <PurchasePlanPreview
          v-if="showPreview"
          key="preview"
          v-model:plan-name="editingPlanName"
          v-model:items="editingItems"
          :loading="updateLoading"
          :fridge-name="selectedPlan?.fridgeName || ''"
          :date-text="selectedPlanDateText"
          :source="selectedPlan?.source || ''"
          :scene-desc="selectedPlan?.sceneDesc || ''"
          :tips="[]"
          @return="exitEdit"
          @confirm="handleUpdatePlan"
      />

      <!-- 结算态：核对入库表单（已迁移到 /purchase/check-in，暂时注释保留）
      <div v-else-if="showSettle" key="settle" class="task-view settle-view">
        ... 原内联结算模板内容 ...
      </div>
      -->

      <!-- 查看态：纸张卡片 + 书签 -->
      <div v-else key="task" class="task-view">
        <!-- 空状态 -->
        <div v-if="!hasPlans" class="empty-paper">
          <div class="paper-card empty-card">
            <div class="paper-header">
              <h2 class="paper-title">暂无待采购任务</h2>
              <p class="paper-meta">当前没有待采购的方案，去采购助手创建一个新计划吧</p>
            </div>
            <div class="paper-actions">
              <button type="button" class="action-note action-note--primary" @click="goToCreate">
                去创建采购计划
              </button>
            </div>
          </div>
        </div>

        <template v-else>
          <!-- 纸张卡片 -->
          <div class="paper-card">
            <div class="paper-tape"></div>
            <div class="paper-header">
              <h2 class="paper-title">{{ selectedPlan?.planName }}</h2>
              <div class="paper-meta-row">
                <span class="status-tag status-pending">待采购</span>
                <span class="paper-meta">创建于 {{ selectedPlanDateText }}</span>
                <span class="paper-meta">目标冰箱：{{ selectedPlan?.fridgeName || '未选择' }}</span>
              </div>
            </div>

            <div class="paper-divider"></div>

            <!-- 物品清单 -->
            <div class="paper-body">
              <div class="item-list-header">
                <span>物品名称</span>
                <span>计划数量</span>
                <span>分类</span>
              </div>
              <ul class="item-list">
                <li v-for="item in selectedPlan?.items" :key="item.id" class="item-row">
                  <span class="item-check"></span>
                  <span class="item-name">{{ item.itemName }}</span>
                  <span class="item-num">{{ formatDecimal(item.plannedNum) }} {{ item.itemUnitName || '' }}</span>
                  <span class="item-category">{{ item.categoryName || '-' }}</span>
                </li>
              </ul>
              <p v-if="selectedPlan?.sceneDesc" class="scene-desc">
                <i class="iconfont icon-edit"/> {{ selectedPlan.sceneDesc }}
              </p>
            </div>

            <div class="paper-divider"></div>

            <!-- 操作按钮 -->
            <div class="paper-actions">
              <button type="button" class="action-note action-note--default" @click="handleCancel">
                取消计划
              </button>
              <button type="button" class="action-note action-note--primary" @click="handleModify">
                修改计划
              </button>
              <button type="button" class="action-note action-note--success" @click="handleComplete">
                采购完成
              </button>
            </div>
          </div>

          <!-- 书签栏 -->
          <div class="bookmark-list">
            <div
                v-for="(plan, index) in pendingPlans"
                :key="plan.id"
                :class="['bookmark', { active: plan.id === selectedPlanId }]"
                :style="{ animationDelay: `${0.3 + index * 0.08}s` }"
                :title="plan.planName"
                @click="selectPlan(plan.id)"
            >
              <span class="bookmark-title">{{ plan.planName }}</span>
              <span class="bookmark-count">{{ plan.totalItems }} 件</span>
            </div>
          </div>
        </template>
      </div>
    </Transition>

    <!-- 取消确认弹窗 -->
    <ConfirmDialog
        v-model:visible="showCancelDialog"
        title="取消任务"
        :message="`确定要取消采购任务「${selectedPlan?.planName || ''}」吗？取消后不可恢复。`"
        confirm-text="确定取消"
        cancel-text="再想想"
        @confirm="confirmCancel"
        width="400px"
    />
    <PurchaseTaskTour ref="tourRef"/>
  </div>
</template>

<script setup>
import {computed, onMounted, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useTourStore, TOUR_SCENES} from '@/stores/tour'
import PurchaseTaskTour from '@/components/tour/PurchaseTaskTour.vue'
import PurchasePlanPreview from '@/components/purchase/PurchasePlanPreview.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import showMessage from '@/utils/message'
import {listPurchasePlans, cancelPurchasePlan, updatePurchasePlan} from '@/api/purchase'

const route = useRoute()
const router = useRouter()

// 数据状态
const plans = ref([])
const selectedPlanId = ref(null)
const loading = ref(false)
const updateLoading = ref(false)

// 编辑态
const showPreview = ref(false)
const editingPlanName = ref('')
const editingItems = ref([])

// 结算态（已迁移到 /purchase/check-in，暂时注释保留）
// const showSettle = ref(false)
// const settleLoading = ref(false)
// const settleItems = ref([])

// 取消确认
const showCancelDialog = ref(false)

// 计算属性
const pendingPlans = computed(() => plans.value.filter(p => p.planStatus === 1))
const selectedPlan = computed(() => pendingPlans.value.find(p => p.id === selectedPlanId.value) || null)
const hasPlans = computed(() => pendingPlans.value.length > 0)

const selectedPlanDateText = computed(() => {
  if (!selectedPlan.value?.createTime) return ''
  const date = new Date(selectedPlan.value.createTime)
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
})

// 工具函数
const stripTrailingZeros = (value) => {
  if (value === null || value === undefined || value === '') return '1'
  const num = Number(value)
  if (Number.isNaN(num)) return '1'
  return String(num)
}

const formatDecimal = (value) => {
  if (value === null || value === undefined || value === '') return ''
  const num = parseFloat(value)
  if (Number.isNaN(num)) return value
  return Number(num.toFixed(2))
}

// const formatDate = (date) => {
//   if (!date || isNaN(date.getTime())) return ''
//   const y = date.getFullYear()
//   const m = String(date.getMonth() + 1).padStart(2, '0')
//   const d = String(date.getDate()).padStart(2, '0')
//   return `${y}-${m}-${d}`
// }

const normalizePlanItems = (items = []) => items.map(it => ({
  id: it.id,
  itemName: it.itemName,
  plannedNum: stripTrailingZeros(it.plannedNum),
  unitName: it.itemUnitName || '',
  categoryName: it.categoryName || '',
  reason: '',
  categoryId: it.categoryId ?? null,
  itemUnitId: it.itemUnitId ?? null,
  unitTypeId: null,
  storeInFridge: it.storeInFridge ?? true
}))

// 数据获取
const fetchPlans = async () => {
  loading.value = true
  try {
    const res = await listPurchasePlans({planStatus: 1})
    if (res.code === 200 && Array.isArray(res.data)) {
      plans.value = res.data
      // 初始化选中
      const activeId = Number(route.query.active)
      if (activeId && plans.value.some(p => p.id === activeId && p.planStatus === 1)) {
        selectedPlanId.value = activeId
      } else if (pendingPlans.value.length > 0) {
        selectedPlanId.value = pendingPlans.value[0].id
      } else {
        selectedPlanId.value = null
      }
    } else {
      plans.value = []
      selectedPlanId.value = null
    }
  } catch (error) {
    console.error('获取采购任务失败:', error)
    showMessage.error('获取采购任务失败')
    plans.value = []
  } finally {
    loading.value = false
  }
}

// 选择书签
const selectPlan = (id) => {
  selectedPlanId.value = id
  // 清除 URL 中的 active 参数，避免刷新后仍停留在旧高亮
  if (route.query.active) {
    router.replace({path: route.path})
  }
}

// 取消任务
const handleCancel = () => {
  if (!selectedPlan.value) return
  showCancelDialog.value = true
}

const confirmCancel = async () => {
  if (!selectedPlan.value) return
  try {
    const res = await cancelPurchasePlan(selectedPlan.value.id)
    if (res.code === 200) {
      showMessage.success('任务已取消')
      // 取消后该任务会消失，重新拉取列表
      await fetchPlans()
    } else {
      showMessage.error(res.message || '取消任务失败')
    }
  } catch (error) {
    console.error('取消任务失败:', error)
    showMessage.error('取消任务失败')
  }
}

// 修改任务
const handleModify = () => {
  if (!selectedPlan.value) return
  editingPlanName.value = selectedPlan.value.planName
  editingItems.value = normalizePlanItems(selectedPlan.value.items)
  showPreview.value = true
}

const exitEdit = () => {
  showPreview.value = false
  editingPlanName.value = ''
  editingItems.value = []
}

const handleUpdatePlan = async () => {
  if (!selectedPlan.value) return
  if (!editingPlanName.value.trim()) {
    showMessage.warning('请输入计划名称')
    return
  }
  const validItems = editingItems.value.filter(i => i.itemName?.trim() && Number(i.plannedNum) > 0)
  if (validItems.length === 0) {
    showMessage.warning('请至少添加一件有效物品')
    return
  }

  updateLoading.value = true
  try {
    const res = await updatePurchasePlan(selectedPlan.value.id, {
      planName: editingPlanName.value.trim(),
      sceneDesc: selectedPlan.value.sceneDesc,
      items: validItems.map(i => ({
        itemName: i.itemName.trim(),
        categoryId: i.categoryId,
        plannedNum: String(i.plannedNum),
        itemUnitId: i.itemUnitId,
        storeInFridge: i.storeInFridge ?? true
      }))
    })
    if (res.code === 200) {
      showMessage.success('任务修改成功')
      exitEdit()
      await fetchPlans()
    } else {
      showMessage.error(res.message || '修改任务失败')
    }
  } catch (error) {
    console.error('修改任务失败:', error)
    showMessage.error('修改任务失败')
  } finally {
    updateLoading.value = false
  }
}

// 进入结算态（已迁移到 /purchase/check-in，暂时注释保留）
// const initSettleItems = (plan) => {
//   const today = formatDate(new Date())
//   settleItems.value = (plan?.items || []).map(it => ({
//     planItemId: it.id,
//     itemName: it.itemName,
//     plannedNum: it.plannedNum,
//     itemUnitName: it.itemUnitName || '',
//     categoryName: it.categoryName || '',
//     skip: false,
//     actualNum: parseFloat(it.plannedNum) || 0,
//     productionDate: today,
//     shelfLifeDays: null,
//     storageLocation: '',
//     remark: '',
//     forceStoreInFridge: it.storeInFridge ?? true
//   }))
// }

// const handleComplete = async () => {
//   if (!selectedPlan.value) return
//   settleLoading.value = true
//   try {
//     const res = await getPurchasePlan(selectedPlan.value.id)
//     const plan = res.code === 200 && res.data ? res.data : selectedPlan.value
//     initSettleItems(plan)
//     showSettle.value = true
//   } catch (error) {
//     console.error('获取方案详情失败:', error)
//     initSettleItems(selectedPlan.value)
//     showSettle.value = true
//   } finally {
//     settleLoading.value = false
//   }
// }

// const exitSettle = () => {
//   showSettle.value = false
//   settleItems.value = []
// }

// const handleSettleConfirm = async () => {
//   if (!selectedPlan.value) return
//   for (const item of settleItems.value) {
//     if (!item.skip && (!item.actualNum || Number(item.actualNum) <= 0)) {
//       showMessage.warning(`「${item.itemName}」的实际数量必须大于 0`)
//       return
//     }
//   }
//
//   settleLoading.value = true
//   try {
//     const payload = {
//       items: settleItems.value.map(it => {
//         if (it.skip) {
//           return {planItemId: it.planItemId, skip: true}
//         }
//         const entry = {
//           planItemId: it.planItemId,
//           skip: false,
//           actualNum: String(it.actualNum),
//           forceStoreInFridge: it.forceStoreInFridge
//         }
//         if (it.productionDate) entry.productionDate = it.productionDate
//         if (it.shelfLifeDays != null && it.shelfLifeDays !== '') entry.shelfLifeDays = Number(it.shelfLifeDays)
//         if (it.storageLocation?.trim()) entry.storageLocation = it.storageLocation.trim()
//         if (it.remark?.trim()) entry.remark = it.remark.trim()
//         return entry
//       })
//     }
//     const res = await settlePurchasePlan(selectedPlan.value.id, payload)
//     if (res.code === 200) {
//       const data = res.data || {}
//       const settled = data.settledCount ?? 0
//       const notStored = data.notStoredCount ?? 0
//       const skipped = data.skippedCount ?? 0
//       const exp = data.expGained ?? 0
//       const levelTip = data.leveledUp ? `，升级至 Lv.${data.currentLevel || data.level?.level || ''}` : ''
//       showMessage.success(`结算完成：入库 ${settled} 件，未入库 ${notStored} 件，跳过 ${skipped} 件${exp ? `，获得 ${exp} 经验` : ''}${levelTip}`)
//       exitSettle()
//       await fetchPlans()
//     } else {
//       showMessage.error(res.message || '结算失败')
//     }
//   } catch (error) {
//     console.error('结算失败:', error)
//     showMessage.error('结算失败')
//   } finally {
//     settleLoading.value = false
//   }
// }

// 跳转核对入库页
const handleComplete = () => {
  if (!selectedPlan.value) return
  router.push(`/purchase/check-in?planId=${selectedPlan.value.id}`)
}

// 去创建页面
const goToCreate = () => {
  router.push('/purchase/plan')
}

onMounted(() => {
  fetchPlans()
})

// 页面引导
const tourRef = ref(null)
const tourStore = useTourStore()

watch(() => tourStore.pendingStartScene, (scene) => {
  if (scene === TOUR_SCENES.PURCHASE_TASK) {
    tourRef.value?.start()
  }
})
</script>

<style scoped lang="scss">
/* =========================================================
 * 采购任务页 · 信笺纸 + 书签风格
 * ========================================================= */
.purchase-task-page {
  width: 100%;
  padding: var(--space-6) var(--space-4);
  animation: fade-in-up 0.5s ease-out;
}

.task-view {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  gap: 0;
  max-width: 1000px;
  margin: 0 auto;
}

/* 纸张卡片 */
.paper-card {
  position: relative;
  min-width: 600px;
  background:
      linear-gradient(90deg, var(--paper-margin-line) 0, var(--paper-margin-line) 32px, transparent 32px),
      repeating-linear-gradient(
          0deg,
          transparent 0,
          transparent 31px,
          var(--paper-line) 31px,
          var(--paper-line) 32px
      ),
      var(--paper-bg);
  border-radius: 2px;
  padding: var(--space-8) var(--space-8) var(--space-8) 56px;
  box-shadow:
      0 1px 1px rgba(0, 0, 0, 0.08),
      0 4px 12px var(--paper-shadow);
  transform: rotate(-0.5deg);
  z-index: 2;
  animation: paper-drop-in 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

/* 顶部胶带 */
.paper-tape {
  position: absolute;
  top: -12px;
  left: 50%;
  transform: translateX(-50%) rotate(1deg);
  width: 140px;
  height: 32px;
  background: var(--paper-tape);
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(2px);
}

.paper-header {
  margin-bottom: var(--space-4);
}

.paper-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 var(--space-3);
  line-height: 1.3;
}

.paper-body {
  min-height: 400px;
}

.paper-meta-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-3);
}

.paper-meta {
  font-size: 13px;
  color: var(--text-secondary);
}

.status-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 600;
}

.status-pending {
  background: var(--warning-light);
  color: var(--warning-color);
}

.paper-divider {
  height: 1px;
  background: var(--paper-divider);
  margin: var(--space-4) 0;
}

/* 物品清单 */
.item-list-header {
  display: grid;
  grid-template-columns: 1fr 120px 100px;
  gap: var(--space-3);
  padding: 0 var(--space-2) var(--space-2);
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  border-bottom: 1px dashed var(--paper-line);
  margin-bottom: var(--space-2);
}

.item-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.item-row {
  display: grid;
  grid-template-columns: 24px 1fr 120px 100px;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2);
  font-size: 16px;
  color: var(--text-primary);
  border-bottom: 1px dashed var(--paper-line);
  min-height: 32px;
}

.item-check {
  width: 14px;
  height: 14px;
  border: 2px solid var(--text-tertiary);
  border-radius: 50%;
  opacity: 0.5;
}

.item-name {
  font-weight: 600;
}

.item-num,
.item-category {
  font-size: 14px;
  color: var(--text-secondary);
}

.scene-desc {
  margin: var(--space-4) 0 0;
  padding: var(--space-3);
  background: rgba(0, 0, 0, 0.02);
  border-radius: var(--radius-md);
  font-size: 14px;
  color: var(--text-secondary);

  .iconfont {
    margin-right: var(--space-1);
    color: var(--primary-color);
  }
}

/* 操作按钮（便利贴风格） */
.paper-actions {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: var(--space-6);
  margin-top: var(--space-6);
  z-index: 3;
}

.action-note {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 110px;
  padding: var(--space-3) var(--space-5);
  font-size: 15px;
  font-weight: 600;
  line-height: 1.4;
  border: none;
  border-radius: 2px;
  cursor: pointer;
  box-shadow: 2px 3px 8px rgba(0, 0, 0, 0.15);
  transform: rotate(var(--rotate, 0deg));
  transition: transform 0.25s ease, box-shadow 0.25s ease, background-color 0.2s ease;
}

.action-note:hover:not(:disabled) {
  transform: rotate(0deg) translateY(-3px);
  box-shadow: 3px 6px 14px rgba(0, 0, 0, 0.2);
}

.action-note:active:not(:disabled) {
  transform: rotate(0deg) translateY(-1px);
}

.action-note:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  transform: none;
}

.action-note--default {
  --rotate: -1deg;
  background: var(--note-btn-default-bg);
  color: var(--note-btn-default-text);
}

.action-note--default:hover {
  background: var(--note-btn-default-hover);
}

.action-note--primary {
  --rotate: 1deg;
  background: var(--note-btn-primary-bg);
  color: var(--note-btn-primary-text);
}

.action-note--primary:hover {
  background: var(--note-btn-primary-hover);
}

.action-note--success {
  --rotate: -0.5deg;
  background: var(--note-btn-success-bg);
  color: var(--note-btn-success-text);
}

.action-note--success:hover {
  background: var(--note-btn-success-hover);
}

/* 书签栏 */
.bookmark-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding-top: var(--space-8);
  margin-left: -18px;
  max-height: 600px;
  transform: rotate(-0.5deg);
  padding-right: var(--space-6);
  overflow: hidden auto;
}

.bookmark {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 84px;
  max-width: 140px;
  min-height: 72px;
  padding: var(--space-2) var(--space-6);
  background: var(--bookmark-bg);
  color: var(--bookmark-text);
  border-radius: 0 4px 4px 0;
  cursor: pointer;
  box-shadow: 2px 2px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease, background-color 0.2s ease;
  clip-path: polygon(0 0, 100% 0, calc(100% - 8px) 50%, 100% 100%, 0 100%);
  animation: bookmark-fade-in 0.4s ease-out backwards;
}

.bookmark:hover {
  transform: translateX(4px);
  background: var(--bookmark-hover-bg);
}

.bookmark.active {
  background: var(--bookmark-active-bg);
  color: var(--bookmark-active-text);
  transform: translateX(8px);
  box-shadow: 3px 4px 10px rgba(0, 0, 0, 0.18);
  z-index: 1;
}

.bookmark-title {
  font-size: 13px;
  font-weight: 600;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.bookmark-count {
  font-size: 11px;
  opacity: 0.85;
  margin-top: 2px;
}

/* 空状态 */
.empty-paper {
  width: 100%;
  display: flex;
  justify-content: center;
}

.empty-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  max-width: 520px;
  padding: var(--space-10) var(--space-8);
}

.empty-card .paper-title {
  margin-bottom: var(--space-3);
}

.empty-card .paper-meta {
  margin-bottom: var(--space-6);
}

/* 纸张落入动画 */
@keyframes paper-drop-in {
  from {
    opacity: 0;
    transform: rotate(-2.5deg) translateY(40px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: rotate(-0.5deg) translateY(0) scale(1);
  }
}

/* 书签淡入动画 */
@keyframes bookmark-fade-in {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 编辑态切换动画 */
.preview-slide-enter-active {
  transition: all 0.45s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.preview-slide-leave-active {
  transition: all 0.3s ease-in;
}

.preview-slide-enter-from {
  opacity: 0;
  transform: translateX(40px) scale(0.98);
}

.preview-slide-leave-to {
  opacity: 0;
  transform: translateX(-30px) scale(0.98);
}

/* 响应式 */
@media (max-width: 768px) {
  .task-view {
    flex-direction: column;
    align-items: center;
  }

  .paper-card {
    width: 100%;
    padding: var(--space-6) var(--space-5) var(--space-6) 48px;
  }

  .bookmark-list {
    flex-direction: row;
    margin-left: 0;
    margin-top: var(--space-4);
    padding-top: 0;
    max-width: 100%;
    max-height: none;
    overflow-x: auto;
    overflow-y: hidden;
  }

  .bookmark {
    min-width: 90px;
    max-width: 140px;
    min-height: 52px;
    border-radius: 4px 4px 0 0;
    clip-path: none;
  }

  .bookmark.active {
    transform: translateY(-4px);
  }

  .item-list-header {
    grid-template-columns: 1fr 80px 70px;
  }

  .item-row {
    grid-template-columns: 20px 1fr 80px 70px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .paper-card {
    padding: var(--space-5) var(--space-4);
  }

  .paper-title {
    font-size: 22px;
  }

  .paper-meta-row {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-1);
  }

  .item-list-header {
    display: none;
  }

  .item-row {
    grid-template-columns: 20px 1fr;
    gap: var(--space-1) var(--space-2);
  }

  .item-num,
  .item-category {
    grid-column: 2;
  }

  .paper-actions {
    flex-direction: column;
    gap: var(--space-3);
  }

  .action-note {
    width: 100%;
  }
}

/* =========================================================
 * 结算表单样式
 * ========================================================= */
.settle-view {
  align-items: stretch;
}

.settle-card {
  min-width: 640px;
  max-width: 800px;
}

.settle-body {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.settle-item {
  background: var(--card-bg);
  border: 1px dashed var(--border-color);
  border-radius: var(--radius-md);
  padding: var(--space-4);
  transition: opacity 0.25s ease;
}

.settle-item.is-skipped {
  opacity: 0.5;
}

.settle-item-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  min-height: 32px;
}

.settle-item-header .item-name {
  font-weight: 600;
  font-size: 16px;
  color: var(--text-primary);
  flex: 1;
}

.settle-item-header .item-num {
  font-size: 14px;
  color: var(--text-secondary);
}

.settle-item-fields {
  margin-top: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.field-row {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: var(--space-4);
}

.field {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  flex: 1;
  min-width: 120px;
}

.field--num,
.field--date {
  flex: 0 0 auto;
}

.field label {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 500;
}

@media (max-width: 768px) {
  .settle-card {
    min-width: auto;
    width: 100%;
  }

  .field-row {
    flex-direction: column;
    align-items: stretch;
  }

  .field--num,
  .field--date {
    flex: 1;
  }
}
</style>
