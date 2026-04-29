<template>
  <div class="index-container">
    <!-- 头部组件 -->
    <Header @show-logout-dialog="showLogoutDialog = true" />

    <!-- 主体内容区域 -->
    <div class="main-content-wrapper">
      <!-- 左侧导航栏 -->
      <Sidebar />

      <!-- 主内容区域 -->
      <main class="main-content">
        <div class="item-manage-container">
          <!-- 返回栏 -->
          <div class="back-bar">
            <CustomButton type="link" @click="handleBack">
              <i class="iconfont icon-arrow-left" />
              返回冰箱详情
            </CustomButton>
            <CustomButton type="link" @click="openSelectFridgeDialog">
              <i class="iconfont icon-switch" />
              切换冰箱
            </CustomButton>
          </div>

          <!-- 单卡片布局 -->
          <div class="cards-wrapper">
            <!-- 左侧卡片：物品搜索与展示 -->
            <div class="card search-card">
              <div class="card-header">
                <div class="card-title-wrapper">
                  <i class="iconfont icon-search-list card-title-icon" />
                  <h3 class="card-title">物品列表</h3>
                </div>
                <span v-if="!itemLoading" class="card-subtitle">
                  共 {{ itemList.length }} 件物品
                </span>
                <el-tooltip :disabled="hasFridgeId" content="请选择冰箱" placement="top">
                  <CustomButton type="primary" :disabled="!hasFridgeId" @click="showCreateDialog = true">
                    添加物品
                  </CustomButton>
                </el-tooltip>
              </div>

              <!-- 搜索区域 -->
              <div class="search-section">
                <SearchBar
                  v-model="searchForm.keyword"
                  placeholder="搜索物品名称"
                  @search="handleSearch"
                  @clear="handleReset"
                >
                  <SortControl
                    v-model:field="sortField"
                    v-model:order="sortOrder"
                    :field-options="sortFieldOptions"
                    @change="handleSortChange"
                  />
                  <CustomSelect
                    v-model="searchForm.categoryId"
                    placeholder="全部分类"
                    clearable
                    grid
                    class="filter-select"
                    :options="categoryOptions"
                    @change="handleSortChange"
                  />
                  <CustomSelect
                    v-model="searchForm.unitTypeId"
                    placeholder="全部单位类型"
                    clearable
                    grid
                    class="filter-select"
                    :options="unitTypeOptions"
                    @change="handleSortChange"
                  />
                  <CustomSelect
                    v-model="searchForm.unitId"
                    placeholder="全部单位"
                    clearable
                    grid
                    dropdown-align="right"
                    class="filter-select"
                    :disabled="!searchForm.unitTypeId"
                    :options="unitOptions"
                    @change="handleSortChange"
                  />
                  <CustomButton @click="handleReset">
                    重置
                  </CustomButton>
                </SearchBar>
              </div>

              <!-- 列表区域 -->
              <div v-loading="itemLoading" class="list-section">
                <!-- 空状态 -->
                <el-empty
                  v-if="!itemLoading && itemList.length === 0"
                  description="暂无物品"
                  class="item-empty"
                />

                <!-- 物品表格列表 -->
                <div v-else class="item-table-wrapper">
                  <el-table
                      max-height="520px"
                    :data="itemList"
                    class="item-table"
                    :header-cell-style="{ background: 'var(--gray-20)', color: 'var(--text-primary)', fontWeight: 600 }"
                  >
                    <el-table-column label="物品名称" min-width="140" show-overflow-tooltip>
                      <template #default="{ row }">
                        <div class="item-name-cell">
                          <div class="item-icon-sm">
                            <i class="iconfont icon-item" />
                          </div>
                          <div class="item-name-info">
                            <span class="item-name-text">{{ row.itemName }}</span>
                            <el-tag
                                v-if="row.categoryName"
                                size="small"
                                :effect="themeStore.theme === 'dark' ? 'dark' : 'light'"
                                color="var(--primary-light)"
                                type="info"
                                class="item-category-tag">
                              {{ row.categoryName }}
                            </el-tag>
                            <span v-else class="item-category-none">未分类</span>
                          </div>
                        </div>
                      </template>
                    </el-table-column>

                    <el-table-column label="数量" width="120" align="center">
                      <template #default="{ row }">
                        <span class="quantity-value-table">{{ row.itemNum }}</span>
                        <span class="quantity-unit-table">{{ row.unitName }}</span>
                      </template>
                    </el-table-column>

                    <el-table-column label="生产日期" width="120" align="center">
                      <template #default="{ row }">
                        <span class="date-text">{{ row.productionDate || '-' }}</span>
                      </template>
                    </el-table-column>

                    <el-table-column label="保质期" width="100" align="center">
                      <template #default="{ row }">
                        <span v-if="row.shelfLifeDays" class="shelf-life-tag">
                          {{ row.shelfLifeDays }} 天
                        </span>
                        <span v-else class="date-text">-</span>
                      </template>
                    </el-table-column>

                    <el-table-column label="入库时间" width="120" align="center">
                      <template #default="{ row }">
                        <span class="date-text">{{ row.storedDate || '-' }}</span>
                      </template>
                    </el-table-column>

                    <el-table-column label="备注" min-width="140" show-overflow-tooltip>
                      <template #default="{ row }">
                        <span class="remark-text">{{ row.remark || '-' }}</span>
                      </template>
                    </el-table-column>

                    <el-table-column label="操作" width="120" align="center" fixed="right">
                      <template #default="{ row }">
                        <div class="action-btns">
                          <el-tooltip content="编辑" placement="top">
                            <CustomButton type="link" size="small" @click="handleEditItem(row)">
                              <i class="iconfont icon-edit" />
                            </CustomButton>
                          </el-tooltip>
                          <el-tooltip content="删除" placement="top">
                            <CustomButton type="link" size="small" class="danger-link" @click="handleDeleteItem(row)">
                              <i class="iconfont icon-delete" />
                            </CustomButton>
                          </el-tooltip>
                        </div>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </div>
            </div>


          </div>
        </div>
      </main>
    </div>

    <!-- 底部版权信息 -->
    <CopyrightFooter />

    <!-- 选择冰箱对话框 -->
    <ConfirmDialog
      v-model:visible="showSelectFridgeDialog"
      v-model:select-value="selectedFridgeId"
      title="选择冰箱"
      message="请选择一个冰箱："
      confirm-text="确定"
      cancel-text="取消"
      type="select"
      :persistent="true"
      :show-close="false"
      width="420px"
      :options="fridgeList"
      option-label="fridgeName"
      option-value="id"
      select-placeholder="请选择冰箱"
      :select-loading="fridgeListLoading"
      @confirm="handleSelectFridgeConfirm"
      @cancel="handleSelectFridgeCancel"
    />

    <!-- 添加物品弹窗 -->
    <ItemCreateDialog
      v-model:visible="showCreateDialog"
      :category-list="categoryList"
      :unit-list="unitList"
      :unit-type-list="unitTypeList"
      :fridge-id="currentFridgeId"
      @success="fetchItems"
    />

    <!-- 登出确认对话框 -->
    <ConfirmDialog
      v-model:visible="showLogoutDialog"
      title="退出登录"
      message="您确定要退出登录吗？"
      confirm-text="确定"
      cancel-text="取消"
      @confirm="handleLogout"
    />
  </div>
</template>


<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Header from '@/components/Header.vue'
import Sidebar from '@/components/Sidebar.vue'
import CopyrightFooter from '@/components/CopyrightFooter.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import showMessage from '@/utils/message'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import {
  listItemCategories,
  listItemUnits,
  listUnitTypes,
  searchItems
} from '@/api/item'
import { listMyFridges, getDefaultFridge } from '@/api/fridge'
import CustomButton from '@/components/CustomButton.vue'
import CustomSelect from '@/components/CustomSelect.vue'
import SortControl from '@/components/SortControl.vue'
import SearchBar from '@/components/SearchBar.vue'
import ItemCreateDialog from '@/components/item/ItemCreateDialog.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()
const { logout } = userStore

// ==================== 数据加载状态 ====================
const categoryLoading = ref(false)
const unitLoading = ref(false)
const unitTypeLoading = ref(false)
const itemLoading = ref(false)

// ==================== 基础数据列表 ====================
const categoryList = ref([])
const unitList = ref([])
const unitTypeList = ref([])
const itemList = ref([])

// ==================== 搜索表单 ====================
const searchForm = reactive({
  keyword: '',
  categoryId: '',
  unitTypeId: '',
  unitId: ''
})

const categoryOptions = computed(() => {
  return categoryList.value.map(cat => ({ label: cat.categoryName, value: cat.id }))
})

const unitTypeOptions = computed(() => {
  return unitTypeList.value.map(t => ({ label: t.unitTypeName, value: t.id }))
})

const unitOptions = computed(() => {
  if (!searchForm.unitTypeId) return []
  return unitList.value
    .filter(u => u.unitTypeId === searchForm.unitTypeId)
    .map(u => ({ label: u.unitName, value: u.id }))
})

const sortFieldOptions = [
  { label: '入库时间', value: 'storedDate' },
  { label: '数量', value: 'itemNum' }
]

const sortField = ref('storedDate')
const sortOrder = ref('desc')

// ==================== 冰箱ID检查 ====================
const hasFridgeId = computed(() => {
  return !!(route.params.id || route.query.fridgeId)
})

const currentFridgeId = computed(() => {
  const id = route.params.id || route.query.fridgeId
  return id ? Number(id) : null
})

// ==================== 对话框控制 ====================
const showLogoutDialog = ref(false)
const showCreateDialog = ref(false)
const showSelectFridgeDialog = ref(false)

// 冰箱选择列表
const fridgeList = ref([])
const selectedFridgeId = ref(null)
const fridgeListLoading = ref(false)

// 打开选择冰箱对话框
const openSelectFridgeDialog = async () => {
  fridgeListLoading.value = true
  try {
    const res = await listMyFridges()
    if (res.code === 200 && res.data) {
      fridgeList.value = res.data || []
    } else {
      fridgeList.value = []
    }
  } catch (error) {
    console.error('获取冰箱列表失败:', error)
    fridgeList.value = []
  } finally {
    fridgeListLoading.value = false
  }
  selectedFridgeId.value = null
  showSelectFridgeDialog.value = true
}

// 确认选择冰箱
const handleSelectFridgeConfirm = () => {
  if (selectedFridgeId.value) {
    showSelectFridgeDialog.value = false
    router.push({
      name: 'fridge-items',
      params: { id: selectedFridgeId.value }
    })
  } else {
    showMessage.warning('请选择一个冰箱')
  }
}

// 取消选择冰箱
const handleSelectFridgeCancel = () => {
  showSelectFridgeDialog.value = false
  showMessage.error('冰箱ID不能为空')
}

// ==================== 数据获取 ====================

// 获取分类列表
const fetchCategories = async () => {
  categoryLoading.value = true
  try {
    const res = await listItemCategories()
    if (res.code === 200 && Array.isArray(res.data)) {
      categoryList.value = res.data
    } else {
      categoryList.value = []
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
    showMessage.error('获取分类列表失败')
    categoryList.value = []
  } finally {
    categoryLoading.value = false
  }
}

// 获取单位列表
const fetchUnits = async () => {
  unitLoading.value = true
  try {
    const res = await listItemUnits()
    if (res.code === 200 && Array.isArray(res.data)) {
      unitList.value = res.data
    } else {
      unitList.value = []
    }
  } catch (error) {
    console.error('获取单位列表失败:', error)
    showMessage.error('获取单位列表失败')
    unitList.value = []
  } finally {
    unitLoading.value = false
  }
}

// 获取单位类型列表
const fetchUnitTypes = async () => {
  unitTypeLoading.value = true
  try {
    const res = await listUnitTypes()
    if (res.code === 200 && Array.isArray(res.data)) {
      unitTypeList.value = res.data
    } else {
      unitTypeList.value = []
    }
  } catch (error) {
    console.error('获取单位类型列表失败:', error)
    showMessage.error('获取单位类型列表失败')
    unitTypeList.value = []
  } finally {
    unitTypeLoading.value = false
  }
}

// 搜索物品
const fetchItems = async () => {
  itemLoading.value = true
  try {
    const params = {}
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.categoryId) params.categoryId = searchForm.categoryId
    if (searchForm.unitTypeId) params.unitTypeId = searchForm.unitTypeId
    if (searchForm.unitId) params.unitId = searchForm.unitId
    // 从路由参数获取冰箱ID
    const fridgeId = route.params.id || route.query.fridgeId
    if (fridgeId) params.fridgeId = Number(fridgeId)
    if (sortField.value) {
      params.sortField = sortField.value
      params.sortOrder = sortOrder.value
    }

    const res = await searchItems(params)
    if (res.code === 200 && Array.isArray(res.data)) {
      itemList.value = res.data
    } else {
      itemList.value = []
    }
  } catch (error) {
    console.error('搜索物品失败:', error)
    showMessage.error('搜索物品失败')
    itemList.value = []
  } finally {
    itemLoading.value = false
  }
}

// ==================== 事件处理 ====================

// 搜索
const handleSearch = () => {
  fetchItems()
}

// 重置搜索
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.categoryId = ''
  searchForm.unitTypeId = ''
  searchForm.unitId = ''
  sortField.value = ''
  sortOrder.value = 'desc'
  fetchItems()
}

// 排序变化
const handleSortChange = () => {
  fetchItems()
}

// 编辑物品
const handleEditItem = (row) => {
  showMessage.info(`编辑物品：${row.itemName}（功能开发中）`)
}

// 删除物品
const handleDeleteItem = (row) => {
  showMessage.info(`删除物品：${row.itemName}（功能开发中）`)
}

// 返回冰箱详情
const handleBack = () => {
  const fridgeId = route.query.fridgeId
  if (fridgeId) {
    router.push({
      name: 'fridge-detail',
      params: { id: fridgeId }
    })
  } else {
    router.push({ name: 'fridge-list' })
  }
}

// 处理退出登录
const handleLogout = () => {
  logout()
  showLogoutDialog.value = false
  router.push('/login')
  showMessage.info('已退出登录')
}

// 解析当前冰箱ID，如果没有则尝试获取默认冰箱
const resolveFridgeId = async () => {
  let fridgeId = route.params.id || route.query.fridgeId
  if (!fridgeId) {
    try {
      const res = await getDefaultFridge()
      if (res.code === 200 && res.data) {
        fridgeId = res.data.id
        await router.replace({
          name: 'fridge-items',
          params: { id: fridgeId }
        })
      }
    } catch (error) {
      console.error('获取默认冰箱失败:', error)
    }
  }
  return fridgeId
}

// ==================== 生命周期 ====================
onMounted(async () => {
  const fridgeId = await resolveFridgeId()
  if (!fridgeId) {
    openSelectFridgeDialog()
    return
  }
  fetchCategories()
  fetchUnits()
  fetchUnitTypes()
  fetchItems()
})

// 监听单位类别变化，清空单位选择
watch(
  () => searchForm.unitTypeId,
  () => {
    searchForm.unitId = ''
  }
)

// 监听路由参数变化，切换冰箱时重新获取物品
watch(
  () => route.params.id,
  (newId, oldId) => {
    if (newId && newId !== oldId) {
      fetchItems()
    }
  }
)
</script>


<style scoped>
.index-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-content-wrapper {
  margin-top: var(--header-height);
}

.main-content {
  margin-left: var(--sidebar-width);
  transition: all 0.3s ease;
  max-height: calc(100vh - var(--header-height) - var(--footer-height));
  min-height: calc(100vh - var(--header-height) - var(--footer-height));
  background: var(--main-content-bg);
  padding: var(--space-5);
  overflow-y: auto;
}

.item-manage-container {
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  animation: fade-in-up 0.6s ease-out;
}

/* 返回栏 */
.back-bar {
  display: flex;
  margin-bottom: var(--space-5);
}

.back-bar .custom-button {
  font-size: 14px;
  color: var(--text-secondary);
}

.back-bar :deep(.iconfont) {
  margin-right: 4px;
  font-size: 12px;
}

/* 单卡片布局 */
.cards-wrapper {
  display: grid;
  grid-template-columns: 1fr;
  gap: var(--space-6);
  align-items: start;
}

/* 通用卡片样式 */
.card {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-5);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--gray-40);
}

.card-title-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-title-icon {
  font-size: 22px;
  color: var(--primary-color);
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.card-subtitle {
  font-size: 13px;
  color: var(--text-tertiary);
}

/* 搜索区域 */
.search-section {
  margin-bottom: var(--space-5);
}

/* 列表区域 */
.list-section {
  min-height: 300px;
}

.item-empty {
  padding: var(--space-10) 0;
}

/* 物品表格 */
.item-table-wrapper {
  overflow: hidden;

}

.item-table {
  --el-table-border-color: var(--gray-40);
  --el-table-row-hover-bg-color: var(--primary-10);
  width: 100%;
  border-radius: var(--radius-md);
}

.item-table :deep(.el-table__cell) {
  padding: 12px 0;
}

/* 物品名称单元格 */
.item-name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.item-icon-sm {
  width: 32px;
  height: 32px;
  background: var(--primary-light);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.item-icon-sm .iconfont {
  font-size: 16px;
  color: var(--primary-color);
}

.item-name-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.item-name-text {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-category-tag {
  width: fit-content;
}

.item-category-none {
  font-size: 12px;
  color: var(--text-tertiary);
}

/* 数量 */
.quantity-value-table {
  font-size: 15px;
  font-weight: 700;
  color: var(--primary-color);
}

.quantity-unit-table {
  font-size: 12px;
  color: var(--text-secondary);
  margin-left: 2px;
}

/* 日期文本 */
.date-text {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 保质期标签 */
.shelf-life-tag {
  font-size: 13px;
  color: var(--success-color);
  font-weight: 600;
  background: var(--success-light);
  padding: 2px var(--space-2);
  border-radius: 4px;
}

/* 备注 */
.remark-text {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 操作按钮 */
.action-btns {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.action-btns .custom-button {
  padding: 4px var(--space-2);
  font-size: 14px;
}

.action-btns .danger-link {
  color: var(--danger-color);
}

.action-btns .danger-link:hover {
  color: var(--danger-dark);
}

/* 响应式设计 */
@media (max-width: 1100px) {
  .cards-wrapper {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .main-content {
    margin-left: var(--sidebar-width-md);
    padding: var(--space-4);
  }

  .search-section :deep(.search-bar-wrapper) {
    flex-direction: column;
    align-items: stretch;
  }

  .search-section :deep(.search-input-group),
  .search-section :deep(.sort-control-wrapper) {
    width: 100%;
  }

  .search-section .filter-select {
    width: 100%;
  }

  .item-table-wrapper {
    overflow-x: auto;
  }
}

@media (max-width: 480px) {
  .main-content {
    margin-left: 0;
    padding: var(--space-3);
  }

  .card {
    padding: 16px;
  }

  .item-table-wrapper {
    border-radius: 8px;
  }
}
</style>
