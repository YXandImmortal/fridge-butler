<template>
  <div class="fridge-list-container">
    <!-- 页面标题栏 -->
    <div class="page-header">
      <h2 class="page-title">冰箱一览</h2>
      <CustomButton type="primary" @click="handleCreate" class="create-btn">
        新建冰箱
      </CustomButton>
    </div>

    <!-- 搜索栏 -->
    <SearchBar
      v-model="searchForm.keyword"
      placeholder="请输入搜索内容"
      @search="handleSearch"
      @clear="handleSearch"
      class="search-bar"
    >
      <SortControl
        v-model:field="searchForm.sortField"
        v-model:order="searchForm.sortOrder"
        :field-options="sortFieldOptions"
        @change="handleSearch"
      />
    </SearchBar>

    <!-- 冰箱列表 -->
    <div v-loading="loading" class="fridge-list-wrapper">
      <!-- 空状态 -->
      <el-empty
        v-if="!loading && fridgeList.length === 0"
        description="暂无冰箱，快去创建一个吧"
      >
        <CustomButton type="primary" @click="handleCreate">立即创建</CustomButton>
      </el-empty>

      <!-- 卡片列表 -->
      <div v-else class="fridge-grid">
        <div
          v-for="fridge in fridgeList"
          :key="fridge.id"
          class="fridge-card"
          :class="{ 'fridge-card--default': fridge.isDefault }"
          @click="handleViewDetail(fridge.id)"
        >
          <div v-if="fridge.isDefault" class="default-badge">
            <i class="iconfont icon-star-fill" />
            默认冰箱
          </div>
          <div class="card-header">
            <div class="fridge-icon">
              <i class="iconfont icon-fridge-line" />
            </div>
            <div class="fridge-basic-info">
              <h3 class="fridge-name">{{ fridge.fridgeName }}</h3>
              <p class="fridge-desc">{{ fridge.remark || '暂无描述' }}</p>
            </div>
          </div>

          <div class="fridge-meta">
            <span v-if="fridge.itemCount !== undefined" class="meta-item">
              <i class="iconfont icon-item" />
              {{ fridge.itemCount }} 件物品
            </span>
          </div>
          <div class="fridge-meta">
            <span class="meta-item">
              <i class="iconfont icon-calendar" />
              {{ formatDate(fridge.createTime) }}
            </span>
            <span v-if="fridge.totalCapacity === null" class="meta-item">
              未设置容量
            </span>
            <span v-else class="meta-item">
              容量 {{ fridge.totalCapacity }} L
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建冰箱对话框 -->
    <FridgeCreateDialog
      v-model:visible="showCreateDialog"
      :loading="createLoading"
      @submit="handleCreateSubmit"
    />

    <!-- 删除确认对话框 -->
    <ConfirmDialog
      v-model:visible="showDeleteDialog"
      title="删除冰箱"
      :message="`确定要删除冰箱「${selectedFridge?.fridgeName || ''}」吗？删除后可在后台恢复。`"
      confirm-text="确定删除"
      cancel-text="取消"
      @confirm="confirmDelete"
    />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import FridgeCreateDialog from '@/components/fridge/FridgeCreateDialog.vue'
import showMessage from '@/utils/message'
import { listMyFridges, deleteFridge, searchFridges, createFridge } from '@/api/fridge'
import CustomButton from "@/components/CustomButton.vue";
import SearchBar from "@/components/SearchBar.vue";
import SortControl from "@/components/SortControl.vue";

const router = useRouter()

// 加载状态
const loading = ref(false)

// 冰箱列表
const fridgeList = ref([])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  sortField: 'createTime',
  sortOrder: 'asc'
})

// 排序选项
const sortFieldOptions = [
  { label: '创建日期', value: 'createTime' },
  { label: '冰箱名称', value: 'name' },
  { label: '容量大小', value: 'totalCapacity' }
]

const sortOrderOptions = [
  { label: '升序', value: 'asc' },
  { label: '降序', value: 'desc' }
]

// 对话框控制
const showCreateDialog = ref(false)
const showDeleteDialog = ref(false)
const selectedFridge = ref(null)
const createLoading = ref(false)

// 获取冰箱列表
const fetchFridgeList = async () => {
  loading.value = true
  try {
    const res = await listMyFridges()
    if (res.code === 200 && Array.isArray(res.data)) {
      fridgeList.value = res.data
    } else {
      fridgeList.value = []
    }
  } catch (error) {
    console.error('获取冰箱列表失败:', error)
    showMessage.error('获取冰箱列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = async () => {
  try {
    const params = {
      keyword: searchForm.keyword,
      sortField: searchForm.sortField,
      sortOrder: searchForm.sortOrder
    }
    const res = await searchFridges(params)
    if (res.code === 200 && Array.isArray(res.data)) {
      fridgeList.value = res.data
    } else {
      fridgeList.value = []
    }
  } catch (error) {
    console.error('查询冰箱列表失败:', error)
    showMessage.error('查询冰箱列表失败')
  }
}

// 查看详情
const handleViewDetail = (id) => {
  router.push({
    name: 'fridge-detail',
    params: {
      id: id
    }
  })
}

// 创建冰箱
const handleCreate = () => {
  showCreateDialog.value = true
}

// 创建提交
const handleCreateSubmit = async ({ fridgeName, remark, fridgeAddress }) => {
  createLoading.value = true
  try {
    const res = await createFridge({
      fridgeName,
      remark,
      fridgeAddress
    })

    if (res.code === 200) {
      showMessage.success('冰箱创建成功')
      showCreateDialog.value = false
      await router.push({
        name: 'fridge-detail',
        params: { id: res.data ? res.data : '' }
      })
    } else {
      showMessage.error(res.message || '创建失败')
    }
  } catch (error) {
    console.error('创建冰箱失败:', error)
    showMessage.error('创建失败')
  } finally {
    createLoading.value = false
  }
}

// 删除确认
const handleDelete = (fridge) => {
  selectedFridge.value = fridge
  showDeleteDialog.value = true
}

// 确认删除
const confirmDelete = async () => {
  if (!selectedFridge.value) return
  try {
    const res = await deleteFridge(selectedFridge.value.id)
    if (res.code === 200) {
      showMessage.success('删除成功')
      await fetchFridgeList()
    } else {
      showMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    console.error('删除冰箱失败:', error)
    showMessage.error('删除失败')
  } finally {
    showDeleteDialog.value = false
    selectedFridge.value = null
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

onMounted(() => {
  fetchFridgeList()
})
</script>

<style scoped lang="scss">
.fridge-list-container {
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

.create-btn {
  border-radius: var(--radius-md);
  padding: 10px var(--space-5);
  font-weight: 200;
}

.create-btn :deep(.iconfont) {
  margin-right: 6px;
  font-size: 14px;
}

.search-bar {
  margin-bottom: var(--space-6);
  max-width: 700px;
}

.fridge-list-wrapper {
  min-height: 400px;
  padding-bottom: var(--space-6);
}

.fridge-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: var(--space-5);
}

.fridge-card {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--gray-40);
  cursor: pointer;
  transition: all 0.3s ease;
}

.fridge-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-card-hover);
}

.fridge-card--default {
  border: 2px solid var(--badge-gold);
  background: linear-gradient(135deg, var(--badge-gold-bg) 0%, var(--glass-bg) 60%);
  box-shadow: 0 4px 20px var(--badge-gold-shadow);
  position: relative;
  overflow: hidden;
}

.fridge-card--default:hover {
  box-shadow: 0 8px 28px var(--badge-gold-shadow-hover);
  border-color: var(--badge-gold-hover);
}

.fridge-card--default .fridge-icon {
  background: linear-gradient(135deg, var(--badge-gold-light) 0%, var(--badge-gold-accent) 100%);
}

.fridge-card--default .fridge-icon .iconfont {
  color: var(--badge-gold-icon);
}

.fridge-card--default .fridge-name {
  color: var(--badge-gold-text);
}

.default-badge {
  position: absolute;
  top: 0;
  right: 0;
  background: linear-gradient(135deg, var(--badge-gold) 0%, var(--badge-gold-hover) 100%);
  color: var(--badge-gold-text-dark);
  font-size: 11px;
  font-weight: 600;
  padding: 4px 10px;
  border-bottom-left-radius: 10px;
  display: flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 2px 8px var(--badge-gold-shadow);
  z-index: 1;
}

.default-badge .iconfont {
  font-size: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: var(--space-4);
  gap: var(--space-4);
}

.fridge-icon {
  min-width: 53px;
  min-height: 53px;
  background: var(--primary-light);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.fridge-icon .iconfont {
  font-size: 24px;
  color: var(--primary-color);
}

.fridge-actions {
  display: flex;
  gap: 8px;
}

.fridge-basic-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.fridge-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.fridge-desc {
  font-size: 14px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-height: 1.5em;
}

.fridge-meta {
  display: flex;
  gap: var(--space-4);
  flex-wrap: wrap;
  justify-content: flex-start;
}

.meta-item {
  font-size: 13px;
  color: var(--text-tertiary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-item .iconfont {
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-title {
    font-size: 22px;
  }

  .fridge-grid {
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

  .search-bar {
    max-width: 100%;
  }

  .search-bar :deep(.search-bar-wrapper) {
    flex-direction: column;
    align-items: stretch;
  }

  .search-bar :deep(.search-input-group) {
    width: 100%;
  }
}
</style>
