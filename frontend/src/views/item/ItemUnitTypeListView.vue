<template>
  <div class="unit-type-list-container">
    <!-- 页面标题栏 -->
    <div class="page-header">
      <h2 class="page-title">单位分类一览</h2>
      <CustomButton type="primary" @click="handleCreate">
        <i class="iconfont icon-add-box"/>
        新建单位分类
      </CustomButton>
    </div>

    <!-- 单位类型列表 -->
    <div v-loading="loading" class="unit-type-list-wrapper">
      <!-- 自定义单位类型 -->
      <div class="unit-type-section">
        <h3 class="section-title">我的单位分类</h3>
        <el-empty
            v-if="!loading && customUnitTypes.length === 0"
            description="您还没有创建自定义单位分类"
        >
          <CustomButton type="primary" @click="handleCreate">立即创建</CustomButton>
        </el-empty>
        <div v-else class="unit-type-grid">
          <div
              v-for="unitType in customUnitTypes"
              :key="unitType.id"
              class="unit-type-card"
              @click="handleExpand(unitType)"
          >
            <div class="card-header">
              <div class="unit-type-icon">
                <i class="iconfont icon-inbox-all"/>
              </div>
              <div class="unit-type-info">
                <h3 class="unit-type-name">{{ unitType.unitTypeName }}</h3>
                <p class="unit-type-meta">
                  <span class="unit-count">{{ getUnitCount(unitType.id) }} 个单位</span>
                  <span class="unit-type-label">自定义分类</span>
                </p>
              </div>
            </div>

            <div class="card-actions">
              <CustomButton type="primary" size="small" @click.stop="handleEdit(unitType)">
                <i class="iconfont icon-edit-box"/>
                编辑
              </CustomButton>
              <CustomButton type="danger" size="small" @click.stop="handleDelete(unitType)">
                <i class="iconfont icon-delete"/>
                删除
              </CustomButton>
            </div>
          </div>
        </div>
      </div>

      <!-- 系统默认单位类型 -->
      <div class="unit-type-section unit-type-section--system" v-if="systemUnitTypes.length > 0">
        <div class="section-header" @click="isSystemCollapsed = !isSystemCollapsed">
          <h3 class="section-title">系统默认单位分类</h3>
          <i class="iconfont icon-chevron-down toggle-icon" :class="{ 'is-collapsed': isSystemCollapsed }"/>
        </div>
        <el-collapse-transition>
          <div v-show="!isSystemCollapsed" class="unit-type-grid">
            <div
                v-for="unitType in systemUnitTypes"
                :key="unitType.id"
                class="unit-type-card unit-type-card--system"
                @click="handleExpand(unitType)"
            >
              <div class="system-badge">
                <i class="iconfont icon-bookmark"/>
                系统默认
              </div>
              <div class="card-header">
                <div class="unit-type-icon">
                  <i class="iconfont icon-inbox-full"/>
                </div>
                <div class="unit-type-info">
                  <h3 class="unit-type-name">{{ unitType.unitTypeName }}</h3>
                  <p class="unit-type-meta">
                    <span class="unit-count">{{ getUnitCount(unitType.id) }} 个单位</span>
                    <span class="unit-type-label">系统预设，不可编辑</span>
                  </p>
                </div>
              </div>
              <div class="card-actions">
                <!-- 系统默认分类仅支持查看展开，无操作按钮 -->
              </div>
            </div>
          </div>
        </el-collapse-transition>
      </div>
    </div>

    <!-- 删除确认对话框 -->
    <ConfirmDialog
        v-model:visible="showDeleteDialog"
        title="删除单位分类"
        :message="`确定要删除单位分类「${selectedUnitType?.unitTypeName || ''}」吗？删除后无法恢复，且会影响使用该分类下单位的物品。`"
        confirm-text="确定删除"
        cancel-text="取消"
        @confirm="confirmDelete"
        width="450px"
    />

    <!-- 创建单位分类对话框 -->
    <InputDialog
        v-model:visible="showCreateDialog"
        title="创建单位分类"
        label="分类名称"
        placeholder="请输入单位分类名称，如：重量、容量、数量"
        icon="icon-inbox"
        value-prop="typeName"
        confirm-text="创建分类"
        :loading="createLoading"
        @submit="handleCreateSubmit"
    />

    <!-- 编辑单位分类对话框 -->
    <InputDialog
        v-model:visible="showEditDialog"
        title="编辑单位分类"
        label="分类名称"
        placeholder="请输入单位分类名称"
        icon="icon-inbox"
        value-prop="unitTypeName"
        confirm-text="确认修改"
        :data="selectedUnitType"
        :loading="editLoading"
        @submit="handleEditSubmit"
    />

    <!-- 展开单位分类对话框 -->
    <UnitListDialog
        v-model:visible="showExpandDialog"
        :unit-type="selectedUnitType"
        :unit-list="filteredUnitList"
        @success="handleExpandSuccess"
    />
    <ItemUnitTypeTour ref="tourRef"/>
  </div>
</template>

<script setup>
import ItemUnitTypeTour from '@/components/tour/ItemUnitTypeTour.vue'
import {useTourStore, TOUR_SCENES} from '@/stores/tour'
import {onMounted, ref, computed, watch} from 'vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import InputDialog from '@/components/ui/InputDialog.vue'
import UnitListDialog from '@/components/unit/UnitListDialog.vue'
import showMessage from '@/utils/message'
import {listUnitTypes, listItemUnits, deleteUnitType, updateUnitType, createUnitType} from '@/api/item'
import CustomButton from '@/components/ui/CustomButton.vue'

// 加载状态
const loading = ref(false)

// 单位类型列表
const unitTypeList = ref([])

// 单位列表
const unitList = ref([])

// 系统默认分类折叠状态（默认折叠）
const isSystemCollapsed = ref(true)

// 自定义单位类型
const customUnitTypes = computed(() =>
    unitTypeList.value
        .filter(t => !t.isSystemDefault)
        .sort((a, b) => a.id - b.id)
)

// 系统默认单位类型
const systemUnitTypes = computed(() =>
    unitTypeList.value
        .filter(t => t.isSystemDefault)
        .sort((a, b) => a.id - b.id)
)

// 对话框控制
const showDeleteDialog = ref(false)
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const showExpandDialog = ref(false)
const selectedUnitType = ref(null)
const createLoading = ref(false)
const editLoading = ref(false)

// 获取该单位类型下的单位数量
const getUnitCount = (unitTypeId) => {
  return unitList.value.filter(u => u.unitTypeId === unitTypeId).length
}

// 过滤后的单位列表（用于展开对话框）
const filteredUnitList = computed(() => {
  if (!selectedUnitType.value) return []
  return unitList.value.filter(u => u.unitTypeId === selectedUnitType.value.id)
})

// 获取单位类型列表
const fetchUnitTypeList = async () => {
  loading.value = true
  try {
    const res = await listUnitTypes()
    if (res.code === 200 && Array.isArray(res.data)) {
      unitTypeList.value = res.data
    } else {
      unitTypeList.value = []
    }
  } catch (error) {
    console.error('获取单位分类列表失败:', error)
    showMessage.error('获取单位分类列表失败')
    unitTypeList.value = []
  } finally {
    loading.value = false
  }
}

// 获取单位列表
const fetchUnitList = async () => {
  try {
    const res = await listItemUnits()
    if (res.code === 200 && Array.isArray(res.data)) {
      unitList.value = res.data
    } else {
      unitList.value = []
    }
  } catch (error) {
    console.error('获取单位列表失败:', error)
    unitList.value = []
  }
}

// 创建单位类型
const handleCreate = () => {
  showCreateDialog.value = true
}

// 创建提交
const handleCreateSubmit = async ({value}) => {
  createLoading.value = true
  try {
    const res = await createUnitType({
      typeName: value.trim()
    })
    if (res.code === 200) {
      showMessage.success('创建成功')
      showCreateDialog.value = false
      await fetchUnitTypeList()
    } else {
      showMessage.error(res.message || '创建失败')
    }
  } catch (error) {
    console.error('创建单位分类失败:', error)
    showMessage.error('创建失败')
  } finally {
    createLoading.value = false
  }
}

// 展开单位类型
const handleExpand = (unitType) => {
  selectedUnitType.value = unitType
  showExpandDialog.value = true
}

// 编辑单位类型
const handleEdit = (unitType) => {
  if (unitType.isSystemDefault) {
    showMessage.warning('系统默认单位分类不可编辑')
    return
  }
  selectedUnitType.value = unitType
  showEditDialog.value = true
}

// 编辑提交
const handleEditSubmit = async ({id, value}) => {
  editLoading.value = true
  try {
    const res = await updateUnitType({
      id,
      typeName: value
    })
    if (res.code === 200) {
      showMessage.success('修改成功')
      showEditDialog.value = false
      await fetchUnitTypeList()
    } else {
      showMessage.error(res.message || '修改失败')
    }
  } catch (error) {
    console.error('修改单位分类失败:', error)
    showMessage.error('修改失败')
  } finally {
    editLoading.value = false
  }
}

// 展开成功回调（单位增删后刷新）
const handleExpandSuccess = () => {
  fetchUnitList()
}

// 删除确认
const handleDelete = (unitType) => {
  if (unitType.isSystemDefault) {
    showMessage.warning('系统默认单位分类不可删除')
    return
  }
  selectedUnitType.value = unitType
  showDeleteDialog.value = true
}

// 确认删除
const confirmDelete = async () => {
  if (!selectedUnitType.value) return
  try {
    const res = await deleteUnitType(selectedUnitType.value.id)
    if (res.code === 200) {
      showMessage.success('删除成功')
      await fetchUnitTypeList()
    } else {
      showMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    console.error('删除单位分类失败:', error)
    showMessage.error('删除失败')
  } finally {
    showDeleteDialog.value = false
    selectedUnitType.value = null
  }
}

onMounted(() => {
  fetchUnitTypeList()
  fetchUnitList()
})
// 页面引导
const tourRef = ref(null)
const tourStore = useTourStore()

watch(() => tourStore.pendingStartScene, (scene) => {
  if (scene === TOUR_SCENES.ITEM_UNIT_TYPE) {
    tourRef.value?.start()
  }
})
</script>

<style scoped lang="scss">
.unit-type-list-container {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  animation: fade-in-up 0.6s ease-out;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-6);
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.unit-type-list-wrapper {
  min-height: 400px;
  padding-bottom: var(--space-6);
}

.unit-type-section + .unit-type-section {
  margin-top: var(--space-8);
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-4);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  user-select: none;
  padding: var(--space-3) var(--space-4);
  margin: 0 calc(-1 * var(--space-4)) var(--space-4);
  border-radius: var(--radius-md);
  transition: background-color 0.2s ease;
}

.section-header:hover {
  background-color: var(--gray-30);
}

.section-header .section-title {
  margin: 0;
}

.toggle-icon {
  font-size: 18px;
  color: var(--text-secondary);
  transition: transform 0.3s ease;
}

.toggle-icon.is-collapsed {
  transform: rotate(0deg);
}

.toggle-icon:not(.is-collapsed) {
  transform: rotate(180deg);
}

.unit-type-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-5);
}

.unit-type-card {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--gray-40);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 140px;
  cursor: pointer;
}

.unit-type-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-card-hover);
}

.unit-type-card--system {
  border: 2px solid var(--badge-silver);
  background: linear-gradient(135deg, var(--badge-silver-bg) 0%, var(--glass-bg) 60%);
  box-shadow: 0 4px 20px var(--badge-silver-shadow);
}

.unit-type-card--system:hover {
  box-shadow: 0 8px 28px var(--badge-silver-shadow-hover);
  border-color: var(--badge-silver-hover);
}

.system-badge {
  position: absolute;
  top: 0;
  right: 0;
  background: linear-gradient(135deg, var(--badge-silver) 0%, var(--badge-silver-hover) 100%);
  color: var(--badge-silver-text-dark);
  font-size: 11px;
  font-weight: 600;
  padding: 4px 10px;
  border-bottom-left-radius: 10px;
  display: flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 2px 8px var(--badge-silver-shadow);
  z-index: 1;
}

.system-badge .iconfont {
  font-size: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

.unit-type-icon {
  min-width: 48px;
  min-height: 48px;
  background: var(--primary-light);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.unit-type-icon .iconfont {
  font-size: 22px;
  color: var(--primary-color);
}

.unit-type-card--system .unit-type-icon {
  background: linear-gradient(135deg, var(--badge-silver-light) 0%, var(--badge-silver-accent) 100%);
}

.unit-type-card--system .unit-type-icon .iconfont {
  color: var(--badge-silver-icon);
}

.unit-type-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
  overflow: hidden;
}

.unit-type-name {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin: 0;
}

.unit-type-card--system .unit-type-name {
  color: var(--badge-silver-text);
}

.unit-type-meta {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 13px;
  color: var(--text-tertiary);
  margin: 0;
}

.unit-count {
  background: var(--primary-light);
  color: var(--primary-color);
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  white-space: nowrap;
  flex-shrink: 0;
}

.unit-type-label {
  color: var(--text-tertiary);
}

.card-actions {
  display: flex;
  gap: 8px;
  margin-top: var(--space-2);
}

.card-actions .custom-button {
  flex: 1;
  justify-content: center;
}

.card-actions .custom-button .iconfont {
  margin-right: 4px;
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-title {
    font-size: 22px;
  }

  .unit-type-grid {
    grid-template-columns: 1fr;
  }
}

/* 小屏幕适配 */
@media (max-width: 480px) {
  .page-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .card-actions {
    flex-wrap: wrap;
  }
}
</style>
