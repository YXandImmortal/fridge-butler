<template>
  <div class="purchase-plan-page">
    <Transition name="preview-slide" mode="out-in">
      <div v-if="!showPreview" key="creator" class="purchase-plan-creator">
          <!-- 页面标题 -->
          <div class="page-header">
            <div class="page-title-wrapper">
              <div class="page-title-icon">
                <i class="iconfont icon-cart"/>
              </div>
              <div>
                <h1 class="page-title text-gradient-primary">采购助手</h1>
                <p class="page-subtitle">选择创建方式，开启智能采购</p>
              </div>
            </div>
          </div>

          <!-- 冰箱选择器 -->
          <div class="fridge-select-wrapper">
            <label class="fridge-select-label">为这台冰箱创建采购计划</label>
            <CustomSelect
                v-model="selectedFridgeId"
                :options="fridgeOptions"
                placeholder="请选择要采购的冰箱"
                :empty-text="loadingFridges ? '冰箱列表加载中...' : '暂无可用冰箱，请先创建冰箱'"
                size="large"
                full-width
                :disabled="loadingFridges"
            />
          </div>

          <!-- 胶囊 Tab 切换 -->
          <CustomSegmented
              v-model="activeTab"
              :options="tabs"
              size="large"
              @change="handleTabChange"
          />

          <!-- Tab 内容区 -->
          <div class="tab-content">
            <Transition name="tab-switch" mode="out-in">
              <!-- 新建计划 Tab -->
              <template v-if="activeTab === 'create'">
              <div class="create-plan-content">
                <!-- 我的模板 -->
                <div v-if="templateList.length > 0" v-loading="loadingTemplates" class="template-panel">
                  <h3 class="section-title">
                    <i class="iconfont icon-cart"/>
                    我的模板
                  </h3>
                  <div class="template-grid">
                    <div
                        v-for="template in templateList"
                        :key="template.id"
                        class="template-card"
                        @click="handleUseTemplate(template)"
                    >
                      <button
                          type="button"
                          class="template-delete-btn"
                          title="删除模板"
                          @click.stop="handleDeleteTemplate(template)"
                      >
                        <i class="iconfont icon-trash"/>
                      </button>
                      <div class="template-icon">
                        <i class="iconfont icon-cart"/>
                      </div>
                      <div class="template-info">
                        <h4 class="template-name">{{ template.templateName }}</h4>
                        <p class="template-desc">{{ template.sceneDesc || '暂无描述' }}</p>
                        <span class="template-meta">{{ template.itemCount ?? template.items?.length ?? 0 }} 件物品</span>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 空模板提示 -->
                <div v-else class="template-empty-tip">
                  <i class="iconfont icon-cart template-empty-icon"/>
                  <h4 class="template-empty-title">从这里创建采购计划</h4>
                  <p class="template-empty-desc">
                    还没有保存的采购计划模板。点击下方按钮即可新建采购计划，<br>
                    也可以点击上方的「日常采购」或「特别采购」切换不同的创建方式。<br>
                    完成计划后，可以将其保存为模板，方便下次一键复用。
                  </p>
                </div>

                <!-- 新建采购计划按钮（在卡片范围之外） -->
                <CustomButton type="primary" size="large" @click="handleCreatePlan">
                  <i class="iconfont icon-add-box"/>
                  新建采购计划
                </CustomButton>
              </div>
            </template>

            <!-- 日常采购 Tab -->
            <template v-else-if="activeTab === 'daily'">
              <div class="daily-plan-content">
                <div class="daily-plan-intro">
                  <i class="iconfont icon-trending daily-plan-icon"/>
                  <h3 class="daily-plan-title">让 AI 为你规划日常采购</h3>
                  <p class="daily-plan-desc">
                    AI 采购助手会综合分析您的出入库记录、冰箱容量等日常数据，<br>
                    智能推荐需要补充的物品，并一键生成完整采购计划。
                  </p>
                </div>
                <LogoButton
                    type="primary"
                    size="large"
                    :loading="dailyLoading"
                    loadingText="生成中..."
                    @click="handleAIGenerateDaily"
                >
                  AI助手智能创建
                </LogoButton>
              </div>
            </template>

            <!-- 特别采购 Tab -->
            <template v-else>
              <div class="special-plan-content">
                <div class="special-plan-intro">
                  <i class="iconfont icon-item special-plan-icon"/>
                  <h3 class="special-plan-title">为特别场景生成采购计划</h3>
                  <p class="special-plan-desc">
                    选择一个场景模板，或输入具体需求，<br>
                    AI 会根据场景为你生成专属的采购清单。
                  </p>
                </div>

                <!-- 特殊场景模板按钮 -->
                <div v-loading="loadingSceneTemplates" class="special-template-section">
                  <p class="special-template-label">使用预设模板快速创建</p>
                  <div class="special-template-list">
                    <CustomButton
                        v-for="template in specialSceneTemplates"
                        :key="template.code"
                        :type="selectedSpecialTemplate === template.code ? 'primary' : 'default'"
                        size="small"
                        @click="handleSelectSpecialTemplate(template)"
                    >
                      {{ template.name }}
                    </CustomButton>
                  </div>
                </div>

                <!-- 预计人数 -->
                <div class="special-people-row">
                  <span class="special-people-label">预计人数</span>
                  <CustomInputNumber
                      v-model="specialEstimatedPeople"
                      class="special-people-input"
                      :min="1"
                      :max="99"
                      :step="1"
                      placeholder="人数"
                  />
                </div>

                <!-- 场景描述输入 -->
                <div class="special-textarea-wrapper">
                  <CustomInput
                      v-model="specialSceneDesc"
                      type="textarea"
                      :rows="4"
                      placeholder="描述一下具体场合，例如：准备生日派对、亲友聚餐、周末火锅等"
                  />
                </div>

                <!-- AI 生成按钮 -->
                <LogoButton
                    type="primary"
                    size="large"
                    :loading="specialLoading"
                    loadingText="AI思考中...请耐心等待"
                    @click="handleAIGenerateSpecial"
                >
                  AI助手智能创建
                </LogoButton>
              </div>
            </template>
            </Transition>
          </div>
      </div>

      <!-- 采购计划预览区 -->
      <PurchasePlanPreview
          v-else
          key="preview"
          v-model:plan-name="previewPlanName"
          v-model:items="previewItems"
          :loading="previewLoading"
          :fridge-name="previewFridgeName"
          :date-text="previewDateText"
          :source="previewSource"
          :scene-desc="specialSceneDesc"
          :tips="previewTips"
          @return="resetPreview"
          @confirm="handlePreviewConfirm"
          @template-deleted="handleTemplateDeleted"
      />
    </Transition>

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

    <!-- 数据不足提示 -->
    <InsufficientDataDialog
        v-model:visible="showInsufficientDialog"
        title="数据不足"
        :message="insufficientReason"
        @manual-create="handleManualCreateAfterInsufficient"
    />
    <PurchasePlanTour ref="tourRef" :mode="tourMode"/>
  </div>
</template>

<script setup>
import {computed, nextTick, onMounted, ref, watch} from 'vue'
import {useRouter} from 'vue-router'
import {useTourStore, TOUR_SCENES} from '@/stores/tour'
import PurchasePlanTour from '@/components/tour/PurchasePlanTour.vue'
import CustomSegmented from '@/components/ui/CustomSegmented.vue'
import CustomSelect from '@/components/ui/CustomSelect.vue'
import CustomButton from '@/components/ui/CustomButton.vue'
import CustomInput from '@/components/ui/CustomInput.vue'
import CustomInputNumber from '@/components/ui/CustomInputNumber.vue'
import LogoButton from '@/components/brand/LogoButton.vue'
import InsufficientDataDialog from '@/components/ui/InsufficientDataDialog.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import PurchasePlanPreview from '@/components/purchase/PurchasePlanPreview.vue'
import showMessage from '@/utils/message'
import notifyGamificationResult from '@/utils/gamificationNotify'
import {listMyTemplates, listSceneTemplates, purchaseRecommend, createPurchasePlan, getTemplateDetail, deletePurchaseTemplate} from '@/api/purchase'
import {listMyFridges} from '@/api/fridge'

const router = useRouter()

const tabs = [
  {key: 'create', value: 'create', label: '新建计划', icon: 'icon-add-box'},
  {key: 'daily', value: 'daily', label: '日常采购', icon: 'icon-trending'},
  {key: 'special', value: 'special', label: '特别采购', icon: 'icon-item'}
]

const activeTab = ref('create')
const currentTabName = computed(() => tabs.find(t => t.value === activeTab.value)?.label || '')

// 加载状态
const loadingTemplates = ref(false)
const loadingSceneTemplates = ref(false)
const loadingFridges = ref(false)
const previewLoading = ref(false)
const dailyLoading = ref(false)
const specialLoading = ref(false)

// 冰箱列表与当前选中冰箱
const fridgeList = ref([])
const fridgeOptions = computed(() => fridgeList.value.map(f => ({label: f.fridgeName, value: f.id})))
const selectedFridgeId = ref(null)

// 我的模板列表（后端限制最多 10 个）
const templateList = ref([])

// 特别采购表单
const selectedSpecialTemplate = ref('')
const specialSceneDesc = ref('')
const specialEstimatedPeople = ref(2)

// 采购计划预览区
const showPreview = ref(false)
const previewPlanName = ref('新建采购计划')
const previewItems = ref([])
const previewSource = ref('')
const previewDateText = ref('')
const previewTips = ref([])
const insufficientReason = ref('')
const showInsufficientDialog = ref(false)
const showDeleteTemplateDialog = ref(false)
const deletingTemplate = ref(null)

const previewFridgeName = computed(() => {
  const fridge = fridgeList.value.find(f => f.id === selectedFridgeId.value)
  return fridge ? fridge.fridgeName : ''
})

// 特殊场景模板列表
const specialSceneTemplates = ref([])

const handleTabChange = (value) => {
  if (value === 'create' && templateList.value.length === 0) {
    fetchMyTemplates()
  }
  if (value === 'special' && specialSceneTemplates.value.length === 0) {
    fetchSceneTemplates()
  }
}

const ensureFridgeSelected = () => {
  if (!selectedFridgeId.value) {
    showMessage.warning('请先选择要采购的冰箱')
    return false
  }
  return true
}

const formatDate = (date) => {
  const y = date.getFullYear()
  const m = date.getMonth() + 1
  const d = date.getDate()
  return `${y}年${m}月${d}日`
}

const stripTrailingZeros = (value) => {
  if (value === null || value === undefined || value === '') return '1'
  const num = Number(value)
  if (Number.isNaN(num)) return '1'
  return String(num)
}

const openPreview = (source, items = [], tips = []) => {
  previewSource.value = source
  previewDateText.value = formatDate(new Date())
  previewItems.value = items
  previewTips.value = tips
  showPreview.value = true
}

const resetPreview = () => {
  showPreview.value = false
  previewLoading.value = false
  previewPlanName.value = '新建采购计划'
  previewItems.value = []
  previewSource.value = ''
  previewDateText.value = ''
  previewTips.value = []
  insufficientReason.value = ''
  // 重置特别采购表单
  selectedSpecialTemplate.value = ''
  specialSceneDesc.value = ''
  specialEstimatedPeople.value = 2
}

const handleManualCreateAfterInsufficient = () => {
  showInsufficientDialog.value = false
  previewPlanName.value = '新建采购计划'
  openPreview('MANUAL_CREATE', [])
}

const normalizeRecommendItems = (items = []) => items.map((it, idx) => ({
  id: `ai-${idx}`,
  itemName: it.itemName,
  plannedNum: stripTrailingZeros(it.plannedNum),
  unitName: it.unitName || '',
  categoryName: it.categoryName || '',
  reason: it.reason || '',
  categoryId: it.categoryId ?? null,
  itemUnitId: it.unitId ?? null,
  storeInFridge: it.storeInFridge ?? true
}))

const normalizePlanItems = (items = []) => items.map((it, idx) => ({
  id: it.id ?? `tpl-${idx}`,
  itemName: it.itemName,
  plannedNum: stripTrailingZeros(it.plannedNum),
  unitName: it.itemUnitName || '',
  categoryName: it.categoryName || '',
  reason: it.reason || '',
  categoryId: it.categoryId ?? null,
  itemUnitId: it.itemUnitId ?? null,
  storeInFridge: it.storeInFridge ?? true
}))

const handleCreatePlan = () => {
  if (!ensureFridgeSelected()) return
  previewPlanName.value = '新建采购计划'
  openPreview('MANUAL_CREATE', [])
}

const handleTemplateDeleted = (id) => {
  templateList.value = templateList.value.filter(t => t.id !== id)
}

const handleUseTemplate = async (template) => {
  if (!ensureFridgeSelected()) return
  previewLoading.value = true
  try {
    const res = await getTemplateDetail(template.id)
    if (res.code === 200 && res.data) {
      previewPlanName.value = res.data.templateName || '模板采购计划'
      openPreview('TEMPLATE', normalizePlanItems(res.data.items))
    } else {
      showMessage.error(res.message || '获取模板详情失败')
    }
  } catch (error) {
    console.error('获取模板详情失败:', error)
    showMessage.error('获取模板详情失败')
  } finally {
    previewLoading.value = false
  }
}

const handleAIGenerateDaily = async () => {
  if (!ensureFridgeSelected()) return
  dailyLoading.value = true
  previewLoading.value = true
  try {
    const res = await purchaseRecommend({fridgeId: selectedFridgeId.value, mode: 'daily'})
    if (res.code === 200 && res.data) {
      if (!res.data.sufficientData) {
        insufficientReason.value = res.data.insufficientReason || '冰箱数据不足，无法生成日常采购推荐，请多使用一段时间后再试。'
        showInsufficientDialog.value = true
        return
      }
      if (hasRecommendFallback(res.data)) {
        showMessage.warning(res.data.insufficientReason)
        return
      }
      previewPlanName.value = res.data.planName || '日常采购计划'
      openPreview('DAILY_RECOMMEND', normalizeRecommendItems(res.data.items), res.data.tips || [])
    } else {
      showMessage.error(res.message || '生成推荐失败')
    }
  } catch (error) {
    console.error('日常采购推荐失败:', error)
    showMessage.error('日常采购推荐失败')
  } finally {
    dailyLoading.value = false
    previewLoading.value = false
  }
}

const handleAIGenerateSpecial = async () => {
  if (!ensureFridgeSelected()) return
  if (!selectedSpecialTemplate.value && !specialSceneDesc.value.trim()) {
    showMessage.warning('请选择场景模板或输入场景描述')
    return
  }
  specialLoading.value = true
  previewLoading.value = true
  try {
    const res = await purchaseRecommend({
      fridgeId: selectedFridgeId.value,
      mode: 'special',
      sceneTemplate: selectedSpecialTemplate.value || undefined,
      sceneDesc: specialSceneDesc.value.trim() || undefined,
      estimatedPeople: specialEstimatedPeople.value
    })
    if (res.code === 200 && res.data) {
      if (hasRecommendFallback(res.data)) {
        showMessage.warning(res.data.insufficientReason)
        return
      }
      previewPlanName.value = res.data.planName || '特别采购计划'
      openPreview('SPECIAL_GENERATE', normalizeRecommendItems(res.data.items), res.data.tips || [])
    } else {
      showMessage.error(res.message || '生成场景采购计划失败')
    }
  } catch (error) {
    console.error('特别采购生成失败:', error)
    showMessage.error('特别采购生成失败')
  } finally {
    specialLoading.value = false
    previewLoading.value = false
  }
}

const handlePreviewConfirm = async () => {
  if (!previewPlanName.value.trim()) {
    showMessage.warning('请输入计划名称')
    return
  }
  const validItems = previewItems.value.filter(i => i.itemName.trim() && Number(i.plannedNum) > 0)
  if (validItems.length === 0) {
    showMessage.warning('请至少添加一件有效物品')
    return
  }
  const payload = {
    fridgeId: selectedFridgeId.value,
    planName: previewPlanName.value.trim(),
    sceneDesc: previewSource.value === 'SPECIAL_GENERATE' ? specialSceneDesc.value.trim() : undefined,
    items: validItems.map(i => ({
      itemName: i.itemName.trim(),
      categoryId: i.categoryId,
      plannedNum: String(i.plannedNum),
      itemUnitId: i.itemUnitId,
      storeInFridge: i.storeInFridge ?? true
    }))
  }
  try {
    const res = await createPurchasePlan(payload)
    if (res.code === 200) {
      notifyGamificationResult(res, '创建采购计划')
      showMessage.success('采购计划创建成功')
      const planId = res.data?.id
      if (planId) {
        await router.push(`/purchase/tasks?active=${planId}`)
      } else {
        await router.push('/purchase/tasks')
      }
    } else {
      showMessage.error(res.message || '创建采购计划失败')
    }
  } catch (error) {
    console.error('创建采购计划失败:', error)
    showMessage.error('创建采购计划失败')
  }
}

// 获取我的模板列表
const fetchMyTemplates = async () => {
  loadingTemplates.value = true
  try {
    const res = await listMyTemplates()
    if (res.code === 200 && Array.isArray(res.data)) {
      templateList.value = res.data
    } else {
      templateList.value = []
    }
  } catch (error) {
    console.error('获取采购计划模板失败:', error)
    showMessage.error('获取采购计划模板失败')
    templateList.value = []
  } finally {
    loadingTemplates.value = false
  }
}

// 获取特殊场景模板列表
const fetchSceneTemplates = async () => {
  loadingSceneTemplates.value = true
  try {
    const res = await listSceneTemplates()
    if (res.code === 200 && Array.isArray(res.data)) {
      specialSceneTemplates.value = res.data
    } else {
      specialSceneTemplates.value = []
    }
  } catch (error) {
    console.error('获取特殊场景模板失败:', error)
    showMessage.error('获取特殊场景模板失败')
    specialSceneTemplates.value = []
  } finally {
    loadingSceneTemplates.value = false
  }
}

// 获取当前用户冰箱列表，并默认选中默认冰箱（无默认则选第一个）
const fetchFridgeList = async () => {
  loadingFridges.value = true
  try {
    const res = await listMyFridges()
    if (res.code === 200 && Array.isArray(res.data)) {
      fridgeList.value = res.data
      if (fridgeList.value.length > 0) {
        const defaultFridge = fridgeList.value.find(f => f.isDefault)
        selectedFridgeId.value = defaultFridge ? defaultFridge.id : fridgeList.value[0].id
      }
    } else {
      fridgeList.value = []
    }
  } catch (error) {
    console.error('获取冰箱列表失败:', error)
    showMessage.error('获取冰箱列表失败')
    fridgeList.value = []
  } finally {
    loadingFridges.value = false
  }
}

onMounted(() => {
  fetchMyTemplates()
  fetchSceneTemplates()
  fetchFridgeList()
})

// 页面引导
const tourRef = ref(null)
const tourStore = useTourStore()
const tourMode = ref('creator')

watch(() => tourStore.pendingStartScene, (scene) => {
  if (scene === TOUR_SCENES.PURCHASE_PLAN) {
    // 根据当前状态决定展示创建态还是预览态的指引
    tourMode.value = showPreview.value ? 'preview' : 'creator'
    nextTick(() => {
      tourRef.value?.start()
    })
  }
})

const handleSelectSpecialTemplate = (template) => {
  selectedSpecialTemplate.value = selectedSpecialTemplate.value === template.code ? '' : template.code
}

const hasRecommendFallback = (data) => {
  return data && Boolean(data.insufficientReason) && (!data.items || data.items.length === 0)
}

const handleDeleteTemplate = (template) => {
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
</script>

<style scoped lang="scss">
/* =========================================================
 * 采购助手页面样式（制定计划页）
 * ========================================================= */
.purchase-plan-page {
  animation: purchase-page-in 0.5s ease forwards;
}

@keyframes purchase-page-in {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 主页面 ↔ 预览页切换动画 */
.preview-slide-enter-active {
  transition: all 0.45s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.preview-slide-leave-active {
  transition: all 0.35s ease-in;
}

.preview-slide-enter-from {
  opacity: 0;
  transform: translateX(60px) rotate(2deg) scale(0.98);
}

.preview-slide-leave-to {
  opacity: 0;
  transform: translateX(-40px) rotate(-1deg) scale(0.98);
}

.purchase-plan-creator {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-8);
  width: 100%;
  max-width: 900px;
  min-height: inherit;
  margin: var(--space-6) auto 0;
}

.fridge-select-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  width: 260px;
  max-width: 100%;
}

.fridge-select-label {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.4;
}

.page-header {
  display: flex;
  justify-content: center;
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
  @include flex-center;

  .iconfont {
    font-size: 32px;
    color: var(--primary-color);
  }
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

/* Tab 内容区 */
.tab-content {
  width: 100%;
  min-height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  padding: 0 var(--space-10);
}

/* Tab 内容切换过渡：淡入淡出 + 微上移 */
.tab-switch-enter-active,
.tab-switch-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.tab-switch-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.tab-switch-leave-to {
  opacity: 0;
  transform: translateY(-12px);
}

/* =========================================================
 * 新建计划 Tab 内容
 * ========================================================= */
.create-plan-content {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-6);
}

/* 空模板提示 */
.template-empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: var(--space-3);
  padding-bottom: var(--space-8);
  max-width: 480px;
}

.template-empty-icon {
  font-size: 48px;
  color: var(--primary-30);
}

.template-empty-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.template-empty-desc {
  font-size: 14px;
  line-height: 1.7;
  color: var(--text-tertiary);
  margin: 0;
}

/* =========================================================
 * 日常采购 Tab 内容
 * ========================================================= */
.daily-plan-content {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-6);
}

.daily-plan-intro {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: var(--space-3);
  max-width: 520px;
  padding-bottom: var(--space-8);
}

.daily-plan-icon {
  font-size: 48px;
  color: var(--primary-30);
}

.daily-plan-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.daily-plan-desc {
  font-size: 14px;
  line-height: 1.7;
  color: var(--text-tertiary);
  margin: 0;
}

/* =========================================================
 * 特别采购 Tab 内容
 * ========================================================= */
.special-plan-content {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-5);
  padding-bottom: var(--space-8);
}

.special-plan-intro {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: var(--space-3);
  max-width: 520px;
}

.special-plan-icon {
  font-size: 48px;
  color: var(--primary-30);
}

.special-plan-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.special-plan-desc {
  font-size: 14px;
  line-height: 1.7;
  color: var(--text-tertiary);
  margin: 0;
}

/* 特殊场景模板按钮列表 */
.special-template-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  width: 100%;
  max-width: 600px;
}

.special-template-label {
  font-size: 13px;
  color: var(--text-tertiary);
  margin: 0;
}

.special-template-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: var(--space-3);
  width: 100%;
}

/* 场景描述输入框 */
.special-people-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  width: 100%;
  max-width: 520px;
}

.special-people-label {
  font-size: 14px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.special-people-input {
  width: 140px;
}

.special-textarea-wrapper {
  width: 100%;
  max-width: 520px;
}

.template-panel {
  @include glass-card;
  width: 100%;
  padding: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.section-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;

  .iconfont {
    color: var(--primary-color);
    font-size: 22px;
  }
}

/* 模板网格：2 行 5 列 */
.template-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  grid-template-rows: repeat(2, auto);
  gap: var(--space-4);
}

/* 模板卡片 —— 参考冰箱列表卡片 */
.template-card {
  position: relative;
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: var(--space-3);
  max-width: 140px;
}

.template-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-card-hover);
  border-color: var(--primary-30);
}

.template-delete-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 26px;
  height: 26px;
  @include flex-center;
  border-radius: 50%;
  border: none;
  background: transparent;
  color: var(--text-tertiary);
  opacity: 0;
  cursor: pointer;
  transition: all 0.2s ease;
  z-index: 1;

  .iconfont {
    font-size: 14px;
  }

  .template-card:hover &,
  .template-card:focus-within & {
    opacity: 1;
  }

  &:hover {
    background: var(--danger-light);
    color: var(--danger-color);
  }
}

.template-icon {
  width: 48px;
  height: 48px;
  min-width: 48px;
  min-height: 48px;
  background: var(--primary-light);
  border-radius: var(--radius-md);
  @include flex-center;

  .iconfont {
    font-size: 24px;
    color: var(--primary-color);
  }
}

.template-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  width: 100%;
  min-width: 0;
}

.template-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.template-desc {
  font-size: 12px;
  color: var(--text-tertiary);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.template-meta {
  font-size: 12px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}



/* 响应式：平板 */
@media (max-width: 768px) {
  .demo-sidebar-placeholder {
    width: var(--sidebar-width-md);
  }

  .demo-layout__content {
    margin-left: var(--sidebar-width-md);
    padding: var(--space-4);
  }

  .purchase-plan-creator {
    gap: var(--space-6);
  }

  .page-title {
    font-size: 28px;
  }

  .page-title-icon {
    width: 48px;
    height: 48px;

    .iconfont {
      font-size: 28px;
    }
  }

  .tab-content {
    min-height: 280px;
  }

  .tab-panel,
  .template-panel {
    padding: var(--space-6);
  }

  .template-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

/* 响应式：手机 */
@media (max-width: 480px) {
  .preview-slide-enter-from {
    transform: translateX(30px) rotate(2deg) scale(0.98);
  }

  .preview-slide-leave-to {
    transform: translateX(-20px) rotate(-1deg) scale(0.98);
  }

  .demo-sidebar-placeholder {
    display: none;
  }

  .demo-layout__content {
    margin-left: 0;
    padding: var(--space-4);
  }

  .purchase-plan-creator {
    gap: var(--space-6);
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-3);
  }

  .page-title-wrapper {
    flex-direction: column;
    gap: var(--space-3);
    text-align: center;
  }

  .page-title {
    font-size: 24px;
  }

  .tab-content {
    min-height: 240px;
  }

  .tab-panel,
  .template-panel {
    padding: var(--space-5);
  }

  .template-grid {
    grid-template-columns: repeat(2, 1fr);
  }

}

</style>
